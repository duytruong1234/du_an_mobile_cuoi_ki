# HƯỚNG DẪN SỬA LỖI MÀN HÌNH ĐEN KHI KHỞI CHẠY ỨNG DỤNG

## CÁC NGUYÊN NHÂN CHÍNH GÂY MÀN HÌNH ĐEN

### 1. **LỖI KẾT NỐI SERVER**
- **Triệu chứng**: App khởi động màn hình đen, không có thông báo lỗi
- **Nguyên nhân**: BASE_URL đang trỏ tới `http://10.0.2.2/appbandienthoai/` (localhost emulator) nhưng server không chạy
- **Giải pháp**:
  ```java
  // File: Utils.java
  // Nếu chạy trên emulator:
  public static final String BASE_URL = "http://10.0.2.2/appbandienthoai/";
  
  // Nếu chạy trên thiết bị thật, thay bằng IP thật:
  public static final String BASE_URL = "http://192.168.1.XXX/appbandienthoai/";
  
  // Hoặc dùng server online:
  public static final String BASE_URL = "https://yourdomain.com/appbandienthoai/";
  ```

### 2. **LỖI GOOGLE SERVICES (Firebase/Google Sign-In)**
- **Triệu chứng**: App crash ngay khi khởi động, màn hình đen
- **Nguyên nhân**: File `google-services.json` bị thiếu hoặc không đúng cấu hình
- **Giải pháp**:
  1. Kiểm tra file `app/google-services.json` có tồn tại không
  2. Đảm bảo SHA-1 fingerprint đã được cấu hình đúng trong Firebase Console
  3. Nếu không dùng Firebase, tạm thời comment code Firebase trong MainActivity:
     ```java
     // Comment dòng này nếu chưa cấu hình Firebase
     // FirebaseMessaging.getInstance().getToken()...
     ```

### 3. **LỖI LAYOUT XML**
- **Triệu chứng**: App crash khi load layout, màn hình đen
- **Nguyên nhân**: Thiếu resource (drawable, string) được tham chiếu trong XML
- **Kiểm tra**:
  - Build app và xem logcat để tìm lỗi ResourceNotFoundException
  - Đảm bảo tất cả @drawable/xxx và @string/xxx đều tồn tại

### 4. **LỖI NULLPOINTEREXCEPTION**
- **Triệu chứng**: App crash ngay sau khi khởi động
- **Nguyên nhân**: findViewById() trả về null
- **Giải pháp**: Đã thêm try-catch và null checks trong code

## CÁC BƯỚC KIỂM TRA VÀ SỬA LỖI

### Bước 1: Kiểm tra Logcat
```bash
# Mở Android Studio -> Logcat
# Filter: MainActivity hoặc DangNhapActivity
# Tìm các dòng có tag ERROR, Exception, Crash
```

### Bước 2: Kiểm tra Server đã chạy chưa
```bash
# Nếu dùng XAMPP:
# 1. Mở XAMPP Control Panel
# 2. Start Apache và MySQL
# 3. Truy cập http://localhost/appbandienthoai/ để kiểm tra
```

### Bước 3: Test kết nối API
```bash
# Truy cập các URL sau trong browser:
http://10.0.2.2/appbandienthoai/getloaisp.php
http://10.0.2.2/appbandienthoai/chitiet.php

# Phải trả về JSON, không được lỗi 404
```

### Bước 4: Kiểm tra Dependencies
```kotlin
// File: build.gradle.kts (app level)
// Đảm bảo các dependencies sau đã có:
dependencies {
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.android.gms:play-services-auth")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
}
```

### Bước 5: Clean và Rebuild
```bash
# Trong Android Studio:
Build -> Clean Project
Build -> Rebuild Project
```

## CODE ĐÃ ĐƯỢC SỬA

### MainActivity.java - ✅ ĐÃ SỬA XONG
- ✅ **SỬA LỖI CÚ PHÁP NGHIÊM TRỌNG**: Tách riêng phương thức `Anhxa()` và `ActionBar()` bị lẫn lộn
- ✅ Thêm try-catch trong onCreate() để bắt lỗi
- ✅ Thêm error handling trong Anhxa() để tránh NullPointerException
- ✅ Log chi tiết để debug khi có lỗi

### DangNhapActivity.java - ✅ ĐÃ SỬA XONG
- ✅ Thêm try-catch trong onCreate()
- ✅ Thêm null checks trong initView() để tránh crash
- ✅ Không finish() khi gặp lỗi để user thấy thông báo lỗi

### Kết quả:
- **Không còn lỗi ERROR** - App có thể build và chạy được
- Chỉ còn một số WARNING không ảnh hưởng

## CÁCH XEM LOG ĐỂ TÌM LỖI

