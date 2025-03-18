package ordertracker.core.services;

import java.util.List;
import java.util.Optional;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Order;

public interface OrderService {
    List<Order> getAllOrders();

    Optional<Order> getOrderById(int id);

    Order addOrder(int customerId);

    Order updateOrderStatus(int id, OrderStatus status);

    Order addMealToOrder(int orderId, int mealId);

    void deleteOrder(int id);

    void deleteMealInOrder(int orderId, int mealId);
}
