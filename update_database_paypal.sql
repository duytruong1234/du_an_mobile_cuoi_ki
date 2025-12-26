-- Cập nhật bảng donhang để hỗ trợ PayPal
-- Thêm các cột để lưu thông tin PayPal

ALTER TABLE `donhang`
ADD COLUMN `paypal_order_id` VARCHAR(100) NULL AFTER `vnpay_pay_date`,
ADD COLUMN `paypal_payer_id` VARCHAR(100) NULL AFTER `paypal_order_id`,
ADD COLUMN `paypal_payment_date` DATETIME NULL AFTER `paypal_payer_id`;

-- Tạo index cho tìm kiếm nhanh
ALTER TABLE `donhang`
ADD INDEX `idx_paypal_order_id` (`paypal_order_id`);

