# 🗄️ SƠ ĐỒ KẾT NỐI DATABASE - HỆ THỐNG VOUCHER

**Ngày:** 03/11/2025  
**Database:** appbandongho

---

## 📊 SƠ ĐỒ QUAN HỆ CÁC BẢNG

```
┌─────────────────────────────────────────────────────────────────┐
│                    QUAN HỆ GIỮA CÁC BẢNG                         │
└─────────────────────────────────────────────────────────────────┘

        ┌──────────────┐
        │    user      │
        │──────────────│
        │ id (PK)      │◄─────────┐
        │ email        │          │
        │ username     │          │
        │ mobile       │          │
        │ role         │          │
        └──────────────┘          │
               ▲                  │
               │ iduser           │ user_id
               │                  │
        ┌──────────────┐   ┌──────────────────┐
        │  donhang     │   │ voucher_usage    │
        │──────────────│   │──────────────────│
        │ id (PK)      │◄──│ donhang_id       │
        │ iduser (FK)  │   │ user_id (FK)     │
        │ madonhang    │──►│ ma_donhang       │
        │ diachi       │   │ voucher_id (FK)  │◄──┐
        │ sodienthoai  │   │ gia_tri_don_hang │   │
        │ tongtien     │   │ gia_tri_giam     │   │
        │──────────────│   │ ngay_su_dung     │   │
        │ voucher_id   │◄──┘                      │
        │ ma_voucher   │                          │
        │ gia_tri_giam │   ┌──────────────────┐   │
        │ tong_truoc_  │   │    voucher       │   │
        │   giam       │   │──────────────────│   │
        └──────────────┘   │ id (PK)          │───┘
               │           │ ma_voucher       │
               │           │ ten_voucher      │
               │           │ loai_giam        │
        ┌──────▼─────┐    │ gia_tri_giam     │
        │chitietdonhang│   │ don_toi_thieu    │
        │──────────────│   │ so_luong         │
        │ iddonhang(FK)│   │ da_su_dung       │
        │ idsp (FK)    │   │ ngay_bat_dau     │
        │ soluong      │   │ ngay_het_han     │
        │ gia          │   │ trang_thai       │
        └──────────────┘   └──────────────────┘
               │
               │ idsp
               ▼
        ┌──────────────┐
        │ sanphammoi   │
        │──────────────│
        │ id (PK)      │
        │ tensp        │
        │ giasp        │
        │ hinhanh      │
        │ soluongtonkho│
        └──────────────┘
```

---

## 🔗 CHI TIẾT KẾT NỐI

### **1. BẢNG `voucher` (Mã giảm giá)**
```sql
Bảng chính lưu thông tin voucher
├─ PRIMARY KEY: id
├─ UNIQUE KEY: ma_voucher
└─ Không có FK (bảng độc lập)

Kết nối:
├─> voucher_usage.voucher_id (1-N)
└─> donhang.voucher_id (1-N)
```

### **2. BẢNG `voucher_usage` (Lịch sử sử dụng)**
```sql
Lưu lịch sử ai đã dùng voucher nào, cho đơn hàng nào
├─ PRIMARY KEY: id
├─ FOREIGN KEY: voucher_id → voucher.id
├─ FOREIGN KEY: user_id → user.id (đã bỏ để tránh lỗi)
└─ INDEX: donhang_id (không ràng buộc FK)

Kết nối:
user (1) ──< voucher_usage (N)
voucher (1) ──< voucher_usage (N)
donhang (1) ──< voucher_usage (N)
```

### **3. BẢNG `donhang` (Đơn hàng - CẬP NHẬT)**
```sql
Các cột MỚI được thêm:
├─ voucher_id INT(11)         → ID voucher đã dùng
├─ ma_voucher VARCHAR(50)     → Mã voucher (backup)
├─ gia_tri_giam DECIMAL(10,2) → Số tiền đã giảm
└─ tong_truoc_giam DECIMAL(10,2) → Tổng tiền gốc

Kết nối:
donhang.voucher_id → voucher.id (N-1)
donhang.iduser → user.id (N-1)
```

