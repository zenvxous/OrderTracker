package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import ordertracker.core.models.Customer;
import ordertracker.core.repositories.CustomerRepository;
import ordertracker.core.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
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
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        customer.setId(customerDetails.getId());
        customer.setName(customerDetails.getName());
        customer.setPhoneNumber(customerDetails.getPhoneNumber());
        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(int id) {
        var customer = getCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        customerRepository.delete(customer);
    }
}
