
package com.fooddelivery.controller;

import com.fooddelivery.entity.CustomerOrder;
import com.fooddelivery.entity.OrderItem;
import com.fooddelivery.repository.OrderItemRepository;
import com.fooddelivery.service.CustomerOrderService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

    private final CustomerOrderService orderService;
    private final OrderItemRepository orderItemRepository;

    public CustomerOrderController(
            CustomerOrderService orderService,
            OrderItemRepository orderItemRepository) {
        this.orderService = orderService;
        this.orderItemRepository = orderItemRepository;
    }

    // Order details returned to the logged-in customer
    public record MyOrder(
            Long id,
            String status,
            double totalAmount,
            LocalDateTime orderDate,
            String deliveryAddress,
            List<OrderItem> items) {
    }

    // Place an order; prices are calculated by the backend
    @PostMapping
    public CustomerOrder placeOrder(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        String name = text(request.get("customerName"));
        String phone = text(request.get("phone"));
        String address = text(request.get("deliveryAddress"));

        if (name.isBlank() || name.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter your name (max 100 characters)");
        }

        if (!phone.matches("\\d{10}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number must contain 10 digits");
        }

        if (address.isBlank() || address.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter a delivery address (max 500 characters)");
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomerName(name);
        order.setPhone(phone);
        order.setDeliveryAddress(address);

        // Associate the order with a logged-in customer only.
        // Guest checkout remains available.
        if (authentication != null
                && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_CUSTOMER"))) {
            order.setCustomerEmail(authentication.getName());
        }

        Object rawItems = request.get("items");

        if (!(rawItems instanceof List<?> items) || items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order must contain at least one item");
        }

        List<OrderItem> orderItems = items.stream()
                .map(rawItem -> {
                    if (!(rawItem instanceof Map<?, ?> item)) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid order item");
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setFoodItemId(
                            getLong(item.get("foodItemId")));
                    orderItem.setQuantity(
                            getInteger(item.get("quantity")));

                    if (orderItem.getQuantity() <= 0
                            || orderItem.getQuantity() > 50) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Quantity must be between 1 and 50");
                    }

                    return orderItem;
                })
                .toList();

        return orderService.placeOrder(order, orderItems);
    }

    // Orders belonging to the logged-in customer
    @GetMapping({"/my-orders", "/my"})
    public List<MyOrder> getMyOrders(
            Authentication authentication) {

        if (authentication == null
                || authentication.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority()
                        .equals("ROLE_CUSTOMER"))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Customer login is required");
        }

        return orderService
                .getOrdersForCustomer(authentication.getName())
                .stream()
                .map(o -> new MyOrder(
                        o.getId(),
                        o.getStatus(),
                        o.getTotalAmount(),
                        o.getOrderDate(),
                        o.getDeliveryAddress(),
                        orderItemRepository.findByOrderId(o.getId())))
                .toList();
    }

    // Customer can cancel their own pending order
    @PutMapping("/my/{id}/cancel")
    public CustomerOrder cancelMyOrder(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null
                || authentication.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority()
                        .equals("ROLE_CUSTOMER"))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Customer login is required");
        }

        return orderService.cancelOwnOrder(
                id, authentication.getName());
    }

    // ---------------- Admin endpoints ----------------

    @GetMapping
    public List<CustomerOrder> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public CustomerOrder getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/{id}/items")
    public List<OrderItem> getOrderItems(@PathVariable Long id) {
        orderService.getOrderById(id);
        return orderItemRepository.findByOrderId(id);
    }

    @PutMapping("/{id}/status")
    public CustomerOrder updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        return orderService.updateOrderStatus(
                id, request.get("status"));
    }

    // ---------------- Helpers ----------------

    private String text(Object value) {
        return value instanceof String s ? s.trim() : "";
    }

    private Long getLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid food item ID");
    }

    private int getInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid item quantity");
    }
}
