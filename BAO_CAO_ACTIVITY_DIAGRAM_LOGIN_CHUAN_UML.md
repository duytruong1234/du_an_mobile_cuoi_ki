# BÁO CÁO: SƠ ĐỒ ACTIVITY - CHỨC NĂNG ĐĂNG NHẬP (CHUẨN UML 2.5)

## 📋 THÔNG TIN DỰ ÁN

- **Tên dự án:** AppBanDongHo (Ứng dụng bán đồng hồ)
- **Nền tảng:** Android (Java) + PHP Backend
- **Chức năng:** Đăng nhập (Login)
- **Chuẩn UML:** UML 2.5 Activity Diagram
- **Ngày tạo:** 29/11/2025
- **File sơ đồ:** `ACTIVITY_DIAGRAM_LOGIN_STANDARD_UML.puml`

---

## 🎯 MỤC ĐÍCH

Sơ đồ Activity này mô tả chi tiết **quy trình đăng nhập** của ứng dụng bán đồng hồ, bao gồm:

1. ✅ **Đăng nhập thường** (Email + Password)
2. ✅ **Đăng nhập Google** (Google Sign-In OAuth 2.0)
3. ✅ **Xác thực tài khoản** (Validation)
4. ✅ **Kiểm tra phân quyền** (Role: Admin/User)
5. ✅ **Kiểm tra trạng thái** (Status: Active/Locked)
6. ✅ **Xử lý lỗi** (Error Handling)

---

## 📐 CHUẨN UML ÁP DỤNG

### ✔️ Tuân thủ 100% UML 2.5 Standard

#### **1. Activity Node (Hoạt động)**
- ✅ Sử dụng hình chữ nhật bo tròn cho các hoạt động
- ✅ Mỗi hoạt động có tên rõ ràng, ngắn gọn
- ✅ Sử dụng động từ để mô tả hành động

#### **2. Decision Node (Nút quyết định)**
- ✅ Sử dụng hình th菱形 (diamond) cho câu hỏi Yes/No
- ✅ Mỗi decision có điều kiện rõ ràng
- ✅ Mỗi nhánh có nhãn điều kiện (Có/Không, Yes/No)

#### **3. Control Flow (Luồng điều khiển)**
- ✅ Sử dụng mũi tên liền nét
- ✅ Mũi tên có hướng rõ ràng
- ✅ Không có mũi tên chéo chồng lên nhau

#### **4. Swimlane (Partition)**
- ✅ Phân chia theo vai trò: Người dùng, Hệ thống (Client), Server (Backend), Google Service
- ✅ Mỗi swimlane thể hiện trách nhiệm của actor/system
- ✅ Luồng có thể chuyển qua lại giữa các swimlane

#### **5. Initial Node & Final Node**
- ✅ **Start:** Hình tròn đặc màu đen
- ✅ **Stop:** Hình tròn đặc có viền tròn bên ngoài

#### **6. Note (Ghi chú)**
- ✅ Sử dụng note để giải thích:
  - Tên file/class
  - Thư viện sử dụng
  - API endpoint
  - SQL query
  - Lý do thiết kế

#### **7. Fork & Join**
- ✅ Sử dụng thanh ngang màu đen cho parallel activities (nếu có)

#### **8. Màu sắc**
- ✅ **Màu đen trắng** (Monochrome) - Chuẩn UML truyền thống
- ✅ Nền trắng, viền đen, chữ đen
- ✅ Dễ in ấn, photocopy

---

## 🔍 PHÂN TÍCH CHI TIẾT QUY TRÌNH

### **A. QUY TRÌNH ĐĂNG NHẬP THƯỜNG**

#### **1. Khởi tạo màn hình**
```
Người dùng → Mở ứng dụng
           → Hiển thị DangNhapActivity
           → Kiểm tra PaperDB (offline storage)
           → Điền sẵn email/password nếu có
```

**File liên quan:**
- `DangNhapActivity.java` (Lines 60-95)
- `activity_dang_nhap.xml` (Layout)

**Thư viện:**
- **PaperDB** - NoSQL offline storage

#### **2. Validate input (Client-side)**
```
Email rỗng? → "Bạn chưa nhập Email" → STOP
Password rỗng? → "Bạn chưa nhập Pass" → STOP
```

