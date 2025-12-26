<?php
include "connect.php";

// API Lấy danh sách giỏ hàng của user
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;

    if ($iduser == 0) {
        echo json_encode(array(
            'success' => false,
            'message' => 'ID user không hợp lệ'
        ));
        exit;
    }

    $sql = "SELECT id, idsp, tensp, giasp, hinhsp, soluong, ngaythem, ngaycapnhat
            FROM giohang
            WHERE iduser = $iduser
            ORDER BY ngaycapnhat DESC";

    $result = mysqli_query($conn, $sql);

    if ($result) {
        $giohang = array();

        while ($row = mysqli_fetch_assoc($result)) {
            $giohang[] = array(
                'id' => $row['id'],
                'idsp' => $row['idsp'],
                'tensp' => $row['tensp'],
                'giasp' => $row['giasp'],
                'hinhsp' => $row['hinhsp'],
                'soluong' => $row['soluong'],
                'ngaythem' => $row['ngaythem'],
                'ngaycapnhat' => $row['ngaycapnhat']
            );
        }

        echo json_encode(array(
            'success' => true,
            'message' => 'Lấy giỏ hàng thành công',
            'total' => count($giohang),
            'result' => $giohang
        ));
    } else {
        echo json_encode(array(
            'success' => false,
            'message' => 'Lỗi truy vấn: ' . mysqli_error($conn)
        ));
    }
} else {
    echo json_encode(array(
        'success' => false,
        'message' => 'Method không hợp lệ'
    ));
}

mysqli_close($conn);
?>

