@echo off
chcp 65001 >nul
echo ===================================================
echo   DỌN DẸP DỰ ÁN - XÓA CÁC FILE DƯ THỪA
echo ===================================================
echo.

REM Chuyển đến thư mục dự án
cd /d D:\AppBanDongHo

echo [1/7] Đang xóa các file check_* (debug files)...
del /F /Q check_admin.php 2>nul
del /F /Q check_chitiet.php 2>nul
del /F /Q check_data_detail.php 2>nul
del /F /Q check_database.php 2>nul
del /F /Q check_loaisp.php 2>nul
del /F /Q check_mapping.php 2>nul
del /F /Q check_products_by_type.php 2>nul
del /F /Q check_sanpham.php 2>nul
del /F /Q check_users.html 2>nul
del /F /Q check_users.php 2>nul

echo [2/7] Đang xóa các file debug_* (debug files)...
del /F /Q debug_admin_users.php 2>nul
del /F /Q debug_database.php 2>nul
del /F /Q debug_price.php 2>nul

echo [3/7] Đang xóa các file test_* (test files)...
del /F /Q test_api.php 2>nul
del /F /Q test_check_price.php 2>nul
del /F /Q test_login.html 2>nul
del /F /Q test_login_debug.php 2>nul
del /F /Q test_update_cancelled_order.html 2>nul

echo [4/7] Đang xóa các file PHP trùng lặp (đã có trong Server/)...
del /F /Q capNhatTonKho.php 2>nul
del /F /Q capNhatTrangThai.php 2>nul
del /F /Q chitiet.php 2>nul
del /F /Q connect.php 2>nul
del /F /Q dangki.php 2>nul
del /F /Q dangnhap.php 2>nul
del /F /Q deleteUser.php 2>nul
del /F /Q donhang.php 2>nul
del /F /Q getAllUsers.php 2>nul
del /F /Q getChiTietDonHang.php 2>nul
del /F /Q getDonHang.php 2>nul
del /F /Q getloaisp.php 2>nul
del /F /Q getTonKho.php 2>nul
del /F /Q kiemTraTonKho.php 2>nul
del /F /Q setTonKho.php 2>nul
del /F /Q taoDonHang.php 2>nul
del /F /Q updatesp.php 2>nul
del /F /Q updateUserRole.php 2>nul
del /F /Q xemdonhang.php 2>nul
del /F /Q fix_loaisp_names.php 2>nul

echo [5/7] Đang xóa file getAllVouchers_new.php (không sử dụng)...
del /F /Q getAllVouchers_new.php 2>nul

echo [6/7] Đang xóa các file SQL cũ/fix tạm thời...
del /F /Q add_login_type_column.sql 2>nul
del /F /Q add_login_type_fixed.sql 2>nul
del /F /Q add_profile_menu.sql 2>nul
del /F /Q fix_database.sql 2>nul
del /F /Q fix_duplicate_image_urls.sql 2>nul
del /F /Q update_database_add_role.sql 2>nul
del /F /Q update_donhang_tonkho.sql 2>nul
del /F /Q update_reset_password.sql 2>nul
del /F /Q update_reset_password_otp.sql 2>nul

echo [7/7] Đang xóa các file .bat copy không cần thiết...
del /F /Q copy_all_paypal_files.bat 2>nul
del /F /Q copy_paypal_files.bat 2>nul
del /F /Q copy_paypal_to_xampp.bat 2>nul
del /F /Q xoa_file_du_thua.bat 2>nul

echo.
echo [BONUS] Đang xóa các file test trong Server/...
cd Server
del /F /Q test_chitiet.php 2>nul
del /F /Q test_ngrok.php 2>nul
del /F /Q test_paypal.php 2>nul
del /F /Q test_response.php 2>nul
del /F /Q test_vnpay_config.php 2>nul
del /F /Q test_vnpay_simple.php 2>nul
del /F /Q test.php 2>nul
del /F /Q fix_duplicate_urls.php 2>nul
cd ..

echo.
echo ===================================================
echo   HOÀN TẤT! CÁC FILE DƯ THỪA ĐÃ ĐƯỢC XÓA
echo ===================================================
echo.
echo Các loại file đã xóa:
echo   - File check_* và debug_*: 13 files
echo   - File test_*: 12 files
echo   - File PHP trùng lặp: 19 files
echo   - File SQL cũ: 9 files
echo   - File .bat copy: 4 files
echo   - File getAllVouchers_new.php: 1 file
echo ===================================================
echo.
echo Tổng cộng: ~58 files dư thừa đã được xóa
echo.
pause

