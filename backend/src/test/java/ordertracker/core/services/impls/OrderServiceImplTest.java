package ordertracker.core.services.impls;

import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Customer;
import ordertracker.core.models.Meal;
import ordertracker.core.models.Order;
import ordertracker.core.repositories.CustomerRepository;
import ordertracker.core.repositories.MealRepository;
import ordertracker.core.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MealRepository mealRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        Order order1 = new Order(1, new Customer(), new ArrayList<>(), OrderStatus.ACCEPTED);
        Order order2 = new Order(2, new Customer(), new ArrayList<>(), OrderStatus.READY);
        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(order1, order2)));
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        int orderId = 1;
        Order order = new Order(orderId, new Customer(), new ArrayList<>(), OrderStatus.ACCEPTED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Optional<Order> result = orderService.getOrderById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_WhenOrderNotExists_ShouldReturnEmpty() {
        // Arrange
        int orderId = 999;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderById(orderId);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void addOrder_WhenCustomerExists_ShouldCreateAndReturnOrder() {
        // Arrange
        int customerId = 1;
        Customer customer = new Customer(customerId, "John Doe", "1234567890");
        Order expectedOrder = new Order(null, customer, new ArrayList<>(), OrderStatus.ACCEPTED);
        Order savedOrder = new Order(1, customer, new ArrayList<>(), OrderStatus.ACCEPTED);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.save(expectedOrder)).thenReturn(savedOrder);

        // Act
        Order result = orderService.addOrder(customerId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.ACCEPTED, result.getStatus());
        assertEquals(customer, result.getCustomer());
        assertTrue(result.getMeals().isEmpty());
        verify(customerRepository, times(1)).findById(customerId);
        verify(orderRepository, times(1)).save(expectedOrder);
    }

    @Test
    void addOrder_WhenCustomerNotExists_ShouldThrowException() {
        // Arrange
        int customerId = 999;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.addOrder(customerId));

        assertEquals("Customer not found with id: " + customerId, exception.getMessage());
        verify(customerRepository, times(1)).findById(customerId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_WhenOrderExists_ShouldUpdateAndReturnOrder() {
        // Arrange
        int orderId = 1;
        Order existingOrder = new Order(orderId, new Customer(), new ArrayList<>(), OrderStatus.ACCEPTED);
        OrderStatus newStatus = OrderStatus.READY;

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        // Act
        Order result = orderService.updateOrderStatus(orderId, newStatus);

        // Assert
        assertEquals(newStatus, result.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(existingOrder);
    }

    @Test
    void updateOrderStatus_WhenOrderNotExists_ShouldThrowException() {
        // Arrange
        int orderId = 999;
        OrderStatus newStatus = OrderStatus.READY;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrderStatus(orderId, newStatus));

        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void addMealToOrder_WhenOrderAndMealExist_ShouldAddMealAndReturnOrder() {
        // Arrange
        int orderId = 1;
        int mealId = 10;
        Order order = new Order(orderId, new Customer(), new ArrayList<>(), OrderStatus.ACCEPTED);
        Meal meal = new Meal(mealId, "Pizza", new BigDecimal("10.99"), 11);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(mealRepository.findById(mealId)).thenReturn(Optional.of(meal));
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        Order result = orderService.addMealToOrder(orderId, mealId);

        // Assert
        assertTrue(result.getMeals().contains(meal));
        verify(orderRepository, times(1)).findById(orderId);
        verify(mealRepository, times(1)).findById(mealId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void addMealToOrder_WhenOrderNotExists_ShouldThrowException() {
        // Arrange
        int orderId = 999;
        int mealId = 10;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.addMealToOrder(orderId, mealId));

        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(mealRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void addMealToOrder_WhenMealNotExists_ShouldThrowException() {
        // Arrange
        int orderId = 1;
        int mealId = 999;
        Order order = new Order(orderId, new Customer(), new ArrayList<>(), OrderStatus.ACCEPTED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(mealRepository.findById(mealId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.addMealToOrder(orderId, mealId));

        assertEquals("Meal not found with id: " + mealId, exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(mealRepository, times(1)).findById(mealId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldDeleteOrder() {
        // Arrange
        int orderId = 1;
        Order order = new Order(orderId, new Customer(), new ArrayList<>(), OrderStatus.ACCEPTED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        // Act
        orderService.deleteOrder(orderId);

        // Assert
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void deleteOrder_WhenOrderNotExists_ShouldThrowException() {
        // Arrange
        int orderId = 999;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteOrder(orderId));

        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void deleteMealInOrder_WhenOrderAndMealExist_ShouldRemoveMealAndSaveOrder() {
        // Arrange
        int orderId = 1;
        int mealId = 10;
        Meal meal = new Meal(mealId, "Pizza", new BigDecimal("10.99"), 11);
        List<Meal> meals = new ArrayList<>(List.of(meal));
        Order order = new Order(orderId, new Customer(), meals, OrderStatus.ACCEPTED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(mealRepository.findById(mealId)).thenReturn(Optional.of(meal));
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        orderService.deleteMealInOrder(orderId, mealId);

        // Assert
        assertFalse(order.getMeals().contains(meal));
        verify(orderRepository, times(1)).findById(orderId);
        verify(mealRepository, times(1)).findById(mealId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void deleteMealInOrder_WhenOrderNotExists_ShouldThrowException() {
        // Arrange
        int orderId = 999;
        int mealId = 10;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteMealInOrder(orderId, mealId));

        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(mealRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteMealInOrder_WhenMealNotExists_ShouldThrowException() {
        // Arrange
        int orderId = 1;
        int mealId = 999;
        Order order = new Order(orderId, new Customer(), new ArrayList<>(), OrderStatus.ACCEPTED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(mealRepository.findById(mealId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteMealInOrder(orderId, mealId));

        assertEquals("Meal not found with id: " + mealId, exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(mealRepository, times(1)).findById(mealId);
        verify(orderRepository, never()).save(any());
    }
}