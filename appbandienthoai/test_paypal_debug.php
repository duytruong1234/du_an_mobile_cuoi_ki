<?php
// File test để debug lỗi PayPal HTTP 500
error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "<h2>TEST PAYPAL API - DEBUG</h2>";

// Test 1: Kiểm tra file connect.php
echo "<h3>1. Test Database Connection</h3>";
if (file_exists("connect.php")) {
    echo "✅ File connect.php tồn tại<br>";
    include "connect.php";
    if (isset($conn) && $conn->ping()) {
        echo "✅ Kết nối database thành công<br>";
        echo "Database: " . $dbname . "<br>";
    } else {
        echo "❌ Không kết nối được database<br>";
    }
} else {
    echo "❌ File connect.php KHÔNG tồn tại!<br>";
}

// Test 2: Kiểm tra file paypal_config.php
echo "<h3>2. Test PayPal Config</h3>";
if (file_exists("paypal_config.php")) {
    echo "✅ File paypal_config.php tồn tại<br>";
    include "paypal_config.php";

    if (defined('PAYPAL_CLIENT_ID')) {
        echo "✅ PAYPAL_CLIENT_ID: " . substr(PAYPAL_CLIENT_ID, 0, 20) . "...<br>";
    } else {
        echo "❌ PAYPAL_CLIENT_ID chưa được define<br>";
    }

    if (defined('PAYPAL_CLIENT_SECRET')) {
        echo "✅ PAYPAL_CLIENT_SECRET: " . substr(PAYPAL_CLIENT_SECRET, 0, 20) . "...<br>";
    } else {
        echo "❌ PAYPAL_CLIENT_SECRET chưa được define<br>";
    }

    if (defined('PAYPAL_API_URL')) {
        echo "✅ PAYPAL_API_URL: " . PAYPAL_API_URL . "<br>";
    }

    if (defined('PAYPAL_RETURN_URL')) {
        echo "✅ PAYPAL_RETURN_URL: " . PAYPAL_RETURN_URL . "<br>";
    }
} else {
    echo "❌ File paypal_config.php KHÔNG tồn tại!<br>";
}

// Test 3: Kiểm tra file paypal_create_payment.php
echo "<h3>3. Test PayPal Create Payment File</h3>";
if (file_exists("paypal_create_payment.php")) {
    echo "✅ File paypal_create_payment.php tồn tại<br>";
    echo "Kích thước: " . filesize("paypal_create_payment.php") . " bytes<br>";
} else {
    echo "❌ File paypal_create_payment.php KHÔNG tồn tại!<br>";
}

// Test 4: Test cURL
echo "<h3>4. Test cURL Extension</h3>";
if (function_exists('curl_version')) {
    $version = curl_version();
    echo "✅ cURL đã được cài đặt<br>";
    echo "Version: " . $version['version'] . "<br>";
} else {
    echo "❌ cURL CHƯA được cài đặt - cần thiết cho PayPal API!<br>";
}

// Test 5: Thử gọi API PayPal (test connection)
echo "<h3>5. Test PayPal API Connection</h3>";
if (defined('PAYPAL_API_URL') && defined('PAYPAL_CLIENT_ID') && defined('PAYPAL_CLIENT_SECRET')) {
    $ch = curl_init(PAYPAL_API_URL . '/v1/oauth2/token');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, 'grant_type=client_credentials');
    curl_setopt($ch, CURLOPT_USERPWD, PAYPAL_CLIENT_ID . ':' . PAYPAL_CLIENT_SECRET);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Accept: application/json',
        'Accept-Language: en_US'
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);

    echo "HTTP Code: " . $httpCode . "<br>";

    if ($httpCode == 200) {
        echo "✅ Kết nối PayPal API thành công!<br>";
        $data = json_decode($response, true);
        if (isset($data['access_token'])) {
            echo "✅ Access Token: " . substr($data['access_token'], 0, 30) . "...<br>";
        }
    } else {
        echo "❌ Lỗi kết nối PayPal API<br>";
        echo "Response: " . htmlspecialchars(substr($response, 0, 200)) . "<br>";
        if ($error) {
            echo "cURL Error: " . $error . "<br>";
        }
    }
} else {
    echo "⚠️ Chưa có đủ thông tin để test PayPal API<br>";
}

echo "<h3>6. PHP Info</h3>";
echo "PHP Version: " . phpversion() . "<br>";
echo "Error Reporting: " . error_reporting() . "<br>";
echo "Display Errors: " . ini_get('display_errors') . "<br>";

echo "<hr>";
echo "<h3>KẾT LUẬN</h3>";
echo "Nếu tất cả test đều PASS (✅), vấn đề có thể là ở code xử lý request.<br>";
echo "Nếu có test FAIL (❌), cần sửa phần đó trước.<br>";
?>

