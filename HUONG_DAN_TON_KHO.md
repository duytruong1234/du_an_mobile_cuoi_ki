# HƯỚNG DẪN CHỨC NĂNG TỒN KHO

## Tổng quan
Chức năng tồn kho đã được hoàn thiện cho phép:
1. **Khách hàng**: Xem số lượng tồn kho khi xem chi tiết sản phẩm
2. **Admin**: Quản lý và cập nhật tồn kho trong TonKhoActivity

## 🎯 Các file đã tạo/cập nhật

### 1. Backend PHP (Đã có sẵn)
- ✅ `kiemTraTonKho.php` - API kiểm tra tồn kho của 1 sản phẩm
- ✅ `capNhatTonKho.php` - API cập nhật tồn kho (dùng cho admin)
- ✅ `getTonKho.php` - API lấy danh sách tồn kho tất cả sản phẩm

### 2. Model Classes (Mới tạo/cập nhật)
- ✅ `TonKhoResponse.java` - Model response từ API kiemTraTonKho.php
- ✅ `SanPhamMoi.java` - Thêm trường `soluongtonkho`

### 3. Retrofit API (Đã cập nhật)
- ✅ `ApiBanHang.java` - Thêm endpoint `kiemTraTonKho(int idsp)`

### 4. Activity (Đã cập nhật)
- ✅ `ChiTietActivity.java` - Hiển thị tồn kho và kiểm tra khi thêm vào giỏ hàng

### 5. Layout XML (Đã cập nhật)
- ✅ `activity_chi_tiet.xml` - Thêm TextView hiển thị số lượng tồn kho

## 📋 Chi tiết triển khai

### 1. API Retrofit - ApiBanHang.java
```java
@GET("kiemTraTonKho.php")
Observable<TonKhoResponse> kiemTraTonKho(@Query("idsp") int idsp);
```

### 2. Model - TonKhoResponse.java
```java
public class TonKhoResponse {
    private boolean success;
    private String message;
    private TonKhoData data;
    
    public static class TonKhoData {
        private int id;
        private String tensp;
        private String hinhanh;
        private String giasp;
        private int soluongtonkho;
        private String mota;
        // getters and setters
    }
}
```

### 3. Hiển thị tồn kho - ChiTietActivity.java

#### Các thay đổi chính:
1. **Thêm biến**:
   - `TextView txtTonKho` - Hiển thị số lượng tồn kho
   - `int tonKhoHienTai` - Lưu số lượng tồn kho hiện tại
   - `CompositeDisposable` - Quản lý RxJava subscriptions
   - `ApiBanHang apiBanHang` - Instance để gọi API

2. **Hàm loadTonKho()**:
   - Gọi API `kiemTraTonKho` với ID sản phẩm
   - Hiển thị số lượng tồn kho
   - Thay đổi màu sắc dựa trên tình trạng:
     - 🔴 **Đỏ**: Hết hàng (≤ 0) - Vô hiệu hóa nút thêm giỏ hàng
     - 🟠 **Cam**: Sắp hết (≤ 5)
     - 🟢 **Xanh**: Còn nhiều (> 5)

3. **Hàm themGioHang()** - Cải tiến:
   - Kiểm tra tồn kho trước khi thêm
   - Kiểm tra số lượng đã có trong giỏ hàng
   - Không cho phép thêm nếu vượt quá tồn kho
   - Hiển thị thông báo rõ ràng về số lượng còn lại

### 4. Layout - activity_chi_tiet.xml

Thêm section hiển thị tồn kho:
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:layout_marginBottom="15dp"
    android:gravity="center_vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Tồn kho: "
        android:textSize="16sp"
        android:textColor="#555555" />

    <TextView
        android:id="@+id/txtTonKho"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Đang tải..."
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="#FF6600" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text=" sản phẩm"
        android:textSize="16sp"
        android:textColor="#555555" />

