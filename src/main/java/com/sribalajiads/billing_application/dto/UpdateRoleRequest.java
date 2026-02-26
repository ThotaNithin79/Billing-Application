package com.sribalajiads.billing_application.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;

@Data
public class UpdateRoleRequest {
    @NotEmpty(message = "Roles cannot be empty")
    private Set<String> roles;
}