@echo off
echo Dang xoa cac file khong su dung va du thua...

REM Xoa cac file test va debug trong thu muc goc
del /F /Q "check_admin.php" 2>nul
del /F /Q "check_chitiet.php" 2>nul
del /F /Q "check_data_detail.php" 2>nul
del /F /Q "check_database.php" 2>nul
del /F /Q "check_loaisp.php" 2>nul
del /F /Q "check_mapping.php" 2>nul
del /F /Q "check_products_by_type.php" 2>nul
del /F /Q "check_sanpham.php" 2>nul
del /F /Q "check_users.html" 2>nul
del /F /Q "check_users.php" 2>nul
del /F /Q "debug_database.php" 2>nul
del /F /Q "test_api.php" 2>nul
del /F /Q "test_login.html" 2>nul
del /F /Q "test_login_debug.php" 2>nul
del /F /Q "test_update_cancelled_order.html" 2>nul
del /F /Q "fix_loaisp_names.php" 2>nul

REM Xoa cac file SQL cu va trung lap
del /F /Q "add_login_type_column.sql" 2>nul
del /F /Q "add_profile_menu.sql" 2>nul
del /F /Q "fix_database.sql" 2>nul
del /F /Q "fix_duplicate_image_urls.sql" 2>nul
del /F /Q "update_database_add_role.sql" 2>nul
del /F /Q "update_donhang_tonkho.sql" 2>nul
del /F /Q "update_reset_password.sql" 2>nul

REM Xoa cac file PHP trung lap trong thu muc goc (da co trong Server/)
del /F /Q "capNhatTonKho.php" 2>nul
del /F /Q "capNhatTrangThai.php" 2>nul
del /F /Q "chitiet.php" 2>nul
del /F /Q "connect.php" 2>nul
del /F /Q "dangki.php" 2>nul
del /F /Q "dangnhap.php" 2>nul
del /F /Q "deleteUser.php" 2>nul
del /F /Q "donhang.php" 2>nul
del /F /Q "getAllUsers.php" 2>nul
del /F /Q "getChiTietDonHang.php" 2>nul
del /F /Q "getDonHang.php" 2>nul
del /F /Q "getloaisp.php" 2>nul
del /F /Q "getTonKho.php" 2>nul
del /F /Q "kiemTraTonKho.php" 2>nul
del /F /Q "setTonKho.php" 2>nul
del /F /Q "taoDonHang.php" 2>nul
del /F /Q "updatesp.php" 2>nul
del /F /Q "updateUserRole.php" 2>nul
del /F /Q "xemdonhang.php" 2>nul

REM Xoa cac file test trong Server/
del /F /Q "Server\test.php" 2>nul
del /F /Q "Server\test_chitiet.php" 2>nul
del /F /Q "Server\test_vnpay_config.php" 2>nul
del /F /Q "Server\test_vnpay_simple.php" 2>nul
del /F /Q "Server\fix_duplicate_urls.php" 2>nul

REM Xoa cac file huong dan trung lap va cu
del /F /Q "HUONG_DAN_DANG_NHAP_GOOGLE.md" 2>nul
del /F /Q "HUONG_DAN_QUEN_MAT_KHAU.md" 2>nul
del /F /Q "HUONG_DAN_SUA_LOI_DOI_MAT_KHAU_GOOGLE.md" 2>nul
del /F /Q "HUONG_DAN_SUA_LOI_GOOGLE_SIGNIN.md" 2>nul
del /F /Q "HUONG_DAN_SUA_LOI_GOOGLE_SIGNIN_SHA1.md" 2>nul
del /F /Q "HUONG_DAN_QUAN_LY_NGUOI_DUNG.md" 2>nul
del /F /Q "HUONG_DAN_VNPAY.md" 2>nul
del /F /Q "DANH_GIA_SO_DO_USE_CASE.md" 2>nul
del /F /Q "FIX_THONGKE_MPANDROIDCHART.md" 2>nul
del /F /Q "GLIDE_IMAGE_FIX_GUIDE.md" 2>nul
del /F /Q "IMAGE_LOADING_ISSUES_FIXED.md" 2>nul
del /F /Q "LAY_SHA1_FINGERPRINT.md" 2>nul
del /F /Q "TEST_VNPAY_NHANH.md" 2>nul
del /F /Q "TOM_TAT_PHAN_QUYEN.md" 2>nul
del /F /Q "USE_CASE_DIAGRAM.md" 2>nul

echo.
echo Da xoa xong cac file khong su dung va du thua!
echo.
pause

