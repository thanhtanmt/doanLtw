package com.example.clothesshop.service.impl;

import com.example.clothesshop.model.Order;
import com.example.clothesshop.model.OrderStatus;
import com.example.clothesshop.model.User;
import com.example.clothesshop.model.OrderDetail;
import com.example.clothesshop.dto.OrderDto;
import com.example.clothesshop.dto.OrderDetailDto;
import com.example.clothesshop.dto.ShipperStatsDto;
import com.example.clothesshop.dto.DeliveryUpdateDto;
import com.example.clothesshop.repository.OrderRepository;
import com.example.clothesshop.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<Order> findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }

    @Override
    public List<Order> findPendingOrders() {
        return orderRepository.findPendingOrders(OrderStatus.CONFIRMED);
    }

    @Override
    public List<Order> findOrdersByShipperAndStatus(User shipper, OrderStatus status) {
        return orderRepository.findByShipperAndStatus(shipper, status);
    }

    @Override
    public Order assignOrderToShipper(String orderCode, User shipper) {
        Order order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + orderCode));
        
        if (order.getShipper() != null) {
            throw new RuntimeException("Đơn hàng đã được giao cho shipper khác");
        }
        
        order.setShipper(shipper);
        order.setStatus(OrderStatus.DELIVERING);
        order.setAssignedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    @Override
    public Order updateDeliveryStatus(DeliveryUpdateDto deliveryUpdate, User shipper) {
        Order order = orderRepository.findByOrderCode(deliveryUpdate.getOrderCode())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + deliveryUpdate.getOrderCode()));
        
        if (!order.getShipper().getId().equals(shipper.getId())) {
            throw new RuntimeException("Bạn không có quyền cập nhật đơn hàng này");
        }
        
        if (deliveryUpdate.getStatus().equals("DELIVERED")) {
            order.setStatus(OrderStatus.DELIVERED);
            order.setDeliveredAt(LocalDateTime.now());
            order.setDeliveryNotes(deliveryUpdate.getDeliveryNotes());
        } else if (deliveryUpdate.getStatus().equals("FAILED")) {
            order.setStatus(OrderStatus.FAILED);
            order.setDeliveredDate(LocalDateTime.now()); // ✅ thêm dòng này
            order.setFailureReason(deliveryUpdate.getFailureReason());
        }
        
        return orderRepository.save(order);
    }

    @Override
    public ShipperStatsDto getShipperStats(User shipper) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDateTime.now();
        
        Long totalDelivered = orderRepository.countDeliveredOrdersByShipperAndDateRange(
            shipper, OrderStatus.DELIVERED, startOfMonth, endOfMonth);
        
        Long totalFailed = orderRepository.countDeliveredOrdersByShipperAndDateRange(
            shipper, OrderStatus.FAILED, startOfMonth, endOfMonth);
        
        Long totalPending = (long) orderRepository.findByShipperAndStatus(shipper, OrderStatus.DELIVERING).size();
        
        // Tính thu nhập theo bậc 10k/15k/20k mỗi đơn (đơn càng lớn ăn càng nhiều)
        // Quy ước:
        //  - <= 300.000đ  => 10.000đ/đơn
        //  - 300.001-700.000đ => 15.000đ/đơn
        //  - > 700.000đ => 20.000đ/đơn
        List<Order> allDeliveredByShipper = orderRepository.findByShipperAndStatus(shipper, OrderStatus.DELIVERED);
        double estimatedIncome = allDeliveredByShipper.stream()
            .filter(o -> o.getDeliveredAt() != null && !o.getDeliveredAt().isBefore(startOfMonth) && !o.getDeliveredAt().isAfter(endOfMonth))
            .mapToDouble(o -> {
                if (o.getTotalAmount() == null) return 0d;
                long amount = o.getTotalAmount().longValue();
                if (amount <= 300_000L) return 10_000d;
                if (amount <= 700_000L) return 15_000d;
                return 20_000d;
            })
            .sum();
        
        // Giả sử rating được tính từ feedback (chưa implement)
        Double rating = 4.8;
        
        // Lấy ngày giao hàng cuối cùng
        List<Order> deliveredOrders = orderRepository.findByShipperAndStatus(shipper, OrderStatus.DELIVERED);
        LocalDateTime lastDeliveryDate = deliveredOrders.stream()
            .map(Order::getDeliveredAt)
            .filter(date -> date != null)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        return new ShipperStatsDto(
            totalPending,
            totalDelivered,
            totalFailed,
            estimatedIncome,
            rating,
            lastDeliveryDate
        );
    }

    @Override
    public List<Order> searchOrders(String searchTerm) {
        return orderRepository.findBySearchCriteria(searchTerm, searchTerm, searchTerm);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingPhone(order.getShippingPhone());
        dto.setShippingName(order.getShippingName());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCodAmount(order.getCodAmount());
        
        if (order.getShipper() != null) {
            dto.setShipperId(order.getShipper().getId());
            dto.setShipperName(order.getShipper().getFirstName() + " " + order.getShipper().getLastName());
        }
        
        dto.setAssignedAt(order.getAssignedAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setDeliveryNotes(order.getDeliveryNotes());
        dto.setFailureReason(order.getFailureReason());
        dto.setCreatedAt(order.getCreatedAt());
        
        // Convert order details
        if (order.getOrderDetails() != null) {
            dto.setOrderDetails(order.getOrderDetails().stream()
                .map(detail -> convertOrderDetailToDto(detail))
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    @Override
    public List<OrderDto> convertToDtoList(List<Order> orders) {
        return orders.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    private OrderDetailDto convertOrderDetailToDto(OrderDetail orderDetail) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(orderDetail.getId());
        dto.setProductId(orderDetail.getProduct().getId());
        dto.setProductName(orderDetail.getProduct().getName());
        dto.setQuantity(orderDetail.getQuantity());
        dto.setUnitPrice(orderDetail.getUnitPrice());
        dto.setTotalPrice(orderDetail.getTotalPrice());
        return dto;
    }
}
