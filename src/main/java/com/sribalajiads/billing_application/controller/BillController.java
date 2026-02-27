package com.sribalajiads.billing_application.controller;

import com.sribalajiads.billing_application.dto.BillCloneRequest;
import com.sribalajiads.billing_application.dto.BillDetailedHistoryDTO;
import com.sribalajiads.billing_application.dto.PlannerBillUpdateRequest;
import com.sribalajiads.billing_application.dto.StatusUpdateRequest;
import com.sribalajiads.billing_application.model.Bill;
import com.sribalajiads.billing_application.service.BillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    // --- 1. PLANNER ACTIONS ---

    /**
     * Initial stage: Planner raises a new bill.
     */
    @PostMapping(value = "/raise", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<?> raiseBill(
            @RequestParam("executiveName") String executiveName,
            @RequestParam("clientName") String clientName,
            @RequestParam("billStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate billStartDate,
            @RequestParam("billEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate billEndDate,
            @RequestParam("bookingOrderNumber") String bookingOrderNumber,
            @RequestParam(value = "workOrderNumber", required = false) String workOrderNumber,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            Bill savedBill = billService.raiseNewBill(
                    executiveName.trim(), clientName.trim(), billStartDate, billEndDate,
                    bookingOrderNumber.trim(),
                    workOrderNumber != null ? workOrderNumber.trim() : null,
                    remarks != null ? remarks.trim() : "",
                    file);

            return ResponseEntity.ok("Bill raised successfully. ID: " + savedBill.getBillId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Planner corrects a bill (only if status is BILL_RAISED or BILL_REJECTED).
     */
    @PutMapping(value = "/{id}/planner-update", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<?> plannerUpdate(
            @PathVariable Long id,
            @RequestParam("executiveName") String executiveName,
            @RequestParam("clientName") String clientName,
            @RequestParam("billStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate billStartDate,
            @RequestParam("billEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate billEndDate,
            @RequestParam("bookingOrderNumber") String bookingOrderNumber,
            @RequestParam(value = "workOrderNumber", required = false) String workOrderNumber,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            PlannerBillUpdateRequest dto = new PlannerBillUpdateRequest();
            dto.setExecutiveName(executiveName.trim());
            dto.setClientName(clientName.trim());
            dto.setBillStartDate(billStartDate);
            dto.setBillEndDate(billEndDate);
            dto.setBookingOrderNumber(bookingOrderNumber.trim());
            dto.setWorkOrderNumber(workOrderNumber != null ? workOrderNumber.trim() : null);

            billService.updateBillByPlanner(id, dto, remarks, file);
            return ResponseEntity.ok("Bill updated by Planner and returned to RAISED status.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // --- 2. RO CREATOR ACTIONS ---

    @PatchMapping(value = "/{id}/ro-create", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('RO_CREATOR')")
    public ResponseEntity<?> roCreate(
            @PathVariable Long id,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            billService.updateToRoCreated(id, remarks, file);
            return ResponseEntity.ok("Bill advanced to RO_CREATED.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PatchMapping(value = "/{id}/ro-update", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('RO_CREATOR')")
    public ResponseEntity<?> roUpdate(
            @PathVariable Long id,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            billService.updateRoData(id, remarks, file);
            return ResponseEntity.ok("RO details updated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // --- 3. INVOICE CREATOR ACTIONS ---

    @PatchMapping(value = "/{id}/invoice-create", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('INVOICE_CREATOR')")
    public ResponseEntity<?> invoiceCreate(
            @PathVariable Long id,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            billService.updateToInvoiceCreated(id, remarks, file);
            return ResponseEntity.ok("Bill advanced to INVOICE_CREATED.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PatchMapping(value = "/{id}/invoice-update", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('INVOICE_CREATOR')")
    public ResponseEntity<?> invoiceUpdate(
            @PathVariable Long id,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            billService.updateInvoiceData(id, remarks, file);
            return ResponseEntity.ok("Invoice details updated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // --- 4. E-INVOICE CREATOR ACTIONS ---

    @PatchMapping(value = "/{id}/e-invoice-create", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('E_INVOICE_CREATOR')")
    public ResponseEntity<?> eInvoiceCreate(
            @PathVariable Long id,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            billService.updateToEInvoiceCreated(id, remarks, file);
            return ResponseEntity.ok("Bill finalized to E_INVOICE_CREATED.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PatchMapping(value = "/{id}/e-invoice-update", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('E_INVOICE_CREATOR')")
    public ResponseEntity<?> eInvoiceUpdate(
            @PathVariable Long id,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            billService.updateEInvoiceData(id, remarks, file);
            return ResponseEntity.ok("E-Invoice details updated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // --- 5. REJECTION ACTIONS ---

    @PatchMapping("/{id}/reject-bill")
    @PreAuthorize("hasRole('RO_CREATOR')")
    public ResponseEntity<?> rejectBill(@PathVariable Long id, @RequestParam String remarks) {
        try {
            billService.rejectRaisedBill(id, remarks);
            return ResponseEntity.ok("Bill rejected by RO Creator.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject-ro")
    @PreAuthorize("hasRole('INVOICE_CREATOR')")
    public ResponseEntity<?> rejectRo(@PathVariable Long id, @RequestParam String remarks) {
        try {
            billService.rejectRo(id, remarks);
            return ResponseEntity.ok("RO rejected by Invoice Creator.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject-invoice")
    @PreAuthorize("hasRole('E_INVOICE_CREATOR')")
    public ResponseEntity<?> rejectInvoice(@PathVariable Long id, @RequestParam String remarks) {
        try {
            billService.rejectInvoice(id, remarks);
            return ResponseEntity.ok("Invoice rejected by E-Invoice Creator.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 6. COMMON & HISTORY ACTIONS ---

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateActiveStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        try {
            billService.updateBillActiveStatus(id, request.getStatus());
            return ResponseEntity.ok("Bill status (Active/Hold) updated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/detailed-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BillDetailedHistoryDTO>> getDetailedHistory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(billService.getBillDetailedHistory(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clones an existing bill.
     * Path Variable 'id' is the source bill to copy from.
     */
    @PostMapping(value = "/{id}/clone", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<?> cloneBill(
            @PathVariable Long id,
            @RequestParam("executiveName") String executiveName,
            @RequestParam("clientName") String clientName,
            @RequestParam("billStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate billStartDate,
            @RequestParam("billEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate billEndDate,
            @RequestParam("bookingOrderNumber") String bookingOrderNumber,
            @RequestParam(value = "workOrderNumber", required = false) String workOrderNumber,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            BillCloneRequest dto = new BillCloneRequest();
            dto.setExecutiveName(executiveName.trim());
            dto.setClientName(clientName.trim());
            dto.setBillStartDate(billStartDate);
            dto.setBillEndDate(billEndDate);
            dto.setBookingOrderNumber(bookingOrderNumber.trim());
            dto.setWorkOrderNumber(workOrderNumber != null ? workOrderNumber.trim() : null);

            Bill savedBill = billService.cloneBill(id, dto, file);
            return ResponseEntity.ok("Bill cloned successfully. New Bill ID: " + savedBill.getBillId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cloning Error: " + e.getMessage());
        }
    }
}