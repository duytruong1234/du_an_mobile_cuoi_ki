<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "appbandongho"; 

$conn = new mysqli($servername, $username, $password, $dbname);
$conn->set_charset("utf8");

if ($conn->connect_error) {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi kết nối database: ' . $conn->connect_error,
        'result' => []
    ]);
    exit;
}

