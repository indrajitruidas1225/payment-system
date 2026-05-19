package com.service.payment_system.repository;

import com.service.payment_system.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository
        extends JpaRepository<LedgerEntry, Long> {
}