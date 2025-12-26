-- ================================================
-- TẠO HỆ THỐNG VOUCHER/MÃ GIẢM GIÁ HOÀN CHỈNH
-- ================================================

-- 1. Bảng voucher - lưu thông tin mã giảm giá
CREATE TABLE IF NOT EXISTS `voucher` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ma_voucher` varchar(50) NOT NULL UNIQUE COMMENT 'Mã voucher (VD: NEWUSER10, FREESHIP50K)',
  `ten_voucher` varchar(200) NOT NULL COMMENT 'Tên voucher hiển thị',
  `mo_ta` text DEFAULT NULL COMMENT 'Mô tả chi tiết voucher',

  -- Loại giảm giá
  `loai_giam` enum('percent','fixed','freeship') NOT NULL DEFAULT 'percent' COMMENT 'percent=%, fixed=số tiền, freeship=miễn phí ship',
  `gia_tri_giam` decimal(10,2) NOT NULL COMMENT 'Giá trị giảm (nếu %, nhập 10 = 10%)',
  `giam_toi_da` decimal(10,2) DEFAULT NULL COMMENT 'Số tiền giảm tối đa (với loại %)',

  -- Điều kiện áp dụng
  `don_toi_thieu` decimal(10,2) DEFAULT 0 COMMENT 'Giá trị đơn hàng tối thiểu',
  `ap_dung_cho` enum('all','new_user','old_user','first_order') DEFAULT 'all' COMMENT 'Áp dụng cho đối tượng',

  -- Giới hạn sử dụng
  `so_luong` int(11) DEFAULT NULL COMMENT 'Số lượng voucher (NULL = không giới hạn)',
  `da_su_dung` int(11) DEFAULT 0 COMMENT 'Số lượng đã sử dụng',
  `gioi_han_moi_user` int(11) DEFAULT 1 COMMENT 'Mỗi user được dùng bao nhiêu lần',

  -- Thời gian
  `ngay_bat_dau` datetime NOT NULL,
  `ngay_het_han` datetime NOT NULL,

  -- Trạng thái
  `trang_thai` tinyint(1) DEFAULT 1 COMMENT '1=active, 0=inactive',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),

  PRIMARY KEY (`id`),
  KEY `ma_voucher` (`ma_voucher`),
  KEY `trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bảng quản lý mã giảm giá/voucher';

