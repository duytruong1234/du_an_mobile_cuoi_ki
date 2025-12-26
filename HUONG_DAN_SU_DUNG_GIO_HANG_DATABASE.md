# HƯỚNG DẪN SỬ DỤNG HỆ THỐNG GIỎ HÀNG VỚI DATABASE

## 📋 TỔNG QUAN

Hệ thống giỏ hàng mới đã được nâng cấp để lưu trữ dữ liệu vào **MySQL Database** thay vì chỉ lưu trong RAM như trước. Điều này mang lại nhiều lợi ích:

✅ **Lưu trữ lâu dài**: Giỏ hàng không bị mất khi đóng app
✅ **Đồng bộ đa thiết bị**: Có thể truy cập giỏ hàng từ nhiều thiết bị
✅ **Backup dữ liệu**: An toàn hơn với database
✅ **Quản lý tốt hơn**: Admin có thể xem thống kê giỏ hàng

---

## 🗄️ BƯỚC 1: TẠO BẢNG DATABASE

### 1.1. Mở phpMyAdmin
- Truy cập: `http://localhost/phpmyadmin`
- Chọn database: `appbandienthoai`

### 1.2. Chạy Script SQL
- Click tab **SQL** trong phpMyAdmin
- Copy toàn bộ nội dung file `create_giohang_table.sql`
- Paste vào và click **Go** (Thực hiện)

### 1.3. Cấu trúc bảng `giohang`

```sql
CREATE TABLE `giohang` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `iduser` int(11) NOT NULL,              -- ID người dùng
  `idsp` int(11) NOT NULL,                -- ID sản phẩm
  `tensp` varchar(255) NOT NULL,          -- Tên sản phẩm
  `giasp` bigint(20) NOT NULL,            -- Giá sản phẩm
  `hinhsp` varchar(500) DEFAULT NULL,     -- URL hình ảnh
  `soluong` int(11) NOT NULL DEFAULT 1,   -- Số lượng
  `ngaythem` timestamp DEFAULT CURRENT_TIMESTAMP,
  `ngaycapnhat` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_product` (`iduser`, `idsp`)
);
```

**Lưu ý**: Constraint `UNIQUE KEY` đảm bảo mỗi user chỉ có 1 record cho mỗi sản phẩm.

---

## 🔌 BƯỚC 2: API ĐÃ TẠO

Các file PHP API đã được tạo sẵn trong thư mục gốc:

### 2.1. `themGioHang.php`
**Chức năng**: Thêm sản phẩm vào giỏ hàng
**Parameters**:
- `iduser`: ID người dùng (int)
- `idsp`: ID sản phẩm (int)
- `tensp`: Tên sản phẩm (string)
- `giasp`: Giá sản phẩm (bigint)
- `hinhsp`: URL hình ảnh (string)
- `soluong`: Số lượng (int)

**Logic**:
- Nếu sản phẩm đã có trong giỏ → **Cộng thêm số lượng**
- Nếu sản phẩm chưa có → **Thêm mới**

### 2.2. `getGioHang.php`
**Chức năng**: Lấy danh sách giỏ hàng của user
**Parameters**:
- `iduser`: ID người dùng (int)

**Response**: Array các sản phẩm trong giỏ hàng

### 2.3. `xoaGioHang.php`
**Chức năng**: Xóa 1 sản phẩm khỏi giỏ hàng
**Parameters**:
- `iduser`: ID người dùng (int)
- `idsp`: ID sản phẩm cần xóa (int)

### 2.4. `capNhatGioHang.php`
**Chức năng**: Cập nhật số lượng sản phẩm
**Parameters**:
- `iduser`: ID người dùng (int)
- `idsp`: ID sản phẩm (int)
- `soluong`: Số lượng mới (int, nếu = 0 thì xóa)

### 2.5. `xoaToanBoGioHang.php`
**Chức năng**: Xóa toàn bộ giỏ hàng của user
**Parameters**:
- `iduser`: ID người dùng (int)

---

## 📱 BƯỚC 3: TÍCH HỢP VÀO ANDROID

### 3.1. API Interface đã được thêm vào `ApiBanHang.java`

```java
// Thêm sản phẩm vào giỏ hàng
@POST("themGioHang.php")
@FormUrlEncoded
Observable<MessageModel> themGioHang(
    @Field("iduser") int iduser,
    @Field("idsp") int idsp,
    @Field("tensp") String tensp,
    @Field("giasp") long giasp,
    @Field("hinhsp") String hinhsp,
    @Field("soluong") int soluong
);

