@echo off
echo ========================================
echo COPY FILE PAYPAL VAO XAMPP
echo ========================================

REM Kiem tra thu muc Server
if not exist "D:\AppBanDongHo\Server" (
    echo ERROR: Thu muc D:\AppBanDongHo\Server khong ton tai!
    pause
    exit /b 1
)

REM Kiem tra thu muc XAMPP
if not exist "C:\xampp\htdocs\appbandienthoai" (
    echo ERROR: Thu muc C:\xampp\htdocs\appbandienthoai khong ton tai!
    echo Vui long tao thu muc truoc khi chay script!
    pause
    exit /b 1
)

echo.
echo Dang copy cac file PayPal...
echo.

REM Copy tat ca file paypal
copy /Y "D:\AppBanDongHo\Server\paypal_*.php" "C:\xampp\htdocs\appbandienthoai\"

REM Kiem tra ket qua
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo THANH CONG! Da copy cac file:
    echo ========================================
    dir /B "C:\xampp\htdocs\appbandienthoai\paypal_*.php"
    echo.
    echo HOAN THANH! Vui long chay lai app!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo LOI! Khong the copy file!
    echo ========================================
)

echo.
pause

