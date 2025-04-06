package ordertracker.core.repositories;

import java.util.Optional;
import ordertracker.core.models.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRepository extends JpaRepository<Meal, Integer> {
    Optional<Meal> findByName(String name);
}
