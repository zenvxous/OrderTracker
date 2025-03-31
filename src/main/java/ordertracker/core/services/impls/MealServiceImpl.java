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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MealServiceImpl implements MealService {

    private static final Logger logger = LoggerFactory.getLogger(MealServiceImpl.class);
    private static final long MAX_CACHE_MEMORY_BYTES = 100L * 1024 * 1024; // 100MB limit
    private static final String NOT_FOUND_MESSAGE = "Meal not found with id: ";
    private static final String ADDED_TO_CACHE_MESSAGE = "Meal added to cache: ";

    private final OrderRepository orderRepository;
    private final MealRepository mealRepository;
    private final InMemoryCache<Integer, Meal> mealCache;
    private final InMemoryCache<Integer, Order> orderCache;
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    private long currentCacheMemoryUsage = 0;

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
        cacheCleaner.scheduleAtFixedRate(() -> {
            logger.info("Clearing the meal cache");
            mealCache.clear();
            currentCacheMemoryUsage = 0;
        }, 30, 30, TimeUnit.MINUTES);
    }

    private synchronized void updateMemoryUsage(Meal meal, boolean add) {
        long mealSize = estimateObjectSize(meal);
        if (add) {
            currentCacheMemoryUsage += mealSize;
        } else {
            currentCacheMemoryUsage -= mealSize;
            if (currentCacheMemoryUsage < 0) {
                currentCacheMemoryUsage = 0;
            }
        }
    }

    private long estimateObjectSize(Meal meal) {
        long baseSize = 100;
        if (meal.getName() != null) {
            baseSize += meal.getName().length() * 2L;
        }
        return baseSize;
    }

    private void putMealInCache(int id, Meal meal) {
        if (currentCacheMemoryUsage + estimateObjectSize(meal) > MAX_CACHE_MEMORY_BYTES) {
            logger.warn("Cannot cache meal {} - memory limit would be exceeded", id);
            return;
        }
        mealCache.put(id, meal);
        updateMemoryUsage(meal, true);
        logger.info("{}", id);
    }

    private void evictMealFromCache(int id) {
        Meal meal = mealCache.get(id);
        if (meal != null) {
            updateMemoryUsage(meal, false);
        }
        mealCache.evict(id);
        logger.info("Meal evicted from cache: {}", id);
    }

    @Override
    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    @Override
    public Optional<Meal> getMealById(int id) {
        Meal cachedMeal = mealCache.get(id);
        if (cachedMeal != null) {
            logger.info("Meal retrieved from cache: {}", id);
            return Optional.of(cachedMeal);
        }

        Optional<Meal> meal = mealRepository.findById(id);
        meal.ifPresent(m -> {
            putMealInCache(id, m);
            logger.info("{}{}", ADDED_TO_CACHE_MESSAGE, id);
        });
        return meal;
    }

    @Override
    public Optional<Meal> getMealByName(String name) {
        return mealRepository.findByName(name);
    }

    @Override
    public Meal addMeal(@Valid Meal meal) {
        Meal savedMeal = mealRepository.save(meal);
        putMealInCache(savedMeal.getId(), savedMeal);
        logger.info("{}{}", ADDED_TO_CACHE_MESSAGE, savedMeal.getId());
        return savedMeal;
    }

    @Override
    public List<Meal> addMeals(List<Meal> meals) {
        if (meals.stream().anyMatch(meal -> meal.getId() != null)) {
            throw new IllegalArgumentException("IDs should not be provided for new meals");
        }

        return mealRepository.saveAll(meals).stream()
                .map(meal -> {
                    putMealInCache(meal.getId(), meal);
                    logger.info("{}{}", ADDED_TO_CACHE_MESSAGE, meal.getId());
                    return meal;
                })
                .toList();
    }

    @Override
    public Meal updateMeal(int id, Meal mealDetails) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));

        meal.setName(mealDetails.getName());
        meal.setCookingTime(mealDetails.getCookingTime());
        meal.setPrice(mealDetails.getPrice());

        Meal updatedMeal = mealRepository.save(meal);
        putMealInCache(id, updatedMeal);
        logger.info("Meal updated in cache: {}", id);

        return updatedMeal;
    }

    @Override
    public void deleteMeal(int id) {
        var meal = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
        var orders = orderRepository.findOrdersByMealId(id);

        for (var order : orders) {
            order.getMeals().remove(meal);
        }

        orderRepository.saveAll(orders);
        mealRepository.delete(meal);

        orderCache.clear();
        evictMealFromCache(id);
        logger.info("Order cache cleared due to meal deletion");
    }
}