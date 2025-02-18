package ordertracker.core.services;

import java.util.List;
import ordertracker.core.models.Meal;


public interface MealService {
    List<Meal> getAllMeals();

    Meal getMealById(int id);

    Meal getMealByName(String name);
}