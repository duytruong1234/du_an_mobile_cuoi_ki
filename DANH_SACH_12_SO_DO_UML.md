# 📊 DANH SÁCH 12 SƠ ĐỒ UML - ỨNG DỤNG BÁN ĐỒNG HỒ

**Ngày tạo:** 15/11/2025  
**Công cụ:** Draw.io (XML format)  
**Chuẩn:** UML 2.0  
**Màu sắc:** Trắng đen (Black & White)

---

## 🔷 PHẦN I: 6 SƠ ĐỒ SEQUENCE DIAGRAM

### ✅ 1. SEQUENCE_01_DANG_KY_TAI_KHOAN.drawio
**Tên:** Sequence Diagram: Đăng ký tài khoản (User Registration)

**Đối tượng tham gia:**
- User (Actor)
- DangKiActivity (UI)
- ApiDangKi (Retrofit)
- UserDatabase (MySQL)
- PHPMailer (Email Service)

**Luồng chính (15 bước):**
1. User nhập thông tin (email, password, name, mobile)
2. Validate dữ liệu
3. Click nút Đăng ký
4. POST dangki.php
5. Kiểm tra email đã tồn tại
6. Return exists = false
7. Kiểm tra mobile đã tồn tại
8. Return exists = false
9. INSERT INTO user
10. Return user_id
11. Gửi email chào mừng
12. Email sent
13. Return success response
14. Hiển thị Toast thành công
15. Chuyển về màn hình đăng nhập

**File:** `SEQUENCE_01_DANG_KY_TAI_KHOAN.drawio`

---

### ✅ 2. SEQUENCE_02_DANG_NHAP.drawio
**Tên:** Sequence Diagram: Đăng nhập (Login)

**Đối tượng tham gia:**
- User (Actor)
- DangNhapActivity (UI)
- ApiDangNhap (Retrofit)
- dangnhap.php (Backend)
- UserDatabase (MySQL)

**Luồng chính (14 bước):**
1. Nhập email và password
2. Validate input
3. Click Đăng nhập
4. Gọi API dangnhap()
5. POST dangnhap.php
6. SELECT * FROM user WHERE email=? AND pass=md5(?)
7. Return userData (id, email, username, mobile, role)
8. Check user exists
9. Generate token
10. Return success response
11. Return Observable<UserResponse>
12. Lưu vào PaperDB (Utils.user_current)
13. Hiển thị Toast thành công
14. Chuyển về MainActivity

**Alt Flow:** [else] return error "Email hoặc mật khẩu không đúng"

**File:** `SEQUENCE_02_DANG_NHAP.drawio`

---

### ✅ 3. SEQUENCE_03_XEM_DANH_SACH_SAN_PHAM.drawio
**Tên:** Sequence Diagram: Xem danh sách sản phẩm (Browse Products)

**Đối tượng tham gia:**
- User (Actor)
- MainActivity (UI)
- DienThoaiActivity (Category Activity)
- ApiChiTiet (Retrofit)
- chitiet.php (Backend)
- ProductDB (MySQL)

**Luồng chính (14 bước):**
1. Mở ứng dụng
2. Load danh mục
3. Hiển thị danh mục
4. Click chọn danh mục (loaisp_id)
5. onCreate()
6. Gọi API getSanPhamTheoLoai(page, loaisp_id)
7. POST chitiet.php
8. SELECT * FROM sanphammoi WHERE loaisp=? LIMIT ?, 10
9. Return List<Product>
10. Format JSON
11. Return {success: true, data: [products]}
12. Return Observable<ProductModel>
13. adapter.notifyDataSetChanged()
14. Hiển thị danh sách sản phẩm

**Note:** Hỗ trợ phân trang (pagination) - mỗi trang 10 sản phẩm

**File:** `SEQUENCE_03_XEM_DANH_SACH_SAN_PHAM.drawio`

---

### ✅ 4. SEQUENCE_04_XEM_CHI_TIET_SAN_PHAM.drawio
**Tên:** Sequence Diagram: Xem chi tiết sản phẩm (View Product Detail)