// Lấy giỏ hàng
@POST("getGioHang.php")
@FormUrlEncoded
Observable<MessageModel> getGioHang(
    @Field("iduser") int iduser
);

// Và các API khác...
```

### 3.2. URL Constants đã được thêm vào `Utils.java`

```java
public static final String URL_THEM_GIO_HANG = BASE_URL + "themGioHang.php";
public static final String URL_GET_GIO_HANG = BASE_URL + "getGioHang.php";
public static final String URL_XOA_GIO_HANG = BASE_URL + "xoaGioHang.php";
public static final String URL_CAP_NHAT_GIO_HANG = BASE_URL + "capNhatGioHang.php";
public static final String URL_XOA_TOAN_BO_GIO_HANG = BASE_URL + "xoaToanBoGioHang.php";
```

---

## 🔧 BƯỚC 4: CẬP NHẬT CODE ANDROID (KHUYẾN NGHỊ)

### 4.1. Sửa `ChiTietActivity.java` - Phương thức `themGioHang()`

**TÙY CHỌN 1: Lưu cả local và server** (Khuyến nghị)

```java
private void themGioHang() {
    if (sanPhamMoi == null) return;

    // Kiểm tra đăng nhập
    if (Utils.user_current == null) {
        Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        return;
    }

    // Kiểm tra tồn kho
    if (tonKhoHienTai <= 0) {
        Toast.makeText(this, "Sản phẩm hiện đã hết hàng", Toast.LENGTH_SHORT).show();
        return;
    }

    // ... code kiểm tra số lượng tồn kho ...

    // 1. Thêm vào local (giữ nguyên code cũ)
    if (Utils.manggiohang == null) {
        Utils.manggiohang = new ArrayList<>();
    }
    
    // ... code thêm vào Utils.manggiohang như cũ ...

    // 2. Đồng bộ lên server
    long giaDonVi;
    try {
        String raw = sanPhamMoi.getGiasp() == null ? "" : sanPhamMoi.getGiasp();
        double price = Double.parseDouble(raw.trim());
        giaDonVi = (long) price;
    } catch (Exception ex) {
        giaDonVi = 0;
        Log.w("ChiTietActivity", "Không parse được giá", ex);
    }

    compositeDisposable.add(apiBanHang.themGioHang(
            Utils.user_current.getId(),
            sanPhamMoi.getId(),
            sanPhamMoi.getTensp(),
            giaDonVi,
            sanPhamMoi.getHinhanh(),
            soLuong
    ).subscribeOn(Schedulers.io())
     .observeOn(AndroidSchedulers.mainThread())
     .subscribe(
         messageModel -> {
             if (messageModel.isSuccess()) {
                 Log.d("ChiTietActivity", "Đã sync giỏ hàng lên server");
             }
         },
         throwable -> {
             Log.e("ChiTietActivity", "Lỗi sync giỏ hàng: " + throwable.getMessage());
         }
     ));

    Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
}
```

**TÙY CHỌN 2: Chỉ lưu server và load về khi cần**

Nếu muốn loại bỏ hoàn toàn `Utils.manggiohang` và chỉ dùng database, bạn cần:
1. Load giỏ hàng từ server khi vào `GioHangActivity`
2. Xóa toàn bộ logic local storage
3. Mọi thao tác đều qua API

### 4.2. Sửa `GioHangActivity.java` - Load giỏ hàng từ server

**Thêm phương thức load giỏ hàng**:

```java
private void loadGioHangFromServer() {
    if (Utils.user_current == null) return;

    compositeDisposable.add(apiBanHang.getGioHang(Utils.user_current.getId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            messageModel -> {
                if (messageModel.isSuccess()) {
                    // Parse JSON result thành List<GioHang>
                    // Cập nhật Utils.manggiohang
                    // Refresh adapter
                    Log.d("GioHangActivity", "Load giỏ hàng thành công");
                }
            },
            throwable -> {
                Log.e("GioHangActivity", "Lỗi load giỏ hàng: " + throwable.getMessage());
            }
        ));
}
```

**Gọi trong `onCreate()`**:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ... code khác ...
    
    loadGioHangFromServer(); // Thêm dòng này
}
```

