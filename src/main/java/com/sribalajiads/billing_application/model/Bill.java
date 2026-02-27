package com.sribalajiads.billing_application.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited; // <--- ADD THIS IMPORT
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Audited // <--- THIS SINGLE WORD TURNS ON COMPLETE HISTORY TRACKING
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    @NotBlank(message = "Booking Order Number is mandatory")
    @Column(name = "booking_order_number", unique = true, nullable = false)
    private String bookingOrderNumber;

    @NotBlank(message = "Executive Name is mandatory")
    @Column(name = "executive_name", nullable = false)
    private String executiveName;

    @NotBlank(message = "Client Name is mandatory")
    @Column(name = "client_name", nullable = false)
    private String clientName;

    @NotNull(message = "Bill Start Date is mandatory")
    @Column(name = "bill_start_date", nullable = false)
    private LocalDate billStartDate;

    @NotNull(message = "Bill End Date is mandatory")
    @Column(name = "bill_end_date", nullable = false)
    private LocalDate billEndDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EStatus status = EStatus.ACTIVE;

    @Column(name = "work_order_number")
    private String workOrderNumber;

    @Column(name = "work_order_attachment")
    private String workOrderAttachment;

    @Column(name = "ro_attachment")
    private String roAttachment;

    @Column(name = "invoice_attachment")
    private String invoiceAttachment;

    @Column(name = "e_invoice_attachment")
    private String eInvoiceAttachment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "bill_status", nullable = false, length = 30)
    private EBillStatus billStatus = EBillStatus.BILL_RAISED;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- NEW ATTRIBUTE FOR COMMUNICATION ---
    @Column(columnDefinition = "TEXT")
    private String remarks; // Holds messages from the user performing the action
}