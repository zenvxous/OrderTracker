package ordertracker.core.models;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Customer {
    private int id;
    private String phoneNumber;
    private String name;
    private List<Order> orders;
}