**Đối tượng tham gia:**
- User (Actor)
- DienThoaiActivity (List)
- ChiTietActivity (Detail)
- ApiTonKho (Retrofit)
- kiemTraTonKho.php (Backend)
- ProductDB (MySQL)

**Luồng chính (15 bước):**
1. Click sản phẩm
2. Intent(ChiTietActivity, sanpham_id)
3. onCreate()
4. getSanPhamFromIntent()
5. Hiển thị thông tin cơ bản (tên, giá, hình, mô tả)
6. Gọi API kiemTraTonKho(idsp)
7. GET kiemTraTonKho.php?idsp=xxx
8. SELECT soluongtonkho FROM sanphammoi WHERE id=?
9. Return productData
10. Format JSON
11. Return {success: true, data: {soluongtonkho: 50}}
12. Return Observable<TonKhoResponse>
13. updateTonKhoUI(soluongtonkho)
14. Hiển thị tồn kho với màu sắc
15. Enable/Disable nút thêm giỏ hàng

**Note:** Màu sắc tồn kho:
- Đỏ (≤0): Hết hàng
- Cam (≤5): Sắp hết
- Xanh (>5): Còn nhiều

**File:** `SEQUENCE_04_XEM_CHI_TIET_SAN_PHAM.drawio`

---

### ✅ 5. SEQUENCE_05_THEM_VAO_GIO_HANG.drawio
**Tên:** Sequence Diagram: Thêm vào giỏ hàng (Add to Cart)

**Đối tượng tham gia:**
- User (Actor)
- ChiTietActivity (UI)
- Utils (Local Storage)
- ApiGioHang (Retrofit)
- themGioHang.php (Backend)
- GioHangDB (MySQL)

**Luồng chính (19 bước):**
1. Chọn số lượng (+/-)
2. Update số lượng
3. Click Thêm giỏ hàng
4. Check tồn kho
5. Lấy số lượng đã có trong giỏ
6. Duyệt Utils.manggiohang
7. Return soLuongDaCo
8. Validate: soLuongDaCo + soLuong <= tonKho
9. addOrUpdateCart()
10. checkProductExists()
11. syncGioHangToServer()
12. POST themGioHang.php
13. INSERT INTO giohang ON DUPLICATE KEY UPDATE
14. Return success
15. Return JSON
16. onSuccess()
17. Update badge
18. EventBus.post(GioHangEvent)
19. Hiển thị Toast "Đã thêm vào giỏ hàng"

**Alt Flow:**
- [tonKho <= 0] → Hiển thị "Sản phẩm đã hết hàng"
- [soLuong > tonKho] → Hiển thị "Không đủ hàng trong kho"

**File:** `SEQUENCE_05_THEM_VAO_GIO_HANG.drawio`

---

### ✅ 6. SEQUENCE_06_THANH_TOAN_TAO_DON_HANG.drawio
**Tên:** Sequence Diagram: Thanh toán & Tạo đơn hàng (Checkout)

**Đối tượng tham gia:**
- User (Actor)
- GioHangActivity (Cart)
- DatHangActivity (Order Info)
- ThanhToanActivity (Payment)
- ApiDonHang (Retrofit)
- taoDonHang.php (Backend)
- Database (MySQL)
- VNPay Gateway (Payment)

**Luồng chính (26 bước):**
1. Xem giỏ hàng
2. Chọn sản phẩm mua (checkbox)
3. EventBus.post(TinhTongEvent)
4. Click Mua hàng
5. Nhập địa chỉ và số điện thoại
6. Validate input
7. Click Thanh toán
8. Hiển thị phương thức (COD, VNPay)
9. Chọn VNPay
10. Create VNPay payment URL
11. Open WebView
12. User nhập thông tin thẻ tại VNPay
13. VNPay callback: appbandienthoai://vnpay_return
14. Gọi API taoDonHang()
15. POST taoDonHang.php
16. BEGIN TRANSACTION
17. INSERT INTO donhang
18. Return donhang_id
19. INSERT INTO chitietdonhang → Trigger trừ tồn kho
20. Return success
21. COMMIT
22. Return success response
23. Return Observable
24. clearCart()
25. Hiển thị thông báo thành công
26. Chuyển về MainActivity