-- 2. Bảng lịch sử sử dụng voucher
CREATE TABLE IF NOT EXISTS `voucher_usage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `voucher_id` int(11) NOT NULL COMMENT 'ID voucher',
  `user_id` int(11) NOT NULL COMMENT 'ID người dùng',
  `donhang_id` int(11) DEFAULT NULL COMMENT 'ID đơn hàng (sau khi đặt hàng)',
  `ma_donhang` varchar(50) DEFAULT NULL COMMENT 'Mã đơn hàng',
  `gia_tri_don_hang` decimal(10,2) NOT NULL COMMENT 'Giá trị đơn hàng trước giảm',
  `gia_tri_giam` decimal(10,2) NOT NULL COMMENT 'Số tiền đã giảm',
  `ngay_su_dung` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `voucher_id` (`voucher_id`),
  KEY `user_id` (`user_id`),
  KEY `donhang_id` (`donhang_id`)
  -- Note: FOREIGN KEY sẽ được thêm sau nếu cần, tránh lỗi nếu bảng user chưa có
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử sử dụng voucher';

-- Thêm FOREIGN KEY sau khi tạo bảng (an toàn hơn)
-- 3. Thêm cột voucher vào bảng donhang (nếu chưa có)
-- Kiểm tra và thêm từng cột riêng biệt để tránh lỗi duplicate column

-- Thêm cột voucher_id nếu chưa có
SET @dbname = DATABASE();
SET @tablename = 'donhang';
SET @columnname = 'voucher_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename)
   AND (table_schema = @dbname)
   AND (column_name = @columnname)) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " int(11) DEFAULT NULL COMMENT 'ID voucher đã dùng' AFTER iduser")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Thêm cột ma_voucher nếu chưa có
SET @columnname = 'ma_voucher';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename)
   AND (table_schema = @dbname)
   AND (column_name = @columnname)) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " varchar(50) DEFAULT NULL COMMENT 'Mã voucher đã dùng' AFTER voucher_id")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Thêm cột gia_tri_giam nếu chưa có
SET @columnname = 'gia_tri_giam';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename)
   AND (table_schema = @dbname)
   AND (column_name = @columnname)) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " decimal(10,2) DEFAULT 0 COMMENT 'Số tiền giảm từ voucher' AFTER ma_voucher")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Thêm cột tong_truoc_giam nếu chưa có
SET @columnname = 'tong_truoc_giam';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename)
   AND (table_schema = @dbname)
   AND (column_name = @columnname)) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " decimal(10,2) DEFAULT NULL COMMENT 'Tổng tiền trước khi giảm' AFTER gia_tri_giam")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
ADD COLUMN `voucher_id` int(11) DEFAULT NULL COMMENT 'ID voucher đã dùng' AFTER `iduser`,
ADD COLUMN `ma_voucher` varchar(50) DEFAULT NULL COMMENT 'Mã voucher đã dùng' AFTER `voucher_id`,
ADD COLUMN `gia_tri_giam` decimal(10,2) DEFAULT 0 COMMENT 'Số tiền giảm từ voucher' AFTER `ma_voucher`,
ADD COLUMN `tong_truoc_giam` decimal(10,2) DEFAULT NULL COMMENT 'Tổng tiền trước khi giảm' AFTER `gia_tri_giam`,
ADD KEY `voucher_id` (`voucher_id`);

-- 4. INSERT DỮ LIỆU MẪU - CÁC MÃ GIẢM GIÁ PHỔ BIẾN
INSERT INTO `voucher` (`ma_voucher`, `ten_voucher`, `mo_ta`, `loai_giam`, `gia_tri_giam`, `giam_toi_da`, `don_toi_thieu`, `ap_dung_cho`, `so_luong`, `gioi_han_moi_user`, `ngay_bat_dau`, `ngay_het_han`, `trang_thai`) VALUES

-- Voucher cho khách hàng mới
('NEWUSER20', 'Giảm 20% cho khách hàng mới', 'Chào mừng khách hàng mới - Giảm ngay 20% cho đơn hàng đầu tiên', 'percent', 20.00, 200000.00, 500000.00, 'new_user', 100, 1, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1),

-- Voucher đơn hàng đầu tiên
('FIRSTORDER', 'Giảm 100k đơn đầu tiên', 'Giảm 100,000đ cho đơn hàng đầu tiên từ 1 triệu', 'fixed', 100000.00, NULL, 1000000.00, 'first_order', NULL, 1, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1),

-- Voucher freeship
('FREESHIP50K', 'Miễn phí vận chuyển', 'Miễn phí ship cho đơn từ 500k', 'freeship', 50000.00, NULL, 500000.00, 'all', NULL, 3, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1),

-- Voucher giảm % có giới hạn
('SALE15', 'Giảm 15% tối đa 150k', 'Giảm 15% cho đơn hàng từ 800k, tối đa 150k', 'percent', 15.00, 150000.00, 800000.00, 'all', 200, 2, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1),

-- Voucher giảm tiền cố định
('GIAM200K', 'Giảm 200k cho đơn 2 triệu', 'Giảm ngay 200,000đ cho đơn hàng từ 2 triệu', 'fixed', 200000.00, NULL, 2000000.00, 'all', 50, 1, '2025-01-01 00:00:00', '2025-03-31 23:59:59', 1),

-- Voucher khách hàng cũ (tri ân)
('OLDUSER10', 'Tri ân khách hàng cũ 10%', 'Giảm 10% cho khách hàng thân thiết', 'percent', 10.00, 100000.00, 300000.00, 'old_user', NULL, 5, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1),

-- Voucher flash sale
('FLASH30', 'Flash Sale 30%', 'Giảm sốc 30% trong thời gian giới hạn', 'percent', 30.00, 300000.00, 1000000.00, 'all', 30, 1, '2025-11-01 00:00:00', '2025-11-10 23:59:59', 1);

-- 5. Tạo trigger tự động tăng số lượng đã sử dụng khi đặt hàng
DELIMITER $$
CREATE TRIGGER `after_donhang_insert_update_voucher`
AFTER INSERT ON `donhang`
FOR EACH ROW
BEGIN
    -- Nếu đơn hàng có dùng voucher, tăng số lượng đã sử dụng
    IF NEW.voucher_id IS NOT NULL THEN
        UPDATE `voucher`
        SET `da_su_dung` = `da_su_dung` + 1
        WHERE `id` = NEW.voucher_id;
    END IF;
END$$
DELIMITER ;

-- 6. View để admin xem thống kê voucher
CREATE OR REPLACE VIEW `voucher_statistics` AS
SELECT
    v.id,
    v.ma_voucher,
    v.ten_voucher,
    v.loai_giam,
    v.gia_tri_giam,
    v.don_toi_thieu,
    v.so_luong,
    v.da_su_dung,
    CASE
        WHEN v.so_luong IS NULL THEN 'Không giới hạn'
        ELSE CONCAT(v.da_su_dung, '/', v.so_luong)
    END as ti_le_su_dung,
    v.ngay_bat_dau,
    v.ngay_het_han,
    CASE
        WHEN v.trang_thai = 0 THEN 'Vô hiệu hóa'
        WHEN NOW() < v.ngay_bat_dau THEN 'Chưa bắt đầu'
        WHEN NOW() > v.ngay_het_han THEN 'Hết hạn'
        WHEN v.so_luong IS NOT NULL AND v.da_su_dung >= v.so_luong THEN 'Đã hết'
        ELSE 'Đang hoạt động'
    END as trang_thai_hien_tai,
    COUNT(DISTINCT vu.user_id) as so_luong_user_da_dung,
    COALESCE(SUM(vu.gia_tri_giam), 0) as tong_tien_da_giam
FROM `voucher` v
LEFT JOIN `voucher_usage` vu ON v.id = vu.voucher_id
GROUP BY v.id;

-- ================================================
-- HƯỚNG DẪN SỬ DỤNG
-- ================================================
-- 1. Chạy file SQL này để tạo bảng và dữ liệu mẫu
-- 2. Tạo các file PHP API (sẽ tạo tiếp theo)
-- 3. Tạo model và activity trong Android
-- 4. Test các mã voucher mẫu:
--    - NEWUSER20: Dành cho user mới
--    - FIRSTORDER: Đơn hàng đầu tiên
--    - FREESHIP50K: Miễn phí ship
--    - SALE15: Giảm 15%
-- ================================================