---

## 🔄 LUỒNG DỮ LIỆU KHI ĐẶT HÀNG VỚI VOUCHER

```
BƯỚC 1: User chọn voucher "NEWUSER20"
        ↓
BƯỚC 2: Kiểm tra trong bảng `voucher`
        ├─ SELECT * FROM voucher WHERE ma_voucher='NEWUSER20'
        └─ Kiểm tra: còn hạn? còn số lượng? user đủ điều kiện?
        ↓
BƯỚC 3: Tính toán
        ├─ Tổng trước giảm: 1,500,000đ
        ├─ Giảm 20% = 300,000đ (nhưng max 200k)
        └─ Tổng sau giảm: 1,300,000đ
        ↓
BƯỚC 4: Tạo đơn hàng
        INSERT INTO donhang (
            iduser, madonhang, tongtien,
            voucher_id, ma_voucher, 
            gia_tri_giam, tong_truoc_giam
        ) VALUES (
            5, 'DH1730624589', 1300000,
            1, 'NEWUSER20',
            200000, 1500000
        )
        ↓
BƯỚC 5: Trigger tự động chạy (after_donhang_insert_update_voucher)
        UPDATE voucher 
        SET da_su_dung = da_su_dung + 1 
        WHERE id = 1
        ↓
BƯỚC 6: Lưu lịch sử
        INSERT INTO voucher_usage (
            voucher_id, user_id, donhang_id, ma_donhang,
            gia_tri_don_hang, gia_tri_giam
        ) VALUES (
            1, 5, 123, 'DH1730624589',
            1500000, 200000
        )
```

---

## 📝 CÁC QUERY QUAN TRỌNG

### **Query 1: Kiểm tra voucher hợp lệ**
```sql
SELECT v.* 
FROM voucher v
WHERE v.ma_voucher = 'NEWUSER20'
  AND v.trang_thai = 1
  AND v.ngay_bat_dau <= NOW()
  AND v.ngay_het_han >= NOW()
  AND (v.so_luong IS NULL OR v.da_su_dung < v.so_luong)
```

### **Query 2: Kiểm tra user đã dùng voucher bao nhiêu lần**
```sql
SELECT COUNT(*) as count
FROM voucher_usage
WHERE voucher_id = 1 
  AND user_id = 5
```

### **Query 3: Kiểm tra user mới hay cũ**
```sql
SELECT COUNT(*) as order_count
FROM donhang
WHERE iduser = 5
-- Nếu = 0 → user mới
-- Nếu > 0 → user cũ
```

### **Query 4: Lấy đơn hàng với thông tin voucher**
```sql
SELECT 
    d.*,
    v.ten_voucher,
    v.loai_giam,
    u.username
FROM donhang d
LEFT JOIN voucher v ON d.voucher_id = v.id
LEFT JOIN user u ON d.iduser = u.id
WHERE d.iduser = 5
ORDER BY d.id DESC
```

### **Query 5: Thống kê voucher đã dùng**
```sql
SELECT 
    v.ma_voucher,
    v.ten_voucher,
    v.da_su_dung,
    COUNT(DISTINCT vu.user_id) as so_user_da_dung,
    SUM(vu.gia_tri_giam) as tong_tien_da_giam
FROM voucher v
LEFT JOIN voucher_usage vu ON v.id = vu.voucher_id
GROUP BY v.id
ORDER BY v.da_su_dung DESC
```

---

## ⚙️ TRIGGER VÀ STORED PROCEDURE

### **Trigger 1: Tự động tăng số lượng đã sử dụng**
```sql
DELIMITER $$
CREATE TRIGGER after_donhang_insert_update_voucher
AFTER INSERT ON donhang
FOR EACH ROW
BEGIN
    IF NEW.voucher_id IS NOT NULL THEN
        UPDATE voucher 
        SET da_su_dung = da_su_dung + 1 
        WHERE id = NEW.voucher_id;
    END IF;
END$$
DELIMITER ;
```