**Note:**
- Nếu chọn COD, bỏ qua bước 10-13
- Trigger tự động trừ tồn kho khi INSERT chitietdonhang

**File:** `SEQUENCE_06_THANH_TOAN_TAO_DON_HANG.drawio`

---

## 🔶 PHẦN II: 6 SƠ ĐỒ ACTIVITY DIAGRAM

### ✅ 7. ACTIVITY_01_DANG_KY_TAI_KHOAN.drawio
**Tên:** Activity Diagram: Đăng ký tài khoản (Registration Activity)

**Luồng hoạt động:**

```
[Start]
   ↓
Mở trang đăng ký
   ↓
Nhập thông tin (email, password, name, mobile)
   ↓
Click nút "Đăng ký"
   ↓
[Decision] Dữ liệu hợp lệ?
   ├─ Không → Hiển thị lỗi validation → Back to Nhập thông tin
   └─ Có ↓
Gửi request đăng ký đến server
   ↓
[Decision] Email hoặc mobile đã tồn tại?
   ├─ Có → Hiển thị lỗi "Email/Mobile đã được đăng ký" → Back
   └─ Không ↓
Lưu tài khoản vào database (INSERT INTO user)
   ↓
Gửi email chào mừng (PHPMailer)
   ↓
Hiển thị thông báo "Đăng ký thành công"
   ↓
Chuyển về màn hình đăng nhập
   ↓
[End]
```

**Node đặc biệt:**
- 2 Decision nodes (rhombus)
- 2 Error loops
- Role mặc định = 0 (User thường)

**File:** `ACTIVITY_01_DANG_KY_TAI_KHOAN.drawio`

---

### ✅ 8. ACTIVITY_02_DANG_NHAP.drawio
**Tên:** Activity Diagram: Đăng nhập (Login Activity)

**Luồng hoạt động:**

```
[Start]
   ↓
Mở màn hình đăng nhập
   ↓
Nhập email và password
   ↓
Click nút "Đăng nhập"
   ↓
[Decision] Dữ liệu hợp lệ?
   ├─ Không → Hiển thị lỗi "Vui lòng nhập đầy đủ" → Back
   └─ Có ↓
Gửi request đăng nhập đến server
   ↓
Truy vấn database: SELECT * FROM user WHERE email=? AND pass=md5(?)
   ↓
[Decision] Tài khoản tồn tại?
   ├─ Không → Hiển thị lỗi "Email hoặc mật khẩu không đúng" → Back
   └─ Có ↓
Lấy thông tin user (id, email, username, mobile, role)
   ↓
Lưu thông tin vào PaperDB (Utils.user_current = userData)
   ↓
Hiển thị Toast "Đăng nhập thành công"
   ↓
Chuyển về MainActivity (Trang chủ)
   ↓
[End]
```

**Note:**
- Mật khẩu được mã hóa md5() trước khi so sánh
- Alternative Flow: Đăng nhập Google (sử dụng Google Sign-In API)

**File:** `ACTIVITY_02_DANG_NHAP.drawio`

---

### ✅ 9. ACTIVITY_03_TIM_KIEM_DUYET_SAN_PHAM.drawio
**Tên:** Activity Diagram: Tìm kiếm / Duyệt sản phẩm (Browse/Search Activity)

**Luồng hoạt động:**

```
[Start]
   ↓
[Fork] ───┬─────────────────────┐
          ↓                     ↓
    Chọn danh mục         Nhập từ khóa
    (Đồng hồ, ĐT...)     tìm kiếm
          ↓                     ↓
    API chitiet.php       API timkiem.php
    (page, loaisp)        (keyword)
          ↓                     ↓
    SELECT WHERE          SELECT WHERE
    loaisp=?              tensp LIKE '%?%'
          ↓                     ↓
[Join] ───┴─────────────────────┘
   ↓
[Decision] Có sản phẩm phù hợp?
   ├─ Không → Hiển thị "Không tìm thấy sản phẩm" → [End]
   └─ Có ↓
Hiển thị danh sách sản phẩm trên RecyclerView
   ↓
User xem danh sách
   ↓
[Decision] Click xem chi tiết?
   ├─ Không → [End]
   └─ Có ↓
Chuyển sang ChiTietActivity
   ↓
[End]
```

