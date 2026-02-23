
package com.app.TechSphere.service;


import com.app.TechSphere.model.Product;
import com.app.TechSphere.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Fetch all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Fetch featured products (assuming 'featured' is a boolean field in Product)
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }

    // Fetch products by category slug
    public List<Product> getProductsByCategory(String categorySlug) {
        return productRepository.findByCategorySlug(categorySlug);
    }

    // Optional: fetch single product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    public List<Product> getRecommendedProducts() {
        // For now, just return first 5 products as a placeholder
        return productRepository.findAll().stream().limit(5).toList();
    }
    public long getProductCount() {
        return productRepository.count();
    }
    public List<Product> getAllDeals() {
    return productRepository.findAll()
            .stream()
            .filter(p -> p.getOriginalPrice() != null 
                      && p.getOriginalPrice() > p.getPrice())
            .toList();
}

}

