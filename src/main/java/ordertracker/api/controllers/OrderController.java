package ordertracker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.exceptions.ResourceNotFoundException;
import ordertracker.core.models.Order;
import ordertracker.core.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order controller", description = "Controller for managing customer orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Get all orders", description = "Retrieves a list of all orders")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders")
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(summary = "Get order by ID", description = "Retrieves a single order by its ID")
    @ApiResponse(responseCode = "200", description = "Order found and returned")
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "ID of the order to be retrieved", required = true, example = "1")
            @PathVariable @Min(1) int id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id:" + " " + id));
    }

    @Operation(summary = "Create new order", description = "Creates a new order for the specified customer")
    @ApiResponse(responseCode = "201", description = "Order successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid customer ID supplied")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @PostMapping
    public ResponseEntity<Order> addOrder(
            @Parameter(description = "ID of the customer who places the order", required = true, example = "1")
            @RequestParam @Min(1) int customerId) {
        try {
            var savedOrder = orderService.addOrder(customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
    }

    @Operation(summary = "Update order status", description = "Updates the status of an existing order")
    @ApiResponse(responseCode = "200", description = "Order status successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid ID or status supplied")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @Parameter(description = "ID of the order to update", required = true, example = "1")
            @PathVariable @Min(1) int id,
            @Parameter(description = "New status for the order", required = true)
            @RequestParam OrderStatus status) {
        try {
            var order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.status(HttpStatus.OK).body(order);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Order not found with id:" + " " + id);
        }
    }

    @Operation(summary = "Add meal to order", description = "Adds a meal to an existing order")
    @ApiResponse(responseCode = "200", description = "Meal successfully added to order")
    @ApiResponse(responseCode = "400", description = "Invalid order ID or meal ID supplied")
    @ApiResponse(responseCode = "404", description = "Order or meal not found")
    @PutMapping("/{id}/meals")
    public ResponseEntity<Order> addMealToOrder(
            @Parameter(description = "ID of the order to modify", required = true, example = "1")
            @PathVariable @Min(1) int id,
            @Parameter(description = "ID of the meal to add", required = true, example = "1")
            @RequestParam @Min(1) int mealId) {
        try {
            var order = orderService.addMealToOrder(id, mealId);
            return ResponseEntity.status(HttpStatus.OK).body(order);
        } catch (Exception e) {
            throw new ResourceNotFoundException(
                    "Order not found with id: " + id + " or meal not found with id: " + mealId);
        }
    }

    @Operation(summary = "Delete order", description = "Deletes an existing order")
    @ApiResponse(responseCode = "204", description = "Order successfully deleted")
    @ApiResponse(responseCode = "400", description = "Invalid ID supplied")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID of the order to delete", required = true, example = "1")
            @PathVariable @Min(1) int id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Order not found with id:" + id);
        }
    }

    @Operation(summary = "Remove meal from order", description = "Removes a meal from an existing order")
    @ApiResponse(responseCode = "204", description = "Meal successfully removed from order")
    @ApiResponse(responseCode = "400", description = "Invalid order ID or meal ID supplied")
    @ApiResponse(responseCode = "404", description = "Order or meal not found")
    @DeleteMapping("/{id}/meals")
    public ResponseEntity<Void> deleteOrderFood(
            @Parameter(description = "ID of the order to modify", required = true, example = "1")
            @PathVariable @Min(1) int id,
            @Parameter(description = "ID of the meal to remove", required = true, example = "1")
            @RequestParam @Min(1) int mealId) {
        try {
            orderService.deleteMealInOrder(id, mealId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResourceNotFoundException(
                    "Order not found with id: " + id + " or meal not found in order with id: " + mealId);
        }
    }
}
