package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ordertracker.apllication.cache.InMemoryCache;
import ordertracker.core.models.Meal;
import ordertracker.core.repositories.MealRepository;
import ordertracker.core.services.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final InMemoryCache<Integer, Meal> cache;
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);

    @Autowired
    public MealServiceImpl(
            MealRepository mealRepository,
            InMemoryCache<Integer, Meal> mealCache) {
        this.mealRepository = mealRepository;
        this.cache = mealCache;
        cacheCleaner.scheduleAtFixedRate(cache::clear, 30, 30, TimeUnit.MINUTES);
    }

    @Override
    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    @Override
    public Optional<Meal> getMealById(int id) {
        Meal cachedMeal = cache.get(id);
        if (cachedMeal != null) {
            return Optional.of(cachedMeal);
        }

        return mealRepository.findById(id).map(meal -> {
            cache.put(id, meal);
            return meal;
        });
    }

    @Override
    public Optional<Meal> getMealByName(String name) {
        return mealRepository.findByName(name);
    }

    @Override
    public Meal addMeal(@Valid Meal meal) {
        Meal savedMeal = mealRepository.save(meal);
        cache.put(savedMeal.getId(), savedMeal);
        return savedMeal;
    }

    @Override
    public Meal updateMeal(int id, Meal mealDetails) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + id));

        meal.setName(mealDetails.getName());
        meal.setCookingTime(mealDetails.getCookingTime());
        meal.setPrice(mealDetails.getPrice());

        Meal updatedMeal = mealRepository.save(meal);

        cache.put(id, updatedMeal);
        return updatedMeal;
    }

    @Override
    public void deleteMeal(int id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + id));

        mealRepository.delete(meal);
        cache.evict(id);
    }
}