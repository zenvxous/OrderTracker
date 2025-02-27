package ordertracker.core.repositories;

import java.util.Optional;
import ordertracker.core.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    Optional<Customer> findByName(String name);
}
