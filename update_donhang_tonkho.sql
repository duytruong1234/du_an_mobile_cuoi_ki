-- =====================================================
-- CẬP NHẬT DATABASE CHO CHỨC NĂNG TỒN KHO VÀ ĐƠN HÀNG
-- =====================================================

-- 1. CẬP NHẬT BẢNG sanphammoi: Thêm cột số lượng tồn kho
ALTER TABLE sanphammoi
ADD COLUMN soluongtonkho INT(11) DEFAULT 0 COMMENT 'Số lượng tồn kho';

-- 2. CẬP NHẬT BẢNG donhang: Thêm các trường cần thiết
ALTER TABLE donhang
ADD COLUMN madonhang VARCHAR(50) UNIQUE COMMENT 'Mã đơn hàng duy nhất',
ADD COLUMN ngaydat DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày đặt hàng',
ADD COLUMN ngaygiaodukien DATE COMMENT 'Ngày giao hàng dự kiến',
ADD COLUMN trangthai VARCHAR(50) DEFAULT 'Chờ xử lý' COMMENT 'Trạng thái đơn hàng';

-- Tạo index cho madonhang để tìm kiếm nhanh hơn
CREATE INDEX idx_madonhang ON donhang(madonhang);
CREATE INDEX idx_trangthai ON donhang(trangthai);
CREATE INDEX idx_ngaydat ON donhang(ngaydat);

-- 3. CẬP NHẬT BẢNG chitietdonhang: Thay đổi kiểu dữ liệu giá
ALTER TABLE chitietdonhang
MODIFY COLUMN gia DECIMAL(10,2) NOT NULL COMMENT 'Giá sản phẩm';

-- Thêm primary key cho chitietdonhang
ALTER TABLE chitietdonhang
ADD COLUMN id INT(11) AUTO_INCREMENT PRIMARY KEY FIRST;

-- 4. TẠO TRIGGER TỰ ĐỘNG GIẢM TỒN KHO KHI TẠO CHI TIẾT ĐƠN HÀNG
DELIMITER //

DROP TRIGGER IF EXISTS giam_tonkho_khi_dat//
CREATE TRIGGER giam_tonkho_khi_dat
AFTER INSERT ON chitietdonhang
FOR EACH ROW
BEGIN
    -- Giảm số lượng tồn kho
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho - NEW.soluong
    WHERE id = NEW.idsp;

    -- Kiểm tra nếu tồn kho âm thì báo lỗi
    IF (SELECT soluongtonkho FROM sanphammoi WHERE id = NEW.idsp) < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lượng tồn kho không đủ';
    END IF;
END//

-- 5. TẠO TRIGGER TỰ ĐỘNG TĂNG TỒN KHO KHI XÓA CHI TIẾT ĐƠN HÀNG
DROP TRIGGER IF EXISTS tang_tonkho_khi_xoa//
CREATE TRIGGER tang_tonkho_khi_xoa
AFTER DELETE ON chitietdonhang
FOR EACH ROW
BEGIN
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho + OLD.soluong
    WHERE id = OLD.idsp;
END//

-- 6. TẠO TRIGGER CẬP NHẬT TỒN KHO KHI SỬA CHI TIẾT ĐƠN HÀNG
DROP TRIGGER IF EXISTS capnhat_tonkho_khi_sua//
CREATE TRIGGER capnhat_tonkho_khi_sua
AFTER UPDATE ON chitietdonhang
FOR EACH ROW
BEGIN
    -- Hoàn lại số lượng cũ
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho + OLD.soluong
    WHERE id = OLD.idsp;

    -- Trừ số lượng mới
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho - NEW.soluong
    WHERE id = NEW.idsp;

    -- Kiểm tra tồn kho
    IF (SELECT soluongtonkho FROM sanphammoi WHERE id = NEW.idsp) < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lượng tồn kho không đủ';
    END IF;
END//

DELIMITER ;

-- 7. CẬP NHẬT MÃ ĐƠN HÀNG CHO CÁC ĐƠN HÀNG CŨ (nếu có)
UPDATE donhang
SET madonhang = CONCAT('DH', LPAD(id, 8, '0'))
WHERE madonhang IS NULL OR madonhang = '';

