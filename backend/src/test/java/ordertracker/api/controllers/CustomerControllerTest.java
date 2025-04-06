package ordertracker.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.exceptions.ResourceNotFoundException;
import ordertracker.core.models.Customer;
import ordertracker.core.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Тестовые данные
    private final Customer testCustomer = new Customer(1, "1234567890", "Alice");

    @Test
    void getAllCustomers_ReturnsListOfCustomers() throws Exception {
        Mockito.when(customerService.getAllCustomers())
                .thenReturn(Collections.singletonList(testCustomer));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    void getCustomerById_ReturnsCustomer() throws Exception {
        Mockito.when(customerService.getCustomerById(1))
                .thenReturn(Optional.of(testCustomer));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"));
    }

    @Test
    void getCustomerById_ThrowsNotFound() throws Exception {
        Mockito.when(customerService.getCustomerById(99))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerByName_ReturnsCustomer() throws Exception {
        Mockito.when(customerService.getCustomerByName("Alice"))
                .thenReturn(Optional.of(testCustomer));

        mockMvc.perform(get("/api/customers/name/Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addCustomer_ReturnsCreated() throws Exception {
        Customer newCustomer = new Customer(null, "9876543210", "BOb");
        Mockito.when(customerService.addCustomer(newCustomer))
                .thenReturn(testCustomer);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void addCustomer_ThrowsBadRequest_WhenIdProvided() throws Exception {
        Customer invalidCustomer = new Customer(1, "Bob", "9876543210");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCustomer_ReturnsUpdatedCustomer() throws Exception {
        Customer updatedDetails = new Customer(1, "1111111111", "Alice Updated");
        Mockito.when(customerService.updateCustomer(1, updatedDetails))
                .thenReturn(updatedDetails);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("1111111111"));
    }

    @Test
    void updateCustomer_ThrowsBadRequest_WhenIdMismatch() throws Exception {
        Customer invalidDetails = new Customer(2, "Alice", "1234567890");

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCustomer_ReturnsNoContent() throws Exception {
        Mockito.doNothing().when(customerService).deleteCustomer(1);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCustomer_ThrowsNotFound_WhenInvalidId() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Customer not found"))
                .when(customerService).deleteCustomer(99);

        mockMvc.perform(delete("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomersByOrderStatusAndMealName_ReturnsFilteredList() throws Exception {
        Mockito.when(customerService.getCustomersByOrderStatusAndMealName(OrderStatus.READY, "Pizza"))
                .thenReturn(Collections.singletonList(testCustomer));

        mockMvc.perform(get("/api/customers/filter/meal")
                        .param("status", "READY")
                        .param("mealName", "Pizza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    void getCustomersByOrderStatusAndMealName_ThrowsNotFound_WhenEmpty() throws Exception {
        Mockito.when(customerService.getCustomersByOrderStatusAndMealName(OrderStatus.COOKING, "Burger"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/customers/filter/meal")
                        .param("status", "COOKING")
                        .param("mealName", "Burger"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addCustomer_ThrowsBadRequest_WhenInvalidInput() throws Exception {
        Customer invalidCustomer = new Customer(null, "", "");  // Пустое имя и телефон

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest());
    }
}