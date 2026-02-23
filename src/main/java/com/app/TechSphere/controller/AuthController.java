package com.app.TechSphere.controller;

import com.app.TechSphere.model.User;
import com.app.TechSphere.model.User.Role;
import com.app.TechSphere.repository.UserRepository;
import com.app.TechSphere.service.CartService;
import com.app.TechSphere.service.CategoryService;
import com.app.TechSphere.service.ProductService;
import com.app.TechSphere.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final CartService cartService;


    public AuthController(UserRepository userRepo, PasswordEncoder encoder,ProductService productService,CategoryService categoryService,
            UserService userService,CartService cartService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.cartService = cartService;
    }

    // PUBLIC HOMEPAGE
    @GetMapping({"/", "/index"})
    public String publicHomePage(Principal principal,Model model,HttpSession session) {
         /*if (principal != null) {
        User user = userService.findByName(principal.getName());
        cartService.mergeSessionCartWithUserCart(user, session);
    } */

        System.out.println("PUBLIC HOME CONTROLLER HIT 🔥");
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index"; // public homepage template
        
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }

        return "login"; // login.html
    }
    
    @GetMapping("/user/login")
    public String userLoginPage(
            @RequestParam(value = "error", required = false) String error,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }

        return "user-login"; // user-login.html
    }
    
    // REGISTER PAGE
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // REGISTER PROCESS
    @PostMapping("/user/register")
    public String register(User user, Model model) {

        if (userRepo.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER); // default role
        System.out.println("LOGIN EMAIL: " + user.getEmail());
        System.out.println("DB PASSWORD: " + user.getPassword());


        userRepo.save(user);

        return "redirect:/user/login?registered";
    }
}
