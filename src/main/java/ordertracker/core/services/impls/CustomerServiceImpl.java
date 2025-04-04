package ordertracker.core.services.impls;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ordertracker.core.enums.OrderStatus;
import ordertracker.core.models.Customer;
import ordertracker.core.models.Meal;
import ordertracker.core.models.Order;
import ordertracker.core.repositories.CustomerRepository;
import ordertracker.core.repositories.MealRepository;
import ordertracker.core.repositories.OrderRepository;
import ordertracker.core.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String NOT_FOUND_MESSAGE = "Customer not found with id: ";

    private final CustomerRepository customerRepository;
    private final MealRepository mealRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            MealRepository mealRepository,
            OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.mealRepository = mealRepository;
        this.orderRepository = orderRepository;
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
    public List<Order> getCustomerOrders(int customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            return customer.get().getOrders();
        }
        throw new EntityNotFoundException(NOT_FOUND_MESSAGE + customerId);
    }

    @Override
    public Order createOrder(int customerId, List<Integer> mealIds) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id:" + customerId));
        ArrayList<Meal> meals = new ArrayList<>();
        for (Integer mealId : mealIds) {
            var meal = mealRepository.findById(mealId);
            if (meal.isEmpty()) {
                throw new EntityNotFoundException("Meal not found with id:" + mealId);
            }
            meals.add(meal.get());
        }

        var order = new Order(null, customer, meals, OrderStatus.ACCEPTED);

        return orderRepository.save(order);
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