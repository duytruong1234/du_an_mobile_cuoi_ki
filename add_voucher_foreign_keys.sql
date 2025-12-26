-- ================================================
-- THÊM FOREIGN KEY CHO HỆ THỐNG VOUCHER
-- ================================================
-- File này thêm các ràng buộc khóa ngoại để đảm bảo tính toàn vẹn dữ liệu
-- Chạy sau khi đã có đầy đủ các bảng: user, voucher, donhang, voucher_usage

-- ================================================
-- 1. THÊM FOREIGN KEY CHO BẢNG voucher_usage
-- ================================================

-- 1.1. Liên kết với bảng voucher
-- Khi xóa voucher, các bản ghi sử dụng sẽ bị xóa theo (CASCADE)
-- hoặc SET NULL nếu muốn giữ lịch sử
ALTER TABLE `voucher_usage`
ADD CONSTRAINT `fk_voucher_usage_voucher`
FOREIGN KEY (`voucher_id`)
REFERENCES `voucher`(`id`)
ON DELETE CASCADE
ON UPDATE CASCADE;

-- 1.2. Liên kết với bảng user
-- Khi xóa user, các bản ghi sử dụng voucher của user đó cũng bị xóa
ALTER TABLE `voucher_usage`
ADD CONSTRAINT `fk_voucher_usage_user`
FOREIGN KEY (`user_id`)
REFERENCES `user`(`id`)
ON DELETE CASCADE
ON UPDATE CASCADE;

-- 1.3. Liên kết với bảng donhang
-- Nếu đơn hàng bị xóa, SET NULL cho donhang_id (giữ lịch sử)
ALTER TABLE `voucher_usage`
ADD CONSTRAINT `fk_voucher_usage_donhang`
FOREIGN KEY (`donhang_id`)
REFERENCES `donhang`(`id`)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- ================================================
-- 2. THÊM FOREIGN KEY CHO BẢNG donhang
-- ================================================

-- 2.1. Liên kết với bảng voucher
-- Nếu voucher bị xóa, SET NULL cho voucher_id (giữ thông tin đơn hàng)
ALTER TABLE `donhang`
ADD CONSTRAINT `fk_donhang_voucher`
FOREIGN KEY (`voucher_id`)
REFERENCES `voucher`(`id`)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- 2.2. Liên kết với bảng user (nếu chưa có)
-- Kiểm tra xem constraint đã tồn tại chưa
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'donhang'
    AND CONSTRAINT_NAME = 'fk_donhang_user'
);

-- Nếu chưa có, thêm foreign key cho user
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE `donhang`
     ADD CONSTRAINT `fk_donhang_user`
     FOREIGN KEY (`iduser`)
     REFERENCES `user`(`id`)
     ON DELETE CASCADE
     ON UPDATE CASCADE',
    'SELECT "Foreign key fk_donhang_user already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ================================================
-- 3. TẠO INDEXES ĐỂ TỐI ƯU PERFORMANCE
-- ================================================

-- Indexes cho voucher_usage (nếu chưa có)
CREATE INDEX IF NOT EXISTS `idx_voucher_usage_voucher_id` ON `voucher_usage`(`voucher_id`);
CREATE INDEX IF NOT EXISTS `idx_voucher_usage_user_id` ON `voucher_usage`(`user_id`);
CREATE INDEX IF NOT EXISTS `idx_voucher_usage_donhang_id` ON `voucher_usage`(`donhang_id`);
CREATE INDEX IF NOT EXISTS `idx_voucher_usage_ngay_su_dung` ON `voucher_usage`(`ngay_su_dung`);

-- Indexes cho donhang (nếu chưa có)
CREATE INDEX IF NOT EXISTS `idx_donhang_voucher_id` ON `donhang`(`voucher_id`);
CREATE INDEX IF NOT EXISTS `idx_donhang_iduser` ON `donhang`(`iduser`);
CREATE INDEX IF NOT EXISTS `idx_donhang_ngaytao` ON `donhang`(`ngaygiaodukien`);

-- Indexes cho voucher
CREATE INDEX IF NOT EXISTS `idx_voucher_ma_voucher` ON `voucher`(`ma_voucher`);
CREATE INDEX IF NOT EXISTS `idx_voucher_trang_thai` ON `voucher`(`trang_thai`);
CREATE INDEX IF NOT EXISTS `idx_voucher_ngay_bat_dau` ON `voucher`(`ngay_bat_dau`);
CREATE INDEX IF NOT EXISTS `idx_voucher_ngay_het_han` ON `voucher`(`ngay_het_han`);

