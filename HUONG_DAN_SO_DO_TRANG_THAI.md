# 📊 HƯỚNG DẪN SƠ ĐỒ TRẠNG THÁI HỆ THỐNG BÁN ĐỒNG HỒ

**File sơ đồ:** `STATE_DIAGRAM_HE_THONG.drawio`  
**Ngày tạo:** 30/11/2025  
**Công cụ:** Draw.io (diagrams.net)

---

## 📋 TỔNG QUAN

Sơ đồ trạng thái (State Diagram) mô tả **6 thành phần chính** của hệ thống và các chuyển đổi trạng thái của chúng:

1. **Tài khoản người dùng** - Quản lý trạng thái đăng nhập/đăng ký
2. **Sản phẩm** - Quản lý tồn kho
3. **Đơn hàng** - Vòng đời từ đặt hàng đến giao hàng
4. **Giỏ hàng** - Trạng thái mua sắm
5. **Voucher** - Trạng thái mã giảm giá
6. **Thanh toán** - Luồng thanh toán COD và VNPay

---

## 🎯 1. TRẠNG THÁI TÀI KHOẢN NGƯỜI DÙNG

### Các trạng thái:
```
● Start
→ Chưa đăng ký (Guest)
→ Đã đăng ký (Registered)
→ Đã đăng nhập User (role=0)
→ Quản trị viên (Admin - role=1)
→ Tài khoản bị khóa (Inactive)
```

### Chuyển đổi trạng thái:

| Từ trạng thái | Đến trạng thái | Sự kiện | File liên quan |
|--------------|---------------|---------|----------------|
| Start | Chưa đăng ký | Mở app lần đầu | - |
| Chưa đăng ký | Đã đăng ký | Đăng ký | `dangki.php` |
| Đã đăng ký | Đã đăng nhập (User) | Đăng nhập thường | `dangnhap.php` |
| Chưa đăng ký | Đã đăng nhập (User) | Đăng nhập Google | `dangnhap.php` (login_type=google) |
| Đã đăng ký | Admin | Đăng nhập với role=1 | `dangnhap.php` |
| Đã đăng nhập | Tài khoản bị khóa | Admin khóa tài khoản | `updateUserStatus.php` |
| Tài khoản bị khóa | Đã đăng nhập | Admin mở khóa | `updateUserStatus.php` |
| Đã đăng nhập | Chưa đăng ký | Đăng xuất | `DangNhapActivity.java` |

### Đặc điểm:
- ✅ Hỗ trợ đăng nhập Google (tự động tạo tài khoản)
- ✅ Phân quyền rõ ràng: User (role=0) vs Admin (role=1)
- ✅ Admin có thể khóa/mở khóa tài khoản
- ✅ Có chức năng quên mật khẩu qua OTP email

---

## 📦 2. TRẠNG THÁI SẢN PHẨM

### Các trạng thái:
```
● Start
→ Sản phẩm mới (Created)
→ Còn hàng (tonkho > 0)
→ Sắp hết hàng (tonkho < 10)
→ Hết hàng (tonkho = 0)
→ Đã xóa (Deleted)
```

### Chuyển đổi trạng thái:

| Từ trạng thái | Đến trạng thái | Sự kiện | File liên quan |
|--------------|---------------|---------|----------------|
| Start | Sản phẩm mới | Admin tạo sản phẩm | `Server/submit_new.php` |
| Sản phẩm mới | Còn hàng | Nhập kho (set tồn kho) | `setTonKho.php` |
| Còn hàng | Sắp hết hàng | Bán sản phẩm (tonkho giảm) | Trigger `after_insert_chitietdonhang` |
| Sắp hết hàng | Hết hàng | Bán tiếp đến hết | Trigger tự động |
| Sắp hết hàng | Còn hàng | Admin nhập thêm hàng | `capNhatTonKho.php` |
| Hết hàng | Còn hàng | Admin nhập lại hàng | `capNhatTonKho.php` |
| Còn hàng | Đã xóa | Admin xóa sản phẩm | `Server/xoa.php` |

