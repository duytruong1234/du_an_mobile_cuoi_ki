<?php
include "connect.php";

// Get parameters từ URL
$madonhang = isset($_GET['madonhang']) ? $_GET['madonhang'] : '';
$paymentId = isset($_GET['token']) ? $_GET['token'] : '';
$payerId = isset($_GET['PayerID']) ? $_GET['PayerID'] : '';

if (empty($madonhang) || empty($paymentId) || empty($payerId)) {
    echo "Thiếu thông tin thanh toán";
    exit;
}

// Redirect về app với deep link
$deeplink = "appbandienthoai://payment/paypal?madonhang=" . urlencode($madonhang) .
            "&paymentId=" . urlencode($paymentId) .
            "&PayerID=" . urlencode($payerId) .
            "&status=success";

header("Location: " . $deeplink);
exit;
?>

