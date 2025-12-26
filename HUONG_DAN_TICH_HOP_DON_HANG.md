# HƯỚNG DẪN TÍCH HỢP CHỨC NĂNG ĐƠN HÀNG VÀ TỒN KHO

## 1. Đã Hoàn Thành

### 1.1 Database (SQL)
- ✅ File: `update_donhang_tonkho.sql`
- ✅ Thêm cột `soluongtonkho` vào bảng `sanphammoi`
- ✅ Thêm các cột mới vào bảng `donhang`: `madonhang`, `ngaydat`, `ngaygiaodukien`, `trangthai`
- ✅ Tạo trigger tự động cập nhật tồn kho
- ✅ Tạo stored procedures và views hỗ trợ

### 1.2 PHP API
Đã tạo 5 file PHP API:

1. **taoDonHang.php** - Tạo đơn hàng mới
   - Kiểm tra tồn kho trước khi đặt
   - Sử dụng transaction để đảm bảo tính toàn vẹn dữ liệu
   - Tự động tạo mã đơn hàng unique

2. **kiemTraTonKho.php** - Kiểm tra số lượng tồn kho của sản phẩm

3. **getDonHang.php** - Lấy danh sách đơn hàng theo user

4. **getChiTietDonHang.php** - Lấy chi tiết đơn hàng theo mã đơn hàng

5. **capNhatTrangThai.php** - Cập nhật trạng thái đơn hàng

### 1.3 Java Models
- ✅ `DonHang.java` - Model đơn hàng
- ✅ `ChiTietDonHang.java` - Model chi tiết đơn hàng

### 1.4 Java Activities
- ✅ `DatHangActivity.java` - Màn hình đặt hàng
- ✅ `DonHangActivity.java` - Danh sách đơn hàng
- ✅ `ChiTietDonHangActivity.java` - Chi tiết đơn hàng

### 1.5 Java Adapters
- ✅ `DonHangAdapter.java` - Adapter cho danh sách đơn hàng
- ✅ `ChiTietDonHangAdapter.java` - Adapter cho chi tiết đơn hàng

### 1.6 XML Layouts
- ✅ `activity_dat_hang.xml` - Layout đặt hàng
- ✅ `activity_don_hang.xml` - Layout danh sách đơn hàng
- ✅ `activity_chi_tiet_don_hang.xml` - Layout chi tiết đơn hàng
- ✅ `item_don_hang.xml` - Item layout cho đơn hàng
- ✅ `item_chi_tiet_don_hang.xml` - Item layout cho chi tiết
- ✅ `bg_status.xml` - Background cho trạng thái

### 1.7 Utils
- ✅ Đã cập nhật `Utils.java` với các URL API mới

---

## 2. CÁC BƯỚC TÍCH HỢP

### Bước 1: Cập nhật Database
```bash
# Import file SQL vào MySQL
mysql -u root -p ten_database < update_donhang_tonkho.sql
```

### Bước 2: Upload các file PHP lên server
Upload 5 file PHP vào thư mục web server của bạn:
- taoDonHang.php
- kiemTraTonKho.php
- getDonHang.php
- getChiTietDonHang.php
- capNhatTrangThai.php

### Bước 3: Cập nhật AndroidManifest.xml
Thêm các Activity mới vào AndroidManifest.xml:

```xml
<activity
    android:name=".activity.DatHangActivity"
    android:exported="false"
    android:theme="@style/Theme.AppBanDienThoai" />

<activity
    android:name=".activity.DonHangActivity"
    android:exported="false"
    android:theme="@style/Theme.AppBanDienThoai" />

<activity
    android:name=".activity.ChiTietDonHangActivity"
    android:exported="false"
    android:theme="@style/Theme.AppBanDienThoai" />
```

### Bước 4: Cập nhật build.gradle (Module: app)
Đảm bảo có các dependencies sau:

```gradle
dependencies {
    // Material Design
    implementation 'com.google.android.material:material:1.9.0'
    
    // RecyclerView
    implementation 'androidx.recyclerview:recyclerview:1.3.1'
    
    // CardView
    implementation 'androidx.cardview:cardview:1.0.0'
    
    // Volley for API calls
    implementation 'com.android.volley:volley:1.2.1'
    
    // Gson for JSON parsing
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Glide for image loading
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.15.1'
}
```

### Bước 5: Cập nhật BASE_URL trong Utils.java
Thay đổi BASE_URL thành địa chỉ server thực tế của bạn:

```java
public static final String BASE_URL = "http://your-server.com/appbandienthoai/";
```

Hoặc nếu test trên máy thật:
```java
public static final String BASE_URL = "http://192.168.1.XXX/appbandienthoai/";
```

### Bước 6: Thêm chức năng vào màn hình Giỏ hàng
Trong Activity Giỏ hàng hiện tại, thêm nút "Đặt hàng":

```java
btnDatHang.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Kiểm tra đăng nhập
        if (Utils.user_current == null) {
            Toast.makeText(GioHangActivity.this, 
                "Vui lòng đăng nhập để đặt hàng", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Copy giỏ hàng sang mangmuahang
        Utils.mangmuahang = new ArrayList<>(Utils.manggiohang);
        
        // Chuyển sang màn hình đặt hàng
        Intent intent = new Intent(GioHangActivity.this, DatHangActivity.class);
        startActivity(intent);
    }
});
```

### Bước 7: Thêm menu "Đơn hàng của tôi"
Trong Activity chính hoặc menu drawer, thêm:

