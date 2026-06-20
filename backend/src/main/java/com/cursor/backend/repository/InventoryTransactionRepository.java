package com.cursor.backend.repository;

import com.cursor.backend.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    Page<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    Page<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
