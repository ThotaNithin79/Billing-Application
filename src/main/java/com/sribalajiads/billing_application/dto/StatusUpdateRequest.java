package com.sribalajiads.billing_application.dto;

import com.sribalajiads.billing_application.model.EStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull(message = "Status is required (ACTIVE or HOLD)")
    private EStatus status;
}