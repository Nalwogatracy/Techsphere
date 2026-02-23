package com.app.TechSphere.service;

import com.app.TechSphere.model.Cart;
import com.app.TechSphere.model.CartItem;
import com.app.TechSphere.model.Order;
import com.app.TechSphere.model.OrderItem;
import com.app.TechSphere.model.User;
import com.app.TechSphere.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void updateStatus(Order order, Order.OrderStatus status) {
        order.setStatus(status);
        orderRepository.save(order);
    }
     public Order getById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
     public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
     
     public Order createOrder(Cart cart, User user) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // 1. Create a new order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING); // ⚡ Use enum, not string
        order.setCreatedAt(LocalDateTime.now());

        // 2. Add cart items to order
        List<CartItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            CartItem oi = new CartItem(cartItem.getProduct(), cartItem.getQuantity());
            oi.setOrder(order);
            orderItems.add(oi);
        }
        order.setItems(orderItems);

        // 3. Calculate total
        double total = orderItems.stream()
                           .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                           .sum();
        order.setTotalAmount(total);

        // 4. Save order
        return orderRepository.save(order);
    }

}
