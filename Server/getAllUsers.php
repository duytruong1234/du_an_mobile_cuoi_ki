<?php
header('Content-Type: application/json; charset=utf-8');
include "connect.php";

// Kiểm tra xem có phải request từ admin không
// (trong production, nên kiểm tra token hoặc session)

$query = "SELECT id, email, username, mobile, role, login_type, status FROM user ORDER BY id DESC";
$result = mysqli_query($conn, $query);

if (!$result) {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi truy vấn: ' . mysqli_error($conn),
        'result' => []
    ]);
    exit;
}

$users = [];
while ($row = mysqli_fetch_assoc($result)) {
    // Đảm bảo role luôn có giá trị
    if (!isset($row['role']) || $row['role'] === null) {
        $row['role'] = 0;
    }
    $row['role'] = intval($row['role']);

    // Đảm bảo status luôn có giá trị (mặc định = 1 nếu null)
    if (!isset($row['status']) || $row['status'] === null) {
        $row['status'] = 1;
    }
    $row['status'] = intval($row['status']);

    // Đảm bảo login_type có giá trị
    if (!isset($row['login_type']) || $row['login_type'] === null) {
        // Kiểm tra nếu mobile = '0000000000' thì là Google account
        if ($row['mobile'] === '0000000000') {
            $row['login_type'] = 'google';
        } else {
            $row['login_type'] = 'normal';
        }
    }

    // Không trả về password
    unset($row['pass']);

    $users[] = $row;
}

echo json_encode([
    'success' => true,
    'message' => 'Lấy danh sách người dùng thành công',
    'result' => $users
]);

mysqli_close($conn);
?>

