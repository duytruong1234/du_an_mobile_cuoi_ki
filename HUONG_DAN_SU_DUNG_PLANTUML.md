# 🎨 HƯỚNG DẪN SỬ DỤNG SƠ ĐỒ PLANTUML

## 📁 DANH SÁCH FILE PLANTUML ĐÃ TẠO

### ✅ Đã tạo 3 file sơ đồ chuyên nghiệp:

1. **ARCHITECTURE_DIAGRAM_COMPLETE.puml** ⭐ KHUYẾN NGHỊ
   - Sơ đồ kiến trúc hệ thống đầy đủ nhất
   - Bao gồm: Client, Server, Database, External Services
   - Chi tiết: 23 Activities, 50+ APIs, 13 Tables, 3 Triggers
   - Relationships và Data flow
   - Kích thước: ~700 dòng code

2. **ARCHITECTURE_DIAGRAM_SIMPLE.puml**
   - Phiên bản đơn giản, dễ hiểu
   - Tổng quan 4 tầng chính
   - Phù hợp cho presentation
   - Kích thước: ~200 dòng code

3. **DEPLOYMENT_DIAGRAM.puml**
   - Sơ đồ triển khai (Deployment)
   - Development vs Production environment
   - Infrastructure details
   - Deployment process
   - Kích thước: ~300 dòng code

---

## 🚀 CÁCH XEM SƠ ĐỒ

### Phương án 1: VS Code (KHUYẾN NGHỊ) ⭐

#### Bước 1: Cài đặt extension
```
1. Mở VS Code
2. Vào Extensions (Ctrl+Shift+X)
3. Tìm "PlantUML" by jebbs
4. Click Install
```

#### Bước 2: Cài đặt Java (bắt buộc)
```
PlantUML cần Java để chạy

• Download Java: https://www.java.com/download/
• Hoặc dùng OpenJDK: https://adoptium.net/

Kiểm tra đã cài Java:
java -version
```

#### Bước 3: Cài đặt Graphviz (tùy chọn, cải thiện layout)
```
• Download: https://graphviz.org/download/
• Windows: Chạy installer
• Thêm vào PATH environment variable
```

#### Bước 4: Xem sơ đồ
```
1. Mở file .puml trong VS Code
2. Nhấn Alt+D để preview
3. Hoặc click chuột phải → "Preview Current Diagram"
4. Sơ đồ sẽ hiển thị ở panel bên phải
```

#### Bước 5: Export ra file ảnh
```
1. Click chuột phải trong preview panel
2. Chọn "Export Current Diagram"
3. Chọn format:
   • PNG (khuyến nghị cho báo cáo)
   • SVG (cho web, scalable)
   • PDF (cho document)
4. Chọn thư mục lưu
```

---

### Phương án 2: Online (NHANH NHẤT) 🌐

#### Bước 1: Truy cập PlantUML Online
```
https://www.plantuml.com/plantuml/uml/
```

#### Bước 2: Copy code
```
1. Mở file .puml trong Notepad hoặc editor bất kỳ
2. Copy TOÀN BỘ nội dung (Ctrl+A, Ctrl+C)
```

#### Bước 3: Paste và xem
```
1. Paste vào editor trên website
2. Click nút "Submit" hoặc nhấn Ctrl+Enter
3. Sơ đồ sẽ hiển thị ngay lập tức
```

#### Bước 4: Download ảnh
```
1. Click vào sơ đồ
2. Chọn format muốn download:
   • PNG (khuyến nghị)
   • SVG
   • LaTeX
   • ASCII Art
3. Ảnh sẽ tự động download
```

**LƯU Ý:** Website này có giới hạn độ phức tạp. Nếu sơ đồ quá lớn, hãy dùng VS Code.

---

### Phương án 3: IntelliJ IDEA / Android Studio 🔧

#### Bước 1: Cài đặt plugin
```
1. Mở Settings (Ctrl+Alt+S)
2. Vào Plugins
3. Tìm "PlantUML integration"
4. Click Install
5. Restart IDE
```

