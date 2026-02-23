
package com.app.TechSphere.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private double rating;
    private Integer reviewCount;

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    private Double originalPrice;

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

   @Transient
    public Double getDiscount() {
        if (originalPrice == null || originalPrice <= 0) {
            return 0.0;
        }
        double discount = ((originalPrice - price) / originalPrice) * 100;
        return Math.round(discount * 10.0) / 10.0;
    }
 
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderItem> orderItems = new ArrayList<>();

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    @Transient
    public int getSoldQuantity() {
        if (orderItems == null) return 0;
        return orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    @Transient
    public int getSoldPercentage() {
        int sold = getSoldQuantity();
        int total = sold + stockQuantity; // total is sold + remaining stock
        if (total == 0) return 0;
        return (sold * 100) / total;
    }
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProductImage> images = new ArrayList<>();

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("images");
    }

    private boolean featured= false;
    private String features;
    private LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    @Transient
    public String getIconClass() {
        return category != null ? category.getIconClass() : "fas fa-microchip";
    }
    
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private User vendor;

    public User getVendor() {
        return vendor;
    }

    public void setVendor(User vendor) {
        this.vendor = vendor;
    }
    @Transient
    public String getDealType() {

        // Flash = very high discount
        if (getDiscount() >= 50) {
            return "flash";
        }

        // Clearance = medium discount
        if (getDiscount() >= 30) {
            return "clearance";
        }

        // Bundle = maybe products with features containing "bundle"
        if (features != null && features.toLowerCase().contains("bundle")) {
            return "bundle";
        }

        // Exclusive = featured items
        if (featured) {
            return "exclusive";
        }

        // Limited = low stock
        if (stockQuantity <= 5) {
            return "limited";
        }

        return "normal";
    }
}

