-- Tạo bảng giỏ hàng để lưu trữ sản phẩm trong giỏ hàng của từng user
-- Chạy script này trong phpMyAdmin hoặc MySQL client

USE appbandienthoai;

-- Tạo bảng giohang
CREATE TABLE IF NOT EXISTS `giohang` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `iduser` int(11) NOT NULL,
  `idsp` int(11) NOT NULL,
  `tensp` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `giasp` bigint(20) NOT NULL,
  `hinhsp` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `soluong` int(11) NOT NULL DEFAULT 1,
  `ngaythem` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ngaycapnhat` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_product` (`iduser`, `idsp`),
  KEY `idx_iduser` (`iduser`),
  KEY `idx_idsp` (`idsp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Thêm foreign key constraints (nếu bảng user và sanphammoi đã tồn tại)
-- Uncomment các dòng dưới nếu cần:
-- ALTER TABLE `giohang`
--   ADD CONSTRAINT `fk_giohang_user` FOREIGN KEY (`iduser`) REFERENCES `user` (`id`) ON DELETE CASCADE,
--   ADD CONSTRAINT `fk_giohang_sanpham` FOREIGN KEY (`idsp`) REFERENCES `sanphammoi` (`id`) ON DELETE CASCADE;

-- Index để tối ưu performance
CREATE INDEX `idx_ngaythem` ON `giohang` (`ngaythem`);

-- View thống kê giỏ hàng
CREATE OR REPLACE VIEW `view_giohang_thongke` AS
SELECT
    g.iduser,
    COUNT(g.id) as so_sanpham,
    SUM(g.soluong) as tong_soluong,
    SUM(g.giasp * g.soluong) as tong_giatri
FROM giohang g
GROUP BY g.iduser;

-- Sample query để test
-- SELECT * FROM giohang WHERE iduser = 1;
-- SELECT * FROM view_giohang_thongke;

