<?php
header('Content-Type: text/html; charset=utf-8');
include "connect.php";

echo "<h2>Kiểm tra và cập nhật quyền Admin</h2>";

// Lấy tất cả users
$query = "SELECT id, email, username, mobile, role FROM user ORDER BY id";
$result = mysqli_query($conn, $query);

echo "<h3>Danh sách người dùng hiện tại:</h3>";
echo "<table border='1' cellpadding='10' cellspacing='0'>";
echo "<tr><th>ID</th><th>Email</th><th>Username</th><th>Mobile</th><th>Role</th><th>Action</th></tr>";

while ($row = mysqli_fetch_assoc($result)) {
    $roleText = ($row['role'] == 1) ? '<b style="color: red;">ADMIN</b>' : 'User';
    echo "<tr>";
    echo "<td>" . $row['id'] . "</td>";
    echo "<td>" . $row['email'] . "</td>";
    echo "<td>" . $row['username'] . "</td>";
    echo "<td>" . $row['mobile'] . "</td>";
    echo "<td>" . $roleText . "</td>";
    echo "<td>";
    if ($row['role'] != 1) {
        echo "<a href='?set_admin=" . $row['id'] . "' style='color: green;'>Set Admin</a>";
    } else {
        echo "<a href='?remove_admin=" . $row['id'] . "' style='color: red;'>Remove Admin</a>";
    }
    echo "</td>";
    echo "</tr>";
}

echo "</table>";

// Xử lý cập nhật quyền
if (isset($_GET['set_admin'])) {
    $userid = intval($_GET['set_admin']);
    $update = mysqli_query($conn, "UPDATE user SET role = 1 WHERE id = $userid");
    if ($update) {
        echo "<script>alert('Đã set quyền Admin cho user ID: $userid'); window.location.href = 'check_admin.php';</script>";
    }
}

if (isset($_GET['remove_admin'])) {
    $userid = intval($_GET['remove_admin']);
    $update = mysqli_query($conn, "UPDATE user SET role = 0 WHERE id = $userid");
    if ($update) {
        echo "<script>alert('Đã gỡ quyền Admin cho user ID: $userid'); window.location.href = 'check_admin.php';</script>";
    }
}

// Tự động set admin@admin.com thành admin nếu chưa có
$check_admin = mysqli_query($conn, "SELECT id, role FROM user WHERE email = 'admin@admin.com'");
if ($check_admin && mysqli_num_rows($check_admin) > 0) {
    $admin_data = mysqli_fetch_assoc($check_admin);
    if ($admin_data['role'] != 1) {
        mysqli_query($conn, "UPDATE user SET role = 1 WHERE email = 'admin@admin.com'");
        echo "<div style='background: yellow; padding: 10px; margin: 20px 0;'>";
        echo "<b>✅ Đã tự động set admin@admin.com thành ADMIN!</b><br>";
        echo "Vui lòng đăng xuất và đăng nhập lại trong app.";
        echo "</div>";
    } else {
        echo "<div style='background: lightgreen; padding: 10px; margin: 20px 0;'>";
        echo "<b>✅ admin@admin.com đã có quyền ADMIN (role = 1)</b>";
        echo "</div>";
    }
} else {
    echo "<div style='background: #ffcccc; padding: 10px; margin: 20px 0;'>";
    echo "<b>⚠️ Không tìm thấy tài khoản admin@admin.com trong database!</b><br>";
    echo "Bạn cần tạo tài khoản này hoặc set admin cho tài khoản khác.";
    echo "</div>";
}

mysqli_close($conn);
?>

<style>
    body {
        font-family: Arial, sans-serif;
        padding: 20px;
        background: #f5f5f5;
    }
    h2, h3 {
        color: #333;
    }
    table {
        background: white;
        width: 100%;
        margin-top: 10px;
    }
    th {
        background: #4CAF50;
        color: white;
        padding: 12px;
    }
    td {
        padding: 10px;
    }
    tr:nth-child(even) {
        background: #f9f9f9;
    }
    a {
        text-decoration: none;
        font-weight: bold;
        padding: 5px 10px;
        border-radius: 3px;
        background: #eee;
    }
    a:hover {
        background: #ddd;
    }
</style>

