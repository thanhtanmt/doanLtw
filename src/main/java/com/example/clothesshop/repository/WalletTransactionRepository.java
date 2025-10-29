package com.example.clothesshop.repository;

import com.example.clothesshop.model.WalletTransaction;
import com.example.clothesshop.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
}
