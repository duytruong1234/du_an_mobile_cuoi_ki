<?php
/**
 * thongke.php
 * API lấy thống kê sản phẩm bán chạy nhất cho admin
 * Output: JSON danh sách sản phẩm và số lượng đã bán
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';

// Thiết lập UTF-8
mysqli_set_charset($conn, "utf8mb4");

try {
    // Thống kê top sản phẩm bán chạy nhất
    // Lấy dữ liệu từ bảng chitietdonhang, chỉ tính đơn hàng đã thanh toán
    $sql = "
        SELECT
            s.tensp,
            SUM(c.soluong) as tong
        FROM chitietdonhang c
        INNER JOIN sanphammoi s ON c.idsp = s.id
        INNER JOIN donhang d ON c.iddonhang = d.id
        WHERE d.trangthai IN ('Đã thanh toán', 'Đang giao hàng', 'Đã giao hàng')
        GROUP BY c.idsp, s.tensp
        ORDER BY tong DESC
        LIMIT 10
    ";

    $result = mysqli_query($conn, $sql);

    if (!$result) {
        throw new Exception('Lỗi truy vấn: ' . mysqli_error($conn));
    }

    $data = array();
    while ($row = mysqli_fetch_assoc($result)) {
        $data[] = array(
            'tensp' => $row['tensp'],
            'tong' => (int)$row['tong']
        );
    }

    echo json_encode(array(
        'success' => true,
        'message' => 'Lấy thống kê thành công',
        'result' => $data
    ), JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    echo json_encode(array(
        'success' => false,
        'message' => $e->getMessage(),
        'result' => array()
    ), JSON_UNESCAPED_UNICODE);
}

mysqli_close($conn);
?>