### Trong Android Studio:
1. Mở Logcat (View -> Tool Windows -> Logcat)
2. Chọn device/emulator đang chạy
3. Filter theo tag:
   - `MainActivity` - Xem lỗi ở màn hình chính
   - `DangNhapActivity` - Xem lỗi ở màn đăng nhập
   - `AndroidRuntime` - Xem crash logs
4. Tìm các dòng có:
   - **ERROR** - Lỗi nghiêm trọng
   - **Exception** - Lỗi runtime
   - **FATAL** - App crash

### Ví dụ log lỗi thường gặp:

```
// Lỗi kết nối server:
E/MainActivity: Error loading products
java.net.ConnectException: Failed to connect to /10.0.2.2:80

// Lỗi thiếu resource:
E/AndroidRuntime: android.content.res.Resources$NotFoundException: 
Resource ID #0x7f0800xx

// Lỗi NullPointerException:
E/MainActivity: Anhxa error: Attempt to invoke virtual method on null object
```

## GIẢI PHÁP TẠM THỜI

Nếu vẫn gặp màn hình đen, hãy thử:

### 1. Bỏ qua Firebase tạm thời
```java
// Comment code Firebase trong MainActivity onCreate():
/*
FirebaseMessaging.getInstance().getToken()
    .addOnCompleteListener(task -> {
        // ...
    });
*/
```

### 2. Cho phép app chạy offline
```java
// Trong MainActivity onCreate(), comment dòng kiểm tra internet:
// if (isConnected(this)){
    ActionViewFlipper();
    getLoaiSanPham();
    getSpMoi();
    getEventClick();
// }
```

### 3. Test với layout đơn giản
Tạo layout test đơn giản để kiểm tra app có chạy được không:
```xml
<!-- activity_main_test.xml -->
<LinearLayout>
    <TextView
        android:text="App đã chạy được!"
        android:textSize="24sp"/>
</LinearLayout>
```

## LƯU Ý QUAN TRỌNG

1. **Luôn kiểm tra Logcat** - Đây là công cụ quan trọng nhất để debug
2. **Server phải chạy** - Nếu dùng localhost, phải start XAMPP trước
3. **Internet phải có** - App cần internet để load dữ liệu
4. **Google Services** - Đảm bảo google-services.json đúng cấu hình

## LIÊN HỆ HỖ TRỢ

Nếu vẫn gặp lỗi, hãy:
1. Chụp màn hình Logcat khi app crash
2. Gửi thông tin:
   - Phiên bản Android đang test
   - Dùng emulator hay thiết bị thật
   - Nội dung lỗi trong Logcat
# HƯỚNG DẪN SỬA LỖI MÀN HÌNH ĐEN KHI KHỞI CHẠY ỨNG DỤNG

## ✅ LỖI ĐÃ ĐƯỢC SỬA

**LỖI CHÍNH: Lỗi cú pháp trong MainActivity.java**
- Code của phương thức `Anhxa()` và `ActionBar()` bị lẫn lộn với nhau
- Gây ra lỗi compile khiến app không thể khởi động
- **ĐÃ SỬA XONG** - Code đã được tách riêng đúng cách

## CÁC NGUYÊN NHÂN CHÍNH GÂY MÀN HÌNH ĐEN

### 1. **LỖI KẾT NỐI SERVER**
- **Triệu chứng**: App khởi động màn hình đen, không có thông báo lỗi
- **Nguyên nhân**: BASE_URL đang trỏ tới `http://10.0.2.2/appbandienthoai/` (localhost emulator) nhưng server không chạy
- **Giải pháp**:
  ```java
  // File: Utils.java
  // Nếu chạy trên emulator:
  public static final String BASE_URL = "http://10.0.2.2/appbandienthoai/";
  
  // Nếu chạy trên thiết bị thật, thay bằng IP thật:
  public static final String BASE_URL = "http://192.168.1.XXX/appbandienthoai/";
  
  // Hoặc dùng server online:
  public static final String BASE_URL = "https://yourdomain.com/appbandienthoai/";
  ```

### 2. **LỖI GOOGLE SERVICES (Firebase/Google Sign-In)**
- **Triệu chứng**: App crash ngay khi khởi động, màn hình đen
- **Nguyên nhân**: File `google-services.json` bị thiếu hoặc không đúng cấu hình
- **Giải pháp**:
  1. Kiểm tra file `app/google-services.json` có tồn tại không
  2. Đảm bảo SHA-1 fingerprint đã được cấu hình đúng trong Firebase Console
  3. Nếu không dùng Firebase, tạm thời comment code Firebase trong MainActivity:
     ```java
     // Comment dòng này nếu chưa cấu hình Firebase
     // FirebaseMessaging.getInstance().getToken()...
     ```