### **View: Thống kê voucher**
```sql
CREATE OR REPLACE VIEW voucher_statistics AS
SELECT 
    v.*,
    COUNT(DISTINCT vu.user_id) as so_user_da_dung,
    SUM(vu.gia_tri_giam) as tong_tien_da_giam,
    CASE 
        WHEN v.trang_thai = 0 THEN 'Vô hiệu hóa'
        WHEN NOW() > v.ngay_het_han THEN 'Hết hạn'
        WHEN v.so_luong IS NOT NULL AND v.da_su_dung >= v.so_luong THEN 'Đã hết'
        ELSE 'Đang hoạt động'
    END as trang_thai_hien_tai
FROM voucher v
LEFT JOIN voucher_usage vu ON v.id = vu.voucher_id
GROUP BY v.id;
```

---

## 🚀 HƯỚNG DẪN CHẠY FILE SQL

### **Bước 1: Backup database hiện tại**
```bash
# Vào phpMyAdmin → Export → Chọn database → Export
```

### **Bước 2: Import file SQL**
```bash
# Cách 1: Qua phpMyAdmin
1. Vào phpMyAdmin
2. Chọn database "appbandongho"
3. Tab "Import"
4. Chọn file: create_voucher_system.sql
5. Nhấn "Go"

# Cách 2: Qua MySQL Command Line
mysql -u root -p appbandongho < create_voucher_system.sql
```

### **Bước 3: Kiểm tra kết quả**
```sql
-- Kiểm tra bảng đã tạo
SHOW TABLES LIKE 'voucher%';

-- Kiểm tra cột mới trong donhang
DESCRIBE donhang;

-- Kiểm tra dữ liệu mẫu
SELECT * FROM voucher;

-- Kiểm tra trigger
SHOW TRIGGERS WHERE `Table` = 'donhang';

-- Kiểm tra view
SELECT * FROM voucher_statistics;
```

---

## ✅ CHECKLIST SAU KHI CHẠY SQL

- [ ] Bảng `voucher` đã tạo (7 mã mẫu)
- [ ] Bảng `voucher_usage` đã tạo
- [ ] Bảng `donhang` có 4 cột mới:
  - [ ] voucher_id
  - [ ] ma_voucher
  - [ ] gia_tri_giam
  - [ ] tong_truoc_giam
- [ ] Trigger `after_donhang_insert_update_voucher` đã tạo
- [ ] View `voucher_statistics` đã tạo

---

## 🔧 TROUBLESHOOTING

### Lỗi 1: "Duplicate column name"
```
Nguyên nhân: Cột đã tồn tại trong bảng donhang
Giải pháp: File SQL đã được sửa để kiểm tra tự động
           Chạy lại file SQL là OK
```

### Lỗi 2: "Cannot add foreign key constraint"
```
Nguyên nhân: Bảng user chưa có hoặc cấu trúc khác
Giải pháp: Đã bỏ FOREIGN KEY trong CREATE TABLE
           Có thể thêm sau bằng ALTER TABLE nếu cần
```

### Lỗi 3: "Table 'voucher' already exists"
```
Nguyên nhân: Đã chạy file SQL trước đó
Giải pháp: DROP TABLE voucher, voucher_usage trước
           Hoặc bỏ qua lỗi này (dùng IF NOT EXISTS)
```

---

## 📞 CONTACT & SUPPORT

Nếu gặp vấn đề khi chạy SQL:
1. Kiểm tra phiên bản MySQL/MariaDB
2. Kiểm tra quyền user database
3. Xem log lỗi chi tiết
4. Chạy từng đoạn SQL riêng lẻ để tìm lỗi

---

**File liên quan:**
- `create_voucher_system.sql` - File SQL tạo database
- `HUONG_DAN_VOUCHER_SYSTEM.md` - Hướng dẫn logic voucher
- `Server/checkVoucher.php` - API kiểm tra voucher
- `Server/getVouchers.php` - API lấy danh sách voucher

