
package com.fooddelivery.service;

import com.fooddelivery.entity.CustomerOrder;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.OrderItem;

import com.fooddelivery.repository.CustomerOrderRepository;
import com.fooddelivery.repository.OrderItemRepository;
import com.fooddelivery.repository.FoodItemRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FoodItemRepository foodItemRepository;

    public CustomerOrderService(
            CustomerOrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            FoodItemRepository foodItemRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.foodItemRepository = foodItemRepository;
    }

    // Place an order using prices stored in MySQL
    @Transactional
    public CustomerOrder placeOrder(
            CustomerOrder order, List<OrderItem> items) {

        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "An order must contain at least one item");
        }

        double subtotal = 0;

        // Validate each item and get its actual database price
        for (OrderItem item : items) {

            if (item.getFoodItemId() == null
                    || item.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid food item or quantity");
            }

            FoodItem food = foodItemRepository
                    .findById(item.getFoodItemId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Food item not found: "
                                            + item.getFoodItemId()));

            // Never trust the price or name sent by the browser
            item.setFoodName(food.getName());
            item.setPrice(food.getPrice());

            subtotal += food.getPrice() * item.getQuantity();
        }

        // ₹35 delivery below ₹499; free delivery at ₹499 or more
        double deliveryFee =
                subtotal > 0 && subtotal < 499 ? 35 : 0;

        double verifiedTotal =
                Math.round((subtotal + deliveryFee) * 100.0) / 100.0;

        // Ignore the total amount submitted by the client
        order.setTotalAmount(verifiedTotal);
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());

        CustomerOrder savedOrder = orderRepository.save(order);

        // Save items against the newly created order
        for (OrderItem item : items) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }

        return savedOrder;
    }

    // Get all orders
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get one order by ID
    public CustomerOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found: " + id));
    }

    // Get food items belonging to an order
    public List<OrderItem> getItemsByOrderId(Long orderId) {
        getOrderById(orderId);
        return orderItemRepository.findByOrderId(orderId);
    }

    // Update order status
    @Transactional
    public CustomerOrder updateOrderStatus(Long id, String status) {

        Set<String> allowedStatuses = Set.of(
                "PENDING",
                "CONFIRMED",
                "PREPARING",
                "OUT_FOR_DELIVERY",
                "DELIVERED",
                "CANCELLED"
        );

        if (status == null ||
                !allowedStatuses.contains(
                        status.trim().toUpperCase())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order status");
        }

        CustomerOrder order = getOrderById(id);
        order.setStatus(status.trim().toUpperCase());

        return orderRepository.save(order);
    }

    // Orders belonging to one customer, newest first
    public List<CustomerOrder> getOrdersForCustomer(String email) {
        return orderRepository
                .findByCustomerEmailIgnoreCaseOrderByOrderDateDesc(email);
    }

    // A customer may cancel only their own order, and only while PENDING
    @Transactional
    public CustomerOrder cancelOwnOrder(Long id, String email) {

        CustomerOrder order = getOrderById(id);

        if (order.getCustomerEmail() == null
                || !order.getCustomerEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order not found: " + id);
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending orders can be cancelled");
        }

        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }
}