**Node đặc biệt:**
- Fork (parallel activities)
- Join (merge)
- 2 Decision nodes

**Note:** Hỗ trợ phân trang (pagination) khi duyệt theo danh mục

**File:** `ACTIVITY_03_TIM_KIEM_DUYET_SAN_PHAM.drawio`

---

### ✅ 10. ACTIVITY_04_XEM_CHI_TIET_SAN_PHAM.drawio
**Tên:** Activity Diagram: Xem chi tiết sản phẩm (Product Detail Activity)

**Luồng hoạt động:**

```
[Start]
   ↓
Click chọn sản phẩm từ danh sách
   ↓
Mở ChiTietActivity
   ↓
Lấy thông tin sản phẩm từ Intent
   ↓
Hiển thị thông tin cơ bản (tên, giá, hình ảnh, mô tả)
   ↓
Gọi API kiemTraTonKho.php(idsp)
   ↓
SELECT soluongtonkho FROM sanphammoi WHERE id=?
   ↓
[Decision] Tồn kho > 0?
   ├─ Không (≤0) ─────────────┐
   │   ↓                       │
   │   Hiển thị "Hết hàng"     │
   │   (màu đỏ)                │
   │   ↓                       │
   │   Vô hiệu hóa nút         │
   │   "Thêm giỏ hàng"         │
   │   ↓                       │
   └─ Có (>0) ────────────────┤
       ↓                       │
       Hiển thị tồn kho        │
       với màu sắc phù hợp     │
       ↓                       │
       Kích hoạt nút           │
       "Thêm giỏ hàng"         │
       ↓                       │
[Join] ────────────────────────┘
   ↓
User xem đầy đủ thông tin sản phẩm
   ↓
[End]
```

**Màu sắc tồn kho:**
- 🔴 Đỏ (≤0): Hết hàng
- 🟠 Cam (≤5): Sắp hết
- 🟢 Xanh (>5): Còn nhiều

**Next Activity:** Thêm vào giỏ hàng (nếu user click nút)

**File:** `ACTIVITY_04_XEM_CHI_TIET_SAN_PHAM.drawio`

---

### ✅ 11. ACTIVITY_05_THEM_VAO_GIO_HANG.drawio
**Tên:** Activity Diagram: Thêm vào giỏ hàng (Add to Cart Activity)

**Luồng hoạt động:**

```
[Start]
   ↓
Chọn số lượng sản phẩm (+/-)
   ↓
Click nút "Thêm giỏ hàng"
   ↓
[Decision] Tồn kho > 0?
   ├─ Không → Hiển thị lỗi "Sản phẩm đã hết hàng" → [End]
   └─ Có ↓
Kiểm tra số lượng đã có trong giỏ hàng
   ↓
[Decision] Tổng SL <= Tồn kho?
   ├─ Không → Hiển thị lỗi "Không đủ hàng. Còn lại: X" → [End]
   └─ Có ↓
[Decision] SP đã có trong giỏ?
   ├─ Không ───────────────────┐
   │   ↓                       │
   │   Thêm sản phẩm mới       │
   │   vào Utils.manggiohang   │
   │   ↓                       │
   └─ Có ─────────────────────┤
       ↓                       │
       Cộng thêm số lượng      │
       cho sản phẩm đã có      │
       ↓                       │
[Join] ────────────────────────┘
   ↓
Đồng bộ giỏ hàng lên server (nếu đã đăng nhập)
   ↓
Cập nhật badge số lượng trên icon giỏ hàng
   ↓
Gửi event cập nhật: EventBus.post(GioHangEvent)
   ↓
Hiển thị Toast "Đã thêm vào giỏ hàng"
   ↓
[End]
```

