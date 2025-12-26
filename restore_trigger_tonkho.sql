-- ===============================================
-- KHÔI PHỤC TRIGGER TỒN KHO TỰ ĐỘNG
-- ===============================================
-- Trigger sẽ tự động trừ/hoàn tồn kho khi thao tác với chitietdonhang
-- Điều này giúp tất cả phương thức thanh toán (COD, PayPal, VNPay) đều nhất quán
-- ===============================================

USE appbandienthoai;

-- Xóa trigger cũ trước (nếu có)
DROP TRIGGER IF EXISTS after_chitietdonhang_insert;
DROP TRIGGER IF EXISTS after_chitietdonhang_delete;
DROP TRIGGER IF EXISTS giam_tonkho_khi_dat;
DROP TRIGGER IF EXISTS tang_tonkho_khi_xoa;
DROP TRIGGER IF EXISTS capnhat_tonkho_khi_sua;

DELIMITER $$

-- ===============================================
-- TRIGGER 1: TỰ ĐỘNG GIẢM TỒN KHO KHI THÊM CHI TIẾT ĐƠN HÀNG
-- ===============================================
CREATE TRIGGER after_chitietdonhang_insert
AFTER INSERT ON chitietdonhang
FOR EACH ROW
BEGIN
    -- Giảm tồn kho
    UPDATE sanphammoi
    SET soluongtonkho = GREATEST(0, soluongtonkho - NEW.soluong)
    WHERE id = NEW.idsp;
END$$

-- ===============================================
-- TRIGGER 2: TỰ ĐỘNG HOÀN TỒN KHO KHI XÓA CHI TIẾT ĐƠN HÀNG
-- ===============================================
CREATE TRIGGER after_chitietdonhang_delete
AFTER DELETE ON chitietdonhang
FOR EACH ROW
BEGIN
    -- Hoàn lại tồn kho
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho + OLD.soluong
    WHERE id = OLD.idsp;
END$$

-- ===============================================
-- TRIGGER 3: TỰ ĐỘNG CẬP NHẬT TỒN KHO KHI SỬA CHI TIẾT ĐƠN HÀNG
-- ===============================================
CREATE TRIGGER after_chitietdonhang_update
AFTER UPDATE ON chitietdonhang
FOR EACH ROW
BEGIN
    -- Hoàn lại số lượng cũ
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho + OLD.soluong
    WHERE id = OLD.idsp;

    -- Trừ số lượng mới
    UPDATE sanphammoi
    SET soluongtonkho = GREATEST(0, soluongtonkho - NEW.soluong)
    WHERE id = NEW.idsp;
END$$

DELIMITER ;

-- ===============================================
-- KIỂM TRA KẾT QUẢ
-- ===============================================
SELECT '====== TRIGGER ĐÃ ĐƯỢC TẠO ======' AS Step;

SHOW TRIGGERS WHERE `Table` = 'chitietdonhang';

SELECT '====== HOÀN THÀNH ======' AS Step;

