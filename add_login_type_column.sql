-- Thêm cột login_type vào bảng user để phân biệt tài khoản Google và tài khoản thường
-- login_type: 'normal' = tài khoản đăng ký thường, 'google' = tài khoản Google

-- Kiểm tra và thêm cột nếu chưa có
ALTER TABLE user
ADD COLUMN IF NOT EXISTS login_type VARCHAR(20) DEFAULT 'normal';

-- Cập nhật tài khoản Google hiện có (số điện thoại = 0000000000)
UPDATE user
SET login_type = 'google'
WHERE mobile = '0000000000' OR email LIKE '%gmail.com';

-- Kiểm tra kết quả
SELECT id, email, username, mobile, login_type, role FROM user;

