package com.app.TechSphere.controller;

import com.app.TechSphere.model.Category;
import com.app.TechSphere.model.OldProductSale;
import com.app.TechSphere.model.Product;
import com.app.TechSphere.model.User;
import com.app.TechSphere.model.User.Role;
import com.app.TechSphere.repository.UserRepository;
import com.app.TechSphere.service.CartService;
import com.app.TechSphere.service.CategoryService;
import com.app.TechSphere.service.OldProductSaleService;
import com.app.TechSphere.service.ProductService;
import com.app.TechSphere.service.UserService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final OldProductSaleService oldProductSaleService;

    @Autowired
    public UserController(ProductService productService, CategoryService categoryService,UserService userService,CartService cartService,UserRepository userRepository,OldProductSaleService oldProductSaleService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.oldProductSaleService = oldProductSaleService;
    }
    
    // USER HOMEPAGE AFTER LOGIN
    @GetMapping("/user/home")
    public String userHome(Model model,Principal principal) {
        List<Product> featuredProducts = productService.getFeaturedProducts();
        User user = userService.findByName(principal.getName());
        model.addAttribute("userId", user.getId());
        model.addAttribute("user", user);

        List<Category> categories = categoryService.getAllCategories();
        List<Product> allProducts = productService.getAllProducts();
        model.addAttribute("products", allProducts);
        
        
        model.addAttribute("pageCss", "fragments/mypage-css :: css");
        model.addAttribute("pageJs", "fragments/mypage-js :: js");
        model.addAttribute("content", "fragments/mypage-content :: content");
        model.addAttribute("title", "Future Technology Store");
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("categories", categories);
        model.addAttribute("cartCount", cartService.getCartItemCount(user));
        model.addAttribute("productCount", productService.getProductCount());
        model.addAttribute("categoryCount", categoryService.getCategoryCount());
        System.out.println("HOME METHOD CALLED ✅");

        return "user-dashboard"; // Thymeleaf template for logged-in users
    }

    @GetMapping("/user/products")
    public String productsPage(Model model) {
        List<Product> allProducts = productService.getAllProducts();
        model.addAttribute("title", "Products");
        model.addAttribute("products", allProducts);
        model.addAttribute("productCount", productService.getProductCount());
        model.addAttribute("categoryCount", categoryService.getCategoryCount());

        return "user-products";
    }

    @GetMapping("/user/categories")
    public String categoriesPage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "user-categories";
    }

    @GetMapping("/category/{slug}")
    public String categoryProducts(@PathVariable("slug") String slug, Model model) {
        
        Category category = categoryService.getCategoryBySlug(slug);
            if (category == null) {
            // redirect to home or show 404 page
            return "redirect:/"; 
            // or return "error/404";
        }
        List<Product> products = productService.getProductsByCategory(slug);

        model.addAttribute("category", category);
        model.addAttribute("products", products);

        return "user-category-products";
    }
    @GetMapping("/featured-categories")
    public String getFeaturedCategories(Model model) {
        List<Category> allCategories = categoryService.getAllCategories();
        List<Category> featuredCategories = allCategories.stream()
            .filter(Category::hasFeaturedProducts)
            .toList();
        model.addAttribute("featuredCategories", featuredCategories);
        return "user-categories"; // or whatever your template is called
    }
    @GetMapping("/user/deals")
    public String dealsPage(Model model) {
        model.addAttribute("products", productService.getFeaturedProducts());
        return "user-deals";
    }

    @GetMapping("/user/contact")
    public String contactPage() {
        return "user-contact";
    }

    @PostMapping("/user/request-vendor")
    public String requestVendor(Principal principal, RedirectAttributes redirectAttributes) {
        User user = userService.findByName(principal.getName());

        if (user.getRole() == Role.VENDOR) {
            redirectAttributes.addFlashAttribute("message", "You already requested vendor access.");
            return "redirect:/profile";
        }

        // Mark as pending vendor
        user.setRole(Role.VENDOR);
        user.setEnabled(false);  // pending approval
        userService.save(user);

        redirectAttributes.addFlashAttribute("message", "Vendor request submitted! Waiting for admin approval.");
        return "redirect:/profile";
    }
    @GetMapping("/old-sales/submit")
    public String showSubmitForm(Model model) {
        model.addAttribute("sale", new OldProductSale());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "vendor-old-sale-submit";
    }

    @PostMapping("/old-sales/submit")
    public String submitOldSale(@ModelAttribute OldProductSale sale,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        User vendor = userService.findByName(principal.getName());
        sale.setUser(vendor);
        sale.setApproved(false); // always pending
        oldProductSaleService.submitSale(sale);

        redirectAttributes.addFlashAttribute("success",
            "Your old product sale request has been submitted! Waiting for admin approval.");
        return "redirect:/vendor/dashboard";
    }
    @GetMapping("/deals")
    public String getDeals(@RequestParam(required = false) String type,
                           Model model) {

        List<Product> deals = productService.getAllDeals();

        if (type != null) {
            deals = deals.stream()
                    .filter(p -> type.equalsIgnoreCase(p.getDealType()))
                    .toList();
        }

        model.addAttribute("products", deals);
        model.addAttribute("selectedType", type);

        return "user-deals";
    }

}
