# 📊 SƠ ĐỒ SEQUENCE: GIỎ HÀNG & THANH TOÁN

**Ngày tạo:** 15/11/2025  
**Công cụ:** Draw.io (XML format)  
**Chuẩn:** UML 2.0 Sequence Diagram  
**Màu sắc:** Black & White (Trắng đen)

---

## ✅ 1. SEQUENCE_05_THEM_VAO_GIO_HANG.drawio

### 📋 Thông tin chung
**Tên:** Sequence Diagram: Thêm vào giỏ hàng (Add to Cart)

**Mục đích:** Mô tả quy trình thêm sản phẩm vào giỏ hàng với validation tồn kho đầy đủ

### 👥 Đối tượng tham gia (6 actors/objects)
1. **User** (Actor) - Người dùng cuối
2. **:ChiTietActivity** (UI) - Màn hình chi tiết sản phẩm
3. **:Utils (Local Storage)** - Quản lý giỏ hàng local
4. **:ApiGioHang (Retrofit)** - API client
5. **:themGioHang.php** (Backend) - API endpoint
6. **:GioHangDB (MySQL)** - Database giỏ hàng

### 🔄 Luồng chính (20 bước)

#### Phase 1: Chọn số lượng và validation (1-8)
1. User chọn số lượng (+/-)
2. ChiTietActivity → updateSoLuong()
3. User click "Thêm giỏ hàng"
4. ChiTietActivity → checkTonKho()
5. ChiTietActivity → Utils: getSoLuongDaCo(idsp)
6. Utils → duyệt manggiohang để tìm sản phẩm
7. Utils → ChiTietActivity: return soLuongDaCo
8. ChiTietActivity → validate: soLuongDaCo + soLuong <= tonKho

#### Phase 2: Alt Frame - Validation Result
**[tonKho > 0 && soLuong <= tonKho]** - Đủ tồn kho
9. ChiTietActivity → Utils: addOrUpdateCart(sanpham, soLuong)
10. Utils → checkProductExists()

**[else]** - Không đủ tồn kho
11. ChiTietActivity → User: Toast("Không đủ hàng trong kho")

#### Phase 3: Sync với server (12-20)
12. ChiTietActivity → ApiGioHang: syncGioHangToServer()
13. ApiGioHang → POST themGioHang.php(user_id, idsp, soluong)
14. themGioHang.php → GioHangDB: INSERT INTO giohang ON DUPLICATE KEY UPDATE
15. GioHangDB → themGioHang.php: return success
16. themGioHang.php → ApiGioHang: return {success: true}
17. ApiGioHang → ChiTietActivity: onSuccess()
18. ChiTietActivity → Utils: updateCartBadge()
19. Utils → ChiTietActivity: EventBus.post(GioHangEvent)
20. ChiTietActivity → User: Toast("Đã thêm vào giỏ hàng")

### ⚡ Điểm nổi bật
- **Validation 2 lớp:** Kiểm tra số lượng đã có + số lượng mới vs tồn kho
- **Alt frame** cho luồng lỗi rõ ràng
- **EventBus** để cập nhật badge realtime
- **UPSERT pattern** với ON DUPLICATE KEY UPDATE

### 📊 Độ phức tạp
- **Số bước:** 20
- **Số đối tượng:** 6
- **Độ phức tạp:** ⭐⭐⭐⭐ (High)

---

## ✅ 2. SEQUENCE_06_THANH_TOAN_TAO_DON_HANG.drawio

### 📋 Thông tin chung
**Tên:** Sequence Diagram: Thanh toán & Tạo đơn hàng (Checkout & Payment)

**Mục đích:** Quy trình thanh toán với 2 phương thức: VNPay (online) và COD (trả khi nhận hàng)

