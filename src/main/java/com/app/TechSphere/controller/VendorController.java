package com.app.TechSphere.controller;

import com.app.TechSphere.model.OldProductSale;
import com.app.TechSphere.model.Product;
import com.app.TechSphere.model.User;
import com.app.TechSphere.service.CategoryService;
import com.app.TechSphere.service.OldProductSaleService;
import com.app.TechSphere.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/vendor")
public class VendorController {

    private final OldProductSaleService oldProductSaleService;
    private final UserService userService;
    private final CategoryService categoryService;

    public VendorController(OldProductSaleService oldProductSaleService,CategoryService categoryService,
                            UserService userService) {
        this.oldProductSaleService = oldProductSaleService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    /**
     * Vendor Dashboard
     */
    @GetMapping("/dashboard")
    public String vendorDashboard(Model model, Principal principal) {
        User vendor = userService.findByName(principal.getName());

        if (!vendor.isEnabled()) {
            return "vendor-pending-approval";
        }

        List<OldProductSale> sales = oldProductSaleService.getUserSales(vendor.getId());

        // Example stats
        long totalSales = sales.size();
        double totalRevenue = sales.stream()
                .mapToDouble(OldProductSale::getPrice)
                .sum();

        model.addAttribute("sales", sales);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("vendor", vendor);

        return "vendor-dashboard";
    }

    /**
     * Vendor Products (OldProductSale)
     */
    @GetMapping("/products")
    public String vendorProducts(Model model, Principal principal) {
        User vendor = userService.findByName(principal.getName());

        // If vendor is not enabled, show pending approval page
        if (!vendor.isEnabled()) {
            return "vendor-pending-approval";
        }

        // Get all products submitted by this vendor
        List<OldProductSale> vendorProducts = oldProductSaleService.getUserSales(vendor.getId());

        // Filter only approved products
        List<OldProductSale> approvedVendorProducts = vendorProducts.stream()
            .filter(OldProductSale::isApproved)
                .map(sale -> {
                sale.setShortDescription(
                    sale.getDescription() != null
                    ? sale.getDescription().substring(0, Math.min(sale.getDescription().length(), 30)) + "..."
                    : "No description"
                );
                return sale;
            })
            .toList();
        
        // Count of approved products
        int approvedCount = approvedVendorProducts.size();
        int pendingCount = vendorProducts.size() - approvedCount;
        double totalValue = approvedVendorProducts.stream().mapToDouble(OldProductSale::getPrice).sum();

        // Add to model
        model.addAttribute("products", approvedVendorProducts);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("vendor", vendor);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("totalValue", totalValue);

        return "vendor-products"; // Thymeleaf template
    }

    /**
     * Add new old product form
     */
    @GetMapping("/products/add")
    public String addProductForm(Model model, Principal principal) {
        User vendor = userService.findByName(principal.getName());

        if (!vendor.isEnabled()) {
            return "vendor-pending-approval";
        }

        model.addAttribute("sale", new OldProductSale());
        model.addAttribute("vendor", vendor);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "vendor-oldproduct-form"; // create this template
    }

    /**
     * Save new old product
     */
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute OldProductSale sale, Principal principal) {
        User vendor = userService.findByName(principal.getName());

        if (!vendor.isEnabled()) {
            return "vendor-pending-approval";
        }

        // Set user for this sale
        sale.setUser(vendor);

        // Pending approval by default
        sale.setApproved(false);

        oldProductSaleService.submitSale(sale);

        return "redirect:/vendor/products?success=Product submitted successfully";
    }

    /**
     * Edit old product form
     */
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, Principal principal) {
        User vendor = userService.findByName(principal.getName());

        if (!vendor.isEnabled()) {
            return "vendor-pending-approval";
        }

        OldProductSale sale = oldProductSaleService.getById(id);

        // Ensure this product belongs to the vendor
        if (!sale.getUser().getId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        model.addAttribute("sale", sale);
        model.addAttribute("vendor", vendor);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "vendor-oldproduct-form"; // same template for add/edit
    }

    /**
     * Update old product
     */
    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute OldProductSale updatedSale,
                                Principal principal) {
        User vendor = userService.findByName(principal.getName());

        if (!vendor.isEnabled()) {
            return "vendor-pending-approval";
        }

        OldProductSale existingSale = oldProductSaleService.getById(id);

        if (!existingSale.getUser().getId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        // Update fields
        existingSale.setProductName(updatedSale.getProductName());
        existingSale.setDescription(updatedSale.getDescription());
        existingSale.setPrice(updatedSale.getPrice());
        existingSale.setCategory(updatedSale.getCategory());
        existingSale.setCondition(updatedSale.getCondition());
        existingSale.setProductImage(updatedSale.getProductImage());
        existingSale.setOriginalPrice(updatedSale.getOriginalPrice());

        oldProductSaleService.submitSale(existingSale); // save updated sale

        return "redirect:/vendor/products?success=Product updated successfully";
    }

    /**
     * Delete old product
     */
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Principal principal) {
        User vendor = userService.findByName(principal.getName());

        if (!vendor.isEnabled()) {
            return "vendor-pending-approval";
        }

        OldProductSale sale = oldProductSaleService.getById(id);

        if (!sale.getUser().getId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        oldProductSaleService.rejectSale(id); // delete

        return "redirect:/vendor/products?success=Product deleted successfully";
    }
}
