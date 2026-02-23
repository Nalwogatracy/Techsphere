package com.app.TechSphere.repository;

import com.app.TechSphere.model.CartItem;
import com.app.TechSphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Optional: find all items by a specific cart
    List<CartItem> findByCartId(Long cartId);

    // Optional: find a specific product in a cart
    CartItem findByCartIdAndProductId(Long cartId, Long productId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user = :user")
    List<CartItem> findByUser(User user);
    
    Optional<CartItem> findByUserAndProduct_Id(User user, Long productId);


}
