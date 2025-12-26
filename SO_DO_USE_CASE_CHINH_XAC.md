# SƠ ĐỒ USE CASE - ỨNG DỤNG BÁN ĐỒNG HỒ

## 📋 TỔNG QUAN HỆ THỐNG

**Tên dự án:** Ứng dụng Bán Đồng Hồ (AppBanDongHo)  
**Nền tảng:** Android (Java) + PHP Backend  
**Database:** MySQL  
**Thanh toán:** VNPay  
**Thông báo:** Firebase Cloud Messaging (FCM)

---

## 👥 TÁC NHÂN (ACTORS)

### 1. **User (Người dùng thường - role=0)**
- Người dùng đã đăng ký/đăng nhập
- Có thể xem, tìm kiếm, mua sản phẩm
- Quản lý đơn hàng của mình

### 2. **Admin (Quản trị viên - role=1)**
- Có tất cả quyền của User
- Quản lý sản phẩm (CRUD)
- Quản lý tồn kho
- Xem tất cả đơn hàng của user
- Xem thống kê

### 3. **VNPay (Cổng thanh toán)**
- Xử lý thanh toán online

### 4. **Firebase Cloud Messaging**
- Gửi thông báo push

---

## 🎯 CÁC USE CASE CHI TIẾT

## A. NHÓM XÁC THỰC (AUTHENTICATION) - User & Admin

### UC-A1: Đăng ký tài khoản
**Actor:** User  
**Luồng chính:**
1. Mở màn hình đăng ký
2. Nhập email, mật khẩu, tên, số điện thoại
3. Xác thực dữ liệu
4. Tạo tài khoản với role=0 (user thường)
5. Chuyển về màn hình đăng nhập

**API:** `dangki.php`  
**Activity:** `DangKiActivity`

---

### UC-A2: Đăng nhập thường
**Actor:** User, Admin  
**Luồng chính:**
1. Nhập email và mật khẩu
2. Click "Đăng nhập"
3. Xác thực thông tin
4. Nhận thông tin user (bao gồm role)
5. Lưu vào PaperDB
6. Chuyển về MainActivity

**API:** `dangnhap.php`  
**Activity:** `DangNhapActivity`  
**Dữ liệu trả về:** id, email, username, mobile, role

---

### UC-A3: Đăng nhập Google
**Actor:** User  
**Luồng chính:**
1. Click nút "Đăng nhập Google"
2. Chọn tài khoản Google
3. Xác thực với Firebase
4. Tạo/cập nhật tài khoản trong database
5. Tự động set role=0 (user thường)
6. Chuyển về MainActivity

**API:** `dangnhap.php` (với login_type=google)  
**Activity:** `DangNhapActivity`  
**Service:** Google Sign-In API

---

### UC-A4: Quên mật khẩu (OTP qua Email)
**Actor:** User, Admin  
**Luồng chính:**
1. Nhập email
2. Click "Gửi OTP"
3. Hệ thống gửi mã OTP qua email
4. Nhập OTP và mật khẩu mới
5. Xác thực OTP
6. Cập nhật mật khẩu mới

**API:** `reset_pass.php`, `verify_otp_reset_pass.php`  
**Activity:** `ResetPassActivity`  
**Service:** PHPMailer

---

### UC-A5: Đăng xuất
**Actor:** User, Admin  
**Luồng chính:**
1. Click "Đăng xuất" trong menu
2. Xóa dữ liệu user từ PaperDB
3. Chuyển về màn hình đăng nhập

**Activity:** `MainActivity`

---

## B. NHÓM XEM SẢN PHẨM - User & Admin

### UC-B1: Xem trang chủ
**Actor:** User, Admin  
**Luồng chính:**
1. Mở ứng dụng
2. Xem banner quảng cáo (ViewFlipper)
3. Xem menu danh mục sản phẩm (Drawer)
4. Xem danh sách sản phẩm mới nhất (RecyclerView)

**API:** `getloaisp.php`, `getspmoi.php`  
**Activity:** `MainActivity`

---

### UC-B2: Xem sản phẩm theo danh mục
**Actor:** User, Admin  
**Luồng chính:**
1. Click vào danh mục (Đồng hồ cơ / Đồng hồ điện tử)
2. Xem danh sách sản phẩm theo loại
3. Có thể sắp xếp theo giá

**API:** `chitiet.php?loai=<id>`  
**Activity:** `DienThoaiActivity`, `LaptopActivity`

---

