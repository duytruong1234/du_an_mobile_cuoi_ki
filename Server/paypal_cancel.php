<?php
include "connect.php";

// Get parameters từ URL
$madonhang = isset($_GET['madonhang']) ? $_GET['madonhang'] : '';

if (!empty($madonhang)) {
    // Cập nhật trạng thái đơn hàng bị hủy
    $stmt = $conn->prepare("UPDATE donhang SET trangthai = 'da_huy' WHERE madonhang = ?");
    $stmt->bind_param("s", $madonhang);
    $stmt->execute();
    $stmt->close();
}

// Redirect về app
$deeplink = "appbandienthoai://payment/paypal?madonhang=" . urlencode($madonhang) . "&status=cancelled";
header("Location: " . $deeplink);
exit;
?>

