package com.sribalajiads.billing_application.model;

public enum EBillStatus {
    BILL_RAISED,
    BILL_REJECTED,        // New
    RO_CREATED,
    RO_REJECTED,          // New
    INVOICE_CREATED,
    INVOICE_REJECTED,     // New
    E_INVOICE_CREATED,
    E_INVOICE_REJECTED    // New (for completeness)
}