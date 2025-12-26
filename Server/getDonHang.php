<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    $iduser = isset($_GET['iduser']) ? intval($_GET['iduser']) : 0;
    $isadmin = isset($_GET['isadmin']) ? intval($_GET['isadmin']) : 0;

    // Nếu là request của admin (isadmin=1) thì chỉ chấp nhận khi kèm `iduser` của chính admin
    if ($isadmin === 1) {
        if ($iduser == 0) {
            echo json_encode([
                'success' => false,
                'message' => 'Yêu cầu iduser của admin'
            ]);
            exit;
        }

        // Kiểm tra role của người dùng gửi request
        $checkSql = "SELECT role FROM user WHERE id = $iduser LIMIT 1";
        $checkRes = mysqli_query($conn, $checkSql);
        if (!$checkRes || mysqli_num_rows($checkRes) == 0) {
            echo json_encode([
                'success' => false,
                'message' => 'Người dùng không tồn tại'
            ]);
            exit;
        }

        $row = mysqli_fetch_assoc($checkRes);
        if (intval($row['role']) !== 1) {
            echo json_encode([
                'success' => false,
                'message' => 'Không có quyền truy cập'
            ]);
            exit;
        }

        $sql = "SELECT
                dh.id,
                dh.madonhang,
                dh.diachi,
                dh.sodienthoai,
                dh.soluong,
                dh.tongtien,
                dh.ngaydat,
                dh.ngaygiaodukien,
                dh.trangthai
            FROM donhang dh
            JOIN user u ON dh.iduser = u.id
            WHERE COALESCE(u.role, 0) = 0
            ORDER BY dh.ngaydat DESC";
    } else {
        if ($iduser == 0) {
            echo json_encode([
                'success' => false,
                'message' => 'ID người dùng không hợp lệ'
            ]);
            exit;
        }

        $sql = "SELECT
                dh.id,
                dh.madonhang,
                dh.diachi,
                dh.sodienthoai,
                dh.soluong,
                dh.tongtien,
                dh.ngaydat,
                dh.ngaygiaodukien,
                dh.trangthai
            FROM donhang dh
            WHERE dh.iduser = $iduser
            ORDER BY dh.ngaydat DESC";
    }

    $result = mysqli_query($conn, $sql);

    if ($result) {
        $donhangs = array();
        while ($row = mysqli_fetch_assoc($result)) {
            $donhangs[] = $row;
        }

        echo json_encode([
            'success' => true,
            'data' => $donhangs
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Lỗi truy vấn dữ liệu'
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
