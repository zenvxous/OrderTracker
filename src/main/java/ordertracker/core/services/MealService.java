package ordertracker.core.services;

import java.util.List;
import java.util.Optional;
import ordertracker.core.models.Meal;


public interface MealService {
    List<Meal> getAllMeals();

    Optional<Meal> getMealById(int id);

    Optional<Meal> getMealByName(String name);

    Meal addMeal(Meal meal);

    Meal updateMeal(int id, Meal mealDetails);

    void deleteMeal(int id);
}