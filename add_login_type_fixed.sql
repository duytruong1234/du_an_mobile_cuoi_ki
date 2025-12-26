-- Thêm cột login_type vào bảng user
-- login_type: 'normal' = đăng nhập thường, 'google' = đăng nhập Google

ALTER TABLE `user` ADD COLUMN `login_type` VARCHAR(20) DEFAULT 'normal' AFTER `role`;

-- Cập nhật các tài khoản Google hiện tại (có mobile = '0000000000')
UPDATE `user` SET `login_type` = 'google' WHERE `mobile` = '0000000000';

-- Hiển thị kết quả
SELECT id, email, username, mobile, login_type FROM `user`;

