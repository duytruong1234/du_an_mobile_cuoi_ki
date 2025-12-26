<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $madonhang = isset($_POST['madonhang']) ? mysqli_real_escape_string($conn, $_POST['madonhang']) : '';
    $trangthai = isset($_POST['trangthai']) ? $_POST['trangthai'] : '';

    if (empty($madonhang) || $trangthai === '') {
        echo json_encode([
            'success' => false,
            'message' => 'Thiếu thông tin'
        ]);
        exit;
    }

    // Define labels mapping (1-based indexes expected from client)
    $labels = array('Đang xử lý', 'Đã chuẩn bị', 'Đã giao', 'Thành công', 'Đã hủy');

    // Normalize incoming trangthai: if numeric map to label, otherwise use provided text
    $newStatusText = '';
    if (is_numeric($trangthai)) {
        $idx = intval($trangthai);
        if ($idx >= 1 && $idx <= count($labels)) {
            $newStatusText = $labels[$idx - 1];
        } else {
            $newStatusText = strval($trangthai);
        }
    } else {
        $newStatusText = mysqli_real_escape_string($conn, $trangthai);
    }

    // Helper to detect canceled-like statuses
    function is_canceled($s) {
        if ($s === null) return false;
        $t = mb_strtolower($s, 'UTF-8');
        return (mb_strpos($t, 'hủy') !== false || mb_strpos($t, 'huy') !== false);
    }

    // Start transaction
    mysqli_begin_transaction($conn);
    try {
        // Fetch the order and current status
        $q = "SELECT id, trangthai FROM donhang WHERE madonhang = '" . $madonhang . "' FOR UPDATE";
        $res = mysqli_query($conn, $q);
        if (!$res || mysqli_num_rows($res) == 0) {
            throw new Exception('Không tìm thấy đơn hàng');
        }

        $row = mysqli_fetch_assoc($res);
        $iddonhang = intval($row['id']);
        $prevStatusText = $row['trangthai'];

        $prevCanceled = is_canceled($prevStatusText);
        $newCanceled = is_canceled($newStatusText);

        // KHÔNG cho phép admin cập nhật nếu đơn hàng đã bị khách hàng hủy
        if ($prevCanceled) {
            throw new Exception('Không thể cập nhật đơn hàng đã bị hủy bởi khách hàng');
        }

        // If transitioning to canceled from a non-canceled state: restore stock
        if (!$prevCanceled && $newCanceled) {
            // for each item in order, add back the quantity
            $sqlItems = "SELECT idsp, soluong FROM chitietdonhang WHERE iddonhang = " . $iddonhang;
            $rItems = mysqli_query($conn, $sqlItems);
            if ($rItems) {
                while ($it = mysqli_fetch_assoc($rItems)) {
                    $idsp = intval($it['idsp']);
                    $sl = intval($it['soluong']);
                    // update stock
                    $u = "UPDATE sanphammoi SET soluongtonkho = soluongtonkho + $sl WHERE id = $idsp";
                    if (!mysqli_query($conn, $u)) {
                        throw new Exception('Lỗi cập nhật tồn kho: ' . mysqli_error($conn));
                    }
                }
            }
        }

        // If transitioning from canceled to non-canceled: try to subtract stock (ensure availability)
        if ($prevCanceled && !$newCanceled) {
            $sqlItems = "SELECT idsp, soluong FROM chitietdonhang WHERE iddonhang = " . $iddonhang;
            $rItems = mysqli_query($conn, $sqlItems);
            if ($rItems) {
                // First check availability for all items
                while ($it = mysqli_fetch_assoc($rItems)) {
                    $idsp = intval($it['idsp']);
                    $sl = intval($it['soluong']);
                    $chk = mysqli_query($conn, "SELECT soluongtonkho FROM sanphammoi WHERE id = $idsp");
                    if (!$chk || mysqli_num_rows($chk) == 0) {
                        throw new Exception('Sản phẩm không tồn tại khi kiểm kho');
                    }
                    $rchk = mysqli_fetch_assoc($chk);
                    $available = intval($rchk['soluongtonkho']);
                    if ($available < $sl) {
                        throw new Exception("Không đủ tồn kho cho sản phẩm ID $idsp: còn $available, cần $sl");
                    }
                }
                // If all good, subtract
                mysqli_data_seek($rItems, 0);
                while ($it = mysqli_fetch_assoc($rItems)) {
                    $idsp = intval($it['idsp']);
                    $sl = intval($it['soluong']);
                    $u = "UPDATE sanphammoi SET soluongtonkho = soluongtonkho - $sl WHERE id = $idsp";
                    if (!mysqli_query($conn, $u)) {
                        throw new Exception('Lỗi cập nhật tồn kho: ' . mysqli_error($conn));
                    }
                }
            }
        }

        // Finally update order status text
        $upd = "UPDATE donhang SET trangthai = '" . mysqli_real_escape_string($conn, $newStatusText) . "' WHERE id = " . $iddonhang;
        if (!mysqli_query($conn, $upd)) {
            throw new Exception('Lỗi cập nhật trạng thái: ' . mysqli_error($conn));
        }

        mysqli_commit($conn);
        echo json_encode([
            'success' => true,
            'message' => 'Cập nhật trạng thái thành công',
            'trangthai' => $newStatusText
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
