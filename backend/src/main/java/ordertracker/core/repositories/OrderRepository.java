package ordertracker.core.repositories;

import java.util.List;
import ordertracker.core.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o FROM Order o JOIN o.meals m WHERE m.id = :mealId")
    List<Order> findOrdersByMealId(@Param("mealId") Integer mealId);
}
