<?php
header('Content-Type: application/json; charset=utf-8');

// Kết nối database
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "appbandongho";

// Tạo kết nối
$conn = new mysqli($servername, $username, $password, $dbname);

// Kiểm tra kết nối
if ($conn->connect_error) {
    $result = array(
        'success' => false,
        'message' => 'Lỗi kết nối database: ' . $conn->connect_error,
        'result' => []
    );
    echo json_encode($result);
    exit();
}

// Thiết lập charset
$conn->set_charset("utf8");

// Lấy dữ liệu từ POST
$email = isset($_POST['email']) ? $_POST['email'] : '';
$pass = isset($_POST['pass']) ? $_POST['pass'] : '';
$username = isset($_POST['username']) ? $_POST['username'] : '';
$mobile = isset($_POST['mobile']) ? $_POST['mobile'] : '';
$login_type = isset($_POST['login_type']) ? $_POST['login_type'] : 'normal';

// Tự động nhận diện tài khoản Google qua mobile = '0000000000'
if ($mobile === '0000000000') {
    $login_type = 'google';
}

// Validate dữ liệu
if (empty($email) || empty($pass) || empty($username) || empty($mobile)) {
    $result = array(
        'success' => false,
        'message' => 'Vui lòng điền đầy đủ thông tin',
        'result' => []
    );
    echo json_encode($result);
    exit();
}

// Kiểm tra email đã tồn tại chưa
$check_sql = "SELECT * FROM user WHERE email = ?";
$stmt = $conn->prepare($check_sql);
$stmt->bind_param("s", $email);
$stmt->execute();
$check_result = $stmt->get_result();

if ($check_result->num_rows > 0) {
    $result = array(
        'success' => false,
        'message' => 'Email đã được sử dụng',
        'result' => []
    );
    echo json_encode($result);
    $stmt->close();
    $conn->close();
    exit();
}
$stmt->close();

// Mã hóa mật khẩu
$hashed_pass = md5($pass);

// Thêm user mới
$insert_sql = "INSERT INTO user (email, pass, username, mobile, login_type) VALUES (?, ?, ?, ?, ?)";
$stmt = $conn->prepare($insert_sql);
$stmt->bind_param("sssss", $email, $hashed_pass, $username, $mobile, $login_type);

if ($stmt->execute()) {
    $user_id = $stmt->insert_id;
    
    // Lấy thông tin user vừa tạo
    $user_data = array(
        'id' => $user_id,
        'email' => $email,
        'username' => $username,
        'mobile' => $mobile,
        'pass' => $hashed_pass
    );
    
    $result = array(
        'success' => true,
        'message' => 'Đăng ký thành công',
        'result' => array($user_data)
    );
    echo json_encode($result);
} else {
    $result = array(
        'success' => false,
        'message' => 'Đăng ký thất bại: ' . $stmt->error,
        'result' => []
    );
    echo json_encode($result);
}

$stmt->close();
$conn->close();
?>

