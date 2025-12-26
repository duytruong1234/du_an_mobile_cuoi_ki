# Hướng Dẫn Sửa Lỗi Google Sign-In (ApiException: 10)

## Nguyên Nhân
Lỗi `ApiException: 10` (DEVELOPER_ERROR) xảy ra vì:
- File `google-services.json` thiếu OAuth 2.0 Client IDs
- Chưa cấu hình SHA-1 fingerprint trong Firebase Console
- Package name hoặc signing certificate không khớp

## Cách Sửa

### Bước 1: Lấy SHA-1 Fingerprint

Mở Command Prompt (cmd) và chạy lệnh sau:

```cmd
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Lưu ý SHA-1** từ kết quả (dạng: `AA:BB:CC:DD:...`)

### Bước 2: Cấu Hình Firebase Console

1. Truy cập: https://console.firebase.google.com/
2. Chọn project **appbandongho** (Project ID: 123992685047)
3. Vào **Project Settings** (biểu tượng bánh răng)
4. Chọn tab **General**
5. Tìm app **vn.duytruong.appbandienthoai**
6. Click **Add fingerprint** và dán SHA-1 vừa lấy được
7. Click **Save**

### Bước 3: Thêm OAuth 2.0 Client

1. Trong Firebase Console, vào tab **Authentication**
2. Click **Sign-in method**
3. Enable **Google** sign-in provider
4. Hoặc truy cập Google Cloud Console:
   - https://console.cloud.google.com/apis/credentials?project=appbandongho
5. Click **Create Credentials** > **OAuth 2.0 Client ID**
6. Chọn **Android**
7. Nhập:
   - **Name**: AppBanDienThoai Android Client
   - **Package name**: `vn.duytruong.appbandienthoai`
   - **SHA-1**: (dán SHA-1 từ Bước 1)
8. Click **Create**

### Bước 4: Tải Google Services JSON Mới

1. Quay lại Firebase Console > Project Settings
2. Tải xuống `google-services.json` mới
3. Thay thế file cũ tại: `D:\AppBanDienThoai\app\google-services.json`

File mới sẽ có `oauth_client` array không rỗng:
```json
"oauth_client": [
  {
    "client_id": "xxxxx.apps.googleusercontent.com",
    "client_type": 1,
    "android_info": {
      "package_name": "vn.duytruong.appbandienthoai",
      "certificate_hash": "..."
    }
  },
  {
    "client_id": "xxxxx.apps.googleusercontent.com",
    "client_type": 3
  }
]
```

### Bước 5: Cập Nhật strings.xml (Nếu Cần)

File `strings.xml` của bạn đã có `default_web_client_id`, nhưng cần kiểm tra xem có khớp với Web Client ID trong `google-services.json` mới không.

Tìm client_type = 3 trong `oauth_client` array và copy `client_id` đó vào `strings.xml`:

```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE.apps.googleusercontent.com</string>
```

### Bước 6: Rebuild và Test

1. Trong Android Studio: **Build** > **Clean Project**
2. **Build** > **Rebuild Project**
3. Chạy app và thử đăng nhập Google lại

## Kiểm Tra Lỗi

Nếu vẫn gặp lỗi, kiểm tra logcat:
- `ApiException: 10` = DEVELOPER_ERROR (SHA-1/package name sai)
- `ApiException: 7` = NETWORK_ERROR (không có kết nối)
- `ApiException: 12500` = Sign In cancelled

## Ghi Chú Quan Trọng

⚠️ **Đối với Release Build**: Bạn cần lấy SHA-1 của release keystore và thêm vào Firebase:
```cmd
keytool -list -v -keystore "D:\path\to\your\release.keystore" -alias your_alias
```

⚠️ **Multiple Build Variants**: Nếu có debug và release, cần thêm CẢ 2 SHA-1 fingerprints vào Firebase.

