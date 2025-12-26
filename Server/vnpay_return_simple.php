<?php
/**
 * vnpay_return_simple.php
 * Redirect trực tiếp về app sau khi thanh toán VNPay
 */

require_once __DIR__ . '/connect.php';
require_once __DIR__ . '/vnpay_config.php';

// Thu thập các tham số bắt đầu bằng vnp_
$inputData = array();
foreach ($_GET as $key => $value) {
    if (substr($key, 0, 4) === 'vnp_') {
        $inputData[$key] = $value;
    }
}

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

$vnp_TxnRef = $inputData['vnp_TxnRef'] ?? '';
$vnp_ResponseCode = $inputData['vnp_ResponseCode'] ?? '';
$vnp_TransactionNo = $inputData['vnp_TransactionNo'] ?? '';
$vnp_BankCode = $inputData['vnp_BankCode'] ?? '';
$vnp_PayDate = $inputData['vnp_PayDate'] ?? '';
$vnp_Amount = $inputData['vnp_Amount'] ?? 0;

// Kiểm tra chữ ký
if ($secureHash !== $vnp_SecureHash) {
    $deepLink = "appbandienthoai://payment_return?status=error&message=" . urlencode("Chữ ký không hợp lệ");
    redirectToApp($deepLink);
    exit;
}

// Tìm đơn hàng
$madonhang_goc = $vnp_TxnRef;
if (strpos($vnp_TxnRef, '_') !== false) {
    $madonhang_goc = explode('_', $vnp_TxnRef)[0];
}

$txnRefEsc = $conn->real_escape_string($madonhang_goc);
$q = mysqli_query($conn, "SELECT * FROM donhang WHERE madonhang = '$txnRefEsc' LIMIT 1");
if (!$q || mysqli_num_rows($q) === 0) {
    $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($madonhang_goc) . "&status=error&message=" . urlencode("Không tìm thấy đơn hàng");
    redirectToApp($deepLink);
    exit;
}
$order = mysqli_fetch_assoc($q);
$iddonhang = intval($order['id']);

if ($vnp_ResponseCode === '00') {
    // Thanh toán thành công
    mysqli_begin_transaction($conn);
    try {
        $txnNoEsc = $conn->real_escape_string($vnp_TransactionNo);
        $bankCodeEsc = $conn->real_escape_string($vnp_BankCode);
        $payDateEsc = $conn->real_escape_string($vnp_PayDate);

        $sqlUpdate = "UPDATE donhang SET trangthai = 'Đã thanh toán', vnpay_transaction_no = '$txnNoEsc', vnpay_bank_code = '$bankCodeEsc', vnpay_pay_date = '$payDateEsc' WHERE id = $iddonhang";
        mysqli_query($conn, $sqlUpdate);

        // Giảm tồn kho
        $resDetails = mysqli_query($conn, "SELECT idsp, soluong FROM chitietdonhang WHERE iddonhang = $iddonhang");
        while ($detail = mysqli_fetch_assoc($resDetails)) {
            $idsp = intval($detail['idsp']);
            $sl = intval($detail['soluong']);
            mysqli_query($conn, "UPDATE sanphammoi SET soluongtonkho = GREATEST(0, soluongtonkho - $sl) WHERE id = $idsp");
        }

        mysqli_commit($conn);

        $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=success&amount=" . urlencode($vnp_Amount);
        redirectToApp($deepLink);
        exit;

    } catch (Exception $e) {
        mysqli_rollback($conn);
        $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=error&message=" . urlencode($e->getMessage());
        redirectToApp($deepLink);
        exit;
    }
} else {
    // Thanh toán thất bại
    mysqli_query($conn, "UPDATE donhang SET trangthai = 'Đã hủy' WHERE id = $iddonhang");
    $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=failed&code=" . urlencode($vnp_ResponseCode);
    redirectToApp($deepLink);
    exit;
}

mysqli_close($conn);

function redirectToApp($deepLink) {
    // Thử redirect bằng HTTP header trước
    header("Location: " . $deepLink);

    // Nếu header không work, hiển thị trang HTML với meta refresh và nút manual
    ?>
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="refresh" content="0;url=<?php echo htmlspecialchars($deepLink); ?>">
        <title>Thanh toán thành công</title>
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            body {
                margin: 0;
                padding: 20px;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            .container {
                background: white;
                border-radius: 20px;
                padding: 40px 30px;
                max-width: 400px;
                width: 100%;
                text-align: center;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
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
            .success-icon {
                width: 80px;
                height: 80px;
                background: #4caf50;
                border-radius: 50%;
                margin: 0 auto 20px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 50px;
                color: white;
                animation: scaleIn 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
            }
            @keyframes scaleIn {
                0% { transform: scale(0); }
                50% { transform: scale(1.2); }
                100% { transform: scale(1); }
            }
            h1 {
                color: #4caf50;
                font-size: 24px;
                margin-bottom: 10px;
            }
            p {
                color: #666;
                font-size: 15px;
                margin-bottom: 30px;
                line-height: 1.6;
            }
            .btn {
                background: #667eea;
                color: white;
                border: none;
                padding: 16px 40px;
                border-radius: 30px;
                font-size: 16px;
                font-weight: 600;
                cursor: pointer;
                text-decoration: none;
                display: inline-block;
                transition: all 0.3s;
                box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
            }
            .btn:hover {
                background: #5568d3;
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
            }
            .btn:active {
                transform: translateY(0);
            }
            .note {
                margin-top: 20px;
                font-size: 13px;
                color: #999;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="success-icon">✓</div>
            <h1>Thanh toán thành công!</h1>
            <p>Đơn hàng của bạn đã được xử lý thành công.<br>Đang chuyển về ứng dụng...</p>
            <a href="<?php echo htmlspecialchars($deepLink); ?>" class="btn" id="openAppBtn">
                📱 Mở ứng dụng ngay
            </a>
            <p class="note">Nếu ứng dụng không tự động mở,<br>vui lòng nhấn nút bên trên</p>
        </div>

        <script>
            const deepLink = <?php echo json_encode($deepLink); ?>;

            // Thử nhiều cách để mở app
            function attemptOpenApp() {
                // Cách 1: window.location
                try {
                    window.location.href = deepLink;
                } catch(e) {}

                // Cách 2: window.open
                setTimeout(() => {
                    try {
                        window.open(deepLink, '_self');
                    } catch(e) {}
                }, 100);

                // Cách 3: Iframe hidden
                setTimeout(() => {
                    try {
                        const iframe = document.createElement('iframe');
                        iframe.style.display = 'none';
                        iframe.src = deepLink;
                        document.body.appendChild(iframe);
                    } catch(e) {}
                }, 200);

                // Cách 4: Click link tự động
                setTimeout(() => {
                    try {
                        document.getElementById('openAppBtn').click();
                    } catch(e) {}
                }, 300);
            }

            // Chạy ngay khi trang load
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', attemptOpenApp);
            } else {
                attemptOpenApp();
            }

            // Manual click
            document.getElementById('openAppBtn').addEventListener('click', function(e) {
                e.preventDefault();
                window.location.href = deepLink;
            });
        </script>
    </body>
    </html>
    <?php
    exit;
}
?>

