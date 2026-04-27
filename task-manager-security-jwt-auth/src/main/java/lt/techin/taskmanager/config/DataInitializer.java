package lt.techin.taskmanager.config;

import lt.techin.taskmanager.model.Role;
import lt.techin.taskmanager.model.User;
import lt.techin.taskmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByEmail("admin@example.com")) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole(Role.ADMIN);

                repo.save(admin);
            }
        };
    }
}