### UC-B3: Xem chi tiết sản phẩm
**Actor:** User, Admin  
**Luồng chính:**
1. Click vào sản phẩm
2. Xem hình ảnh, tên, giá, mô tả
3. Xem số lượng tồn kho
4. Ch��n số lượng muốn mua (+/-)
5. Click "Thêm vào giỏ hàng"

**API:** `kiemTraTonKho.php`  
**Activity:** `ChiTietActivity`

---

### UC-B4: Tìm kiếm sản phẩm
**Actor:** User, Admin  
**Luồng chính:**
1. Click icon tìm kiếm
2. Nhập từ khóa
3. Xem kết quả tự động (real-time)
4. Sắp xếp theo giá (tăng/giảm dần)

**API:** `timkiem.php`  
**Activity:** `SearchActivity`

---

## C. NHÓM GIỎ HÀNG & MUA HÀNG - User & Admin

### UC-C1: Quản lý giỏ hàng
**Actor:** User, Admin  
**Luồng chính:**
1. Vào "Giỏ hàng" từ icon
2. Xem danh sách sản phẩm đã thêm
3. **Check/Uncheck** sản phẩm muốn mua
4. Thay đổi số lượng (+/-)
5. Xóa sản phẩm khỏi giỏ
6. Xem tổng tiền (chỉ tính sản phẩm đã check)

**Activity:** `GioHangActivity`  
**Adapter:** `GioHangAdapter`  
**Lưu ý:** Tổng tiền tính từ `Utils.mangmuahang` (sản phẩm đã check)

---

### UC-C2: Đặt hàng
**Actor:** User, Admin  
**Luồng chính:**
1. Từ giỏ hàng, click "MUA HÀNG"
2. Kiểm tra đăng nhập
3. Kiểm tra có sản phẩm được chọn
4. Nhập thông tin giao hàng:
   - Email (tự động từ tài khoản)
   - Số điện thoại
   - Địa chỉ giao hàng
   - Ngày giao dự kiến (DatePicker)
5. Chọn phương thức thanh toán

**Activity:** `DatHangActivity`, `ThanhToanActivity`

---

### UC-C3: Thanh toán VNPay
**Actor:** User, Admin  
**Luồng chính:**
1. Click "Thanh toán VNPay"
2. Tạo đơn hàng trong database
3. Chuyển đến VNPay
4. Nhập thông tin thẻ
5. Xác nhận thanh toán
6. VNPay callback về app
7. Cập nhật trạng thái đơn hàng
8. Gửi thông báo FCM

**API:** 
- `taoDonHang.php`
- `vnpay_create_payment.php`
- `vnpay_return.php`
- `vnpay_check_status.php`

**Activity:** `ThanhToanActivity`, `XemDonActivity`  
**Deep Link:** `appbandienthoai://payment_return`

---

### UC-C4: Thanh toán khi nhận hàng (COD)
**Actor:** User, Admin  
**Luồng chính:**
1. Click "Đặt hàng"
2. Tạo đơn hàng với trạng thái "Chờ xử lý"
3. Kiểm tra và giảm tồn kho (trigger)
4. Xóa sản phẩm đã mua khỏi giỏ hàng
5. Hiển thị thông báo thành công
6. Chuyển về màn hình đơn hàng

**API:** `taoDonHang.php`  
**Trigger:** `after_chitietdonhang_insert` (tự động giảm tồn kho)

---

### UC-C5: Xem đơn hàng của tôi
**Actor:** User, Admin  
**Luồng chính:**
1. Vào menu "Đơn hàng"
2. Xem danh sách đơn hàng của mình
3. Click vào đơn để xem chi tiết:
   - Mã đơn hàng
   - Trạng thái
   - Địa chỉ, SĐT
   - Danh sách sản phẩm
   - Tổng tiền
   - Ngày đặt, ngày giao dự kiến

**API:** `xemdonhang.php?iduser=<id>&isadmin=0&viewmode=my`  
**Activity:** `XemDonActivity`, `ChiTietDonHangActivity`

---

### UC-C6: Hủy đơn hàng
**Actor:** User, Admin  
**Luồng chính:**
1. Vào chi tiết đơn hàng
2. Click "Hủy đơn hàng"
3. Xác nhận hủy
4. Cập nhật trạng thái "Đã hủy"
5. Hoàn lại tồn kho (trigger)

**API:** `huyDonHang.php`  
**Activity:** `ChiTietDonHangActivity`  
**Trigger:** `after_cancel_order` (hoàn tồn kho)

---

## D. NHÓM QUẢN LÝ CÁ NHÂN - User & Admin

