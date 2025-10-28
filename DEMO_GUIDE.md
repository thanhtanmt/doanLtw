# Hướng dẫn chạy demo ClothesShop - Shipper API (SQL Server)

## 🚀 Cài đặt và chạy ứng dụng

### 1. Yêu cầu hệ thống
- Java 17+
- SQL Server 2019+ hoặc SQL Server Express
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### 2. Cài đặt SQL Server
```bash
# Cài đặt SQL Server Express (Windows)
# Tải từ: https://www.microsoft.com/en-us/sql-server/sql-server-downloads

# Hoặc sử dụng SQL Server trong Docker
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=123" -p 1433:1433 --name sqlserver -d mcr.microsoft.com/mssql/server:2019-latest
```

### 3. Tạo database và dữ liệu demo
```bash
# Sử dụng SQL Server Management Studio (SSMS)
# 1. Mở SSMS và kết nối đến SQL Server
# 2. Mở file database_setup_sqlserver.sql
# 3. Chạy script

# Hoặc sử dụng sqlcmd
sqlcmd -S localhost -U sa -P 123 -i database_setup_sqlserver.sql
```

Hoặc import file `database_setup_sqlserver.sql` vào SQL Server Management Studio.

### 4. Cấu hình ứng dụng
File `src/main/resources/application.properties` đã được cấu hình sẵn:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ClothesShop;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=123
```

**Lưu ý**: Thay đổi password SQL Server trong file `application.properties` nếu cần.

### 5. Chạy ứng dụng
```bash
# Clone project và chạy
cd doanLtw
mvn clean install
mvn spring-boot:run
```

Hoặc chạy từ IDE:
- Mở project trong IntelliJ IDEA/Eclipse
- Chạy class `Application.java`

### 6. Truy cập ứng dụng
- **URL**: http://localhost:8080
- **Login**: http://localhost:8080/login

## 👥 Tài khoản demo

| Role | Username | Password | Mô tả |
|------|----------|----------|-------|
| Admin | admin | 123456 | Quản trị hệ thống |
| Seller | seller1 | 123456 | Người bán hàng |
| Shipper | shipper1 | 123456 | Người giao hàng |
| Shipper | shipper2 | 123456 | Người giao hàng |
| Customer | customer1 | 123456 | Khách hàng |
| Customer | customer2 | 123456 | Khách hàng |
| Customer | customer3 | 123456 | Khách hàng |

## 📱 Test chức năng Shipper

### 1. Đăng nhập với tài khoản Shipper
- Truy cập: http://localhost:8080/login
- Đăng nhập với: `shipper1` / `123456`

### 2. Truy cập các trang Shipper
- **Dashboard**: http://localhost:8080/shipper/dashboard
- **Đơn hàng chờ giao**: http://localhost:8080/shipper/shipped
- **Đơn hàng đã giao**: http://localhost:8080/shipper/shipping
- **Profile**: http://localhost:8080/shipper/profile

### 3. Test API trực tiếp
Sử dụng Postman hoặc curl để test các API:

#### Lấy thống kê dashboard
```bash
curl -X GET http://localhost:8080/api/shipper/dashboard/stats \
  -H "Content-Type: application/json"
```

#### Lấy đơn hàng chờ giao
```bash
curl -X GET http://localhost:8080/api/shipper/orders/pending \
  -H "Content-Type: application/json"
```

#### Nhận đơn hàng để giao
```bash
curl -X POST http://localhost:8080/api/shipper/orders/DH3001/assign \
  -H "Content-Type: application/json"
```

#### Cập nhật trạng thái giao hàng
```bash
curl -X PUT http://localhost:8080/api/shipper/orders/delivery-status \
  -H "Content-Type: application/json" \
  -d '{
    "orderCode": "DH3001",
    "status": "DELIVERED",
    "deliveryNotes": "Giao hàng thành công"
  }'
```

## 🗄️ Dữ liệu demo

### Đơn hàng có sẵn:
- **DH3001**: Quận 1, COD 350,000đ - Chờ giao
- **DH3002**: Quận 7, Đã thanh toán 320,000đ - Chờ giao  
- **DH3003**: Quận Tân Bình, COD 850,000đ - Chờ giao
- **DH3004**: Quận 3, COD 250,000đ - Đang giao (shipper1)
- **DH3005**: Quận Bình Thạnh, Đã thanh toán 600,000đ - Đang giao (shipper2)
- **DH3006**: Quận 10, COD 450,000đ - Đã giao (shipper1)
- **DH3007**: Quận 2, Đã thanh toán 800,000đ - Đã giao (shipper2)
- **DH3008**: Quận 5, COD 320,000đ - Giao thất bại (shipper1)

### Sản phẩm:
- Áo thun nam/nữ (150,000đ - 120,000đ)
- Quần jean nam/nữ (350,000đ - 320,000đ)
- Áo sơ mi nam (250,000đ)
- Váy dài nữ (450,000đ)
- Giày sneaker nam (800,000đ)
- Túi xách nữ (600,000đ)

## 🔧 Troubleshooting

### Lỗi kết nối database
```bash
# Kiểm tra SQL Server đang chạy (Windows)
net start MSSQLSERVER

# Kiểm tra port SQL Server
netstat -an | findstr 1433

# Kiểm tra user và password
sqlcmd -S localhost -U sa -P 123
```

### Lỗi dependency
```bash
# Clean và rebuild
mvn clean install -U

# Kiểm tra Java version
java -version
```

### Lỗi port 8080 đã được sử dụng
```bash
# Thay đổi port trong application.properties
server.port=8081
```

### Lỗi SQL Server connection
```bash
# Kiểm tra SQL Server Browser service
net start SQLBrowser

# Kiểm tra firewall
# Mở port 1433 trong Windows Firewall
```

## 📚 API Documentation

Xem file `API_SHIPPER_GUIDE.md` để biết chi tiết về các API endpoint.

## 🎯 Tính năng đã implement

✅ **Backend API**:
- 9 API endpoints cho shipper
- Authentication & Authorization
- CRUD operations
- Business logic đầy đủ
- Error handling

✅ **Frontend Integration**:
- Dashboard với thống kê real-time
- Danh sách đơn hàng với tìm kiếm/lọc
- Nhận đơn hàng và cập nhật trạng thái
- Responsive design

✅ **Database**:
- Schema hoàn chỉnh
- Dữ liệu demo phong phú
- Indexes tối ưu
- Foreign key constraints

✅ **Security**:
- Spring Security integration
- Role-based access control
- Password encryption

## 🚀 Next Steps

Để phát triển thêm:
1. Thêm tính năng tracking vị trí shipper
2. Push notifications
3. Rating & feedback system
4. Payment integration
5. Mobile app
