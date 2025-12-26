<?php
header('Content-Type: application/json; charset=utf-8');

// Test đơn giản - chỉ trả về JSON
echo json_encode([
    'success' => true,
    'message' => 'API PayPal hoạt động!',
    'test' => 'OK'
]);
exit;
?>

