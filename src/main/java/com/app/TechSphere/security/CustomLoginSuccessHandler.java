package com.app.TechSphere.security;

import com.app.TechSphere.model.Product;
import com.app.TechSphere.model.User;
import com.app.TechSphere.repository.UserRepository;
import com.app.TechSphere.service.CartService;
import com.app.TechSphere.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
     private final UserRepository userRepository;
     private final ProductService productService;
     private final CartService cartService;
    public CustomLoginSuccessHandler(UserRepository userRepository, ProductService productService,CartService cartService) {
        this.userRepository = userRepository;
        this.productService = productService;
        this.cartService = cartService;
    }
    

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
             String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow();

            user.setLastLogin(LocalDateTime.now()); // ✅ update last login
            userRepository.save(user);

            cartService.mergeSessionCartWithUserCart(
                    user,
                    request.getSession()
            );


        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
                return;
            } else if (authority.getAuthority().equals("ROLE_CUSTOMER")) {
                response.sendRedirect("/user/home");
                return;
            }
            else if (authority.getAuthority().equals("ROLE_VENDOR")) {
                response.sendRedirect("/vendor/dashboard");
                return;
            }
        }

        response.sendRedirect("/");
    }
    
}

