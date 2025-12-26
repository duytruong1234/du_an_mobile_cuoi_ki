<?php
/**
 * vnpay_return.php
 * API xác thực kết quả thanh toán VNPay, cập nhật trạng thái đơn hàng, giảm tồn kho, trả về JSON
 * Input: GET các tham số vnp_*
 * Output: JSON { success, message, status, madonhang, vnp_TransactionNo, vnp_BankCode, vnp_PayDate }
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';
require_once __DIR__ . '/vnpay_config.php';

// Thu thập các tham số bắt đầu bằng vnp_
$inputData = array();
foreach ($_GET as $key => $value) {
    if (substr($key, 0, 4) === 'vnp_') {
        $inputData[$key] = $value;
    }
}

// ✅ LOGGING DEBUG - Ghi toàn bộ dữ liệu VNPay trả về
file_put_contents(__DIR__ . '/vnpay_debug_return.txt', json_encode([
    'timestamp' => date('Y-m-d H:i:s'),
    'all_get_params' => $_GET
], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) . "\n" . str_repeat('=', 80) . "\n\n", FILE_APPEND);

// Lấy secure hash gửi về
$vnp_SecureHash = isset($inputData['vnp_SecureHash']) ? $inputData['vnp_SecureHash'] : '';
unset($inputData['vnp_SecureHash'], $inputData['vnp_SecureHashType']);

ksort($inputData);
$hashData = '';
$i = 0;
foreach ($inputData as $key => $value) {
    if ($i === 0) {
        $hashData .= urlencode($key) . '=' . urlencode($value);
    } else {
        $hashData .= '&' . urlencode($key) . '=' . urlencode($value);
    }
    $i++;
}
$secureHash = hash_hmac('sha512', $hashData, VNPAY_HASH_SECRET);

// ✅ LOGGING DEBUG - So sánh hash
file_put_contents(__DIR__ . '/vnpay_debug_return.txt', json_encode([
    'hashData_raw' => $hashData,
    'vnp_SecureHash_from_vnpay' => $vnp_SecureHash,
    'secureHash_calculated' => $secureHash,
    'hash_match' => ($secureHash === $vnp_SecureHash)
], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) . "\n" . str_repeat('-', 80) . "\n\n", FILE_APPEND);

$vnp_TxnRef = $inputData['vnp_TxnRef'] ?? '';
$vnp_ResponseCode = $inputData['vnp_ResponseCode'] ?? '';
$vnp_TransactionNo = $inputData['vnp_TransactionNo'] ?? '';
$vnp_BankCode = $inputData['vnp_BankCode'] ?? '';
$vnp_PayDate = $inputData['vnp_PayDate'] ?? '';
$vnp_Amount = $inputData['vnp_Amount'] ?? 0;

if ($secureHash !== $vnp_SecureHash) {
    echo json_encode([
        'success' => false,
        'message' => 'Chữ ký không hợp lệ',
        'status' => 'invalid_hash'
    ]);
    mysqli_close($conn);
    exit;
}

// Tìm đơn hàng
// vnp_TxnRef có dạng: DH20251027123456_20251027080312456
// Cần tách lấy mã đơn hàng gốc (phần trước dấu _)
$madonhang_goc = $vnp_TxnRef;
if (strpos($vnp_TxnRef, '_') !== false) {
    $madonhang_goc = explode('_', $vnp_TxnRef)[0];
}

$txnRefEsc = $conn->real_escape_string($madonhang_goc);
$q = mysqli_query($conn, "SELECT * FROM donhang WHERE madonhang = '$txnRefEsc' LIMIT 1");
if (!$q || mysqli_num_rows($q) === 0) {
    // Redirect về app với lỗi không tìm thấy đơn hàng
    $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($madonhang_goc) . "&status=error&message=" . urlencode("Không tìm thấy đơn hàng");
    redirectToApp($deepLink, 'error', $madonhang_goc, 0, '', '', 'Không tìm thấy đơn hàng');
    exit;
}
$order = mysqli_fetch_assoc($q);
$iddonhang = intval($order['id']);

if ($vnp_ResponseCode === '00') {
    // Thanh toán thành công
    mysqli_begin_transaction($conn);
    try {
        // ✅ KIỂM TRA ĐÃ TRỪ TỒN KHO CHƯA - Tránh VNPay callback nhiều lần
        if (intval($order['is_tonkho_updated'] ?? 0) == 1) {
            // Đã trừ tồn kho rồi, chỉ cần commit và redirect
            mysqli_commit($conn);
            $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=success&amount=" . urlencode($vnp_Amount);
            redirectToApp($deepLink, 'success', $vnp_TxnRef, $vnp_Amount, $vnp_TransactionNo, $vnp_BankCode);
            exit;
        }

        $txnNoEsc = $conn->real_escape_string($vnp_TransactionNo);
        $bankCodeEsc = $conn->real_escape_string($vnp_BankCode);
        $payDateEsc = $conn->real_escape_string($vnp_PayDate);

        // ✅ Cập nhật trạng thái đơn hàng - Tồn kho sẽ tự động giảm qua TRIGGER
        $sqlUpdate = "UPDATE donhang SET trangthai = 'Đã thanh toán', vnpay_transaction_no = '$txnNoEsc', vnpay_bank_code = '$bankCodeEsc', vnpay_pay_date = '$payDateEsc', is_tonkho_updated = 1 WHERE id = $iddonhang";
        mysqli_query($conn, $sqlUpdate);

        // ✅ ĐÃ XÓA CODE TRỪ TỒN KHO THỦ CÔNG - Trigger sẽ tự động xử lý khi UPDATE trangthai

        mysqli_commit($conn);

        // Redirect về app qua deep link ngay lập tức
        $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=success&amount=" . urlencode($vnp_Amount);
        redirectToApp($deepLink, 'success', $vnp_TxnRef, $vnp_Amount, $vnp_TransactionNo, $vnp_BankCode);
        exit;

    } catch (Exception $e) {
        mysqli_rollback($conn);

        // Redirect về app với trạng thái lỗi
        $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=error&message=" . urlencode($e->getMessage());
        redirectToApp($deepLink, 'error', $vnp_TxnRef, 0, '', '', $e->getMessage());
        exit;
    }
} else {
    // Thanh toán thất bại hoặc bị hủy
    mysqli_query($conn, "UPDATE donhang SET trangthai = 'Đã hủy' WHERE id = $iddonhang");

    // Redirect về app với trạng thái thất bại
    $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=failed&code=" . urlencode($vnp_ResponseCode);
    redirectToApp($deepLink, 'failed', $vnp_TxnRef, 0, '', '', $vnp_ResponseCode);
    exit;
}

mysqli_close($conn);

// === HÀM REDIRECT VỀ APP ===

function redirectToApp($deepLink, $status, $madonhang, $amount = 0, $transactionNo = '', $bankCode = '', $error = '') {
    // Redirect trực tiếp về app bằng HTTP header
    header("Location: " . $deepLink);
    exit;
}
    ?>
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><?php echo $statusText; ?></title>
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: <?php echo $bgGradient; ?>;
                min-height: 100vh;
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 20px;
            }
            .container {
                background: white;
                border-radius: 20px;
                padding: 40px 30px;
                max-width: 450px;
                width: 100%;
                box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                text-align: center;
                animation: slideUp 0.5s ease-out;
            }
            @keyframes slideUp {
                from {
                    opacity: 0;
                    transform: translateY(30px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }
            .status-icon {
                width: 100px;
                height: 100px;
                background: <?php echo $statusColor; ?>;
                border-radius: 50%;
                margin: 0 auto 25px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 60px;
                color: white;
                animation: scaleIn 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
            }
            @keyframes scaleIn {
                0% { transform: scale(0); }
                50% { transform: scale(1.2); }
                100% { transform: scale(1); }
            }
            h1 {
                color: <?php echo $statusColor; ?>;
                font-size: 26px;
                margin-bottom: 15px;
            }
            .order-info {
                background: #f8f9fa;
                border-radius: 12px;
                padding: 20px;
                margin: 25px 0;
            }
            .info-row {
                display: flex;
                justify-content: space-between;
                padding: 12px 0;
                border-bottom: 1px solid #e9ecef;
                font-size: 14px;
            }
            .info-row:last-child {
                border-bottom: none;
            }
            .info-label {
                color: #6c757d;
                font-weight: 500;
            }
            .info-value {
                color: #212529;
                font-weight: 600;
            }
            .amount {
                color: <?php echo $statusColor; ?>;
                font-size: 22px;
            }
            .btn-container {
                margin-top: 30px;
            }
            .btn {
                background: #667eea;
                color: white;
                border: none;
                padding: 16px 50px;
                border-radius: 30px;
                font-size: 16px;
                font-weight: 600;
                cursor: pointer;
                text-decoration: none;
                display: inline-block;
                transition: all 0.3s;
                box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                margin: 5px;
            }
            .btn:hover {
                background: #5568d3;
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
            }
            .btn:active {
                transform: translateY(0);
            }
            .btn-secondary {
                background: #6c757d;
                box-shadow: 0 4px 15px rgba(108, 117, 125, 0.4);
            }
            .btn-secondary:hover {
                background: #5a6268;
                box-shadow: 0 6px 20px rgba(108, 117, 125, 0.6);
            }
            .countdown {
                color: #6c757d;
                font-size: 13px;
                margin-top: 15px;
            }
            .error-msg {
                background: #fff3f3;
                border-left: 4px solid #f44336;
                padding: 15px;
                margin: 20px 0;
                text-align: left;
                border-radius: 8px;
                color: #721c24;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="status-icon"><?php echo $statusIcon; ?></div>
            <h1><?php echo $statusText; ?></h1>

            <?php if ($status === 'success'): ?>
                <p style="color: #6c757d; margin-bottom: 5px;">Đơn hàng đã được xử lý thành công</p>
                <div class="order-info">
                    <div class="info-row">
                        <span class="info-label">Mã đơn hàng:</span>
                        <span class="info-value"><?php echo htmlspecialchars($madonhang); ?></span>
                    </div>
                    <?php if ($amount > 0): ?>
                    <div class="info-row">
                        <span class="info-label">Số tiền:</span>
                        <span class="info-value amount"><?php echo $amountVND; ?> đ</span>
                    </div>
                    <?php endif; ?>
                    <?php if ($transactionNo): ?>
                    <div class="info-row">
                        <span class="info-label">Mã giao dịch:</span>
                        <span class="info-value"><?php echo htmlspecialchars($transactionNo); ?></span>
                    </div>
                    <?php endif; ?>
                    <?php if ($bankCode): ?>
                    <div class="info-row">
                        <span class="info-label">Ngân hàng:</span>
                        <span class="info-value"><?php echo htmlspecialchars($bankCode); ?></span>
                    </div>
                    <?php endif; ?>
                </div>
            <?php elseif ($status === 'failed'): ?>
                <div class="error-msg">
                    <strong>Lý do:</strong> <?php echo htmlspecialchars($error); ?><br>
                    <small style="color: #999; margin-top: 5px; display: block;">Mã đơn hàng: <?php echo htmlspecialchars($madonhang); ?></small>
                </div>
                <p style="color: #6c757d; font-size: 14px;">Vui lòng thử lại hoặc chọn phương thức thanh toán khác</p>
            <?php else: ?>
                <div class="error-msg">
                    <?php echo htmlspecialchars($error); ?><br>
                    <small style="color: #999; margin-top: 5px; display: block;">Mã đơn hàng: <?php echo htmlspecialchars($madonhang); ?></small>
                </div>
            <?php endif; ?>

            <div class="btn-container">
                <a href="<?php echo htmlspecialchars($deepLink); ?>" class="btn" id="openAppBtn">
                    📱 Mở ứng dụng
                </a>
                <br>
                <button class="btn btn-secondary" onclick="window.close()">Đóng trang</button>
            </div>

            <p class="countdown" id="countdown">Tự động mở app sau <strong><span id="timer">3</span></strong> giây...</p>
        </div>

        <script>
            // Deep link để mở app
            const deepLink = <?php echo json_encode($deepLink); ?>;

            // Countdown và tự động redirect
            let seconds = 3;
            const timerElement = document.getElementById('timer');
            const countdownElement = document.getElementById('countdown');

            const countdown = setInterval(() => {
                seconds--;
                if (timerElement) {
                    timerElement.textContent = seconds;
                }

                if (seconds <= 0) {
                    clearInterval(countdown);
                    if (countdownElement) {
                        countdownElement.innerHTML = '<strong>Đang mở ứng dụng...</strong>';
                    }
                    openApp();
                }
            }, 1000);

            // Hàm mở app
            function openApp() {
                // Thử mở deep link
                window.location.href = deepLink;

                // Fallback: Nếu sau 2 giây không mở được, hiển thị thông báo
                setTimeout(() => {
                    if (countdownElement) {
                        countdownElement.innerHTML = 'Không thể tự động mở app? <br><small>Vui lòng nhấn nút "Mở ứng dụng" hoặc mở app thủ công</small>';
                    }
                }, 2000);
            }

            // Click vào nút mở app
            document.getElementById('openAppBtn').addEventListener('click', (e) => {
                e.preventDefault();
                openApp();
            });

            // Thử mở app ngay khi trang load (backup)
            setTimeout(() => {
                const iframe = document.createElement('iframe');
                iframe.style.display = 'none';
                iframe.src = deepLink;
                document.body.appendChild(iframe);

                setTimeout(() => {
                    document.body.removeChild(iframe);
                }, 1000);
            }, 500);
        </script>
    </body>
    </html>
    <?php
    exit;
}

// === CÁC HÀM HIỂN THỊ TRANG KẾT QUẢ ===

function showSuccessPage($madonhang, $amount, $transactionNo, $bankCode) {
    $amountVND = number_format($amount / 100, 0, ',', '.');
    ?>
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Thanh toán thành công</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                margin: 0;
                padding: 20px;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
            }
            .container {
                background: white;
                border-radius: 15px;
                padding: 40px;
                max-width: 500px;
                width: 100%;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                text-align: center;
            }
            .success-icon {
                width: 80px;
                height: 80px;
                background: #4caf50;
                border-radius: 50%;
                margin: 0 auto 20px;
                display: flex;
                align-items: center;
                justify-content: center;
                animation: scaleIn 0.5s ease-out;
            }
            .success-icon::after {
                content: "✓";
                color: white;
                font-size: 50px;
                font-weight: bold;
            }
            h1 {
                color: #4caf50;
                margin: 0 0 10px 0;
                font-size: 28px;
            }
            .info {
                background: #f5f5f5;
                border-radius: 10px;
                padding: 20px;
                margin: 20px 0;
                text-align: left;
            }
            .info-row {
                display: flex;
                justify-content: space-between;
                padding: 10px 0;
                border-bottom: 1px solid #e0e0e0;
            }
            .info-row:last-child {
                border-bottom: none;
            }
            .info-label {
                color: #666;
                font-weight: 500;
            }
            .info-value {
                color: #333;
                font-weight: bold;
            }
            .amount {
                color: #4caf50;
                font-size: 24px;
                font-weight: bold;
            }
            .btn {
                background: #667eea;
                color: white;
                border: none;
                padding: 15px 40px;
                border-radius: 25px;
                font-size: 16px;
                cursor: pointer;
                margin-top: 20px;
                text-decoration: none;
                display: inline-block;
                transition: background 0.3s;
            }
            .btn:hover {
                background: #5568d3;
            }
            @keyframes scaleIn {
                from {
                    transform: scale(0);
                }
                to {
                    transform: scale(1);
                }
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="success-icon"></div>
            <h1>Thanh toán thành công!</h1>
            <p style="color: #666; margin: 10px 0 20px 0;">Đơn hàng của bạn đã được thanh toán thành công</p>

            <div class="info">
                <div class="info-row">
                    <span class="info-label">Mã đơn hàng:</span>
                    <span class="info-value"><?php echo htmlspecialchars($madonhang); ?></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Số tiền:</span>
                    <span class="info-value amount"><?php echo $amountVND; ?> đ</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Mã giao dịch:</span>
                    <span class="info-value"><?php echo htmlspecialchars($transactionNo); ?></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Ngân hàng:</span>
                    <span class="info-value"><?php echo htmlspecialchars($bankCode); ?></span>
                </div>
            </div>

            <p style="color: #888; font-size: 14px;">Bạn có thể đóng trang này và quay lại ứng dụng để xem chi tiết đơn hàng</p>

            <button class="btn" onclick="window.close()">Đóng trang</button>
        </div>
    </body>
    </html>
    <?php
}

function showFailedPage($madonhang, $responseCode) {
    $errorMessages = [
        '07' => 'Giao dịch bị nghi ngờ gian lận',
        '09' => 'Thẻ chưa đăng ký dịch vụ Internet Banking',
        '10' => 'Xác thực thông tin không chính xác quá số lần quy định',
        '11' => 'Hết thời gian chờ thanh toán',
        '12' => 'Thẻ bị khóa',
        '13' => 'Sai mật khẩu xác thực giao dịch',
        '24' => 'Giao dịch bị hủy',
        '51' => 'Tài khoản không đủ số dư',
        '65' => 'Tài khoản đã vượt quá hạn mức giao dịch trong ngày',
        '75' => 'Ngân hàng thanh toán đang bảo trì',
        '79' => 'Nhập sai mật khẩu quá số lần quy định'
    ];

    $errorMsg = $errorMessages[$responseCode] ?? 'Giao dịch không thành công';
    ?>
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Thanh toán thất bại</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                margin: 0;
                padding: 20px;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
            }
            .container {
                background: white;
                border-radius: 15px;
                padding: 40px;
                max-width: 500px;
                width: 100%;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                text-align: center;
            }
            .error-icon {
                width: 80px;
                height: 80px;
                background: #f44336;
                border-radius: 50%;
                margin: 0 auto 20px;
                display: flex;
                align-items: center;
                justify-content: center;
                animation: shake 0.5s;
            }
            .error-icon::after {
                content: "✕";
                color: white;
                font-size: 50px;
                font-weight: bold;
            }
            h1 {
                color: #f44336;
                margin: 0 0 10px 0;
                font-size: 28px;
            }
            .error-msg {
                background: #fff3f3;
                border-left: 4px solid #f44336;
                padding: 15px;
                margin: 20px 0;
                text-align: left;
                border-radius: 5px;
            }
            .btn {
                background: #667eea;
                color: white;
                border: none;
                padding: 15px 40px;
                border-radius: 25px;
                font-size: 16px;
                cursor: pointer;
                margin-top: 20px;
                text-decoration: none;
                display: inline-block;
                transition: background 0.3s;
            }
            .btn:hover {
                background: #5568d3;
            }
            @keyframes shake {
                0%, 100% { transform: translateX(0); }
                25% { transform: translateX(-10px); }
                75% { transform: translateX(10px); }
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="error-icon"></div>
            <h1>Thanh toán thất bại!</h1>
            <p style="color: #666; margin: 10px 0 20px 0;">Mã đơn hàng: <strong><?php echo htmlspecialchars($madonhang); ?></strong></p>

            <div class="error-msg">
                <strong>Lý do:</strong> <?php echo $errorMsg; ?><br>
                <small style="color: #999;">Mã lỗi: <?php echo htmlspecialchars($responseCode); ?></small>
            </div>

            <p style="color: #888; font-size: 14px;">Vui lòng thử lại hoặc chọn phương thức thanh toán khác</p>

            <button class="btn" onclick="window.close()">Đóng trang</button>
        </div>
    </body>
    </html>
    <?php
}

function showErrorPage($madonhang, $error) {
    ?>
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Lỗi hệ thống</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                margin: 0;
                padding: 20px;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
            }
            .container {
                background: white;
                border-radius: 15px;
                padding: 40px;
                max-width: 500px;
                width: 100%;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                text-align: center;
            }
            h1 {
                color: #ff9800;
                margin: 20px 0 10px 0;
                font-size: 24px;
            }
            .btn {
                background: #667eea;
                color: white;
                border: none;
                padding: 15px 40px;
                border-radius: 25px;
                font-size: 16px;
                cursor: pointer;
                margin-top: 20px;
                text-decoration: none;
                display: inline-block;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>⚠️ Lỗi hệ thống</h1>
            <p>Mã đơn hàng: <strong><?php echo htmlspecialchars($madonhang); ?></strong></p>
            <p style="color: #f44336;"><?php echo htmlspecialchars($error); ?></p>
            <p style="color: #888; font-size: 14px;">Vui lòng liên hệ bộ phận hỗ trợ</p>
            <button class="btn" onclick="window.close()">Đóng trang</button>
        </div>
    </body>
    </html>
    <?php
}
?>
