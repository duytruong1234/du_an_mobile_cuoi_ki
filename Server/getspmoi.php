<?php
include "connect.php";
$query = "SELECT * FROM `sanphammoi` ORDER BY id DESC";
$data = mysqli_query($conn, $query);
$result = array();

while ($row = mysqli_fetch_assoc($data)) {
    // Ensure hinhanh is just the filename, not a full URL
    // Strip base URL if it exists in database
    if (isset($row['hinhanh'])) {
        $row['hinhanh'] = str_replace('http://10.0.2.2/appbandienthoai/images/', '', $row['hinhanh']);
        $row['hinhanh'] = str_replace('http://localhost/appbandienthoai/images/', '', $row['hinhanh']);
    }
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
