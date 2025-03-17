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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    static final String NOT_FOUND_MESSAGE = "Order not found with id: ";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MealRepository mealRepository;
    private final InMemoryCache<Integer, Order> cache;
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);

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
        cacheCleaner.scheduleAtFixedRate(cache::clear, 30, 30, TimeUnit.MINUTES);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(int id) {
        Order cachedOrder = cache.get(id);

        if (cachedOrder != null) {
            return Optional.of(cachedOrder);
        }

        Optional<Order> order = orderRepository.findById(id);
        order.ifPresent(o -> cache.put(id, o));
        return order;
    }

    @Override
    public Order addOrder(int customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        var order = new Order(null, customer, new ArrayList<>(), OrderStatus.ACCEPTED);
        Order savedOrder = orderRepository.save(order);

        cache.put(savedOrder.getId(), savedOrder);
        return savedOrder;
    }

    @Override
    public Order updateOrderStatus(int id, OrderStatus status) {
        var order = getOrderById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        cache.put(id, updatedOrder);
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

        cache.put(orderId, updatedOrder);
        return updatedOrder;
    }

    @Override
    public void deleteOrder(int id) {
        var order = getOrderById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
        orderRepository.delete(order);
        cache.evict(id);
    }
}