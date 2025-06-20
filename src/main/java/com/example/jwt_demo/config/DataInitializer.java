package com.example.jwt_demo.config;

import com.example.jwt_demo.model.Role;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(encoder.encode("testpwd"));
            admin.setRoles(Set.of(Role.ROLE_ADMIN));
            admin.setEmailVerified(true);	// don't need to verify default admin (for now)
            userRepository.save(admin);
            System.out.println("Default admin user created.");
        } else {
            System.out.println("Default admin user already exists.");
        }
    }
}
