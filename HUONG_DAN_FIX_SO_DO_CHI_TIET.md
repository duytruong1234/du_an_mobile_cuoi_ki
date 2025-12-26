# 🔧 HƯỚNG DẪN CHỈNH SỬA 12 SƠ ĐỒ UML - CHUẨN HÓATÀI LIỆU

## ⚠️ CÁC VẤN ĐỀ CẦN FIX

### 🔴 Sequence Diagram:
1. **Khoảng cách không chuẩn** → Fix: 250px giữa các lifeline
2. **Đè luồng/chồng message** → Fix: Message spacing 40px
3. **Chồng chữ** → Fix: Font 11px, label positioning
4. **Activation bar sai** → Fix: Đúng thời điểm bắt đầu/kết thúc
5. **Arrow không rõ** → Fix: Solid (sync), Dashed (return)

### 🔴 Activity Diagram:
1. **Thiếu Swimlanes** → Fix: Thêm 3-4 lanes (User, UI, Backend, DB)

---

## ✅ CHUẨN ĐÃ ÁP DỤNG

### Sequence Diagram Measurements:

```xml
<!-- Page size -->
pageWidth="1400" pageHeight="900"

<!-- Object positions (X coordinate) -->
User:        x="70"    (Actor)
UI:          x="270"   (+200px from User, +140 width = 410)
Backend:     x="530"   (+120px gap)
Database:    x="790"   (+120px gap)
Email/Other: x="1050"  (+120px gap)

<!-- Object dimensions -->
Actor:       width="30"  height="60"
Lifeline:    width="140" height="770"
Activation:  width="10"

<!-- Message spacing -->
First message:  y="190"
Second message: y="230"  (+40px)
Third message:  y="270"  (+40px)
...continuing with 40px increments

<!-- Font -->
fontSize="11"
```

### Activity Diagram with Swimlanes:

```xml
<!-- Page size -->
pageWidth="1200" pageHeight="1000"

<!-- Swimlane structure -->
<mxCell id="pool" value="Quy trình [Tên]" style="swimlane;html=1;childLayout=stackLayout;resizeParent=1;resizeParentMax=0;horizontal=1;startSize=30;horizontalStack=0;fillColor=#FFFFFF;strokeColor=#000000;" vertex="1" parent="1">
  <mxGeometry x="40" y="60" width="1100" height="880" as="geometry" />
</mxCell>

<!-- Swimlane 1: User -->
<mxCell id="lane1" value="User" style="swimlane;html=1;startSize=30;fillColor=#FFFFFF;strokeColor=#000000;swimlaneFillColor=#F5F5F5;" vertex="1" parent="pool">
  <mxGeometry y="30" width="275" height="850" as="geometry" />
</mxCell>

<!-- Swimlane 2: UI/Activity -->
<mxCell id="lane2" value="UI/Activity" style="swimlane;html=1;startSize=30;fillColor=#FFFFFF;strokeColor=#000000;swimlaneFillColor=#F5F5F5;" vertex="1" parent="pool">
  <mxGeometry x="275" y="30" width="275" height="850" as="geometry" />
</mxCell>

<!-- Swimlane 3: Backend/API -->
<mxCell id="lane3" value="Backend/API" style="swimlane;html=1;startSize=30;fillColor=#FFFFFF;strokeColor=#000000;swimlaneFillColor=#F5F5F5;" vertex="1" parent="pool">
  <mxGeometry x="550" y="30" width="275" height="850" as="geometry" />
</mxCell>

<!-- Swimlane 4: Database -->
<mxCell id="lane4" value="Database" style="swimlane;html=1;startSize=30;fillColor=#FFFFFF;strokeColor=#000000;swimlaneFillColor=#F5F5F5;" vertex="1" parent="pool">
  <mxGeometry x="825" y="30" width="275" height="850" as="geometry" />
</mxCell>
```

---

## 📝 QUY TRÌNH FIX TỪNG SƠ ĐỒ

### BƯỚC 1: Fix Sequence Diagrams

#### 1.1. Mở file gốc trong Draw.io
```
File → Open → Chọn SEQUENCE_01_DANG_KY_TAI_KHOAN.drawio
```

#### 1.2. Điều chỉnh Page Size
```
File → Page Setup → Custom Size
Width: 1400
Height: 900
```

#### 1.3. Điều chỉnh Object positions
```
1. Select Actor "User"
   - Position: X=70, Y=70
   
2. Select Lifeline ":DangKiActivity"
   - Position: X=270, Y=70
   - Size: Width=140, Height=770
   
3. Select Lifeline ":dangki.php"
   - Position: X=530, Y=70
   - Size: Width=140, Height=770
   
4. Select Lifeline ":UserDB"
   - Position: X=790, Y=70
   - Size: Width=140, Height=770
   
5. Select Lifeline ":PHPMailer"
   - Position: X=1050, Y=70
   - Size: Width=140, Height=770
```

#### 1.4. Điều chỉnh Activation Bars
```
1. Select activation bar on DangKiActivity
   - Start Y: 190 (sau message đầu tiên)
   - Height: 580 (đến trước message cuối)
   - Width: 10
   - Position X: 335 (= lifeline X + width/2)

2. Tương tự cho các lifeline khác
```

#### 1.5. Điều chỉnh Messages
```
Công thức Y position:
message_n = 190 + (n-1) * 40

Message 1:  Y=190
Message 2:  Y=230
Message 3:  Y=270
Message 4:  Y=310
...
Message 15: Y=750
```

#### 1.6. Adjust Font
```
Select all messages:
- Font: Arial
- Size: 11
- Style: Regular
```

---

