<?php
header('Content-Type: application/json; charset=utf-8');
include "connect.php";

echo "<h2>🔍 Debug: Kiểm tra API đăng nhập</h2>";

// Test đăng nhập với admin@admin.com
$email = "admin@admin.com";
$pass = md5("admin"); // Thay "admin" bằng mật khẩu thực của bạn

$query = "SELECT * FROM user WHERE email = '$email' AND pass = '$pass'";
$data = mysqli_query($conn, $query);

echo "<h3>📊 Kết quả truy vấn:</h3>";
echo "<p><b>Query:</b> $query</p>";

if (mysqli_num_rows($data) > 0) {
    $row = mysqli_fetch_assoc($data);

    echo "<h3>✅ Tìm thấy user:</h3>";
    echo "<table border='1' cellpadding='10'>";
    echo "<tr><th>Field</th><th>Value</th></tr>";
    echo "<tr><td>ID</td><td>" . $row['id'] . "</td></tr>";
    echo "<tr><td>Email</td><td>" . $row['email'] . "</td></tr>";
    echo "<tr><td>Username</td><td>" . $row['username'] . "</td></tr>";
    echo "<tr><td>Role (raw)</td><td>" . var_export($row['role'], true) . "</td></tr>";
    echo "<tr><td>Role (intval)</td><td>" . intval($row['role']) . "</td></tr>";
    echo "<tr><td>Is Admin?</td><td>" . ($row['role'] == 1 ? "✅ YES" : "❌ NO") . "</td></tr>";
    echo "</table>";

    // Chuẩn bị response giống API
    if (!isset($row['role']) || $row['role'] === null || $row['role'] === '') {
        $row['role'] = 0;
    }
    $row['role'] = intval($row['role']);

    echo "<h3>📤 JSON Response (giống API trả về):</h3>";
    echo "<pre style='background: #f0f0f0; padding: 10px;'>";
    echo json_encode([
        'success' => true,
        'message' => 'Đăng nhập thành công',
        'result' => [$row]
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    echo "</pre>";

    // Kiểm tra xem isAdmin sẽ return gì
    $isAdmin = ($row['role'] == 1);
    echo "<h3>🎯 Kết quả kiểm tra isAdmin():</h3>";
    echo "<p style='font-size: 20px; background: " . ($isAdmin ? "lightgreen" : "pink") . "; padding: 10px;'>";
    echo "role = " . $row['role'] . " → isAdmin() = " . ($isAdmin ? "✅ TRUE (ADMIN)" : "❌ FALSE (USER)");
    echo "</p>";

} else {
    echo "<h3 style='color: red;'>❌ Không tìm thấy user với email=$email</h3>";
    echo "<p>Vui lòng kiểm tra:</p>";
    echo "<ul>";
    echo "<li>Email có đúng không?</li>";
    echo "<li>Mật khẩu có đúng không? (hiện đang test với md5('admin'))</li>";
    echo "<li>User có tồn tại trong database không?</li>";
    echo "</ul>";
}

// Hiển thị tất cả users để tham khảo
echo "<hr><h3>📋 Tất cả users trong database:</h3>";
$all_users = mysqli_query($conn, "SELECT id, email, username, role FROM user ORDER BY id");
echo "<table border='1' cellpadding='10'>";
echo "<tr><th>ID</th><th>Email</th><th>Username</th><th>Role</th><th>Is Admin?</th></tr>";
while ($u = mysqli_fetch_assoc($all_users)) {
    echo "<tr>";
    echo "<td>" . $u['id'] . "</td>";
    echo "<td>" . $u['email'] . "</td>";
    echo "<td>" . $u['username'] . "</td>";
    echo "<td>" . $u['role'] . "</td>";
    echo "<td>" . ($u['role'] == 1 ? "✅ ADMIN" : "❌ USER") . "</td>";
    echo "</tr>";
}
echo "</table>";

mysqli_close($conn);
?>

