package ordertracker.api.controllers;

import java.util.List;
import ordertracker.core.models.Meal;
import ordertracker.core.services.MealService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/meals")
public class MealController {
    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    public List<Meal> getAllMeals() {
        return mealService.getAllMeals();
    }

    @GetMapping("/{id}")
    public Meal getMealById(@PathVariable int id) {
        var response =  mealService.getMealById(id);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter is missing");
        }
        return response;
    }

    @GetMapping("/by-name")
    public Meal getMealByName(@RequestParam String name) {
        var response = mealService.getMealByName(name);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter is missing");
        }
        return response;
    }
}
