package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import ordertracker.core.models.Meal;
import ordertracker.core.repositories.MealRepository;
import ordertracker.core.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceImplTest {

    @Mock
    private MealRepository mealRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private MealServiceImpl mealService;

    @Test
    void getAllMeals_ShouldReturnAllMeals() {
        // Arrange
        Meal meal1 = new Meal(1, "Pizza", new BigDecimal("10.99"), 11);
        Meal meal2 = new Meal(2, "Burger", new BigDecimal("8.99"), 9);
        when(mealRepository.findAll()).thenReturn(Arrays.asList(meal1, meal2));

        // Act
        List<Meal> result = mealService.getAllMeals();

        // Assert
        assertEquals(2, result.size());
        verify(mealRepository, times(1)).findAll();
    }

    @Test
    void getMealById_WhenMealExists_ShouldReturnMeal() {
        // Arrange
        Meal meal = new Meal(1, "Pizza", new BigDecimal("10.99"), 11);
        when(mealRepository.findById(1)).thenReturn(Optional.of(meal));

        // Act
        Optional<Meal> result = mealService.getMealById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Pizza", result.get().getName());
        verify(mealRepository, times(1)).findById(1);
    }

    @Test
    void getMealById_WhenMealNotExists_ShouldReturnEmpty() {
        // Arrange
        when(mealRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        Optional<Meal> result = mealService.getMealById(1);

        // Assert
        assertTrue(result.isEmpty());
        verify(mealRepository, times(1)).findById(1);
    }

    @Test
    void getMealByName_WhenMealExists_ShouldReturnMeal() {
        // Arrange
        Meal meal = new Meal(1, "Pizza", new BigDecimal("10.99"), 11);
        when(mealRepository.findByName("Pizza")).thenReturn(Optional.of(meal));

        // Act
        Optional<Meal> result = mealService.getMealByName("Pizza");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        verify(mealRepository, times(1)).findByName("Pizza");
    }

    @Test
    void addMeal_ShouldSaveAndReturnMeal() {
        // Arrange
        Meal newMeal = new Meal(1, "Pizza", new BigDecimal("10.99"), 11);
        Meal savedMeal = new Meal(1, "Pizza", new BigDecimal("15"), 11);
        when(mealRepository.save(newMeal)).thenReturn(savedMeal);

        // Act
        Meal result = mealService.addMeal(newMeal);

        // Assert
        assertEquals(1, result.getId());
        verify(mealRepository, times(1)).save(newMeal);
    }

    @Test
    void addMeals_WhenMealsWithIdsProvided_ShouldThrowException() {
        // Arrange
        Meal mealWithId = new Meal(1, "Pizza", new BigDecimal("10.99"), 11);
        List<Meal> meals = List.of(mealWithId);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> mealService.addMeals(meals));
        verify(mealRepository, never()).saveAll(any());
    }

    @Test
    void updateMeal_WhenMealExists_ShouldUpdateAndReturnMeal() {
        // Arrange
        Meal existingMeal = new Meal(1, "Pizza", new BigDecimal("10.99"), 11);
        Meal updatedDetails = new Meal(null, "New Pizza", new BigDecimal("12.99"), 20);

        when(mealRepository.findById(1)).thenReturn(Optional.of(existingMeal));
        when(mealRepository.save(existingMeal)).thenReturn(existingMeal);

        // Act
        Meal result = mealService.updateMeal(1, updatedDetails);

        // Assert
        assertEquals("New Pizza", result.getName());
        assertEquals(20, result.getCookingTime());
        assertEquals(new BigDecimal("12.99"), result.getPrice());
        verify(mealRepository, times(1)).findById(1);
        verify(mealRepository, times(1)).save(existingMeal);
    }

    @Test
    void updateMeal_WhenMealNotExists_ShouldThrowException() {
        // Arrange
        when(mealRepository.findById(1)).thenReturn(Optional.empty());
        Meal updatedDetails = new Meal(null, "New Pizza", new BigDecimal("12.99"), 20);

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> mealService.updateMeal(1, updatedDetails));
        verify(mealRepository, never()).save(any());
    }

    @Test
    void deleteMeal_WhenMealExists_ShouldDeleteMeal() {
        // Arrange
        Meal meal = new Meal(1, "Pizza", new BigDecimal("10.99"), 11);
        when(mealRepository.findById(1)).thenReturn(Optional.of(meal));

        // Act
        mealService.deleteMeal(1);

        // Assert
        verify(orderRepository, times(1)).findOrdersByMealId(1);
        verify(mealRepository, times(1)).delete(meal);
    }

    @Test
    void deleteMeal_WhenMealNotExists_ShouldThrowException() {
        // Arrange
        when(mealRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> mealService.deleteMeal(1));
        verify(mealRepository, never()).delete(any());
    }
}