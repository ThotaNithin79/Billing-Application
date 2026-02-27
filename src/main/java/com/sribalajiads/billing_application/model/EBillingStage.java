package com.sribalajiads.billing_application.model;

public enum EBillingStage {
    BILL_RAISED,        // Handled by ROLE_PLANNER
    RO_CREATED,         // Handled by ROLE_RO_CREATOR
    INVOICE_CREATED,    // Handled by ROLE_INVOICE_CREATOR
    E_INVOICE_CREATED   // Handled by ROLE_E_INVOICE_CREATOR
}