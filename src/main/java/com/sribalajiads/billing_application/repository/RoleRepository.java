package com.sribalajiads.billing_application.repository;

import com.sribalajiads.billing_application.model.ERole;
import com.sribalajiads.billing_application.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    // This is the missing piece.
    // Spring translates this method name into: "SELECT * FROM roles WHERE name = ?"
    Optional<Role> findByName(ERole name);
}