### UC-D1: Xem/Cập nhật thông tin cá nhân
**Actor:** User, Admin  
**Luồng chính:**
1. Vào menu "Thông tin cá nhân"
2. Xem thông tin hiện tại
3. Chỉnh sửa: username, mobile
4. Click "Cập nhật"
5. Lưu vào database và PaperDB

**API:** `updateProfile.php`  
**Activity:** `UpdateProfileActivity`  
**Lưu ý:** 
- Tài khoản Google KHÔNG thể đổi mật khẩu
- Tài khoản thường có thể đổi mật khẩu

---

## E. NHÓM QUẢN LÝ SẢN PHẨM - CHỈ ADMIN

### UC-E1: Vào màn hình quản lý
**Actor:** Admin  
**Luồng chính:**
1. Vào menu "Quản lí" (chỉ admin thấy)
2. Kiểm tra quyền admin
3. Xem danh sách tất cả sản phẩm
4. Có các nút: Thêm sản phẩm, Thống kê, Tồn kho

**API:** `getspmoi.php`  
**Activity:** `QuanLiActivity`  
**Kiểm tra:** `Utils.user_current.isAdmin()`

---

### UC-E2: Thêm sản phẩm mới
**Actor:** Admin  
**Luồng chính:**
1. Click nút "Thêm sản phẩm"
2. Chọn ảnh từ thư viện
3. Nhập thông tin:
   - Tên sản phẩm
   - Giá
   - Mô tả
   - Loại (Đồng hồ cơ / Đồng hồ điện tử)
   - Số lượng tồn kho
4. Click "Thêm sản phẩm"
5. Upload ảnh và lưu vào database

**API:** `insertsp.php`  
**Activity:** `ThemSPActivity`

---

### UC-E3: Sửa sản phẩm
**Actor:** Admin  
**Luồng chính:**
1. Click vào sản phẩm trong danh sách
2. Chọn "Sửa"
3. Chỉnh sửa thông tin
4. Có thể thay đổi ảnh
5. Click "Cập nhật"

**API:** `updatesp.php`  
**Activity:** `ThemSPActivity` (chế độ edit)

---

### UC-E4: Xóa sản phẩm
**Actor:** Admin  
**Luồng chính:**
1. Click vào sản phẩm trong danh sách
2. Chọn "Xóa"
3. Xác nhận xóa
4. Xóa khỏi database

**API:** `xoa.php`  
**Activity:** `QuanLiActivity`

---

### UC-E5: Quản lý tồn kho
**Actor:** Admin  
**Luồng chính:**
1. Click "Tồn kho" từ màn hình quản lý
2. Xem danh sách sản phẩm và số lượng tồn
3. Chọn sản phẩm cần cập nhật
4. Chọn hành động:
   - Nhập kho (tăng số lượng)
   - Xuất kho (giảm số lượng)
5. Nhập số lượng thay đổi
6. Cập nhật vào database

**API:** `getTonKho.php`, `capNhatTonKho.php`  
**Activity:** `TonKhoActivity`

---

### UC-E6: Xem thống kê sản phẩm
**Actor:** Admin  
**Luồng chính:**
1. Click "Thống kê" từ màn hình quản lý
2. Xem biểu đồ cột (Bar Chart):
   - Trục X: Tên sản phẩm
   - Trục Y: Số lượng đã bán
3. Dữ liệu từ bảng chitietdonhang

**API:** `thongke.php`  
**Activity:** `ThongKeActivity`  
**Library:** MPAndroidChart

---

### UC-E7: Xem tất cả đơn hàng (Quản lý đơn)
**Actor:** Admin  
**Luồng chính:**
1. Vào menu "Đơn hàng"
2. Thấy 2 nút toggle:
   - "Đơn của tôi" (mặc định)
   - "Tất cả đơn hàng"
3. Click "Tất cả đơn hàng"
4. Xem tất cả đơn của user thường (role=0)
5. Click vào đơn để xem chi tiết
6. Có thể cập nhật trạng thái đơn

**API:** `xemdonhang.php?iduser=<id>&isadmin=1&viewmode=all`  
**Activity:** `XemDonActivity`

---

## F. NHÓM HỆ THỐNG

### UC-F1: Nhận thông báo Push
**Actor:** User, Admin  
**Luồng chính:**
1. Đơn hàng được tạo/cập nhật
2. Backend gửi thông báo FCM
3. App nhận thông báo
4. Hiển thị notification

**Service:** Firebase Cloud Messaging

---

### UC-F2: Kiểm tra kết nối Internet
**Actor:** User, Admin  
**Luồng chính:**
1. App kiểm tra kết nối khi khởi động
2. Nếu không có Internet → hiển thị thông báo
3. Nếu có → load dữ liệu

