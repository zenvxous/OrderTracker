package ordertracker.core.services;

import java.util.List;
import java.util.Optional;
import ordertracker.core.models.Customer;

public interface CustomerService {
    List<Customer> getAllCustomers();

    Optional<Customer> getCustomerById(int id);

    Optional<Customer> getCustomerByPhoneNumber(String phoneNumber);

    Optional<Customer> getCustomerByName(String name);

    Customer addCustomer(Customer customer);

    Customer updateCustomer(int id, Customer customerDetails);

    void deleteCustomer(int id);
}
