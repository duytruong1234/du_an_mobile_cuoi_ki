# Báo Cáo Sơ Đồ Logic Hệ Thống (System Logic Architecture)

## 1. Tổng Quan
Báo cáo này mô tả kiến trúc logic của dự án "App Bán Đồng Hồ", bao gồm sự tương tác giữa Client (Android), Server (PHP), Database (MySQL) và các dịch vụ bên thứ 3.

## 2. Các Thành Phần Chính

### A. Client Side (Android)
- **Presentation Layer**: Các Activity/Fragment hiển thị giao diện (MainActivity, CartActivity, PaymentActivity).
- **Data/Logic Layer**:
  - **RetrofitClient**: Quản lý kết nối HTTP.
  - **ApiBanHang**: Interface định nghĩa toàn bộ các endpoints (30+ API).
  - **PaperDB**: Lưu trữ cục bộ (Session user, Giỏ hàng tạm).
  - **RxJava**: Xử lý bất đồng bộ cho các luồng dữ liệu.

### B. Server Side (PHP Native)
Hệ thống Backend được chia thành các nhóm logic xử lý chính:

1.  **Authentication (Xác thực)**
    - `dangnhap.php`: Xử lý đăng nhập thường và Google Login.
    - `dangki.php`: Đăng ký tài khoản mới.
    - `reset_pass.php`: Gửi OTP và đặt lại mật khẩu.

2.  **Product Management (Sản phẩm)**
    - `getspmoi.php`: Lấy danh sách sản phẩm mới/nổi bật.
    - `chitiet.php`: Lấy thông tin chi tiết sản phẩm.
    - `timkiem.php`: Tìm kiếm sản phẩm theo tên.

3.  **Order Processing (Đơn hàng)**
    - `taoDonHang.php`: Logic phức tạp nhất. Sử dụng Transaction để đảm bảo tính toàn vẹn dữ liệu:
        - Khóa dòng (Row Lock) sản phẩm để trừ tồn kho.
        - Tạo bản ghi `donhang`.
        - Tạo các bản ghi `chitietdonhang`.
        - Cập nhật trạng thái Voucher (nếu có).
    - `donhang.php`: Lấy lịch sử đơn hàng của user.

4.  **Payment (Thanh toán)**
    - Tích hợp VNPay và PayPal.
    - API tạo URL thanh toán và xử lý IPN/Return từ cổng thanh toán.

### C. Database (MySQL)
- **user**: Lưu thông tin người dùng, hỗ trợ login thường và login Google (cột `login_type`).
- **sanpham**: Lưu thông tin sản phẩm, giá, tồn kho.
- **donhang**: Lưu thông tin đơn hàng tổng quát.
- **chitietdonhang**: Lưu chi tiết từng sản phẩm trong đơn.
- **voucher**: Hệ thống mã giảm giá.

### D. External Services (Bên thứ 3)
- **Google Cloud**: Dùng cho Google Sign-In.
- **Firebase (FCM)**: Dùng để bắn thông báo đẩy (Push Notification) khi trạng thái đơn hàng thay đổi.
- **VNPay & PayPal**: Cổng thanh toán điện tử.

## 3. Luồng Logic Quan Trọng (Critical Logic Flows)

### 3.1. Luồng Đặt Hàng (Place Order)
1.  **Client**: Gửi request `POST /taoDonHang` kèm thông tin user, giỏ hàng, voucher.
2.  **Server**:
    -   `START TRANSACTION`.
    -   Kiểm tra tồn kho từng sản phẩm (Sử dụng `FOR UPDATE` để khóa).
    -   Nếu đủ tồn kho -> Trừ tồn kho -> Insert `donhang` -> Insert `chitietdonhang`.
    -   Nếu có Voucher -> Kiểm tra điều kiện -> Cập nhật `voucher_usage`.
    -   `COMMIT` nếu thành công, `ROLLBACK` nếu lỗi.
3.  **Response**: Trả về `id_donhang` cho Client.

### 3.2. Luồng Thanh Toán Online
1.  **Client**: Chọn phương thức (VNPay/PayPal).
2.  **Server**: Tạo URL thanh toán (với số tiền và mã đơn hàng) trả về Client.
3.  **Client**: Mở WebView/Browser để user thanh toán.
4.  **Gateway**: Gọi lại Server (IPN/Callback) để cập nhật trạng thái đơn hàng (`thanh_toan` = 1).

## 4. Kết Luận
Hệ thống được thiết kế theo mô hình Client-Server cổ điển nhưng chắc chắn, với việc xử lý Transaction cẩn thận ở phía Backend để đảm bảo dữ liệu đơn hàng và tồn kho luôn chính xác. Việc tích hợp đa dạng cổng thanh toán và đăng nhập Google làm tăng trải nghiệm người dùng.