#### Bước 2: Xem sơ đồ
```
1. Mở file .puml
2. Toolbar sẽ xuất hiện icon PlantUML
3. Click icon để xem preview
4. Hoặc nhấn Ctrl+Shift+P
```

#### Bước 3: Export
```
1. Right-click trong editor
2. Chọn "PlantUML" → "Export"
3. Chọn format và location
```

---

### Phương án 4: Command Line (ADVANCED) 💻

#### Cài đặt PlantUML JAR
```bash
# Download plantuml.jar
wget https://sourceforge.net/projects/plantuml/files/plantuml.jar/download

# Hoặc
curl -L https://sourceforge.net/projects/plantuml/files/plantuml.jar/download -o plantuml.jar
```

#### Generate sơ đồ
```bash
# PNG format (default)
java -jar plantuml.jar ARCHITECTURE_DIAGRAM_COMPLETE.puml

# SVG format (scalable)
java -jar plantuml.jar -tsvg ARCHITECTURE_DIAGRAM_COMPLETE.puml

# PDF format
java -jar plantuml.jar -tpdf ARCHITECTURE_DIAGRAM_COMPLETE.puml

# Tất cả files .puml trong thư mục
java -jar plantuml.jar *.puml

# Output vào thư mục cụ thể
java -jar plantuml.jar -o "D:/Output" ARCHITECTURE_DIAGRAM_COMPLETE.puml
```

---

## 📊 SO SÁNH PHƯƠNG ÁN

| Phương án | Độ dễ | Chất lượng | Tốc độ | Export | Khuyến nghị |
|-----------|-------|------------|--------|--------|-------------|
| VS Code | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ Tất cả format | ⭐⭐⭐⭐⭐ |
| Online | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ PNG, SVG | ⭐⭐⭐⭐ |
| Android Studio | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ Tất cả format | ⭐⭐⭐ |
| Command Line | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ Tất cả format | ⭐⭐ |

**Khuyến nghị:**
- Lần đầu xem: Dùng **Online** (nhanh nhất)
- Làm việc thường xuyên: Dùng **VS Code** (tốt nhất)
- Đang mở Android Studio: Dùng **Plugin**
- Automation/CI: Dùng **Command Line**

---

## 🎨 TÙY CHỈNH SƠ ĐỒ

### Thay đổi màu sắc
```plantuml
' Thêm vào đầu file
skinparam backgroundColor #F5F5F5
skinparam componentBackgroundColor #E3F2FD
skinparam componentBorderColor #1976D2
```

### Thay đổi theme
```plantuml
' Chọn theme có sẵn
!theme plain
!theme bluegray
!theme materia
!theme materia-outline
!theme sketchy
!theme sketchy-outline
```

### Thay đổi font
```plantuml
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam defaultFontColor #333333
```

### Ẩn/hiện phần tử
```plantuml
' Ẩn legend
hide legend

' Ẩn footer
hide footbox

' Chỉ hiện Client layer
package "CLIENT LAYER" {
    ' ... code ...
}
' Comment out các layer khác
```

---

## 🔧 TROUBLESHOOTING

### Lỗi: "Cannot find Java"
**Giải pháp:**
```
1. Cài Java: https://www.java.com/download/
2. Kiểm tra: java -version
3. Thêm Java vào PATH:
   - Windows: System Properties → Environment Variables
   - Add: C:\Program Files\Java\jdk-xx\bin
4. Restart VS Code/IDE
```

### Lỗi: "Graphviz not found"
**Giải pháp:**
```
1. Download Graphviz: https://graphviz.org/download/
2. Cài đặt (Windows: chạy installer)
3. Thêm vào PATH: C:\Program Files\Graphviz\bin
4. Restart
```

### Lỗi: Sơ đồ không hiển thị đầy đủ
**Giải pháp:**
```
1. Sơ đồ quá lớn → Dùng file SIMPLE
2. Online timeout → Dùng VS Code hoặc Command Line
3. Export PNG với độ phân giải cao:
   java -jar plantuml.jar -DPPI=300 file.puml
```

### Lỗi: Font Unicode (tiếng Việt) không hiển thị
**Giải pháp:**
```plantuml
' Thêm vào đầu file
skinparam defaultFontName "Arial Unicode MS"
' Hoặc
skinparam defaultFontName "Times New Roman"
```