### Đặc điểm:
- ✅ **Trigger tự động trừ tồn kho** khi thêm `chitietdonhang`
- ✅ Cảnh báo sắp hết hàng (tonkho < 10)
- ✅ Không cho phép đặt hàng khi hết hàng
- ✅ Admin có thể nhập/xuất kho thủ công

### Trigger MySQL quan trọng:
```sql
-- File: create_trigger_tonkho.sql
CREATE TRIGGER after_insert_chitietdonhang
AFTER INSERT ON chitietdonhang
FOR EACH ROW
BEGIN
    UPDATE sanphammoi
    SET soluongtonkho = soluongtonkho - NEW.soluong
    WHERE id = NEW.idsp;
END;
```

---

## 📦 3. TRẠNG THÁI ĐƠN HÀNG

### Các trạng thái:
```
● Start
→ Chờ xử lý (Chờ xử lý)
→ Đã thanh toán (VNPay Success) + Trừ tồn kho
→ Thanh toán thất bại (VNPay Failed)
→ Đang xử lý (Đang xử lý)
→ Đã chuẩn bị (Đã chuẩn bị)
→ Đang giao (Đã giao)
→ Thành công (Thành công)
→ Đã hủy (Đã hủy) + Hoàn tồn kho
◉ End
```

### Luồng chính (COD - Thanh toán khi nhận hàng):
```
Start → Chờ xử lý → Đang xử lý → Đã chuẩn bị → Đang giao → Thành công → End
```

### Luồng thanh toán VNPay:
```
Start → Chờ xử lý → [VNPay] → Đã thanh toán → Đang xử lý → ... → Thành công → End
                   ↓
              Thanh toán thất bại (End)
```

### Chuyển đổi trạng thái:

| Từ trạng thái | Đến trạng thái | Sự kiện | File liên quan |
|--------------|---------------|---------|----------------|
| Start | Chờ xử lý | User đặt hàng | `taoDonHang.php` |
| Chờ xử lý | Đã thanh toán | VNPay callback (code=00) | `Server/vnpay_return.php` |
| Chờ xử lý | Thanh toán thất bại | VNPay callback (code!=00) | `Server/vnpay_return.php` |
| Chờ xử lý | Đang xử lý | Admin xác nhận (COD) | `capNhatTrangThai.php` |
| Đã thanh toán | Đang xử lý | Tự động chuyển | - |
| Đang xử lý | Đã chuẩn bị | Admin cập nhật | `capNhatTrangThai.php` |
| Đã chuẩn bị | Đang giao | Admin/Shipper | `capNhatTrangThai.php` |
| Đang giao | Thành công | Giao thành công | `capNhatTrangThai.php` |
| Chờ xử lý | Đã hủy | Khách hàng hủy | `capNhatTrangThai.php` |
| Đang xử lý | Đã hủy | Admin hủy | `capNhatTrangThai.php` |

### Đặc điểm đặc biệt:

#### ✅ Quản lý tồn kho thông minh:
```php
// Khi hủy đơn → Hoàn lại tồn kho
if (!$prevCanceled && $newCanceled) {
    foreach ($chitietdonhang as $item) {
        UPDATE sanphammoi 
        SET soluongtonkho = soluongtonkho + $item['soluong']
        WHERE id = $item['idsp'];
    }
}
```

#### ✅ Tránh trừ tồn kho 2 lần:
```php
// File: Server/vnpay_return.php
if (intval($order['is_tonkho_updated'] ?? 0) == 1) {
    // Đã trừ tồn kho rồi, không trừ nữa
    return;
}
```

#### ⚠️ Ràng buộc nghiệp vụ:
- **KHÔNG** cho phép admin cập nhật đơn đã bị khách hàng hủy
- **KHÔNG** cho phép khách hàng hủy đơn đang giao hoặc đã giao
- Chỉ trừ tồn kho 1 lần duy nhất (khi thanh toán thành công)

---

## 🛒 4. TRẠNG THÁI GIỎ HÀNG

### Các trạng thái:
```
● Start
→ Giỏ hàng trống (Empty)
→ Có sản phẩm (Has Items)
→ Đã chọn mua (Items Selected)
→ Đang thanh toán (Checkout)
```

