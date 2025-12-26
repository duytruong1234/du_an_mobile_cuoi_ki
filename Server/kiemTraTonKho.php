<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    $idsp = isset($_GET['idsp']) ? intval($_GET['idsp']) : 0;

    // Nếu idsp bằng 0 (không được cung cấp), trả về danh sách tồn kho đầy đủ (tương thích ngược)
    if ($idsp == 0) {
        // Thử sử dụng view tồn kho nếu có sẵn, nếu không thì quay lại sanphammoi
        $sqlList = "SELECT id, tensp, hinhanh, giasp, soluongtonkho, loaisanpham, tinhtrang FROM v_ThongKeTonKho ORDER BY tensp ASC";
        $res = mysqli_query($conn, $sqlList);
        if ($res) {
            $rows = array();
            while ($r = mysqli_fetch_assoc($res)) {
                $rows[] = $r;
            }
            echo json_encode([
                'success' => true,
                'data' => $rows
            ]);
            mysqli_close($conn);
            exit;
        }
        // nếu view không có hoặc truy vấn thất bại, tiếp tục trả về lỗi
    }

    // Nếu chúng ta đến đây và idsp > 0, trả về thông tin sản phẩm đơn lẻ
    if ($idsp > 0) {
        $sql = "SELECT id, tensp, hinhanh, giasp, soluongtonkho, mota
            FROM sanphammoi
            WHERE id = $idsp";

        $result = mysqli_query($conn, $sql);

        if ($result && mysqli_num_rows($result) > 0) {
            $row = mysqli_fetch_assoc($result);
            echo json_encode([
                'success' => true,
                'data' => $row
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'message' => 'Không tìm thấy sản phẩm'
            ]);
        }

        mysqli_close($conn);
    } else {
        // Nếu idsp == 0 và truy vấn danh sách thất bại trước đó, trả về lỗi
        echo json_encode([
            'success' => false,
            'message' => 'Không thể lấy dữ liệu tồn kho'
        ]);
        mysqli_close($conn);
    }
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không hợp lệ'
    ]);
}
?>
