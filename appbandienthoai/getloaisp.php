<?php
include "connect.php";
$query = "SELECT * FROM sanpham";
$data = mysqli_query($conn, $query);
$result = array();

while ($row = mysqli_fetch_assoc($data)) {
    $result[] = $row;
}

// Set header trả về JSON
header('Content-Type: application/json; charset=utf-8');

if (!empty($result)) {
    $arr = [
        'success' => true,
        'message' => 'thanh cong',
        'result' => $result
    ];
} else {
    $arr = [
        'success' => false,
        'message' => 'khong co du lieu',
        'result' => []
    ];
}

echo json_encode($arr);
?>
