# 🎫 LUỒNG HOẠT ĐỘNG HỆ THỐNG VOUCHER

**Ngày tạo:** 03/11/2025  
**Mục đích:** Hướng dẫn chi tiết cách user nhận và sử dụng voucher, admin quản lý voucher

---

## 📱 PHẦN 1: LUỒNG USER NHẬN VÀ SỬ DỤNG VOUCHER

### 🎁 **CÁCH 1: User Xem Danh Sách Voucher Khả Dụng**

```
┌─────────────────────────────────────────────────────┐
│          MÀNG HÌNH THANH TOÁN (Android App)         │
└─────────────────────────────────────────────────────┘

Bước 1: User vào màn hình Thanh toán/Giỏ hàng
   ↓
Bước 2: Nhấn nút "Chọn mã giảm giá" hoặc "Xem voucher"
   ↓
Bước 3: App gọi API getVouchers.php
   - Truyền: user_id, tong_tien
   - Nhận về 2 danh sách:
     ✅ vouchers_applicable: Voucher có thể dùng ngay (đủ điều kiện)
     ⛔ vouchers_not_applicable: Voucher chưa đủ điều kiện
   ↓
Bước 4: Hiển thị danh sách voucher
   - Mã voucher: NEWUSER20
   - Tên: Giảm 20% cho khách hàng mới
   - Điều kiện: Đơn tối thiểu 500,000đ
   - Giảm tối đa: 200,000đ
   - Còn lại: 95/100 lượt
   - Hết hạn: 31/12/2025
   ↓
Bước 5: User chọn voucher → Áp dụng tự động
```

**API: `getVouchers.php`**
```php
POST: Server/getVouchers.php
Params:
  - user_id: 123
  - tong_tien: 1000000 (1 triệu đồng)

Response:
{
  "success": true,
  "vouchers_applicable": [
    {
      "id": 1,
      "ma_voucher": "NEWUSER20",
      "ten_voucher": "Giảm 20% cho khách mới",
      "text_giam": "Giảm 20% (tối đa 200,000đ)",
      "don_toi_thieu": 500000,
      "co_the_dung": true,
      "con_luot": 5
    }
  ],
  "vouchers_not_applicable": [
    {
      "ma_voucher": "SALE50K",
      "don_toi_thieu": 2000000,
      "co_the_dung": false,
      "thieu": 1000000  // Còn thiếu 1 triệu
    }
  ]
}
```

---

### ✍️ **CÁCH 2: User Nhập Mã Voucher Thủ Công**

```
┌─────────────────────────────────────────────────────┐
│              NHẬP MÃ GIẢM GIÁ                       │
└─────────────────────────────────────────────────────┘

Bước 1: User nhập mã "NEWUSER20" vào ô text
   ↓
Bước 2: Nhấn nút "Áp dụng"
   ↓
Bước 3: App gọi API checkVoucher.php
   - Truyền: ma_voucher, user_id, tong_tien
   ↓
Bước 4: Server kiểm tra:
   ✓ Mã có tồn tại?
   ✓ Còn hạn sử dụng?
   ✓ Đơn hàng đủ điều kiện?
   ✓ User đủ điều kiện? (new/old user)
   ✓ User còn lượt dùng?
   ✓ Voucher còn số lượng?
   ↓
Bước 5: Server tính toán:
   - Tổng trước giảm: 1,000,000đ
   - Giảm 20%: 200,000đ
   - Tổng sau giảm: 800,000đ
   ↓
Bước 6: Trả về kết quả → App hiển thị
```

**API: `checkVoucher.php`**
```php
POST: Server/checkVoucher.php
Params:
  - ma_voucher: "NEWUSER20"
  - user_id: 123
  - tong_tien: 1000000

Response (Success):
{
  "success": true,
  "message": "Áp dụng mã giảm giá thành công",
  "voucher": {
    "id": 1,
    "ma_voucher": "NEWUSER20",
    "ten_voucher": "Giảm 20% cho khách mới",
    "loai_giam": "percent"
  },
  "tinh_toan": {
    "tong_truoc_giam": 1000000,
    "gia_tri_giam": 200000,
    "tong_sau_giam": 800000
  }
}

Response (Fail):
{
  "success": false,
  "message": "Mã voucher chỉ dành cho khách hàng mới"
}
```

