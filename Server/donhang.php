<?php
include "connect.php";

$sdt = $_POST['sdt'];
$email = $_POST['email'];
$tongtien = $_POST['tongtien'];
$iduser = $_POST['iduser'];
$diachi = $_POST['diachi'];
$soluong = $_POST['soluong'];
$chitiet = $_POST['chitiet'];

$query = "INSERT INTO donhang(`iduser`, `diachi`, `sodienthoai`, `soluong`, `tongtien`, `thoigian`)
          VALUES ('$iduser', '$diachi', '$sdt', '$soluong', '$tongtien', NOW())";

$data = mysqli_query($conn, $query);

if ($data == true) {
    // Lấy id đơn hàng vừa tạo
    $query = "SELECT id AS iddonhang FROM donhang WHERE iduser = '$iduser' ORDER BY id DESC LIMIT 1";
    $data = mysqli_query($conn, $query);
    $row = mysqli_fetch_assoc($data);
    $iddonhang = $row['iddonhang'];

    if (!empty($chitiet)) {
        $chitiet = json_decode($chitiet, true);
        $success = true;

        foreach ($chitiet as $value) {
            $idsp = $value['idsp'];
            $soluongsp = $value['soluong'];
            $giasp = $value['giasp'];

            $sql = "INSERT INTO chitietdonhang(`iddonhang`, `idsp`, `soluong`, `gia`) 
                    VALUES ('$iddonhang', '$idsp', '$soluongsp', '$giasp')";
            $result = mysqli_query($conn, $sql);

            if (!$result) {
                $success = false;
                break;
            }

            // ✅ TRỪ TỒN KHO SAU KHI THÊM CHI TIẾT ĐƠN HÀNG (COD - thanh toán khi nhận hàng)
            $sqlUpdateStock = "UPDATE sanphammoi SET soluongtonkho = GREATEST(0, soluongtonkho - $soluongsp) WHERE id = $idsp";
            mysqli_query($conn, $sqlUpdateStock);
        }

        if ($success) {
            $arr = [
                'success' => true,
                'message' => 'Đặt hàng thành công',
                'iddonhang' => $iddonhang
            ];
        } else {
            $arr = [
                'success' => false,
                'message' => 'Không thể thêm chi tiết đơn hàng'
            ];
        }
    } else {
        $arr = [
            'success' => false,
            'message' => 'Không có chi tiết đơn hàng'
        ];
    }
} else {
    $arr = [A
        'success' => false,
        'message' => 'Không thể tạo đơn hàng'
    ];
}

header('Content-Type: application/json; charset=utf-8');
echo json_encode($arr);
?>
