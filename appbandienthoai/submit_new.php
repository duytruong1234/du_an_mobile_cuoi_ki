<?php
// Set HTML content type
header('Content-Type: text/html; charset=utf-8');

include "connect.php";

// Enable error reporting for debugging
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

$message = '';
$success = false;

if (isset($_POST['email']) && isset($_POST['password']) && isset($_POST['token'])) {
    $email = trim($_POST['email']);
    $password = trim($_POST['password']);
    $token = trim($_POST['token']);
    
    // Validate inputs
    if (empty($email) || empty($password) || empty($token)) {
        $message = 'Vui lòng điền đầy đủ thông tin.';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $message = 'Email không hợp lệ.';
    } elseif (strlen($password) < 6) {
        $message = 'Mật khẩu phải có ít nhất 6 ký tự.';
    } else {
        // Verify token is valid using prepared statement
        $stmt = $conn->prepare("SELECT email FROM user WHERE email = ? AND pass = ?");
        if ($stmt === false) {
            $message = 'Lỗi hệ thống. Vui lòng thử lại sau.';
        } else {
            $stmt->bind_param("ss", $email, $token);
            $stmt->execute();
            $result = $stmt->get_result();
            
            if ($result->num_rows === 1) {
                // Token valid, update password
                $hashed_password = md5($password);
                
                $update_stmt = $conn->prepare("UPDATE user SET pass = ? WHERE email = ?");
                if ($update_stmt === false) {
                    $message = 'Lỗi hệ thống. Vui lòng thử lại sau.';
                } else {
                    $update_stmt->bind_param("ss", $hashed_password, $email);
                    
                    if ($update_stmt->execute()) {
                        $success = true;
                        $message = 'Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.';
                    } else {
                        $message = 'Lỗi khi cập nhật mật khẩu: ' . htmlspecialchars($conn->error);
                    }
                    $update_stmt->close();
                }
            } else {
                $message = 'Token không hợp lệ hoặc đã hết hạn.';
            }
            $stmt->close();
        }
    }
} else {
    $message = 'Thiếu thông tin. Vui lòng thử lại.';
}
?>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết quả đặt lại mật khẩu</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            max-width: 400px;
            text-align: center;
        }
        .success {
            color: #28a745;
            font-size: 18px;
            margin-bottom: 20px;
        }
        .error {
            color: #dc3545;
            font-size: 18px;
            margin-bottom: 20px;
        }
        .icon {
            font-size: 48px;
            margin-bottom: 20px;
        }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            background-color: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            margin-top: 10px;
        }
        .btn:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
    <div class="container">
        <?php if ($success): ?>
            <div class="icon">✅</div>
            <div class="success"><?php echo htmlspecialchars($message); ?></div>
            <a href="dangnhap.php" class="btn">Đăng nhập ngay</a>
        <?php else: ?>
            <div class="icon">❌</div>
            <div class="error"><?php echo htmlspecialchars($message); ?></div>
            <a href="javascript:history.back()" class="btn">Quay lại</a>
        <?php endif; ?>
    </div>
</body>
</html>
