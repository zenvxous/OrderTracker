package ordertracker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import ordertracker.core.exceptions.BadRequestException;
import ordertracker.core.exceptions.ResourceNotFoundException;
import ordertracker.core.models.Meal;
import ordertracker.core.services.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/meals")
@Tag(name = "Meal controller", description = "Controller for managing meals in the system")
public class MealController {
    private static final String MEAL_NOT_FOUND = "Meal Not Found";
    private final MealService mealService;

    @Autowired
    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @Operation(summary = "Get all meals", description = "Retrieves a list of all available meals")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of meals")
    @GetMapping
    public ResponseEntity<List<Meal>> getAllMeals() {
        return ResponseEntity.ok(mealService.getAllMeals());
    }

    @Operation(summary = "Get meal by ID", description = "Retrieves a single meal by its ID")
    @ApiResponse(responseCode = "200", description = "Meal found and returned")
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied")
    @ApiResponse(responseCode = "404", description = "Meal not found")
    @GetMapping("/{id}")
    public ResponseEntity<Meal> getMealById(
            @Parameter(description = "ID of the meal to be retrieved", required = true, example = "1")
            @PathVariable int id) {
        return mealService.getMealById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(MEAL_NOT_FOUND + " with id:" + id));
    }

    @Operation(summary = "Get meal by name", description = "Retrieves a single meal by its exact name")
    @ApiResponse(responseCode = "200", description = "Meal found and returned")
    @ApiResponse(responseCode = "400", description = "Invalid name parameter")
    @ApiResponse(responseCode = "404", description = "Meal not found with the specified name")
    @GetMapping("/name")
    public ResponseEntity<Meal> getMealByName(
            @Parameter(description = "Exact name of the meal to search for", required = true, example = "Burger")
            @RequestParam String name) {
        return mealService.getMealByName(name)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with name: " + name));
    }

    @Operation(summary = "Add new meal", description = "Creates a new meal in the system")
    @ApiResponse(responseCode = "201", description = "Meal successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input (e.g., ID provided for new meal)")
    @ApiResponse(responseCode = "409", description = "Meal with same name already exists")
    @PostMapping
    public ResponseEntity<Meal> addMeal(
            @Parameter(description = "Meal object that needs to be added to the system", required = true)
            @Valid @RequestBody Meal meal) {
        if (meal.getId() != null) {
            throw new BadRequestException("ID should not be provided for new meal");
        }
        var savedMeal = mealService.addMeal(meal);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMeal);
    }

    @Operation(summary = "Add multiple meals", description = "Creates multiple new meals in the system in one operation")
    @ApiResponse(responseCode = "201", description = "Meals successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input (e.g., ID provided for new meals)")
    @ApiResponse(responseCode = "409", description = "One or more meals with same names already exist")
    @PostMapping("/bulk")
    public ResponseEntity<List<Meal>> addMeals(
            @Parameter(description = "List of meal objects to be added", required = true)
            @Valid @RequestBody List<Meal> meals) {
        // Проверка на null и пустой список
        if (meals == null || meals.isEmpty()) {
            throw new BadRequestException("Meals list cannot be null or empty");
        }

        // Проверка наличия ID у любого из блюд
        boolean anyMealHasId = meals.stream()
                .anyMatch(meal -> meal.getId() != null);

        if (anyMealHasId) {
            throw new BadRequestException("IDs should not be provided for new meals");
        }

        List<Meal> savedMeals = mealService.addMeals(meals);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMeals);
    }

    @Operation(summary = "Update meal", description = "Updates an existing meal's details")
    @ApiResponse(responseCode = "200", description = "Meal successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid ID or input data")
    @ApiResponse(responseCode = "404", description = "Meal not found")
    @PutMapping("/{id}")
    public ResponseEntity<Meal> updateMeal(
            @Parameter(description = "ID of the meal to be updated", required = true, example = "1")
            @PathVariable int id,
            @Parameter(description = "Updated meal object", required = true)
            @Valid @RequestBody Meal mealDetails) {
        try {
            var updatedMeal = mealService.updateMeal(id, mealDetails);
            return ResponseEntity.ok(updatedMeal);
        } catch (Exception e) {
            throw new ResourceNotFoundException(MEAL_NOT_FOUND + " with id:" + id);
        }
    }

    @Operation(summary = "Delete meal", description = "Deletes an existing meal from the system")
    @ApiResponse(responseCode = "204", description = "Meal successfully deleted")
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied")
    @ApiResponse(responseCode = "404", description = "Meal not found")
    @ApiResponse(responseCode = "409", description = "Meal is referenced in existing orders")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(
            @Parameter(description = "ID of the meal to be deleted", required = true, example = "1")
            @PathVariable int id) {
        try {
            mealService.deleteMeal(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Meal not found with id: " + id);
        }
    }
}