package ordertracker.api.controllers;

import java.util.List;
import ordertracker.core.models.Meal;
import ordertracker.core.services.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/meals")
public class MealController {
    private final MealService mealService;

    @Autowired
    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    public ResponseEntity<List<Meal>> getAllMeals() {
        return ResponseEntity.ok(mealService.getAllMeals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meal> getMealById(@PathVariable int id) {
        return mealService.getMealById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name")
    public ResponseEntity<Meal> getMealByName(@RequestParam String name) {
        return mealService.getMealByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Meal> addMeal(@RequestBody Meal meal) {
        meal.setId(null);
        var savedMeal = mealService.addMeal(meal);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMeal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meal> updateMeal(@PathVariable int id, @RequestBody Meal mealDetails) {
        try {
            var updatedMeal = mealService.updateMeal(id, mealDetails);
            return ResponseEntity.ok(updatedMeal);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable int id) {
        try {
            mealService.deleteMeal(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }
}
