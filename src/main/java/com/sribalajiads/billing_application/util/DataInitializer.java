package com.sribalajiads.billing_application.util;

import com.sribalajiads.billing_application.model.*;
import com.sribalajiads.billing_application.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed Roles
        for (ERole eRole : ERole.values()) {
            if (roleRepository.findByName(eRole).isEmpty()) {
                roleRepository.save(new Role(eRole));
            }
        }

        // Seed Initial Admin
        if (!userRepository.existsByEmail("admin@sribalajiads.com")) {
            User admin = new User("Nithin Thota", "admin@sribalajiads.com", encoder.encode("Admin@123"));
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).get();
            admin.setRoles(Collections.singleton(adminRole));
            userRepository.save(admin);
        }
    }
}