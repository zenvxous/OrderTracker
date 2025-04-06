package ordertracker.core.services;

import java.util.List;
import java.util.Optional;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Customer;
import ordertracker.core.models.Order;

public interface CustomerService {
    List<Customer> getAllCustomers();

    List<Customer> getCustomersByOrderStatusAndMealName(OrderStatus status, String mealName);

    Optional<Customer> getCustomerById(int id);

    Optional<Customer> getCustomerByPhoneNumber(String phoneNumber);

    Optional<Customer> getCustomerByName(String name);

    List<Order> getCustomerOrders(int customerId);

    Order createOrder(int customerId, List<Integer> mealIds);

    Customer addCustomer(Customer customer);

    Customer updateCustomer(int id, Customer customerDetails);

    void deleteCustomer(int id);
}
