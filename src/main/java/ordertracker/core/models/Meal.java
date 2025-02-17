package ordertracker.core.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Meal {
    private int id;
    private String name;
    private int price;
    private int cookingTime;
}
