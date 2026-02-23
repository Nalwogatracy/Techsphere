package com.app.TechSphere.service;

import com.app.TechSphere.model.Cart;
import com.app.TechSphere.model.CartItem;
import com.app.TechSphere.model.Product;
import com.app.TechSphere.model.User;
import com.app.TechSphere.model.User.Role;
import com.app.TechSphere.repository.CartItemRepository;
import com.app.TechSphere.repository.CartRepository;
import com.app.TechSphere.repository.ProductRepository;
import com.app.TechSphere.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserRepository userRepo;
    

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductService productService,
                       UserRepository userRepo,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.userRepo = userRepo;
    }

    // Get Cart Items for a User
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    // Add product to user's cart
    public void addToCart(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cartRepository.save(cart);
    }

    // Update a cart item
    public void updateCartItem(User user, Long productId, int quantity) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        cartRepository.save(cart);
    }

    // Remove from cart
    public void removeFromCart(User user, Long productId) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item != null) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
        }
    }

    // Count total cart items
    public int getCartItemCount(User user) {
        return getCartItems(user).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    

    // Merge guest session cart with user cart
    @Transactional
    public void mergeSessionCartWithUserCart(User user, HttpSession session) {
        if (user.getRole() != Role.CUSTOMER) return;
        Cart userCart = cartRepository.findByUserId(user.getId());
            if (userCart == null) {
            userCart = new Cart();
            userCart.setUser(user);
            cartRepository.save(userCart);
        }
            
        userCart.getItems().size(); 
        Map<Long, Integer> sessionCart = (Map<Long, Integer>) session.getAttribute("cart");
        if (sessionCart == null) return;

        sessionCart.forEach((productId, qty) -> addToCart(user, productId, qty));
        session.removeAttribute("cart");
    }
    
    
    public void addToCart(Cart cart, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cartRepository.save(cart);
    }
    
    public Cart findCartByUser(User user) {
        if (user == null) return null;
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        return cartOpt.orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }
    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    // In CartService.java
    public Cart getCartForCheckout(User user, HttpSession session) {
        if (user != null) {
            // fetch DB cart for user
            return findCartByUser(user);
        } else {
            // create a Cart object from sessionCart
            Map<Long, Integer> sessionCart = (Map<Long, Integer>) session.getAttribute("sessionCart");
            if (sessionCart == null || sessionCart.isEmpty()) return null;

            Cart cart = new Cart();
            List<CartItem> items = new ArrayList<>();
            sessionCart.forEach((productId, quantity) -> {
                Product product = productService.getProductById(productId);
                items.add(new CartItem(product, quantity));
            });
            cart.setItems(items);
            return cart;
        }
    }
    
    // In CartService.java
    public void clearCart(User user, HttpSession session) {
        if (user != null) {
            Cart cart = findCartByUser(user);
            if (cart != null) {
                cart.getItems().clear();
                saveCart(cart);
            }
        } else {
            session.removeAttribute("sessionCart");
        }
    }
    
    public void updateCart(Long productId,
                       int quantity,
                       Principal principal,
                       HttpSession session) {

        if (principal != null) {

            Optional<User> optionalUser =
                    userRepo.findByName(principal.getName());

            if (optionalUser.isEmpty()) {
                return; // or throw exception
            }

            User user = optionalUser.get();

            Optional<CartItem> optionalItem = 
        cartItemRepository.findByUserAndProduct_Id(user, productId);

        if (optionalItem.isPresent()) {
            CartItem item = optionalItem.get();
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }     

        } else {
            // Guest cart
            Map<Long, Integer> cart =
                    (Map<Long, Integer>) session.getAttribute("cart");

            if (cart != null && cart.containsKey(productId)) {
                cart.put(productId, quantity);
                session.setAttribute("cart", cart);
            }
        }
    }

}


