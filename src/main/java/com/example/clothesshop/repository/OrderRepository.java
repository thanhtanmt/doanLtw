package com.example.clothesshop.repository;

import com.example.clothesshop.model.Order;
import com.example.clothesshop.model.OrderStatus;
import com.example.clothesshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderCode(String orderCode);
    
    List<Order> findByShipperAndStatus(User shipper, OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.shipper IS NULL")
    List<Order> findPendingOrders(@Param("status") OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.shipper = :shipper AND o.status = :status " +
           "AND o.deliveredAt BETWEEN :startDate AND :endDate")
    Long countDeliveredOrdersByShipperAndDateRange(
        @Param("shipper") User shipper, 
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :orderCode, '%')) OR " +
           "LOWER(o.shippingName) LIKE LOWER(CONCAT('%', :shippingName, '%')) OR " +
           "LOWER(o.shippingPhone) LIKE LOWER(CONCAT('%', :shippingPhone, '%'))")
    List<Order> findBySearchCriteria(
        @Param("orderCode") String orderCode,
        @Param("shippingName") String shippingName, 
        @Param("shippingPhone") String shippingPhone
    );
    
    // Find orders containing products from a specific seller
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderDetails od " +
           "JOIN od.variant v " +
           "JOIN v.product p " +
           "WHERE p.seller = :seller " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBySellerAndDateRange(
        @Param("seller") User seller,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Count orders with delivered status for a seller
    @Query("SELECT COUNT(DISTINCT o) FROM Order o " +
           "JOIN o.orderDetails od " +
           "JOIN od.variant v " +
           "JOIN v.product p " +
           "WHERE p.seller = :seller " +
           "AND o.status = :status " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    Long countOrdersBySellerAndStatusAndDateRange(
        @Param("seller") User seller,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Calculate total revenue for a seller
    @Query("SELECT COALESCE(SUM(od.quantity * od.unitPrice), 0) FROM Order o " +
           "JOIN o.orderDetails od " +
           "JOIN od.variant v " +
           "JOIN v.product p " +
           "WHERE p.seller = :seller " +
           "AND o.status = :status " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBySellerAndStatusAndDateRange(
        @Param("seller") User seller,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