### Chuyển đổi trạng thái:

| Từ trạng thái | Đến trạng thái | Sự kiện | File liên quan |
|--------------|---------------|---------|----------------|
| Start | Giỏ hàng trống | Mở app | - |
| Giỏ hàng trống | Có sản phẩm | Thêm SP vào giỏ | `themGioHang.php`, `ChiTietActivity.java` |
| Có sản phẩm | Giỏ hàng trống | Xóa hết SP | `xoaToanBoGioHang.php` |
| Có sản phẩm | Đã chọn mua | Check checkbox | `GioHangAdapter.java` |
| Đã chọn mua | Có sản phẩm | Bỏ check | `GioHangAdapter.java` |
| Đã chọn mua | Đang thanh toán | Click "Đặt hàng" | `DatHangActivity.java` |
| Đang thanh toán | Giỏ hàng trống | Hoàn tất/Hủy | - |

### Đặc điểm:
- ✅ Phân biệt rõ `Utils.manggiohang` (tất cả SP) và `Utils.mangmuahang` (SP đã chọn)
- ✅ Kiểm tra tồn kho trước khi thêm vào giỏ
- ✅ Tính tổng tiền chỉ dựa trên SP đã chọn (checkbox)
- ✅ Sync giỏ hàng với server (nếu đã đăng nhập)

---

## 🎟️ 5. TRẠNG THÁI VOUCHER

### Các trạng thái:
```
● Start
→ Voucher mới (Created)
→ Đang hoạt động (Active - trang_thai=1)
→ Đã sử dụng (Used)
→ Hết hạn (Expired)
→ Đã hết lượt (Full Usage)
→ Ngưng hoạt động (Inactive - trang_thai=0)
```

### Chuyển đổi trạng thái:

| Từ trạng thái | Đến trạng thái | Sự kiện | File liên quan |
|--------------|---------------|---------|----------------|
| Start | Voucher mới | Admin tạo voucher | `addVoucher.php` |
| Voucher mới | Đang hoạt động | Admin kích hoạt | `updateVoucher.php` |
| Đang hoạt động | Đã sử dụng | User sử dụng | `Server/checkVoucher.php` |
| Đã sử dụng | Đã hết lượt | Dùng hết `so_luong` | Trigger tự động |
| Đang hoạt động | Hết hạn | Quá `ngay_het_han` | Auto check |
| Đang hoạt động | Ngưng hoạt động | Admin tắt | `updateVoucher.php` |
| Ngưng hoạt động | Đang hoạt động | Admin bật lại | `updateVoucher.php` |

### Loại voucher:
- **percent**: Giảm theo % (VD: 10% đơn hàng)
- **fixed**: Giảm số tiền cố định (VD: giảm 50,000đ)
- **freeship**: Miễn phí vận chuyển

### Điều kiện áp dụng:
- `don_toi_thieu`: Giá trị đơn hàng tối thiểu
- `ap_dung_cho`: all/new_user/old_user/first_order
- `gioi_han_moi_user`: Mỗi user được dùng bao nhiêu lần

### Đặc điểm:
- ✅ Trigger tự động tăng `da_su_dung` khi dùng voucher
- ✅ Lưu lịch sử sử dụng trong bảng `voucher_usage`
- ✅ Kiểm tra điều kiện trước khi áp dụng

---

## 💳 6. TRẠNG THÁI THANH TOÁN

### Các trạng thái:
```
● Start
→ Khởi tạo (Initialized)
→ COD (Thanh toán khi nhận)
→ VNPay đang xử lý (Pending)
→ VNPay thành công (Success)
→ VNPay thất bại (Failed)
◉ End
```

### Luồng COD:
```
Start → Khởi tạo → COD → End
```

### Luồng VNPay:
```
Start → Khởi tạo → VNPay Pending → VNPay Success/Failed → End
```

### Chuyển đổi trạng thái:

