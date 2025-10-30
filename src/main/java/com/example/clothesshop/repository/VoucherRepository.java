package com.example.clothesshop.repository;

import com.example.clothesshop.model.Voucher;
import com.example.clothesshop.model.VoucherType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Voucher> findByActiveTrue();
    
    List<Voucher> findByType(VoucherType type);
    
    List<Voucher> findByCreatedBy_Id(Long userId);
    
    List<Voucher> findByCreatedByAndType(com.example.clothesshop.model.User createdBy, VoucherType type);
    
    @Query("SELECT v FROM Voucher v WHERE v.active = true " +
           "AND v.startDate <= :date " +
           "AND v.endDate >= :date " +
           "AND v.usedQuantity < v.totalQuantity")
    List<Voucher> findAvailableVouchers(LocalDate date);
    
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.active = true " +
           "AND v.startDate <= :date " +
           "AND v.endDate >= :date " +
           "AND v.usedQuantity < v.totalQuantity")
    long countActiveVouchers(LocalDate date);
    
    @Query("SELECT SUM(v.usedQuantity) FROM Voucher v")
    Long getTotalUsedQuantity();
}
