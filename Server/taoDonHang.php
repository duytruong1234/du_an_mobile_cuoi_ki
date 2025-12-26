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

    // Thông tin voucher (nếu có)
    $voucher_id = isset($_POST['voucher_id']) ? intval($_POST['voucher_id']) : null;
    $ma_voucher = isset($_POST['ma_voucher']) ? $_POST['ma_voucher'] : null;
    $gia_tri_giam = isset($_POST['gia_tri_giam']) ? floatval($_POST['gia_tri_giam']) : 0;
    $tong_truoc_giam = isset($_POST['tong_truoc_giam']) ? floatval($_POST['tong_truoc_giam']) : 0;

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
        // Kiểm tra tồn kho trước khi tạo đơn
        foreach ($cartItems as $item) {
            $idsp = intval($item['idsp']);
            $sl = intval($item['soluong']);

            $check = mysqli_query($conn, "SELECT id, tensp, soluongtonkho FROM sanphammoi WHERE id = $idsp");

            if (!$check || mysqli_num_rows($check) == 0) {
                throw new Exception("Sản phẩm ID $idsp không tồn tại");
            }

            $row = mysqli_fetch_assoc($check);
            $tonkho = intval($row['soluongtonkho']);

            // Kiểm tra: nếu tồn kho KHÔNG ĐỦ (ít hơn số lượng muốn mua)
            if ($tonkho < $sl) {
                if ($tonkho == 0) {
                    throw new Exception("Sản phẩm '{$row['tensp']}' đã hết hàng");
                } else {
                    throw new Exception("Sản phẩm '{$row['tensp']}' không đủ hàng. Bạn muốn mua $sl sản phẩm nhưng chỉ còn $tonkho trong kho");
                }
            }
        }

        // Thêm đơn hàng (với thông tin voucher nếu có)
        if ($voucher_id > 0) {
            $sql = "INSERT INTO donhang (madonhang, iduser, diachi, sodienthoai, soluong, tongtien, ngaydat, ngaygiaodukien, trangthai, voucher_id, ma_voucher, gia_tri_giam, tong_truoc_giam)
                    VALUES ('$madonhang', $iduser, '$diachi', '$sodienthoai', $soluong, '$tongtien', NOW(), '$ngaygiaodukien', 'Chờ xử lý', $voucher_id, '$ma_voucher', $gia_tri_giam, $tong_truoc_giam)";
        } else {
            $sql = "INSERT INTO donhang (madonhang, iduser, diachi, sodienthoai, soluong, tongtien, ngaydat, ngaygiaodukien, trangthai)
                    VALUES ('$madonhang', $iduser, '$diachi', '$sodienthoai', $soluong, '$tongtien', NOW(), '$ngaygiaodukien', 'Chờ xử lý')";
        }

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

        // Nếu có sử dụng voucher, lưu lịch sử sử dụng
        if ($voucher_id > 0) {
            // 1. Lưu vào bảng voucher_usage
            $sqlVoucherUsage = "INSERT INTO voucher_usage (voucher_id, user_id, donhang_id, ma_donhang, gia_tri_don_hang, gia_tri_giam, ngay_su_dung)
                               VALUES ($voucher_id, $iduser, $iddonhang, '$madonhang', $tong_truoc_giam, $gia_tri_giam, NOW())";

            if (!mysqli_query($conn, $sqlVoucherUsage)) {
                throw new Exception("Lỗi lưu lịch sử voucher: " . mysqli_error($conn));
            }

            // Lưu ý: KHÔNG CẦN UPDATE voucher.da_su_dung ở đây
            // Vì đã có TRIGGER 'after_donhang_insert_update_voucher' tự động tăng da_su_dung
            // khi INSERT vào bảng donhang
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

