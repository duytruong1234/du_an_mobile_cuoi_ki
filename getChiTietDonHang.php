<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    $madonhang = isset($_GET['madonhang']) ? $_GET['madonhang'] : '';

    if (empty($madonhang)) {
        echo json_encode([
            'success' => false,
            'message' => 'Mã đơn hàng không hợp lệ'
        ]);
        exit;
    }

    $sql = "SELECT
                dh.id,
                dh.madonhang,
                dh.diachi,
                dh.sodienthoai,
                dh.tongtien,
                dh.ngaydat,
                dh.ngaygiaodukien,
                dh.trangthai,
                ct.id as idchitiet,
                ct.idsp,
                COALESCE(sp.tensp, '') AS tensp,
                COALESCE(sp.hinhanh, '') AS hinhanh,
                ct.soluong,
                ct.gia,
                (ct.soluong * ct.gia) AS thanhtien
            FROM donhang dh
            INNER JOIN chitietdonhang ct ON dh.id = ct.iddonhang
            LEFT JOIN sanphammoi sp ON ct.idsp = sp.id
            WHERE dh.madonhang = '" . mysqli_real_escape_string($conn, $madonhang) . "'
            ORDER BY ct.id ASC";

    $result = mysqli_query($conn, $sql);

    if ($result && mysqli_num_rows($result) > 0) {
        $items = array();
        $donhang = null;

        while ($row = mysqli_fetch_assoc($result)) {
            if ($donhang == null) {
                $donhang = array(
                    'id' => $row['id'],
                    'madonhang' => $row['madonhang'],
                    'diachi' => $row['diachi'],
                    'sodienthoai' => $row['sodienthoai'],
                    'tongtien' => $row['tongtien'],
                    'ngaydat' => $row['ngaydat'],
                    'ngaygiaodukien' => $row['ngaygiaodukien'],
                    'trangthai' => $row['trangthai']
                );
            }

            $items[] = array(
                'idchitiet' => $row['idchitiet'],
                'idsp' => $row['idsp'],
                'tensp' => $row['tensp'],
                'hinhanh' => $row['hinhanh'],
                'soluong' => $row['soluong'],
                'gia' => $row['gia'],
                'thanhtien' => $row['thanhtien']
            );
        }

        echo json_encode([
            'success' => true,
            'donhang' => $donhang,
            'items' => $items
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Không tìm thấy đơn hàng'
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
