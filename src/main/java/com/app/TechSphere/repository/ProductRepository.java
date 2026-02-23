package com.app.TechSphere.repository;

import com.app.TechSphere.model.Product;
import com.app.TechSphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByFeaturedTrue();
    List<Product> findByCategorySlug(String slug);
    List<Product> findByVendor(User vendor);
    
    List<Product> findByFeaturedFalse();


}
