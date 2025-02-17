package ordertracker.api.controllers;

import java.util.List;
import ordertracker.core.models.Meal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/meals")
public class MealController {
    private List<Meal> meals = List.of(
            new Meal(0, "pizza", 100, 20),
            new Meal(1, "fyrki", 50, 10),
            new Meal(2, "sushi", 100, 30),
            new Meal(3, "pasta", 25, 20));

    @GetMapping
    public List<Meal> getAllMeals() {
        return meals;
    }

    @GetMapping("/{id}")
    public Meal getMealById(@PathVariable int id) {
        return meals.stream()
                .filter(meal -> meal.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/by-name")
    public Meal getMealByName(@RequestParam String name) {
        return meals.stream()
                .filter(meal -> meal.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
