# 📱 HƯỚNG DẪN SỬ DỤNG QUẢN LÝ VOUCHER TRONG ANDROID APP

**Ngày tạo:** 03/11/2025  
**Phiên bản:** 1.0

---

## ✅ ĐÃ TẠO XONG

### 📂 **Files đã tạo:**

#### **1. Java Classes:**
- ✅ `QuanLyVoucherActivity.java` - Activity quản lý voucher cho Admin
- ✅ `VoucherAdminAdapter.java` - Adapter hiển thị danh sách voucher

#### **2. Layout XML:**
- ✅ `activity_quan_ly_voucher.xml` - Giao diện chính với RecyclerView
- ✅ `item_voucher_admin.xml` - Item layout cho từng voucher
- ✅ `dialog_voucher_form.xml` - Dialog thêm/sửa voucher

#### **3. API đã thêm vào ApiBanHang.java:**
```java
getAllVouchers()     // Lấy danh sách voucher với filter
addVoucher()         // Thêm voucher mới
updateVoucher()      // Cập nhật voucher
deleteVoucher()      // Xóa voucher (soft delete)
toggleVoucher()      // Bật/Tắt voucher
getVoucherStats()    // Thống kê voucher
```

#### **4. AndroidManifest.xml:**
- ✅ Đã đăng ký `QuanLyVoucherActivity`

---

## 🚀 CÁCH TRUY CẬP VÀO MÀN HÌNH QUẢN LÝ VOUCHER

### **Cách 1: Thêm nút vào MainActivity (cho Admin)**

Trong `MainActivity.java`, thêm nút "Quản lý Voucher" vào menu admin:

```java
// Trong MainActivity.java
private void openQuanLyVoucher() {
    if (Utils.user_current != null && Utils.user_current.isAdmin()) {
        Intent intent = new Intent(getApplicationContext(), QuanLyVoucherActivity.class);
        startActivity(intent);
    } else {
        Toast.makeText(this, "Chỉ Admin mới có quyền truy cập", Toast.LENGTH_SHORT).show();
    }
}
```

### **Cách 2: Thêm vào Navigation Drawer**

Trong `activity_main.xml`, thêm item vào Navigation Menu:

```xml
<item
    android:id="@+id/nav_quan_ly_voucher"
    android:icon="@android:drawable/ic_menu_recent_history"
    android:title="Quản Lý Voucher"
    android:visible="false" />
```

Trong `MainActivity.java`, xử lý click:

```java
case R.id.nav_quan_ly_voucher:
    if (Utils.user_current.isAdmin()) {
        Intent intent = new Intent(getApplicationContext(), QuanLyVoucherActivity.class);
        startActivity(intent);
    }
    break;
```

### **Cách 3: Thêm vào QuanLiActivity (Khuyến nghị)**

Trong `QuanLiActivity.java`, thêm nút vào Toolbar:

```java
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_quan_li, menu);
    return true;
}

@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_voucher) {
        Intent intent = new Intent(this, QuanLyVoucherActivity.class);
        startActivity(intent);
        return true;
    }
    return super.onOptionsItemSelected(item);
}
```

Tạo file `res/menu/menu_quan_li.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item
        android:id="@+id/menu_voucher"
        android:icon="@android:drawable/ic_menu_recent_history"
        android:title="Quản Lý Voucher"
        app:showAsAction="ifRoom" />
    <item
        android:id="@+id/menu_tonkho"
        android:icon="@android:drawable/ic_menu_info_details"
        android:title="Quản Lý Tồn Kho"
        app:showAsAction="ifRoom" />
    <item
        android:id="@+id/menu_thongke"
        android:icon="@android:drawable/ic_menu_view"
        android:title="Thống Kê"
        app:showAsAction="ifRoom" />
</menu>
```

---

## 🎯 TÍNH NĂNG CỦA MÀN HÌNH QUẢN LÝ VOUCHER

### **📊 Dashboard Thống Kê**
- Tổng số voucher
- Số voucher đang hoạt động
- Tổng lượt sử dụng

### **🔍 Bộ Lọc & Tìm Kiếm**
- Lọc theo trạng thái: Tất cả / Hoạt động / Hết hạn / Đã tắt
- Lọc theo loại giảm: Tất cả / % / Cố định / Free Ship
- Tìm kiếm theo mã voucher (realtime)

### **➕ Thêm Voucher Mới**
1. Nhấn nút FAB (Floating Action Button) ở góc dưới phải
2. Điền thông tin:
   - Mã voucher (VD: NEWUSER20)
   - Tên voucher
   - Loại giảm: % / Cố định / Free Ship
   - Giá trị giảm
   - Giảm tối đa (nếu chọn %)
   - Đơn tối thiểu
   - Áp dụng cho: Tất cả / Khách mới / Khách cũ / Đơn đầu tiên
   - Số lượng (để trống = không giới hạn)
   - Giới hạn / user
   - Ngày bắt đầu & hết hạn (click để chọn)
   - Kích hoạt ngay
