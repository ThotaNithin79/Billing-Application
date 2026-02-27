package com.sribalajiads.billing_application.service;

import com.sribalajiads.billing_application.audit.AuditRevisionEntity;
import com.sribalajiads.billing_application.dto.BillCloneRequest;
import com.sribalajiads.billing_application.dto.BillDetailedHistoryDTO;
import com.sribalajiads.billing_application.dto.PlannerBillUpdateRequest;
import com.sribalajiads.billing_application.model.Bill;
import com.sribalajiads.billing_application.model.EBillStatus;
import com.sribalajiads.billing_application.model.EStatus;
import com.sribalajiads.billing_application.repository.BillRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    @Value("${app.upload.path}")
    private String uploadPath;

    @PersistenceContext
    private EntityManager entityManager;

    // --- HELPER: ROBUST FILE UPLOAD ---
    private String uploadFile(MultipartFile file, String subFolder) throws IOException {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueName = UUID.randomUUID().toString() + "_" + originalFileName;

        Path uploadDir = Paths.get(uploadPath, subFolder).toAbsolutePath().normalize();
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path destination = uploadDir.resolve(uniqueName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueName;
    }

    // --- HELPER: VALIDATIONS ---
    private void validateBillIsActive(Bill bill) {
        if (bill.getStatus() == EStatus.HOLD) {
            throw new RuntimeException("Action Denied: This bill is currently on HOLD.");
        }
    }

    private void validateStatusIn(Bill bill, EBillStatus... allowedStatuses) {
        List<EBillStatus> allowedList = Arrays.asList(allowedStatuses);
        if (!allowedList.contains(bill.getBillStatus())) {
            throw new RuntimeException("Professionalism Error: Cannot update in " + bill.getBillStatus() + " state.");
        }
    }

    // --- PLANNER ACTIONS ---

    @Transactional
    public Bill raiseNewBill(String execName, String client, LocalDate start, LocalDate end,
                             String bkgNo, String workOrderNo, String remarks, MultipartFile file) throws IOException {
        if (billRepository.existsByBookingOrderNumber(bkgNo)) {
            throw new RuntimeException("Booking Order Number already exists.");
        }

        Bill bill = new Bill();
        bill.setExecutiveName(execName);
        bill.setClientName(client);
        bill.setBillStartDate(start);
        bill.setBillEndDate(end);
        bill.setBookingOrderNumber(bkgNo);
        bill.setWorkOrderNumber(workOrderNo);
        bill.setRemarks(remarks);

        if (file != null && !file.isEmpty()) {
            bill.setWorkOrderAttachment(uploadFile(file, "work_orders"));
        }

        bill.setStatus(EStatus.ACTIVE);
        bill.setBillStatus(EBillStatus.BILL_RAISED);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill updateBillByPlanner(Long billId, PlannerBillUpdateRequest request, String remarks, MultipartFile file) throws IOException {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        validateBillIsActive(bill);
        validateStatusIn(bill, EBillStatus.BILL_RAISED, EBillStatus.BILL_REJECTED);

        bill.setExecutiveName(request.getExecutiveName());
        bill.setClientName(request.getClientName());
        bill.setBillStartDate(request.getBillStartDate());
        bill.setBillEndDate(request.getBillEndDate());
        bill.setBookingOrderNumber(request.getBookingOrderNumber());
        bill.setWorkOrderNumber(request.getWorkOrderNumber());
        bill.setRemarks(remarks);

        if (file != null && !file.isEmpty()) {
            bill.setWorkOrderAttachment(uploadFile(file, "work_orders"));
        }

        bill.setBillStatus(EBillStatus.BILL_RAISED);
        return billRepository.save(bill);
    }

    // --- RO CREATOR ACTIONS ---

    @Transactional
    public Bill updateToRoCreated(Long billId, String remarks, MultipartFile roFile) throws IOException {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        validateBillIsActive(bill);

        if (bill.getBillStatus() != EBillStatus.BILL_RAISED && bill.getBillStatus() != EBillStatus.BILL_REJECTED) {
            throw new RuntimeException("Workflow Error: RO can only be created from Raised or Rejected state.");
        }

        bill.setRemarks(remarks);
        if (roFile != null && !roFile.isEmpty()) {
            bill.setRoAttachment(uploadFile(roFile, "ro_attachments"));
        }
        bill.setBillStatus(EBillStatus.RO_CREATED);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill updateRoData(Long billId, String remarks, MultipartFile roFile) throws IOException {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        validateBillIsActive(bill);
        validateStatusIn(bill, EBillStatus.RO_CREATED, EBillStatus.RO_REJECTED);

        bill.setRemarks(remarks);
        if (roFile != null && !roFile.isEmpty()) {
            bill.setRoAttachment(uploadFile(roFile, "ro_attachments"));
        }
        bill.setBillStatus(EBillStatus.RO_CREATED);
        return billRepository.save(bill);
    }

    // --- INVOICE CREATOR ACTIONS ---

    @Transactional
    public Bill updateToInvoiceCreated(Long billId, String remarks, MultipartFile invoiceFile) throws IOException {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        validateBillIsActive(bill);

        if (bill.getBillStatus() != EBillStatus.RO_CREATED) {
            throw new RuntimeException("Workflow Error: RO must be created first.");
        }

        bill.setRemarks(remarks);
        if (invoiceFile != null && !invoiceFile.isEmpty()) {
            bill.setInvoiceAttachment(uploadFile(invoiceFile, "invoice_attachments"));
        }
        bill.setBillStatus(EBillStatus.INVOICE_CREATED);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill updateInvoiceData(Long billId, String remarks, MultipartFile invFile) throws IOException {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        validateBillIsActive(bill);
        validateStatusIn(bill, EBillStatus.INVOICE_CREATED, EBillStatus.INVOICE_REJECTED);

        bill.setRemarks(remarks);
        if (invFile != null && !invFile.isEmpty()) {
            bill.setInvoiceAttachment(uploadFile(invFile, "invoice_attachments"));
        }
        bill.setBillStatus(EBillStatus.INVOICE_CREATED);
        return billRepository.save(bill);
    }

    // --- E-INVOICE CREATOR ACTIONS ---

    @Transactional
    public Bill updateToEInvoiceCreated(Long billId, String remarks, MultipartFile eInvoiceFile) throws IOException {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        validateBillIsActive(bill);

        if (bill.getBillStatus() != EBillStatus.INVOICE_CREATED) {
            throw new RuntimeException("Workflow Error: Standard Invoice must be created first.");
        }

        bill.setRemarks(remarks);
        if (eInvoiceFile != null && !eInvoiceFile.isEmpty()) {
            bill.setEInvoiceAttachment(uploadFile(eInvoiceFile, "e_invoice_attachments"));
        }
        bill.setBillStatus(EBillStatus.E_INVOICE_CREATED);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill updateEInvoiceData(Long billId, String remarks, MultipartFile eInvFile) throws IOException {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        validateBillIsActive(bill);
        validateStatusIn(bill, EBillStatus.E_INVOICE_CREATED);

        bill.setRemarks(remarks);
        if (eInvFile != null && !eInvFile.isEmpty()) {
            bill.setEInvoiceAttachment(uploadFile(eInvFile, "e_invoice_attachments"));
        }
        return billRepository.save(bill);
    }

    // --- REJECTION LOGIC ---

    @Transactional
    public Bill rejectRaisedBill(Long billId, String remarks) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found."));
        validateBillIsActive(bill);
        validateStatusIn(bill, EBillStatus.BILL_RAISED);

        bill.setRemarks(remarks);
        bill.setBillStatus(EBillStatus.BILL_REJECTED);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill rejectRo(Long billId, String remarks) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found."));
        validateBillIsActive(bill);
        validateStatusIn(bill, EBillStatus.RO_CREATED);

        bill.setRemarks(remarks);
        bill.setBillStatus(EBillStatus.RO_REJECTED);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill rejectInvoice(Long billId, String remarks) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found."));
        validateBillIsActive(bill);
        validateStatusIn(bill, EBillStatus.INVOICE_CREATED);

        bill.setRemarks(remarks);
        bill.setBillStatus(EBillStatus.INVOICE_REJECTED);
        return billRepository.save(bill);
    }

    // --- ADMIN ACTIONS ---

    @Transactional
    public Bill updateBillActiveStatus(Long billId, EStatus newStatus) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        if (bill.getStatus() == newStatus) return bill;
        bill.setStatus(newStatus);
        return billRepository.save(bill);
    }

    // --- HISTORY RETRIEVAL ---

    public List<BillDetailedHistoryDTO> getBillDetailedHistory(Long billId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Object[]> revisions = reader.createQuery()
                .forRevisionsOfEntity(Bill.class, false, true)
                .add(AuditEntity.id().eq(billId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return revisions.stream().map(row -> {
            Bill billSnap = (Bill) row[0];
            AuditRevisionEntity revEntity = (AuditRevisionEntity) row[1];
            org.hibernate.envers.RevisionType revType = (org.hibernate.envers.RevisionType) row[2];

            return BillDetailedHistoryDTO.builder()
                    .revisionId(revEntity.getId())
                    .modifiedBy(revEntity.getModifiedBy())
                    .modifiedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(revEntity.getTimestamp()), ZoneId.systemDefault()))
                    .revisionType(revType.name())
                    .billId(billSnap.getBillId())
                    .bookingOrderNumber(billSnap.getBookingOrderNumber())
                    .executiveName(billSnap.getExecutiveName())
                    .clientName(billSnap.getClientName())
                    .billStartDate(billSnap.getBillStartDate())
                    .billEndDate(billSnap.getBillEndDate())
                    .status(billSnap.getStatus())
                    .billStatus(billSnap.getBillStatus())
                    .workOrderNumber(billSnap.getWorkOrderNumber())
                    .workOrderAttachment(billSnap.getWorkOrderAttachment())
                    .roAttachment(billSnap.getRoAttachment())
                    .invoiceAttachment(billSnap.getInvoiceAttachment())
                    .eInvoiceAttachment(billSnap.getEInvoiceAttachment())
                    .remarks(billSnap.getRemarks()) // Ensure this exists in your DTO!
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Clones an existing bill into a new one.
     * Reuses the old file reference if no new file is provided.
     */
    @Transactional
    public Bill cloneBill(Long sourceBillId, BillCloneRequest request, MultipartFile newFile) throws IOException {
        // 1. Fetch Source Bill
        Bill sourceBill = billRepository.findById(sourceBillId)
                .orElseThrow(() -> new RuntimeException("Source Bill not found with ID: " + sourceBillId));

        // 2. Uniqueness Check for the new Booking Order Number
        if (billRepository.existsByBookingOrderNumber(request.getBookingOrderNumber())) {
            throw new RuntimeException("Error: New Booking Order Number already exists.");
        }

        // 3. Create New Bill Instance
        Bill clonedBill = new Bill();
        clonedBill.setExecutiveName(request.getExecutiveName());
        clonedBill.setClientName(request.getClientName());
        clonedBill.setBillStartDate(request.getBillStartDate());
        clonedBill.setBillEndDate(request.getBillEndDate());
        clonedBill.setBookingOrderNumber(request.getBookingOrderNumber());
        clonedBill.setWorkOrderNumber(request.getWorkOrderNumber());

        // 4. Robust File Logic
        if (newFile != null && !newFile.isEmpty()) {
            // Option A: Planner uploaded a NEW file for this period
            String newFileName = uploadFile(newFile, "work_orders");
            clonedBill.setWorkOrderAttachment(newFileName);
        } else {
            // Option B: Reuse the reference to the OLD file from the source bill
            clonedBill.setWorkOrderAttachment(sourceBill.getWorkOrderAttachment());
        }

        // 5. Reset Statuses for the new Billing Cycle
        clonedBill.setStatus(EStatus.ACTIVE);
        clonedBill.setBillStatus(EBillStatus.BILL_RAISED);
        clonedBill.setRemarks("Cloned from Bill ID: " + sourceBillId);

        // 6. Save (Triggers new Audit record and History snapshot)
        return billRepository.save(clonedBill);
    }
}