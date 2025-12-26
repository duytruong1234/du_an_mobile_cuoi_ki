-- Script cập nhật database để thêm hệ thống phân quyền
-- Chạy script này trong phpMyAdmin hoặc MySQL Workbench

-- 1. Thêm cột 'role' vào bảng user nếu chưa có
-- role = 0: user thường (mặc định)
-- role = 1: admin (có quyền thêm, sửa, xóa sản phẩm)

ALTER TABLE `user`
ADD COLUMN IF NOT EXISTS `role` INT(11) NOT NULL DEFAULT 0 COMMENT '0=user thường, 1=admin';

-- 2. Tạo tài khoản admin mặc định (nếu chưa có)
-- Email: admin@admin.com
-- Password: admin123
-- Bạn NÊN đổi mật khẩu sau khi đăng nhập lần đầu

INSERT INTO `user` (`email`, `pass`, `username`, `mobile`, `role`)
VALUES ('admin@admin.com', 'admin123', 'Administrator', '0123456789', 1)
ON DUPLICATE KEY UPDATE `role` = 1;

-- 3. Cập nhật tất cả user hiện tại thành user thường (trừ admin)
UPDATE `user`
SET `role` = 0
WHERE `email` != 'admin@admin.com';

-- 4. Kiểm tra kết quả
SELECT * FROM `user`;

-- LƯU Ý:
-- - CHỈ CÓ TÀI KHOẢN ADMIN MỚI CÓ QUYỀN:
--   + Vào màn hình "Quản lí"
--   + Thêm sản phẩm mới
--   + Sửa sản phẩm
--   + Xóa sản phẩm
-- - User thường chỉ có thể xem và mua sản phẩm
-- - Để tạo thêm admin, bạn phải vào database và UPDATE role = 1 thủ công

