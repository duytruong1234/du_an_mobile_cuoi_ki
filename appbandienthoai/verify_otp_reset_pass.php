<?php
include "connect.php";

// Kiểm tra method POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
    exit;
}

// Kiểm tra dữ liệu đầu vào
if (!isset($_POST['email']) || empty($_POST['email']) ||
    !isset($_POST['otp']) || empty($_POST['otp']) ||
    !isset($_POST['new_password']) || empty($_POST['new_password'])) {
    echo json_encode([
        'success' => false,
        'message' => 'Vui lòng nhập đầy đủ thông tin'
    ]);
    exit;
}

$email = mysqli_real_escape_string($conn, $_POST['email']);
$otp = mysqli_real_escape_string($conn, $_POST['otp']);
$new_password = mysqli_real_escape_string($conn, $_POST['new_password']);

// Kiểm tra email và OTP
$query = "SELECT * FROM user WHERE email = '$email'";
$result = mysqli_query($conn, $query);

if (!$result || mysqli_num_rows($result) == 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Email không tồn tại'
    ]);
    exit;
}

$user = mysqli_fetch_assoc($result);

// Kiểm tra OTP có tồn tại không
if (empty($user['reset_otp']) || empty($user['reset_otp_expiry'])) {
    echo json_encode([
        'success' => false,
        'message' => 'Không tìm thấy mã OTP. Vui lòng yêu cầu gửi lại mã.'
    ]);
    exit;
}

// Kiểm tra OTP có đúng không
if ($user['reset_otp'] !== $otp) {
    echo json_encode([
        'success' => false,
        'message' => 'Mã OTP không chính xác'
    ]);
    exit;
}

// Kiểm tra OTP có hết hạn không
$current_time = date('Y-m-d H:i:s');
if ($current_time > $user['reset_otp_expiry']) {
    echo json_encode([
        'success' => false,
        'message' => 'Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới.'
    ]);
    exit;
}

// Kiểm tra độ dài mật khẩu
if (strlen($new_password) < 6) {
    echo json_encode([
        'success' => false,
        'message' => 'Mật khẩu phải có ít nhất 6 ký tự'
    ]);
    exit;
}

// Mã hóa mật khẩu mới
$hashed_password = md5($new_password);

// Cập nhật mật khẩu và xóa OTP
$updateQuery = "UPDATE user SET pass = '$hashed_password', reset_otp = NULL, reset_otp_expiry = NULL WHERE email = '$email'";

if (mysqli_query($conn, $updateQuery)) {
    echo json_encode([
        'success' => true,
        'message' => 'Đổi mật khẩu thành công. Vui lòng đăng nhập lại.',
        'result' => [[
            'email' => $email
        ]]
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi hệ thống, vui lòng thử lại',
        'result' => []
    ]);
}

mysqli_close($conn);
?>