### 👥 Đối tượng tham gia (8 actors/objects)
1. User (Actor)
2. :GioHangActivity (Cart UI)
3. :DatHangActivity (Order Info UI)
4. :ThanhToanActivity (Payment Method UI)
5. :VNPay Gateway (External Service)
6. :ApiDonHang (Retrofit)
7. :taoDonHang.php (Backend)
8. :Database (MySQL)

### 🔄 Luồng chính (33 bước)

#### Phase 1: Giỏ hàng (1-6)
1. Xem giỏ hàng
2. Hiển thị danh sách sản phẩm
3. Chọn sản phẩm mua (checkbox)
4. EventBus.post(TinhTongEvent)
5. Hiển thị tổng tiền
6. Click "Mua hàng"

#### Phase 2: Nhập thông tin (7-12)
7. startActivity(DatHangActivity)
8. Nhập địa chỉ & số điện thoại
9. validate()
10. Hiển thị tổng tiền
11. Click "Thanh toán"
12. validateAddress()

#### Phase 3: Chọn phương thức (13-14)
13. startActivity(ThanhToanActivity)
14. Hiển thị phương thức (COD, VNPay)

#### Phase 4: Alt Frame – Payment Method

**[VNPay Payment]** (15-21)
15. Chọn "VNPay"
16. createVNPayURL(amount)
17. return paymentURL
18. openWebView(paymentURL)
19. Nhập thông tin thẻ
20. processPayment()
21. Callback appbandienthoai://vnpay_return

**[COD Payment]**
- Bỏ qua xử lý online → nhảy tới tạo đơn hàng

#### Phase 5: Tạo đơn hàng (22-28)
22. taoDonHang(user_id, diachi, tongtien, phuongthuc)
23. POST taoDonHang.php
24. BEGIN TRANSACTION
25. INSERT INTO donhang
26. return donhang_id
27. INSERT INTO chitietdonhang (Trigger trừ tồn kho)
28. COMMIT

#### Phase 6: Hoàn tất (29-33)
29. return {success: true, donhang_id}
30. return Observable<DonHangResponse>
31. clearCart()
32. Toast("Đặt hàng thành công")
33. finish() → MainActivity

### ⚡ Điểm nổi bật
- Alt frame phân tách rõ Online vs Offline (COD)
- VNPay WebView + deep link callback
- Transaction an toàn (BEGIN/COMMIT)
- Trigger tự động trừ tồn kho khi insert chi tiết đơn hàng
- Phân tách UI theo 3 activity (Cart → Order Info → Payment)

### 📊 Độ phức tạp
- **Số bước:** 33
- **Số đối tượng:** 8
- **Độ phức tạp:** ⭐⭐⭐⭐⭐ (Very High)

---

## 🎨 UML Guidelines
- Khoảng cách lifeline ổn định (≈200px)
- Message spacing ≈30px
- Gạch ngang divider giữa luồng online và COD
- Return dùng dashed arrow

---

## 📁 Files
```
SEQUENCE_05_THEM_VAO_GIO_HANG.drawio
SEQUENCE_06_THANH_TOAN_TAO_DON_HANG.drawio (không còn PayPal)
SO_DO_SEQUENCE_GIO_HANG_THANH_TOAN.md (cập nhật)
```

---

## ✅ Checklist
- [x] Xóa toàn bộ PayPal branch khỏi sơ đồ
- [x] Thu nhỏ lại alt frame
- [x] Khôi phục vị trí divider (y=1030)
- [x] Giữ nguyên numbering ban đầu (1–33)
- [x] Cập nhật tài liệu markdown

---

## 🎯 Kết luận
Đã xóa hoàn toàn luồng PayPal và khôi phục sơ đồ thanh toán về dạng 2 phương thức (VNPay + COD) đúng chuẩn ban đầu.

Nếu cần thêm lại PayPal sau này hoặc bổ sung Momo/ZaloPay, có thể tạo nhánh mới mà không ảnh hưởng numbering hiện tại.

**Phiên bản:** 1.2 (Loại bỏ PayPal)  
**Ngày cập nhật:** 15/11/2025
