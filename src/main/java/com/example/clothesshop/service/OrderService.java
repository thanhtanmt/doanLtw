package com.example.clothesshop.service;

import com.example.clothesshop.model.Order;
import com.example.clothesshop.model.OrderStatus;
import com.example.clothesshop.model.User;
import com.example.clothesshop.dto.OrderDto;
import com.example.clothesshop.dto.ShipperStatsDto;
import com.example.clothesshop.dto.DeliveryUpdateDto;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    // Lấy tất cả đơn hàng
    List<Order> findAll();
    
    // Lấy đơn hàng theo ID
    Optional<Order> findById(Long id);
    
    // Lấy đơn hàng theo mã đơn hàng
    Optional<Order> findByOrderCode(String orderCode);
    
    // Lấy đơn hàng chờ giao (chưa được giao cho shipper)
    List<Order> findPendingOrders();
    
    // Lấy đơn hàng của shipper theo trạng thái
    List<Order> findOrdersByShipperAndStatus(User shipper, OrderStatus status);
    
    // Giao đơn hàng cho shipper
    Order assignOrderToShipper(String orderCode, User shipper);
    
    // Cập nhật trạng thái giao hàng
    Order updateDeliveryStatus(DeliveryUpdateDto deliveryUpdate, User shipper);
    
    // Lấy thống kê của shipper
    ShipperStatsDto getShipperStats(User shipper);
    
    // Tìm kiếm đơn hàng
    List<Order> searchOrders(String searchTerm);
    
    // Lưu đơn hàng
    Order save(Order order);
    
    // Xóa đơn hàng
    void deleteById(Long id);
    
    // Chuyển đổi Order thành OrderDto
    OrderDto convertToDto(Order order);
    
    // Chuyển đổi danh sách Order thành OrderDto
    List<OrderDto> convertToDtoList(List<Order> orders);
}