-- update_vnpay_database.sql
-- Thêm các cột để lưu thông tin giao dịch VNPay trong bảng `donhang`
-- Chạy file này trong phpMyAdmin hoặc MySQL client (ví dụ: chạy trong database `appbandongho`).

-- LƯU Ý: Nếu bạn đang dùng MySQL < 8.0 hoặc môi trường không hỗ trợ IF NOT EXISTS
-- thì câu lệnh ALTER TABLE sẽ báo lỗi nếu cột đã tồn tại. Trong trường hợp đó,
-- bạn có thể kiểm tra cấu trúc bảng trước khi chạy hoặc bỏ qua các lỗi cột đã tồn tại.

ALTER TABLE `donhang`
  ADD COLUMN `vnpay_transaction_no` VARCHAR(50) NULL COMMENT 'Mã giao dịch VNPay',
  ADD COLUMN `vnpay_bank_code` VARCHAR(20) NULL COMMENT 'Mã ngân hàng thanh toán',
  ADD COLUMN `vnpay_pay_date` VARCHAR(20) NULL COMMENT 'Thời gian thanh toán VNPay';

-- (Tùy chọn) Tạo index cho cột madonhang nếu chưa có, để tìm kiếm trạng thái nhanh hơn
-- ALTER TABLE `donhang` ADD INDEX `idx_madonhang` (`madonhang`(50));

-- Ví dụ kiểm tra nhanh sau khi chạy:
-- SELECT madonhang, trangthai, vnpay_transaction_no, vnpay_bank_code, vnpay_pay_date FROM donhang ORDER BY id DESC LIMIT 10;
