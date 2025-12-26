-- Script để sửa các URL hình ảnh bị lặp trong database
-- Chạy script này trong phpMyAdmin hoặc MySQL client

-- 1. Sửa bảng sanphammoi
UPDATE sanphammoi
SET hinhanh = REPLACE(hinhanh, 'http://10.0.2.2/appbandienthoai/images/http://10.0.2.2/appbandienthoai/images/', '')
WHERE hinhanh LIKE '%http://10.0.2.2/appbandienthoai/images/http://10.0.2.2/appbandienthoai/images/%';

UPDATE sanphammoi
SET hinhanh = REPLACE(hinhanh, 'http://10.0.2.2/appbandienthoai/images/', '')
WHERE hinhanh LIKE 'http://10.0.2.2/appbandienthoai/images/%';

UPDATE sanphammoi
SET hinhanh = REPLACE(hinhanh, 'http://localhost/appbandienthoai/images/', '')
WHERE hinhanh LIKE 'http://localhost/appbandienthoai/images/%';

-- 2. Sửa bảng sanpham (nếu có)
UPDATE sanpham
SET hinhanh = REPLACE(hinhanh, 'http://10.0.2.2/appbandienthoai/images/http://10.0.2.2/appbandienthoai/images/', '')
WHERE hinhanh LIKE '%http://10.0.2.2/appbandienthoai/images/http://10.0.2.2/appbandienthoai/images/%';

UPDATE sanpham
SET hinhanh = REPLACE(hinhanh, 'http://10.0.2.2/appbandienthoai/images/', '')
WHERE hinhanh LIKE 'http://10.0.2.2/appbandienthoai/images/%';

UPDATE sanpham
SET hinhanh = REPLACE(hinhanh, 'http://localhost/appbandienthoai/images/', '')
WHERE hinhanh LIKE 'http://localhost/appbandienthoai/images/%';

-- Kiểm tra kết quả
SELECT id, tensp, hinhanh FROM sanphammoi LIMIT 10;
SELECT id, tensp, hinhanh FROM sanpham LIMIT 10;