### BƯỚC 2: Add Swimlanes to Activity Diagrams

#### 2.1. Mở file Activity gốc
```
File → Open → Chọn ACTIVITY_01_DANG_KY_TAI_KHOAN.drawio
```

#### 2.2. Delete toàn bộ nội dung cũ
```
Ctrl+A → Delete
```

#### 2.3. Insert Swimlane Pool
```
1. Từ left panel, chọn "Swimlane" (hoặc tìm "pool")
2. Kéo vào canvas
3. Resize: Width=1100, Height=880
4. Position: X=40, Y=60
5. Edit title: "Quy trình Đăng ký tài khoản"
```

#### 2.4. Add Vertical Lanes
```
1. Right-click on pool → Add Lane (repeat 3 times to have 4 lanes)
2. Resize each lane: Width=275
3. Edit lane titles:
   - Lane 1: "User"
   - Lane 2: "UI/Activity"  
   - Lane 3: "Backend/API"
   - Lane 4: "Database"
```

#### 2.5. Place Activities in Lanes
```
Activity nodes position:

Lane 1 (User):
- Start node: X=150, Y=100
- "Nhập thông tin": X=100, Y=160
- "Click Đăng ký": X=100, Y=260

Lane 2 (UI/Activity):
- "Validate()": X=375, Y=210
- "Hiển thị lỗi": X=375, Y=310 (if error)
- "showToast()": X=375, Y=660

Lane 3 (Backend/API):
- "POST dangki.php": X=650, Y=360
- "Format response": X=650, Y=560

Lane 4 (Database):
- "CHECK email exists": X=925, Y=410
- "INSERT user": X=925, Y=510
```

#### 2.6. Connect với Arrows
```
Arrows vượt qua lane boundaries:
- User → UI: From lane1 to lane2
- UI → Backend: From lane2 to lane3  
- Backend → DB: From lane3 to lane4
- DB → Backend: Return arrows (dashed)
```

---

## 🎨 TEMPLATE XML

### Sequence Diagram Template:

```xml
<mxCell id="msg-1" value="1: messageName(params)" 
        style="html=1;verticalAlign=bottom;endArrow=block;strokeColor=#000000;fontSize=11;" 
        edge="1" parent="1" 
        source="life-source" 
        target="life-target">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="[source_x]" y="[y_pos]" as="sourcePoint" />
    <mxPoint x="[target_x]" y="[y_pos]" as="targetPoint" />
  </mxGeometry>
</mxCell>
```

### Return Message Template:

```xml
<mxCell id="return-1" value="return data" 
        style="html=1;verticalAlign=bottom;endArrow=open;dashed=1;endSize=8;strokeColor=#000000;fontSize=11;" 
        edge="1" parent="1" 
        source="life-source" 
        target="life-target">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="[source_x]" y="[y_pos]" as="sourcePoint" />
    <mxPoint x="[target_x]" y="[y_pos]" as="targetPoint" />
  </mxGeometry>
</mxCell>
```

### Activity with Swimlanes Template:

```xml
<!-- Activity Node -->
<mxCell id="act-1" value="Activity Name" 
        style="rounded=1;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;fontSize=11;" 
        vertex="1" parent="lane1">
  <mxGeometry x="50" y="100" width="160" height="40" as="geometry" />
</mxCell>

<!-- Decision Node -->
<mxCell id="dec-1" value="Condition?" 
        style="rhombus;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;fontSize=11;" 
        vertex="1" parent="lane2">
  <mxGeometry x="50" y="180" width="120" height="100" as="geometry" />
</mxCell>

<!-- Arrow crossing lanes -->
<mxCell id="edge-1" value="label" 
        style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;strokeColor=#000000;endArrow=block;endFill=1;fontSize=11;" 
        edge="1" parent="1" 
        source="act-1" 
        target="act-2">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

---

## 📊 CHECKLIST KHI FIX XONG

### Sequence Diagram:
- [ ] Page size: 1400x900
- [ ] Object spacing: 250-260px
- [ ] Message spacing: 40px
- [ ] Activation bars: Correct start/end
- [ ] All messages numbered (1, 2, 3...)
- [ ] Sync calls: solid arrow
- [ ] Returns: dashed open arrow
- [ ] Font: 11px
- [ ] No overlapping text
- [ ] No overlapping arrows

### Activity Diagram:
- [ ] Has 3-4 swimlanes
- [ ] Lane titles clear (User, UI, Backend, DB)
- [ ] Start node in first lane
- [ ] End node in last lane
- [ ] Activities in correct lanes
- [ ] Arrows cross lanes properly
- [ ] Decision nodes: diamond shape
- [ ] Fork/Join: thick bar (if needed)
- [ ] No overlapping nodes
- [ ] Flow is clear top-to-bottom

---

## 💡 TIPS

1. **Alignment:** Use Draw.io's alignment tools (Arrange → Align)
2. **Distribution:** Use "Distribute horizontally/vertically" for even spacing
3. **Grid:** Enable grid (View → Grid) and snap to grid (20px grid recommended)
4. **Layers:** Use layers to separate diagram elements
5. **Groups:** Group related elements (Ctrl+G)
6. **Styles:** Save custom styles for consistency

---

## 🚀 EXPORT

Sau khi fix xong:

```
File → Export as → PNG
- Resolution: 300 DPI
- Border: 20px
- Background: White
- Transparent: No

Save as:
- SEQUENCE_01_DANG_KY_FIXED_FINAL.png
- ACTIVITY_01_DANG_KY_SWIMLANES_FINAL.png
```

---

**Ngày tạo:** 15/11/2025  
**Tool:** Draw.io 22.0.0  
**Chuẩn:** UML 2.5

