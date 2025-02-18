package ordertracker.core.services.impls;

import java.util.List;
import ordertracker.core.models.Meal;
import ordertracker.core.services.MealService;
import org.springframework.stereotype.Service;


@Service
class MealServiceImpl implements MealService {
    private final List<Meal> meals = List.of(
            new Meal(0, "pizza", 100, 20),
            new Meal(1, "fyrki", 50, 10),
            new Meal(2, "sushi", 100, 30),
            new Meal(3, "pasta", 25, 20));

    @Override
    public List<Meal> getAllMeals() {
        return meals;
    }

    @Override
    public Meal getMealById(int id) {
        return meals.stream()
                .filter(meal -> meal.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Meal getMealByName(String name) {
        return meals.stream()
                .filter(meal -> meal.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
