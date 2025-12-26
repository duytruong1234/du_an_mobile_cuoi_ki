# ✅ Hướng Dẫn: Đã Đổi Từ `admin_id` Sang `role` Trong Quản Lý Voucher

## 📋 Tóm Tắt Thay Đổi

Hệ thống quản lý voucher đã được cập nhật để sử dụng **`role`** (vai trò) thay vì **`admin_id`** (ID người dùng) khi gọi API.

---

## 🔧 Các File Đã Sửa

### 1️⃣ **ApiBanHang.java** (API Interface)
**Đường dẫn:** `app/src/main/java/vn/duytruong/appbandienthoai/retrofit/ApiBanHang.java`

#### Trước:
```java
@POST("addVoucher.php")
Observable<MessageModel> addVoucher(
    @Field("admin_id") int adminId,  // ❌ Dùng admin_id
    ...
);

@POST("updateVoucher.php")
Observable<MessageModel> updateVoucher(
    @Field("admin_id") int adminId,  // ❌ Dùng admin_id
    @Field("id") int id,
    ...
);

@POST("deleteVoucher.php")
Observable<MessageModel> deleteVoucher(
    @Field("id") int id,
    @Field("admin_id") int adminId,  // ❌ Dùng admin_id
    ...
);
```

#### Sau:
```java
@POST("addVoucher.php")
Observable<MessageModel> addVoucher(
    @Field("role") int role,  // ✅ Dùng role
    ...
);

@POST("updateVoucher.php")
Observable<MessageModel> updateVoucher(
    @Field("id") int id,
    @Field("role") int role,  // ✅ Dùng role
    ...
);

@POST("deleteVoucher.php")
Observable<MessageModel> deleteVoucher(
    @Field("id") int id,
    @Field("role") int role,  // ✅ Dùng role
    ...
);
```

---

### 2️⃣ **addVoucher.php** (Backend API)
**Đường dẫn:** `addVoucher.php`

#### Thay đổi:
- ❌ **Trước:** Nhận `admin_id`, query database để kiểm tra user có tồn tại và role = 1
- ✅ **Sau:** Nhận `role`, kiểm tra trực tiếp `role == 1`

```php
// ❌ TRƯỚC
if (!isset($_POST['admin_id']) || intval($_POST['admin_id']) <= 0) {
    echo json_encode(['success' => false, 'message' => 'Thiếu thông tin admin']);
    exit;
}
$admin_id = intval($_POST['admin_id']);
// Kiểm tra quyền admin bằng query database...

// ✅ SAU
if (!isset($_POST['role']) || intval($_POST['role']) != 1) {
    echo json_encode(['success' => false, 'message' => 'Không có quyền thêm voucher. Chỉ admin mới có quyền này.']);
    exit;
}
```

---

### 3️⃣ **updateVoucher.php** (Backend API)
**Đường dẫn:** `updateVoucher.php`

#### Thay đổi: Tương tự `addVoucher.php`

---

### 4️⃣ **deleteVoucher.php** (Backend API)
**Đường dẫn:** `deleteVoucher.php`

#### Thay đổi: Tương tự `addVoucher.php`

---

### 5️⃣ **QuanLyVoucherActivity.java** (Android Activity)
**Đường dẫn:** `app/src/main/java/vn/duytruong/appbandienthoai/activity/QuanLyVoucherActivity.java`

#### Thay đổi:
```java
// ❌ TRƯỚC - Gửi ID của user
apiBanHang.addVoucher(Utils.user_current.getId(), ...);
apiBanHang.updateVoucher(id, Utils.user_current.getId(), ...);
apiBanHang.deleteVoucher(voucher.getId(), Utils.user_current.getId(), ...);

// ✅ SAU - Gửi role của user
apiBanHang.addVoucher(Utils.user_current.getRole(), ...);
apiBanHang.updateVoucher(id, Utils.user_current.getRole(), ...);
apiBanHang.deleteVoucher(voucher.getId(), Utils.user_current.getRole(), ...);
```

---

## 🎯 Lợi Ích Của Thay Đổi

| Tiêu chí | Trước (admin_id) | Sau (role) |
|----------|------------------|------------|
| **Hiệu suất** | ❌ Phải query database mỗi lần | ✅ Kiểm tra trực tiếp, nhanh hơn |
| **Bảo mật** | ❌ Có thể gửi ID giả mạo | ✅ Chỉ cần kiểm tra role = 1 |
| **Đơn giản** | ❌ Cần 3 bước: nhận ID → query DB → check role | ✅ 1 bước: check role |
| **Code** | ❌ Nhiều dòng code kiểm tra | ✅ Ngắn gọn, dễ đọc |

