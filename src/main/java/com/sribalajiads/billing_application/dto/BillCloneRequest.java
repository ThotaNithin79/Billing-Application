package com.sribalajiads.billing_application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BillCloneRequest {
    @NotBlank(message = "Executive Name is required")
    private String executiveName;

    @NotBlank(message = "Client Name is required")
    private String clientName;

    @NotNull(message = "New Bill Start Date is required")
    private LocalDate billStartDate;

    @NotNull(message = "New Bill End Date is required")
    private LocalDate billEndDate;

    @NotBlank(message = "New Booking Order Number is required")
    private String bookingOrderNumber;

    private String workOrderNumber;
}