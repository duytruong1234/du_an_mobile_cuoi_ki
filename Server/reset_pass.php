<?php
include "connect.php";
include "email_config.php";

// Kiểm tra method POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
    exit;
}

// Kiểm tra dữ liệu đầu vào
if (!isset($_POST['email']) || empty($_POST['email'])) {
    echo json_encode([
        'success' => false,
        'message' => 'Vui lòng nhập email'
    ]);
    exit;
}

$email = mysqli_real_escape_string($conn, $_POST['email']);

// Kiểm tra email có tồn tại trong hệ thống không
$query = "SELECT * FROM user WHERE email = '$email'";
$result = mysqli_query($conn, $query);

if (!$result || mysqli_num_rows($result) == 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Email không tồn tại trong hệ thống'
    ]);
    exit;
}

$user = mysqli_fetch_assoc($result);

// Kiểm tra nếu là tài khoản Google (mobile = '0000000000')
if ($user['mobile'] === '0000000000') {
    echo json_encode([
        'success' => false,
        'message' => 'Tài khoản Google không thể đổi mật khẩu. Vui lòng đăng nhập bằng Google.'
    ]);
    exit;
}

// Tạo mã OTP
$otp = generateOTP();
$expiry = date('Y-m-d H:i:s', strtotime('+5 minutes')); // OTP có hiệu lực 5 phút

// Lưu OTP vào database
$updateQuery = "UPDATE user SET reset_otp = '$otp', reset_otp_expiry = '$expiry' WHERE email = '$email'";
if (!mysqli_query($conn, $updateQuery)) {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi hệ thống, vui lòng thử lại'
    ]);
    exit;
}

// Gửi email OTP
$emailSent = sendOTPEmail($email, $otp, $user['username']);

if ($emailSent) {
    echo json_encode([
        'success' => true,
        'message' => 'Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.',
        'result' => [[
            'email' => $email,
            'otp_expiry' => '5 phút'
        ]]
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Không thể gửi email. Vui lòng kiểm tra lại email hoặc thử lại sau.',
        'result' => []
    ]);
}

mysqli_close($conn);
?>
