-- ===============================================
-- XÓA TẤT CẢ TRIGGER TỒN KHO ĐỂ FIX LỖI TRỪ 2 LẦN
-- ===============================================
-- Lý do: Trigger tự động trừ tồn kho khi INSERT chitietdonhang
-- Nhưng code vnpay_return.php cũng trừ tồn kho khi thanh toán thành công
-- => Dẫn đến trừ 2 lần (mua 1 sản phẩm nhưng tồn kho trừ 2)
--
-- CÁC TRIGGER CẦN XÓA (từ screenshot):
-- 1. giam_tonkho_khi_dat (INSERT - AFTER)
-- 2. capnhat_tonkho_khi_sua (UPDATE - AFTER)
-- 3. tang_tonkho_khi_xoa (DELETE - AFTER)
-- ===============================================

USE appbandienthoai;

-- Xóa tất cả trigger liên quan đến chitietdonhang
DROP TRIGGER IF EXISTS after_chitietdonhang_insert;
DROP TRIGGER IF EXISTS after_chitietdonhang_delete;
DROP TRIGGER IF EXISTS after_chitietdonhang_update;
DROP TRIGGER IF EXISTS giam_tonkho_khi_dat;
DROP TRIGGER IF EXISTS tang_tonkho_khi_xoa;
DROP TRIGGER IF EXISTS capnhat_tonkho_khi_sua;

-- Xóa thêm các trigger có thể có tên khác
DROP TRIGGER IF EXISTS before_chitietdonhang_insert;
DROP TRIGGER IF EXISTS before_chitietdonhang_delete;
DROP TRIGGER IF EXISTS before_chitietdonhang_update;
DROP TRIGGER IF EXISTS update_stock_after_insert;
DROP TRIGGER IF EXISTS update_stock_after_delete;
DROP TRIGGER IF EXISTS update_stock_after_update;

-- ===============================================
-- KIỂM TRA KẾT QUẢ
-- ===============================================
SELECT '====== KIỂM TRA TRIGGER CÒN LẠI ======' AS Step;

-- Kiểm tra trigger còn lại trên bảng chitietdonhang
SHOW TRIGGERS WHERE `Table` = 'chitietdonhang';

-- Nếu kết quả trả về "Empty set (0.00 sec)" => THÀNH CÔNG ✅
-- Nếu vẫn còn trigger => CHẠY LẠI SCRIPT NÀY

SELECT '====== KẾT THÚC ======' AS Step;

