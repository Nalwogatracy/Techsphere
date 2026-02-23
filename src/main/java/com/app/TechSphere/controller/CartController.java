package com.app.TechSphere.controller;

import com.app.TechSphere.config.NotificationService;
import com.app.TechSphere.model.Cart;
import com.app.TechSphere.model.CartItem;
import com.app.TechSphere.model.Order;
import com.app.TechSphere.model.Product;
import com.app.TechSphere.model.User;
import com.app.TechSphere.service.CartService;
import com.app.TechSphere.service.OrderService;
import com.app.TechSphere.service.ProductService;
import com.app.TechSphere.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    public CartController(CartService cartService, ProductService productService, UserService userService,OrderService orderService,NotificationService notificationService) {
        this.cartService = cartService;
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @GetMapping("")
    public String viewCart(HttpSession session, Model model, Principal principal) {
        int cartItemCount = 0;
        double subtotal = 0.0;
        List<CartItem> cartItems = null;

        if (principal == null) {
            // Guest cart (session)
            Map<Long, Integer> sessionCart = (Map<Long, Integer>) session.getAttribute("cart");
            if (sessionCart == null) sessionCart = Map.of();

            cartItems = sessionCart.entrySet().stream().map(entry -> {
                Product product = productService.getProductById(entry.getKey());
                CartItem item = new CartItem();
                item.setProduct(product);
                item.setQuantity(entry.getValue());
                return item;
            }).toList();

        } else {
            // Authenticated user
            User user = userService.findByName(principal.getName());
            cartItems = cartService.getCartItems(user);
        }

        // Calculate totals
        for (CartItem item : cartItems) {
            subtotal += item.getProduct().getPrice() * item.getQuantity();
            cartItemCount += item.getQuantity();
        }

        // Promo code
        String promoCode = (String) session.getAttribute("promoCode");
        double discount = 0.0;
        if (promoCode != null) {
            switch (promoCode.toUpperCase()) {
                case "SAVE10" -> discount = subtotal * 0.10;
                case "TECH20" -> discount = subtotal * 0.20;
                case "WELCOME5" -> discount = subtotal * 0.05;
            }
        }

        double subtotalAfterDiscount = subtotal - discount;
        double shipping = subtotalAfterDiscount > 100 ? 0 : 15.0;
        double tax = subtotalAfterDiscount * 0.085;
        double total = subtotalAfterDiscount + shipping + tax;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("subtotal", String.format("%.2f", subtotal));
        model.addAttribute("discount", String.format("%.2f", discount));
        model.addAttribute("subtotalAfterDiscount", String.format("%.2f", subtotalAfterDiscount));
        model.addAttribute("shippingCost", shipping);
        model.addAttribute("tax", String.format("%.2f", tax));
        model.addAttribute("total", String.format("%.2f", total));
        model.addAttribute("promoApplied", promoCode != null);
        model.addAttribute("promoCode", promoCode);

        // Recommended products
        model.addAttribute("recommendedProducts", productService.getRecommendedProducts());

        return "cart";
    }
    @PostMapping("/remove/{productId}")
    @ResponseBody
    public Map<String, Object> removeFromCart(@PathVariable Long productId,
                                             HttpSession session,
                                             Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            int cartItemCount = 0;

            if (principal == null) {
                // Guest user - session cart
                Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
                if (cart != null) {
                    cart.remove(productId);
                    session.setAttribute("cart", cart);
                    // Update cart count
                    cartItemCount = cart.values().stream().mapToInt(Integer::intValue).sum();
                }
            } else {
                // Authenticated user - database cart
                User user = userService.findByName(principal.getName());
                cartService.removeFromCart(user, productId);
                cartItemCount = cartService.getCartItemCount(user);
            }

            response.put("success", true);
            response.put("cartItemCount", cartItemCount);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
    // Add product to cart
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Long productId,
                                         @RequestParam(defaultValue = "1") Integer quantity,
                                         HttpSession session,
                                         Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            int cartItemCount = 0;

            if (principal == null) {
                // Guest user
                Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
                if (cart == null) cart = new HashMap<>();

                cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
                session.setAttribute("cart", cart);

                // Update cart count
                cartItemCount = cart.values().stream().mapToInt(Integer::intValue).sum();
            } else {
                // Logged-in user
                User user = userService.findByName(principal.getName());
                cartService.addToCart(user, productId, quantity); 
                cartItemCount = cartService.getCartItemCount(user);
            }

            response.put("success", true);
            response.put("cartItemCount", cartItemCount);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    // Apply promo code
    @PostMapping("/apply-promo")
    @ResponseBody
    public Map<String, Object> applyPromo(@RequestParam String promoCode, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isValid = "SAVE10".equalsIgnoreCase(promoCode) ||
                              "TECH20".equalsIgnoreCase(promoCode) ||
                              "WELCOME5".equalsIgnoreCase(promoCode);

            if (isValid) {
                session.setAttribute("promoCode", promoCode.toUpperCase());
                response.put("valid", true);
            } else {
                response.put("valid", false);
                response.put("message", "Invalid promo code");
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Remove promo code
    @PostMapping("/remove-promo")
    @ResponseBody
    public Map<String, Object> removePromo(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        session.removeAttribute("promoCode");
        response.put("success", true);
        return response;
    }
    
    @PostMapping("/checkout")
    public String processCheckout(Principal principal, HttpSession session,Model model,RedirectAttributes redirectAttributes) {
        // 1. Get user
        User user = userService.findByName(principal.getName());

        // 2. Get cart (session or DB)
        Cart cart = cartService.getCartForCheckout(user, session);

        // 3. Create order
        Order order = orderService.createOrder(cart, user);

        // 4. Notify admin and user (WebSocket + email)
        notificationService.notifyNewOrder(order);

        // 5. Clear cart
        cartService.clearCart(user, session);

        // 6. Redirect to confirmation page
            redirectAttributes.addFlashAttribute(
            "success",
            "Order placed successfully! Order ID: " + order.getId()
        );
        return "redirect:/orders";
    }
    
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            Principal principal,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            cartService.updateCart(productId, quantity, principal, session);
            response.put("success", true);
        } catch (Exception e) {
            e.printStackTrace(); // VERY IMPORTANT
            response.put("success", false);
        }

        return response;
    }
}