# 🔧 HƯỚNG DẪN SỬA LỖI THIẾU FOREIGN KEY - HỆ THỐNG VOUCHER

## ❌ VẤN ĐỀ PHÁT HIỆN

Bảng `voucher` và `voucher_usage` **KHÔNG CÓ** ràng buộc khóa ngoại (Foreign Key) với các bảng khác:
- `voucher_usage.voucher_id` → không liên kết `voucher.id`
- `voucher_usage.user_id` → không liên kết `user.id`
- `voucher_usage.donhang_id` → không liên kết `donhang.id`
- `donhang.voucher_id` → không liên kết `voucher.id`

### Hậu quả:
1. ⚠️ Mất tính toàn vẹn dữ liệu (data integrity)
2. ⚠️ Có thể insert ID không tồn tại
3. ⚠️ Xóa voucher nhưng vẫn còn tham chiếu trong donhang
4. ⚠️ Báo cáo thống kê sai

---

## ✅ GIẢI PHÁP

### Bước 1: Backup Database
```sql
mysqldump -u root -p appbandongho > backup_before_foreign_key.sql
```

### Bước 2: Kiểm tra dữ liệu hiện có
```sql
-- Kiểm tra voucher_usage có voucher_id không tồn tại
SELECT vu.* 
FROM voucher_usage vu
LEFT JOIN voucher v ON vu.voucher_id = v.id
WHERE v.id IS NULL;

-- Kiểm tra voucher_usage có user_id không tồn tại
SELECT vu.* 
FROM voucher_usage vu
LEFT JOIN user u ON vu.user_id = u.id
WHERE u.id IS NULL;

-- Kiểm tra donhang có voucher_id không tồn tại
SELECT d.* 
FROM donhang d
LEFT JOIN voucher v ON d.voucher_id = v.id
WHERE d.voucher_id IS NOT NULL AND v.id IS NULL;
```

### Bước 3: Xóa dữ liệu rác (nếu có)
```sql
-- Xóa voucher_usage có voucher_id không tồn tại
DELETE vu FROM voucher_usage vu
LEFT JOIN voucher v ON vu.voucher_id = v.id
WHERE v.id IS NULL;

-- Xóa voucher_usage có user_id không tồn tại
DELETE vu FROM voucher_usage vu
LEFT JOIN user u ON vu.user_id = u.id
WHERE u.id IS NULL;

-- SET NULL cho donhang.voucher_id không tồn tại
UPDATE donhang d
LEFT JOIN voucher v ON d.voucher_id = v.id
SET d.voucher_id = NULL
WHERE d.voucher_id IS NOT NULL AND v.id IS NULL;
```

### Bước 4: Chạy file SQL thêm Foreign Keys
```bash
# Mở phpMyAdmin hoặc MySQL Workbench
# Import file: add_voucher_foreign_keys.sql
```

Hoặc dùng command line:
```bash
mysql -u root -p appbandongho < add_voucher_foreign_keys.sql
```

### Bước 5: Kiểm tra kết quả
```sql
-- Xem tất cả foreign keys đã tạo
SELECT 
    CONSTRAINT_NAME AS 'Tên ràng buộc',
    TABLE_NAME AS 'Bảng',
    COLUMN_NAME AS 'Cột',
    REFERENCED_TABLE_NAME AS 'Bảng tham chiếu',
    REFERENCED_COLUMN_NAME AS 'Cột tham chiếu'
FROM information_schema.KEY_COLUMN_USAGE
WHERE CONSTRAINT_SCHEMA = 'appbandongho'
AND TABLE_NAME IN ('voucher_usage', 'donhang')
AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, CONSTRAINT_NAME;
```

Kết quả mong đợi:
```
+----------------------------------+----------------+-------------+---------------------+------------------------+
| Tên ràng buộc                    | Bảng           | Cột         | Bảng tham chiếu     | Cột tham chiếu         |
+----------------------------------+----------------+-------------+---------------------+------------------------+
| fk_donhang_user                  | donhang        | iduser      | user                | id                     |
| fk_donhang_voucher               | donhang        | voucher_id  | voucher             | id                     |
| fk_voucher_usage_donhang         | voucher_usage  | donhang_id  | donhang             | id                     |
| fk_voucher_usage_user            | voucher_usage  | user_id     | user                | id                     |
| fk_voucher_usage_voucher         | voucher_usage  | voucher_id  | voucher             | id                     |
+----------------------------------+----------------+-------------+---------------------+------------------------+
```

---

## 📊 CASCADE ACTIONS - GIẢI THÍCH

### 1. **ON DELETE CASCADE** (Xóa theo chuỗi)
```
Xóa voucher (id=1) 
   ↓
Tự động xóa tất cả voucher_usage có voucher_id=1
```

**Áp dụng:**
- `voucher_usage.voucher_id` → `voucher.id`
- `voucher_usage.user_id` → `user.id`
- `donhang.iduser` → `user.id`

### 2. **ON DELETE SET NULL** (Đặt NULL, giữ lịch sử)
```
Xóa voucher (id=1)
   ↓
Đặt donhang.voucher_id = NULL (giữ đơn hàng)
```

**Áp dụng:**
- `donhang.voucher_id` → `voucher.id`
- `voucher_usage.donhang_id` → `donhang.id`

### 3. **ON UPDATE CASCADE** (Cập nhật theo)
```
Đổi voucher.id từ 5 → 10
   ↓
Tự động update voucher_usage.voucher_id = 10
```

---

## 🧪 TEST FOREIGN KEY

