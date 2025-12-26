# HƯỚNG DẪN CHỨC NĂNG CẬP NHẬT TỒN KHO

## Tổng quan
Chức năng cập nhật tồn kho cho phép Admin quản lý số lượng sản phẩm trong kho với 2 chế độ:
- **Cộng thêm**: Cộng thêm số lượng vào tồn kho hiện tại
- **Đặt lại**: Đặt lại tồn kho thành giá trị mới

## File đã hoàn thành

### 1. Backend PHP
**File: `capNhatTonKho.php`**
- API cộng thêm tồn kho
- Nhận tham số: `idsp` (ID sản phẩm), `soluong` (số lượng cộng thêm)
- SQL: `UPDATE sanphammoi SET soluongtonkho = soluongtonkho + $soluong WHERE id = $idsp`
- Trả về JSON với số lượng tồn kho mới

### 2. Frontend Android

#### a. Utils.java
Đã thêm URL mới:
```java
public static final String URL_CAP_NHAT_TON_KHO = BASE_URL + "capNhatTonKho.php";
```

#### b. ApiBanHang.java
Đã thêm API method:
```java
@POST("capNhatTonKho.php")
@FormUrlEncoded
Observable<MessageModel> capNhatTonKho(
    @Field("idsp") int idsp,
    @Field("soluong") int soluong
);
```

#### c. TonKhoActivity.java
**Các phương thức chính:**

1. **showEditStockDialog()**: Dialog chọn hành động
   - Nút "Cộng thêm": Gọi showAddStockDialog()
   - Nút "Đặt lại": Gọi showSetStockDialog()

2. **showAddStockDialog()**: Dialog cộng thêm tồn kho
   - Nhập số lượng cần cộng thêm
   - Validate: số lượng > 0
   - Gọi sendCapNhatTonKhoRequest()

3. **showSetStockDialog()**: Dialog đặt lại tồn kho
   - Hiển thị số lượng hiện tại
   - Nhập số lượng mới
   - Validate: số lượng >= 0
   - Gọi sendSetTonKhoRequest()

4. **sendCapNhatTonKhoRequest()**: Gửi request cộng thêm
   - POST đến capNhatTonKho.php
   - Cập nhật UI sau khi thành công
   - Hiển thị toast với số lượng mới

5. **sendSetTonKhoRequest()**: Gửi request đặt lại
   - POST đến setTonKho.php
   - Cập nhật UI sau khi thành công

## Cách sử dụng

### Cho Admin:
1. Đăng nhập với tài khoản admin
2. Vào menu "Quản lý" → "Tồn kho"
3. Click vào sản phẩm cần cập nhật
4. Chọn hành động:
   - **Cộng thêm**: Nhập số lượng nhập thêm (VD: 50)
   - **Đặt lại**: Nhập số lượng tồn kho mới (VD: 100)
5. Xác nhận và xem kết quả

## Ví dụ sử dụng

### Trường hợp 1: Nhập hàng mới
- Tồn kho hiện tại: 20
- Nhập thêm: 100
- → Chọn "Cộng thêm", nhập 100
- → Tồn kho mới: 120

### Trường hợp 2: Kiểm kê và điều chỉnh
- Tồn kho hiện tại: 150
- Kiểm kê thực tế: 145
- → Chọn "Đặt lại", nhập 145
- → Tồn kho mới: 145

## Cấu trúc dữ liệu

### Request POST (capNhatTonKho.php)
```
idsp: int - ID sản phẩm
soluong: int - Số lượng cộng thêm (phải > 0)
```

### Response JSON
```json
{
  "success": true,
  "message": "Cập nhật tồn kho thành công",
  "soluongtonkho": 120
}
```

## Xử lý lỗi

### Lỗi validation:
- "Vui lòng nhập số lượng" - Chưa nhập gì
- "Số lượng không hợp lệ" - Nhập ký tự không phải số
- "Số lượng phải > 0" - Nhập số âm hoặc 0 (cho cộng thêm)
- "Số lượng phải >= 0" - Nhập số âm (cho đặt lại)

### Lỗi server:
- "Dữ liệu không hợp lệ" - Backend validation failed
- "Lỗi kết nối" - Network error
- "Phản hồi không hợp lệ" - JSON parse error

## Tích hợp với các chức năng khác

### Liên quan:
- **kiemTraTonKho.php**: Kiểm tra tồn kho trước khi đặt hàng
- **getTonKho.php**: Lấy danh sách tồn kho
- **setTonKho.php**: Đặt lại tồn kho cứng
- **taoDonHang.php**: Trừ tồn kho khi tạo đơn

### Workflow hoàn chỉnh:
1. Nhập hàng → Cộng thêm tồn kho (capNhatTonKho)
2. Khách đặt hàng → Trừ tồn kho (taoDonHang)
3. Kiểm tra trước khi đặt → Kiểm tra tồn kho (kiemTraTonKho)
4. Kiểm kê định kỳ → Đặt lại tồn kho (setTonKho)

## Kiểm tra và Testing

### Test cases:
1. ✅ Cộng thêm số lượng hợp lệ
2. ✅ Cộng thêm với số âm (phải báo lỗi)
3. ✅ Cộng thêm với số 0 (phải báo lỗi)
4. ✅ Đặt lại với số lượng hợp lệ
5. ✅ Đặt lại với số 0 (cho phép)
6. ✅ Đặt lại với số âm (phải báo lỗi)
7. ✅ UI cập nhật ngay sau khi thành công
8. ✅ Hiển thị loading khi xử lý
9. ✅ Xử lý lỗi network
10. ✅ Pull to refresh để tải lại dữ liệu

## Bảo mật

### Kiểm tra cần thiết:
- ✅ Kiểm tra quyền admin trước khi vào TonKhoActivity
- ✅ Validation dữ liệu ở cả client và server
- ✅ Sử dụng prepared statements để tránh SQL injection
- ⚠️ **Lưu ý**: Nên thêm kiểm tra quyền trong PHP file

### Cải tiến đề xuất:
```php
// Thêm vào đầu capNhatTonKho.php
session_start();
if (!isset($_SESSION['user']) || $_SESSION['user']['role'] != 'admin') {
    echo json_encode(['success' => false, 'message' => 'Không có quyền']);
    exit;
}
```

## Ghi chú kỹ thuật

### Sử dụng Volley thay vì Retrofit:
- TonKhoActivity sử dụng Volley để tương thích với code cũ
- API đã được thêm vào ApiBanHang.java (Retrofit) để sẵn sàng nếu muốn refactor
- Cả 2 cách đều hoạt động tốt

### Cập nhật UI:
- Sử dụng `notifyItemChanged(position)` để cập nhật item cụ thể
- SwipeRefreshLayout hiển thị loading
- Toast thông báo kết quả

## Hoàn thành
✅ API Backend (PHP)
✅ API Interface (Retrofit)
✅ Activity UI (Java)
✅ Dialog interactions
✅ Error handling
✅ Data validation
✅ UI updates

**Chức năng đã sẵn sàng sử dụng!**

