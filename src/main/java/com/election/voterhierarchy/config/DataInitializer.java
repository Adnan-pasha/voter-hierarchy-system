package com.election.voterhierarchy.config;

import com.election.voterhierarchy.enums.Role;
import com.election.voterhierarchy.repository.UserRepository;
import com.election.voterhierarchy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    public DataInitializer(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if no users exist
        if (userRepository.count() == 0) {
            log.info("No users found. Creating default users...");

            // Create ADMIN user
            userService.createUser(
                "admin",
                "admin123",
                "System Administrator",
                "admin@voterhierarchy.com",
                Set.of(Role.ADMIN),
                "SYSTEM"
            );
            log.info("Default ADMIN user created: admin/admin123");

            // Create OPERATOR user
            userService.createUser(
                "operator",
                "operator123",
                "Data Entry Operator",
                "operator@voterhierarchy.com",
                Set.of(Role.OPERATOR),
                "SYSTEM"
            );
            log.info("Default OPERATOR user created: operator/operator123");

            // Create VIEWER user
            userService.createUser(
                "viewer",
                "viewer123",
                "Read-Only Viewer",
                "viewer@voterhierarchy.com",
                Set.of(Role.VIEWER),
                "SYSTEM"
            );
            log.info("Default VIEWER user created: viewer/viewer123");

            log.info("=================================================");
            log.info("Default users created successfully!");
            log.info("ADMIN:    admin/admin123");
            log.info("OPERATOR: operator/operator123");
            log.info("VIEWER:   viewer/viewer123");
            log.info("=================================================");
        }
    }
}