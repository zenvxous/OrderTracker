package ordertracker.api.controllers;

import ordertracker.core.enums.OrderStatus;
import ordertracker.core.exceptions.ResourceNotFoundException;
import ordertracker.core.models.*;
import ordertracker.core.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private Customer testCustomer;
    private Meal testMeal;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer(1, "Alice", "+1234567890");


        testMeal = new Meal(1, "Burger", new BigDecimal("9.99"), 15);

        testOrder = new Order(1, testCustomer, Collections.singletonList(testMeal), OrderStatus.ACCEPTED);
    }

    @Test
    void getAllOrders_ReturnsListOfOrders() throws Exception {
        List<Order> orders = Collections.singletonList(testOrder);
        Mockito.when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    @Test
    void getOrderById_ReturnsOrder() throws Exception {
        Mockito.when(orderService.getOrderById(1)).thenReturn(Optional.of(testOrder));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meals[0].name").value("Burger"));
    }

    @Test
    void getOrderById_ThrowsNotFound_WhenInvalidId() throws Exception {
        Mockito.when(orderService.getOrderById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addOrder_ReturnsCreated() throws Exception {
        Mockito.when(orderService.addOrder(1)).thenReturn(testOrder);

        mockMvc.perform(post("/api/orders")
                        .param("customerId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addOrder_ThrowsNotFound_WhenInvalidCustomerId() throws Exception {
        Mockito.when(orderService.addOrder(99))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: 99"));

        mockMvc.perform(post("/api/orders")
                        .param("customerId", "99"))
                .andExpect(status().isNotFound());
    }


    @Test
    void addMealToOrder_ThrowsNotFound_WhenInvalidIds() throws Exception {
        Mockito.when(orderService.addMealToOrder(99, 99))
                .thenThrow(new ResourceNotFoundException(
                        "Order not found with id: 99 or meal not found with id: 99"));

        mockMvc.perform(put("/api/orders/99/meals")
                        .param("mealId", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMealFromOrder_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/1/meals")
                        .param("mealId", "1"))
                .andExpect(status().isNoContent());
    }
}