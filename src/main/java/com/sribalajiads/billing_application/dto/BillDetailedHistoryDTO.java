package com.sribalajiads.billing_application.dto;

import com.sribalajiads.billing_application.model.EBillStatus;
import com.sribalajiads.billing_application.model.EStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a full snapshot of a Bill at a specific point in history.
 * Used by any authenticated user to see a step-by-step audit trail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailedHistoryDTO {

    // --- 1. REVISION METADATA (From revinfo_custom) ---
    private int revisionId;
    private String modifiedBy;     // Email of the user who performed the action
    private LocalDateTime modifiedAt;
    private String revisionType;   // ADD, MOD, or DEL

    // --- 2. BILL IDENTITY & CORE DETAILS ---
    private Long billId;
    private String bookingOrderNumber;
    private String executiveName;
    private String clientName;
    private LocalDate billStartDate;
    private LocalDate billEndDate;

    // --- 3. STATE & WORKFLOW ---
    private EStatus status;         // ACTIVE / HOLD
    private EBillStatus billStatus; // BILL_RAISED, RO_CREATED, etc.

    // --- 4. ARTIFACTS & ATTACHMENTS ---
    private String workOrderNumber;
    private String workOrderAttachment;
    private String roAttachment;
    private String invoiceAttachment;
    private String eInvoiceAttachment;

    // --- 5. COMMUNICATION ---
    private String remarks;        // The message provided by the user during this revision
}