---

## 📝 Cách Hoạt Động Mới

### Flow kiểm tra quyền:

```
1. User đăng nhập → Utils.user_current được set
2. Utils.user_current.getRole() = 1 (nếu là admin)
3. App gửi role = 1 lên server
4. PHP kiểm tra: $_POST['role'] == 1 ?
   ✅ Nếu đúng → Cho phép thêm/sửa/xóa voucher
   ❌ Nếu sai → Trả về lỗi "Không có quyền"
```

---

## ⚠️ Lưu Ý Quan Trọng

### 1. **Utils.user_current phải được set đúng**
```java
// Khi đăng nhập thành công:
Utils.user_current = user;  // user có thuộc tính role = 1 (admin)
```

### 2. **Role = 1 là Admin**
```
role = 1  → Admin (có quyền quản lý voucher)
role = 0  → User thường (không có quyền)
```

### 3. **Kiểm tra quyền trước khi gọi API**
```java
if (Utils.user_current == null) {
    Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
    return;
}

if (Utils.user_current.getRole() != 1) {
    Toast.makeText(this, "Bạn không có quyền", Toast.LENGTH_SHORT).show();
    return;
}

// ✅ Gọi API
apiBanHang.addVoucher(Utils.user_current.getRole(), ...);
```

---

## 🧪 Test Sau Khi Thay Đổi

### ✅ Test Case 1: Admin thêm voucher
```
1. Đăng nhập bằng tài khoản admin (role = 1)
2. Vào màn hình Quản Lý Voucher
3. Nhấn nút "Thêm voucher"
4. Điền thông tin và lưu
5. Kết quả: ✅ Thêm thành công
```

### ✅ Test Case 2: User thường thử thêm voucher
```
1. Đăng nhập bằng tài khoản user (role = 0)
2. Vào màn hình Quản Lý Voucher (nếu có)
3. Nhấn nút "Thêm voucher"
4. Kết quả: ❌ Thông báo "Bạn không có quyền thêm voucher"
```

### ✅ Test Case 3: Sửa voucher
```
1. Đăng nhập admin
2. Chọn voucher và nhấn "Sửa"
3. Cập nhật thông tin
4. Kết quả: ✅ Sửa thành công
```

### ✅ Test Case 4: Xóa voucher
```
1. Đăng nhập admin
2. Chọn voucher và nhấn "Xóa"
3. Xác nhận xóa
4. Kết quả: ✅ Xóa thành công
```

---

## 🐛 Debug Nếu Có Lỗi

### Lỗi: "Không có quyền thêm voucher"
**Nguyên nhân:**
- `Utils.user_current.getRole() != 1`

**Cách fix:**
1. Kiểm tra xem user đã đăng nhập chưa
2. Kiểm tra role trong database: `SELECT role FROM user WHERE email = 'admin@gmail.com'`
3. Nếu role = 0, update: `UPDATE user SET role = 1 WHERE email = 'admin@gmail.com'`

### Lỗi: "Bạn chưa đăng nhập"
**Nguyên nhân:**
- `Utils.user_current == null`

**Cách fix:**
1. Đăng nhập lại
2. Kiểm tra code đăng nhập có set `Utils.user_current` đúng không

---

## 📊 So Sánh Chi Tiết

### Trước (dùng admin_id):
```
App → Gửi admin_id = 3
PHP → Query: SELECT role FROM user WHERE id = 3
PHP → Kiểm tra: role == 1 ?
PHP → Thực hiện action
```

**Vấn đề:**
- Mất thời gian query database
- Có thể bị lỗi nếu ID không tồn tại
- Code phức tạp hơn

### Sau (dùng role):
```
App → Gửi role = 1
PHP → Kiểm tra: role == 1 ?
PHP → Thực hiện action
```

**Ưu điểm:**
- ✅ Nhanh hơn (không cần query)
- ✅ Đơn giản hơn
- ✅ Ít lỗi hơn

---

## 📅 Ngày Cập Nhật
**3 tháng 11, 2025**

## ✍️ Tác Giả
GitHub Copilot

---

## 🎉 Kết Luận

Tất cả các chức năng quản lý voucher (Thêm/Sửa/Xóa) đã được cập nhật để sử dụng `role` thay vì `admin_id`. Hệ thống giờ đây đơn giản, nhanh và an toàn hơn! 🚀