### Test 1: Thử xóa voucher
```sql
-- Tạo voucher test
INSERT INTO voucher (ma_voucher, ten_voucher, loai_giam, gia_tri_giam, 
    ngay_bat_dau, ngay_het_han, trang_thai)
VALUES ('TEST123', 'Test FK', 'percent', 10, 
    '2025-01-01', '2025-12-31', 1);

SET @test_voucher_id = LAST_INSERT_ID();

-- Tạo voucher_usage liên kết
INSERT INTO voucher_usage (voucher_id, user_id, gia_tri_don_hang, gia_tri_giam)
VALUES (@test_voucher_id, 1, 500000, 50000);

-- Xóa voucher → voucher_usage phải tự động xóa
DELETE FROM voucher WHERE id = @test_voucher_id;

-- Kiểm tra: voucher_usage phải không còn
SELECT * FROM voucher_usage WHERE voucher_id = @test_voucher_id;
-- Kết quả: Empty set (0 rows)
```

### Test 2: Thử insert voucher_id không tồn tại
```sql
-- Trước khi có FK: Insert thành công ✅
-- Sau khi có FK: Báo lỗi ❌
INSERT INTO voucher_usage (voucher_id, user_id, gia_tri_don_hang, gia_tri_giam)
VALUES (99999, 1, 100000, 10000);

-- Lỗi mong đợi:
-- ERROR 1452: Cannot add or update a child row: 
-- a foreign key constraint fails (`voucher_usage`, 
-- CONSTRAINT `fk_voucher_usage_voucher`)
```

---

## 🎯 LỢI ÍCH SAU KHI CÓ FOREIGN KEY

### 1. **Tính toàn vẹn dữ liệu**
✅ Không thể insert `voucher_id` không tồn tại  
✅ Không thể xóa voucher nếu đang được sử dụng (nếu dùng RESTRICT)  
✅ Xóa user → tự động dọn dẹp voucher_usage và donhang

### 2. **Tự động dọn dẹp**
✅ Xóa voucher → xóa lịch sử sử dụng  
✅ Xóa user → xóa tất cả dữ liệu liên quan  
✅ Không còn dữ liệu "rác"

### 3. **Query nhanh hơn**
✅ Indexes tự động được tạo  
✅ JOIN query tối ưu hơn

### 4. **Báo cáo chính xác**
✅ View tự động cập nhật đúng  
✅ Thống kê không bị sai lệch

---

## 📁 CẤU TRÚC DATABASE SAU KHI FIX

```
┌─────────────┐
│   voucher   │
│  (id PK)    │
└──────┬──────┘
       │
       │ FK: voucher_id
       │ ON DELETE CASCADE
       │
       ├──────────────────────┐
       │                      │
       ▼                      ▼
┌─────────────────┐    ┌──────────────┐
│ voucher_usage   │    │   donhang    │
│ (voucher_id FK) │    │(voucher_id FK│
│ (user_id FK)    │◄───│ ON DELETE    │
│ (donhang_id FK) │    │ SET NULL)    │
└─────────────────┘    └──────────────┘
       │                      │
       │ FK: user_id          │ FK: iduser
       │ CASCADE              │ CASCADE
       │                      │
       ▼                      ▼
    ┌──────────┐
    │   user   │
    │ (id PK)  │
    └──────────┘
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### Khi nào KHÔNG nên dùng CASCADE DELETE?

Nếu bạn muốn **giữ lịch sử** khi xóa voucher, đổi thành:

```sql
-- Thay vì CASCADE
ALTER TABLE `voucher_usage`
ADD CONSTRAINT `fk_voucher_usage_voucher`
FOREIGN KEY (`voucher_id`) 
REFERENCES `voucher`(`id`)
ON DELETE SET NULL  -- Đặt NULL thay vì xóa
ON UPDATE CASCADE;
```

Hoặc dùng **RESTRICT** để ngăn xóa:
```sql
ON DELETE RESTRICT  -- Không cho xóa voucher nếu đã được dùng
```

### Soft Delete thay vì Hard Delete

Thay vì xóa voucher, có thể:
```sql
-- Vô hiệu hóa
UPDATE voucher SET trang_thai = 0 WHERE id = 1;

-- Hoặc thêm cột deleted_at
ALTER TABLE voucher ADD COLUMN deleted_at DATETIME DEFAULT NULL;
UPDATE voucher SET deleted_at = NOW() WHERE id = 1;
```

---

## 📞 TROUBLESHOOTING

### Lỗi: "Cannot add foreign key constraint"

**Nguyên nhân:**
- Có dữ liệu không hợp lệ (voucher_id không tồn tại)
- Kiểu dữ liệu không khớp (INT vs BIGINT)
- Collation không khớp

**Giải pháp:**
```sql
-- 1. Kiểm tra dữ liệu rác
SELECT vu.voucher_id, COUNT(*) 
FROM voucher_usage vu
LEFT JOIN voucher v ON vu.voucher_id = v.id
WHERE v.id IS NULL
GROUP BY vu.voucher_id;

-- 2. Xóa dữ liệu rác
DELETE FROM voucher_usage WHERE voucher_id NOT IN (SELECT id FROM voucher);

-- 3. Kiểm tra kiểu dữ liệu
DESCRIBE voucher_usage;
DESCRIBE voucher;

-- 4. Thử lại
SOURCE add_voucher_foreign_keys.sql;
```

---

## ✅ CHECKLIST

- [ ] Backup database
- [ ] Kiểm tra dữ liệu rác
- [ ] Xóa dữ liệu không hợp lệ
- [ ] Chạy `add_voucher_foreign_keys.sql`
- [ ] Kiểm tra foreign keys đã tạo
- [ ] Test cascade delete
- [ ] Test insert ID không tồn tại (phải báo lỗi)
- [ ] Test các views mới
- [ ] Update tài liệu dự án

---

**Tác giả:** GitHub Copilot  
**Ngày tạo:** 3/11/2025  
**Phiên bản:** 1.0

