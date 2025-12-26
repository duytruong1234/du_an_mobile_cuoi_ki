# HƯỚNG DẪN SỬA LỖI GOOGLE SIGN-IN - NÚT GOOGLE KHÔNG HOẠT ĐỘNG

## Vấn đề
Khi bấm nút "Đăng nhập với Google", nút chỉ nháy mà không hiện giao diện chọn Gmail.

## Nguyên nhân
SHA-1 fingerprint trong Firebase Console không khớp với SHA-1 của máy tính hiện tại.

## Cách sửa - THỰC HIỆN NGAY

### Bước 1: Lấy SHA-1 Fingerprint hiện tại

**Cách 1 - Dùng Terminal (KHUYÊN DÙNG):**
```cmd
cd D:\AppBanDienThoai
gradlew signingReport
```

Tìm dòng có **SHA-1** trong kết quả (phần `Variant: debug`), ví dụ:
```
SHA-1: A1:B2:C3:D4:E5:F6:G7:H8:I9:J0:K1:L2:M3:N4:O5:P6:Q7:R8:S9:T0
```

**Cách 2 - Dùng keytool:**
```cmd
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Copy dòng SHA-1 (40 ký tự, định dạng XX:XX:XX:...)

### Bước 2: Cập nhật SHA-1 vào Firebase Console

1. Truy cập: https://console.firebase.google.com/
2. Chọn project: **appbandongho**
3. Vào **Project Settings** (biểu tượng bánh răng)
4. Tab **General** → cuộn xuống phần **Your apps**
5. Tìm app **vn.duytruong.appbandienthoai**
6. Click vào **SHA certificate fingerprints**
7. Click **Add fingerprint**
8. Dán SHA-1 vừa copy vào
9. Click **Save**

### Bước 3: Tải file google-services.json mới

1. Vẫn ở trang Firebase Console
2. Click nút **Download google-services.json**
3. Copy file vừa tải về
4. Paste vào: `D:\AppBanDienThoai\app\`
5. Ghi đè file cũ

### Bước 4: Clean và Rebuild project

**Trong Android Studio:**
- Build → Clean Project
- Build → Rebuild Project

**Hoặc dùng Terminal:**
```cmd
cd D:\AppBanDienThoai
gradlew clean
gradlew build
```

### Bước 5: Chạy lại app

Run app và thử bấm nút "Đăng nhập với Google" lại.

---

## Lưu ý quan trọng

- **SHA-1 thay đổi** khi bạn chuyển máy hoặc cài lại Android Studio
- Nếu build **Release APK**, cần thêm SHA-1 của **release keystore**
- Có thể có **nhiều SHA-1** trong 1 project (debug, release, các máy khác nhau)

## Kiểm tra xem đã đúng chưa

Sau khi làm xong, chạy app và xem log:
- **Nếu thành công:** Sẽ hiện popup chọn tài khoản Google
- **Nếu vẫn lỗi:** Xem log có dòng `DEVELOPER_ERROR` → SHA-1 vẫn chưa đúng

---

## SHA-1 Fingerprint hiện có trong Firebase (CŨ):
```
487764f73957f2f81c951aa55c024c10bfbf512e
```

**→ CẦN THÊM SHA-1 MỚI của máy hiện tại!**

