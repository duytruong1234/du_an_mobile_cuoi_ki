<?php
include "connect.php";
header('Content-Type: application/json; charset=utf-8');

$sdt = $_POST['sdt'];
$email = $_POST['email'];
$tongtien = $_POST['tongtien'];
$iduser = $_POST['iduser'];
$diachi = $_POST['diachi'];
$soluong = $_POST['soluong'];
$chitiet = $_POST['chitiet'];

// Log để debug
error_log("Chitiet received: " . $chitiet);

// Tạo đơn hàng mới
$query = "INSERT INTO donhang(`iduser`, `diachi`, `sodienthoai`, `soluong`, `tongtien`, `thoigian`)
          VALUES ('$iduser', '$diachi', '$sdt', '$soluong', '$tongtien', NOW())";

$data = mysqli_query($conn, $query);

if ($data == true) {
    // Lấy id đơn hàng mới tạo
    $iddonhang = mysqli_insert_id($conn);
    error_log("ID don hang moi: " . $iddonhang);

    // Parse JSON chi tiết
    $chitiet_array = json_decode($chitiet, true);

    // Kiểm tra chi tiết có hợp lệ không
    if ($chitiet_array !== null && is_array($chitiet_array) && count($chitiet_array) > 0) {
        $success = true;
        $count = 0;

        foreach ($chitiet_array as $item) {
            $idsp = mysqli_real_escape_string($conn, $item['idsp']);
            $soluongsp = mysqli_real_escape_string($conn, $item['soluong']);
            $giasp = mysqli_real_escape_string($conn, $item['giasp']);
            $tensp = mysqli_real_escape_string($conn, $item['tensp']);
            $hinhanh = mysqli_real_escape_string($conn, $item['hinhanh']);

            $sql = "INSERT INTO chitietdonhang(`iddonhang`, `idsp`, `tensp`, `soluong`, `gia`, `hinhanh`)
                    VALUES ('$iddonhang', '$idsp', '$tensp', '$soluongsp', '$giasp', '$hinhanh')";

            error_log("Insert query: " . $sql);
            $result = mysqli_query($conn, $sql);

            if ($result) {
                $count++;
            } else {
                error_log("Error inserting chitiet: " . mysqli_error($conn));
                $success = false;
            }
        }

        if ($success && $count > 0) {
            $arr = [
                'success' => true,
                'message' => 'Đặt hàng thành công. Đã lưu ' . $count . ' sản phẩm',
                'result' => ['iddonhang' => $iddonhang, 'count' => $count]
            ];
        } else {
            $arr = [
                'success' => false,
                'message' => 'Không thể thêm chi tiết đơn hàng. Đã lưu ' . $count . '/' . count($chitiet_array) . ' sản phẩm'
            ];
        }
    } else {
        error_log("Chitiet array is null or empty");
        $arr = [
            'success' => false,
            'message' => 'Không có chi tiết đơn hàng hoặc dữ liệu không hợp lệ. Chitiet: ' . $chitiet
        ];
    }
} else {
    $arr = [
        'success' => false,
        'message' => 'Không thể tạo đơn hàng: ' . mysqli_error($conn)
    ];
}

echo json_encode($arr, JSON_UNESCAPED_UNICODE);
?>
