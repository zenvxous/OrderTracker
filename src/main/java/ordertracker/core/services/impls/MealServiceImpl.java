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
import ordertracker.core.models.Order;
import ordertracker.core.repositories.MealRepository;
import ordertracker.core.repositories.OrderRepository;
import ordertracker.core.services.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class MealServiceImpl implements MealService {

    private final OrderRepository orderRepository;
    private final MealRepository mealRepository;
    private final InMemoryCache<Integer, Meal> mealCache;
    private final InMemoryCache<Integer, Order> orderCache;
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);

    @Autowired
    public MealServiceImpl(
            OrderRepository orderRepository,
            MealRepository mealRepository,
            InMemoryCache<Integer, Meal> mealCache,
            InMemoryCache<Integer, Order> orderCache) {
        this.orderRepository = orderRepository;
        this.mealRepository = mealRepository;
        this.mealCache = mealCache;
        this.orderCache = orderCache;
        cacheCleaner.scheduleAtFixedRate(this.mealCache::clear, 30, 30, TimeUnit.MINUTES);
    }

    @Override
    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    @Override
    public Optional<Meal> getMealById(int id) {
        Meal cachedMeal = mealCache.get(id);
        if (cachedMeal != null) {
            return Optional.of(cachedMeal);
        }

        return mealRepository.findById(id).map(meal -> {
            mealCache.put(id, meal);
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
        mealCache.put(savedMeal.getId(), savedMeal);
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

        mealCache.put(id, updatedMeal);
        return updatedMeal;
    }

    @Override
    public void deleteMeal(int id) {
        var meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + id));
        var orders = orderRepository.findOrdersByMealId(id);

        for (var order : orders) {
            order.getMeals().remove(meal);
        }

        orderRepository.saveAll(orders);
        mealRepository.delete(meal);

        orderCache.clear();
        mealCache.evict(id);
    }
}