gradlew signingReport# Hướng Dẫn Sửa Lỗi Google Sign-In (Error Code 10)

## Vấn Đề
Lỗi `ApiException: 10` (DEVELOPER_ERROR) khi đăng nhập Google - nghĩa là SHA-1 fingerprint chưa được cấu hình đúng trong Firebase Console.

## Các Bước Sửa Lỗi

### Bước 1: Lấy SHA-1 Fingerprint

#### Cách 1: Dùng Gradle (Khuyến nghị)
1. Mở Terminal trong Android Studio
2. Chạy lệnh:
```bash
gradlew signingReport
```

3. Tìm và copy SHA-1 trong phần **Variant: debug** (sẽ có dạng: `SHA1: AA:BB:CC:DD:EE:...`)

#### Cách 2: Dùng Keytool (Thủ công)
1. Mở Command Prompt (cmd)
2. Chạy lệnh sau (thay `YOUR_USERNAME` bằng tên user Windows của bạn):

```bash
keytool -list -v -keystore "C:\Users\YOUR_USERNAME\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

3. Copy SHA-1 fingerprint từ kết quả

### Bước 2: Thêm SHA-1 vào Firebase Console

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Project Settings** (biểu tượng bánh răng ⚙️)
4. Kéo xuống phần **Your apps** → chọn app Android của bạn
5. Kéo xuống phần **SHA certificate fingerprints**
6. Click **Add fingerprint**
7. Dán SHA-1 fingerprint vừa copy
8. Click **Save**

### Bước 3: Tải lại google-services.json

1. Trong Firebase Console, vẫn ở trang Project Settings
2. Click nút **Download google-services.json**
3. Copy file mới vào thư mục: `D:\AppBanDienThoai\app\`
4. Thay thế file cũ

### Bước 4: Clean và Rebuild Project

1. Trong Android Studio:
   - **Build** → **Clean Project**
   - **Build** → **Rebuild Project**

2. Hoặc dùng lệnh:
```bash
gradlew clean
gradlew build
```

### Bước 5: Gỡ cài đặt App và cài lại

**Quan trọng:** Phải gỡ app cũ khỏi thiết bị/emulator

1. Gỡ app khỏi thiết bị:
   - Vào Settings → Apps → Tìm app → Uninstall
   - Hoặc giữ icon app → Uninstall

2. Cài đặt lại từ Android Studio

### Bước 6: Test lại Google Sign-In

Chạy app và thử đăng nhập bằng Google.

## Kiểm Tra Thêm

### 1. Kiểm tra Package Name
Đảm bảo package name trong Firebase Console khớp với trong app:
- Package name trong Firebase: `vn.duytruong.appbandienthoai`
- Package name trong `build.gradle.kts`: 
```kotlin
android {
    namespace = "vn.duytruong.appbandienthoai"
    defaultConfig {
        applicationId = "vn.duytruong.appbandienthoai"
    }
}
```

### 2. Kiểm tra Google Sign-In API đã được bật
1. Vào [Google Cloud Console](https://console.cloud.google.com/)
2. Chọn project (cùng tên với Firebase project)
3. Vào **APIs & Services** → **Library**
4. Tìm **Google Sign-In API** hoặc **Google+ API**
5. Click **Enable** nếu chưa bật

### 3. Kiểm tra OAuth 2.0 Client ID
1. Trong Firebase Console → Project Settings
2. Vào tab **Service accounts**
3. Đảm bảo có OAuth client được tạo cho Android

## Lưu Ý Quan Trọng

### Cho Debug Build:
- Cần SHA-1 của **debug keystore** (thường tại `C:\Users\[USER]\.android\debug.keystore`)

### Cho Release Build:
- Cần SHA-1 của **release keystore** (keystore bạn dùng để ký APK release)
- Thêm cả 2 SHA-1 (debug và release) vào Firebase

### Nếu vẫn lỗi:
1. Đợi 5-10 phút sau khi thêm SHA-1 (Firebase cần thời gian cập nhật)
2. Xóa cache Google Play Services trên thiết bị:
   - Settings → Apps → Google Play Services → Storage → Clear Cache
3. Thử trên thiết bị thật (không phải emulator)

## Các Lỗi Khác

| Code | Ý Nghĩa | Giải Pháp |
|------|---------|-----------|
| 10 | DEVELOPER_ERROR | Sửa SHA-1 (hướng dẫn trên) |
| 7 | NETWORK_ERROR | Kiểm tra kết nối Internet |
| 12500 | Sign in cancelled | Người dùng hủy đăng nhập |
| 12501 | Sign in currently in progress | Đợi quá trình đăng nhập hiện tại hoàn thành |

## Kiểm Tra Cấu Hình Hiện Tại

Để kiểm tra SHA-1 đã được thêm chưa:
1. Firebase Console → Project Settings
2. Scroll xuống phần app Android
3. Xem danh sách **SHA certificate fingerprints**

Nếu danh sách trống hoặc không có SHA-1 của bạn → Đó là nguyên nhân lỗi!

---

**Tóm tắt nhanh:**
1. Lấy SHA-1: `gradlew signingReport`
2. Thêm SHA-1 vào Firebase Console
3. Tải lại google-services.json
4. Clean + Rebuild project
5. Gỡ app cũ và cài lại
6. Test lại

Good luck! 🎉

