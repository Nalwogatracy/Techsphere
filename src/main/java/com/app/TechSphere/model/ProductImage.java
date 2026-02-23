package com.app.TechSphere.model;

import jakarta.persistence.*;

@Entity
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl; // stores file path or URL

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    
}
