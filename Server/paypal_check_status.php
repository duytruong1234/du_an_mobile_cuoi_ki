<?php
include "connect.php";

header('Content-Type: application/json');

$madonhang = isset($_POST['madonhang']) ? $_POST['madonhang'] : '';

if (empty($madonhang)) {
    echo json_encode([
        'success' => false,
        'message' => 'Thiếu mã đơn hàng'
    ]);
    exit;
}

try {
    $stmt = $conn->prepare("SELECT * FROM donhang WHERE madonhang = ?");
    $stmt->bind_param("s", $madonhang);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows == 0) {
        throw new Exception("Không tìm thấy đơn hàng");
    }

    $order = $result->fetch_assoc();
    $stmt->close();

    echo json_encode([
        'success' => true,
        'message' => 'Lấy trạng thái đơn hàng thành công',
        'data' => [
            'iddonhang' => $order['id'],
            'madonhang' => $order['madonhang'],
            'trangthai' => $order['trangthai'],
            'paypal_order_id' => isset($order['paypal_order_id']) ? $order['paypal_order_id'] : '',
            'paypal_payer_id' => isset($order['paypal_payer_id']) ? $order['paypal_payer_id'] : '',
            'paypal_payment_date' => isset($order['paypal_payment_date']) ? $order['paypal_payment_date'] : ''
        ]
    ]);

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
}
?>