---

### 🛒 **CÁCH 3: Áp Dụng Voucher Khi Thanh Toán**

```
┌─────────────────────────────────────────────────────┐
│         ĐẶT HÀNG VỚI VOUCHER (taoDonHang.php)       │
└─────────────────────────────────────────────────────┘

Bước 1: User nhấn "Đặt hàng" với voucher đã chọn
   ↓
Bước 2: App gọi API taoDonHang.php
   - Gửi thêm: voucher_id, ma_voucher, gia_tri_giam, tong_truoc_giam
   ↓
Bước 3: Server tạo đơn hàng
   INSERT INTO donhang (
     madonhang, iduser, tongtien,
     voucher_id,        // ID voucher
     ma_voucher,        // Mã voucher
     gia_tri_giam,      // Số tiền giảm
     tong_truoc_giam    // Tổng tiền gốc
   )
   ↓
Bước 4: Lưu lịch sử sử dụng voucher
   INSERT INTO voucher_usage (
     voucher_id, user_id, donhang_id,
     gia_tri_giam, ngay_su_dung
   )
   ↓
Bước 5: Trigger tự động tăng voucher.da_su_dung += 1
   ↓
Bước 6: Trả về đơn hàng thành công
```

**Luồng PayPal/VNPay:**
```
User chọn thanh toán PayPal/VNPay
  ↓
App gọi: paypal_create_payment.php / vnpay_create_payment.php
  - Gửi: voucher_id, ma_voucher, gia_tri_giam, tong_truoc_giam
  ↓
Server tạo đơn hàng với trạng thái "Chờ thanh toán PayPal/VNPay"
  - Lưu thông tin voucher vào đơn hàng
  - LƯU lịch sử voucher_usage
  ↓
User thanh toán thành công/thất bại
  ↓
PayPal/VNPay gọi callback → Cập nhật trạng thái đơn hàng
```

---

## 🔧 PHẦN 2: ADMIN QUẢN LÝ VOUCHER

### 🖥️ **LUỒNG ADMIN QUẢN LÝ VOUCHER (WEB INTERFACE)**

```
┌────────────────────────────────────────────────────┐
│         MÀN HÌNH QUẢN LÝ VOUCHER (Web Admin)       │
└────────────────────────────────────────────────────┘

Bước 1: Admin đăng nhập vào trang quản trị
   - URL: http://localhost/Server/admin/login_admin.php
   - Kiểm tra role = 1 (admin)
   ↓
Bước 2: Chọn menu "Voucher / Mã giảm giá"
   - URL: http://localhost/Server/admin/voucher_list.php
   ↓
Bước 3: Hệ thống gọi API getAllVouchers.php
   - Hiển thị bảng danh sách voucher:
     • Mã voucher, Tên, Loại giảm
     • Giá trị, Đơn tối thiểu
     • Số lượng: Đã dùng / Tổng số
     • Ngày hết hạn, Trạng thái
     • Hành động: Sửa | Xóa | Tắt/Bật
   ↓
Bước 4: Admin có thể:
   ➕ THÊM VOUCHER MỚI
      - Nhấn nút "Thêm voucher"
      - Điền form: Mã, Tên, Loại, Giá trị, Điều kiện...
      - Gọi API: addVoucher.php
   
   ✏️ SỬA VOUCHER
      - Nhấn "Sửa" trên dòng voucher
      - Form hiện thông tin cũ
      - Cập nhật → Gọi API: updateVoucher.php
   
   ❌ XÓA VOUCHER
      - Nhấn "Xóa" → Confirm
      - Gọi API: deleteVoucher.php
      - Xóa mềm: Chuyển trang_thai = 0
   
   🔍 TÌM KIẾM / LỌC
      - Lọc theo trạng thái: Hoạt động / Hết hạn / Sắp hết hạn
      - Tìm theo mã voucher
      - Lọc theo loại giảm: % / Fixed / Freeship
   
   📊 XEM THỐNG KÊ
      - Xem lịch sử sử dụng
      - Báo cáo hiệu quả voucher
      - Top voucher được dùng nhiều nhất
   ↓
Bước 5: Kết quả
   - Thông báo thành công / thất bại
   - Reload danh sách voucher
   - Log hành động admin
```