3. Nhấn "Lưu"

### **✏️ Sửa Voucher**
- Nhấn nút "✏️ Sửa" trên card voucher
- Cập nhật thông tin
- Nhấn "Lưu"

### **🔴 Bật/Tắt Voucher**
- Nhấn nút "🔴 Tắt" hoặc "🟢 Bật"
- Không xóa dữ liệu, chỉ thay đổi trạng thái

### **🗑️ Xóa Voucher**
- Nhấn nút "🗑️ Xóa"
- Xác nhận xóa
- **Xóa mềm:** Chuyển `trang_thai = 0`, không xóa khỏi database

---

## 📋 DANH SÁCH VOUCHER HIỂN THỊ

Mỗi card voucher hiển thị:
- **Mã voucher** (in đậm, chữ lớn)
- **Tên voucher**
- **Giá trị giảm** (màu đỏ nổi bật)
- **Điều kiện** (đơn tối thiểu)
- **Số lượng đã dùng / Tổng số**
- **Ngày hết hạn**
- **Trạng thái** (badge màu):
  - 🟢 Hoạt động (xanh)
  - 🔴 Hết hạn (đỏ)
  - 🟠 Hết lượt (cam)
  - ⚫ Đã tắt (xám)
- **3 nút hành động:** Sửa | Bật/Tắt | Xóa

---

## 🔐 BẢO MẬT

Màn hình này **CHỈ DÀNH CHO ADMIN**:
- Kiểm tra `Utils.user_current.isAdmin()` trước khi mở
- Server API cũng kiểm tra session admin
- User thường không thể truy cập

---

## 🐛 XỬ LÝ LỖI THƯỜNG GẶP

### **1. Lỗi "Cannot resolve symbol 'R'"**
**Giải pháp:** Build > Clean Project > Rebuild Project

### **2. Lỗi API không gọi được**
**Kiểm tra:**
- Server PHP đã chạy chưa? (XAMPP/WAMP)
- URL trong `Utils.BASE_URL` đúng chưa?
- Các file API trong folder `Server/admin/` đã tạo chưa?

### **3. Session admin không hoạt động**
**Tạm thời comment dòng kiểm tra trong API:**
```php
// Comment dòng này để test
// if (!isset($_SESSION['user']) || $_SESSION['user']['role'] != 1) {
//     echo json_encode(['success' => false, 'message' => 'Không có quyền']);
//     exit;
// }
```

### **4. Layout không hiển thị đúng**
**Kiểm tra:**
- Đã import đủ thư viện Material Design chưa?
- Trong `build.gradle`:
  ```gradle
  implementation 'com.google.android.material:material:1.9.0'
  implementation 'androidx.cardview:cardview:1.0.0'
  ```

---

## 📸 DEMO LUỒNG SỬ DỤNG

```
1. Admin đăng nhập → MainActivity
   ↓
2. Click menu "Quản Lý Voucher"
   ↓
3. Màn hình QuanLyVoucherActivity
   - Hiển thị dashboard: 5 voucher, 3 hoạt động, 120 lượt dùng
   - Danh sách voucher dạng card
   ↓
4. Nhấn FAB (+) → Dialog thêm voucher
   - Điền: NEWUSER20, Giảm 20%, Đơn min 500k
   - Chọn ngày: 01/11/2025 - 31/12/2025
   - Nhấn "Lưu"
   ↓
5. Voucher mới xuất hiện trong danh sách
   - Badge "Hoạt động" màu xanh
   - Đã dùng: 0/100
   ↓
6. Nhấn "✏️ Sửa" → Thay đổi giá trị
   - Tăng giảm tối đa lên 300k
   - Nhấn "Lưu"
   ↓
7. Nhấn "🔴 Tắt" → Voucher ngừng hoạt động
   - Badge chuyển sang "Đã tắt" màu xám
   ↓
8. Nhấn "🟢 Bật" → Kích hoạt lại
```

---

## 🎉 KẾT QUẢ

Bạn đã có **hệ thống quản lý voucher hoàn chỉnh trong Android App**:

✅ **Giao diện đẹp** với Material Design  
✅ **CRUD đầy đủ:** Thêm, Sửa, Xóa, Bật/Tắt  
✅ **Dashboard thống kê** realtime  
✅ **Bộ lọc & tìm kiếm** mạnh mẽ  
✅ **Date/Time Picker** tiện lợi  
✅ **API tích hợp** với server PHP  
✅ **Bảo mật** chỉ Admin truy cập  

---

## 📞 HỖ TRỢ

Nếu gặp lỗi hoặc cần thêm tính năng:
- Thêm xem chi tiết thống kê voucher
- Export danh sách voucher ra Excel
- Push notification khi tạo voucher mới
- Hẹn giờ tự động bật/tắt voucher

Báo cho tôi biết! 🚀