### Lỗi: Syntax error
**Giải pháp:**
```
1. Check dấu ngoặc { } đóng/mở
2. Check dấu @ ở đầu và cuối file
3. Check typo trong keyword (component, package, etc)
4. Dùng online editor để tìm lỗi nhanh
```

---

## 📚 HƯỚNG DẪN SỬ DỤNG TỪNG FILE

### 1️⃣ ARCHITECTURE_DIAGRAM_COMPLETE.puml

**Khi nào dùng:**
- Cần hiểu chi tiết toàn bộ hệ thống
- Làm tài liệu kỹ thuật
- Onboarding developer mới
- Technical documentation

**Cách dùng hiệu quả:**
```
1. Xem toàn bộ trước để có overview
2. Zoom vào từng package để xem chi tiết:
   • Client Layer → Xem Activities
   • Server Layer → Xem API modules
   • Database Layer → Xem Tables & Triggers
3. Follow các mũi tên để hiểu data flow
4. Đọc notes để hiểu thêm context
```

**Export khuyến nghị:**
- Format: PNG (độ phân giải cao)
- Size: A3 hoặc A2 nếu in ra
- DPI: 300 cho quality tốt

---

### 2️⃣ ARCHITECTURE_DIAGRAM_SIMPLE.puml

**Khi nào dùng:**
- Present cho sếp/khách hàng
- Meeting overview
- Quick explanation
- PowerPoint presentation

**Cách dùng hiệu quả:**
```
1. Dùng làm slide đầu tiên để giới thiệu
2. Giải thích 4 tầng chính:
   • Client → User interface
   • Server → Business logic
   • Database → Data storage
   • External → Third-party services
3. Nhấn mạnh data flow giữa các tầng
4. Show luồng mua hàng trong note
```

**Export khuyến nghị:**
- Format: PNG hoặc SVG
- Size: 1920x1080 (16:9) cho slide
- Background: Trắng (dễ nhìn trên projector)

---

### 3️⃣ DEPLOYMENT_DIAGRAM.puml

**Khi nào dùng:**
- Plan deployment lên production
- Setup infrastructure
- DevOps documentation
- System administration

**Cách dùng hiệu quả:**
```
1. So sánh Development vs Production
2. Hiểu deployment process (10 bước)
3. Xác định server requirements
4. Plan backup & monitoring strategy
5. Checklist trước khi deploy
```

**Export khuyến nghị:**
- Format: PDF cho documentation
- Include trong deployment guide
- Share với DevOps team

---

## 🎯 USE CASES CỤ THỂ

### Use Case 1: Làm báo cáo đồ án
```
Bước 1: Export ARCHITECTURE_DIAGRAM_SIMPLE.puml → PNG
Bước 2: Insert vào Word/PowerPoint (Overview)
Bước 3: Export ARCHITECTURE_DIAGRAM_COMPLETE.puml → PNG
Bước 4: Insert vào phần phụ lục (Chi tiết)
Bước 5: Export DEPLOYMENT_DIAGRAM.puml → PDF
Bước 6: Attach trong phần triển khai
```

### Use Case 2: Present cho giáo viên/sếp
```
PowerPoint structure:
Slide 1: Title + Project info
Slide 2: ARCHITECTURE_DIAGRAM_SIMPLE (Overview)
Slide 3: Zoom vào Client Layer (Android App)
Slide 4: Zoom vào Server Layer (PHP APIs)
Slide 5: Zoom vào Database Layer (Tables)
Slide 6: DEPLOYMENT_DIAGRAM (Production plan)
Slide 7: Demo live app
```

### Use Case 3: Onboarding developer mới
```
Day 1: Đọc ARCHITECTURE_DIAGRAM_SIMPLE
       → Hiểu tổng quan 4 tầng
       
Day 2: Đọc ARCHITECTURE_DIAGRAM_COMPLETE
       → Hiểu chi tiết từng module
       
Day 3: Follow data flow examples
       → Hiểu luồng xử lý
       
Day 4: Xem DEPLOYMENT_DIAGRAM
       → Hiểu environment setup
       
Day 5: Hands-on coding
```

