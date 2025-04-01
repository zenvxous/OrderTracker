package ordertracker.core.services.impls;

import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Customer;
import ordertracker.core.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() {
        // Arrange
        Customer customer1 = new Customer(1, "John Doe", "1234567890");
        Customer customer2 = new Customer(2, "Jane Smith", "0987654321");
        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer1, customer2));

        // Act
        List<Customer> result = customerService.getAllCustomers();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(customer1, customer2)));
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getCustomersByOrderStatusAndMealName_ShouldReturnFilteredCustomers() {
        // Arrange
        OrderStatus status = OrderStatus.READY;
        String mealName = "Pizza";
        Customer customer = new Customer(1, "John Doe", "1234567890");
        when(customerRepository.findCustomersByOrderStatusAndMealName(status, mealName))
                .thenReturn(List.of(customer));

        // Act
        List<Customer> result = customerService.getCustomersByOrderStatusAndMealName(status, mealName);

        // Assert
        assertEquals(1, result.size());
        assertEquals(customer, result.get(0));
        verify(customerRepository, times(1))
                .findCustomersByOrderStatusAndMealName(status, mealName);
    }

    @Test
    void getCustomerById_WhenCustomerExists_ShouldReturnCustomer() {
        // Arrange
        int customerId = 1;
        Customer customer = new Customer(customerId, "John Doe", "1234567890");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Act
        Optional<Customer> result = customerService.getCustomerById(customerId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void getCustomerById_WhenCustomerNotExists_ShouldReturnEmpty() {
        // Arrange
        int customerId = 999;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act
        Optional<Customer> result = customerService.getCustomerById(customerId);

        // Assert
        assertTrue(result.isEmpty());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void getCustomerByName_ShouldReturnCustomer() {
        // Arrange
        String name = "John Doe";
        Customer customer = new Customer(1, name, "1234567890");
        when(customerRepository.findByName(name)).thenReturn(Optional.of(customer));

        // Act
        Optional<Customer> result = customerService.getCustomerByName(name);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
        verify(customerRepository, times(1)).findByName(name);
    }

    @Test
    void getCustomerByPhoneNumber_ShouldReturnCustomer() {
        // Arrange
        String phoneNumber = "1234567890";
        Customer customer = new Customer(1, "John Doe", phoneNumber);
        when(customerRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(customer));

        // Act
        Optional<Customer> result = customerService.getCustomerByPhoneNumber(phoneNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
        verify(customerRepository, times(1)).findByPhoneNumber(phoneNumber);
    }

    @Test
    void addCustomer_ShouldSaveAndReturnCustomer() {
        // Arrange
        Customer customerToSave = new Customer(null, "John Doe", "1234567890");
        Customer savedCustomer = new Customer(1, "John Doe", "1234567890");
        when(customerRepository.save(customerToSave)).thenReturn(savedCustomer);

        // Act
        Customer result = customerService.addCustomer(customerToSave);

        // Assert
        assertEquals(savedCustomer, result);
        verify(customerRepository, times(1)).save(customerToSave);
    }

    @Test
    void updateCustomer_WhenCustomerExists_ShouldUpdateAndReturnCustomer() {
        // Arrange
        int customerId = 1;
        Customer existingCustomer = new Customer(customerId, "Old Name", "1111111111");
        Customer updatedDetails = new Customer(null, "New Name", "2222222222");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(updatedDetails);

        // Act
        Customer result = customerService.updateCustomer(customerId, updatedDetails);

        // Assert
        assertEquals("2222222222", result.getName());
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).save(existingCustomer);
    }

    @Test
    void updateCustomer_WhenCustomerNotExists_ShouldThrowException() {
        // Arrange
        int customerId = 999;
        Customer updatedDetails = new Customer(null, "New Name", "2222222222");
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> customerService.updateCustomer(customerId, updatedDetails));

        assertEquals("Customer not found with id: " + customerId, exception.getMessage());
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomer_WhenCustomerExists_ShouldDeleteCustomer() {
        // Arrange
        int customerId = 1;
        Customer customer = new Customer(customerId, "John Doe", "1234567890");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).delete(customer);

        // Act
        customerService.deleteCustomer(customerId);

        // Assert
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void deleteCustomer_WhenCustomerNotExists_ShouldThrowException() {
        // Arrange
        int customerId = 999;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> customerService.deleteCustomer(customerId));

        assertEquals("Customer not found with id: " + customerId, exception.getMessage());
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, never()).delete(any());
    }
}