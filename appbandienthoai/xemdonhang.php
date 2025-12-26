<?php
include "connect.php";
header('Content-Type: application/json; charset=utf-8');

// Accept iduser from either GET or POST for compatibility with various clients
if (!isset($_REQUEST['iduser']) || trim((string)$_REQUEST['iduser']) === '') {
    echo json_encode([ 'success' => false, 'message' => 'iduser is required', 'result' => [] ], JSON_UNESCAPED_UNICODE);
    exit;
}

$iduser = intval($_REQUEST['iduser']);
$isadmin = isset($_REQUEST['isadmin']) ? intval($_REQUEST['isadmin']) : 0;
$viewmode = isset($_REQUEST['viewmode']) ? $_REQUEST['viewmode'] : 'my'; // 'my' = đơn của tôi, 'all' = tất cả đơn (chỉ admin)

// Nếu isadmin=1 thì xác thực iduser là admin
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
            echo json_encode(['success' => false, 'message' => 'Người dùng không tồn tại', 'result' => []], JSON_UNESCAPED_UNICODE);
            exit;
        }
        if (intval($row['role']) !== 1) {
            echo json_encode(['success' => false, 'message' => 'Không có quyền truy cập', 'result' => []], JSON_UNESCAPED_UNICODE);
            exit;
        }

        // Nếu admin muốn xem tất cả đơn hàng của user thường (viewmode=all)
        if ($viewmode === 'all') {
            // Lấy tất cả đơn hàng của user có role = 0 hoặc role IS NULL
            $query = "SELECT dh.* FROM donhang dh JOIN user u ON dh.iduser = u.id WHERE COALESCE(u.role, 0) = 0 ORDER BY dh.id DESC";
            $data = mysqli_query($conn, $query);
        } else {
            // Mặc định: Admin xem đơn hàng của chính mình
            $query = "SELECT * FROM donhang WHERE iduser = ? ORDER BY id DESC";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_bind_param($stmt, 'i', $iduser);
            mysqli_stmt_execute($stmt);
            $data = mysqli_stmt_get_result($stmt);
            mysqli_stmt_close($stmt);
        }
        
        if ($data === false) {
            echo json_encode(['success' => false, 'message' => 'Lỗi truy vấn: ' . mysqli_error($conn), 'result' => []], JSON_UNESCAPED_UNICODE);
            exit;
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'Lỗi chuẩn bị truy vấn', 'result' => []], JSON_UNESCAPED_UNICODE);
        exit;
    }
} else {
    // Lấy danh sách đơn hàng của user bình thường (sử dụng prepared statement)
    $query = "SELECT * FROM donhang WHERE iduser = ? ORDER BY id DESC";
    if ($stmt = mysqli_prepare($conn, $query)) {
        mysqli_stmt_bind_param($stmt, 'i', $iduser);
        mysqli_stmt_execute($stmt);
        $data = mysqli_stmt_get_result($stmt);
        mysqli_stmt_close($stmt);
        if ($data === false) {
            echo json_encode(['success' => false, 'message' => 'Lỗi truy vấn: ' . mysqli_error($conn), 'result' => []], JSON_UNESCAPED_UNICODE);
            exit;
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'Lỗi chuẩn bị truy vấn', 'result' => []], JSON_UNESCAPED_UNICODE);
        exit;
    }
}

$result = array();

// Chuẩn bị câu truy vấn chi tiết 1 lần (an toàn và hiệu quả hơn)
// Sử dụng LEFT JOIN để nếu sản phẩm bị xóa vẫn giữ được dòng chi tiết (tránh mất thông tin đơn hàng)
$detailSql = "SELECT ct.iddonhang AS iddonhang, ct.idsp AS idsp, ct.soluong AS soluong, ct.gia AS gia, COALESCE(sp.tensp, '') AS tensp, COALESCE(sp.hinhanh, '') AS hinhanh
              FROM chitietdonhang ct
              LEFT JOIN sanphammoi sp ON ct.idsp = sp.id
              WHERE ct.iddonhang = ?";
$detailStmt = mysqli_prepare($conn, $detailSql);
if (!$detailStmt) {
    echo json_encode(['success' => false, 'message' => 'Lỗi chuẩn bị truy vấn chi tiết: ' . mysqli_error($conn), 'result' => []], JSON_UNESCAPED_UNICODE);
    exit;
}

// Duyệt từng đơn hàng
$orderIds = array();
while ($row = mysqli_fetch_assoc($data)) {
    $iddonhang = intval($row['id']);

    // Lấy chi tiết cho đơn hàng này
    $items = [];
    mysqli_stmt_bind_param($detailStmt, 'i', $iddonhang);
    mysqli_stmt_execute($detailStmt);
    $resDetail = mysqli_stmt_get_result($detailStmt);
    if ($resDetail && mysqli_num_rows($resDetail) > 0) {
        while ($r = mysqli_fetch_assoc($resDetail)) {
            $items[] = $r;
        }
    }

    // Gắn danh sách sản phẩm vào đơn hàng
    $row['item'] = $items;
    $result[] = $row;
    $orderIds[] = $iddonhang;
}

mysqli_stmt_close($detailStmt);

// Prepare debug log before echoing
$debugLog = "[" . date('c') . "] iduser=" . $iduser . ", isadmin=" . $isadmin . ", query=" . (isset($query) ? $query : '') . ", orders_returned=" . count($result) . "\n";
// Optionally include the order ids
$debugLog .= "order_ids: " . implode(',', $orderIds) . "\n";
file_put_contents(__DIR__ . "/debug_orders.log", $debugLog, FILE_APPEND);

// Trả kết quả JSON
if (!empty($result)) {
    $arr = [
        'success' => true,
        'message' => 'thanh cong',
        'count' => count($result),
        'order_ids' => $orderIds,
        'result' => $result
    ];
} else {
    $arr = [
        'success' => false,
        'message' => 'khong co don hang',
        'count' => 0,
        'order_ids' => [],
        'result' => []
    ];
}

// Also append full result dump for deeper debugging (avoid in production)
file_put_contents(__DIR__ . "/debug_orders.log", print_r($arr, true) . "\n", FILE_APPEND);

echo json_encode($arr, JSON_UNESCAPED_UNICODE);
mysqli_close($conn);
?>
