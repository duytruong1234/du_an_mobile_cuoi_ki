<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;
    $diachi = isset($_POST['diachi']) ? $_POST['diachi'] : '';
    $sodienthoai = isset($_POST['sodienthoai']) ? $_POST['sodienthoai'] : '';
    $soluong = isset($_POST['soluong']) ? intval($_POST['soluong']) : 0;
    $tongtien = isset($_POST['tongtien']) ? $_POST['tongtien'] : 0;
    $ngaygiaodukien = isset($_POST['ngaygiaodukien']) ? $_POST['ngaygiaodukien'] : '';
    $cartItems = isset($_POST['cartItems']) ? json_decode($_POST['cartItems'], true) : [];

    // Validate dữ liệu
    if ($iduser == 0 || empty($diachi) || empty($sodienthoai) || empty($cartItems)) {
        echo json_encode([
            'success' => false,
            'message' => 'Thiếu thông tin bắt buộc'
        ]);
        exit;
    }

    // Tạo mã đơn hàng duy nhất
    $madonhang = 'DH' . date('YmdHis') . rand(100, 999);

    // Bắt đầu transaction
    mysqli_begin_transaction($conn);

    try {
        // Kiểm tra tồn kho trước khi tạo đơn với pessimistic locking
        foreach ($cartItems as $item) {
            $idsp = intval($item['idsp']);
            $sl = intval($item['soluong']);

            // ✅ Thêm FOR UPDATE để lock row, tránh race condition
            $check = mysqli_query($conn, "SELECT id, tensp, soluongtonkho FROM sanphammoi WHERE id = $idsp FOR UPDATE");

            if (!$check || mysqli_num_rows($check) == 0) {
                throw new Exception("Sản phẩm ID $idsp không tồn tại");
            }

            $row = mysqli_fetch_assoc($check);

            if ($row['soluongtonkho'] < $sl) {
                throw new Exception("Sản phẩm '{$row['tensp']}' chỉ còn {$row['soluongtonkho']} sản phẩm trong kho");
            }
        }

        // Thêm đơn hàng
        $sql = "INSERT INTO donhang (madonhang, iduser, diachi, sodienthoai, soluong, tongtien, ngaydat, ngaygiaodukien, trangthai)
                VALUES ('$madonhang', $iduser, '$diachi', '$sodienthoai', $soluong, '$tongtien', NOW(), '$ngaygiaodukien', 'Chờ xử lý')";

        if (!mysqli_query($conn, $sql)) {
            throw new Exception("Lỗi tạo đơn hàng: " . mysqli_error($conn));
        }

        $iddonhang = mysqli_insert_id($conn);

        // Thêm chi tiết đơn hàng (trigger tự động giảm tồn kho)
        foreach ($cartItems as $item) {
            $idsp = intval($item['idsp']);
            $sl = intval($item['soluong']);
            $gia = floatval($item['giasp']);

            $sqlDetail = "INSERT INTO chitietdonhang (iddonhang, idsp, soluong, gia)
                         VALUES ($iddonhang, $idsp, $sl, $gia)";

            if (!mysqli_query($conn, $sqlDetail)) {
                throw new Exception("Lỗi thêm chi tiết đơn hàng: " . mysqli_error($conn));
            }
        }

        mysqli_commit($conn);

        echo json_encode([
            'success' => true,
            'message' => 'Đặt hàng thành công',
            'madonhang' => $madonhang,
            'iddonhang' => $iddonhang
        ]);

    } catch (Exception $e) {
        mysqli_rollback($conn);
        echo json_encode([
            'success' => false,
            'message' => $e->getMessage()
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
