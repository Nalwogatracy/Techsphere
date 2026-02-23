package com.app.TechSphere.repository;

import com.app.TechSphere.model.Cart;
import com.app.TechSphere.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Cart findByUserId(Long userId);
    Optional<Cart> findByUser(User user);
}
