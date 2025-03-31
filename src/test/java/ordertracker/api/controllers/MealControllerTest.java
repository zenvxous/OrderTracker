package ordertracker.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ordertracker.core.exceptions.ResourceNotFoundException;
import ordertracker.core.models.Meal;
import ordertracker.core.services.MealService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealController.class)
class MealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MealService mealService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Meal testMeal = new Meal(1, "Burger", new BigDecimal("9.99"), 10);
    private final Meal testMeal2 = new Meal(2, "Pizza", new BigDecimal("12.99"), 13);

    @Test
    void getAllMeals_ReturnsListOfMeals() throws Exception {
        List<Meal> meals = Arrays.asList(testMeal, testMeal2);
        Mockito.when(mealService.getAllMeals()).thenReturn(meals);

        mockMvc.perform(get("/api/meals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Burger"))
                .andExpect(jsonPath("$[1].name").value("Pizza"));
    }

    @Test
    void getMealById_ReturnsMeal() throws Exception {
        Mockito.when(mealService.getMealById(1)).thenReturn(Optional.of(testMeal));

        mockMvc.perform(get("/api/meals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Burger"))
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    void getMealById_ThrowsNotFound_WhenInvalidId() throws Exception {
        Mockito.when(mealService.getMealById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/meals/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMealByName_ReturnsMeal() throws Exception {
        Mockito.when(mealService.getMealByName("Burger")).thenReturn(Optional.of(testMeal));

        mockMvc.perform(get("/api/meals/name").param("name", "Burger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    void getMealByName_ThrowsNotFound_WhenInvalidName() throws Exception {
        Mockito.when(mealService.getMealByName("Unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/meals/name").param("name", "Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMeal_ReturnsCreated() throws Exception {
        Meal newMeal = new Meal(null, "Salad", new BigDecimal("5.99"), 6);
        Mockito.when(mealService.addMeal(any(Meal.class))).thenReturn(testMeal);

        mockMvc.perform(post("/api/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMeal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addMeal_ThrowsBadRequest_WhenIdProvided() throws Exception {
        Meal invalidMeal = new Meal(1, "Salad", new BigDecimal("5.99"), 6);

        mockMvc.perform(post("/api/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMeal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMeals_ReturnsCreated() throws Exception {
        List<Meal> newMeals = Arrays.asList(
                new Meal(null, "Salad", new BigDecimal("5.99"), 6),
                new Meal(null, "Soup", new BigDecimal("4.99"), 5)
        );
        List<Meal> savedMeals = Arrays.asList(testMeal, testMeal2);
        Mockito.when(mealService.addMeals(anyList())).thenReturn(savedMeals);

        mockMvc.perform(post("/api/meals/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMeals)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void addMeals_ThrowsBadRequest_WhenEmptyList() throws Exception {
        mockMvc.perform(post("/api/meals/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMeals_ThrowsBadRequest_WhenIdProvided() throws Exception {
        List<Meal> invalidMeals = Collections.singletonList(
                new Meal(1, "Salad", new BigDecimal("5.99"), 6)
        );

        mockMvc.perform(post("/api/meals/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMeals)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMeal_ReturnsUpdatedMeal() throws Exception {
        Meal updatedDetails = new Meal(1, "Cheeseburger", new BigDecimal("10.99"), 11);
        Mockito.when(mealService.updateMeal(anyInt(), any(Meal.class))).thenReturn(updatedDetails);

        mockMvc.perform(put("/api/meals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cheeseburger"))
                .andExpect(jsonPath("$.cookingTime").value(11))
                .andExpect(jsonPath("$.price").value(10.99));
    }

    @Test
    void updateMeal_ThrowsNotFound_WhenInvalidId() throws Exception {
        Meal updatedDetails = new Meal(1, "Cheeseburger", new BigDecimal("10.99"), 11);
        Mockito.when(mealService.updateMeal(anyInt(), any(Meal.class)))
                .thenThrow(new ResourceNotFoundException("Meal Not Found with id:1"));

        mockMvc.perform(put("/api/meals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMeal_ReturnsNoContent() throws Exception {
        Mockito.doNothing().when(mealService).deleteMeal(1);

        mockMvc.perform(delete("/api/meals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMeal_ThrowsNotFound_WhenInvalidId() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Meal not found with id: 99"))
                .when(mealService).deleteMeal(99);

        mockMvc.perform(delete("/api/meals/99"))
                .andExpect(status().isNotFound());
    }
}