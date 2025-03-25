package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ordertracker.apllication.cache.InMemoryCache;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Order;
import ordertracker.core.repositories.CustomerRepository;
import ordertracker.core.repositories.MealRepository;
import ordertracker.core.repositories.OrderRepository;
import ordertracker.core.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    static final String NOT_FOUND_MESSAGE = "Order not found with id: ";
    static final String ADDED_TO_CACHE_MESSAGE = "Order added to cache: ";
    static final String UPDATED_IN_CACHE_MESSAGE = "Order updated in cache: ";
    static final String EVICTED_FROM_CACHE_MESSAGE = "Order evicted from cache: ";
    private static final long MAX_CACHE_MEMORY_BYTES = 100L * 1024 * 1024; // 100MB limit

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MealRepository mealRepository;
    private final InMemoryCache<Integer, Order> cache;
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    private long currentCacheMemoryUsage = 0;

    @Autowired
    public OrderServiceImpl(
            OrderRepository repository,
            CustomerRepository customerRepository,
            MealRepository mealRepository,
            InMemoryCache<Integer, Order> orderCache) {
        this.orderRepository = repository;
        this.customerRepository = customerRepository;
        this.mealRepository = mealRepository;
        this.cache = orderCache;

        cacheCleaner.scheduleAtFixedRate(() -> {
            logger.info("Performing cache maintenance");
            checkCacheMemoryUsage();
        }, 30, 30, TimeUnit.MINUTES);
    }

    private synchronized void checkCacheMemoryUsage() {
        if (currentCacheMemoryUsage > MAX_CACHE_MEMORY_BYTES) {
            logger.warn("Cache memory limit exceeded ({} bytes), clearing cache", currentCacheMemoryUsage);
            cache.clear();
            currentCacheMemoryUsage = 0;
        } else {
            logger.info("Current cache memory usage: {}/{} bytes",
                    currentCacheMemoryUsage, MAX_CACHE_MEMORY_BYTES);
        }
    }

    private synchronized void updateMemoryUsage(Order order, boolean add) {
        long orderSize = estimateObjectSize(order);
        if (add) {
            currentCacheMemoryUsage += orderSize;
        } else {
            currentCacheMemoryUsage -= orderSize;
            if (currentCacheMemoryUsage < 0) {
                currentCacheMemoryUsage = 0;
            }
        }
    }

    private long estimateObjectSize(Order order) {
        long baseSize = 100;
        if (order.getCustomer() != null) {
            baseSize += 50;
        }
        if (order.getMeals() != null) {
            baseSize += 50 + order.getMeals().size() * 30L;
        }
        return baseSize;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(int id) {
        Order cachedOrder = cache.get(id);

        if (cachedOrder != null) {
            logger.info("Order retrieved from cache: {}", id);
            return Optional.of(cachedOrder);
        }

        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            logger.info("Order added to cache: {}", id);
            putOrderInCache(id, order.get());
        }
        return order;
    }

    private void putOrderInCache(int id, Order order) {
        if (currentCacheMemoryUsage + estimateObjectSize(order) > MAX_CACHE_MEMORY_BYTES) {
            logger.warn("Cannot cache order {} - memory limit would be exceeded", id);
            return;
        }
        cache.put(id, order);
        updateMemoryUsage(order, true);
    }

    @Override
    public Order addOrder(int customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        var order = new Order(null, customer, new ArrayList<>(), OrderStatus.ACCEPTED);
        Order savedOrder = orderRepository.save(order);

        logger.info("{}{}", ADDED_TO_CACHE_MESSAGE, savedOrder.getId());
        putOrderInCache(savedOrder.getId(), savedOrder);
        return savedOrder;
    }

    @Override
    public Order updateOrderStatus(int id, OrderStatus status) {
        var order = getOrderById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        logger.info("{} {}", UPDATED_IN_CACHE_MESSAGE, id);
        putOrderInCache(id, updatedOrder);

        return updatedOrder;
    }

    @Override
    public Order addMealToOrder(int orderId, int mealId) {
        var order = getOrderById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + orderId));
        var meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + mealId));

        order.getMeals().add(meal);
        Order updatedOrder = orderRepository.save(order);

        logger.info("{} {}", UPDATED_IN_CACHE_MESSAGE, orderId);
        putOrderInCache(orderId, updatedOrder);
        return updatedOrder;
    }

    @Override
    public void deleteOrder(int id) {
        var order = getOrderById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
        orderRepository.delete(order);
        logger.info("{}{}", EVICTED_FROM_CACHE_MESSAGE, id);
        evictOrderFromCache(id, order);
    }

    private void evictOrderFromCache(int id, Order order) {
        cache.evict(id);
        updateMemoryUsage(order, false);
    }

    @Override
    public void deleteMealInOrder(int orderId, int mealId) {
        var order = getOrderById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + orderId));
        var meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + mealId));

        order.getMeals().remove(meal);
        orderRepository.save(order);
        logger.info("{}{}", UPDATED_IN_CACHE_MESSAGE, orderId);
        evictOrderFromCache(orderId, order);
    }
}