```java
menuDonHang.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (Utils.user_current == null) {
            Toast.makeText(MainActivity.this, 
                "Vui lòng đăng nhập", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(MainActivity.this, DonHangActivity.class);
        startActivity(intent);
    }
});
```

---

## 3. KIỂM TRA VÀ TEST

### 3.1 Kiểm tra Database
```sql
-- Kiểm tra cột mới đã được thêm
DESCRIBE sanphammoi;
DESCRIBE donhang;

-- Kiểm tra trigger
SHOW TRIGGERS;

-- Test thêm tồn kho
UPDATE sanphammoi SET soluongtonkho = 100 WHERE id = 1;
```

### 3.2 Test PHP API
Sử dụng Postman hoặc trình duyệt:

1. **Test kiểm tra tồn kho:**
   ```
   GET: http://localhost/appbandienthoai/kiemTraTonKho.php?idsp=1
   ```

2. **Test tạo đơn hàng:**
   ```
   POST: http://localhost/appbandienthoai/taoDonHang.php
   Body:
   - iduser: 1
   - diachi: "123 ABC Street"
   - sodienthoai: "0123456789"
   - soluong: 2
   - tongtien: "20000000"
   - ngaygiaodukien: "2024-01-15"
   - cartItems: [{"idsp":1,"soluong":2,"gia":"10000000"}]
   ```

3. **Test lấy đơn hàng:**
   ```
   GET: http://localhost/appbandienthoai/getDonHang.php?iduser=1
   ```

### 3.3 Test Android App
1. Build và chạy app
2. Đăng nhập với tài khoản
3. Thêm sản phẩm vào giỏ hàng
4. Nhấn "Đặt hàng"
5. Điền thông tin và đặt hàng
6. Kiểm tra "Đơn hàng của tôi"
7. Xem chi tiết đơn hàng

---

## 4. TÍNH NĂNG CHÍNH

### 4.1 Quản lý tồn kho
- Tự động giảm tồn kho khi đặt hàng thành công
- Kiểm tra tồn kho trước khi cho phép đặt hàng
- Trigger tự động cập nhật khi thêm/xóa/sửa đơn hàng

### 4.2 Đơn hàng
- Tạo mã đơn hàng unique tự động (DH + timestamp)
- Lưu ngày đặt hàng
- Cho phép chọn ngày giao hàng dự kiến
- Hiển thị trạng thái đơn hàng với màu sắc khác nhau:
  * Chờ xử lý (Cam)
  * Đang giao (Xanh dương)
  * Đã giao (Xanh lá)
  * Đã hủy (Đỏ)

### 4.3 Chi tiết đơn hàng
- Hiển thị đầy đủ thông tin đơn hàng
- Danh sách sản phẩm với hình ảnh
- Tổng tiền được format đẹp

---

## 5. LƯU Ý QUAN TRỌNG

### 5.1 Bảo mật
- Validate tất cả input từ client
- Sử dụng prepared statements để tránh SQL injection
- Kiểm tra quyền truy cập (user chỉ xem được đơn hàng của mình)

### 5.2 Xử lý lỗi
- Transaction đảm bảo dữ liệu không bị lỗi
- Rollback nếu có lỗi trong quá trình đặt hàng
- Thông báo lỗi rõ ràng cho user

### 5.3 Performance
- Sử dụng index cho các cột tìm kiếm thường xuyên
- Cache danh sách đơn hàng
- Lazy loading cho hình ảnh

### 5.4 Model GioHang
Đảm bảo model GioHang có các trường cần thiết:
```java
public class GioHang {
    private int idsp;
    private String tensp;
    private String hinhanh;
    private long giasp;
    private int soluong;
    
    // Getters and Setters
}
```

---

## 6. MỞ RỘNG TƯƠNG LAI

### 6.1 Tính năng có thể thêm:
- [ ] Hủy đơn hàng
- [ ] Đánh giá đơn hàng sau khi nhận
- [ ] Theo dõi vận chuyển
- [ ] Lịch sử thay đổi trạng thái
- [ ] Thông báo push khi đơn hàng thay đổi trạng thái
- [ ] Xuất hóa đơn PDF
- [ ] Tích hợp thanh toán online

### 6.2 Báo cáo cho Admin:
- Thống kê doanh thu theo ngày/tháng
- Top sản phẩm bán chạy
- Quản lý tồn kho - cảnh báo sắp hết hàng
- Quản lý tất cả đơn hàng

---

## 7. TROUBLESHOOTING

### Lỗi thường gặp:

1. **"Số lượng tồn kho không đủ"**
   - Kiểm tra cột soluongtonkho trong database
   - Cập nhật tồn kho: `UPDATE sanphammoi SET soluongtonkho = 100`

2. **"Lỗi kết nối"**
   - Kiểm tra BASE_URL trong Utils.java
   - Đảm bảo server PHP đang chạy
   - Kiểm tra internet permission trong AndroidManifest.xml

3. **"Trigger không hoạt động"**
   - Chạy lại file SQL để tạo trigger
   - Kiểm tra quyền MySQL user

4. **Layout lỗi**
   - Sync project with Gradle files
   - Clean và Rebuild project
   - Invalidate Caches / Restart

---

## 8. LIÊN HỆ HỖ TRỢ

Nếu gặp vấn đề, kiểm tra:
1. Logcat trong Android Studio
2. Error log của PHP (php_error.log)
3. MySQL error log

---

**Chúc bạn tích hợp thành công! 🎉**

