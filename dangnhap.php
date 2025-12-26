<?php
// Tắt hiển thị lỗi PHP để tránh lỗi JSON parsing
error_reporting(0);
ini_set('display_errors', 0);

// Đảm bảo chỉ trả về JSON
header('Content-Type: application/json; charset=utf-8');

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
if (!isset($_POST['email']) || !isset($_POST['pass'])) {
    echo json_encode([
        'success' => false,
        'message' => 'Thiếu thông tin email hoặc mật khẩu'
    ]);
    exit;
}

// Nhận dữ liệu từ POST
$email = mysqli_real_escape_string($conn, $_POST['email']);
$pass = mysqli_real_escape_string($conn, $_POST['pass']);

// Kiểm tra xem đây có phải tài khoản Google không (mobile = '0000000000')
$check_google_query = "SELECT * FROM user WHERE email = '$email'";
$check_data = mysqli_query($conn, $check_google_query);

if ($check_data && mysqli_num_rows($check_data) > 0) {
    $user_info = mysqli_fetch_assoc($check_data);

    // Nếu là tài khoản Google (có mobile = '0000000000')
    if ($user_info['mobile'] === '0000000000') {
        // Tự động reset mật khẩu về mật khẩu mặc định của Google
        $hashed_pass = md5($pass);
        $user_id = $user_info['id'];

        // Cập nhật mật khẩu về mặc định Google
        $update_query = "UPDATE user SET pass = '$hashed_pass' WHERE id = $user_id";
        mysqli_query($conn, $update_query);

        // Lấy lại thông tin user sau khi update
        $query = "SELECT * FROM user WHERE id = $user_id";
        $data = mysqli_query($conn, $query);

        if ($data && mysqli_num_rows($data) > 0) {
            $row = mysqli_fetch_assoc($data);

            // Đảm bảo role có giá trị, NHƯNG KHÔNG ĐỔI nếu đã có
            if (!isset($row['role']) || $row['role'] === null || $row['role'] === '') {
                // Chỉ set role = 0 nếu thực sự chưa có giá trị
                $row['role'] = 0;
                mysqli_query($conn, "UPDATE user SET role = 0 WHERE id = " . $row['id'] . " AND (role IS NULL OR role = '')");
            }

            $row['role'] = intval($row['role']);

            echo json_encode([
                'success' => true,
                'message' => 'Đăng nhập Google thành công',
                'result' => [$row]
            ]);
            mysqli_close($conn);
            exit;
        }
    }
}

// Xử lý đăng nhập thường (không phải Google)
$pass = md5($pass); // Mã hóa để khớp với DB

// Truy vấn kiểm tra email và password
$query = "SELECT * FROM user WHERE email = '$email' AND pass = '$pass'";
$data = mysqli_query($conn, $query);

if (!$data) {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi truy vấn: ' . mysqli_error($conn)
    ]);
    exit;
}

// Kiểm tra kết quả
if (mysqli_num_rows($data) > 0) {
    $row = mysqli_fetch_assoc($data);

    // Đảm bảo role có giá trị, NHƯNG KHÔNG ĐỔI nếu đã có
    if (!isset($row['role']) || $row['role'] === null || $row['role'] === '') {
        // Chỉ set role = 0 nếu thực sự chưa có giá trị
        $row['role'] = 0;
        mysqli_query($conn, "UPDATE user SET role = 0 WHERE id = " . $row['id'] . " AND (role IS NULL OR role = '')");
    }

    $row['role'] = intval($row['role']);

    echo json_encode([
        'success' => true,
        'message' => 'Đăng nhập thành công',
        'result' => [$row]
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Email hoặc mật khẩu không đúng',
        'result' => []
    ]);
}

mysqli_close($conn);
?>

