# FIX LỖI VNPAY TRỪ TỒN KHO 2 LẦN

## ❌ VẤN ĐỀ
Khi thanh toán VNPay, mặc dù chỉ mua 1 sản phẩm nhưng tồn kho lại bị trừ 2 lần.

## 🔍 NGUYÊN NHÂN
Tồn kho bị trừ **2 lần** tại 2 thời điểm khác nhau:

### Lần 1: Khi tạo đơn hàng (vnpay_create_payment.php)
```
Tạo đơn hàng → INSERT vào chitietdonhang 
→ TRIGGER `after_chitietdonhang_insert` tự động chạy 
→ TRỪ TỒN KHO LẦN 1 ✅
```

### Lần 2: Khi VNPay callback (vnpay_return.php)
```
VNPay trả kết quả → vnpay_return.php nhận callback
→ Code thủ công UPDATE sanphammoi SET soluongtonkho = ...
→ TRỪ TỒN KHO LẦN 2 ✅
```

**Kết quả:** Mua 1 sản phẩm → Tồn kho trừ 2 ❌

## ✅ GIẢI PHÁP

### Bước 1: Xóa Trigger tự động trừ tồn kho
Chạy file SQL: `drop_trigger_tonkho.sql`

```sql
DROP TRIGGER IF EXISTS after_chitietdonhang_insert;
DROP TRIGGER IF EXISTS after_chitietdonhang_delete;
DROP TRIGGER IF EXISTS giam_tonkho_khi_dat;
DROP TRIGGER IF EXISTS tang_tonkho_khi_xoa;
DROP TRIGGER IF EXISTS capnhat_tonkho_khi_sua;
```

**Lý do:** Chỉ nên trừ tồn kho khi khách hàng THỰC SỰ THANH TOÁN THÀNH CÔNG, không phải lúc tạo đơn hàng.

### Bước 2: Giữ lại logic trừ tồn kho trong vnpay_return.php
File `vnpay_return.php` đã có code trừ tồn kho đúng:

```php
// Giảm tồn kho dựa trên chitietdonhang
$resDetails = mysqli_query($conn, "SELECT idsp, soluong FROM chitietdonhang WHERE iddonhang = $iddonhang");
while ($detail = mysqli_fetch_assoc($resDetails)) {
    $idsp = intval($detail['idsp']);
    $sl = intval($detail['soluong']);
    mysqli_query($conn, "UPDATE sanphammoi SET soluongtonkho = GREATEST(0, soluongtonkho - $sl) WHERE id = $idsp");
}
```

### Bước 3: Thêm cơ chế kiểm tra để tránh trừ 2 lần (Optional - An toàn hơn)
Thêm cột `is_tonkho_updated` vào bảng `donhang`:

```sql
ALTER TABLE donhang ADD COLUMN is_tonkho_updated TINYINT(1) DEFAULT 0;
```

Sau đó cập nhật code trong `vnpay_return.php`:

```php
// Kiểm tra xem đã trừ tồn kho chưa
if ($order['is_tonkho_updated'] == 1) {
    // Đã trừ rồi, không trừ nữa
    mysqli_commit($conn);
    $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=success&amount=" . urlencode($vnp_Amount);
    redirectToApp($deepLink, 'success', $vnp_TxnRef, $vnp_Amount, $vnp_TransactionNo, $vnp_BankCode);
    exit;
}

// Giảm tồn kho
$resDetails = mysqli_query($conn, "SELECT idsp, soluong FROM chitietdonhang WHERE iddonhang = $iddonhang");
while ($detail = mysqli_fetch_assoc($resDetails)) {
    $idsp = intval($detail['idsp']);
    $sl = intval($detail['soluong']);
    mysqli_query($conn, "UPDATE sanphammoi SET soluongtonkho = GREATEST(0, soluongtonkho - $sl) WHERE id = $idsp");
}

// Đánh dấu đã trừ tồn kho
mysqli_query($conn, "UPDATE donhang SET is_tonkho_updated = 1 WHERE id = $iddonhang");
```

## 📋 CÁCH THỰC HIỆN

### Cách 1: Chỉ xóa trigger (Nhanh)
1. Mở phpMyAdmin
2. Chọn database `appbandienthoai`
3. Paste và chạy nội dung file `drop_trigger_tonkho.sql`
4. Test lại thanh toán VNPay

### Cách 2: Xóa trigger + Thêm cơ chế bảo vệ (An toàn)
1. Làm theo Cách 1
2. Thêm cột `is_tonkho_updated` vào bảng `donhang`
3. Cập nhật code trong `vnpay_return.php` như hướng dẫn ở Bước 3

## 🧪 KIỂM TRA SAU KHI FIX

1. **Kiểm tra trigger đã xóa chưa:**
```sql
SHOW TRIGGERS WHERE `Table` = 'chitietdonhang';
```
Kết quả phải trả về **Empty set** (không có trigger nào)

2. **Test thanh toán VNPay:**
   - Chọn 1 sản phẩm có tồn kho = 10
   - Mua 1 sản phẩm qua VNPay
   - Thanh toán thành công
   - Kiểm tra tồn kho → Phải còn **9** (không phải 8)

3. **Test hủy đơn:**
   - Nếu thanh toán thất bại/hủy → Tồn kho KHÔNG bị trừ ✅

## 📌 LƯU Ý

- **Đơn hàng COD:** Nếu có thanh toán COD (không qua VNPay), cần thêm logic trừ tồn kho tại API xử lý COD
- **Đơn hàng cũ:** Các đơn hàng đã bị trừ 2 lần trước đây cần được hoàn lại tồn kho thủ công
- **Backup:** Nên backup database trước khi chạy script xóa trigger

## ✅ KẾT QUẢ MONG ĐỢI

Sau khi fix:
- Mua 1 sản phẩm → Tồn kho trừ đúng 1 ✅
- Hủy đơn → Tồn kho không bị trừ ✅
- Thanh toán thất bại → Tồn kho không bị trừ ✅
- VNPay callback nhiều lần → Tồn kho chỉ trừ 1 lần ✅ (nếu áp dụng Cách 2)

