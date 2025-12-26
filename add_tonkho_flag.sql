-- THÊM CỘT KIỂM TRA ĐÃ TRỪ TỒN KHO HAY CHƯA
-- Để tránh trường hợp VNPay callback nhiều lần

USE appbandienthoai;

-- Thêm cột is_tonkho_updated vào bảng donhang
ALTER TABLE donhang
ADD COLUMN is_tonkho_updated TINYINT(1) DEFAULT 0 COMMENT 'Đã trừ tồn kho: 0=Chưa, 1=Rồi';

-- Cập nhật các đơn hàng đã thanh toán trước đó
UPDATE donhang
SET is_tonkho_updated = 1
WHERE trangthai IN ('Đã thanh toán', 'Đang xử lý', 'Đang giao hàng', 'Đã giao hàng');

-- Kiểm tra
SELECT COUNT(*) as total_orders,
       SUM(CASE WHEN is_tonkho_updated = 1 THEN 1 ELSE 0 END) as updated_orders
FROM donhang;

