package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ordertracker.apllication.cache.InMemoryCache;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Customer;
import ordertracker.core.repositories.CustomerRepository;
import ordertracker.core.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final InMemoryCache<Integer, Customer> cache;
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);

    @Autowired
    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            InMemoryCache<Integer, Customer> customerCache) {
        this.customerRepository = customerRepository;
        this.cache = customerCache;
        cacheCleaner.scheduleAtFixedRate(cache::clear, 30, 30, TimeUnit.MINUTES);
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
        Customer cachedCustomer = cache.get(id);

        if (cachedCustomer != null) {
            return Optional.of(cachedCustomer);
        }

        Optional<Customer> customer = customerRepository.findById(id);
        customer.ifPresent(c -> cache.put(id, c));
        return customer;
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
        Customer savedCustomer = customerRepository.save(customer);

        cache.put(savedCustomer.getId(), savedCustomer);

        return savedCustomer;
    }

    @Override
    public Customer updateCustomer(int id, Customer customerDetails) {
        var customer = getCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        customer.setName(customerDetails.getName());
        customer.setPhoneNumber(customerDetails.getPhoneNumber());
        Customer updatedCustomer = customerRepository.save(customer);

        cache.evict(id);
        cache.put(id, updatedCustomer);

        return updatedCustomer;
    }

    @Override
    public void deleteCustomer(int id) {
        var customer = getCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        customerRepository.delete(customer);
        cache.evict(id);
    }
}