package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Customer;
import ordertracker.core.repositories.CustomerRepository;
import ordertracker.core.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String NOT_FOUND_MESSAGE = "Customer not found with id: ";

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(
            CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> getCustomersByOrderStatusAndMealName(OrderStatus status, String mealName) {
        return customerRepository.findCustomersByOrderStatusAndMealName(status, mealName);
    }

    @Override
    public Optional<Customer> getCustomerById(int id) {
        return customerRepository.findById(id);
    }

    @Override
    public Optional<Customer> getCustomerByName(String name) {
        return customerRepository.findByName(name);
    }

    @Override
    public Optional<Customer> getCustomerByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public Customer addCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(int id, Customer customerDetails) {
        var customer = getCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));

        customer.setName(customerDetails.getName());
        customer.setPhoneNumber(customerDetails.getPhoneNumber());

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(int id) {
        var customer = getCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));
        customerRepository.delete(customer);
    }
}