---

### 📡 **API CHO ADMIN QUẢN LÝ VOUCHER**

| API | Chức năng | Method | Input | Output |
|-----|-----------|--------|-------|--------|
| **getAllVouchers.php** | Lấy danh sách tất cả voucher | GET/POST | - | `{ success, vouchers[] }` |
| **addVoucher.php** | Thêm voucher mới | POST | Mã, tên, loại, giá trị, điều kiện, hạn sử dụng | `{ success, message, voucher_id }` |
| **updateVoucher.php** | Sửa thông tin voucher | POST | ID, các field cập nhật | `{ success, message }` |
| **deleteVoucher.php** | Xóa voucher | POST | ID | `{ success, message }` |
| **toggleVoucher.php** | Bật/Tắt voucher | POST | ID | `{ success, message, new_status }` |
| **getVoucherStats.php** | Thống kê sử dụng voucher | GET | voucher_id (optional) | `{ success, statistics }` |

---

### 📊 **QUẢN LÝ VOUCHER QUA DATABASE (PHPMyAdmin) - Cách Thủ Công**

Nếu chưa có giao diện Admin, bạn có thể thao tác trực tiếp trên **Database MySQL**:

#### **Cách 1: Tạo Voucher Mới**

```sql
-- Truy cập PHPMyAdmin: http://localhost/phpmyadmin
-- Chọn database: bandienthoai
-- Chọn bảng: voucher
-- Nhấn "Insert" hoặc chạy SQL:

INSERT INTO voucher (
  ma_voucher, ten_voucher, mo_ta,
  loai_giam, gia_tri_giam, giam_toi_da,
  don_toi_thieu, ap_dung_cho,
  so_luong, gioi_han_moi_user,
  ngay_bat_dau, ngay_het_han,
  trang_thai
) VALUES (
  'NEWUSER20',                          -- Mã voucher
  'Giảm 20% cho khách hàng mới',        -- Tên voucher
  'Chỉ dành cho khách hàng đăng ký mới, đơn tối thiểu 500k',
  'percent',                            -- Loại: percent/fixed/freeship
  20,                                   -- Giảm 20%
  200000,                               -- Giảm tối đa 200k
  500000,                               -- Đơn tối thiểu 500k
  'new_user',                           -- all/new_user/old_user/first_order
  100,                                  -- Tổng 100 lượt (NULL = không giới hạn)
  1,                                    -- Mỗi user dùng 1 lần
  '2025-01-01 00:00:00',               -- Ngày bắt đầu
  '2025-12-31 23:59:59',               -- Ngày hết hạn
  1                                     -- 1 = Hoạt động, 0 = Tắt
);
```

#### **Các Ví Dụ Tạo Voucher Khác:**

**1. Voucher giảm giá cố định:**
```sql
INSERT INTO voucher (ma_voucher, ten_voucher, mo_ta, loai_giam, gia_tri_giam, 
                     don_toi_thieu, ap_dung_cho, so_luong, gioi_han_moi_user, 
                     ngay_bat_dau, ngay_het_han, trang_thai)
VALUES ('GIAM100K', 'Giảm 100,000đ', 'Giảm 100k cho đơn từ 1 triệu', 
        'fixed', 100000, 1000000, 'all', 200, 2, 
        '2025-11-01', '2025-11-30', 1);
```

**2. Voucher freeship:**
```sql
INSERT INTO voucher (ma_voucher, ten_voucher, mo_ta, loai_giam, gia_tri_giam, 
                     don_toi_thieu, ap_dung_cho, so_luong, gioi_han_moi_user, 
                     ngay_bat_dau, ngay_het_han, trang_thai)
VALUES ('FREESHIP30K', 'Miễn phí vận chuyển', 'Free ship cho đơn từ 300k', 
        'freeship', 30000, 300000, 'all', NULL, 999, 
        '2025-11-01', '2025-12-31', 1);
```