**Code:**
```java
if (TextUtils.isEmpty(str_email)) {
    Toast.makeText(getApplicationContext(), "Bạn chưa nhập Email", Toast.LENGTH_SHORT).show();
} else if (TextUtils.isEmpty(str_pass)) {
    Toast.makeText(getApplicationContext(), "Bạn chưa nhập Pass", Toast.LENGTH_SHORT).show();
}
```

#### **3. Gọi API đăng nhập**
```
Client → POST dangnhap.php
       → RxJava (IO Thread)
       → Params: email, pass
```

**API Endpoint:**
- **URL:** `{BASE_URL}/dangnhap.php`
- **Method:** POST
- **Params:**
  - `email` (String)
  - `pass` (String)

**Retrofit Interface:**
```java
@FormUrlEncoded
@POST("dangnhap.php")
Observable<UserModel> dangNhap(
    @Field("email") String email,
    @Field("pass") String pass
);
```

#### **4. Xử lý Server (Backend)**

##### **4.1. Validate input (Server-side)**
```php
if (!isset($_POST['email']) || !isset($_POST['pass'])) {
    echo json_encode([
        'success' => false,
        'message' => 'Thiếu thông tin email hoặc mật khẩu'
    ]);
    exit;
}
```

##### **4.2. Escape & Hash**
```php
$email = mysqli_real_escape_string($conn, $_POST['email']);
$pass = mysqli_real_escape_string($conn, $_POST['pass']);
$pass = md5($pass); // MD5 Hash
```

**Security:**
- ✅ `mysqli_real_escape_string()` - Chống SQL Injection
- ✅ `md5()` - Hash password (lưu ý: MD5 không an toàn tuyệt đối, nên nâng cấp lên bcrypt)

##### **4.3. Truy vấn database**
```sql
SELECT * FROM user 
WHERE email = '$email' 
  AND pass = '$pass'
```

##### **4.4. Kiểm tra kết quả**
```
Không tìm thấy → "Email hoặc mật khẩu không đúng" → STOP
Tìm thấy → Kiểm tra role & status
```

##### **4.5. Kiểm tra & cập nhật role**
```php
if (!isset($row['role']) || $row['role'] === null || $row['role'] === '') {
    $row['role'] = 0;
    mysqli_query($conn, "UPDATE user SET role = 0 WHERE id = " . $row['id']);
}
```

**Role:**
- `0` = User thường
- `1` = Admin

##### **4.6. Kiểm tra & cập nhật status**
```php
if (!isset($row['status']) || $row['status'] === null) {
    $row['status'] = 1;
    mysqli_query($conn, "UPDATE user SET status = 1 WHERE id = " . $row['id']);
}
```

**Status:**
- `0` = Tài khoản bị khóa
- `1` = Tài khoản hoạt động

##### **4.7. Kiểm tra trạng thái khóa**
```php
if (isset($row['status']) && $row['status'] == 0) {
    echo json_encode([
        'success' => false,
        'message' => '🔒 Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!',
        'result' => []
    ]);
    exit;
}
```

##### **4.8. Trả về thành công**
```php
echo json_encode([
    'success' => true,
    'message' => 'Đăng nhập thành công',
    'result' => [$row]
]);
```

**Response JSON:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "result": [
    {
      "id": 1,
      "email": "user@example.com",
      "username": "John Doe",
      "mobile": "0123456789",
      "role": 0,
      "status": 1
    }
  ]
}
```

#### **5. Xử lý response (Client)**

##### **5.1. Parse JSON**
```java
.subscribe(
    userModel -> {
        if (userModel != null) {
            if (userModel.isSuccess() && 
                userModel.getResult() != null && 
                !userModel.getResult().isEmpty()) {
                // Đăng nhập thành công
            }
        }
    },
    throwable -> {
        Toast.makeText(getApplicationContext(), 
                       throwable.getMessage(), 
                       Toast.LENGTH_SHORT).show();
    }
)
```

##### **5.2. Lưu trạng thái**
```java
isLogin = true;
Paper.book().write("islogin", isLogin);
Utils.user_current = userModel.getResult().get(0);
Paper.book().write("user", Utils.user_current);
```

##### **5.3. Chuyển màn hình**
```java
Intent intent = new Intent(getApplicationContext(), MainActivity.class);
startActivity(intent);
finish(); // Đóng DangNhapActivity
```

---

### **B. QUY TRÌNH ĐĂNG NHẬP GOOGLE**

#### **1. Khởi tạo Google Sign-In**

##### **1.1. Cấu hình**
```java
GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
    GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken("123992685047-9ih70tan9l5a1d6t7dad52grqcibm7nk.apps.googleusercontent.com")
    .requestEmail()
    .requestProfile()
    .build();
    
mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
```

**Lưu ý:**
- ✅ Web Client ID từ Firebase Console
- ✅ Cần đăng ký SHA-1 fingerprint
- ✅ Package name phải khớp

##### **1.2. Sign out trước**
```java
mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
});
```

**Mục đích:** Đảm bảo hiện popup chọn tài khoản Google

#### **2. Xử lý kết quả**

##### **2.1. onActivityResult**
```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == RC_SIGN_IN) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        handleSignInResult(task);
    }
}
```

##### **2.2. Handle Sign-In Result**
```java
private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
    try {
        GoogleSignInAccount account = completedTask.getResult(ApiException.class);
        String googleEmail = account.getEmail();
        String googleName = account.getDisplayName();
        String googleId = account.getId();
        
        dangNhapGoogle(googleEmail, googleName);
        
    } catch (ApiException e) {
        // Xử lý lỗi
    }
}
```

##### **2.3. Xử lý lỗi Google Sign-In**
```java
switch (e.getStatusCode()) {
    case 10: // DEVELOPER_ERROR
        errorMessage = "Lỗi cấu hình Google Sign-In. Kiểm tra:\n" +
                "1. SHA-1 fingerprint trong Firebase Console\n" +
                "2. google-services.json đã được cập nhật\n" +
                "3. Package name khớp với Firebase project";
        break;
    case 7: // NETWORK_ERROR
        errorMessage = "Lỗi kết nối mạng. Kiểm tra internet và thử lại.";
        break;
    case 12500: // Sign in cancelled
        return; // Không hiện toast
    // ...
}
```

#### **3. Đăng ký/Đăng nhập tài khoản Google**

##### **3.1. Tạo password mặc định**
```java
String defaultPassword = "google_" + Math.abs(googleEmail.hashCode());
String defaultMobile = "0000000000"; // Đặc điểm nhận diện
```

**Quan trọng:**
- ✅ Tài khoản Google có `mobile = "0000000000"`
- ✅ Password được tạo tự động từ email

##### **3.2. Gọi API đăng ký**
```java
compositeDisposable.add(apiBanHang.dangKi(googleEmail, defaultPassword, googleName, defaultMobile)
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        userModel -> {
            // Đăng ký thành công → Đăng nhập
            dangNhap(googleEmail, defaultPassword);
        },
        throwable -> {
            // Nếu email đã tồn tại → Đăng nhập luôn
            dangNhap(googleEmail, defaultPassword);
        }
    ));
```

##### **3.3. Xử lý đặc biệt trên Server**
```php
// Kiểm tra xem có phải tài khoản Google không
$check_google_query = "SELECT * FROM user WHERE email = '$email'";
$check_data = mysqli_query($conn, $check_google_query);

