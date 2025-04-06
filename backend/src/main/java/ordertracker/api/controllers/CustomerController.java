package ordertracker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.exceptions.BadRequestException;
import ordertracker.core.exceptions.ResourceNotFoundException;
import ordertracker.core.models.Customer;
import ordertracker.core.models.Order;
import ordertracker.core.services.CustomerService;
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
@RequestMapping("/api/customers")
@Tag(name = "Customer controller", description = "Controller for managing customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Get all customers", description = "Retrieves a list of all customers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of customers")
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a single customer by their ID")
    @ApiResponse(responseCode = "200", description = "Customer found and returned")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @Parameter(description = "ID of the customer to be retrieved") @PathVariable int id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Operation(summary = "Get customer by name", description = "Retrieves a single customer by their name")
    @ApiResponse(responseCode = "200", description = "Customer found and returned")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @GetMapping("/name/{name}")
    public ResponseEntity<Customer> getCustomerByName(
            @Parameter(description = "Name of the customer to be retrieved") @PathVariable String name) {
        return customerService.getCustomerByName(name)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with name: " + name));
    }

    @Operation(summary = "Get customer by phone number", description = "Retrieves a single customer by their phone number")
    @ApiResponse(responseCode = "200", description = "Customer found and returned")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<Customer> getCustomerByPhoneNumber(
            @Parameter(description = "Phone number of the customer to be retrieved") @PathVariable String phoneNumber) {
        return customerService.getCustomerByPhoneNumber(phoneNumber)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phoneNumber));
    }

    @Operation(summary = "Filter customers by order status and meal name",
            description = "Retrieves customers who have orders with specified status and meal name")
    @ApiResponse(responseCode = "200", description = "Customers found and returned")
    @ApiResponse(responseCode = "404", description = "No customers found with the specified criteria")
    @GetMapping("/filter/meal")
    public ResponseEntity<List<Customer>> getCustomersByOrderStatusAndMealName(
            @Parameter(description = "Status of the order to filter by", required = true)
            @RequestParam OrderStatus status,
            @Parameter(description = "Name of the meal to filter by", required = true)
            @RequestParam String mealName) {

        List<Customer> customers = customerService.getCustomersByOrderStatusAndMealName(status, mealName);
        if (customers.isEmpty()) {
            throw new ResourceNotFoundException("No customers found with status " + status + " and meal " + mealName);
        }
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Add a new customer", description = "Creates a new customer record")
    @ApiResponse(responseCode = "201", description = "Customer successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input (e.g., ID provided for new customer)")
    @PostMapping
    public ResponseEntity<Customer> addCustomer(
            @Parameter(description = "Customer object to be created", required = true)
            @Valid @RequestBody Customer customer) {
        if (customer.getId() != null) {
            throw new BadRequestException("ID should not be provided for new customer");
        }
        Customer savedCustomer = customerService.addCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    @Operation(summary = "Update a customer", description = "Updates an existing customer record")
    @ApiResponse(responseCode = "200", description = "Customer successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid input (e.g., ID mismatch)")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @Parameter(description = "ID of the customer to be updated", required = true)
            @PathVariable int id,
            @Parameter(description = "Updated customer object", required = true)
            @Valid @RequestBody Customer customerDetails) {

        if (customerDetails.getId() != null && !customerDetails.getId().equals(id)) {
            throw new BadRequestException("ID in path and body don't match");
        }

        try {
            var updatedCustomer = customerService.updateCustomer(id, customerDetails);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
    }

    @Operation(summary = "Delete a customer", description = "Deletes an existing customer record")
    @ApiResponse(responseCode = "204", description = "Customer successfully deleted")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "ID of the customer to be deleted", required = true)
            @PathVariable int id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Customer not found with id:" + " " + id);
        }
    }

    @GetMapping("/{customerId}/orders")
    @Operation(summary = "Get all orders for customer")
    public ResponseEntity<List<Order>> getCustomerOrders(
            @PathVariable int customerId) {
        try {
            List<Order> orders = customerService.getCustomerOrders(customerId);
            return ResponseEntity.ok(orders);
        } catch (EntityNotFoundException exception) {
            throw new ResourceNotFoundException("Customer not found with id:" + " " + customerId);
        }
    }

    @PostMapping("/{customerId}/orders")
    @Operation(summary = "Create new order for customer")
    public ResponseEntity<Order> createOrderForCustomer(
            @PathVariable int customerId,
            @RequestBody Map<String, List<Integer>> request) {
        try {
            List<Integer> mealIds = request.get("mealIds");
            Order order = customerService.createOrder(customerId, mealIds);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (EntityNotFoundException exception) {
            throw new ResourceNotFoundException("Something went wrong");
        }
    }
}