package com.sribalajiads.billing_application.controller;

import com.sribalajiads.billing_application.dto.SignupRequest;
import com.sribalajiads.billing_application.dto.UpdateRoleRequest;
import com.sribalajiads.billing_application.model.*;
import com.sribalajiads.billing_application.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Secures all endpoints in this controller
public class AdminController {

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder encoder;

    // 1. Create User
    @PostMapping("/create-user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = new User(signUpRequest.getName(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Role is mandatory!");
        }

        strRoles.forEach(role -> {
            Role userRole = roleRepository.findByName(ERole.valueOf(role))
                    .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found."));
            roles.add(userRole);
        });

        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    // 2. Update User Roles (With Last Admin Protection)
    @PutMapping("/update-role/{id}")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Check if this user is currently an Admin
        boolean isCurrentlyAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(ERole.ROLE_ADMIN));

        // Check if the new request is trying to REMOVE the Admin role
        boolean willBeAdmin = request.getRoles().contains("ROLE_ADMIN");

        if (isCurrentlyAdmin && !willBeAdmin) {
            long activeAdminCount = userRepository.countByRolesNameAndActiveTrue(ERole.ROLE_ADMIN);
            if (activeAdminCount <= 1) {
                return ResponseEntity.badRequest().body("Safety Alert: You cannot remove the Admin role from the last active Admin.");
            }
        }

        // Apply new roles
        Set<Role> newRoles = new HashSet<>();
        request.getRoles().forEach(roleStr -> {
            Role role = roleRepository.findByName(ERole.valueOf(roleStr))
                    .orElseThrow(() -> new RuntimeException("Error: Role " + roleStr + " is not found."));
            newRoles.add(role);
        });

        user.setRoles(newRoles);
        userRepository.save(user);
        return ResponseEntity.ok("User roles updated successfully!");
    }

    // 3. Toggle Status (With Last Admin Protection)
    @PatchMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Check if we are trying to DEACTIVATE an Admin
        boolean isCurrentlyAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(ERole.ROLE_ADMIN));

        if (user.isActive() && isCurrentlyAdmin) {
            long activeAdminCount = userRepository.countByRolesNameAndActiveTrue(ERole.ROLE_ADMIN);
            if (activeAdminCount <= 1) {
                return ResponseEntity.badRequest().body("Safety Alert: You cannot deactivate the last active Admin.");
            }
        }

        user.setActive(!user.isActive());
        userRepository.save(user);
        return ResponseEntity.ok("User status updated to: " + (user.isActive() ? "Active" : "Inactive"));
    }

    // 4. Get All Users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}