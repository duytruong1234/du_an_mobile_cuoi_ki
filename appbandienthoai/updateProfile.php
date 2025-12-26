<?php

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Phương thức không hợp lệ']);
    exit;
}

// Nhận dữ liệu
$iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;
$username = isset($_POST['username']) ? $conn->real_escape_string(trim($_POST['username'])) : '';
$email = isset($_POST['email']) ? $conn->real_escape_string(trim($_POST['email'])) : '';
$mobile = isset($_POST['mobile']) ? $conn->real_escape_string(trim($_POST['mobile'])) : '';
$password = isset($_POST['password']) ? trim($_POST['password']) : '';

// Validate
if ($iduser <= 0) {
    echo json_encode(['success' => false, 'message' => 'ID người dùng không hợp lệ']);
    exit;
}

if (empty($username)) {
    echo json_encode(['success' => false, 'message' => 'Tên hiển thị không được để trống']);
    exit;
}

if (empty($email)) {
    echo json_encode(['success' => false, 'message' => 'Email không được để trống']);
    exit;
}

// Validate email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Email không đúng định dạng']);
    exit;
}

// Kiểm tra user có tồn tại không và lấy login_type
$checkQuery = "SELECT id, email, login_type, mobile FROM user WHERE id = $iduser";
$checkResult = mysqli_query($conn, $checkQuery);

if (!$checkResult || mysqli_num_rows($checkResult) === 0) {
    echo json_encode(['success' => false, 'message' => 'Người dùng không tồn tại']);
    exit;
}

$currentUser = mysqli_fetch_assoc($checkResult);
$isGoogleAccount = ($currentUser['login_type'] === 'google') || ($currentUser['mobile'] === '0000000000');

// Kiểm tra email đã được sử dụng bởi user khác chưa
$emailCheckQuery = "SELECT id FROM user WHERE email = '$email' AND id != $iduser";
$emailCheckResult = mysqli_query($conn, $emailCheckQuery);

if ($emailCheckResult && mysqli_num_rows($emailCheckResult) > 0) {
    echo json_encode(['success' => false, 'message' => 'Email này đã được sử dụng bởi tài khoản khác']);
    exit;
}

// Xây dựng câu query UPDATE
$updateFields = [
    "username = '$username'",
    "email = '$email'",
    "mobile = '$mobile'"
];

// Nếu có mật khẩu mới, hash và thêm vào query
if (!empty($password)) {
    // Kiểm tra nếu là tài khoản Google thì KHÔNG cho phép đổi mật khẩu
    if ($isGoogleAccount) {
        echo json_encode([
            'success' => false,
            'message' => 'Tài khoản Google không thể đổi mật khẩu. Vui lòng quản lý mật khẩu qua tài khoản Google của bạn.'
        ]);
        exit;
    }

    // Hash password với MD5 (giống như trong dangki.php)
    $hashedPassword = md5($password);
    $updateFields[] = "pass = '$hashedPassword'";
}

$updateQuery = "UPDATE user SET " . implode(', ', $updateFields) . " WHERE id = $iduser";

if (mysqli_query($conn, $updateQuery)) {
    // Lấy thông tin user sau khi cập nhật
    $userQuery = "SELECT id, username, email, mobile, role FROM user WHERE id = $iduser";
    $userResult = mysqli_query($conn, $userQuery);

    if ($userResult && mysqli_num_rows($userResult) > 0) {
        $user = mysqli_fetch_assoc($userResult);

        echo json_encode([
            'success' => true,
            'message' => 'Cập nhật thông tin thành công',
            'user' => [
                'id' => intval($user['id']),
                'username' => $user['username'],
                'email' => $user['email'],
                'mobile' => $user['mobile'],
                'role' => intval($user['role'])
            ]
        ]);
    } else {
        echo json_encode([
            'success' => true,
            'message' => 'Cập nhật thông tin thành công'
        ]);
    }
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi cập nhật thông tin: ' . mysqli_error($conn)
    ]);
}

mysqli_close($conn);
?>