### Use Case 4: Viết tài liệu kỹ thuật
```
Document structure:
1. Executive Summary
2. System Overview (SIMPLE diagram)
3. Detailed Architecture (COMPLETE diagram)
   3.1 Client Layer
   3.2 Server Layer
   3.3 Database Layer
   3.4 External Services
4. Deployment Guide (DEPLOYMENT diagram)
5. API Documentation
6. Database Schema
```

---

## 💡 TIPS & TRICKS

### Tip 1: Xem sơ đồ nhanh mà không cần cài đặt
```
1. Vào: http://www.plantuml.com/plantuml/
2. Paste code
3. Copy link PNG
4. Share link với team
```

### Tip 2: Generate nhiều format cùng lúc
```bash
java -jar plantuml.jar -tpng -tsvg -tpdf ARCHITECTURE_DIAGRAM_COMPLETE.puml
```

### Tip 3: Auto-generate khi file thay đổi (VS Code)
```
1. Cài extension "PlantUML"
2. Settings → PlantUML: Export on Save
3. Mỗi lần save file → auto export PNG
```

### Tip 4: Zoom in/out trong preview
```
VS Code:
• Zoom in: Ctrl + Scroll Up
• Zoom out: Ctrl + Scroll Down
• Fit to window: Double click

Online:
• Browser zoom: Ctrl + Plus/Minus
```

### Tip 5: Copy sơ đồ dạng link
```
PlantUML có thể encode diagram thành URL:
http://www.plantuml.com/plantuml/png/[encoded]

Dùng tool encode tại:
http://www.plantuml.com/plantuml/form
```

---

## 📖 TÀI LIỆU THAM KHẢO

### PlantUML Documentation
- Website chính thức: https://plantuml.com/
- Component Diagram: https://plantuml.com/component-diagram
- Deployment Diagram: https://plantuml.com/deployment-diagram
- Styling Guide: https://plantuml.com/skinparam
- Color Names: https://plantuml.com/color

### Video Tutorials
- PlantUML Crash Course: YouTube
- Architecture Diagrams with PlantUML: YouTube
- VS Code PlantUML Setup: YouTube

### Community & Support
- PlantUML Forum: https://forum.plantuml.net/
- GitHub Issues: https://github.com/plantuml/plantuml/issues
- Stack Overflow: Tag [plantuml]

---

## ✅ CHECKLIST SỬ DỤNG

- [ ] Đã cài Java (kiểm tra: `java -version`)
- [ ] Đã cài VS Code + PlantUML extension (hoặc dùng online)
- [ ] Đã mở được file .puml
- [ ] Đã xem được preview sơ đồ
- [ ] Đã export được PNG/SVG
- [ ] Đã hiểu cách đọc sơ đồ
- [ ] Đã biết khi nào dùng file nào
- [ ] Đã áp dụng vào báo cáo/presentation

---

## 🎉 KẾT LUẬN

Bạn đã có **3 sơ đồ PlantUML chuyên nghiệp**:

1. ✅ **COMPLETE** - Chi tiết đầy đủ (700 dòng)
2. ✅ **SIMPLE** - Dễ hiểu, gọn gàng (200 dòng)
3. ✅ **DEPLOYMENT** - Triển khai hệ thống (300 dòng)

**Tổng cộng: 1,200 dòng PlantUML code chuyên nghiệp!**

### Bước tiếp theo:
1. Chọn phương án xem sơ đồ (khuyến nghị: VS Code hoặc Online)
2. Mở file **ARCHITECTURE_DIAGRAM_SIMPLE.puml** trước
3. Export ra PNG để dùng trong báo cáo
4. Xem **ARCHITECTURE_DIAGRAM_COMPLETE.puml** để hiểu chi tiết
5. Tham khảo **DEPLOYMENT_DIAGRAM.puml** khi triển khai

---

**📅 Ngày tạo:** 30/11/2025  
**👨‍💻 Dự án:** Ứng Dụng Bán Đồng Hồ  
**🎯 Trạng thái:** ✅ READY TO USE

**Chúc bạn thành công với dự án! 🚀**