-- ================================================
-- 4. TẠO VIEW NÂNG CAO ĐỂ THEO DÕI QUAN HỆ
-- ================================================

-- View: Lịch sử sử dụng voucher với thông tin đầy đủ
CREATE OR REPLACE VIEW `v_voucher_usage_detail` AS
SELECT
    vu.id AS usage_id,
    vu.voucher_id,
    v.ma_voucher,
    v.ten_voucher,
    vu.user_id,
    u.username,
    u.email,
    vu.donhang_id,
    vu.ma_donhang,
    vu.gia_tri_don_hang,
    vu.gia_tri_giam,
    vu.ngay_su_dung,
    d.trangthai AS trang_thai_don_hang,
    d.tongtien AS tong_tien_don_hang
FROM voucher_usage vu
INNER JOIN voucher v ON vu.voucher_id = v.id
INNER JOIN user u ON vu.user_id = u.id
LEFT JOIN donhang d ON vu.donhang_id = d.id
ORDER BY vu.ngay_su_dung DESC;

-- View: Đơn hàng có sử dụng voucher
CREATE OR REPLACE VIEW `v_donhang_with_voucher` AS
SELECT
    d.id AS donhang_id,
    d.iduser,
    u.username,
    u.email,
    d.voucher_id,
    v.ma_voucher,
    v.ten_voucher,
    d.gia_tri_giam,
    d.tong_truoc_giam,
    d.tongtien AS tong_sau_giam,
    d.trangthai,
    d.ngaygiaodukien,
    d.diachi,
    d.sodienthoai
FROM donhang d
INNER JOIN user u ON d.iduser = u.id
LEFT JOIN voucher v ON d.voucher_id = v.id
WHERE d.voucher_id IS NOT NULL
ORDER BY d.id DESC;

-- View: Thống kê voucher theo user
CREATE OR REPLACE VIEW `v_voucher_user_statistics` AS
SELECT
    u.id AS user_id,
    u.username,
    u.email,
    COUNT(DISTINCT vu.voucher_id) AS so_voucher_da_dung,
    COUNT(vu.id) AS tong_lan_su_dung,
    SUM(vu.gia_tri_giam) AS tong_tien_da_tiet_kiem,
    MAX(vu.ngay_su_dung) AS lan_su_dung_gan_nhat
FROM user u
LEFT JOIN voucher_usage vu ON u.id = vu.user_id
GROUP BY u.id, u.username, u.email;

-- ================================================
-- 5. KIỂM TRA RÀNG BUỘC ĐÃ ĐƯỢC TẠO
-- ================================================

SELECT
    CONSTRAINT_NAME AS 'Tên ràng buộc',
    TABLE_NAME AS 'Bảng',
    COLUMN_NAME AS 'Cột',
    REFERENCED_TABLE_NAME AS 'Bảng tham chiếu',
    REFERENCED_COLUMN_NAME AS 'Cột tham chiếu'
FROM information_schema.KEY_COLUMN_USAGE
WHERE CONSTRAINT_SCHEMA = DATABASE()
AND TABLE_NAME IN ('voucher_usage', 'donhang')
AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

-- ================================================
-- HƯỚNG DẪN SỬ DỤNG
-- ================================================
-- 1. Đảm bảo đã chạy create_voucher_system.sql trước
-- 2. Đảm bảo các bảng user, donhang đã tồn tại
-- 3. Chạy file này để thêm foreign keys
-- 4. Kiểm tra kết quả bằng query cuối cùng
-- 5. Test cascade delete:
--    - Xóa 1 voucher → kiểm tra voucher_usage
--    - Xóa 1 user → kiểm tra voucher_usage và donhang
--    - Xóa 1 donhang → kiểm tra voucher_usage (donhang_id = NULL)

-- ================================================
-- LƯU Ý QUAN TRỌNG
-- ================================================
-- CASCADE DELETE:
-- - Xóa voucher → xóa tất cả voucher_usage liên quan
-- - Xóa user → xóa tất cả voucher_usage và donhang của user
-- - Xóa donhang → SET NULL cho voucher_usage.donhang_id (giữ lịch sử)
--
-- Nếu KHÔNG MUỐN CASCADE, đổi thành:
-- ON DELETE RESTRICT (không cho xóa nếu có dữ liệu liên quan)
-- ON DELETE SET NULL (đặt NULL khi xóa)

