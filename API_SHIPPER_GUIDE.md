# API Shipper - Hướng dẫn sử dụng

## Tổng quan
API Shipper cung cấp các chức năng để quản lý đơn hàng giao hàng, bao gồm:
- Xem thống kê tổng quan
- Quản lý đơn hàng chờ giao
- Quản lý đơn hàng đang giao và đã giao
- Cập nhật trạng thái giao hàng
- Tìm kiếm đơn hàng

## Base URL
```
/api/shipper
```

## Authentication
Tất cả các API đều yêu cầu authentication. Shipper cần đăng nhập để sử dụng các API.

## Endpoints

### 1. Dashboard - Thống kê tổng quan
**GET** `/api/shipper/dashboard/stats`

Lấy thống kê tổng quan của shipper hiện tại.

**Response:**
```json
{
  "success": true,
  "data": {
    "totalPendingOrders": 5,
    "totalDeliveredOrders": 12,
    "totalFailedOrders": 2,
    "estimatedIncome": 150000.0,
    "rating": 4.8,
    "lastDeliveryDate": "2025-01-26T14:30:00"
  },
  "message": "Lấy thống kê thành công"
}
```

### 2. Lấy danh sách đơn hàng chờ giao
**GET** `/api/shipper/orders/pending`

Lấy danh sách các đơn hàng chưa được giao cho shipper nào.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "orderCode": "DH3001",
      "userId": 1,
      "userName": "Nguyễn Văn A",
      "shippingAddress": "123 Đường ABC, Phường XYZ, Quận 1, TP. HCM",
      "shippingPhone": "090xxxx123",
      "shippingName": "Nguyễn Văn A",
      "status": "CONFIRMED",
      "paymentMethod": "COD",
      "totalAmount": 350000.0,
      "codAmount": 350000.0,
      "createdAt": "2025-01-26T10:00:00",
      "orderDetails": [...]
    }
  ],
  "message": "Lấy danh sách đơn hàng chờ giao thành công"
}
```

### 3. Lấy danh sách đơn hàng đang giao
**GET** `/api/shipper/orders/shipping`

Lấy danh sách các đơn hàng đang được shipper hiện tại giao.

**Response:** Tương tự như endpoint trên nhưng chỉ trả về đơn hàng có status = "SHIPPING"

### 4. Lấy danh sách đơn hàng đã giao
**GET** `/api/shipper/orders/delivered`

Lấy danh sách các đơn hàng đã được shipper hiện tại giao thành công.

**Response:** Tương tự như endpoint trên nhưng chỉ trả về đơn hàng có status = "DELIVERED"

### 5. Nhận đơn hàng để giao
**POST** `/api/shipper/orders/{orderCode}/assign`

Shipper nhận một đơn hàng để giao.

**Parameters:**
- `orderCode`: Mã đơn hàng

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderCode": "DH3001",
    "status": "SHIPPING",
    "shipperId": 5,
    "shipperName": "Nguyễn Văn Shipper",
    "assignedAt": "2025-01-26T15:30:00",
    ...
  },
  "message": "Nhận đơn hàng thành công"
}
```

### 6. Cập nhật trạng thái giao hàng
**PUT** `/api/shipper/orders/delivery-status`

Cập nhật trạng thái giao hàng (thành công hoặc thất bại).

**Request Body:**
```json
{
  "orderCode": "DH3001",
  "status": "DELIVERED",  // hoặc "FAILED"
  "deliveryNotes": "Giao hàng thành công",
  "failureReason": "Khách không có nhà"  // chỉ khi status = "FAILED"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderCode": "DH3001",
    "status": "DELIVERED",
    "deliveredAt": "2025-01-26T16:00:00",
    "deliveryNotes": "Giao hàng thành công",
    ...
  },
  "message": "Cập nhật trạng thái giao hàng thành công"
}
```

### 7. Lấy chi tiết đơn hàng
**GET** `/api/shipper/orders/{orderCode}`

Lấy thông tin chi tiết của một đơn hàng.

**Parameters:**
- `orderCode`: Mã đơn hàng

**Response:** Tương tự như các endpoint trên nhưng trả về thông tin chi tiết của một đơn hàng cụ thể.

### 8. Tìm kiếm đơn hàng
**GET** `/api/shipper/orders/search?searchTerm={term}`

Tìm kiếm đơn hàng theo mã đơn hàng, địa chỉ, hoặc số điện thoại.

**Parameters:**
- `searchTerm`: Từ khóa tìm kiếm

**Response:** Danh sách đơn hàng phù hợp với từ khóa tìm kiếm.

### 9. Lấy thông tin profile shipper
**GET** `/api/shipper/profile`

Lấy thông tin cá nhân của shipper hiện tại.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "username": "shipper001",
    "email": "shipper@example.com",
    "firstName": "Nguyễn Văn",
    "lastName": "Shipper",
    "phone": "090xxxx123",
    "enabled": true
  },
  "message": "Lấy thông tin profile thành công"
}
```

## Các trạng thái đơn hàng (Order Status)
- `PENDING`: Chờ xử lý
- `CONFIRMED`: Đã xác nhận
- `SHIPPING`: Đang giao hàng
- `DELIVERED`: Đã giao hàng
- `FAILED`: Giao hàng thất bại
- `CANCELLED`: Đã hủy

## Các phương thức thanh toán (Payment Method)
- `COD`: Thu hộ
- `BANK_TRANSFER`: Chuyển khoản
- `CREDIT_CARD`: Thẻ tín dụng

## Error Handling
Tất cả các API đều trả về response với format:
```json
{
  "success": false,
  "message": "Mô tả lỗi"
}
```

## Ví dụ sử dụng với JavaScript/Fetch
```javascript
// Lấy thống kê dashboard
fetch('/api/shipper/dashboard/stats', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
    // Thêm authentication headers nếu cần
  }
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('Thống kê:', data.data);
  } else {
    console.error('Lỗi:', data.message);
  }
});

// Nhận đơn hàng để giao
fetch('/api/shipper/orders/DH3001/assign', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  }
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('Nhận đơn hàng thành công:', data.data);
  } else {
    console.error('Lỗi:', data.message);
  }
});

// Cập nhật trạng thái giao hàng
fetch('/api/shipper/orders/delivery-status', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    orderCode: 'DH3001',
    status: 'DELIVERED',
    deliveryNotes: 'Giao hàng thành công'
  })
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('Cập nhật thành công:', data.data);
  } else {
    console.error('Lỗi:', data.message);
  }
});
```