if ($check_data && mysqli_num_rows($check_data) > 0) {
    $user_info = mysqli_fetch_assoc($check_data);
    
    // Nếu là tài khoản Google (mobile = '0000000000')
    if ($user_info['mobile'] === '0000000000') {
        // Auto reset password về mặc định
        $hashed_pass = md5($pass);
        $update_query = "UPDATE user SET pass = '$hashed_pass' WHERE id = $user_id";
        mysqli_query($conn, $update_query);
        
        // Lấy lại thông tin sau khi update
        // ...
    }
}
```

**Lý do reset password:**
- ✅ Password Google được tạo tự động từ email
- ✅ Đồng bộ password mỗi lần đăng nhập
- ✅ Đảm bảo luôn đăng nhập được

#### **4. Kiểm tra status (tương tự đăng nhập thường)**
```php
if (isset($row['status']) && $row['status'] == 0) {
    echo json_encode([
        'success' => false,
        'message' => '🔒 Tài khoản của bạn đã bị khóa.',
        'result' => []
    ]);
    exit;
}
```

---

## 📊 DATABASE SCHEMA

### **Table: user**

```sql
CREATE TABLE `user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `pass` VARCHAR(255) NOT NULL,
  `username` VARCHAR(255) NOT NULL,
  `mobile` VARCHAR(20) NOT NULL,
  `role` INT DEFAULT 0 COMMENT '0=User, 1=Admin',
  `status` INT DEFAULT 1 COMMENT '0=Khóa, 1=Hoạt động',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Các trường quan trọng:**
- ✅ `email` - Unique, dùng để đăng nhập
- ✅ `pass` - MD5 hash của password
- ✅ `mobile` - `"0000000000"` = Tài khoản Google
- ✅ `role` - `0` (User) hoặc `1` (Admin)
- ✅ `status` - `0` (Khóa) hoặc `1` (Hoạt động)

---

## 🔐 BẢO MẬT

### **1. Phòng chống SQL Injection**
```php
$email = mysqli_real_escape_string($conn, $_POST['email']);
$pass = mysqli_real_escape_string($conn, $_POST['pass']);
```

### **2. Hash Password**
```php
$pass = md5($pass); // MD5 hash
```

**⚠️ Khuyến nghị:**
- MD5 không còn an toàn tuyệt đối
- Nên nâng cấp lên **bcrypt** hoặc **Argon2**

### **3. Google OAuth 2.0**
- ✅ Token-based authentication
- ✅ Không lưu password Google
- ✅ Sử dụng Web Client ID

### **4. HTTPS**
- ⚠️ Cần sử dụng HTTPS cho production
- ⚠️ Tránh lộ thông tin đăng nhập qua HTTP

---

## 📦 CÔNG NGHỆ SỬ DỤNG

### **Frontend (Android)**

| Thành phần | Công nghệ | Vai trò |
|------------|-----------|---------|
| **Language** | Java | Ngôn ngữ lập trình |
| **UI** | XML Layout | Giao diện |
| **HTTP Client** | Retrofit 2 | Gọi API |
| **Async** | RxJava 2 | Xử lý bất đồng bộ |
| **Offline Storage** | PaperDB | Lưu trữ local |
| **Image Loading** | Glide 5.0.5 | Load ảnh |
| **Google Sign-In** | play-services-auth | Đăng nhập Google |
| **EventBus** | EventBus 3.2.0 | Giao tiếp giữa components |

### **Backend (PHP)**

| Thành phần | Công nghệ | Vai trò |
|------------|-----------|---------|
| **Language** | PHP 7.x+ | Ngôn ngữ backend |
| **Web Server** | Apache (XAMPP) | Server |
| **Database** | MySQL | Lưu trữ dữ liệu |
| **JSON** | json_encode() | Response format |
| **Security** | mysqli_real_escape_string | Chống SQL Injection |

---

## 🧪 TEST CASES

### **Test Case 1: Đăng nhập thành công**
```
Input:
  - Email: "test@example.com"
  - Password: "123456"
  
Expected:
  - Server trả về success = true
  - Lưu user vào PaperDB
  - Chuyển đến MainActivity
  - Hiển thị "Đăng nhập thành công"
```

### **Test Case 2: Email sai**
```
Input:
  - Email: "wrong@example.com"
  - Password: "123456"
  
Expected:
  - Server trả về success = false
  - Hiển thị "Email hoặc mật khẩu không đúng"
  - Không chuyển màn hình
```

### **Test Case 3: Password sai**
```
Input:
  - Email: "test@example.com"
  - Password: "wrongpass"
  
Expected:
  - Server trả về success = false
  - Hiển thị "Email hoặc mật khẩu không đúng"
```

### **Test Case 4: Email rỗng**
```
Input:
  - Email: ""
  - Password: "123456"
  
Expected:
  - Hiển thị "Bạn chưa nhập Email"
  - Không gọi API
```

### **Test Case 5: Password rỗng**
```
Input:
  - Email: "test@example.com"
  - Password: ""
  
Expected:
  - Hiển thị "Bạn chưa nhập Pass"
  - Không gọi API
```

### **Test Case 6: Tài khoản bị khóa**
```
Input:
  - Email: "locked@example.com"
  - Password: "123456"
  - Status trong DB = 0
  
Expected:
  - Server trả về success = false
  - Hiển thị "🔒 Tài khoản của bạn đã bị khóa"
```

### **Test Case 7: Google Sign-In thành công**
```
Input:
  - Chọn tài khoản Google: "user@gmail.com"
  
Expected:
  - Tạo tài khoản nếu chưa có
  - Đăng nhập tự động
  - Chuyển đến MainActivity
```

### **Test Case 8: Google Sign-In - DEVELOPER_ERROR**
```
Input:
  - SHA-1 chưa đăng ký
  
Expected:
  - Hiển thị "Lỗi cấu hình Google Sign-In"
  - Hướng dẫn kiểm tra
