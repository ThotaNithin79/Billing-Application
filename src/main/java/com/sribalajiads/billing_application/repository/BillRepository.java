package com.sribalajiads.billing_application.repository;

import com.sribalajiads.billing_application.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    // Helps us cleanly check for uniqueness before saving
    boolean existsByBookingOrderNumber(String bookingOrderNumber);

}