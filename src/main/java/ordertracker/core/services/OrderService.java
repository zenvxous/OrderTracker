package ordertracker.core.services;

import java.util.List;
import java.util.Optional;
import ordertracker.core.models.Order;

public interface OrderService {
    List<Order> getAllOrders();

    Optional<Order> getOrderById(int id);

    Order addOrder(Order order);

    void deleteOrder(int id);
}
