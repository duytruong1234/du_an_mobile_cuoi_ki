-- TRIGGER TỰ ĐỘNG GIẢM TỒN KHO KHI TẠO ĐƠN HÀNG
-- Chạy script này trong phpMyAdmin để tạo trigger

USE appbandienthoai;

-- Xóa trigger cũ nếu có
DROP TRIGGER IF EXISTS after_chitietdonhang_insert;
DROP TRIGGER IF EXISTS after_chitietdonhang_delete;

-- Trigger: Giảm tồn kho khi thêm chi tiết đơn hàng
DELIMITER $$

CREATE TRIGGER after_chitietdonhang_insert
AFTER INSERT ON chitietdonhang
FOR EACH ROW
BEGIN
    -- Giảm tồn kho
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho - NEW.soluong
    WHERE id = NEW.idsp;

    -- Log để debug (optional)
    -- INSERT INTO log_tonkho (idsp, soluong_thaydoi, loai, thoidiem)
    -- VALUES (NEW.idsp, -NEW.soluong, 'GIAM', NOW());
END$$

-- Trigger: Hoàn tồn kho khi hủy đơn hàng (xóa chi tiết)
CREATE TRIGGER after_chitietdonhang_delete
AFTER DELETE ON chitietdonhang
FOR EACH ROW
BEGIN
    -- Hoàn lại tồn kho
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho + OLD.soluong
    WHERE id = OLD.idsp;
END$$

DELIMITER ;

-- Test trigger
-- INSERT INTO chitietdonhang (iddonhang, idsp, soluong, gia) VALUES (1, 59, 2, 1990000);
-- SELECT soluongtonkho FROM sanphammoi WHERE id = 59; -- Kiểm tra đã giảm chưa

