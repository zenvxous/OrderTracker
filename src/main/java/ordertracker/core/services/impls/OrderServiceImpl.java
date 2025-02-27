package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import ordertracker.core.models.Order;
import ordertracker.core.repositories.OrderRepository;
import ordertracker.core.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    @Autowired
    public OrderServiceImpl(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(int id) {
        return repository.findById(id);
    }

    @Override
    public Order addOrder(Order order) {
        return repository.save(order);
    }

    @Override
    public void deleteOrder(int id) {
        var order = getOrderById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        repository.delete(order);
    }
}
