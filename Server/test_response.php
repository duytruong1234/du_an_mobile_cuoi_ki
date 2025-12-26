<?php
// Test xem response thực tế là gì
ob_start();
include "connect.php";
include "paypal_config.php";
$buffer = ob_get_clean();

header('Content-Type: text/plain');

echo "=== KIỂM TRA OUTPUT BUFFER ===\n";
echo "Độ dài buffer: " . strlen($buffer) . " bytes\n";
echo "Buffer content (hex): " . bin2hex($buffer) . "\n";
echo "Buffer content (raw): [" . $buffer . "]\n\n";

echo "=== KIỂM TRA HEADERS ===\n";
print_r(headers_list());

echo "\n=== TEST JSON RESPONSE ===\n";
ob_start();
include "connect.php";
include "paypal_config.php";
ob_end_clean();

header('Content-Type: application/json');
$testResponse = [
    'success' => true,
    'message' => 'Test response',
    'test' => 'OK'
];
echo json_encode($testResponse);
?>

