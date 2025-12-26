# BÁO CÁO CẬP NHẬT SƠ ĐỒ ACTIVITY DIAGRAM

**Ngày:** 15/11/2025  
**Trạng thái:** ✅ HOÀN THÀNH

---

## TỔNG QUAN CÁC THAY ĐỔI

### 1. ACTIVITY_01: Đăng ký tài khoản
**Trạng thái:** ✅ Đã xóa Email Service trước đó (giữ nguyên)

**Cấu trúc hiện tại:**
- 4 lanes: User, UI/Activity, Backend/API, Database
- Email Service đã được xóa khỏi flow
- Sau khi đăng ký thành công, chỉ hiển thị thông báo và chuyển về màn hình đăng nhập
- Không còn gửi email chào mừng qua PHPMailer

**Ghi chú:** File này không cần thay đổi thêm.

---

### 2. ACTIVITY_02: Đăng nhập
**Trạng thái:** ✅ Đã gộp Auth Service vào Backend/API

**Cấu trúc hiện tại:**
- 4 lanes @ 330px mỗi lane:
  - User (330px)
  - UI/Activity (330px)
  - Backend/API (330px) - **Đã gộp Auth Service vào đây**
  - Database (330px)
- Tổng width pool: 1320px (4 × 330px)

**Luồng xử lý:**
1. User mở màn hình đăng nhập
2. Nhập email + password
3. Click Đăng nhập
4. UI validate input
5. Backend/API xử lý authentication (generateToken đã được gộp vào Backend/API)
6. Database SELECT user
7. Lưu token & user vào bộ nhớ local
8. Chuyển đến MainActivity

**Ghi chú:** Auth Service (generateToken) đã được gộp vào lane "Backend/API", không còn lane riêng biệt.

---

### 3. ACTIVITY_03: Tìm kiếm/Duyệt sản phẩm
**Trạng thái:** ✅ Đã gộp Main/Category/Detail thành UI và Backend/API

**Cấu trúc hiện tại:**
- 4 lanes @ 330px mỗi lane:
  - User (330px)
  - UI (Main/Category/Detail) (330px) - **Đã gộp**
  - Backend/API (330px)
  - Database (330px)

**Luồng xử lý:**
1. User mở ứng dụng
2. UI hiển thị danh mục (Main/Category đã gộp)
3. User chọn danh mục HOẶC nhập từ khóa tìm kiếm
4. UI gọi getSanPhamTheoLoai() hoặc searchProducts()
5. Backend/API xử lý (chitiet.php hoặc timkiem.php)
6. Database query sản phẩm
7. UI hiển thị danh sách
8. User click sản phẩm → Mở ChiTietActivity

**Ghi chú:** 
- Main/Category/Detail activities đã được gộp vào lane "UI"
- API đã được gộp vào "Backend/API"
- Không còn các lane thừa

---

### 4. ACTIVITY_04: Xem chi tiết sản phẩm
**Trạng thái:** ⚠️ Chưa kiểm tra chi tiết (không có trong yêu cầu update)

**Cấu trúc hiện tại:** 5 lanes (User, List Activity, Detail Activity, API, Database)

**Đề xuất:** Có thể giữ nguyên hoặc gộp thành 4 lanes nếu cần.

---

### 5. ACTIVITY_05: Thêm vào giỏ hàng
**Trạng thái:** ✅ Đã gộp Utils và API vào Backend/API

**Cấu trúc hiện tại:**
- 4 lanes @ 330px mỗi lane:
  - User (330px)
  - UI/Detail (330px)
  - Backend/API (330px) - **Đã gộp Utils + API**
  - Database (330px)

**Luồng xử lý:**
1. User chọn số lượng
2. Click Thêm giỏ
3. UI checkTonKho()
4. UI getSoLuongDaCoTrongGio()
5. UI validate tổng số lượng
6. UI addOrUpdateCart()
7. Backend/API syncGioHangToServer() (Utils đã gộp vào đây)
8. Backend/API POST themGioHang.php
9. Database INSERT/UPDATE giohang
10. UI updateBadge() và EventBus.post()
11. User nhận Toast: Đã thêm

**Ghi chú:** Utils (như updateBadge, EventBus) và API đã được gộp vào "Backend/API" lane.

---

### 6. ACTIVITY_06: Quy trình đặt hàng
**Trạng thái:** ✅ Đã gộp Cart/Order/Payment/Gateway, đã xóa PayPal

