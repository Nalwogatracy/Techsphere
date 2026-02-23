package com.app.TechSphere.config;


import com.app.TechSphere.model.Category;
import com.app.TechSphere.model.Product;
import com.app.TechSphere.model.User;
import com.app.TechSphere.model.User.Role;
import com.app.TechSphere.repository.CategoryRepository;
import com.app.TechSphere.repository.ProductRepository;
import com.app.TechSphere.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(CategoryRepository categoryRepo,
                           ProductRepository productRepo,
                           UserRepository userRepo,
                           PasswordEncoder encoder) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {

        // Default ADMIN
        if (userRepo.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@techsphere.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepo.save(admin);
        }

        // Categories
        if (categoryRepo.count() == 0) {
            Category laptops = new Category();
            laptops.setName("Laptops");
            categoryRepo.save(laptops);

            Category phones = new Category();
            phones.setName("Smartphones");
            categoryRepo.save(phones);
            
            Category hardwarespare = new Category();
            phones.setName("Hardware spare");
            categoryRepo.save(hardwarespare);
            
            Category software = new Category();
            phones.setName("Software");
            categoryRepo.save(software);
            
            Category Services = new Category();
            phones.setName("service");
            categoryRepo.save(Services);
            
            Category StorageDevices = new Category();
            phones.setName("StorageDevices");
            categoryRepo.save(StorageDevices);
            
            Category peripheral = new Category();
            phones.setName("peripheral");
            categoryRepo.save(peripheral);
            
        }

        // Products
        if (productRepo.count() == 0) {
            Category laptops = categoryRepo.findByName("Laptops");

            Product p1 = new Product();
            p1.setName("Dell XPS 13");
            p1.setDescription("13-inch Laptop");
            p1.setPrice(1200);
            p1.setStockQuantity(10);
            p1.setCategory(laptops);

            productRepo.save(p1);
        }
    }
}
