# 📐 HƯỚNG DẪN VẼ SƠ ĐỒ USE CASE CHUẨN DRAWIO - APP BÁN ĐỒNG HỒ

**Ngày tạo:** 03/11/2025  
**Phiên bản:** 1.0  
**Công cụ:** Draw.io (https://app.diagrams.net/)

---

## 📋 MỤC LỤC

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Các bước chuẩn bị](#2-các-bước-chuẩn-bị)
3. [Danh sách Actor](#3-danh-sách-actor)
4. [Danh sách Use Case đầy đủ](#4-danh-sách-use-case-đầy-đủ)
5. [Quan hệ giữa Use Case](#5-quan-hệ-giữa-use-case)
6. [Hướng dẫn vẽ từng bước](#6-hướng-dẫn-vẽ-từng-bước)
7. [Mẹo và Best Practices](#7-mẹo-và-best-practices)

---

## 1. TỔNG QUAN DỰ ÁN

### Thông tin hệ thống
- **Tên dự án:** Ứng dụng Bán Đồng Hồ (AppBanDongHo)
- **Loại:** E-commerce Mobile App
- **Nền tảng:** Android (Java) + PHP Backend
- **Database:** MySQL
- **Thanh toán:** VNPay, COD
- **Phân quyền:** User (role=0), Admin (role=1)

### Các module chính
1. **Xác thực (Authentication)** - Đăng ký/Đăng nhập
2. **Quản lý sản phẩm** - Xem/Tìm kiếm/CRUD
3. **Giỏ hàng** - Thêm/Sửa/Xóa
4. **Đặt hàng & Thanh toán** - COD, VNPay
5. **Quản lý đơn hàng** - Xem/Hủy/Cập nhật trạng thái
6. **Quản lý người dùng (Admin)** - Phân quyền
7. **Thống kê (Admin)** - Biểu đồ doanh thu
8. **Quản lý tồn kho (Admin)** - Nhập/Xuất kho
9. **Hệ thống Voucher** - Mã giảm giá
10. **Thông báo Push** - Firebase Cloud Messaging

---

## 2. CÁC BƯỚC CHUẨN BỊ

### Bước 1: Mở Draw.io
1. Truy cập: https://app.diagrams.net/
2. Chọn: **Create New Diagram**
3. Chọn template: **Blank Diagram**
4. Đặt tên file: `UseCase_AppBanDongHo.drawio`

### Bước 2: Thiết lập Page
1. **Page Setup:**
   - Format: A3 hoặc A2 (vì có nhiều use case)
   - Orientation: Landscape (ngang)
   - Grid: 10px

2. **Bật các thanh công cụ:**
   - View → Format Panel (Ctrl+Shift+P)
   - View → Shapes (hiển thị library)

### Bước 3: Chuẩn bị Shapes
Trong thanh **Shapes** bên trái, tìm thư viện **UML**:
- Click **More Shapes** (dưới cùng)
- Tích chọn: **UML 2.5** hoặc **UML**
- Click **Apply**

---

## 3. DANH SÁCH ACTOR

### 3.1. Actor chính (Primary Actors)

#### 👤 **Guest (Khách)**
```
- Vai trò: Người chưa đăng nhập
- Quyền hạn: Chỉ xem sản phẩm, không mua hàng
- Icon: Stick figure (màu xám)
- Vị trí: Bên trái, trên cùng
```

#### 👥 **User (Người dùng)**
```
- Vai trò: Khách hàng đã đăng ký
- Database: role = 0
- Quyền hạn: Mua hàng, quản lý đơn hàng của mình
- Icon: Stick figure (màu xanh dương)
- Vị trí: Bên trái, giữa
```

#### 👨‍💼 **Admin (Quản trị viên)**
```
- Vai trò: Người quản lý hệ thống
- Database: role = 1
- Quyền hạn: Toàn quyền quản lý sản phẩm, đơn hàng, người dùng, thống kê
- Kế thừa: Admin IS-A User (có tất cả quyền của User)
- Icon: Stick figure (màu đỏ)
- Vị trí: Bên trái, dưới cùng
```

### 3.2. Actor phụ (Secondary Actors)

#### 💳 **VNPay**
```
- Loại: External System Actor
- Vai trò: Cổng thanh toán online
- Icon: Actor hoặc Component (màu cam)
- Vị trí: Bên phải, trên
```

#### 🔔 **Firebase Cloud Messaging**
```
- Loại: External System Actor
- Vai trò: Gửi thông báo push
- Icon: Component (màu xanh lá)
- Vị trí: Bên phải, dưới
```

#### 📧 **PHPMailer**
```
- Loại: External System Actor
- Vai trò: Gửi email OTP reset password
- Icon: Component (màu tím)
- Vị trí: Bên phải, giữa
```

#### 🔐 **Google Sign-In API**
```
- Loại: External System Actor
- Vai tró: Xác thực Google
- Icon: Component (màu xanh Google)
- Vị trí: Bên phải, giữa
```

---

## 4. DANH SÁCH USE CASE ĐẦY ĐỦ

### 📦 NHÓM A: XÁC THỰC (AUTHENTICATION)

#### UC-A1: Xem danh sách sản phẩm (không cần đăng nhập)
```
Actor: Guest, User, Admin
Mô tả: Xem tất cả sản phẩm trên trang chủ
File: MainActivity.java
API: getloaisp.php, getspmoi.php
```

#### UC-A2: Tìm kiếm sản phẩm
```
Actor: Guest, User, Admin
Mô tả: Tìm kiếm theo tên, sắp xếp theo giá
File: SearchActivity.java
API: timkiem.php
```

#### UC-A3: Xem chi tiết sản phẩm
```
Actor: Guest, User, Admin
Mô tả: Xem thông tin đầy đủ của sản phẩm
File: ChiTietActivity.java
API: kiemTraTonKho.php
```

#### UC-A4: Đăng ký tài khoản
```
Actor: Guest
Mô tả: Tạo tài khoản mới với email/password
File: DangKiActivity.java
API: dangki.php
Sau UC này: → UC-A5 (Đăng nhập)
```

#### UC-A5: Đăng nhập
```
Actor: Guest
Mô tả: Đăng nhập bằng email/password
File: DangNhapActivity.java
API: dangnhap.php
Extend: UC-A6 (Đăng nhập Google)
Extend: UC-A7 (Quên mật khẩu)
```

#### UC-A6: Đăng nhập Google
```
Actor: Guest, Google Sign-In API
Mô tả: Đăng nhập bằng tài khoản Google
File: DangNhapActivity.java
API: dangnhap.php (với login_type=google)
Quan hệ: «extend» từ UC-A5
```

#### UC-A7: Quên mật khẩu (Reset bằng OTP)
```
Actor: Guest, PHPMailer
Mô tả: Đặt lại mật khẩu qua email OTP
File: ResetPassActivity.java
API: reset_pass.php, verify_otp_reset_pass.php
Quan hệ: «extend» từ UC-A5
Include: UC-A7a (Gửi OTP qua email)
```

#### UC-A7a: Gửi OTP qua email
```
Actor: System, PHPMailer
Mô tả: Gửi mã OTP 6 số qua email
Quan hệ: «include» từ UC-A7
```

#### UC-A8: Cập nhật thông tin cá nhân
```
Actor: User, Admin
Mô tả: Sửa username, mobile, đổi mật khẩu
File: UpdateProfileActivity.java
API: updateProfile.php
Precondition: Phải đăng nhập
```

#### UC-A9: Đăng xuất
```
Actor: User, Admin
Mô tả: Xóa session và về màn hình đăng nhập
File: MainActivity.java (menu)
```

---

### 🛒 NHÓM B: GIỎ HÀNG & MUA HÀNG

#### UC-B1: Thêm vào giỏ hàng
```
Actor: User, Admin
Mô tả: Thêm sản phẩm từ trang chi tiết
File: ChiTietActivity.java
Precondition: Phải đăng nhập
Include: UC-B1a (Kiểm tra tồn kho)
```

#### UC-B1a: Kiểm tra tồn kho
```
Actor: System
Mô tả: Kiểm tra sản phẩm còn hàng không
API: kiemTraTonKho.php
Quan hệ: «include» từ UC-B1
```

#### UC-B2: Xem giỏ hàng
```
Actor: User, Admin
Mô tả: Xem danh sách sản phẩm đã thêm
File: GioHangActivity.java
Include: UC-B3, UC-B4, UC-B5
```

#### UC-B3: Cập nhật số lượng sản phẩm
```
Actor: User, Admin
Mô tả: Tăng/giảm số lượng trong giỏ
Quan hệ: «include» từ UC-B2
```

#### UC-B4: Xóa sản phẩm khỏi giỏ
```
Actor: User, Admin
Mô tả: Xóa sản phẩm không muốn mua
Quan hệ: «include» từ UC-B2
```

#### UC-B5: Chọn sản phẩm thanh toán
```
Actor: User, Admin
Mô tả: Check/uncheck sản phẩm muốn mua
Quan hệ: «include» từ UC-B2
```

#### UC-B6: Đặt hàng
```
Actor: User, Admin
Mô tả: Tạo đơn hàng từ giỏ hàng
File: ThanhToanActivity.java
API: donhang.php
Precondition: Có ít nhất 1 sản phẩm được chọn
Include: UC-B7 (Chọn phương thức thanh toán)
Include: UC-B10 (Áp dụng voucher) - Optional
```

#### UC-B7: Chọn phương thức thanh toán
```
Actor: User, Admin
Mô tả: Chọn COD hoặc VNPay
Quan hệ: «include» từ UC-B6
Extend: UC-B8 (Thanh toán VNPay)
Extend: UC-B9 (Thanh toán COD)
```

#### UC-B8: Thanh toán VNPay
```
Actor: User, Admin, VNPay
Mô tả: Thanh toán online qua VNPay
File: ThanhToanActivity.java
API: vnpay_create_payment.php, vnpay_return.php
Quan hệ: «extend» từ UC-B7
Include: UC-B8a (Tạo payment URL)
Include: UC-B8b (Xác nhận thanh toán)
```

#### UC-B8a: Tạo payment URL VNPay
```
Actor: System, VNPay
API: vnpay_create_payment.php
Quan hệ: «include» từ UC-B8
```

#### UC-B8b: Xác nhận thanh toán VNPay
```
Actor: VNPay
API: vnpay_return.php, vnpay_check_status.php
Quan hệ: «include» từ UC-B8
```

#### UC-B9: Thanh toán COD
```
Actor: User, Admin
Mô tả: Thanh toán khi nhận hàng
API: donhang.php (trangthai=0)
Quan hệ: «extend» từ UC-B7
```

#### UC-B10: Chọn và áp dụng voucher
```
Actor: User, Admin
Mô tả: Chọn mã giảm giá cho đơn hàng
File: ChonVoucherActivity.java
API: getVouchers.php, checkVoucher.php
Quan hệ: «extend» từ UC-B6 (optional)
Include: UC-B10a (Kiểm tra điều kiện voucher)
```

#### UC-B10a: Kiểm tra điều kiện voucher
```
Actor: System
Mô tả: Validate voucher (đơn tối thiểu, user mới/cũ, số lần dùng)
API: checkVoucher.php
Quan hệ: «include» từ UC-B10
```

---

### 📋 NHÓM C: QUẢN LÝ ĐƠN HÀNG (USER)

#### UC-C1: Xem danh sách đơn hàng của tôi
```
Actor: User, Admin
Mô tả: Xem tất cả đơn hàng đã đặt
File: XemDonActivity.java
API: xemdonhang.php
Include: UC-C2 (Xem chi tiết đơn hàng)
Extend: UC-C3 (Hủy đơn hàng)
```

#### UC-C2: Xem chi tiết đơn hàng
```
Actor: User, Admin
Mô tả: Xem sản phẩm, giá, địa chỉ, trạng thái
File: ChiTietDonHangActivity.java
API: getChiTietDonHang.php
Quan hệ: «include» từ UC-C1
```

#### UC-C3: Hủy đơn hàng
```
Actor: User, Admin
Mô tả: Hủy đơn hàng chưa xử lý
API: capNhatTrangThai.php
Quan hệ: «extend» từ UC-C1
Precondition: trangthai = 0 (Chờ xử lý)
Effect: Hoàn lại tồn kho (trigger)
```

#### UC-C4: Tiếp tục thanh toán VNPay
```
Actor: User, Admin, VNPay
Mô tả: Thanh toán lại cho đơn trangthai=-1
File: XemDonActivity.java
API: vnpay_continue_payment.php
Precondition: trangthai = -1 (Chờ thanh toán)
```

---

### 🏪 NHÓM D: QUẢN LÝ SẢN PHẨM (ADMIN)

#### UC-D1: Vào trang quản lý
```
Actor: Admin
Mô tả: Truy cập menu quản trị
File: QuanLiActivity.java
Precondition: role = 1
```

#### UC-D2: Quản lý sản phẩm
```
Actor: Admin
Mô tả: CRUD sản phẩm
File: QuanLiActivity.java
Include: UC-D3, UC-D4, UC-D5
```

#### UC-D3: Thêm sản phẩm mới
```
Actor: Admin
File: ThemSPActivity.java
API: themsp.php
Quan hệ: «include» từ UC-D2
Include: UC-D3a (Upload ảnh sản phẩm)
```

#### UC-D3a: Upload ảnh sản phẩm
```
Actor: Admin, System
Mô tả: Upload ảnh từ mobile lên server
Quan hệ: «include» từ UC-D3
```

#### UC-D4: Sửa sản phẩm
```
Actor: Admin
File: ThemSPActivity.java
API: updatesp.php
Quan hệ: «include» từ UC-D2
```

#### UC-D5: Xóa sản phẩm
```
Actor: Admin
File: QuanLiActivity.java
API: xoa.php
Quan hệ: «include» từ UC-D2
Precondition: Sản phẩm không nằm trong đơn hàng đang xử lý
```

---

### 📊 NHÓM E: QUẢN LÝ TỒN KHO (ADMIN)

#### UC-E1: Xem tồn kho
```
Actor: Admin
Mô tả: Xem số lượng tồn tất cả sản phẩm
File: TonKhoActivity.java
API: getTonKho.php
Include: UC-E2 (Cập nhật tồn kho)
```

#### UC-E2: Cập nhật tồn kho
```
Actor: Admin
Mô tả: Nhập/xuất kho thủ công
API: capNhatTonKho.php, setTonKho.php
Quan hệ: «include» từ UC-E1
```

#### UC-E3: Tự động giảm tồn kho
```
Actor: System (Database Trigger)
Mô tả: Tự động trừ tồn kho khi tạo đơn
File: update_donhang_tonkho.sql (trigger)
Trigger: Khi INSERT vào chitietdonhang
```

#### UC-E4: Tự động hoàn tồn kho
```
Actor: System (Database Trigger)
Mô tả: Hoàn lại tồn kho khi hủy đơn
File: update_donhang_tonkho.sql (trigger)
Trigger: Khi UPDATE donhang SET trangthai=3 (Đã hủy)
```

---

### 📈 NHÓM F: THỐNG KÊ (ADMIN)

#### UC-F1: Xem thống kê
```
Actor: Admin
Mô tả: Xem dashboard thống kê
File: ThongKeActivity.java
API: thongke.php
Include: UC-F2 (Xem biểu đồ sản phẩm bán chạy)
```

#### UC-F2: Xem biểu đồ sản phẩm bán chạy
```
Actor: Admin
Mô tả: Bar chart Top 10 sản phẩm
Library: MPAndroidChart
Quan hệ: «include» từ UC-F1
```

---

### 👥 NHÓM G: QUẢN LÝ NGƯỜI DÙNG (ADMIN)

#### UC-G1: Xem danh sách người dùng
```
Actor: Admin
Mô tả: Xem tất cả user trong hệ thống
File: QuanLyNguoiDungActivity.java
API: getAllUsers.php
Include: UC-G2, UC-G3
```

#### UC-G2: Phân quyền admin
```
Actor: Admin
Mô tả: Nâng/hạ quyền admin cho user
API: updateUserRole.php
Quan hệ: «include» từ UC-G1
Database: UPDATE user SET role=1 hoặc role=0
```

#### UC-G3: Xóa người dùng
```
Actor: Admin
API: deleteUser.php
Quan hệ: «include» từ UC-G1
Precondition: Không được xóa chính mình
```

---

### 📦 NHÓM H: QUẢN LÝ ĐƠN HÀNG (ADMIN)

#### UC-H1: Xem tất cả đơn hàng
```
Actor: Admin
Mô tả: Xem đơn hàng của tất cả user
File: XemDonActivity.java (với toggle "Admin Mode")
API: xemdonhang.php (không filter user)
```

#### UC-H2: Cập nhật trạng thái đơn hàng
```
Actor: Admin
Mô tả: Duyệt đơn, Đang giao, Đã giao
API: capNhatTrangThai.php
Trạng thái:
  - 0: Chờ xử lý
  - 1: Đang giao
  - 2: Đã giao
  - 3: Đã hủy
  - -1: Chờ thanh toán VNPay
```

---

### 🎟️ NHÓM I: HỆ THỐNG VOUCHER

#### UC-I1: Quản lý voucher (Admin)
```
Actor: Admin
Mô tả: CRUD mã giảm giá
File: QuanLyVoucherActivity.java
API: getAllVouchers.php (chưa có - cần tạo)
Include: UC-I2, UC-I3, UC-I4, UC-I5
```

#### UC-I2: Thêm voucher
```
Actor: Admin
API: addVoucher.php (chưa có - cần tạo)
Quan hệ: «include» từ UC-I1
Thông tin: Mã, tên, loại giảm (%, fixed, freeship), điều kiện
```

#### UC-I3: Sửa voucher
```
Actor: Admin
API: updateVoucher.php
Quan hệ: «include» từ UC-I1
```

#### UC-I4: Xóa voucher
```
Actor: Admin
API: deleteVoucher.php
Quan hệ: «include» từ UC-I1
```

#### UC-I5: Bật/tắt voucher
```
Actor: Admin
Mô tả: Toggle trạng thái active/inactive
Quan hệ: «include» từ UC-I1
```

#### UC-I6: Xem lịch sử sử dụng voucher
```
Actor: Admin
Mô tả: Xem user nào đã dùng voucher nào
Table: voucher_usage
Quan hệ: «extend» từ UC-I1
```

---

### 🔔 NHÓM J: THÔNG BÁO PUSH

#### UC-J1: Nhận thông báo đơn hàng mới
```
Actor: Admin, Firebase Cloud Messaging
Mô tả: Admin nhận thông báo khi có đơn mới
File: FirebaseMessagerReceiver.java
Service: FCM
Status: Đã setup nhưng backend chưa gửi
```

#### UC-J2: Nhận thông báo cập nhật đơn hàng
```
Actor: User, Admin, Firebase Cloud Messaging
Mô tả: User nhận thông báo khi đơn thay đổi trạng thái
Status: Chưa implement
```

---

## 5. QUAN HỆ GIỮA USE CASE

### 5.1. Quan hệ Generalization (Kế thừa Actor)

```
Admin ---|> User (Admin kế thừa tất cả quyền của User)
```

**Ý nghĩa:** 
- Admin có thể làm mọi thứ User làm được
- Admin có thêm các quyền quản trị riêng

**Cách vẽ trong DrawIO:**
1. Chọn **Arrow** → **Generalization** (mũi tên tam giác rỗng)
2. Kéo từ **Admin** đến **User**
3. Line style: Solid (nét liền)
4. Arrow: Empty triangle

---

### 5.2. Quan hệ Include (Bắt buộc)

**Định nghĩa:** Use case A **«include»** Use case B = A luôn gọi B

#### Ví dụ:

```
"Đặt hàng" «include» "Chọn phương thức thanh toán"
```
→ Khi đặt hàng, BẮT BUỘC phải chọn phương thức thanh toán

```
"Thêm vào giỏ hàng" «include» "Kiểm tra tồn kho"
```
→ Trước khi thêm vào giỏ, BẮT BUỘC kiểm tra còn hàng không

```
"Thanh toán VNPay" «include» "Tạo payment URL"
```
→ Muốn thanh toán VNPay, BẮT BUỘC phải tạo URL

**Cách vẽ trong DrawIO:**
1. Chọn **Dashed Arrow** (mũi tên nét đứt)
2. Kéo từ Use Case chính → Use case phụ
3. Double click vào line, thêm text: **«include»**
4. Line style: Dashed
5. Arrow: Open arrow

---

### 5.3. Quan hệ Extend (Tùy chọn)

**Định nghĩa:** Use case A **«extend»** Use case B = A là phần mở rộng tùy chọn của B

#### Ví dụ:

```
"Đăng nhập" «extend» "Đăng nhập Google"
```
→ Đăng nhập Google là TUỲ CHỌN mở rộng của Đăng nhập thường

```
"Chọn phương thức thanh toán" «extend» "Thanh toán VNPay"
"Chọn phương thức thanh toán" «extend» "Thanh toán COD"
```
→ VNPay và COD là 2 lựa chọn mở rộng (chọn 1 trong 2)

```
"Xem đơn hàng" «extend» "Hủy đơn hàng"
```
→ Hủy đơn là hành động TÙY CHỌN khi xem đơn

**Cách vẽ trong DrawIO:**
1. Chọn **Dashed Arrow**
2. Kéo từ Use case mở rộng → Use case gốc (NGƯỢC LẠI với include)
3. Double click line, thêm: **«extend»**
4. Có thể thêm điều kiện: `[nếu trangthai=0]`

---

### 5.4. Bảng tổng hợp quan hệ Include

| Use Case chính | Include | Use Case phụ |
|---|---|---|
| Đặt hàng | «include» | Chọn phương thức thanh toán |
| Thêm vào giỏ hàng | «include» | Kiểm tra tồn kho |
| Thanh toán VNPay | «include» | Tạo payment URL |
| Thanh toán VNPay | «include» | Xác nhận thanh toán |
| Quên mật khẩu | «include» | Gửi OTP qua email |
| Xem danh sách đơn hàng | «include» | Xem chi tiết đơn hàng |
| Quản lý sản phẩm | «include» | Thêm sản phẩm |
| Quản lý sản phẩm | «include» | Sửa sản phẩm |
| Quản lý sản phẩm | «include» | Xóa sản phẩm |
| Thêm sản phẩm | «include» | Upload ảnh |
| Xem tồn kho | «include» | Cập nhật tồn kho |
| Xem thống kê | «include» | Xem biểu đồ bán chạy |
| Quản lý người dùng | «include» | Phân quyền admin |
| Quản lý người dùng | «include» | Xóa người dùng |
| Quản lý voucher | «include» | Thêm voucher |
| Quản lý voucher | «include» | Sửa voucher |
| Quản lý voucher | «include» | Xóa voucher |
| Áp dụng voucher | «include» | Kiểm tra điều kiện voucher |

---

### 5.5. Bảng tổng hợp quan hệ Extend

| Use Case gốc | Extend | Use Case mở rộng |
|---|---|---|
| Đăng nhập | «extend» | Đăng nhập Google |
| Đăng nhập | «extend» | Quên mật khẩu |
| Chọn phương thức thanh toán | «extend» | Thanh toán VNPay |
| Chọn phương thức thanh toán | «extend» | Thanh toán COD |
| Xem đơn hàng | «extend» | Hủy đơn hàng [trangthai=0] |
| Xem đơn hàng | «extend» | Tiếp tục thanh toán [trangthai=-1] |
| Đặt hàng | «extend» | Áp dụng voucher (tùy chọn) |
| Quản lý voucher | «extend» | Xem lịch sử sử dụng |

---

## 6. HƯỚNG DẪN VẼ TỪNG BƯỚC

### BƯỚC 1: Vẽ System Boundary (Hệ thống)

1. Kéo shape **Rectangle** từ thanh General
2. Kích thước: 1200px (width) x 800px (height)
3. Màu:
   - Fill: White hoặc Light Blue (#F0F8FF)
   - Border: Blue (#0066CC), thickness 2px
4. Text (đặt ở góc trên):
   ```
   App Bán Đồng Hồ
   E-Commerce System
   ```
5. Font: Arial Bold, 16pt
6. Vị trí: Giữa canvas

---

### BƯỚC 2: Vẽ Actors

#### 2.1. Actor chính (bên TRÁI system boundary)

**Vẽ Guest:**
1. Kéo **Actor** từ UML library
2. Đặt bên trái, trên cùng (ngoài boundary)
3. Rename: `Guest`
4. Màu: Gray (#808080)

**Vẽ User:**
1. Kéo Actor thứ 2
2. Đặt dưới Guest
3. Rename: `User`
4. Màu: Blue (#0066CC)

**Vẽ Admin:**
1. Kéo Actor thứ 3
2. Đặt dưới User
3. Rename: `Admin`
4. Màu: Red (#CC0000)

**Vẽ mũi tên kế thừa:**
1. Chọn **Generalization arrow**
2. Kéo từ Admin → User
3. Style: Solid line, empty triangle

#### 2.2. Actor phụ (bên PHẢI system boundary)

**VNPay:**
- Shape: Component hoặc Actor
- Màu: Orange (#FF6600)
- Text: `VNPay\n«external system»`

**Firebase Cloud Messaging:**
- Shape: Component
- Màu: Green (#00AA00)
- Text: `FCM\n«notification service»`

**PHPMailer:**
- Shape: Component
- Màu: Purple (#9900CC)
- Text: `PHPMailer\n«email service»`

**Google Sign-In:**
- Shape: Component
- Màu: Google Blue (#4285F4)
- Text: `Google Sign-In\n«auth service»`

---

### BƯỚC 3: Vẽ Use Cases (bên TRONG boundary)

#### 3.1. Shape Use Case
- Shape: **Ellipse** (hình ovan)
- Kích thước tiêu chuẩn: 120px x 60px
- Màu fill: Light Yellow (#FFFFCC)
- Border: Black, 1px
- Font: Arial, 10pt, center aligned

#### 3.2. Bố cục Use Case

**Nhóm theo chức năng theo cột:**

```
+-------------------------------------------------+
|                                                 |
|  GUEST  |  UC-A1: Xem SP                        |
|         |  UC-A2: Tìm kiếm                      |
|         |  UC-A3: Xem chi tiết                  |
|---------|                                       |
|  USER   |  UC-A4: Đăng ký                       |
|         |  UC-A5: Đăng nhập                     |
|         |    ├─ UC-A6: Đăng nhập Google         |
|         |    └─ UC-A7: Quên mật khẩu            |
|         |  UC-A8: Cập nhật thông tin            |
|         |                                       |
|         |  UC-B1: Thêm vào giỏ                  |
|         |  UC-B2: Xem giỏ hàng                  |
|         |  UC-B6: Đặt hàng                      |
|         |  UC-B7: Chọn thanh toán               |
|         |    ├─ UC-B8: VNPay                    |
|         |    └─ UC-B9: COD                      |
|         |                                       |
|         |  UC-C1: Xem đơn hàng                  |
|         |    ├─ UC-C2: Xem chi tiết             |
|         |    ├─ UC-C3: Hủy đơn                  |
|         |    └─ UC-C4: Tiếp tục thanh toán      |
|---------|                                       |
|  ADMIN  |  UC-D1: Quản lý SP                    |
|         |  UC-E1: Quản lý tồn kho               |
|         |  UC-F1: Xem thống kê                  |
|         |  UC-G1: Quản lý người dùng            |
|         |  UC-H1: Quản lý tất cả đơn hàng       |
|         |  UC-I1: Quản lý voucher               |
+-------------------------------------------------+
```

#### 3.3. Vẽ từng Use Case

**Ví dụ: Vẽ "Đăng nhập"**

1. Kéo Ellipse vào giữa boundary
2. Resize: 140px x 70px
3. Double click → nhập text: `Đăng nhập`
4. Màu fill: #FFFFCC
5. Border: Black 1px

**Ví dụ: Vẽ "Đăng nhập Google" (extend)**

1. Kéo Ellipse nhỏ hơn bên cạnh
2. Size: 120px x 60px
3. Text: `Đăng nhập Google`
4. Màu fill nhạt hơn: #FFFFEE (để phân biệt extend)

---

### BƯỚC 4: Vẽ quan hệ giữa Actor và Use Case

#### 4.1. Association (nối Actor với Use Case)

**Ví dụ: User → Đăng nhập**

1. Chọn **Line** (không có mũi tên)
2. Kéo từ Actor User → Use Case "Đăng nhập"
3. Style: Solid line, no arrow
4. Thickness: 1px

**Lưu ý:**
- Guest chỉ nối với: Xem SP, Tìm kiếm, Xem chi tiết, Đăng ký, Đăng nhập
- User nối với: Tất cả use case không phải admin
- Admin nối với: Use case quản trị (D, E, F, G, H, I)
- Admin KHÔNG cần nối lại use case của User (vì đã kế thừa)

#### 4.2. Quan hệ với External System

**Ví dụ: Thanh toán VNPay → VNPay actor**

1. Line từ Use Case "Thanh toán VNPay" → VNPay (bên phải)
2. Style: Solid line
3. Có thể thêm text: `xử lý thanh toán`

---

### BƯỚC 5: Vẽ quan hệ Include

**Ví dụ: "Đặt hàng" include "Chọn phương thức thanh toán"**

1. Chọn **Dashed Arrow** (mũi tên nét đứt)
2. **Kéo từ "Đặt hàng" → "Chọn phương thức thanh toán"**
3. Style:
   - Line: Dashed
   - Arrow: Open arrow head
   - Thickness: 1px
4. Double click line → Add label:
   ```
   «include»
   ```
5. Label position: Center của line
6. Font: Arial Italic, 9pt

**Lặp lại cho tất cả quan hệ include trong bảng 5.4**

---

### BƯỚC 6: Vẽ quan hệ Extend

**Ví dụ: "Đăng nhập Google" extend "Đăng nhập"**

1. Chọn Dashed Arrow
2. **Kéo NGƯỢC: từ "Đăng nhập Google" → "Đăng nhập"**
3. Style giống include nhưng hướng ngược lại
4. Label: `«extend»`
5. Có thể thêm điều kiện (optional):
   ```
   «extend»
   [user chọn Google]
   ```

**Lặp lại cho tất cả quan hệ extend trong bảng 5.5**

---

### BƯỚC 7: Tô màu và phân nhóm

#### 7.1. Tạo nhóm bằng màu nền

**Nhóm Xác thực:**
- Tạo Rectangle nhạt (opacity 30%)
- Màu: Light Blue (#E6F3FF)
- Đặt layer: Send to Back
- Bao quanh: UC-A1 đến UC-A9
- Text góc: "Xác thực"

**Nhóm Giỏ hàng:**
- Màu: Light Green (#E6FFE6)
- Bao quanh: UC-B1 đến UC-B10

**Nhóm Quản trị:**
- Màu: Light Red (#FFE6E6)
- Bao quanh: UC-D, E, F, G, H, I

#### 7.2. Thêm ghi chú (Note)

Kéo shape **Note** từ UML:
```
┌─────────────────┐
│ 📝 Note:        │
│ Admin kế thừa   │
│ tất cả quyền    │
│ của User        │
└─────────────────┘
```
Đặt gần mũi tên kế thừa Admin → User

---

### BƯỚC 8: Hoàn thiện và kiểm tra

#### 8.1. Checklist

- [ ] Tất cả Actor đều có ít nhất 1 use case
- [ ] Mọi Use Case đều có ít nhất 1 Actor
- [ ] Quan hệ Include: mũi tên từ chính → phụ
- [ ] Quan hệ Extend: mũi tên từ mở rộng → gốc
- [ ] Mũi tên kế thừa: Admin → User (tam giác rỗng)
- [ ] External actors ở bên phải
- [ ] Primary actors ở bên trái
- [ ] Use cases nằm TRONG boundary
- [ ] Actors nằm NGOÀI boundary

#### 8.2. Thêm Legend (Chú thích)

Góc dưới phải, thêm bảng:

```
┌─────────────────────────────────┐
│ LEGEND                          │
├─────────────────────────────────┤
│ ———————→  Association           │
│ - - - →   «include» (bắt buộc) │
│ - - - →   «extend» (tùy chọn)  │
│ ───▷     Generalization        │
│ 🔵 User   🔴 Admin              │
│ 🟠 External System              │
└─────────────────────────────────┘
```

---

## 7. MẸO VÀ BEST PRACTICES

### 7.1. Cách đặt tên Use Case

✅ **ĐÚNG:**
- "Đăng nhập" (động từ ngắn gọn)
- "Xem danh sách sản phẩm"
- "Cập nhật thông tin cá nhân"

❌ **SAI:**
- "Hệ thống cho phép user đăng nhập" (quá dài)
- "Login" (nên dùng tiếng Việt)
- "Màn hình đăng nhập" (không phải màn hình)

### 7.2. Nguyên tắc Include vs Extend

**Dùng Include khi:**
- Use case A KHÔNG THỂ hoàn thành nếu thiếu B
- B là bước BẮT BUỘC của A
- Ví dụ: "Đặt hàng" phải include "Thanh toán"

**Dùng Extend khi:**
- Use case A có thể hoàn thành KHÔNG CẦN B
- B là lựa chọn TÙY CHỌN
- Ví dụ: "Đăng nhập" có thể không cần "Đăng nhập Google"

### 7.3. Lỗi thường gặp

❌ **Lỗi 1: Vẽ màn hình thay vì chức năng**
```
SAI: "Màn hình giỏ hàng"
ĐÚNG: "Xem giỏ hàng"
```

❌ **Lỗi 2: Include ngược chiều**
```
SAI: "Thanh toán" include "Đặt hàng"
ĐÚNG: "Đặt hàng" include "Thanh toán"
```

❌ **Lỗi 3: Extend sai hướng**
```
SAI: "Đăng nhập" extend "Đăng nhập Google"
ĐÚNG: "Đăng nhập Google" extend "Đăng nhập"
```

❌ **Lỗi 4: Actor nằm trong boundary**
```
Actor phải ở NGOÀI system boundary
```

❌ **Lỗi 5: Quá nhiều use case nhỏ**
```
Không cần tách: "Click nút đăng nhập", "Nhập email", "Nhập password"
Gộp thành: "Đăng nhập"
```

### 7.4. Tối ưu hóa Layout

**Khi có quá nhiều Use Case:**

1. **Tách thành nhiều diagram:**
   - Diagram 1: Xác thực & Sản phẩm
   - Diagram 2: Giỏ hàng & Đặt hàng
   - Diagram 3: Quản trị (Admin)

2. **Sử dụng Package:**
   - Nhóm các use case liên quan vào Package
   - Ví dụ: Package "Thanh toán" chứa VNPay, COD, Voucher

3. **Zoom và Grid:**
   - Zoom: 75% để thấy toàn cảnh
   - Grid: 10px để căn chỉnh đều

### 7.5. Export và Lưu

**Lưu file:**
- Format: `.drawio` (có thể edit lại)
- File → Save As → `UseCase_AppBanDongHo.drawio`

**Export hình ảnh:**
- File → Export As → PNG
- Resolution: 300 DPI (cho báo cáo)
- Transparent background: Tích nếu dùng trong slide

**Export PDF:**
- File → Export As → PDF
- Chọn: Current page hoặc All pages
- Dùng cho in ấn

---

## 8. MẪU DRAWIO HOÀN CHỈNH

### Cấu trúc file DrawIO (XML snippet)

```xml
<mxGraphModel>
  <root>
    <!-- System Boundary -->
    <mxCell id="boundary" value="App Bán Đồng Hồ&#xa;E-Commerce System" 
            style="rounded=0;whiteSpace=wrap;html=1;fillColor=#F0F8FF;strokeColor=#0066CC;strokeWidth=2;fontSize=16;fontStyle=1"/>
    
    <!-- Actor: User -->
    <mxCell id="user" value="User" style="shape=umlActor;verticalLabelPosition=bottom;labelBackgroundColor=#ffffff;verticalAlign=top;fillColor=#0066CC"/>
    
    <!-- Actor: Admin (với Generalization) -->
    <mxCell id="admin" value="Admin" style="shape=umlActor;fillColor=#CC0000"/>
    <mxCell id="gen1" value="" style="endArrow=block;endSize=16;endFill=0;html=1;" source="admin" target="user"/>
    
    <!-- Use Case: Đăng nhập -->
    <mxCell id="uc_login" value="Đăng nhập" style="ellipse;whiteSpace=wrap;html=1;fillColor=#FFFFCC"/>
    
    <!-- Use Case: Đăng nhập Google (Extend) -->
    <mxCell id="uc_google" value="Đăng nhập Google" style="ellipse;whiteSpace=wrap;html=1;fillColor=#FFFFEE"/>
    <mxCell id="extend1" value="&amp;laquo;extend&amp;raquo;" style="endArrow=open;endSize=12;dashed=1;html=1;fontStyle=2" source="uc_google" target="uc_login"/>
    
    <!-- Use Case: Đặt hàng include Thanh toán -->
    <mxCell id="uc_order" value="Đặt hàng" style="ellipse;whiteSpace=wrap;html=1;fillColor=#FFFFCC"/>
    <mxCell id="uc_payment" value="Chọn phương thức&#xa;thanh toán" style="ellipse;whiteSpace=wrap;html=1;fillColor=#FFFFCC"/>
    <mxCell id="include1" value="&amp;laquo;include&amp;raquo;" style="endArrow=open;endSize=12;dashed=1;html=1;fontStyle=2" source="uc_order" target="uc_payment"/>
    
    <!-- External Actor: VNPay -->
    <mxCell id="vnpay" value="VNPay&#xa;&amp;laquo;external system&amp;raquo;" style="shape=umlActor;fillColor=#FF6600"/>
  </root>
</mxGraphModel>
```

---

## 9. TEMPLATE SƠ ĐỒ TỔNG QUAN (TEXT)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     APP BÁN ĐỒNG HỒ - E-COMMERCE SYSTEM                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  GUEST          ┌──────────────────┐                                       │
│   👤──────────> │ Xem sản phẩm     │                                       │
│    ╲            ├──────────────────┤                                       │
│     ╲           │ Tìm kiếm         │                                       │
│      ╲          ├──────────────────┤                                       │
│       ╲         │ Xem chi tiết     │                                       │
│        ╲        ├──────────────────┤                                       │
│         ╲       │ Đăng ký          │                                       │
│          └─────>│ Đăng nhập        │<──«extend»──┐                         │
│                 │   ├─Google       │             │                         │
│                 │   └─Quên MK      │<────────────┘                         │
│                 └──────────────────┘                                       │
│                                                                             │
│  USER           ┌──────────────────┐      ┌────────────────┐              │
│   👤──────────> │ Giỏ hàng         │      │ VNPay          │              │
│    │            │  ├─Thêm SP        │      │ «external»     │              │
│    │            │  ├─Xem giỏ       │      └───────▲────────┘              │
│    │            │  └─Cập nhật SL   │              │                        │
│    │            ├──────────────────┤              │                        │
│    │            │ Đặt hàng         │──«include»──>│ Thanh toán             │
│    │            │                  │              │  ├─VNPay «extend»───────┘
│    │            │                  │              │  └─COD   «extend»         │
│    │            ├──────────────────┤              │                        │
│    │            │ Xem đơn hàng     │              │                        │
│    │            │  ├─Chi tiết       │              │                        │
│    │            │  ├─Hủy đơn       │              │                        │
│    │            │  └─Tiếp tục TT   │──────────────┘                        │
│    │            └──────────────────┘                                       │
│    │                                                                        │
│    │                                                                        │
│    ▲                                                                        │
│    │ Generalization (kế thừa)                                              │
│    │                                                                        │
│  ADMIN          ┌──────────────────┐                                       │
│   👤──────────> │ Quản lý SP       │                                       │
│    │            │  ├─Thêm           │                                       │
│    │            │  ├─Sửa            │                                       │
│    │            │  └─Xóa            │                                       │
│    │            ├──────────────────┤                                       │
│    │            │ Quản lý tồn kho  │                                       │
│    │            ├──────────────────┤      ┌────────────────┐              │
│    │            │ Xem thống kê     │      │ Firebase FCM   │              │
│    │            │  └─Biểu đồ       │<─────│ «notification» │              │
│    │            ├──────────────────┤      └────────────────┘              │
│    │            │ Quản lý user     │                                       │
│    │            │  ├─Phân quyền    │                                       │
│    │            │  └─Xóa user      │                                       │
│    │            ├──────────────────┤                                       │
│    │            │ Quản lý voucher  │                                       │
│    │            │  ├─CRUD voucher  │                                       │
│    │            │  └─Lịch sử dùng  │                                       │
│    └───────────>│ Quản lý đơn hàng │                                       │
│                 │  └─Cập nhật TT   │                                       │
│                 └──────────────────┘                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

LEGEND:
───────>  Association
- - - ->  «include» (bắt buộc)
- - - ->  «extend» (tùy chọn)
───▷     Generalization
```

---

## 10. CHECKLIST HOÀN THIỆN

### ✅ Kiểm tra nội dung

- [ ] **22 Use Case chính** đã vẽ đầy đủ
- [ ] **4 Actor chính** (Guest, User, Admin, External)
- [ ] **Quan hệ kế thừa** Admin → User
- [ ] **18 quan hệ Include** theo bảng 5.4
- [ ] **8 quan hệ Extend** theo bảng 5.5
- [ ] **System Boundary** bao quanh use cases
- [ ] **Actors nằm ngoài** boundary

### ✅ Kiểm tra format

- [ ] Use case: Ellipse, màu vàng nhạt
- [ ] Actor: Stick figure hoặc Component
- [ ] Include: Dashed arrow, «include»
- [ ] Extend: Dashed arrow, «extend», ngược chiều
- [ ] Generalization: Solid arrow, empty triangle
- [ ] Font chữ rõ ràng (Arial 10-12pt)

### ✅ Kiểm tra logic

- [ ] Mọi use case có actor sử dụng
- [ ] Không có use case "mồ côi"
- [ ] Admin không nối trực tiếp use case của User (đã kế thừa)
- [ ] Include: Từ chính → phụ
- [ ] Extend: Từ mở rộng → gốc

### ✅ Kiểm tra thẩm mỹ

- [ ] Căn chỉnh đều các use case (dùng Grid)
- [ ] Nhóm theo màu sắc rõ ràng
- [ ] Không có đường nối chéo nhau quá nhiều
- [ ] Legend/chú thích ở góc
- [ ] Tên file và version ở header

---

## 11. TÀI LIỆU THAM KHẢO

### File liên quan trong dự án:
- `DANH_GIA_SO_DO_USE_CASE.md` - Đánh giá use case hiện tại
- `SO_DO_USE_CASE_CHINH_XAC.md` - Mô tả chi tiết use case
- `USE_CASE_DIAGRAM.md` - Tài liệu use case cũ
- `DANH_GIA_DU_AN_VA_CHUC_NANG_THIEU.md` - Phân tích chức năng

### Công cụ:
- Draw.io: https://app.diagrams.net/
- UML 2.5 Specification: https://www.omg.org/spec/UML/

### Tutorial Draw.io:
1. YouTube: "UML Use Case Diagram Tutorial"
2. Draw.io Documentation: https://www.diagrams.net/doc/

---

## 12. KẾT LUẬN

Sơ đồ Use Case này mô tả **TOÀN BỘ** chức năng của ứng dụng Bán Đồng Hồ, bao gồm:

✅ **10 module chính:**
1. Xác thực (9 use cases)
2. Giỏ hàng & Mua hàng (10 use cases)
3. Quản lý đơn hàng User (4 use cases)
4. Quản lý sản phẩm Admin (5 use cases)
5. Quản lý tồn kho Admin (4 use cases)
6. Thống kê Admin (2 use cases)
7. Quản lý người dùng Admin (3 use cases)
8. Quản lý đơn hàng Admin (2 use cases)
9. Hệ thống Voucher (6 use cases)
10. Thông báo Push (2 use cases)

✅ **Tổng cộng: ~47 use cases chi tiết**

✅ **Chuẩn UML 2.5:**
- Quan hệ Include/Extend đúng
- Generalization rõ ràng
- Actor phân loại đúng (Primary/Secondary)

---

**Người tạo:** AI Assistant  
**Ngày cập nhật:** 03/11/2025  
**Version:** 1.0  

---

📌 **Lưu ý:** Đây là tài liệu hướng dẫn chi tiết nhất cho việc vẽ Use Case Diagram. Hãy làm theo từng bước và kiểm tra checklist để đảm bảo sơ đồ hoàn chỉnh và chuẩn UML!

