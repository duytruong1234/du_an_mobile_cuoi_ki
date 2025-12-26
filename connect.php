<?php
// Cấu hình kết nối database
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "appbandienthoai";

// Tạo kết nối
$conn = mysqli_connect($servername, $username, $password, $dbname);

// Kiểm tra kết nối
if (!$conn) {
    die(json_encode(array(
        'success' => false,
        'message' => 'Kết nối database thất bại: ' . mysqli_connect_error()
    )));
}

// Thiết lập charset UTF-8
mysqli_set_charset($conn, "utf8");
?>

