TYPE=TRIGGERS
triggers='CREATE DEFINER=`root`@`localhost` TRIGGER `after_donhang_insert_update_voucher`\nAFTER INSERT ON `donhang`\nFOR EACH ROW\nBEGIN\n    -- Nếu đơn hàng có dùng voucher, tăng số lượng đã sử dụng\n    IF NEW.voucher_id IS NOT NULL THEN\n        UPDATE `voucher`\n        SET `da_su_dung` = `da_su_dung` + 1\n        WHERE `id` = NEW.voucher_id;\n    END IF;\nEND'
sql_modes=1098907648
definers='root@localhost'
client_cs_names='utf8mb4'
connection_cl_names='utf8mb4_general_ci'
db_cl_names='utf8mb4_general_ci'
created=1762140754559859
