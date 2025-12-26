TYPE=TRIGGERS
triggers='CREATE DEFINER=`root`@`localhost` TRIGGER after_chitietdonhang_insert\nAFTER INSERT ON chitietdonhang\nFOR EACH ROW\nBEGIN\n    -- Giảm tồn kho\n    UPDATE sanphammoi\n    SET soluongtonkho = GREATEST(0, soluongtonkho - NEW.soluong)\n    WHERE id = NEW.idsp;\nEND' 'CREATE DEFINER=`root`@`localhost` TRIGGER after_chitietdonhang_update\nAFTER UPDATE ON chitietdonhang\nFOR EACH ROW\nBEGIN\n    -- Hoàn lại số lượng cũ\n    UPDATE sanphammoi\n    SET soluongtonkho = soluongtonkho + OLD.soluong\n    WHERE id = OLD.idsp;\n    \n    -- Trừ số lượng mới\n    UPDATE sanphammoi\n    SET soluongtonkho = GREATEST(0, soluongtonkho - NEW.soluong)\n    WHERE id = NEW.idsp;\nEND' 'CREATE DEFINER=`root`@`localhost` TRIGGER after_chitietdonhang_delete\nAFTER DELETE ON chitietdonhang\nFOR EACH ROW\nBEGIN\n    -- Hoàn lại tồn kho\n    UPDATE sanphammoi\n    SET soluongtonkho = soluongtonkho + OLD.soluong\n    WHERE id = OLD.idsp;\nEND'
sql_modes=1098907648 1098907648 1098907648
definers='root@localhost' 'root@localhost' 'root@localhost'
client_cs_names='utf8mb4' 'utf8mb4' 'utf8mb4'
connection_cl_names='utf8mb4_general_ci' 'utf8mb4_general_ci' 'utf8mb4_general_ci'
db_cl_names='utf8mb4_general_ci' 'utf8mb4_general_ci' 'utf8mb4_general_ci'
created=1762758897308573 1762758897333504 1762758897318010
