<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

include "connect.php";

// Kiểm tra method POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
    exit;
}

// Lấy dữ liệu từ POST
$userid = isset($_POST['userid']) ? intval($_POST['userid']) : 0;

// Validate dữ liệu
if ($userid <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'ID người dùng không hợp lệ'
    ]);
    exit;
}

// Kiểm tra user có tồn tại không
$check_query = "SELECT id, username FROM user WHERE id = $userid";
$check_result = mysqli_query($conn, $check_query);

if (!$check_result || mysqli_num_rows($check_result) === 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Người dùng không tồn tại'
    ]);
    exit;
}

$user_data = mysqli_fetch_assoc($check_result);

// Xóa người dùng
// Lưu ý: trong production nên xem xét cascade delete hoặc soft delete
// để không mất dữ liệu đơn hàng liên quan
$delete_query = "DELETE FROM user WHERE id = $userid";
$delete_result = mysqli_query($conn, $delete_query);

if ($delete_result) {
    echo json_encode([
        'success' => true,
        'message' => "Đã xóa người dùng " . $user_data['username'],
        'result' => [
            'userid' => $userid
        ]
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi xóa người dùng: ' . mysqli_error($conn)
    ]);
}

mysqli_close($conn);
?>
<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

include "connect.php";

// Kiểm tra xem có phải request từ admin không
// (trong production, nên kiểm tra token hoặc session)

$query = "SELECT id, email, username, mobile, role, login_type FROM user ORDER BY id DESC";
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

