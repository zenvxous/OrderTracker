package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import ordertracker.core.models.Meal;
import ordertracker.core.repositories.MealRepository;
import ordertracker.core.services.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;

    @Autowired
    public MealServiceImpl(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    @Override
    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    @Override
    public Optional<Meal> getMealById(int id) {
        return mealRepository.findById(id);
    }

    @Override
    public Optional<Meal> getMealByName(String name) {
        return mealRepository.findByName(name);
    }

    @Override
    public Meal addMeal(@Valid Meal meal) {
        return mealRepository.save(meal);
    }

    @Override
    public Meal updateMeal(int id, Meal mealDetails) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + id));

        meal.setName(mealDetails.getName());
        meal.setCookingTime(mealDetails.getCookingTime());
        meal.setPrice(mealDetails.getPrice());
        return mealRepository.save(meal);
    }

    @Override
    public void deleteMeal(int id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + id));
        mealRepository.delete(meal);
    }
}
