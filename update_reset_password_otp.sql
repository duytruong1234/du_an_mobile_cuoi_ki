-- Thêm cột OTP và thời gian hết hạn OTP vào bảng user
ALTER TABLE `user` ADD COLUMN `reset_otp` VARCHAR(6) DEFAULT NULL AFTER `login_type`;
ALTER TABLE `user` ADD COLUMN `reset_otp_expiry` DATETIME DEFAULT NULL AFTER `reset_otp`;

-- Kiểm tra cấu trúc bảng
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'user' AND TABLE_SCHEMA = 'appbandongho';

