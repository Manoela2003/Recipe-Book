package recipes.recipeBook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import recipes.recipeBook.entity.Role;
import recipes.recipeBook.entity.User;
import recipes.recipeBook.repository.UserRepository;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.firstname}")
    private String adminFirstName;

    @Value("${app.admin.lastname}")
    private String adminLastName;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            System.out.println("No Admin user found. Generating default Admin account...");

            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setFirstName(adminFirstName);
            admin.setLastName(adminLastName);
            admin.setRole(Role.ADMIN);

            admin.setPassword(passwordEncoder.encode(adminPassword));

            userRepository.save(admin);

            System.out.println("Default Admin account created successfully!");
        } else {
            System.out.println("Admin account already exists. Skipping initialization.");
        }
    }
}