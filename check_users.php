<?php
header('Content-Type: application/json; charset=utf-8');
include "connect.php";

// Xử lý thêm user
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'add') {
    $email = mysqli_real_escape_string($conn, $_POST['email']);
    $pass = mysqli_real_escape_string($conn, $_POST['pass']);
    $username = mysqli_real_escape_string($conn, $_POST['username']);
    $mobile = mysqli_real_escape_string($conn, $_POST['mobile']);
    $role = intval($_POST['role']);

    // Kiểm tra email đã tồn tại
    $check = mysqli_query($conn, "SELECT * FROM user WHERE email = '$email'");
    if (mysqli_num_rows($check) > 0) {
        echo json_encode(array('success' => false, 'message' => 'Email đã tồn tại!'));
        exit;
    }

    // Thêm user mới
    $query = "INSERT INTO user (email, pass, username, mobile, role) VALUES ('$email', '$pass', '$username', '$mobile', $role)";
    if (mysqli_query($conn, $query)) {
        echo json_encode(array('success' => true, 'message' => 'Thêm user thành công'));
    } else {
        echo json_encode(array('success' => false, 'message' => mysqli_error($conn)));
    }
    exit;
}

// Lấy danh sách user
$query = "SELECT id, email, pass, username, mobile, role FROM user ORDER BY role DESC, id ASC";
$result = mysqli_query($conn, $query);

if (!$result) {
    echo json_encode(array(
        'success' => false,
        'message' => 'Lỗi truy vấn: ' . mysqli_error($conn)
    ));
    exit;
}

$users = array();
while ($row = mysqli_fetch_assoc($result)) {
    // Đảm bảo role là số
    if (!isset($row['role']) || $row['role'] === null) {
        $row['role'] = 0;
    } else {
        $row['role'] = intval($row['role']);
    }
    $users[] = $row;
}

echo json_encode(array(
    'success' => true,
    'count' => count($users),
    'users' => $users
));

mysqli_close($conn);
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kiểm tra Database User</title>
    <style>
        body { font-family: Arial; max-width: 1000px; margin: 20px auto; padding: 20px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background: #4CAF50; color: white; }
        tr:nth-child(even) { background: #f2f2f2; }
        .admin { background: #e3f2fd !important; font-weight: bold; }
        .error { color: red; padding: 10px; background: #ffebee; border-radius: 5px; }
        .success { color: green; padding: 10px; background: #e8f5e9; border-radius: 5px; }
        .info { background: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; }
        button { background: #2196F3; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; margin: 5px; }
        button:hover { background: #0b7dda; }
        .add-form { background: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0; }
        input { padding: 8px; margin: 5px; border: 1px solid #ddd; border-radius: 3px; width: 200px; }
    </style>
</head>
<body>
    <h1>🔍 Kiểm tra Database User</h1>

    <div class="info">
        <strong>⚠️ Lưu ý:</strong> File này chỉ dùng để kiểm tra. Đảm bảo đã chạy file <code>fix_database.sql</code> trước!
    </div>

    <button onclick="loadUsers()">🔄 Tải lại danh sách User</button>
    <button onclick="showAddForm()">➕ Thêm User mới</button>
    <button onclick="testLogin()">🧪 Test đăng nhập</button>

    <div id="addForm" class="add-form" style="display:none;">
        <h3>Thêm User mới</h3>
        <input type="email" id="newEmail" placeholder="Email">
        <input type="password" id="newPass" placeholder="Password">
        <input type="text" id="newUsername" placeholder="Username">
        <input type="text" id="newMobile" placeholder="Mobile">
        <select id="newRole">
            <option value="0">User thường</option>
            <option value="1">Admin</option>
        </select>
        <button onclick="addUser()">Thêm</button>
        <button onclick="hideAddForm()">Hủy</button>
    </div>

    <div id="result"></div>

    <script>
        function loadUsers() {
            document.getElementById('result').innerHTML = '<p>Đang tải...</p>';

            fetch('check_users.php')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        displayUsers(data.users);
                    } else {
                        document.getElementById('result').innerHTML =
                            `<div class="error">❌ Lỗi: ${data.message}</div>`;
                    }
                })
                .catch(error => {
                    document.getElementById('result').innerHTML =
                        `<div class="error">❌ Lỗi kết nối: ${error.message}</div>`;
                });
        }

        function displayUsers(users) {
            if (users.length === 0) {
                document.getElementById('result').innerHTML =
                    '<div class="error">⚠️ Không có user nào trong database!</div>';
                return;
            }

            let html = `
                <div class="success">✅ Tìm thấy ${users.length} user trong database</div>
                <table>
                    <tr>
                        <th>ID</th>
                        <th>Email</th>
                        <th>Password</th>
                        <th>Username</th>
                        <th>Mobile</th>
                        <th>Role</th>
                        <th>Loại</th>
                    </tr>
            `;

            users.forEach(user => {
                const roleText = user.role == 1 ? '👑 ADMIN' : '👤 User';
                const rowClass = user.role == 1 ? 'admin' : '';
                html += `
                    <tr class="${rowClass}">
                        <td>${user.id}</td>
                        <td>${user.email}</td>
                        <td>${user.pass}</td>
                        <td>${user.username || 'N/A'}</td>
                        <td>${user.mobile || 'N/A'}</td>
                        <td>${user.role}</td>
                        <td>${roleText}</td>
                    </tr>
                `;
            });

            html += '</table>';
            document.getElementById('result').innerHTML = html;
        }

        function showAddForm() {
            document.getElementById('addForm').style.display = 'block';
        }

        function hideAddForm() {
            document.getElementById('addForm').style.display = 'none';
        }

        function addUser() {
            const email = document.getElementById('newEmail').value;
            const pass = document.getElementById('newPass').value;
            const username = document.getElementById('newUsername').value;
            const mobile = document.getElementById('newMobile').value;
            const role = document.getElementById('newRole').value;

            if (!email || !pass) {
                alert('Email và Password là bắt buộc!');
                return;
            }

            const formData = new FormData();
            formData.append('action', 'add');
            formData.append('email', email);
            formData.append('pass', pass);
            formData.append('username', username);
            formData.append('mobile', mobile);
            formData.append('role', role);

            fetch('check_users.php', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('✅ Thêm user thành công!');
                    hideAddForm();
                    loadUsers();
                } else {
                    alert('❌ Lỗi: ' + data.message);
                }
            })
            .catch(error => {
                alert('❌ Lỗi: ' + error.message);
            });
        }

        function testLogin() {
            const email = prompt('Nhập email để test:', 'minhduc@gmail.com');
            const pass = prompt('Nhập password:', '123456');

            if (!email || !pass) return;

            const formData = new FormData();
            formData.append('email', email);
            formData.append('pass', pass);

            fetch('dangnhap.php', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const user = data.result[0];
                    alert(`✅ Đăng nhập thành công!\n\nEmail: ${user.email}\nRole: ${user.role == 1 ? 'ADMIN' : 'USER'}`);
                } else {
                    alert(`❌ Đăng nhập thất bại!\n\n${data.message}`);
                }
            });
        }

        // Tự động tải danh sách khi load trang
        window.onload = loadUsers;
    </script>
</body>
</html>

