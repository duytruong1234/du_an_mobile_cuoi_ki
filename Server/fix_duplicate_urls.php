<?php
// Script để sửa các URL hình ảnh bị lặp trong database
header('Content-Type: text/html; charset=utf-8');
require_once 'connect.php';

echo "<h2>Đang sửa các URL hình ảnh bị lặp...</h2>";

// 1. Sửa bảng sanphammoi - URL bị lặp 2 lần
$sql1 = "UPDATE sanphammoi
SET hinhanh = REPLACE(hinhanh, 'http://10.0.2.2/appbandienthoai/images/http://10.0.2.2/appbandienthoai/images/', '')
WHERE hinhanh LIKE '%http://10.0.2.2/appbandienthoai/images/http://10.0.2.2/appbandienthoai/images/%'";
$result1 = mysqli_query($conn, $sql1);
echo "Bước 1: Sửa URL lặp 2 lần - " . ($result1 ? "Thành công (" . mysqli_affected_rows($conn) . " dòng)" : "Lỗi") . "<br>";

// 2. Sửa bảng sanphammoi - Loại bỏ base URL còn lại
$sql2 = "UPDATE sanphammoi
SET hinhanh = REPLACE(hinhanh, 'http://10.0.2.2/appbandienthoai/images/', '')
WHERE hinhanh LIKE 'http://10.0.2.2/appbandienthoai/images/%'";
$result2 = mysqli_query($conn, $sql2);
echo "Bước 2: Loại bỏ base URL 10.0.2.2 - " . ($result2 ? "Thành công (" . mysqli_affected_rows($conn) . " dòng)" : "Lỗi") . "<br>";

// 3. Sửa localhost nếu có
$sql3 = "UPDATE sanphammoi
SET hinhanh = REPLACE(hinhanh, 'http://localhost/appbandienthoai/images/', '')
WHERE hinhanh LIKE 'http://localhost/appbandienthoai/images/%'";
$result3 = mysqli_query($conn, $sql3);
echo "Bước 3: Loại bỏ base URL localhost - " . ($result3 ? "Thành công (" . mysqli_affected_rows($conn) . " dòng)" : "Lỗi") . "<br>";

// 4. Kiểm tra kết quả
echo "<br><h3>Kiểm tra kết quả (10 sản phẩm đầu):</h3>";
$check = "SELECT id, tensp, hinhanh FROM sanphammoi LIMIT 10";
$result = mysqli_query($conn, $check);

if ($result) {
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>ID</th><th>Tên SP</th><th>Hình ảnh</th></tr>";
    while ($row = mysqli_fetch_assoc($result)) {
        echo "<tr>";
        echo "<td>" . $row['id'] . "</td>";
        echo "<td>" . $row['tensp'] . "</td>";
        echo "<td>" . htmlspecialchars($row['hinhanh']) . "</td>";
        echo "</tr>";
    }
    echo "</table>";
}

mysqli_close($conn);
echo "<br><h3 style='color: green;'>✓ Hoàn tất! Bây giờ hãy chạy lại app.</h3>";
?>