### 4.3. Sửa `GioHangAdapter.java` - Xóa sản phẩm

Khi user click xóa sản phẩm, gọi API `xoaGioHang.php`:

```java
holder.imgdelete.setOnClickListener(v -> {
    if (Utils.user_current != null) {
        // Xóa trên server
        // Call API xoaGioHang(Utils.user_current.getId(), gioHang.getIdsp())
    }
    // Xóa local
    Utils.manggiohang.remove(position);
    notifyItemRemoved(position);
    EventBus.getDefault().postSticky(new TinhTongEvent());
});
```

---

## 🧪 BƯỚC 5: TESTING

### 5.1. Test Manual

1. **Thêm sản phẩm vào giỏ hàng**:
   - Mở app → Chi tiết sản phẩm → Click "Thêm vào giỏ"
   - Kiểm tra database: `SELECT * FROM giohang WHERE iduser = 1;`

2. **Xem giỏ hàng**:
   - Click icon giỏ hàng
   - Kiểm tra hiển thị đúng sản phẩm

3. **Đóng app và mở lại**:
   - **Nếu dùng TÙY CHỌN 1**: Giỏ hàng vẫn còn trong local
   - **Nếu dùng TÙY CHỌN 2**: Load từ server khi mở app

4. **Xóa sản phẩm**:
   - Trong giỏ hàng, click xóa
   - Kiểm tra database đã xóa

### 5.2. Test với Postman

**Test themGioHang.php**:
```
POST http://localhost/appbandienthoai/themGioHang.php
Body (form-data):
- iduser: 1
- idsp: 5
- tensp: Đồng hồ ABC
- giasp: 5000000
- hinhsp: dongho1.jpg
- soluong: 2
```

**Test getGioHang.php**:
```
POST http://localhost/appbandienthoai/getGioHang.php
Body (form-data):
- iduser: 1
```

---

## 📊 BƯỚC 6: QUERY HỮU ÍCH

### Xem tất cả giỏ hàng
```sql
SELECT u.username, g.tensp, g.soluong, g.giasp, g.ngaythem
FROM giohang g
JOIN user u ON g.iduser = u.id
ORDER BY g.ngaythem DESC;
```

### Thống kê giỏ hàng theo user
```sql
SELECT * FROM view_giohang_thongke;
```

### Xóa giỏ hàng cũ (quá 30 ngày không cập nhật)
```sql
DELETE FROM giohang 
WHERE ngaycapnhat < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

### Tìm sản phẩm phổ biến trong giỏ hàng
```sql
SELECT idsp, tensp, COUNT(*) as so_user, SUM(soluong) as tong_sl
FROM giohang
GROUP BY idsp, tensp
ORDER BY so_user DESC
LIMIT 10;
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

1. **Xử lý khi user chưa đăng nhập**:
   - Lưu giỏ hàng tạm trong local
   - Khi đăng nhập → Sync lên server

2. **Xử lý conflict**:
   - Nếu có cả local và server → Merge hoặc chọn 1 trong 2
   - Khuyến nghị: Ưu tiên server (mới nhất)

3. **Performance**:
   - Cache giỏ hàng local để giảm request
   - Chỉ sync khi cần (thêm, xóa, cập nhật)

4. **Security**:
   - Validate `iduser` để đảm bảo user chỉ truy cập giỏ hàng của mình
   - Có thể thêm token authentication

5. **Tồn kho**:
   - Kiểm tra tồn kho trước khi checkout (không chỉ khi thêm)
   - Sản phẩm trong giỏ có thể hết hàng sau khi thêm

---

## 🎯 KẾT LUẬN

Hệ thống giỏ hàng với database đã sẵn sàng sử dụng! Bạn có thể:

✅ **Dùng ngay**: Chỉ cần chạy SQL script và test API
✅ **Tích hợp dần**: Giữ code cũ, chỉ thêm sync server
✅ **Migration hoàn toàn**: Chuyển toàn bộ sang database

**Khuyến nghị**: Bắt đầu với **TÙY CHỌN 1** (lưu cả local và server) để đảm bảo tính tương thích ngược và trải nghiệm người dùng tốt nhất.

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra connection database trong `connect.php`
2. Xem log Android: `adb logcat | grep GioHang`
3. Kiểm tra response API bằng Postman
4. Đảm bảo bảng `giohang` đã được tạo đúng

**Chúc bạn thành công! 🚀**

