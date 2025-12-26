# Báo Cáo: Activity Diagram Đăng Nhập - Chuẩn UML

## 📋 Tổng Quan

**File diagram:** `ACTIVITY_02_DANG_NHAP_SWIMLANES_CHUAN_UML.drawio`

Đây là Activity Diagram chuẩn UML 2.5 cho tính năng **Đăng nhập (Login)** của ứng dụng thương mại điện tử bán đồng hồ.

---

## 🎯 Yêu Cầu Đã Thực Hiện

### ✅ 1. Vẽ Chuẩn UML

Diagram tuân thủ đầy đủ các thành phần chuẩn UML 2.5:

| Thành Phần | Ký Hiệu | Mô Tả | Đã Có |
|------------|---------|-------|-------|
| **Start Node** | ⚫ (Filled Circle) | Điểm bắt đầu của luồng | ✅ |
| **End Node** | ◎ (Bull's Eye) | Điểm kết thúc của luồng | ✅ |
| **Activity Node** | ▭ (Rounded Rectangle) | Các hoạt động/hành động | ✅ |
| **Decision Node** | ◇ (Diamond) | Điểm ra quyết định/điều kiện | ✅ |
| **Merge Node** | ◆ (Filled Diamond) | Hợp nhất nhiều luồng | ✅ |
| **Control Flow** | → (Solid Arrow) | Luồng điều khiển | ✅ |
| **Object Flow** | ⇢ (Dashed Arrow) | Luồng truyền dữ liệu | ✅ |

### ✅ 2. Swimlane Theo Chiều Dọc

Diagram được chia thành **4 swimlanes** theo chiều dọc:

1. **User (Người dùng)** - Màu cam (#FFF3E0)
2. **UI/Client (Giao diện)** - Màu tím (#F3E5F5)
3. **Backend/API (Server)** - Màu xanh dương (#E8EAF6)
4. **Database (MySQL)** - Màu xanh lá (#E0F2F1)

### ✅ 3. Các Bước Đầy Đủ

#### **A. Luồng Chính (Happy Path)**

```
1. User mở màn hình đăng nhập
   → UI hiển thị form đăng nhập

2. User nhập email và mật khẩu
   → UI thu thập dữ liệu từ form

3. User nhấn nút "Đăng nhập"
   → UI thu thập dữ liệu

4. UI validate dữ liệu
   → Decision: Dữ liệu hợp lệ?
   
5. [CÓ] UI gửi request POST /dangnhap.php
   → Backend nhận request

6. Backend parse request body
   → Backend hash password (MD5/bcrypt)
   → Backend truy vấn database

7. Database nhận query SELECT
   → Database thực thi query
   → Database tìm kiếm user
   → Decision: User tồn tại?

8. [CÓ] Database trả về dữ liệu user
   → Decision Backend: Tìm thấy user?
   
9. [CÓ] Backend tạo JWT token
   → Backend trả về response success

10. UI nhận response
    → UI lưu token vào Local Storage
    → User xem thông báo thành công
    → User chuyển đến MainActivity
    → END
```

#### **B. Luồng Lỗi Validation (Error Path 1)**

```
4. UI validate dữ liệu
   → Decision: Dữ liệu hợp lệ?
   
5. [KHÔNG] UI hiển thị lỗi validation
   → User xem thông báo lỗi nhập liệu
   → Merge Node
   → LOOP back: User nhập lại email và mật khẩu
```

**Các lỗi validation:**
- Email không đúng định dạng
- Password để trống
- Email để trống

#### **C. Luồng Lỗi Đăng Nhập (Error Path 2)**

```
7. Database tìm kiếm user
   → Decision: User tồn tại?
   
8. [KHÔNG] Database trả về NULL
   → Decision Backend: Tìm thấy user?
   
9. [KHÔNG] Backend trả về response error
   → UI nhận response
   → User xem thông báo "Email hoặc mật khẩu không đúng"
   → LOOP back: User nhập lại email và mật khẩu
```

---

## 🏗️ Kiến Trúc và Công Nghệ

### **1. Frontend (UI/Client)**
- **Android (Kotlin/Java)** hoặc **iOS (Swift)**
- Validation:
  - Email format regex
  - Password not empty
  - Min/max length
- Storage:
  - SharedPreferences (Android)
  - UserDefaults (iOS)
  - LocalStorage (Web)

### **2. Backend (API)**
- **PHP** (dangnhap.php)
- Framework: Native PHP hoặc Laravel/Slim
- Security:
  - Password hashing: MD5, SHA256, hoặc bcrypt (khuyến nghị)
  - Token generation: JWT (JSON Web Token)
  - HTTPS/SSL
- Request: `POST /dangnhap.php`
- Response: JSON

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

**Response Success:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "Nguyễn Văn A",
    "role": "customer"
  }
}
```

**Response Error:**
```json
{
  "success": false,
  "message": "Email hoặc mật khẩu không đúng"
}
```

### **3. Database (MySQL)**

**Bảng `users`:**
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(20),
    role ENUM('admin', 'customer') DEFAULT 'customer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Query đăng nhập:**
```sql
SELECT id, email, name, phone, role 
FROM users 
WHERE email = ? AND password = ?
LIMIT 1;
```

---

## 🎨 Màu Sắc và Ý Nghĩa

| Màu | Hex Code | Ý Nghĩa | Áp Dụng |
|-----|----------|---------|---------|
| **Đen** | #000000 | Start/End Node, Merge Node | Điểm bắt đầu/kết thúc |
| **Vàng** | #FFF9C4 | Decision Node | Điều kiện/quyết định |
| **Xanh lá** | #E8F5E9 / #2E7D32 | Success Path | Luồng thành công |
| **Đỏ** | #FFEBEE / #C62828 | Error Path | Luồng lỗi |
| **Xám** | #666666 | Object Flow | Luồng dữ liệu |

---

## 📊 Thống Kê Diagram

- **Số Swimlanes:** 4
- **Số Activity Nodes:** 22
- **Số Decision Nodes:** 3
- **Số Merge Nodes:** 2
- **Số Control Flows:** 36
- **Số Object Flows:** 8
- **Số End Nodes:** 1
- **Số Start Nodes:** 1

---

## 🔒 Bảo Mật

### **1. Client-side Security**
- ✅ Input validation
- ✅ Email format check
- ✅ Password strength check (optional)
- ✅ Sanitize input data
- ✅ HTTPS only

### **2. Server-side Security**
- ✅ Password hashing (MD5 → ❌ không an toàn, nên dùng bcrypt)
- ✅ SQL injection prevention (Prepared Statements)
- ✅ Rate limiting (chống brute-force)
- ✅ Token expiration
- ✅ CORS configuration

### **3. Database Security**
- ✅ Encrypted password storage
- ✅ Unique constraint trên email
- ✅ Index trên email column
- ✅ Connection pooling
- ✅ Regular backups

---

## 🚀 Cải Tiến Đề Xuất

### **1. Tăng Cường Bảo Mật**
```php
// Đổi từ MD5 sang bcrypt
// Cũ: md5($password)
// Mới: password_hash($password, PASSWORD_BCRYPT)

// Verification
if (password_verify($input_password, $hashed_password)) {
    // Login success
}
```

### **2. Thêm Remember Me**
- Lưu refresh token
- Auto-login khi mở app
- Token expiration: 7-30 days

### **3. Thêm Social Login**
- Google Sign-In
- Facebook Login
- Apple Sign-In

### **4. Multi-Factor Authentication (MFA)**
- OTP qua SMS
- Email verification code
- Authenticator app (Google Authenticator)

### **5. Rate Limiting**
```php
// Giới hạn 5 lần đăng nhập sai trong 15 phút
if ($failed_attempts >= 5) {
    return [
        'success' => false,
        'message' => 'Tài khoản tạm khóa 15 phút do đăng nhập sai nhiều lần'
    ];
}
```

### **6. Logging & Monitoring**
- Log mọi lần đăng nhập thành công/thất bại
- IP address tracking
- Device fingerprinting
- Alert khi có hoạt động bất thường

---

## 📱 User Experience (UX)

### **1. Loading State**
- Hiển thị progress bar khi gửi request
- Disable nút "Đăng nhập" khi đang xử lý

### **2. Error Messages**
```
❌ "Email không đúng định dạng"
❌ "Vui lòng nhập mật khẩu"
❌ "Email hoặc mật khẩu không đúng"
✅ "Đăng nhập thành công! Đang chuyển hướng..."
```

### **3. Auto-fill**
- Hỗ trợ Android Autofill
- iOS Password Manager
- Browser autofill

### **4. Show/Hide Password**
- Icon con mắt để hiển thị/ẩn mật khẩu
- Toggle password visibility

---

## 🧪 Test Cases

### **Test Case 1: Đăng nhập thành công**
```
Input:
  - Email: admin@shop.com
  - Password: Admin@123

Expected Output:
  - Hiển thị "Đăng nhập thành công"
  - Lưu token vào storage
  - Chuyển đến MainActivity
```

### **Test Case 2: Email sai định dạng**
```
Input:
  - Email: invalid-email
  - Password: 123456

Expected Output:
  - Hiển thị "Email không đúng định dạng"
  - Không gọi API
```

### **Test Case 3: Password để trống**
```
Input:
  - Email: user@shop.com
  - Password: (empty)

Expected Output:
  - Hiển thị "Vui lòng nhập mật khẩu"
  - Không gọi API
```

### **Test Case 4: Sai email hoặc password**
```
Input:
  - Email: user@shop.com
  - Password: wrongpassword

Expected Output:
  - Hiển thị "Email hoặc mật khẩu không đúng"
  - Không lưu token
  - Cho phép nhập lại
```

### **Test Case 5: Network Error**
```
Input:
  - Email: user@shop.com
  - Password: 123456
  - Network: Offline

Expected Output:
  - Hiển thị "Lỗi kết nối. Vui lòng kiểm tra Internet"
  - Không crash app
```

---

## 📖 Tài Liệu Tham Khảo

### **UML Standards**
- [UML 2.5 Specification - Activity Diagrams](https://www.omg.org/spec/UML/2.5/)
- [UML Activity Diagram Tutorial](https://www.visual-paradigm.com/guide/uml-unified-modeling-language/what-is-activity-diagram/)

### **Security Best Practices**
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [PHP Password Hashing](https://www.php.net/manual/en/function.password-hash.php)

### **API Design**
- [RESTful API Design Guide](https://restfulapi.net/)
- [HTTP Status Codes](https://httpstatuses.com/)

---

## 📝 Kết Luận

Activity Diagram này đã hoàn thành đầy đủ các yêu cầu:

✅ **Vẽ chuẩn UML 2.5** với đầy đủ các thành phần:
  - Start Node, End Node
  - Activity Node, Decision Node, Merge Node
  - Control Flow, Object Flow

✅ **Swimlane theo chiều dọc** với 4 lanes:
  - User, UI/Client, Backend/API, Database

✅ **Thể hiện đầy đủ các bước**:
  - Nhập email/mật khẩu
  - Validate dữ liệu
  - Gửi API request
  - Kiểm tra database
  - Trả về kết quả
  - Chuyển màn hình (success) hoặc hiển thị lỗi (error)

✅ **Trình bày đẹp, rõ ràng**:
  - Màu sắc phân biệt swimlanes
  - Màu xanh cho success path
  - Màu đỏ cho error path
  - Legend đầy đủ
  - Notes và documentation

---

## 🎯 Tác Giả

- **Ngày tạo:** 29/11/2025
- **Chuẩn:** UML 2.5 Activity Diagram
- **Dự án:** App Thương Mại Điện Tử Bán Đồng Hồ
- **File:** ACTIVITY_02_DANG_NHAP_SWIMLANES_CHUAN_UML.drawio

---

**Hướng dẫn sử dụng:**
1. Mở file `.drawio` bằng Draw.io Desktop hoặc [app.diagrams.net](https://app.diagrams.net)
2. Có thể zoom in/out để xem chi tiết
3. Export sang PDF, PNG, SVG để chia sẻ hoặc in ấn
4. Có thể chỉnh sửa và mở rộng diagram theo nhu cầu

---

**✨ Chúc bạn thành công với dự án! ✨**

