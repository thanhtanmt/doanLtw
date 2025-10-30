# ClothesShop – Đồ án môn Lập trình Web Nhóm 1

Ứng dụng web bán quần áo xây dựng trên Spring Boot (Java 17) với giao diện Thymeleaf, bảo mật Spring Security và cơ sở dữ liệu SQL Server. Repo này bao gồm đầy đủ trang người dùng, quản trị, người bán (seller) và shipper cùng dữ liệu mẫu để demo.

## Tính năng chính
- Duyệt danh mục, trang chủ, tìm kiếm sản phẩm, trang chi tiết sản phẩm (có biến thể/size, giá, tồn kho)
- Giỏ hàng, wishlist, thanh toán/đặt hàng, theo dõi đơn hàng, trang thành công/thất bại
- Đăng ký, đăng nhập, xác minh email, quên/mật khẩu, đăng nhập Google OAuth2
- Tài khoản cá nhân, cập nhật hồ sơ, ví (wallet)
- Seller: đăng ký seller, quản lý sản phẩm
- Shipper: nhận đơn, cập nhật trạng thái giao hàng
- Admin: tổng quan (dashboard), quản lý người dùng/sản phẩm và cấu hình cơ bản

## Công nghệ & kiến trúc
- Backend: Spring Boot 3.5.6, Spring MVC, Spring Data JPA (Hibernate)
- Bảo mật: Spring Security, OAuth2 Client (Google), Thymeleaf Extras Spring Security
- View: Thymeleaf + Layout Dialect, SiteMesh (decorator)
- CSDL: Microsoft SQL Server
- Lưu trữ ảnh: Cloudinary SDK (upload ảnh sản phẩm/đại diện)
- Build: Maven Wrapper (mvnw)
- Java: 17

Xem thêm trong `pom.xml` để biết danh sách dependencies chi tiết.

## Cấu trúc thư mục (rút gọn)
```
src/
  main/
    java/com/example/...         # Mã nguồn Spring Boot
    resources/
      application.properties     # Cấu hình ứng dụng
      decorator/decorator.xml    # SiteMesh decorator
      static/{css,js}            # Tài nguyên tĩnh
      templates/                 # Giao diện Thymeleaf (user/admin/seller/shipper)
seed_data_with_variants.sql      # Seed dữ liệu (biến thể)
seed_real_data_with_orders.sql   # Seed dữ liệu đầy đủ (khuyến nghị)
```

## Yêu cầu hệ thống
- JDK 17
- Maven (đã kèm Maven Wrapper, không cần cài nếu dùng `mvnw`)
- Microsoft SQL Server (2019 trở lên khuyến nghị) và tài khoản truy cập
- Tài khoản Cloudinary (tùy chọn, để upload ảnh)
- Tài khoản Gmail SMTP (tùy chọn, để gửi email xác minh/khôi phục)

## Cấu hình ứng dụng
Cấu hình mặc định trong `src/main/resources/application.properties` (mặc định cổng 8080):
- Database (SQL Server):
  - `spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ClothesShop;encrypt=false`
  - `spring.datasource.username=sa`
  - `spring.datasource.password=123`
  - `spring.jpa.hibernate.ddl-auto=update`
- Email (Gmail SMTP): `spring.mail.*`
- OAuth2 Google: `spring.security.oauth2.client.registration.google.*`
- Cloudinary: `cloudinary.*`

Quan trọng (Google OAuth2): Bạn PHẢI sử dụng API/credentials Google của chính mình. Các giá trị `client-id` và `client-secret` trong repo chỉ phục vụ demo. Hãy tạo OAuth 2.0 Client trên Google Cloud Console (Application type: Web) và thêm Authorized redirect URI:

- `http://localhost:8080/login/oauth2/code/google`

Lưu ý bảo mật: các khóa/secret hiện đang ở repo phục vụ mục đích học tập/demo. Khi triển khai thực tế, hãy:
- Sử dụng biến môi trường hoặc file cấu hình cục bộ khác (không commit) để ghi đè các giá trị nhạy cảm.
- Ví dụ trên Windows PowerShell có thể tạm thời export biến trước khi chạy (tùy chọn, không bắt buộc):

```powershell
# (Tùy chọn) Ghi đè nhanh cấu hình khi chạy
$env:SPRING_MAIL_USERNAME = "<gmail>@gmail.com"
$env:SPRING_MAIL_PASSWORD = "<app_password>"
$env:SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID = "<google_client_id>"
$env:SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET = "<google_client_secret>"
$env:SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_SCOPE = "email,profile"
$env:CLOUDINARY_CLOUD_NAME = "<cloud_name>"
$env:CLOUDINARY_API_KEY = "<api_key>"
$env:CLOUDINARY_API_SECRET = "<api_secret>"
```

## Khởi tạo cơ sở dữ liệu và seed dữ liệu
1) Tạo database `ClothesShop` trên SQL Server (ví dụ qua SSMS):
```sql
CREATE DATABASE ClothesShop;
```

2) Chạy script seed (khuyến nghị) `seed_real_data_with_orders.sql` để tạo roles, users, danh mục, sản phẩm, biến thể và một số dữ liệu đơn hàng mẫu. Có thể chạy bằng SSMS hoặc dòng lệnh:

```powershell
# (Tùy chọn) Chạy seed qua sqlcmd – chỉnh lại -S, -U, -P cho phù hợp
sqlcmd -S localhost -d ClothesShop -U sa -P 123 -i .\seed_real_data_with_orders.sql
```

Script này idempotent (có kiểm tra tồn tại) nên có thể chạy nhiều lần an toàn.

## Tài khoản mẫu (từ seed)
Mật khẩu mặc định: `123123`
- admin / 123123 — vai trò: ADMIN
- fashionstore / 123123 — vai trò: SELLER
- trendyshop / 123123 — vai trò: SELLER
- shipper_anhtuan / 123123 — vai trò: SHIPPER
- shipper_thuylinh / 123123 — vai trò: SHIPPER
- nguyenvana / 123123 — vai trò: USER
- tranthib / 123123 — vai trò: USER

Ứng dụng mặc định chạy ở: http://localhost:8080

## Ghi chú triển khai
- Ảnh sản phẩm có thể dùng URL mẫu trong seed; để upload ảnh mới cần cấu hình Cloudinary hợp lệ.
- Gửi email (xác minh/khôi phục) cần cấu hình Gmail SMTP và app password.
- OAuth2 Google yêu cầu đăng ký OAuth Client và cập nhật `client-id`/`client-secret` và redirect URI phù hợp.
- Với môi trường thật, khuyến nghị cấu hình profile riêng (ví dụ `application-prod.properties`) và dùng biến môi trường.

## Liên hệ & đóng góp
Đồ án phục vụ học tập môn Lập trình Web. Nếu bạn muốn đóng góp, hãy mở issue hoặc tạo pull request.
