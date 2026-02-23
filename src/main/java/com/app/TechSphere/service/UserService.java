package com.app.TechSphere.service;

import com.app.TechSphere.model.User;
import com.app.TechSphere.model.User.Role;
import com.app.TechSphere.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Spring Security encoder

    // Register new user
    public User registerUser(User user) throws Exception {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("Email already in use");
        }
        
        // encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().name().isBlank()) {
            user.setRole(Role.CUSTOMER);
        }
        return userRepository.save(user);
    }

    // Authenticate user
    public User login(String email, String password) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) throw new Exception("Invalid email or password");

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid email or password");
        }

        return user;
    }

    // Get user by id
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public void makeAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

         user.setRole(Role.ADMIN);
        userRepository.save(user);
    }
    public void updateRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent demoting an admin if needed
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            throw new RuntimeException("Admins cannot be demoted");
        }

        // Prevent promoting a non-admin directly to ADMIN if you want separate logic
        if (newRole == Role.ADMIN && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only existing admins can stay ADMIN");
        }

        // For toggling CUSTOMER ↔ VENDOR
        if ((user.getRole() == Role.CUSTOMER || user.getRole() == Role.VENDOR) &&
            (newRole == Role.CUSTOMER || newRole == Role.VENDOR)) {
            user.setRole(newRole);
        }

        // For promoting/demoting ADMIN ↔ USER (if allowed)
        if (user.getRole() == Role.ADMIN || newRole == Role.ADMIN) {
            user.setRole(newRole);
        }

        userRepository.save(user);
    }

    public void approveVendor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.VENDOR);
        userRepository.save(user);
    }
    
    public void enableVendor(Long vendorId) {
        User user = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // Set the vendor as enabled
        user.setEnabled(true);

        // Optional: ensure the role is VENDOR (because Role is an enum)
        if (user.getRole() != Role.VENDOR) {
            user.setRole(Role.VENDOR);
        }

        userRepository.save(user);
    }

    public void disableVendor(Long vendorId) {
        User user = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        user.setEnabled(false);
        userRepository.save(user);
    }
    public User findByName(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public User save(User user) {
        return userRepository.save(user);
    }


}
