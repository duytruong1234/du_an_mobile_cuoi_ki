<?php
// File test để kiểm tra PayPal API có hoạt động không
header('Content-Type: application/json');

echo json_encode([
    'test' => 'PayPal API Test',
    'timestamp' => date('Y-m-d H:i:s'),
    'files' => [
        'paypal_config.php' => file_exists('paypal_config.php') ? 'EXISTS' : 'NOT FOUND',
        'paypal_create_payment.php' => file_exists('paypal_create_payment.php') ? 'EXISTS' : 'NOT FOUND',
        'paypal_execute_payment.php' => file_exists('paypal_execute_payment.php') ? 'EXISTS' : 'NOT FOUND',
        'paypal_check_status.php' => file_exists('paypal_check_status.php') ? 'EXISTS' : 'NOT FOUND',
        'paypal_return.php' => file_exists('paypal_return.php') ? 'EXISTS' : 'NOT FOUND',
        'paypal_cancel.php' => file_exists('paypal_cancel.php') ? 'EXISTS' : 'NOT FOUND'
    ],
    'post_data' => $_POST,
    'server_info' => [
        'curl_enabled' => function_exists('curl_init') ? 'YES' : 'NO',
        'php_version' => PHP_VERSION
    ]
]);
?>

