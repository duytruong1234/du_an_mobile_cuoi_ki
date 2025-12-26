<?php
/**
 * FILE DEBUG - KIỂM TRA USER ADMIN
 */
header('Content-Type: application/json; charset=utf-8');
include "connect.php";

echo "<h2>Kiểm tra User Admin</h2>";

// Lấy tất cả users
$query = "SELECT id, email, username, mobile, role FROM user ORDER BY id";
$result = mysqli_query($conn, $query);

echo "<table border='1' cellpadding='10' cellspacing='0'>";
echo "<tr><th>ID</th><th>Email</th><th>Username</th><th>Mobile</th><th>Role</th><th>Loại</th></tr>";

while ($row = mysqli_fetch_assoc($result)) {
    $roleText = ($row['role'] == 1) ? '<b style="color: red;">ADMIN</b>' : 'User';
    echo "<tr>";
    echo "<td>" . $row['id'] . "</td>";
    echo "<td>" . $row['email'] . "</td>";
    echo "<td>" . $row['username'] . "</td>";
    echo "<td>" . $row['mobile'] . "</td>";
    echo "<td>" . $row['role'] . "</td>";
    echo "<td>" . $roleText . "</td>";
    echo "</tr>";
}

echo "</table>";

mysqli_close($conn);
?>