**Note:**
- Công thức kiểm tra: soLuongDaCo + soLuong ≤ tonKhoHienTai
- Giỏ hàng được lưu local trong Utils.manggiohang và sync lên server

**File:** `ACTIVITY_05_THEM_VAO_GIO_HANG.drawio`

---

### ✅ 12. ACTIVITY_06_QUY_TRINH_DAT_HANG.drawio
**Tên:** Activity Diagram: Quy trình đặt hàng & Thanh toán (Checkout Activity)

**Luồng hoạt động:**

```
[Start]
   ↓
Mở giỏ hàng
   ↓
Chọn sản phẩm muốn mua (checkbox)
   ↓
Tính tổng tiền (EventBus.TinhTongEvent)
   ↓
Click nút "Mua hàng"
   ↓
Nhập địa chỉ giao hàng và số điện thoại
   ↓
[Decision] Thông tin hợp lệ?
   ├─ Không → Hiển thị lỗi "Vui lòng nhập đầy đủ" → Back
   └─ Có ↓
Chọn phương thức thanh toán
   ↓
[Decision] Phương thức?
   ├─ COD ────────────────────────┐
   │   ↓                          │
   │   Tạo đơn hàng               │
   │   (trangthai = 1)            │
   │   ↓                          │
   │   Gọi API taoDonHang.php     │
   │   ↓                          │
   │   BEGIN TRANSACTION          │
   │   ↓                          │
   │   INSERT donhang             │
   │   ↓                          │
   │   INSERT chitietdonhang      │
   │   → Trigger trừ tồn kho      │
   │   ↓                          │
   │   COMMIT                     │
   │   ↓                          │
   └─ VNPay ──────────────────────┤
       ↓                          │
       Tạo đơn hàng               │
       (trangthai = 0)            │
       ↓                          │
       Tạo payment URL VNPay      │
       ↓                          │
       Mở WebView VNPay           │
       ↓                          │
       User thanh toán tại VNPay  │
       ↓                          │
       VNPay callback             │
       ↓                          │
       [Decision] Thanh toán      │
       thành công?                │
       ├─ Không → Xóa đơn hàng →[End]
       └─ Có ↓                    │
       Cập nhật trangthai = 1     │
       Trigger trừ tồn kho        │
       ↓                          │
[Join] ────────────────────────────┘
   ↓
Xóa giỏ hàng (Utils.mangmuahang.clear())
   ↓
Hiển thị thông báo thành công và mã đơn hàng
   ↓
Chuyển về MainActivity
   ↓
[End]
```

**Note:**
- COD = Cash on Delivery (Thanh toán khi nhận hàng)
- Trigger tự động trừ tồn kho khi INSERT chitietdonhang

**File:** `ACTIVITY_06_QUY_TRINH_DAT_HANG.drawio`

---

## 📋 BẢNG TỔNG HỢP

| STT | Loại | Tên file | Số bước | Số đối tượng | Complexity |
|-----|------|----------|---------|--------------|------------|
| 1 | Sequence | SEQUENCE_01_DANG_KY_TAI_KHOAN.drawio | 15 | 5 | Medium |
| 2 | Sequence | SEQUENCE_02_DANG_NHAP.drawio | 14 | 5 | Medium |
| 3 | Sequence | SEQUENCE_03_XEM_DANH_SACH_SAN_PHAM.drawio | 14 | 6 | Medium |
| 4 | Sequence | SEQUENCE_04_XEM_CHI_TIET_SAN_PHAM.drawio | 15 | 6 | Medium |
| 5 | Sequence | SEQUENCE_05_THEM_VAO_GIO_HANG.drawio | 19 | 6 | High |
| 6 | Sequence | SEQUENCE_06_THANH_TOAN_TAO_DON_HANG.drawio | 26 | 8 | Very High |
| 7 | Activity | ACTIVITY_01_DANG_KY_TAI_KHOAN.drawio | 8 activities | 2 decisions | Medium |
| 8 | Activity | ACTIVITY_02_DANG_NHAP.drawio | 9 activities | 2 decisions | Medium |
| 9 | Activity | ACTIVITY_03_TIM_KIEM_DUYET_SAN_PHAM.drawio | 8 activities | 2 decisions + fork/join | High |
| 10 | Activity | ACTIVITY_04_XEM_CHI_TIET_SAN_PHAM.drawio | 9 activities | 1 decision + branch/join | Medium |
| 11 | Activity | ACTIVITY_05_THEM_VAO_GIO_HANG.drawio | 11 activities | 3 decisions + branch/join | High |
| 12 | Activity | ACTIVITY_06_QUY_TRINH_DAT_HANG.drawio | 13 activities | 3 decisions + branch/join | Very High |

