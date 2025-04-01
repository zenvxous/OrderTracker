package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    public OrderServiceImpl(
            OrderRepository repository,
            CustomerRepository customerRepository,
            MealRepository mealRepository) {
        this.orderRepository = repository;
        this.customerRepository = customerRepository;
        this.mealRepository = mealRepository;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(int id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order addOrder(int customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        var order = new Order(null, customer, new ArrayList<>(), OrderStatus.ACCEPTED);

        return orderRepository.save(order);
    }

    @Override
    public Order updateOrderStatus(int id, OrderStatus status) {
        var order = getOrderById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
        order.setStatus(status);

        return orderRepository.save(order);
    }

    @Override
    public Order addMealToOrder(int orderId, int mealId) {
        var order = getOrderById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + orderId));
        var meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + mealId));

        order.getMeals().add(meal);
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(int id) {
        var order = getOrderById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
        orderRepository.delete(order);
    }

    @Override
    public void deleteMealInOrder(int orderId, int mealId) {
        var order = getOrderById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + orderId));
        var meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + mealId));

        order.getMeals().remove(meal);
        orderRepository.save(order);
    }
}