**3. Voucher cho khách cũ:**
```sql
INSERT INTO voucher (ma_voucher, ten_voucher, mo_ta, loai_giam, gia_tri_giam, 
                     giam_toi_da, don_toi_thieu, ap_dung_cho, so_luong, gioi_han_moi_user, 
                     ngay_bat_dau, ngay_het_han, trang_thai)
VALUES ('LOYAL15', 'Tri ân khách hàng thân thiết', 'Giảm 15% cho khách đã mua hàng', 
        'percent', 15, 300000, 800000, 'old_user', 50, 1, 
        '2025-11-01', '2025-12-31', 1);
```

---

### 📋 **Xem Danh Sách Voucher**

```sql
-- Xem tất cả voucher
SELECT id, ma_voucher, ten_voucher, loai_giam, gia_tri_giam,
       so_luong, da_su_dung, (so_luong - da_su_dung) as con_lai,
       ngay_het_han, trang_thai
FROM voucher
ORDER BY id DESC;

-- Xem voucher đang hoạt động
SELECT * FROM voucher
WHERE trang_thai = 1
  AND ngay_bat_dau <= NOW()
  AND ngay_het_han >= NOW();

-- Xem voucher sắp hết lượt
SELECT ma_voucher, ten_voucher, so_luong, da_su_dung,
       (so_luong - da_su_dung) as con_lai
FROM voucher
WHERE so_luong IS NOT NULL
  AND (so_luong - da_su_dung) < 10
  AND trang_thai = 1;
```

---

### ✏️ **Sửa/Tắt/Xóa Voucher**

```sql
-- TẮT voucher (khuyến nghị thay vì xóa)
UPDATE voucher
SET trang_thai = 0
WHERE ma_voucher = 'NEWUSER20';

-- BẬT lại voucher
UPDATE voucher
SET trang_thai = 1
WHERE ma_voucher = 'NEWUSER20';

-- GIA HẠN voucher
UPDATE voucher
SET ngay_het_han = '2025-12-31 23:59:59'
WHERE ma_voucher = 'NEWUSER20';

-- TĂNG số lượng voucher
UPDATE voucher
SET so_luong = so_luong + 50
WHERE ma_voucher = 'NEWUSER20';

-- SỬA giá trị giảm
UPDATE voucher
SET gia_tri_giam = 25,
    giam_toi_da = 250000
WHERE ma_voucher = 'NEWUSER20';

-- XÓA voucher (không khuyến nghị)
DELETE FROM voucher WHERE ma_voucher = 'NEWUSER20';
```

---

### 📊 **Báo Cáo & Thống Kê Voucher**

#### **1. Xem lịch sử sử dụng voucher:**
```sql
SELECT v.ma_voucher, v.ten_voucher,
       u.username, u.email,
       vu.ma_donhang,
       vu.gia_tri_don_hang,
       vu.gia_tri_giam,
       vu.ngay_su_dung
FROM voucher_usage vu
JOIN voucher v ON vu.voucher_id = v.id
JOIN user u ON vu.user_id = u.id
ORDER BY vu.ngay_su_dung DESC
LIMIT 100;
```

#### **2. Thống kê hiệu quả voucher:**
```sql
SELECT v.ma_voucher, v.ten_voucher,
       COUNT(vu.id) as so_lan_su_dung,
       SUM(vu.gia_tri_giam) as tong_tien_giam,
       AVG(vu.gia_tri_giam) as tb_tien_giam,
       SUM(vu.gia_tri_don_hang) as tong_doanh_thu
FROM voucher v
LEFT JOIN voucher_usage vu ON v.id = vu.voucher_id
GROUP BY v.id
ORDER BY so_lan_su_dung DESC;
```

