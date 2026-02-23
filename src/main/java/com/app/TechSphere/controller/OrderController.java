package com.app.TechSphere.controller;

import com.app.TechSphere.model.CartItem;
import com.app.TechSphere.model.Order;
import com.app.TechSphere.model.User;
import com.app.TechSphere.service.CartService;
import com.app.TechSphere.service.OrderService;
import com.app.TechSphere.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;

    public OrderController(OrderService orderService, UserService userService, CartService cartService) {
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
    }

    // ===================== USER VIEWS =====================

    // View all orders for logged-in user
    @GetMapping("")
    public String myOrders(Model model, Principal principal) {
        User user = userService.findByName(principal.getName());
        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("user", user); 
        model.addAttribute("orders", orders);
        long totalOrders = orders.size();
        long pendingCount = orders.stream()
                                  .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                                  .count();
        long deliveredCount = orders.stream()
                                    .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                                    .count();
        double totalSpent = orders.stream()
                                  .mapToDouble(Order::getTotalAmount)
                                  .sum();
        
        for (Order order : orders) {
            double paid = order.getPaidAmount() != null ? order.getPaidAmount() : 0;  // default to 0 if null
            double progress = order.getTotalAmount() != 0 
                ? (paid / order.getTotalAmount()) * 100 
                : 0;
            order.setPaymentProgress(progress);
        }
        model.addAttribute("orders", orders);

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("totalSpent", totalSpent);
        
        
        return "orders"; // Thymeleaf page: orders.html
    }

    // Checkout: create order from user's cart
    /* @PostMapping("/checkout")
    public String checkout(HttpSession session, Principal principal, Model model) {
        User user = userService.findByName(principal.getName());
        List<CartItem> cartItems = cartService.getCartItems(user);

        if (cartItems.isEmpty()) {
            model.addAttribute("error", "Your cart is empty!");
            return "cart";
        }

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // Create and save order
        Order order = new Order();
        order.setUser(user);
        order.setItems(cartItems);
        order.setTotalAmount(total);
        orderService.saveOrder(order);

        // Clear user's cart
        cartItems.forEach(item -> cartService.removeFromCart(user, item.getProduct().getId()));

        // Optionally merge guest session cart if needed
        session.removeAttribute("cart");

        model.addAttribute("success", "Order placed successfully! Order ID: " + order.getId());
        return "redirect:/orders";   
    } */

    // ===================== ADMIN VIEWS =====================

    // Admin view all orders
    @GetMapping("/admin")
    public String allOrders(Model model,Principal principal ) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        User user = userService.findByName(principal.getName());
        model.addAttribute("user", user); 
        long totalOrders = orders.size();
        long pendingCount = orders.stream()
                                  .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                                  .count();
        long deliveredCount = orders.stream()
                                    .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                                    .count();
        double totalSpent = orders.stream()
                                  .mapToDouble(Order::getTotalAmount)
                                  .sum();
        double totalRevenue = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
        long shippedCount = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.SHIPPED)
                .count();

        model.addAttribute("shippedCount", shippedCount);


        model.addAttribute("totalRevenue", totalRevenue);


        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("totalSpent", totalSpent);

        return "admin-orders"; // Thymeleaf page: admin/orders.html
    }
        
    
     // Admin updates order status
    @PostMapping("/admin/{id}/update")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        Order order = orderService.getById(id);
        orderService.updateStatus(order, status);

        // Notify user (optional via email)
        return "redirect:/orders/admin";
    }
}
