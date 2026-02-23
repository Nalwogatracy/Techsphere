package com.app.TechSphere.controller;

import com.app.TechSphere.model.User;
import com.app.TechSphere.model.Order;
import com.app.TechSphere.service.UserService;
import com.app.TechSphere.service.OrderService;
import java.security.Principal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final OrderService orderService;

    public ProfileController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal User currentUser, Model model,Principal principal) {
        // Load user info
        if (principal != null) {
        User user = userService.findByName(principal.getName());
        model.addAttribute("user", user); // <-- must not be null
    }
        if (principal == null) {
        return "redirect:/login";
    }
        

        // Load user orders
        List<Order> orders = orderService.getOrdersByUser(currentUser);
        model.addAttribute("orders", orders);

        // You can also add addresses and wishlist if you have entities
        // model.addAttribute("addresses", currentUser.getAddresses());

        return "user-profile";
    }
}