| Từ trạng thái | Đến trạng thái | Sự kiện | File liên quan |
|--------------|---------------|---------|----------------|
| Start | Khởi tạo | User chọn phương thức | `ThanhToanActivity.java` |
| Khởi tạo | COD | Chọn "Thanh toán khi nhận" | `taoDonHang.php` |
| Khởi tạo | VNPay Pending | Chọn VNPay | `Server/vnpay_create_payment.php` |
| VNPay Pending | VNPay Success | vnp_ResponseCode = 00 | `Server/vnpay_return.php` |
| VNPay Pending | VNPay Failed | vnp_ResponseCode != 00 | `Server/vnpay_return.php` |
| COD | End | Tạo đơn thành công | - |
| VNPay Success | End | Cập nhật đơn hàng | - |

### Mã trạng thái VNPay:
- `00`: Giao dịch thành công
- `07`: Trừ tiền thành công, nghi vấn giao dịch
- `09`: Giao dịch thất bại
- `10`: Khách hàng hủy
- `11`: Hết hạn thanh toán
- `24`: Khách hàng hủy

---

## 🎨 MÀU SẮC TRẠNG THÁI

| Màu | Ý nghĩa | Ví dụ |
|-----|---------|-------|
| 🟢 **Xanh lá** | Trạng thái hoạt động tốt | Đã đăng nhập, Còn hàng, Thành công |
| 🟡 **Vàng** | Trạng thái chờ/cảnh báo | Chờ xử lý, Sắp hết hàng, Đang thanh toán |
| 🔴 **Đỏ** | Trạng thái lỗi/hủy | Đã hủy, Hết hàng, Thanh toán thất bại |
| ⚪ **Xám** | Trạng thái không hoạt động | Tài khoản bị khóa, Đã xóa |
| 🟣 **Tím** | Trạng thái đặc biệt (Admin) | Quản trị viên |

---

## 🔧 CÁCH MỞ VÀ CHỈNH SỬA SƠ ĐỒ

### Bước 1: Cài đặt Draw.io
- **Online:** Truy cập https://app.diagrams.net/
- **Desktop:** Tải tại https://github.com/jgraph/drawio-desktop/releases

### Bước 2: Mở file
1. Mở Draw.io
2. File → Open from → Device
3. Chọn file `STATE_DIAGRAM_HE_THONG.drawio`

### Bước 3: Chỉnh sửa
- **Thêm trạng thái:** Kéo thả hình chữ nhật từ thanh bên trái
- **Thêm chuyển đổi:** Kéo từ trạng thái này sang trạng thái khác
- **Đổi màu:** Chọn hình → Style → Fill color

### Bước 4: Xuất file
- File → Export as → PNG/PDF/SVG

---

## 📚 TÀI LIỆU THAM KHẢO

### File PHP Backend:
```
Authentication:
├── dangki.php                    # Đăng ký
├── dangnhap.php                  # Đăng nhập
└── Server/reset_pass.php         # Quên mật khẩu

Product Management:
├── Server/submit_new.php         # Thêm sản phẩm
├── updatesp.php                  # Sửa sản phẩm
├── Server/xoa.php                # Xóa sản phẩm
├── setTonKho.php                 # Set tồn kho
└── capNhatTonKho.php            # Cập nhật tồn kho

Order Management:
├── taoDonHang.php                # Tạo đơn hàng
├── xemdonhang.php                # Xem đơn hàng
└── capNhatTrangThai.php         # Cập nhật trạng thái

Cart:
├── themGioHang.php               # Thêm giỏ hàng
├── capNhatGioHang.php           # Cập nhật giỏ hàng
├── xoaGioHang.php               # Xóa 1 sản phẩm
└── xoaToanBoGioHang.php         # Xóa toàn bộ

Payment:
├── Server/vnpay_create_payment.php
├── Server/vnpay_return.php
└── Server/vnpay_check_status.php

Voucher:
├── addVoucher.php
├── updateVoucher.php
├── deleteVoucher.php
├── Server/checkVoucher.php
└── Server/getVouchers.php
```

