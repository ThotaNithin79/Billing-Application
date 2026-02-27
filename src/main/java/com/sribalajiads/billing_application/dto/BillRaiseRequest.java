package com.sribalajiads.billing_application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BillRaiseRequest {

    @NotBlank(message = "Executive Name is required")
    private String executiveName;

    @NotBlank(message = "Client Name is required")
    private String clientName;

    @NotNull(message = "Bill Start Date is required")
    private LocalDate billStartDate;

    @NotNull(message = "Bill End Date is required")
    private LocalDate billEndDate;

    @NotBlank(message = "Booking Order Number is required")
    private String bookingOrderNumber;

    // These can be blank if the work order isn't provided immediately
    private String workOrderNumber;

    // This will hold the path/URL of the uploaded file
    private String workOrderAttachment;

    private String remarks;

}