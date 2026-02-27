package com.sribalajiads.billing_application.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "revinfo_custom")
@RevisionEntity(AuditRevisionListener.class) // Links to our listener
@Getter
@Setter
public class AuditRevisionEntity {

    // 1. The Revision ID (Auto-incremented)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev")
    private int id;

    // 2. The exact timestamp of the change
    @RevisionTimestamp
    @Column(name = "revtstmp")
    private long timestamp;

    // 3. Our custom column to track WHO made the change
    @Column(name = "modified_by")
    private String modifiedBy;
}