package ordertracker.core.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import ordertracker.core.enums.OrderStatus;


@Data
@AllArgsConstructor
public class Order {
    private int id;
    private int customerId;
    private List<Meal> meals;
    private OrderStatus status;
}
