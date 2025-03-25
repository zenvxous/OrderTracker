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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private static final long MAX_CACHE_MEMORY_BYTES = 100L * 1024 * 1024; // 100MB limit
    private static final String NOT_FOUND_MESSAGE = "Customer not found with id: ";
    private static final String ADDED_TO_CACHE_MESSAGE = "Customer added to cache: ";
    private static final String EVICTED_FROM_CACHE_MESSAGE = "Customer evicted from cache: ";

    private final CustomerRepository customerRepository;
    private final InMemoryCache<Integer, Customer> cache;
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    private long currentCacheMemoryUsage = 0;

    @Autowired
    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            InMemoryCache<Integer, Customer> customerCache) {
        this.customerRepository = customerRepository;
        this.cache = customerCache;
        cacheCleaner.scheduleAtFixedRate(() -> {
            logger.info("Clearing the cache");
            cache.clear();
            currentCacheMemoryUsage = 0;
        }, 30, 30, TimeUnit.MINUTES);
    }

    private synchronized void updateMemoryUsage(Customer customer, boolean add) {
        long customerSize = estimateObjectSize(customer);
        if (add) {
            currentCacheMemoryUsage += customerSize;
        } else {
            currentCacheMemoryUsage -= customerSize;
            if (currentCacheMemoryUsage < 0) {
                currentCacheMemoryUsage = 0;
            }
        }
    }

    private long estimateObjectSize(Customer customer) {
        long baseSize = 100;
        if (customer.getName() != null) {
            baseSize += customer.getName().length() * 2L;
        }
        if (customer.getPhoneNumber() != null) {
            baseSize += customer.getPhoneNumber().length() * 2L;
        }
        return baseSize;
    }

    private void putCustomerInCache(int id, Customer customer) {
        if (currentCacheMemoryUsage + estimateObjectSize(customer) > MAX_CACHE_MEMORY_BYTES) {
            logger.warn("Cannot cache customer {} - memory limit would be exceeded", id);
            return;
        }
        cache.put(id, customer);
        updateMemoryUsage(customer, true);
        logger.info("{}", id);
    }

    private void evictCustomerFromCache(int id) {
        Customer customer = cache.get(id);
        if (customer != null) {
            updateMemoryUsage(customer, false);
        }
        cache.evict(id);
        logger.info("{}", id);
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
            logger.info("Customer retrieved from cache: {}", id);
            return Optional.of(cachedCustomer);
        }

        Optional<Customer> customer = customerRepository.findById(id);
        customer.ifPresent(c -> {
            putCustomerInCache(id, c);
            logger.info("{}{}", ADDED_TO_CACHE_MESSAGE, id);
        });

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
        putCustomerInCache(savedCustomer.getId(), savedCustomer);
        logger.info("Customer added to cache: {}", savedCustomer.getId());
        return savedCustomer;
    }

    @Override
    public Customer updateCustomer(int id, Customer customerDetails) {
        var customer = getCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));

        customer.setName(customerDetails.getName());
        customer.setPhoneNumber(customerDetails.getPhoneNumber());
        Customer updatedCustomer = customerRepository.save(customer);

        putCustomerInCache(id, updatedCustomer);
        logger.info("Customer updated in cache: {}", id);

        return updatedCustomer;
    }

    @Override
    public void deleteCustomer(int id) {
        var customer = getCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE + id));

        customerRepository.delete(customer);
        evictCustomerFromCache(id);
        logger.info("{}{}", EVICTED_FROM_CACHE_MESSAGE, id);
    }
}