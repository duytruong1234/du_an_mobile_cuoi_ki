<?php
/**
 * createUser.php
 * API tạo tài khoản người dùng mới (chỉ admin)
 */

header('Content-Type: application/json; charset=utf-8');
include "connect.php";

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

// Nhận dữ liệu
$email = isset($_POST['email']) ? trim($_POST['email']) : '';
$username = isset($_POST['username']) ? trim($_POST['username']) : '';
$password = isset($_POST['password']) ? trim($_POST['password']) : '';
$mobile = isset($_POST['mobile']) ? trim($_POST['mobile']) : '';
$role = 0; // Luôn tạo tài khoản với quyền User thường

// Validate
if (empty($email) || empty($username) || empty($password) || empty($mobile)) {
    echo json_encode(['success' => false, 'message' => 'Vui lòng điền đầy đủ thông tin']);
    exit;
}

// Validate email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Email không hợp lệ']);
    exit;
}

// Validate phone number (10 digits)
if (!preg_match('/^[0-9]{10}$/', $mobile)) {
    echo json_encode(['success' => false, 'message' => 'Số điện thoại phải có 10 chữ số']);
    exit;
}

// Kiểm tra email đã tồn tại
$email_esc = mysqli_real_escape_string($conn, $email);
$check_query = "SELECT id FROM user WHERE email = '$email_esc'";
$check_result = mysqli_query($conn, $check_query);

if ($check_result && mysqli_num_rows($check_result) > 0) {
    echo json_encode(['success' => false, 'message' => 'Email đã tồn tại trong hệ thống']);
    exit;
}

// Escape dữ liệu
$username_esc = mysqli_real_escape_string($conn, $username);
$password_hash = md5($password); // Mã hóa password
$mobile_esc = mysqli_real_escape_string($conn, $mobile);

// Insert user mới
$query = "INSERT INTO user (email, username, pass, mobile, role, status, login_type)
          VALUES ('$email_esc', '$username_esc', '$password_hash', '$mobile_esc', $role, 1, 'normal')";

if (mysqli_query($conn, $query)) {
    $new_user_id = mysqli_insert_id($conn);

    // Lấy thông tin user vừa tạo
    $get_user = "SELECT * FROM user WHERE id = $new_user_id";
    $result = mysqli_query($conn, $get_user);

    if ($result && mysqli_num_rows($result) > 0) {
        $user = mysqli_fetch_assoc($result);
        unset($user['pass']); // Không trả về password

        echo json_encode([
            'success' => true,
            'message' => 'Tạo tài khoản thành công',
            'user' => $user
        ]);
    } else {
        echo json_encode(['success' => true, 'message' => 'Tạo tài khoản thành công']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Lỗi: ' . mysqli_error($conn)]);
}

mysqli_close($conn);
?>

