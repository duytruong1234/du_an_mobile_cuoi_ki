# HƯỚNG DẪN CHẠY CHỨC NĂNG ĐẶT HÀNG MỚI

## ✅ ĐÃ HOÀN THÀNH

1. ✅ Thêm 3 Activity mới vào AndroidManifest.xml
2. ✅ Sửa nút "ĐẶT HÀNG" trong ThanhToanActivity để chuyển sang DatHangActivity
3. ✅ Cập nhật DatHangActivity nhận địa chỉ và số điện thoại tự động
4. ✅ Thêm dependency Volley vào build.gradle.kts
5. ✅ Tạo file strings.xml với các placeholder cần thiết

---

## 🚀 CÁC BƯỚC TIẾP THEO (QUAN TRỌNG!)

### Bước 1: Sync Gradle
Trong Android Studio, nhấn nút **"Sync Project with Gradle Files"** (biểu tượng voi/gradle ở thanh công cụ)

HOẶC chọn: **File → Sync Project with Gradle Files**

### Bước 2: Clean Project
Chọn: **Build → Clean Project**

Đợi quá trình Clean hoàn tất (xem progress bar ở dưới cùng)

### Bước 3: Rebuild Project
Chọn: **Build → Rebuild Project**

Đợi quá trình build hoàn tất (có thể mất 1-2 phút)

### Bước 4: Chạy App
Nhấn nút **Run** (biểu tượng play màu xanh) hoặc nhấn **Shift + F10**

---

## 📱 CÁCH SỬ DỤNG CHỨC NĂNG MỚI

### Đặt hàng:
1. Thêm sản phẩm vào giỏ hàng
2. Vào giỏ hàng → Nhấn "Thanh toán"
3. Nhập địa chỉ → Nhấn "ĐẶT HÀNG" (nút xanh)
4. **Màn hình mới sẽ mở ra** với các trường:
   - Địa chỉ giao hàng (đã điền sẵn)
   - Số điện thoại (đã điền sẵn từ profile)
   - Ngày giao dự kiến (có thể chọn)
   - Tổng tiền và số lượng
5. Nhấn "ĐẶT HÀNG" để hoàn tất

### Xem đơn hàng:
- Thêm menu "Đơn hàng của tôi" vào MainActivity
- Click vào đơn hàng để xem chi tiết

---

## ⚠️ LƯU Ý

### Nếu vẫn thấy giao diện cũ:
1. **Uninstall app cũ** trên máy/emulator
2. Clean Project
3. Rebuild Project
4. Run lại

### Nếu báo lỗi Volley:
- Đảm bảo đã Sync Gradle (Bước 1)
- Kiểm tra kết nối internet (để tải dependency)
- Nếu vẫn lỗi, thử **File → Invalidate Caches / Restart**

### Cập nhật BASE_URL:
Mở file `Utils.java` và đổi địa chỉ server:
```java
public static final String BASE_URL = "http://your-ip-address/appbandienthoai/";
```

Ví dụ với máy thật (tìm IP máy tính bằng lệnh `ipconfig`):
```java
public static final String BASE_URL = "http://192.168.1.100/appbandienthoai/";
```

---

## 🆕 TÍNH NĂNG MỚI

✅ **Mã đơn hàng tự động** (DH + timestamp + random)
✅ **Chọn ngày giao hàng** (DatePicker)
✅ **Kiểm tra tồn kho** trước khi đặt
✅ **Transaction an toàn** (rollback nếu lỗi)
✅ **Hiển thị chi tiết** đầy đủ (sản phẩm, giá, số lượng)
✅ **Trạng thái đơn hàng** với màu sắc

---

## 📞 NẾU GẶP VẤN ĐỀ

1. Kiểm tra Logcat trong Android Studio
2. Tìm dòng lỗi màu đỏ
3. Copy lỗi và hỏi lại tôi

**Chúc bạn thành công! 🎉**

