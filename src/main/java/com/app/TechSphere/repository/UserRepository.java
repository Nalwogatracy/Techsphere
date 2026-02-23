package com.app.TechSphere.repository;

import com.app.TechSphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // for login
    boolean existsByEmail(String email);
    Optional<User> findByName(String name);// to prevent duplicate registration
}
