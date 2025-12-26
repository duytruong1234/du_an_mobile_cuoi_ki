# 🎫 HỆ THỐNG VOUCHER/MÃ GIẢM GIÁ - HƯỚNG DẪN HOÀN CHỈNH

**Ngày tạo:** 03/11/2025  
**Tác giả:** GitHub Copilot  
**Phiên bản:** 1.0

---

## 📋 MỤC LỤC
1. [Tổng quan logic hệ thống](#1-tổng-quan-logic-hệ-thống)
2. [Cấu trúc database](#2-cấu-trúc-database)
3. [Các loại voucher](#3-các-loại-voucher)
4. [Quy trình hoạt động](#4-quy-trình-hoạt-động)
5. [Hướng dẫn cài đặt](#5-hướng-dẫn-cài-đặt)
6. [Ví dụ thực tế](#6-ví-dụ-thực-tế)
7. [API Reference](#7-api-reference)

---

## 1. TỔNG QUAN LOGIC HỆ THỐNG

### 🎯 **LOGIC VOUCHER TRONG THƯƠNG MẠI ĐIỆN TỬ**

```
┌─────────────────────────────────────────────────────────────┐
│                  LUỒNG SỬ DỤNG VOUCHER                       │
└─────────────────────────────────────────────────────────────┘

1️⃣ ADMIN TẠO MÃ GIẢM GIÁ
   ├─ Mã voucher: "NEWUSER20", "FREESHIP50K"
   ├─ Loại giảm: %, số tiền cố định, miễn phí ship
   ├─ Điều kiện: Giá trị đơn tối thiểu
   ├─ Đối tượng: User mới/cũ, đơn đầu tiên
   └─ Giới hạn: Số lượng, thời gian, số lần/user

2️⃣ KHÁCH HÀNG NHẬN VOUCHER
   ├─ Tự động: Đăng ký mới → nhận mã NEWUSER20
   ├─ Thủ công: Nhập mã tại màn hình thanh toán
   ├─ Xem danh sách: Màn hình "Mã giảm giá của tôi"
   └─ Push notification: Thông báo voucher mới

3️⃣ ÁP DỤNG MÃ KHI THANH TOÁN
   ├─ Nhập mã voucher
   ├─ Hệ thống kiểm tra:
   │  ✓ Mã có tồn tại?
   │  ✓ Còn hạn sử dụng?
   │  ✓ Đơn hàng >= giá trị tối thiểu?
   │  ✓ User đủ điều kiện? (mới/cũ)
   │  ✓ User còn lượt dùng?
   │  ✓ Voucher còn số lượng?
   ├─ Tính số tiền giảm
   ├─ Hiển thị tổng tiền SAU GIẢM
   └─ Lưu thông tin voucher vào đơn hàng

4️⃣ SAU KHI ĐẶT HÀNG THÀNH CÔNG
   ├─ Lưu lịch sử sử dụng voucher
   ├─ Tăng số lượng đã sử dụng
   ├─ Không cho dùng lại (nếu giới hạn 1 lần)
   └─ Ghi nhận vào đơn hàng
```

---

## 2. CẤU TRÚC DATABASE

### 📊 **3 BẢNG CHÍNH**

#### **Bảng 1: `voucher` - Quản lý mã giảm giá**

```sql
CREATE TABLE `voucher` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `ma_voucher` VARCHAR(50) UNIQUE,          -- Mã nhập vào: "NEWUSER20"
  `ten_voucher` VARCHAR(200),               -- Tên hiển thị
  `mo_ta` TEXT,                             -- Mô tả chi tiết
  
  -- LOẠI GIẢM GIÁ
  `loai_giam` ENUM('percent','fixed','freeship'),  
  `gia_tri_giam` DECIMAL(10,2),            -- 20 (%) hoặc 100000 (đồng)
  `giam_toi_da` DECIMAL(10,2),             -- Giảm tối đa (với %)
  
  -- ĐIỀU KIỆN
  `don_toi_thieu` DECIMAL(10,2),           -- Đơn hàng tối thiểu
  `ap_dung_cho` ENUM('all','new_user','old_user','first_order'),
  
  -- GIỚI HẠN
  `so_luong` INT,                          -- NULL = không giới hạn
  `da_su_dung` INT DEFAULT 0,
  `gioi_han_moi_user` INT DEFAULT 1,       -- Mỗi user dùng bao nhiêu lần
  
  -- THỜI GIAN
  `ngay_bat_dau` DATETIME,
  `ngay_het_han` DATETIME,
  `trang_thai` TINYINT(1) DEFAULT 1
);
```

#### **Bảng 2: `voucher_usage` - Lịch sử sử dụng**

```sql
CREATE TABLE `voucher_usage` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `voucher_id` INT,                        -- ID voucher đã dùng
  `user_id` INT,                           -- Ai dùng
  `donhang_id` INT,                        -- Đơn hàng nào
  `ma_donhang` VARCHAR(50),
  `gia_tri_don_hang` DECIMAL(10,2),       -- Tổng tiền trước giảm
  `gia_tri_giam` DECIMAL(10,2),           -- Số tiền đã giảm
  `ngay_su_dung` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`voucher_id`) REFERENCES `voucher`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);
```

#### **Bảng 3: Cập nhật bảng `donhang`**

```sql
ALTER TABLE `donhang` ADD COLUMN:
  `voucher_id` INT,                        -- ID voucher đã dùng
  `ma_voucher` VARCHAR(50),                -- Mã voucher
  `gia_tri_giam` DECIMAL(10,2) DEFAULT 0,  -- Số tiền giảm
  `tong_truoc_giam` DECIMAL(10,2)          -- Tổng tiền gốc
```

---

## 3. CÁC LOẠI VOUCHER

### 🏷️ **PHÂN LOẠI THEO CÁCH GIẢM GIÁ**

#### **1. GIẢM THEO % (`percent`)**
```
Ví dụ: NEWUSER20 - Giảm 20%
├─ Giá trị giảm: 20 (%)
├─ Giảm tối đa: 200,000đ
├─ Đơn tối thiểu: 500,000đ
└─ Tính toán:
   Đơn 1,000,000đ → Giảm 200,000đ (20%)
   Đơn 2,000,000đ → Giảm 200,000đ (tối đa, không phải 400k)
```

#### **2. GIẢM SỐ TIỀN CỐ ĐỊNH (`fixed`)**
```
Ví dụ: GIAM200K - Giảm 200,000đ
├─ Giá trị giảm: 200000 (đồng)
├─ Đơn tối thiểu: 2,000,000đ
└─ Tính toán:
   Đơn 2,000,000đ → Giảm 200,000đ
   Đơn 3,000,000đ → Giảm 200,000đ (cố định)
```

#### **3. MIỄN PHÍ SHIP (`freeship`)**
```
Ví dụ: FREESHIP50K - Free ship
├─ Giá trị giảm: 50000 (phí ship)
├─ Đơn tối thiểu: 500,000đ
└─ Tính toán:
   Đơn 500,000đ + Ship 50,000đ → Giảm 50,000đ
```

---

### 👥 **PHÂN LOẠI THEO ĐỐI TƯỢNG**

#### **1. User mới (`new_user`)**
```
Điều kiện: Chưa có đơn hàng nào
Ví dụ: NEWUSER20 - Giảm 20% cho khách mới
```

#### **2. Đơn hàng đầu tiên (`first_order`)**
```
Điều kiện: Đơn hàng đầu tiên (count = 0)
Ví dụ: FIRSTORDER - Giảm 100k đơn đầu
```

#### **3. Khách hàng cũ (`old_user`)**
```
Điều kiện: Đã có ít nhất 1 đơn hàng
Ví dụ: OLDUSER10 - Tri ân khách thân thiết
```

#### **4. Tất cả (`all`)**
```
Điều kiện: Không giới hạn đối tượng
Ví dụ: SALE15 - Giảm 15% cho mọi người
```

---

## 4. QUY TRÌNH HOẠT ĐỘNG

### 🔄 **LUỒNG XỬ LÝ CHI TIẾT**

#### **BƯỚC 1: Khách hàng nhập mã voucher**

```java
// Tại màn hình ThanhToanActivity
EditText edtVoucher = findViewById(R.id.edt_voucher);
Button btnApDung = findViewById(R.id.btn_ap_dung_voucher);

btnApDung.setOnClickListener(v -> {
    String maVoucher = edtVoucher.getText().toString().trim();
    kiemTraVoucher(maVoucher);
});
```

#### **BƯỚC 2: Gọi API kiểm tra voucher**

```java
private void kiemTraVoucher(String maVoucher) {
    int userId = Utils.user_current.getId();
    double tongTien = 1500000; // Ví dụ đơn 1.5 triệu
    
    compositeDisposable.add(apiBanHang.checkVoucher(maVoucher, userId, tongTien)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            response -> {
                if (response.isSuccess()) {
                    // ✅ Voucher hợp lệ
                    Voucher voucher = response.getVoucher();
                    TinhToan tinhToan = response.getTinh_toan();
                    
                    // Hiển thị số tiền giảm
                    double giaTriGiam = tinhToan.getGia_tri_giam();
                    double tongSauGiam = tinhToan.getTong_sau_giam();
                    
                    Toast.makeText(this, 
                        "Áp dụng thành công! Giảm " + giaTriGiam, 
                        Toast.LENGTH_SHORT).show();
                    
                    // Cập nhật UI
                    txtTongTien.setText("Tổng: " + tongSauGiam);
                    txtGiamGia.setText("-" + giaTriGiam);
                    
                    // Lưu voucher để dùng khi đặt hàng
                    voucherDaChon = voucher;
                    giaTriGiamVoucher = giaTriGiam;
                    
                } else {
                    // ❌ Voucher không hợp lệ
                    Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
                }
            },
            error -> {
                Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        ));
}
```

#### **BƯỚC 3: Xử lý tại PHP (checkVoucher.php)**

```php
// Kiểm tra voucher
$voucher = mysqli_query($conn, 
    "SELECT * FROM voucher 
     WHERE ma_voucher = '$ma_voucher' 
     AND trang_thai = 1 
     AND ngay_bat_dau <= NOW() 
     AND ngay_het_han >= NOW()");

// Kiểm tra số lượng
if ($voucher['so_luong'] != NULL && $voucher['da_su_dung'] >= $voucher['so_luong']) {
    return error("Voucher đã hết");
}

// Kiểm tra giá trị đơn tối thiểu
if ($tong_tien < $voucher['don_toi_thieu']) {
    return error("Đơn tối thiểu " . $voucher['don_toi_thieu']);
}

// Kiểm tra đối tượng (new_user, old_user, etc.)
if ($voucher['ap_dung_cho'] == 'new_user') {
    $count = mysqli_query($conn, "SELECT COUNT(*) FROM donhang WHERE iduser = $user_id");
    if ($count > 0) {
        return error("Chỉ dành cho khách mới");
    }
}

// Tính giá trị giảm
if ($voucher['loai_giam'] == 'percent') {
    $gia_tri_giam = ($tong_tien * $voucher['gia_tri_giam']) / 100;
    if ($voucher['giam_toi_da'] && $gia_tri_giam > $voucher['giam_toi_da']) {
        $gia_tri_giam = $voucher['giam_toi_da'];
    }
} else if ($voucher['loai_giam'] == 'fixed') {
    $gia_tri_giam = $voucher['gia_tri_giam'];
}

$tong_sau_giam = $tong_tien - $gia_tri_giam;

return success([
    'voucher' => $voucher,
    'tinh_toan' => [
        'tong_truoc_giam' => $tong_tien,
        'gia_tri_giam' => $gia_tri_giam,
        'tong_sau_giam' => $tong_sau_giam
    ]
]);
```

#### **BƯỚC 4: Đặt hàng với voucher**

```java
// Tại DatHangActivity hoặc ThanhToanActivity
private void datHangVoiVoucher() {
    // Tạo đơn hàng bình thường
    String cartJson = new Gson().toJson(Utils.mangmuahang);
    
    compositeDisposable.add(apiBanHang.createOder(
        email, sdt, String.valueOf(tongSauGiam), userId, diachi, soluong, cartJson)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            response -> {
                if (response.isSuccess()) {
                    int donhangId = response.getIddonhang();
                    String maDonhang = response.getMadonhang();
                    
                    // Lưu lịch sử sử dụng voucher
                    if (voucherDaChon != null) {
                        luuVoucherUsage(donhangId, maDonhang);
                    }
                }
            }
        ));
}

private void luuVoucherUsage(int donhangId, String maDonhang) {
    compositeDisposable.add(apiBanHang.saveVoucherUsage(
        voucherDaChon.getId(),
        Utils.user_current.getId(),
        donhangId,
        maDonhang,
        tongTruocGiam,
        giaTriGiamVoucher
    ).subscribeOn(Schedulers.io())
     .observeOn(AndroidSchedulers.mainThread())
     .subscribe(
         response -> {
             Log.d("Voucher", "Đã lưu lịch sử sử dụng");
         }
     ));
}
```

---

## 5. HƯỚNG DẪN CÀI ĐẶT

### 📦 **BƯỚC 1: Chạy SQL tạo bảng**

```bash
# Mở phpMyAdmin
# Chọn database của bạn
# Import file: create_voucher_system.sql
```

Hoặc chạy thủ công:
```sql
-- Copy nội dung file create_voucher_system.sql và chạy
```

### 📤 **BƯỚC 2: Upload file PHP lên server**

```
Server/
├── checkVoucher.php          ✅ Đã tạo
├── getVouchers.php           ✅ Đã tạo
└── saveVoucherUsage.php      ✅ Đã tạo
```

### 📱 **BƯỚC 3: Thêm code vào Android**

**3.1. Model đã tạo:**
- ✅ `Voucher.java`
- ✅ `VoucherCheckResponse.java`
- ✅ `VoucherListResponse.java`

**3.2. Retrofit API đã cập nhật:**
- ✅ `ApiBanHang.java` (đã thêm 3 endpoint voucher)

**3.3. Cập nhật màn hình thanh toán:**

Xem file `VOUCHER_UI_IMPLEMENTATION.md` (sẽ tạo tiếp)

---

## 6. VÍ DỤ THỰC TẾ

### 📝 **CASE 1: Khách hàng mới mua lần đầu**

```
👤 User: Nguyễn Văn A (mới đăng ký)
📱 Hành động: Mua đồng hồ giá 1,200,000đ

🎫 Voucher: NEWUSER20
   ├─ Loại: Giảm 20%
   ├─ Giảm tối đa: 200,000đ
   ├─ Đơn tối thiểu: 500,000đ
   └─ Áp dụng: new_user

✅ KẾT QUẢ:
   ├─ Tổng trước giảm: 1,200,000đ
   ├─ Giảm giá: 200,000đ (20% = 240k nhưng max 200k)
   └─ Tổng sau giảm: 1,000,000đ
   
💾 LƯU VÀO DB:
   donhang: voucher_id=1, ma_voucher="NEWUSER20", gia_tri_giam=200000
   voucher_usage: user_id=5, gia_tri_giam=200000
   voucher: da_su_dung + 1
```

### 📝 **CASE 2: Đơn hàng không đủ điều kiện**

```
👤 User: Trần Thị B
📱 Hành động: Mua đồng hồ giá 400,000đ

🎫 Voucher: FREESHIP50K
   ├─ Loại: Freeship
   ├─ Giảm: 50,000đ
   └─ Đơn tối thiểu: 500,000đ ❌

❌ KẾT QUẢ:
   Thông báo: "Đơn hàng tối thiểu 500,000đ để áp dụng mã này"
   Thiếu: 100,000đ
```

### 📝 **CASE 3: Voucher hết lượt**

```
👤 User: Lê Văn C
📱 Hành động: Mua lần 2 với mã FIRSTORDER

🎫 Voucher: FIRSTORDER
   ├─ Giới hạn mỗi user: 1 lần
   └─ User đã dùng: 1 lần ❌

❌ KẾT QUẢ:
   Thông báo: "Bạn đã sử dụng hết lượt cho mã này"
```

---

## 7. API REFERENCE

### 🌐 **API 1: checkVoucher.php**

**Mô tả:** Kiểm tra voucher có hợp lệ và tính số tiền giảm

**Method:** `POST`

**Parameters:**
```json
{
  "ma_voucher": "NEWUSER20",
  "user_id": 5,
  "tong_tien": 1500000
}
```

**Response thành công:**
```json
{
  "success": true,
  "message": "Áp dụng mã giảm giá thành công",
  "voucher": {
    "id": 1,
    "ma_voucher": "NEWUSER20",
    "ten_voucher": "Giảm 20% cho khách hàng mới",
    "loai_giam": "percent",
    "gia_tri_giam": 20,
    "mo_ta": "Chào mừng khách hàng mới"
  },
  "tinh_toan": {
    "tong_truoc_giam": 1500000,
    "gia_tri_giam": 200000,
    "tong_sau_giam": 1300000
  }
}
```

**Response lỗi:**
```json
{
  "success": false,
  "message": "Đơn hàng tối thiểu 500,000đ để áp dụng mã này",
  "don_toi_thieu": 500000
}
```

---

### 🌐 **API 2: getVouchers.php**

**Mô tả:** Lấy danh sách voucher khả dụng cho user

**Method:** `POST`

**Parameters:**
```json
{
  "user_id": 5,
  "tong_tien": 1500000  // Optional
}
```

**Response:**
```json
{
  "success": true,
  "message": "Lấy danh sách voucher thành công",
  "is_new_user": true,
  "tong_tien": 1500000,
  "vouchers_applicable": [
    {
      "id": 1,
      "ma_voucher": "NEWUSER20",
      "ten_voucher": "Giảm 20% cho khách hàng mới",
      "text_giam": "Giảm 20% (tối đa 200,000đ)",
      "text_dieu_kien": "Đơn tối thiểu 500,000đ",
      "co_the_dung": true,
      "con_luot": 1
    }
  ],
  "vouchers_not_applicable": [
    {
      "id": 5,
      "ma_voucher": "GIAM200K",
      "co_the_dung": false,
      "thieu": 500000
    }
  ],
  "total_applicable": 1,
  "total_not_applicable": 1
}
```

---

### 🌐 **API 3: saveVoucherUsage.php**

**Mô tả:** Lưu lịch sử sử dụng voucher sau khi đặt hàng

**Method:** `POST`

**Parameters:**
```json
{
  "voucher_id": 1,
  "user_id": 5,
  "donhang_id": 123,
  "ma_donhang": "DH1730624589123",
  "gia_tri_don_hang": 1500000,
  "gia_tri_giam": 200000
}
```

**Response:**
```json
{
  "success": true,
  "message": "Lưu lịch sử sử dụng voucher thành công",
  "usage_id": 45
}
```

---

## 📊 THỐNG KÊ VOUCHER (ADMIN)

### View thống kê đã tạo sẵn:

```sql
SELECT * FROM voucher_statistics;
```

Kết quả:
```
| ma_voucher  | ti_le_su_dung | trang_thai_hien_tai | so_luong_user_da_dung | tong_tien_da_giam |
|-------------|---------------|---------------------|----------------------|-------------------|
| NEWUSER20   | 45/100        | Đang hoạt động      | 38                   | 5,230,000         |
| FIRSTORDER  | Không giới hạn| Đang hoạt động      | 127                  | 12,700,000        |
| FREESHIP50K | Không giới hạn| Đang hoạt động      | 234                  | 11,700,000        |
| FLASH30     | 30/30         | Đã hết              | 30                   | 8,500,000         |
```

---

## 🎨 TIẾP THEO

File tiếp theo sẽ hướng dẫn:
1. Tạo UI màn hình nhập voucher
2. Tạo màn hình danh sách voucher
3. Adapter hiển thị voucher
4. Tích hợp vào flow thanh toán

**Xem:** `VOUCHER_UI_IMPLEMENTATION.md`

---

## ✅ CHECKLIST TRIỂN KHAI

- [x] Tạo database tables
- [x] Tạo PHP API endpoints
- [x] Tạo Java models
- [x] Cập nhật Retrofit API
- [ ] Tạo UI nhập voucher
- [ ] Tạo màn hình danh sách voucher
- [ ] Tích hợp vào thanh toán
- [ ] Test các trường hợp
- [ ] Tạo panel admin quản lý voucher

---

**📞 Hỗ trợ:** Nếu gặp vấn đề, kiểm tra log tại `logcat` với tag `Voucher`

