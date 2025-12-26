<?php
include "connect.php";

// Nhận dữ liệu từ POST
$email = $_POST['email'];
$pass = $_POST['pass'];
$username = $_POST['username'];
$mobile = $_POST['mobile'];

// Kiểm tra email đã tồn tại chưa
$check_query = "SELECT * FROM user WHERE email = '$email'";
$check_data = mysqli_query($conn, $check_query);

if (mysqli_num_rows($check_data) > 0) {
    // Email đã tồn tại
    echo json_encode(array(
        'success' => false,
        'message' => 'Email đã được sử dụng'
    ));
} else {
    // Thêm user mới với role = 0 (user thường)
    // CHỈ ADMIN CÓ THỂ TẠO TÀI KHOẢN ADMIN KHÁC THÔNG QUA DATABASE
    $insert_query = "INSERT INTO user (email, pass, username, mobile, role) VALUES ('$email', '$pass', '$username', '$mobile', 0)";

    if (mysqli_query($conn, $insert_query)) {
        echo json_encode(array(
            'success' => true,
            'message' => 'Đăng ký thành công'
        ));
    } else {
        echo json_encode(array(
            'success' => false,
            'message' => 'Đăng ký thất bại: ' . mysqli_error($conn)
        ));
    }
}
?>

