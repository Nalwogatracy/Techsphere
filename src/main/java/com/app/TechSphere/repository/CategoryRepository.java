package com.app.TechSphere.repository;

import com.app.TechSphere.model.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByName(String name);
    Optional<Category> findBySlug(String slug);

}