---

## 🎨 ĐẶC ĐIỂM KỸ THUẬT

### Sequence Diagram:
- **Lifeline:** Dashed line (đường đứt)
- **Activation bar:** Solid rectangle trên lifeline
- **Synchronous message:** Solid arrow (→)
- **Return message:** Dashed arrow with open arrowhead (⇢)
- **Actor:** Stick figure
- **Object:** Rectangle với `:ClassName`
- **Alt frame:** Conditional logic với [condition]

### Activity Diagram:
- **Start node:** Filled circle (●)
- **End node:** Filled circle với ring (⊙)
- **Activity:** Rounded rectangle
- **Decision:** Diamond (◆)
- **Fork/Join:** Thick horizontal bar
- **Edge:** Arrow với label
- **Swimlanes:** Vertical partitions (optional)

---

## 📥 CÁCH SỬ DỤNG

### Bước 1: Import vào Draw.io
1. Mở https://app.diagrams.net/
2. File → Open from → Device
3. Chọn file `.drawio` tương ứng
4. Xem và chỉnh sửa nếu cần

### Bước 2: Export sang các format khác
- **PNG:** File → Export as → PNG (cho báo cáo)
- **PDF:** File → Export as → PDF (cho in ấn)
- **SVG:** File → Export as → SVG (cho web)
- **JPEG:** File → Export as → JPEG

### Bước 3: Tích hợp vào tài liệu
- Copy vào Word/PowerPoint
- Nhúng vào LaTeX
- Sử dụng trong báo cáo đồ án

---

## ✅ KIỂM TRA CHUẨN UML

### Sequence Diagram - Checklist:
- [x] Có Actor và Objects đầy đủ
- [x] Lifeline được vẽ đúng (dashed line)
- [x] Activation bar hiển thị thời gian hoạt động
- [x] Message có số thứ tự (1, 2, 3...)
- [x] Return message là dashed arrow
- [x] Synchronous call là solid arrow
- [x] Alt/Opt frame cho logic rẽ nhánh
- [x] Note giải thích logic phức tạp

### Activity Diagram - Checklist:
- [x] Có Start node (filled circle)
- [x] Có End node (circle với ring)
- [x] Activity là rounded rectangle
- [x] Decision node là diamond
- [x] Fork/Join là thick bar
- [x] Edge có label mô tả điều kiện
- [x] Flow logic rõ ràng không bị deadlock
- [x] Error handling được thể hiện

---

## 🎯 MỤC ĐÍCH SỬ DỤNG

### Cho giảng viên:
- Đánh giá thiết kế hệ thống
- Kiểm tra tính đúng đắn của logic
- Chấm điểm đồ án tốt nghiệp

### Cho sinh viên:
- Hiểu rõ flow của từng chức năng
- Tài liệu tham khảo khi code
- Trình bày trong báo cáo đồ án

### Cho developer:
- Documentation chuẩn UML
- Dễ maintain và mở rộng
- Onboarding cho thành viên mới

---

## 📞 HỖ TRỢ

Nếu có vấn đề với các file sơ đồ:
1. Kiểm tra file có mở được trong Draw.io không
2. Kiểm tra encoding (UTF-8)
3. Kiểm tra XML format hợp lệ

**Ngày tạo:** 15/11/2025  
**Tool:** Draw.io Desktop / Web  
**Format:** XML (mxfile)  
**Version:** 22.0.0

