package com.example.clothesshop.repository;

import com.example.clothesshop.model.VNPayTransaction;
import com.example.clothesshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VNPayTransactionRepository extends JpaRepository<VNPayTransaction, Long> {
    
    Optional<VNPayTransaction> findByTxnRef(String txnRef);
    
    List<VNPayTransaction> findByUserOrderByCreatedAtDesc(User user);
    
    List<VNPayTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
