<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    // Return rows from the view v_ThongKeTonKho
    $sql = "SELECT id, tensp, hinhanh, giasp, soluongtonkho, loaisanpham, tinhtrang FROM v_ThongKeTonKho ORDER BY tensp ASC";
    $res = mysqli_query($conn, $sql);
    if ($res) {
        $rows = array();
        while ($r = mysqli_fetch_assoc($res)) {
            $rows[] = $r;
        }
        echo json_encode([
            'success' => true,
            'data' => $rows
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Lỗi truy vấn: ' . mysqli_error($conn)
        ]);
    }

    mysqli_close($conn);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không hợp lệ'
    ]);
}
?>