**Activity:** `MainActivity`

---

## 📊 BẢNG TỔNG HỢP USE CASE

| Mã UC | Tên Use Case | Actor | API | Activity |
|-------|-------------|-------|-----|----------|
| **A. XÁC THỰC** |
| A1 | Đăng ký tài khoản | User | dangki.php | DangKiActivity |
| A2 | Đăng nhập thường | User, Admin | dangnhap.php | DangNhapActivity |
| A3 | Đăng nhập Google | User | dangnhap.php | DangNhapActivity |
| A4 | Quên mật khẩu OTP | User, Admin | reset_pass.php | ResetPassActivity |
| A5 | Đăng xuất | User, Admin | - | MainActivity |
| **B. XEM SẢN PHẨM** |
| B1 | Xem trang chủ | User, Admin | getspmoi.php | MainActivity |
| B2 | Xem theo danh mục | User, Admin | chitiet.php | DienThoaiActivity |
| B3 | Xem chi tiết SP | User, Admin | kiemTraTonKho.php | ChiTietActivity |
| B4 | Tìm kiếm | User, Admin | timkiem.php | SearchActivity |
| **C. GIỎ HÀNG & MUA HÀNG** |
| C1 | Quản lý giỏ hàng | User, Admin | - | GioHangActivity |
| C2 | Đặt hàng | User, Admin | taoDonHang.php | DatHangActivity |
| C3 | Thanh toán VNPay | User, Admin | vnpay_*.php | ThanhToanActivity |
| C4 | Thanh toán COD | User, Admin | taoDonHang.php | ThanhToanActivity |
| C5 | Xem đơn hàng | User, Admin | xemdonhang.php | XemDonActivity |
| C6 | Hủy đơn hàng | User, Admin | huyDonHang.php | ChiTietDonHangActivity |
| **D. QUẢN LÝ CÁ NHÂN** |
| D1 | Cập nhật thông tin | User, Admin | updateProfile.php | UpdateProfileActivity |
| **E. QUẢN LÝ (ADMIN)** |
| E1 | Vào màn hình quản lý | Admin | getspmoi.php | QuanLiActivity |
| E2 | Thêm sản phẩm | Admin | insertsp.php | ThemSPActivity |
| E3 | Sửa sản phẩm | Admin | updatesp.php | ThemSPActivity |
| E4 | Xóa sản phẩm | Admin | xoa.php | QuanLiActivity |
| E5 | Quản lý tồn kho | Admin | capNhatTonKho.php | TonKhoActivity |
| E6 | Xem thống kê | Admin | thongke.php | ThongKeActivity |
| E7 | Xem tất cả đơn hàng | Admin | xemdonhang.php | XemDonActivity |

---

## 🚫 CHỨC NĂNG KHÔNG CÓ/KHÔNG HOẠT ĐỘNG

### ❌ Các tính năng KHÔNG có trong hệ thống:

1. **Gửi tin nhắn/Chat** - Không có
2. **Đánh giá sản phẩm/Rating** - Không có
3. **Wishlist/Yêu thích** - Không có
4. **So sánh sản phẩm** - Không có
5. **Mã giảm giá/Coupon** - Không có
6. **Thanh toán MoMo** - Có file nhưng không tích hợp
7. **Thanh toán ZaloPay** - Có file nhưng không tích hợp
8. **Theo dõi vận chuyển** - Không có
9. **Guest/Khách** - Không có (bắt buộc đăng nhập)
10. **Giao diện Web Admin** - Chỉ có mobile app

---

## 🔐 PHÂN QUYỀN

### User thường (role=0):
✅ Xem sản phẩm  
✅ Tìm kiếm  
✅ Thêm vào giỏ hàng  
✅ Đặt hàng  
✅ Thanh toán  
✅ Xem đơn hàng của mình  
✅ Hủy đơn hàng  
✅ Cập nhật thông tin cá nhân  
❌ KHÔNG thấy menu "Quản lí"  
❌ KHÔNG thể thêm/sửa/xóa sản phẩm  

### Admin (role=1):
✅ **TẤT CẢ quyền của User**  
✅ Vào menu "Quản lí"  
✅ Thêm/Sửa/Xóa sản phẩm  
✅ Quản lý tồn kho  
✅ Xem thống kê  
✅ Xem tất cả đơn hàng của user thường  
✅ Có thể mua sản phẩm (như user bình thường)  

---

## 🗄️ CẤU TRÚC DATABASE

