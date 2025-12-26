<?php
/**
 * thongke_doanhthu.php
 * API lấy thống kê doanh thu theo tháng/năm cho admin
 * Output: JSON danh sách doanh thu
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';

// Thiết lập UTF-8
mysqli_set_charset($conn, "utf8mb4");

try {
    // Lấy tham số loại thống kê (month hoặc year), mặc định là month
    $type = isset($_GET['type']) ? $_GET['type'] : 'month';
    $year = isset($_GET['year']) ? intval($_GET['year']) : date('Y');

    if ($type === 'month') {
        // Thống kê doanh thu theo tháng trong năm
        $sql = "
            SELECT
                MONTH(ngaydat) as thang,
                SUM(tongtien) as doanhthu,
                COUNT(*) as sodonhang
            FROM donhang
            WHERE YEAR(ngaydat) = $year
                AND trangthai IN ('Đã thanh toán', 'Đang giao hàng', 'Đã giao hàng')
            GROUP BY MONTH(ngaydat)
            ORDER BY thang ASC
        ";
    } else {
        // Thống kê doanh thu theo năm
        $sql = "
            SELECT
                YEAR(ngaydat) as nam,
                SUM(tongtien) as doanhthu,
                COUNT(*) as sodonhang
            FROM donhang
            WHERE trangthai IN ('Đã thanh toán', 'Đang giao hàng', 'Đã giao hàng')
            GROUP BY YEAR(ngaydat)
            ORDER BY nam ASC
        ";
    }

    $result = mysqli_query($conn, $sql);

    if (!$result) {
        throw new Exception('Lỗi truy vấn: ' . mysqli_error($conn));
    }

    $data = array();
    while ($row = mysqli_fetch_assoc($result)) {
        if ($type === 'month') {
            $data[] = array(
                'thang' => (int)$row['thang'],
                'doanhthu' => (float)$row['doanhthu'],
                'sodonhang' => (int)$row['sodonhang']
            );
        } else {
            $data[] = array(
                'nam' => (int)$row['nam'],
                'doanhthu' => (float)$row['doanhthu'],
                'sodonhang' => (int)$row['sodonhang']
            );
        }
    }

    // Thống kê tổng quan
    $sqlTongQuan = "
        SELECT
            SUM(tongtien) as tong_doanhthu,
            COUNT(*) as tong_donhang,
            COUNT(DISTINCT iduser) as tong_khachhang
        FROM donhang
        WHERE trangthai IN ('Đã thanh toán', 'Đang giao hàng', 'Đã giao hàng')
    ";

    if ($type === 'month') {
        $sqlTongQuan .= " AND YEAR(ngaydat) = $year";
    }

    $resultTongQuan = mysqli_query($conn, $sqlTongQuan);
    $tongQuan = mysqli_fetch_assoc($resultTongQuan);

    echo json_encode(array(
        'success' => true,
        'message' => 'Lấy thống kê doanh thu thành công',
        'type' => $type,
        'year' => $year,
        'result' => $data,
        'tongquan' => array(
            'tong_doanhthu' => (float)$tongQuan['tong_doanhthu'],
            'tong_donhang' => (int)$tongQuan['tong_donhang'],
            'tong_khachhang' => (int)$tongQuan['tong_khachhang']
        )
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

