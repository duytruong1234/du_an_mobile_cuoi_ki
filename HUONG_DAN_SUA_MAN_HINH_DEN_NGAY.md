# 🔥 HƯỚNG DẪN SỬA LỖI MÀN HÌNH ĐEN - THỰC HIỆN NGAY

## ⚠️ NGUYÊN NHÂN CHÍNH (90% trường hợp)

### **1️⃣ SERVER BACKEND CHƯA CHẠY** ⭐⭐⭐

App của bạn cần kết nối đến server PHP tại `http://10.0.2.2/appbandienthoai/` nhưng XAMPP chưa được khởi động!

---

## ✅ GIẢI PHÁP - THỰC HIỆN NGAY 3 BƯỚC

### **BƯỚC 1: KIỂM TRA VÀ KHỞI ĐỘNG XAMPP**

```bash
1. Mở XAMPP Control Panel
2. Bấm nút "Start" cho Apache (màu xanh = đang chạy)
3. Bấm nút "Start" cho MySQL (màu xanh = đang chạy)
```

**Cách kiểm tra nhanh:**
- Chạy file: `KIEM_TRA_SERVER.bat` (tôi đã tạo sẵn cho bạn)
- Hoặc mở trình duyệt: http://localhost/appbandienthoai/getloaisp.php
- **NẾU THẤY JSON** = Server OK ✅
- **NẾU THẤY LỖI 404** = Server chưa chạy ❌

---

### **BƯỚC 2: KIỂM TRA FILES PHP TRONG HTDOCS**

Đảm bảo folder `appbandienthoai` có trong:
```
C:\xampp\htdocs\appbandienthoai\
```

Các file quan trọng cần có:
- ✅ getloaisp.php
- ✅ chitiet.php
- ✅ dangnhap.php
- ✅ connect.php

---

### **BƯỚC 3: XEM LOGCAT ĐỂ TÌM LỖI CHÍNH XÁC**

Trong Android Studio:
1. Mở tab **Logcat** (phía dưới)
2. Filter theo: `DangNhapActivity` hoặc `MainActivity`
3. Tìm các dòng có tag **ERROR** màu đỏ

**Các lỗi phổ biến:**

| Lỗi trong Logcat | Nguyên nhân | Giải pháp |
|------------------|-------------|-----------|
| `Failed to connect to /10.0.2.2` | Server chưa chạy | Khởi động XAMPP |
| `Unable to resolve host` | Không có internet | Kiểm tra kết nối mạng |
| `ResourceNotFoundException` | Thiếu file drawable/layout | Sync Gradle lại |
| `NullPointerException` | findViewById() trả về null | Kiểm tra file XML layout |
| `DEVELOPER_ERROR` (Google Sign-In) | Thiếu SHA-1 trong Firebase | Xem hướng dẫn SHA-1 |

---

## 🔧 CÁC NGUYÊN NHÂN PHỤ KHÁC

### **2️⃣ NẾU CHẠY TRÊN THIẾT BỊ THẬT (Không phải emulator)**

Cần đổi BASE_URL trong file `Utils.java`:

```java
// File: app/src/main/java/vn/duytruong/appbandienthoai/utils/Utils.java

// ❌ KHÔNG DÙNG cho thiết bị thật:
public static final String BASE_URL = "http://10.0.2.2/appbandienthoai/";

// ✅ DÙNG cho thiết bị thật (thay IP máy tính của bạn):
public static final String BASE_URL = "http://192.168.1.XXX/appbandienthoai/";
```

**Cách lấy IP máy tính:**
1. Mở CMD
2. Gõ: `ipconfig`
3. Tìm dòng **IPv4 Address**: VD: 192.168.1.5
4. Thay XXX = số IP của bạn

---

### **3️⃣ LỖI GOOGLE SERVICES / FIREBASE**

Nếu thấy lỗi:
```
google-services.json not found
SHA-1 fingerprint mismatch
```

**Giải pháp:**
- Xem file: `HUONG_DAN_SUA_LOI_GOOGLE_SIGNIN_SHA1.md`
- Hoặc tạm thời comment code Firebase trong MainActivity.java

---

### **4️⃣ LỖI LAYOUT XML**

Nếu thấy lỗi:
```
Error inflating class
ResourceNotFoundException
```

**Giải pháp:**
```bash
1. Build > Clean Project
2. Build > Rebuild Project
3. File > Invalidate Caches > Invalidate and Restart
```

---

## 🚀 CÁCH CHẠY LẠI APP SAU KHI SỬA

### **Option 1: Chạy từ Android Studio**
```bash
1. Đảm bảo XAMPP đã Start Apache + MySQL
2. Trong Android Studio: Run > Run 'app' (Shift+F10)
3. Chọn emulator hoặc thiết bị
4. Đợi app cài đặt và khởi động
```

### **Option 2: Kiểm tra chi tiết hơn**
```bash
1. Chạy file: KIEM_TRA_SERVER.bat
2. Xác nhận server đang chạy (trình duyệt hiện JSON)
3. Mở Logcat trong Android Studio
4. Run app và xem log real-time
5. Nếu có lỗi, copy dòng lỗi và tìm trong bảng trên
```

---

## 📋 CHECKLIST - ĐÁNH DẤU KHI HOÀN THÀNH

- [ ] ✅ XAMPP đã Start Apache (màu xanh)
- [ ] ✅ XAMPP đã Start MySQL (màu xanh)
- [ ] ✅ Test URL: http://localhost/appbandienthoai/getloaisp.php → Thấy JSON
- [ ] ✅ Files PHP có trong C:\xampp\htdocs\appbandienthoai\
- [ ] ✅ Clean + Rebuild project trong Android Studio
- [ ] ✅ Xem Logcat không có lỗi màu đỏ
- [ ] ✅ Nếu dùng thiết bị thật: Đã đổi BASE_URL sang IP máy tính

---

## 💡 MẸO DEBUG NHANH

### **Test kết nối server từ emulator:**
```bash
# Trong emulator, mở Chrome/Browser
# Truy cập: http://10.0.2.2/appbandienthoai/getloaisp.php
# NẾU THẤY JSON = Server OK
# NẾU LỖI 404 = Server chưa chạy hoặc đường dẫn sai
```

### **Xem log chi tiết:**
```bash
# Tôi đã thêm log debug vào DangNhapActivity:
# - "onCreate started"
# - "setContentView success"
# - "initView success"
# - "initControl success"

# Xem Logcat để biết bước nào bị lỗi
```

---

## 🆘 NẾU VẪN KHÔNG ĐƯỢC

Hãy:
1. **Chụp màn hình Logcat** (phần có lỗi màu đỏ)
2. **Chụp màn hình XAMPP Control Panel** (để xem Apache/MySQL có chạy không)
3. **Test URL** trong trình duyệt: http://localhost/appbandienthoai/getloaisp.php
4. Gửi cho tôi để debug tiếp

---

## 📝 GHI CHÚ QUAN TRỌNG

✅ **ĐÃ SỬA:**
- DangNhapActivity.java: Thêm try-catch và null checks
- MainActivity.java: Thêm error logging chi tiết
- Tạo file KIEM_TRA_SERVER.bat để test nhanh

⚠️ **LƯU Ý:**
- App LUÔN LUÔN cần server PHP chạy mới hoạt động được
- Emulator dùng IP: 10.0.2.2
- Thiết bị thật dùng IP: 192.168.x.x (IP máy tính)

---

**Chúc bạn sửa lỗi thành công! 🎉**