### Bảng chính:
- `user` - Thông tin người dùng (có cột role)
- `sanphammoi` - Sản phẩm (có cột soluongtonkho)
- `loaisp` - Danh mục sản phẩm
- `donhang` - Đơn hàng
- `chitietdonhang` - Chi tiết đơn hàng
- `password_resets` - OTP reset mật khẩu

### Trigger:
- `after_chitietdonhang_insert` - Tự động giảm tồn kho khi đặt hàng
- `after_cancel_order` - Tự động hoàn tồn kho khi hủy đơn

---

## 🎨 SƠ ĐỒ USE CASE (Text-based)

```
┌─────────────────────────────────────────────────────────────┐
│                    HỆ THỐNG BÁN ĐỒNG HỒ                     │
└─────────────────────────────────────────────────────────────┘

        USER (role=0)                    ADMIN (role=1)
            │                                  │
            ├─ Đăng ký                         │
            ├─ Đăng nhập ─────────────────────┤
            ├─ Đăng nhập Google                │
            ├─ Quên mật khẩu ──────────────────┤
            │                                  │
            ├─ Xem trang chủ ──────────────────┤
            ├─ Xem sản phẩm theo danh mục ─────┤
            ├─ Xem chi tiết sản phẩm ──────────┤
            ├─ Tìm kiếm sản phẩm ──────────────┤
            │                                  │
            ├─ Quản lý giỏ hàng ───────────────┤
            ├─ Đặt hàng ───────────────────────┤
            ├─ Thanh toán VNPay ───────────────┤
            ├─ Thanh toán COD ─────────────────┤
            ├─ Xem đơn hàng của tôi ───────────┤
            ├─ Hủy đơn hàng ───────────────────┤
            │                                  │
            ├─ Cập nhật thông tin ─────────────┤
            ├─ Đăng xuất ──────────────────────┤
            │                                  │
            │                                  ├─ Vào màn hình Quản lí
            │                                  ├─ Thêm sản phẩm
            │                                  ├─ Sửa sản phẩm
            │                                  ├─ Xóa sản phẩm
            │                                  ├─ Quản lý tồn kho
            │                                  ├─ Xem thống kê
            │                                  └─ Xem tất cả đơn hàng
            │
            └──────── Firebase FCM (Thông báo) ─────────┘
                              │
                        VNPay (Thanh toán)
```

---

## 📱 LUỒNG HOẠT ĐỘNG CHÍNH

### 1. Luồng đăng ký và mua hàng (User):
```
Đăng ký → Đăng nhập → Xem sản phẩm → Thêm vào giỏ 
→ Check sản phẩm → Nhập thông tin giao hàng 
→ Chọn thanh toán (VNPay/COD) → Đặt hàng thành công 
→ Nhận thông báo → Xem đơn hàng
```

### 2. Luồng quản lý sản phẩm (Admin):
```
Đăng nhập admin → Vào "Quản lí" → Thêm sản phẩm mới 
→ Upload ảnh → Nhập thông tin → Lưu 
→ Sản phẩm hiển thị cho user
```

### 3. Luồng quản lý tồn kho (Admin):
```
Vào "Quản lí" → Click "Tồn kho" → Chọn sản phẩm 
→ Chọn "Nhập kho" hoặc "Xuất kho" → Nhập số lượng 
→ Cập nhật tồn kho
```

### 4. Luồng thanh toán VNPay:
```
Chọn sản phẩm → Đặt hàng → Click "Thanh toán VNPay" 
→ Tạo đơn trong DB → Chuyển sang VNPay 
→ Nhập thẻ → Xác nhận → VNPay callback 
→ Cập nhật trạng thái → Thông báo thành công
```

---

## ✅ KẾT LUẬN

Hệ thống có **2 loại người dùng** (User và Admin) với **27 use case** chính thống được chia thành 6 nhóm chức năng:
- Xác thực (5 UC)
- Xem sản phẩm (4 UC)
- Giỏ hàng & Mua hàng (6 UC)
- Quản lý cá nhân (1 UC)
- Quản lý Admin (7 UC)
- Hệ thống (2 UC)

**Đặc điểm nổi bật:**
- ✅ KHÔNG có chế độ Guest (bắt buộc đăng nhập)
- ✅ Admin có thể mua hàng như user thường
- ✅ Hệ thống phân quyền rõ ràng (role-based)
- ✅ Thanh toán VNPay hoạt động đầy đủ
- ✅ Quản lý tồn kho tự động (trigger)
- ✅ Thông báo Push qua FCM
- ✅ Hỗ trợ đăng nhập Google
- ✅ Reset mật khẩu qua OTP email