```

### **Test Case 9: Đăng nhập lại (có PaperDB)**
```
Input:
  - Đã đăng nhập trước đó
  - PaperDB có email & pass
  
Expected:
  - Điền sẵn email và password
  - User có thể đăng nhập ngay
```

---

## 🚀 CÁCH SỬ DỤNG SƠ ĐỒ

### **1. Xem sơ đồ bằng PlantUML**

#### **Online:**
- Truy cập: https://www.plantuml.com/plantuml/uml/
- Copy nội dung file `ACTIVITY_DIAGRAM_LOGIN_STANDARD_UML.puml`
- Paste và xem kết quả

#### **VS Code:**
```bash
# Cài extension
PlantUML (by jebbs)

# Xem preview
Ctrl + Shift + P → PlantUML: Preview Current Diagram
```

#### **IntelliJ IDEA / Android Studio:**
```bash
# Cài plugin
File → Settings → Plugins → Search "PlantUML"

# Xem preview
Right click file .puml → Show PlantUML Preview
```

### **2. Export sơ đồ**

#### **Export PNG:**
```bash
java -jar plantuml.jar ACTIVITY_DIAGRAM_LOGIN_STANDARD_UML.puml -tpng
```

#### **Export SVG:**
```bash
java -jar plantuml.jar ACTIVITY_DIAGRAM_LOGIN_STANDARD_UML.puml -tsvg
```

#### **Export PDF:**
```bash
java -jar plantuml.jar ACTIVITY_DIAGRAM_LOGIN_STANDARD_UML.puml -tpdf
```

---

## 📝 CHECKLIST CHUẨN UML

### ✅ **Hoàn thành 100%**

- [x] Sử dụng ký hiệu chuẩn UML 2.5
- [x] Initial node (start) - Hình tròn đặc đen
- [x] Final node (stop) - Hình tròn đặc có viền
- [x] Activity - Hình chữ nhật bo tròn
- [x] Decision node - Hình thoi
- [x] Control flow - Mũi tên liền nét
- [x] Swimlane/Partition - Phân chia theo actor
- [x] Note - Ghi chú kỹ thuật
- [x] Màu đen trắng (monochrome)
- [x] Logic rõ ràng, không mơ hồ
- [x] Có điều kiện rõ ràng ở mỗi decision
- [x] Tất cả nhánh đều có end point
- [x] Không có deadlock hoặc infinite loop
- [x] Dễ đọc, dễ hiểu
- [x] Có tài liệu giải thích chi tiết

---

## 🎓 THAM KHẢO

### **UML 2.5 Specification:**
- [OMG UML 2.5.1](https://www.omg.org/spec/UML/2.5.1/)
- Activity Diagram: Chapter 15

### **PlantUML:**
- [PlantUML Official](https://plantuml.com/)
- [Activity Diagram Syntax](https://plantuml.com/activity-diagram-beta)

### **Google Sign-In:**
- [Google Sign-In Android](https://developers.google.com/identity/sign-in/android)
- [Firebase Console](https://console.firebase.google.com/)

---

## 📞 LIÊN HỆ & HỖ TRỢ

Nếu có thắc mắc về sơ đồ hoặc cần giải thích thêm, vui lòng:

1. Đọc tài liệu này kỹ
2. Xem code trong các file:
   - `DangNhapActivity.java`
   - `dangnhap.php`
3. Kiểm tra log trong Logcat (Tag: "DangNhapActivity")

---

## ✨ KẾT LUẬN

Sơ đồ Activity này đã được thiết kế **đúng chuẩn UML 2.5**, bao gồm:

✅ **Đầy đủ các thành phần:** Activity, Decision, Swimlane, Note, Control Flow  
✅ **Logic rõ ràng:** Mô tả chi tiết từng bước đăng nhập  
✅ **Màu đen trắng:** Dễ in ấn, phù hợp báo cáo học thuật  
✅ **Tài liệu chi tiết:** Giải thích kỹ lưỡng mọi khía cạnh  
✅ **Dễ bảo trì:** Có thể cập nhật khi có thay đổi  

Sơ đồ này có thể dùng cho:
- 📚 Báo cáo học tập
- 📝 Tài liệu dự án
- 🎯 Trình bày presentation
- 🔍 Code review
- 🧪 Test planning

---

**© 2025 - AppBanDongHo Project**  
**Phiên bản:** 1.0  
**Ngày cập nhật:** 29/11/2025

