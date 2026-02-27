package com.sribalajiads.billing_application.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        AuditRevisionEntity customRevisionEntity = (AuditRevisionEntity) revisionEntity;

        // Grab the currently logged-in user from the JWT Token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            customRevisionEntity.setModifiedBy(authentication.getName()); // Sets the Email
        } else {
            customRevisionEntity.setModifiedBy("SYSTEM");
        }
    }
}