
package com.lostlink.config;

import com.lostlink.model.CampusUser;
import com.lostlink.model.Role;
import com.lostlink.repository.UserRepository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DemoUserSeeder {

    @Bean
    ApplicationRunner seedDemoUsers(
            UserRepository repository,
            PasswordEncoder encoder) {

        return args -> {
            String password =
                    System.getenv("LOSTLINK_DEMO_PASSWORD");

            if (password == null || password.length() < 12) {
                throw new IllegalStateException(
                    "Set LOSTLINK_DEMO_PASSWORD " +
                    "to at least 12 characters."
                );
            }

            addUser(repository, encoder,
                "DEMO-STU001", "Aarav Kumar",
                Role.STUDENT, password);

            addUser(repository, encoder,
                "DEMO-STU002", "Priya Sharma",
                Role.STUDENT, password);

            addUser(repository, encoder,
                "DEMO-FAC001", "Dr. Meera",
                Role.FACULTY, password);

            addUser(repository, encoder,
                "DEMO-WRK001", "Ravi Kumar",
                Role.OTHERS, password);

            addUser(repository, encoder,
                "DEMO-ADM001", "Security Manager",
                Role.ADMIN, password);
        };
    }

    private void addUser(
            UserRepository repository,
            PasswordEncoder encoder,
            String campusId,
            String name,
            Role role,
            String password) {

        if (repository.existsByCampusId(campusId)) {
            return;
        }

        CampusUser user = new CampusUser();
        user.setCampusId(campusId);
        user.setName(name);
        user.setRole(role);
        user.setPasswordHash(encoder.encode(password));
        user.setMustChangePassword(true);
        user.setEnabled(true);

        repository.save(user);

        System.out.println("Created demo user: " + campusId);
    }
}