### File SQL:
```
├── create_trigger_tonkho.sql     # Trigger trừ tồn kho
├── create_giohang_table.sql      # Tạo bảng giỏ hàng
├── create_voucher_system.sql     # Tạo hệ thống voucher
├── update_vnpay_database.sql     # Cập nhật bảng VNPay
└── update_database_add_role.sql  # Thêm cột role
```

### File Java Android:
```
Activities:
├── DangKiActivity.java
├── DangNhapActivity.java
├── ChiTietActivity.java
├── GioHangActivity.java
├── DatHangActivity.java
├── ThanhToanActivity.java
└── XemDonActivity.java

Adapters:
├── GioHangAdapter.java
└── DonHangAdapter.java

Utils:
├── Utils.java                    # Biến global
└── PaperDB                       # Local storage
```

---

## ✅ CHECKLIST KIỂM TRA

### Trạng thái Tài khoản:
- [x] Đăng ký tài khoản mới
- [x] Đăng nhập bằng email/password
- [x] Đăng nhập bằng Google
- [x] Quên mật khẩu (OTP)
- [x] Phân quyền User/Admin
- [x] Khóa/Mở khóa tài khoản

### Trạng thái Sản phẩm:
- [x] Thêm sản phẩm mới
- [x] Nhập kho (set tồn kho)
- [x] Tự động trừ tồn kho khi bán
- [x] Cảnh báo sắp hết hàng
- [x] Không cho đặt khi hết hàng
- [x] Xóa sản phẩm

### Trạng thái Đơn hàng:
- [x] Tạo đơn hàng COD
- [x] Tạo đơn hàng VNPay
- [x] Admin xác nhận đơn
- [x] Cập nhật trạng thái đơn
- [x] Hủy đơn → Hoàn tồn kho
- [x] Tránh trừ tồn kho 2 lần

### Trạng thái Giỏ hàng:
- [x] Thêm sản phẩm vào giỏ
- [x] Tăng/giảm số lượng
- [x] Chọn sản phẩm mua (checkbox)
- [x] Xóa sản phẩm
- [x] Tính tổng tiền
- [x] Sync với server

### Trạng thái Voucher:
- [x] Tạo voucher
- [x] Kích hoạt/Tắt voucher
- [x] Kiểm tra điều kiện áp dụng
- [x] Áp dụng voucher vào đơn
- [x] Tự động tăng số lượt dùng
- [x] Hết hạn tự động

### Trạng thái Thanh toán:
- [x] Thanh toán COD
- [x] Thanh toán VNPay
- [x] Xác thực secure hash
- [x] Xử lý callback VNPay
- [x] Cập nhật trạng thái đơn

---

## 🐛 LỖI THƯỜNG GẶP VÀ KHẮC PHỤC

### 1. Tồn kho bị trừ 2 lần
**Nguyên nhân:** VNPay callback nhiều lần  
**Giải pháp:** Kiểm tra cột `is_tonkho_updated` trước khi trừ

### 2. Hủy đơn không hoàn tồn kho
**Nguyên nhân:** Logic thiếu trong `capNhatTrangThai.php`  
**Giải pháp:** Đã fix, cộng lại tồn kho khi chuyển sang "Đã hủy"

### 3. Voucher dùng được nhiều lần
**Nguyên nhân:** Chưa check `gioi_han_moi_user`  
**Giải pháp:** Đã fix trong `Server/checkVoucher.php`

### 4. Đăng nhập Google tạo nhiều tài khoản
**Nguyên nhân:** Không check email trùng  
**Giải pháp:** Đã fix, kiểm tra email trước khi INSERT

---

## 📞 HỖ TRỢ

Nếu có thắc mắc về sơ đồ trạng thái, vui lòng:
1. Đọc kỹ tài liệu này
2. Tham khảo các file báo cáo khác:
   - `KIEN_TRUC_HE_THONG_TOM_TAT.md`
   - `DANH_GIA_LOGIC_GIO_HANG_MUA_HANG.md`
   - `SO_DO_USE_CASE_CHINH_XAC.md`
3. Kiểm tra code trong các file PHP/Java tương ứng

---

**Tác giả:** GitHub Copilot  
**Phiên bản:** 1.0  
**Cập nhật lần cuối:** 30/11/2025