</LinearLayout>
```

## 🔄 Luồng hoạt động

### Khách hàng xem chi tiết sản phẩm:
1. Mở `ChiTietActivity`
2. Hiển thị thông tin sản phẩm
3. **Gọi API `kiemTraTonKho.php`** với ID sản phẩm
4. Nhận response và hiển thị số lượng tồn kho
5. Thay đổi màu sắc và trạng thái nút "Thêm vào giỏ hàng"

### Khách hàng thêm vào giỏ hàng:
1. Chọn số lượng muốn mua
2. Nhấn "Thêm vào giỏ hàng"
3. **Kiểm tra tồn kho**:
   - Nếu hết hàng → Thông báo "Sản phẩm hiện đã hết hàng"
   - Nếu vượt quá tồn kho → Thông báo "Không đủ hàng trong kho. Còn lại: X sản phẩm"
   - Nếu OK → Thêm vào giỏ hàng thành công

## 📱 Giao diện người dùng

### Màn hình chi tiết sản phẩm:
```
┌─────────────────────────────────┐
│  [Toolbar với giỏ hàng]        │
├─────────────────────────────────┤
│  [Hình ảnh sản phẩm]           │
│                                 │
│  Tên sản phẩm                  │
│  Giá: 10,000,000 đ             │
│                                 │
│  Tồn kho: 15 sản phẩm          │ ← MỚI THÊM
│                                 │
│  Số lượng:  [-] 1 [+]          │
│                                 │
│  [Thêm vào giỏ hàng]           │
│                                 │
│  Mô tả chi tiết sản phẩm       │
└─────────────────────────────────┘
```

## 🔒 Kiểm tra bảo mật

### Các kiểm tra được thực hiện:
- ✅ Kiểm tra số lượng > 0
- ✅ Kiểm tra không vượt quá tồn kho
- ✅ Kiểm tra số lượng đã có trong giỏ hàng
- ✅ Vô hiệu hóa nút thêm giỏ hàng khi hết hàng
- ✅ Hiển thị thông báo rõ ràng cho người dùng

## 🧪 Cách test

### 1. Test hiển thị tồn kho:
- Mở chi tiết bất kỳ sản phẩm nào
- Kiểm tra xem số tồn kho có hiển thị đúng không
- Kiểm tra màu sắc thay đổi theo số lượng

### 2. Test thêm vào giỏ hàng:
- Thử thêm số lượng nhỏ hơn tồn kho → Phải thành công
- Thử thêm số lượng bằng tồn kho → Phải thành công
- Thử thêm số lượng lớn hơn tồn kho → Phải báo lỗi
- Thêm sản phẩm 2 lần và kiểm tra tổng số lượng

### 3. Test sản phẩm hết hàng:
- Tìm sản phẩm có tồn kho = 0
- Kiểm tra nút "Thêm vào giỏ hàng" bị vô hiệu hóa
- Kiểm tra hiển thị "Hết hàng"

## 📝 Lưu ý quan trọng

1. **URL API**: Đảm bảo `Utils.BASE_URL` đúng với server của bạn
2. **Database**: Đảm bảo bảng `sanphammoi` có cột `soluongtonkho`
3. **Network Permission**: Đảm bảo app có quyền truy cập Internet
4. **RxJava**: Đảm bảo đã thêm dependencies RxJava và RxAndroid

## 🚀 Các tính năng đã hoàn thiện

✅ Hiển thị số lượng tồn kho trong màn hình chi tiết  
✅ Thay đổi màu sắc dựa trên tình trạng tồn kho  
✅ Vô hiệu hóa nút thêm giỏ hàng khi hết hàng  
✅ Kiểm tra tồn kho trước khi thêm vào giỏ hàng  
✅ Kiểm tra tổng số lượng trong giỏ hàng  
✅ Hiển thị thông báo rõ ràng khi không đủ hàng  
✅ Tích hợp Retrofit API với RxJava  
✅ Quản lý lifecycle với CompositeDisposable  

## 🎉 Kết luận

Chức năng tồn kho đã được hoàn thiện đầy đủ với các tính năng:
- Hiển thị số lượng tồn kho cho khách hàng
- Kiểm tra và ngăn chặn đặt hàng vượt quá tồn kho
- Giao diện trực quan với màu sắc phân biệt tình trạng
- Tích hợp hoàn chỉnh với hệ thống giỏ hàng hiện có

Khách hàng giờ đây có thể xem được số lượng còn lại trong kho trước khi quyết định mua hàng!