#### **3. Xem user đã dùng voucher nào:**
```sql
SELECT u.username, u.email,
       v.ma_voucher, v.ten_voucher,
       vu.gia_tri_giam,
       vu.ngay_su_dung
FROM voucher_usage vu
JOIN user u ON vu.user_id = u.id
JOIN voucher v ON vu.voucher_id = v.id
WHERE u.id = 123  -- Thay 123 bằng user_id cần xem
ORDER BY vu.ngay_su_dung DESC;
```

#### **4. Kiểm tra voucher bị lạm dụng:**
```sql
-- Xem user dùng voucher nhiều lần (nghi ngờ gian lận)
SELECT u.username, v.ma_voucher,
       COUNT(*) as so_lan_dung,
       SUM(vu.gia_tri_giam) as tong_tien_giam
FROM voucher_usage vu
JOIN user u ON vu.user_id = u.id
JOIN voucher v ON vu.voucher_id = v.id
GROUP BY u.id, v.id
HAVING so_lan_dung > v.gioi_han_moi_user;
```

---

## 🎯 PHẦN 3: CÁC TRƯỜNG HỢP THỰC TẾ

### **Kịch bản 1: User mới đăng ký**
```
1. User đăng ký tài khoản
2. (Tùy chọn) Hệ thống tự động tặng voucher NEWUSER20
   - Cách làm: Sau khi đăng ký, insert vào bảng user_voucher
   - Hoặc: Hiển thị popup "Bạn nhận được mã NEWUSER20"
3. User vào giỏ hàng, nhập mã NEWUSER20
4. Kiểm tra: User chưa có đơn hàng → Hợp lệ
5. Đơn 1,000,000đ → Giảm 20% = 200,000đ
6. Thanh toán 800,000đ
```

### **Kịch bản 2: User cũ mua lại**
```
1. User đã mua 3 đơn hàng
2. User nhập mã NEWUSER20
3. Kiểm tra: User đã có đơn hàng → BỊ TỪ CHỐI
4. Message: "Mã voucher chỉ dành cho khách hàng mới"
5. Đề xuất: LOYAL15 (dành cho khách cũ)
```

### **Kịch bản 3: Voucher hết lượt**
```
1. Voucher SALE50K: so_luong = 100, da_su_dung = 100
2. User nhập mã SALE50K
3. Kiểm tra: da_su_dung >= so_luong → BỊ TỪ CHỐI
4. Message: "Mã voucher đã hết lượt sử dụng"
```

### **Kịch bản 4: Đơn hàng không đủ điều kiện**
```
1. Voucher GIAM100K: don_toi_thieu = 1,000,000đ
2. Đơn hàng: 500,000đ
3. Kiểm tra: 500,000 < 1,000,000 → BỊ TỪ CHỐI
4. Message: "Đơn hàng tối thiểu 1,000,000đ để áp dụng mã này"
5. Hiển thị: Còn thiếu 500,000đ
```

---

## 🛠️ PHẦN 4: GỢI Ý NÂNG CẤP

### **📱 Tạo Giao Diện Admin Web**

Hiện tại admin phải dùng PHPMyAdmin, bạn nên tạo:

```
📁 Server/admin/
  ├─ login_admin.php          → Đăng nhập admin
  ├─ voucher_list.php         → Danh sách voucher
  ├─ voucher_create.php       → Tạo voucher mới
  ├─ voucher_edit.php         → Sửa voucher
  ├─ voucher_delete.php       → Xóa/tắt voucher
  ├─ voucher_statistics.php   → Thống kê hiệu quả
  └─ voucher_usage_history.php → Lịch sử sử dụng
```

### **🚀 Tính Năng Nâng Cao**

1. **Push Notification khi có voucher mới**
2. **Tự động tặng voucher sinh nhật**
3. **Voucher theo level (VIP, Gold, Silver)**
4. **Voucher combo (mua 2 tặng voucher)**
5. **Voucher giới hạn theo sản phẩm**
6. **Voucher cashback (hoàn tiền)**

---

## 📞 HỖ TRỢ

Nếu cần:
- Tạo giao diện Admin quản lý voucher
- Tích hợp tính năng tự động tặng voucher
- Tạo màn hình "Voucher của tôi" trong app
- Báo cáo thống kê chi tiết

Hãy cho tôi biết! 🎯

