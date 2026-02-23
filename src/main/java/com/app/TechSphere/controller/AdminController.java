
package com.app.TechSphere.controller;

import com.app.TechSphere.config.NotificationService;
import com.app.TechSphere.model.Category;
import com.app.TechSphere.model.ContactMessage;
import com.app.TechSphere.model.OldProductSale;
import com.app.TechSphere.model.Order;
import com.app.TechSphere.model.Product;
import com.app.TechSphere.model.ProductImage;
import com.app.TechSphere.model.User;
import com.app.TechSphere.model.User.Role;
import com.app.TechSphere.repository.CategoryRepository;
import com.app.TechSphere.repository.ProductRepository;
import com.app.TechSphere.service.CategoryService;
import com.app.TechSphere.service.ContactService;
import com.app.TechSphere.service.OldProductSaleService;
import com.app.TechSphere.service.OrderService;
import com.app.TechSphere.service.ProductService;
import com.app.TechSphere.service.UserService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;
import com.itextpdf.text.Document;



@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final OldProductSaleService oldProductSaleService;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepo;
    private final OrderService orderService;
    private final ContactService contactService;
    private final NotificationService notificationService;

    public AdminController(UserService userService,
                           ProductService productService,
                           ProductRepository productRepository,
                           CategoryService categoryService,
                           CategoryRepository categoryRepo,
                           OrderService orderService,
                           ContactService contactService,
                           OldProductSaleService oldProductSaleService,
                           NotificationService notificationService) {
        this.userService = userService;
        this.productService = productService;
        this.oldProductSaleService = oldProductSaleService;
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.categoryRepo = categoryRepo;
        this.orderService = orderService;
        this.contactService = contactService;
        this.notificationService = notificationService;
    }

    // Admin dashboard
   @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Product> products = productService.getAllProducts();
        int totalStock = products.stream().mapToInt(Product::getStockQuantity).sum();
        long featuredCount = products.stream().filter(Product::isFeatured).count();
        productRepository.findByFeaturedTrue();
                List<ContactMessage> messages = contactService.getAllMessages()
                .stream()
                .filter(msg -> !msg.isReplied())
                .toList();

        model.addAttribute("contactMessages", messages);


        List<OldProductSale> pendingSales = oldProductSaleService.findPendingSales();
         List<User> users = userService.findAll();
        List<User> pendingVendorList = users.stream()
        .filter(u -> u.getRole() == Role.VENDOR && !u.isEnabled())
        .toList();

        int activeCustomers = (int) users.stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
        int vendors = (int) users.stream().filter(u -> u.getRole() == Role.VENDOR).count();
        int admins = (int) users.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        int newToday = (int) users.stream().filter(u -> u.getCreatedAt().toLocalDate().equals(java.time.LocalDate.now())).count();
        int totalUsers = userService.findAll().size();
        int pendingApprovals = oldProductSaleService.findPendingSales().size();
        
       
        
       List<Map<String, String>> pendingActivities = new ArrayList<>();
        for (OldProductSale sale : pendingSales) {
            Map<String, String> activity = new HashMap<>();
            activity.put("type", "warning"); // For CSS icon color
            activity.put("title", "Sale Needs Approval");
            activity.put("description", sale.getProductName() + " by " + sale.getUser().getName());
            activity.put("time", formatTimeAgo(sale.getSaleDate()));
            activity.put("link", "/admin/old-sales"); // Link to approve
            pendingActivities.add(activity);
        }
        for (User vendor : pendingVendorList) {  // <- use pendingVendorList here
        Map<String, String> activity = new HashMap<>();
        activity.put("type", "new-user"); 
        activity.put("title", "New Vendor Request");
        activity.put("description", vendor.getName() + " wants to join as vendor");
        activity.put("time", formatTimeAgo(vendor.getCreatedAt()));
      // activity.put("link", "/admin/users");
        activity.put("link", "/admin/users/approve/" + vendor.getId());
        pendingActivities.add(activity);
    }
        
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("featuredCount", featuredCount);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pendingApprovals", pendingApprovals);
        model.addAttribute("activeCustomers", activeCustomers);
        model.addAttribute("vendors", vendors);
        model.addAttribute("admins", admins);
        model.addAttribute("newToday", newToday);
        model.addAttribute("pendingActivities", pendingActivities);
        model.addAttribute("products", products);

        return "admin-dashboard"; // your Thymeleaf template
    }
        private String formatTimeAgo(LocalDateTime time) {
        java.time.Duration duration = java.time.Duration.between(time, LocalDateTime.now());
        if (duration.toMinutes() < 60) return duration.toMinutes() + "m ago";
        if (duration.toHours() < 24) return duration.toHours() + "h ago";
        return duration.toDays() + "d ago";
    }


    // Manage users
    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAll();
        Map<Long, String> initialsMap = new HashMap<>();
    for (User u : users) {
        initialsMap.put(u.getId(), getInitials(u.getName()));
    }
        model.addAttribute("users", userService.findAll());
        model.addAttribute("initialsMap", initialsMap);
        return "admin-users";
    }
    private String getInitials(String name) {
    if (name == null || name.isEmpty()) return "";
    String[] parts = name.split(" ");
    String initials = parts[0].substring(0, 1);
    if (parts.length > 1) {
        initials += parts[1].substring(0, 1);
    }
    return initials.toUpperCase();
}
    // Manage products
    @GetMapping("/products")
    public String manageProducts(@RequestParam(required = false) String featured,Model model) {
        List<Product> products;
        if ("featured".equals(featured)) {
        products = productRepository.findByFeaturedTrue();
        } 
        else if ("not-featured".equals(featured)) {
            products = productRepository.findByFeaturedFalse();
        } 
        else {
            products = productService.getAllProducts();
        }

         int totalStock = products.stream()
                             .mapToInt(Product::getStockQuantity)
                             .sum();
    model.addAttribute("totalStock", totalStock);

    // Featured count = number of products with featured = true
    long featuredCount = products.stream()
                                 .filter(Product::isFeatured)
                                 .count();
    model.addAttribute("featuredCount", featuredCount);

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin-products";
    }
    @PostMapping("/products/toggle-featured/{id}")
    public String toggleFeatured(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        product.setFeatured(!product.isFeatured());
        productRepository.save(product);

        return "redirect:/admin/products";
    }

    
    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        // Create an empty Product object to bind the form
        model.addAttribute("product", new Product());

        // Optional: if you have categories or other dropdowns
        model.addAttribute("categories", categoryService.getAllCategories());

        return "admin-add-product"; // Thymeleaf template for the add product page
    }

   @PostMapping("/products/add")
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam("imageFiles") MultipartFile[] files
    ) throws IOException {

        // Initialize images list if null
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }

        // First save product to generate ID (needed for image association)
        Product savedProduct = productRepository.save(product);

        // Directory to store uploaded images permanently
        String uploadDir = "uploads/products/"; // relative to your app's working directory


        // Make sure the folder exists
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // Generate unique file name to prevent overwriting
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                // Save file to the static folder
                Path filePath = Paths.get(uploadDir + fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Create ProductImage entity
                ProductImage img = new ProductImage();
                img.setImageUrl("/product-images/" + fileName); // URL for frontend
                img.setProduct(savedProduct);

                // Add image to product
                savedProduct.getImages().add(img);
            }
        }

        // Save again to persist images (CascadeType.ALL will handle it)
        productRepository.save(savedProduct);

        return "redirect:/admin/products";
    }
    
    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin-edit-product";
    }

    
    // Approve old product sales
    @GetMapping("/old-sales")
    public String approveOldSales(Model model) {
        model.addAttribute("sales", oldProductSaleService.findPendingSales());
        return "admin-old-sales";
    }

   @PostMapping("/old-sales/{id}/approve")
    public String approveSale(@PathVariable Long id) {
        // 1️⃣ Fetch the sale first
        OldProductSale sale = oldProductSaleService.getById(id); // make sure this method exists

        // 2️⃣ Approve it
        oldProductSaleService.approveSale(id);

        // 3️⃣ Notify vendor
        notificationService.notifyUserEmail(
            sale.getUser().getEmail(),
            "Your product sale has been approved",
            "Hello " + sale.getUser().getName() + ",\n\n" +
            "Your old product sale request for '" + sale.getProductName() + "' has been approved by admin.\n" +
            "You can now see it on the marketplace."
        );

        // 4️⃣ Redirect back
        return "redirect:/admin/old-sales";
    }

    // Change user role (USER ↔ ADMIN)
    @PostMapping("/users/update-role")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserRole(
            @RequestParam Long userId,
            @RequestParam String role
    ) {
        userService.updateRole(userId, Role.valueOf(role));
        return "redirect:/admin/users";
    }


    // Delete user
    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return "redirect:/admin/users";
    }
    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin-add-user";
    }
    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user) throws Exception {
        userService.registerUser(user);
        return "redirect:/admin/users";
    }
    @PostMapping("/users/approve/{id}")
    public String approveVendor(@PathVariable Long id) {
        userService.enableVendor(id);
        return "redirect:/admin/users";
    }
    @PostMapping("/users/reject/{id}")
    public String rejectVendor(@PathVariable Long id) {
        userService.disableVendor(id); // custom method to mark rejected
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        return "admin-categories";
    }

    @PostMapping("/categories")
    public String saveCategory(Category category, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fix the errors");
            return "redirect:/admin/categories";
        }

        // Auto-generate slug from name
        String slug = category.getName().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        category.setSlug(slug);

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("success", "Category added successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}")
    @ResponseBody
    public Category getCategory(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PostMapping("/categories/edit")
    public String editCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        // Update slug if name changed
        Category existing = categoryService.findById(category.getId());
        if (!existing.getName().equals(category.getName())) {
            String slug = category.getName().toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-|-$", "");
            category.setSlug(slug);
        }

        categoryService.update(category);
        redirectAttributes.addFlashAttribute("success", "Category updated!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Category deleted!");
        return "redirect:/admin/categories";
    }
    @GetMapping("/messages")
    public String listMessages(Model model) {
        List<ContactMessage> messages = contactService.getAllMessages(); // make sure this exists
        long unreadCount = messages.stream().filter(m -> !m.isReplied()).count();
        long repliedCount = messages.stream().filter(ContactMessage::isReplied).count();

        model.addAttribute("messages", messages);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("repliedCount", repliedCount);
        model.addAttribute("totalMessages", messages.size());

        return "admin-messages"; // Thymeleaf template
    }

    @GetMapping("/message/view/{id}")
    public String viewMessage(@PathVariable Long id, Model model) {
        ContactMessage message = contactService.getMessageById(id);
        message.setReplied(true);
        contactService.saveMessage(message);
        model.addAttribute("message", message);
        return "admin-message-view";
    }
    @GetMapping("/message/reply/{id}")
    public String replyMessage(@PathVariable Long id, Model model) {
        ContactMessage msg = contactService.getMessageById(id);
        model.addAttribute("message", msg);
        return "admin-message-reply"; // create a template with a reply form
    }

    @PostMapping("/message/reply/{id}")
    public String sendReply(@PathVariable Long id, @RequestParam String replyContent) {
        ContactMessage originalMsg = contactService.getMessageById(id);

        // You can use NotificationService to email the reply
        notificationService.notifyUserEmail(originalMsg.getEmail(), 
            "Reply from Admin: " + originalMsg.getSubject(), replyContent);

        return "redirect:/admin/dashboard?replySent=true";
    }
    @GetMapping("/users/approve/{id}")
    public String approveVendorGet(@PathVariable Long id) {
        userService.enableVendor(id);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/products/report/pdf")
public void exportProductsToPdf(HttpServletResponse response) throws Exception {

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=products_report.pdf");

    List<Product> products = productService.getAllProducts();

    // Using iText 5 example
    com.itextpdf.text.Document document = new com.itextpdf.text.Document();
    com.itextpdf.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());

    document.open();

    document.add(new com.itextpdf.text.Paragraph("TechSphere Products Report"));
    document.add(new com.itextpdf.text.Paragraph(" "));

    for (Product product : products) {
        document.add(new com.itextpdf.text.Paragraph(
                "Name: " + product.getName() +
                " | Price: " + product.getPrice() +
                " | Stock: " + product.getStockQuantity()
        ));
    }

    document.close();
}

 
}