**Cấu trúc hiện tại:**
- 4 lanes @ 330px mỗi lane:
  - User (330px)
  - UI (Cart/Order/Payment) (330px) - **Đã gộp**
  - Backend/API (VNPay/COD) (330px) - **Đã gộp Gateway**
  - Database (330px)

**Luồng xử lý đơn giản hóa:**

#### Nhánh 1: COD (Ship COD)
1. User mở giỏ hàng
2. Chọn sản phẩm & tính tổng
3. Click Mua hàng
4. Nhập địa chỉ + SĐT
5. Validate input
6. Chọn phương thức: **COD**
7. Backend/API: POST taoDonHang.php
8. Database: INSERT donhang + chitietdonhang
9. Database: COMMIT
10. Backend/API: UPDATE trangthai=1 (Chờ xử lý)
11. UI: clearCart()
12. Hiển thị thành công → Về MainActivity

#### Nhánh 2: VNPay (Thanh toán online)
1. User mở giỏ hàng
2. Chọn sản phẩm & tính tổng
3. Click Mua hàng
4. Nhập địa chỉ + SĐT
5. Validate input
6. Chọn phương thức: **VNPay**
7. Backend/API: Mở WebView VNPay
8. User thanh toán trong WebView
9. VNPay callback về Backend/API
10. Backend/API: POST taoDonHang.php
11. Database: INSERT donhang + chitietdonhang
12. Database: COMMIT
13. Backend/API: UPDATE trangthai=1 (Đã thanh toán)
14. UI: clearCart()
15. Hiển thị thành công → Về MainActivity

**Đã xóa:**
- ❌ PayPal Gateway (đã xóa hoàn toàn)
- ❌ Các lane riêng biệt cho Cart/Order/Payment
- ❌ Gateway lane riêng

**Ghi chú:** 
- Chỉ còn 2 phương thức thanh toán: COD và VNPay
- Tất cả đã được gộp vào 4 lanes chuẩn
- Luồng đã được đơn giản hóa, dễ hiểu hơn

---

## CHUẨN HÓA CHUNG CHO TẤT CẢ SƠ ĐỒ

### Layout Swimlanes
- **Số lượng lanes:** 4 lanes cố định
- **Chiều rộng mỗi lane:** 330px
- **Tổng chiều rộng pool:** 1320px (4 × 330px)
- **Chiều cao pool:** 880px - 1020px (tùy độ phức tạp)

### Naming Convention
- Lane 1: **User** (Người dùng)
- Lane 2: **UI/Activity** hoặc **UI** (Giao diện/Logic UI)
- Lane 3: **Backend/API** (Server-side processing)
- Lane 4: **Database** (Cơ sở dữ liệu)

### Style Guide
- **Node shape:** rounded rectangle (rounded=1)
- **Fill color:** #FFF (trắng)
- **Stroke color:** #000 (đen)
- **Font size:** 11px cho nodes, 10px cho edge labels
- **Edge style:** orthogonalEdgeStyle
- **Start node:** Black filled circle (20×20px)
- **End node:** Black filled circle (20×20px)

---

## KẾT LUẬN

✅ **Tất cả 5 sơ đồ chính đã được cập nhật theo yêu cầu:**

1. ✅ ACTIVITY_01: Email Service đã xóa
2. ✅ ACTIVITY_02: Auth Service đã gộp vào Backend/API
3. ✅ ACTIVITY_03: Main/Category/Detail đã gộp thành UI
4. ✅ ACTIVITY_05: Utils đã gộp vào Backend/API
5. ✅ ACTIVITY_06: Cart/Order/Payment/Gateway đã gộp, PayPal đã xóa

**Lợi ích của việc chuẩn hóa:**
- Dễ đọc, dễ hiểu hơn với 4 lanes cố định
- Giảm độ phức tạp của sơ đồ
- Dễ bảo trì và cập nhật
- Phù hợp với kiến trúc thực tế của hệ thống

**Files đã update:**
- `ACTIVITY_01_DANG_KY_TAI_KHOAN_SWIMLANES.drawio` ✅
- `ACTIVITY_02_DANG_NHAP_SWIMLANES.drawio` ✅
- `ACTIVITY_03_TIM_KIEM_DUYET_SAN_PHAM_SWIMLANES.drawio` ✅
- `ACTIVITY_05_THEM_VAO_GIO_HANG_SWIMLANES.drawio` ✅
- `ACTIVITY_06_QUY_TRINH_DAT_HANG_SWIMLANES.drawio` ✅

---

**Người thực hiện:** GitHub Copilot  
**Ngày hoàn thành:** 15/11/2025

