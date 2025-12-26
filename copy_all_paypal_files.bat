@echo off
echo ========================================
echo COPY FILE PAYPAL VAO XAMPP - FIX NGAY
echo ========================================

REM Tao thu muc neu chua ton tai
if not exist "C:\xampp\htdocs\appbandienthoai" (
    mkdir "C:\xampp\htdocs\appbandienthoai"
    echo Đã tạo thư mục C:\xampp\htdocs\appbandienthoai
)

echo.
echo Dang copy cac file PayPal...
echo.

REM Copy tat ca file paypal
copy /Y "D:\AppBanDongHo\Server\paypal_*.php" "C:\xampp\htdocs\appbandienthoai\"

REM Copy file connect.php (cần thiết cho PayPal)
copy /Y "D:\AppBanDongHo\Server\connect.php" "C:\xampp\htdocs\appbandienthoai\"

echo.
echo ========================================
echo DANH SACH FILE DA COPY:
echo ========================================
dir /B "C:\xampp\htdocs\appbandienthoai\paypal_*.php"
echo.
dir /B "C:\xampp\htdocs\appbandienthoai\connect.php"

echo.
echo ========================================
echo HOAN THANH!
echo ========================================
echo.
echo Ngrok URL hien tai:
echo https://donya-barwise-subversively.ngrok-free.dev
echo.
echo File config da duoc cap nhat dung URL!
echo.
pause

