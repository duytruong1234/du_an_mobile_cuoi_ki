<?php
include "connect.php";
header('Content-Type: application/json; charset=utf-8');

// Accept iduser from either GET or POST for compatibility with various clients
if (!isset($_REQUEST['iduser']) || trim((string)$_REQUEST['iduser']) === '') {
    echo json_encode([ 'success' => false, 'message' => 'iduser is required', 'data' => [] ], JSON_UNESCAPED_UNICODE);
    exit;
}

$iduser = intval($_REQUEST['iduser']);
$isadmin = isset($_REQUEST['isadmin']) ? intval($_REQUEST['isadmin']) : 0;

// Prepare $data (mysqli_result or array) depending on branch
$data = false;

if ($isadmin === 1) {
    // Verify requester is admin
    $checkSql = "SELECT role FROM user WHERE id = ? LIMIT 1";
    if ($stmt = mysqli_prepare($conn, $checkSql)) {
        mysqli_stmt_bind_param($stmt, 'i', $iduser);
        mysqli_stmt_execute($stmt);
        $res = mysqli_stmt_get_result($stmt);
        $row = mysqli_fetch_assoc($res);
        mysqli_stmt_close($stmt);
        if (!$row) {
            echo json_encode(['success' => false, 'message' => 'Người dùng không tồn tại', 'data' => []], JSON_UNESCAPED_UNICODE);
            exit;
        }
        if (intval($row['role']) !== 1) {
            echo json_encode(['success' => false, 'message' => 'Không có quyền truy cập', 'data' => []], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $query = "SELECT dh.* FROM donhang dh JOIN user u ON dh.iduser = u.id WHERE COALESCE(u.role,0)=0 ORDER BY dh.id DESC";
        $data = mysqli_query($conn, $query);
        if ($data === false) {
            echo json_encode(['success' => false, 'message' => 'Lỗi truy vấn: ' . mysqli_error($conn), 'data' => []], JSON_UNESCAPED_UNICODE);
            exit;
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'Lỗi chuẩn bị truy vấn', 'data' => []], JSON_UNESCAPED_UNICODE);
        exit;
    }
} else {
    // Normal user: fetch orders for this iduser
    $query = "SELECT * FROM donhang WHERE iduser = ? ORDER BY id DESC";
    if ($stmt = mysqli_prepare($conn, $query)) {
        mysqli_stmt_bind_param($stmt, 'i', $iduser);
        mysqli_stmt_execute($stmt);
        $data = mysqli_stmt_get_result($stmt);
        mysqli_stmt_close($stmt);
        if ($data === false) {
            echo json_encode(['success' => false, 'message' => 'Lỗi truy vấn: ' . mysqli_error($conn), 'data' => []], JSON_UNESCAPED_UNICODE);
            exit;
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'Lỗi chuẩn bị truy vấn', 'data' => []], JSON_UNESCAPED_UNICODE);
        exit;
    }
}

$result = array();

// Prepare detail statement once: LEFT JOIN so deleted products still show
$detailSql = "SELECT ct.iddonhang AS iddonhang, ct.idsp AS idsp, ct.soluong AS soluong, ct.gia AS gia, COALESCE(sp.tensp, '') AS tensp, COALESCE(sp.hinhanh, '') AS hinhanh
              FROM chitietdonhang ct
              LEFT JOIN sanphammoi sp ON ct.idsp = sp.id
              WHERE ct.iddonhang = ?";

$detailStmt = mysqli_prepare($conn, $detailSql);
if (!$detailStmt) {
    echo json_encode(['success' => false, 'message' => 'Lỗi chuẩn bị truy vấn chi tiết: ' . mysqli_error($conn), 'data' => []], JSON_UNESCAPED_UNICODE);
    exit;
}

$orderIds = array();

while ($row = mysqli_fetch_assoc($data)) {
    $iddonhang = intval($row['id']);

    $items = array();
    mysqli_stmt_bind_param($detailStmt, 'i', $iddonhang);
    mysqli_stmt_execute($detailStmt);
    $resDetail = mysqli_stmt_get_result($detailStmt);
    if ($resDetail && mysqli_num_rows($resDetail) > 0) {
        while ($r = mysqli_fetch_assoc($resDetail)) {
            $items[] = $r;
        }
    }

    $row['item'] = $items;
    $result[] = $row;
    $orderIds[] = $iddonhang;
}

mysqli_stmt_close($detailStmt);

if (!empty($result)) {
    echo json_encode([
        'success' => true,
        'message' => 'thanh cong',
        'count' => count($result),
        'order_ids' => $orderIds,
        'data' => $result
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'khong co don hang',
        'count' => 0,
        'order_ids' => [],
        'data' => []
    ], JSON_UNESCAPED_UNICODE);
}

mysqli_close($conn);
?>
