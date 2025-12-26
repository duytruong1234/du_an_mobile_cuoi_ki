-- =====================================================
-- Script kiểm tra và sửa lỗi database
-- Chạy file này trong phpMyAdmin
-- =====================================================

-- 1. Kiểm tra cấu trúc bảng user hiện tại
DESCRIBE user;

-- 2. Kiểm tra xem cột role đã tồn tại chưa
SELECT COUNT(*) as role_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'appbandienthoai'
AND TABLE_NAME = 'user'
AND COLUMN_NAME = 'role';

-- 3. Thêm cột role nếu chưa có (bỏ qua nếu đã có)
ALTER TABLE user
ADD COLUMN role INT DEFAULT 0 COMMENT '0=user thường, 1=admin';

-- 4. Cập nhật tất cả user cũ có role NULL thành 0
UPDATE user
SET role = 0
WHERE role IS NULL;

-- 5. Tạo hoặc cập nhật tài khoản admin
-- Kiểm tra xem admin đã tồn tại chưa
SELECT * FROM user WHERE email = 'admin@admin.com';

-- Nếu chưa có, thêm admin mới
INSERT INTO user (email, pass, username, mobile, role)
VALUES ('admin@admin.com', 'admin123', 'Administrator', '0123456789', 1)
ON DUPLICATE KEY UPDATE role = 1, pass = 'admin123';

-- 6. Hiển thị tất cả user và role của họ
SELECT id, email, username, mobile, role,
       CASE
           WHEN role = 1 THEN 'ADMIN'
           ELSE 'USER THƯỜNG'
       END as loai_tai_khoan
FROM user
ORDER BY role DESC, id ASC;

-- 7. Đếm số admin và user thường
SELECT
    SUM(CASE WHEN role = 1 THEN 1 ELSE 0 END) as so_admin,
    SUM(CASE WHEN role = 0 THEN 1 ELSE 0 END) as so_user_thuong,
    COUNT(*) as tong_so_user
FROM user;

