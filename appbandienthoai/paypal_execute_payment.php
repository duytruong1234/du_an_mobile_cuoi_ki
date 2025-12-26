<?php
include "connect.php";
include "paypal_config.php";

header('Content-Type: application/json');

$madonhang = isset($_POST['madonhang']) ? $_POST['madonhang'] : '';
$paymentId = isset($_POST['paymentId']) ? $_POST['paymentId'] : '';
$payerId = isset($_POST['payerId']) ? $_POST['payerId'] : '';

if (empty($madonhang) || empty($paymentId) || empty($payerId)) {
    echo json_encode([
        'success' => false,
        'message' => 'Thiếu thông tin thanh toán'
    ]);
    exit;
}

try {
    // Lấy thông tin đơn hàng
    $stmt = $conn->prepare("SELECT * FROM donhang WHERE madonhang = ?");
    $stmt->bind_param("s", $madonhang);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows == 0) {
        throw new Exception("Không tìm thấy đơn hàng");
    }

    $order = $result->fetch_assoc();
    $stmt->close();

    // Lấy access token
    $accessToken = getPayPalAccessToken();

    if (!$accessToken) {
        throw new Exception("Không thể kết nối với PayPal");
    }

    // Capture payment
    $ch = curl_init(PAYPAL_API_URL . '/v2/checkout/orders/' . $paymentId . '/capture');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Authorization: Bearer ' . $accessToken
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode != 201) {
        throw new Exception("Lỗi xác nhận thanh toán PayPal: " . $response);
    }

    $captureData = json_decode($response, true);

    // Kiểm tra trạng thái thanh toán
    if ($captureData['status'] == 'COMPLETED') {
        // ✅ SỬA: Đổi trạng thái thành "Đã thanh toán PayPal" giống VNPay
        $stmt = $conn->prepare("UPDATE donhang SET trangthai = 'Đã thanh toán PayPal', paypal_payer_id = ?, paypal_payment_date = NOW() WHERE madonhang = ?");
        $stmt->bind_param("ss", $payerId, $madonhang);
        $stmt->execute();
        $stmt->close();

        // ✅ KHÔNG CẦN TRỪ TỒN KHO Ở ĐÂY - TRIGGER đã tự động xử lý khi INSERT chitietdonhang
        // Trigger 'giam_tonkho_khi_dat' đã giảm tồn kho tự động khi tạo đơn hàng

        echo json_encode([
            'success' => true,
            'message' => 'Thanh toán PayPal thành công',
            'data' => [
                'iddonhang' => $order['id'],
                'madonhang' => $madonhang,
                'trangthai' => 'Đã thanh toán PayPal',
                'paypal_order_id' => $paymentId,
                'paypal_payer_id' => $payerId,
                'paypal_payment_date' => date('Y-m-d H:i:s')
            ]
        ]);
    } else {
        throw new Exception("Thanh toán chưa hoàn tất");
    }

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
}

function getPayPalAccessToken() {
    $ch = curl_init(PAYPAL_API_URL . '/v1/oauth2/token');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, 'grant_type=client_credentials');
    curl_setopt($ch, CURLOPT_USERPWD, PAYPAL_CLIENT_ID . ':' . PAYPAL_CLIENT_SECRET);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Accept: application/json',
        'Accept-Language: en_US'
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode != 200) {
        return false;
    }

    $data = json_decode($response, true);
    return isset($data['access_token']) ? $data['access_token'] : false;
}
?>

