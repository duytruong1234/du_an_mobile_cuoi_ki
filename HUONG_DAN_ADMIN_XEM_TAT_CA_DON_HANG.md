# HƯỚNG DẪN: ADMIN XEM TẤT CẢ ĐơN HÀNG

## 🔍 VẤN ĐỀ ĐÃ KHẮC PHỤC

**Tình huống:**
- Admin đặt hàng → Lưu vào database thành công ✅
- Admin vào "Đơn hàng của tôi" → Chỉ thấy 5 đơn hàng của chính mình
- Trong database có rất nhiều đơn hàng của user khác → Admin không thấy

**Nguyên nhân:**
- Khi admin vào "Đơn hàng", mặc định chỉ xem **đơn hàng của chính mình**
- Các đơn hàng khác thuộc về user thường (role=0) với `iduser` khác

## ✅ GIẢI PHÁP ĐÃ TRIỂN KHAI

Đã thêm **nút chuyển đổi chế độ xem** cho admin:

### 1. **Đơn của tôi** (Mặc định)
- Admin chỉ xem đơn hàng của chính mình
- Giống như user thường

### 2. **Tất cả đơn hàng** (Chức năng quản lý)
- Admin xem TẤT CẢ đơn hàng trong hệ thống
- Bao gồm đơn của tất cả user thường (role=0)

## 📁 CÁC FILE ĐÃ THAY ĐỔI

### Backend (PHP):
**File:** `xemdonhang.php`
- Thêm tham số `viewmode`:
  - `viewmode=my`: Xem đơn của chính mình (mặc định)
  - `viewmode=all`: Xem tất cả đơn hàng (chỉ admin)

### Frontend (Android):
**1. File:** `activity_xem_don.xml`
- Thêm `LinearLayout` chứa 2 nút:
  - Nút "Đơn của tôi"
  - Nút "Tất cả đơn hàng"
- Chỉ hiển thị cho admin (visibility=gone cho user thường)

**2. File:** `XemDonActivity.java`
- Thêm biến `viewingAllOrders` để theo dõi chế độ xem
- Thêm phương thức `updateToggleButtons()` để cập nhật giao diện
- Cập nhật `getDonHang()` để gửi tham số `viewmode`

**3. File:** `ApiBanHang.java`
- Thêm tham số `viewmode` vào API `xemDonHang()`

**4. File:** `MainActivity.java`
- Cập nhật để gửi `viewmode="my"` mặc định

## 🎨 GIAO DIỆN MỚI

Khi admin vào "Đơn hàng của tôi", sẽ thấy 2 nút ở trên:

```
┌─────────────────────────────────────────┐
│         Đơn hàng của tôi                │ ← Toolbar
├─────────────────────────────────────────┤
│  [Đơn của tôi]  [Tất cả đơn hàng]      │ ← Toggle buttons
├─────────────────────────────────────────┤
│  Mã: DH20251028145126621               │
│  Trạng thái: Chờ xử lý                 │
│  ...                                    │
└─────────────────────────────────────────┘
```

### Nút được chọn:
- Màu xanh lá (#36DD07)
- Chữ màu trắng (#FFFFFF)

### Nút không được chọn:
- Màu xám (#CCCCCC)
- Chữ màu xám đậm (#666666)

## 🚀 CÁCH SỬ DỤNG

### Bước 1: Build lại ứng dụng
```
Android Studio → Build → Rebuild Project
```

### Bước 2: Đăng nhập bằng tài khoản admin
- Email: `admin@admin.com`
- Password: `admin123`

### Bước 3: Vào "Đơn hàng"

**Chế độ "Đơn của tôi" (Mặc định):**
- Toolbar: "Đơn hàng của tôi"
- Hiển thị: 5 đơn hàng của admin
- Nút "Đơn của tôi" màu xanh

**Chế độ "Tất cả đơn hàng":**
- Click nút "Tất cả đơn hàng"
- Toolbar: "Tất cả đơn hàng"
- Hiển thị: TẤT CẢ đơn hàng của user thường trong database
- Nút "Tất cả đơn hàng" màu xanh

## 🔐 BẢO MẬT

**Kiểm tra quyền ở Backend:**
```php
if ($isadmin === 1) {
    // Verify requester is admin
    if (intval($row['role']) !== 1) {
        echo json_encode(['success' => false, 'message' => 'Không có quyền truy cập']);
        exit;
    }
    
    if ($viewmode === 'all') {
        // CHỈ admin mới có thể xem tất cả đơn
        $query = "SELECT dh.* FROM donhang dh JOIN user u ON dh.iduser = u.id 
                  WHERE COALESCE(u.role, 0) = 0 ORDER BY dh.id DESC";
    }
}
```

**Kiểm tra quyền ở Frontend:**
```java
// Hiển thị nút toggle chỉ cho admin
if (isAdmin) {
    layoutAdminToggle.setVisibility(View.VISIBLE);
} else {
    layoutAdminToggle.setVisibility(View.GONE);
}
```

## 📊 LOGIC XỬ LÝ

### User thường (role=0):
- Không thấy nút toggle
- Chỉ xem đơn hàng của chính mình
- API: `viewmode=my`, `isadmin=0`

### Admin (role=1):
- Thấy 2 nút toggle
- **Mặc định:** Xem đơn của chính mình (`viewmode=my`)
- **Khi click "Tất cả đơn hàng":** Xem tất cả đơn của user thường (`viewmode=all`)

## ⚠️ LƯU Ý

1. **"Tất cả đơn hàng" CHỈ hiển thị đơn của user thường (role=0)**
   - KHÔNG bao gồm đơn của admin
   - Để thấy đơn của admin → chọn "Đơn của tôi"

2. **Đơn hàng của admin và user được lưu riêng biệt**
   - Admin có `iduser` của riêng mình
   - User thường có `iduser` khác nhau
   - Tất cả được lưu trong cùng bảng `donhang`

3. **Nếu muốn xem TẤT CẢ đơn (bao gồm cả đơn của admin):**
   - Sửa file `xemdonhang.php` dòng 37:
   ```php
   // Thay đổi từ:
   WHERE COALESCE(u.role, 0) = 0
   
   // Thành:
   WHERE 1=1  // Lấy tất cả đơn
   ```

## ✅ KẾT QUẢ

**Trước khi sửa:**
- Admin vào "Đơn hàng" → Chỉ thấy 5 đơn ❌

**Sau khi sửa:**
- Admin vào "Đơn hàng" → Mặc định thấy 5 đơn của mình ✅
- Admin click "Tất cả đơn hàng" → Thấy TẤT CẢ đơn trong database ✅
- Admin có thể chuyển đổi linh hoạt giữa 2 chế độ ✅

## 🎯 MỞ RỘNG

Nếu muốn thêm tính năng lọc nâng cao:
- Lọc theo trạng thái (Chờ xử lý, Đang giao, Đã giao...)
- Lọc theo khoảng thời gian
- Tìm kiếm theo mã đơn hàng
- Xem chi tiết user đặt hàng

→ Có thể mở rộng trong file `XemDonActivity.java` và `xemdonhang.php`