-- 8. CẬP NHẬT NGÀY ĐẶT CHO CÁC ĐƠN HÀNG CŨ
UPDATE donhang
SET ngaydat = thoigian
WHERE ngaydat IS NULL;

-- 9. CẬP NHẬT NGÀY GIAO DỰ KIẾN (3 ngày sau ngày đặt)
UPDATE donhang
SET ngaygiaodukien = DATE_ADD(ngaydat, INTERVAL 3 DAY)
WHERE ngaygiaodukien IS NULL;

-- 10. THÊM DỮ LIỆU MẪU TỒN KHO (tùy chọn)
-- Cập nhật tồn kho cho các sản phẩm hiện có (mặc định 100 sản phẩm)
UPDATE sanphammoi
SET soluongtonkho = 100
WHERE soluongtonkho = 0 OR soluongtonkho IS NULL;

-- =====================================================
-- TẠO STORED PROCEDURE HỖ TRỢ
-- =====================================================

-- Procedure kiểm tra tồn kho
DELIMITER //

DROP PROCEDURE IF EXISTS sp_KiemTraTonKho//
CREATE PROCEDURE sp_KiemTraTonKho(IN p_idsp INT)
BEGIN
    SELECT id, tensp, soluongtonkho, giasp
    FROM sanphammoi
    WHERE id = p_idsp;
END//

-- Procedure lấy chi tiết đơn hàng
DROP PROCEDURE IF EXISTS sp_ChiTietDonHang//
CREATE PROCEDURE sp_ChiTietDonHang(IN p_madonhang VARCHAR(50))
BEGIN
    SELECT
        dh.id,
        dh.madonhang,
        dh.ngaydat,
        dh.ngaygiaodukien,
        dh.trangthai,
        dh.diachi,
        dh.sodienthoai,
        dh.tongtien,
        ct.idsp,
        sp.tensp,
        sp.hinhanh,
        ct.soluong,
        ct.gia,
        (ct.soluong * ct.gia) AS thanhtien
    FROM donhang dh
    INNER JOIN chitietdonhang ct ON dh.id = ct.iddonhang
    INNER JOIN sanphammoi sp ON ct.idsp = sp.id
    WHERE dh.madonhang = p_madonhang;
END//

-- Procedure cập nhật trạng thái đơn hàng
DROP PROCEDURE IF EXISTS sp_CapNhatTrangThai//
CREATE PROCEDURE sp_CapNhatTrangThai(
    IN p_madonhang VARCHAR(50),
    IN p_trangthai VARCHAR(50)
)
BEGIN
    UPDATE donhang
    SET trangthai = p_trangthai
    WHERE madonhang = p_madonhang;
END//

DELIMITER ;

-- =====================================================
-- TẠO VIEW HỖ TRỢ
-- =====================================================

-- View thống kê tồn kho
CREATE OR REPLACE VIEW v_ThongKeTonKho AS
SELECT
    sp.id,
    sp.tensp,
    sp.hinhanh,
    sp.giasp,
    sp.soluongtonkho,
    ls.tensanpham AS loaisanpham,
    CASE
        WHEN sp.soluongtonkho = 0 THEN 'Hết hàng'
        WHEN sp.soluongtonkho < 10 THEN 'Sắp hết'
        ELSE 'Còn hàng'
    END AS tinhtrang
FROM sanphammoi sp
LEFT JOIN sanpham ls ON sp.loai = ls.id;

-- View danh sách đơn hàng đầy đủ
CREATE OR REPLACE VIEW v_DanhSachDonHang AS
SELECT
    dh.id,
    dh.madonhang,
    u.username,
    u.email,
    dh.diachi,
    dh.sodienthoai,
    dh.soluong,
    dh.tongtien,
    dh.ngaydat,
    dh.ngaygiaodukien,
    dh.trangthai,
    DATEDIFF(dh.ngaygiaodukien, CURDATE()) AS songayconlai
FROM donhang dh
INNER JOIN user u ON dh.iduser = u.id
ORDER BY dh.ngaydat DESC;

-- =====================================================
-- HOÀN TẤT
-- =====================================================
SELECT 'Cập nhật database thành công!' AS KetQua;