### 3. **LỖI LAYOUT XML**
- **Triệu chứng**: App crash khi load layout, màn hình đen
- **Nguyên nhân**: Thiếu resource (drawable, string) được tham chiếu trong XML
- **Kiểm tra**:
  - Build app và xem logcat để tìm lỗi ResourceNotFoundException
  - Đảm bảo tất cả @drawable/xxx và @string/xxx đều tồn tại

### 4. **LỖI NULLPOINTEREXCEPTION**
- **Triệu chứng**: App crash ngay sau khi khởi động
- **Nguyên nhân**: findViewById() trả về null
- **Giải pháp**: Đã thêm try-catch và null checks trong code

## CÁC BƯỚC KIỂM TRA VÀ SỬA LỖI

### Bước 1: Kiểm tra Logcat
```bash
# Mở Android Studio -> Logcat
# Filter: MainActivity hoặc DangNhapActivity
# Tìm các dòng có tag ERROR, Exception, Crash
```

### Bước 2: Kiểm tra Server đã chạy chưa
```bash
# Nếu dùng XAMPP:
# 1. Mở XAMPP Control Panel
# 2. Start Apache và MySQL
# 3. Truy cập http://localhost/appbandienthoai/ để kiểm tra
```

### Bước 3: Test kết nối API
```bash
# Truy cập các URL sau trong browser:
http://10.0.2.2/appbandienthoai/getloaisp.php
http://10.0.2.2/appbandienthoai/chitiet.php

# Phải trả về JSON, không được lỗi 404
```

### Bước 4: Kiểm tra Dependencies
```kotlin
// File: build.gradle.kts (app level)
// Đảm bảo các dependencies sau đã có:
dependencies {
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.android.gms:play-services-auth")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
}
```

### Bước 5: Clean và Rebuild
```bash
# Trong Android Studio:
Build -> Clean Project
Build -> Rebuild Project
```

## CODE ĐÃ ĐƯỢC SỬA

### MainActivity.java
- ✅ Thêm try-catch trong onCreate()
- ✅ Thêm error handling trong Anhxa()
- ✅ Log chi tiết để debug

### DangNhapActivity.java
- ✅ Thêm try-catch trong onCreate()
- ✅ Thêm null checks trong initView()
- ✅ Không finish() khi gặp lỗi để user thấy thông báo lỗi

## CÁCH XEM LOG ĐỂ TÌM LỖI

### Trong Android Studio:
1. Mở Logcat (View -> Tool Windows -> Logcat)
2. Chọn device/emulator đang chạy
3. Filter theo tag:
   - `MainActivity` - Xem lỗi ở màn hình chính
   - `DangNhapActivity` - Xem lỗi ở màn đăng nhập
   - `AndroidRuntime` - Xem crash logs
4. Tìm các dòng có:
   - **ERROR** - Lỗi nghiêm trọng
   - **Exception** - Lỗi runtime
   - **FATAL** - App crash

### Ví dụ log lỗi thường gặp:

```
// Lỗi kết nối server:
E/MainActivity: Error loading products
java.net.ConnectException: Failed to connect to /10.0.2.2:80

// Lỗi thiếu resource:
E/AndroidRuntime: android.content.res.Resources$NotFoundException: 
Resource ID #0x7f0800xx

// Lỗi NullPointerException:
E/MainActivity: Anhxa error: Attempt to invoke virtual method on null object
```

## GIẢI PHÁP TẠM THỜI

Nếu vẫn gặp màn hình đen, hãy thử:

### 1. Bỏ qua Firebase tạm thời
```java
// Comment code Firebase trong MainActivity onCreate():
/*
FirebaseMessaging.getInstance().getToken()
    .addOnCompleteListener(task -> {
        // ...
    });
*/
```

### 2. Cho phép app chạy offline
```java
// Trong MainActivity onCreate(), comment dòng kiểm tra internet:
// if (isConnected(this)){
    ActionViewFlipper();
    getLoaiSanPham();
    getSpMoi();
    getEventClick();
// }
```

### 3. Test với layout đơn giản
Tạo layout test đơn giản để kiểm tra app có chạy được không:
```xml
<!-- activity_main_test.xml -->
<LinearLayout>
    <TextView
        android:text="App đã chạy được!"
        android:textSize="24sp"/>
</LinearLayout>
```

## LƯU Ý QUAN TRỌNG

1. **Luôn kiểm tra Logcat** - Đây là công cụ quan trọng nhất để debug
2. **Server phải chạy** - Nếu dùng localhost, phải start XAMPP trước
3. **Internet phải có** - App cần internet để load dữ liệu
4. **Google Services** - Đảm bảo google-services.json đúng cấu hình

## LIÊN HỆ HỖ TRỢ

Nếu vẫn gặp lỗi, hãy:
1. Chụp màn hình Logcat khi app crash
2. Gửi thông tin:
   - Phiên bản Android đang test
   - Dùng emulator hay thiết bị thật
   - Nội dung lỗi trong Logcat

