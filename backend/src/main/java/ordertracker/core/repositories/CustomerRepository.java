package ordertracker.core.repositories;

import java.util.List;
import java.util.Optional;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    Optional<Customer> findByName(String name);

    @Query("SELECT c FROM Customer c JOIN c.orders o JOIN o.meals m WHERE o.status = :status AND m.name = :mealName")
    List<Customer> findCustomersByOrderStatusAndMealName(
            OrderStatus status,
            String mealName);
}
