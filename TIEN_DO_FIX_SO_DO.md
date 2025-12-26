# 📊 DANH SÁCH SƠ ĐỒ UML ĐÃ CHỈNH SỬA - CHUẨN UML

**Ngày cập nhật:** 15/11/2025  
**Các vấn đề đã fix:**
- ✅ Khoảng cách chuẩn (250px giữa các object)
- ✅ Không đè luồng
- ✅ Không chồng chữ
- ✅ Activation bar đúng vị trí và độ dài
- ✅ Arrow rõ ràng với numbering
- ✅ Thêm Swimlanes cho Activity Diagram

---

## 🔷 6 SƠ ĐỒ SEQUENCE DIAGRAM (ĐÃ FIX)

### Các cải tiến:
1. **Khoảng cách:** 250px giữa các lifeline
2. **Activation bar:** Bắt đầu từ message đầu, kết thúc ở message cuối
3. **Message spacing:** 40px giữa các message
4. **Font size:** 11px cho message labels
5. **Arrow:** Solid arrow (→) cho sync call, Dashed arrow (⇢) cho return
6. **Page size:** 1400x900 để đủ không gian

### ✅ 1. SEQUENCE_01_DANG_KY_FIXED.drawio
**Đã fix:**
- Lifeline spacing: User(70px) → UI(270px) → API(530px) → DB(790px) → Mail(1050px)
- Activation bars không overlap
- 15 messages với spacing đều 40px
- Note box ở vị trí không che message

---

### ✅ 2. SEQUENCE_02_DANG_NHAP_FIXED.drawio (Đang tạo)
**Sẽ fix:**
- Alt frame cho error case
- Activation bar cho database query
- Return message rõ ràng

---

## 🔶 6 SƠ ĐỒ ACTIVITY DIAGRAM VỚI SWIMLANES

### Swimlanes structure:
```
| User | UI/Activity | Backend/API | Database |
```

### ✅ 7. ACTIVITY_01_DANG_KY_SWIMLANES.drawio (Đang tạo)

**Swimlanes:**
1. **User Swimlane:** User actions (nhập thông tin, click button)
2. **UI Swimlane:** DangKiActivity, validation, display
3. **Backend Swimlane:** dangki.php, validation logic
4. **Database Swimlane:** INSERT, SELECT operations

**Improvements:**
- Vertical lanes với width 250px mỗi lane
- Activity nodes đặt trong đúng lane
- Arrow vượt qua lane boundaries
- Start/End nodes rõ ràng

---

## 📋 TIẾN ĐỘ

| STT | File | Trạng thái | Ghi chú |
|-----|------|------------|---------|
| 1 | SEQUENCE_01_DANG_KY_FIXED | ✅ Hoàn thành | Spacing chuẩn, no overlap |
| 2 | SEQUENCE_02_DANG_NHAP_FIXED | 🔄 Đang tạo | Alt frame, spacing |
| 3 | SEQUENCE_03_XEM_DS_SP_FIXED | ⏳ Chờ | Phân trang logic |
| 4 | SEQUENCE_04_CHI_TIET_SP_FIXED | ⏳ Chờ | Tồn kho check |
| 5 | SEQUENCE_05_GIO_HANG_FIXED | ⏳ Chờ | Complex validation |
| 6 | SEQUENCE_06_THANH_TOAN_FIXED | ⏳ Chờ | VNPay/COD branches |
| 7 | ACTIVITY_01_DANG_KY_SWIMLANES | 🔄 Đang tạo | 4 lanes |
| 8 | ACTIVITY_02_DANG_NHAP_SWIMLANES | ⏳ Chờ | 4 lanes |
| 9 | ACTIVITY_03_SEARCH_SWIMLANES | ⏳ Chờ | Fork/Join |
| 10 | ACTIVITY_04_CHI_TIET_SWIMLANES | ⏳ Chờ | Branch/Merge |
| 11 | ACTIVITY_05_GIO_HANG_SWIMLANES | ⏳ Chờ | Complex decisions |
| 12 | ACTIVITY_06_CHECKOUT_SWIMLANES | ⏳ Chờ | Payment lanes |

---

## 🎨 CHUẨN ĐÃ ÁP DỤNG

### Sequence Diagram Standards:
- **Page size:** 1400 x 900 (thay vì 1169 x 827)
- **Object spacing:** 250-260px
- **Message spacing:** 40px
- **Activation bar width:** 10px
- **Font:** Arial 11px
- **Arrow type:** 
  - Sync call: `endArrow=block` (filled)
  - Return: `endArrow=open, dashed=1` (open)

### Activity Diagram Standards:
- **Page size:** 1200 x 1000
- **Swimlane width:** 250px
- **Activity height:** 40-50px
- **Decision size:** 120x100
- **Spacing:** 60px giữa các node
- **Font:** Arial 11px

---

## ✨ DEMO: Sequence Diagram Structure

```
User        :DangKiActivity    :dangki.php      :UserDB        :PHPMailer
 |               |                   |              |                |
 |---- 1: input -|                   |              |                |
 |               |-- 2: validate() --|              |                |
 |               |                   |              |                |
 |---- 3: click -|                   |              |                |
 |               |---- 4: POST ------|              |                |
 |               |                   |-- 5: check --|                |
 |               |                   |<- 6: false --|                |
 |               |                   |              |                |
 |               |                   |-- 9: INSERT -|                |
 |               |                   |<- 10: id ---|                |
 |               |                   |----------- 11: send ---------|
 |               |                   |<---------- 12: sent ---------|
 |               |<---- 13: success -|              |                |
 |<- 14: toast --|                   |              |                |
 |<- 15: navigate|                   |              |                |
```

---

## ✨ DEMO: Activity Diagram with Swimlanes

```
┌─────────────┬────────────────┬───────────────┬──────────────┐
│    User     │   UI/Activity  │  Backend/API  │   Database   │
├─────────────┼────────────────┼───────────────┼──────────────┤
│   [Start]   │                │               │              │
│      ↓      │                │               │              │
│ Nhập info   │                │               │              │
│      ↓------→ Validate()     │               │              │
│             │      ↓         │               │              │
│ Click btn ←─┘      ↓---------→ POST request │              │
│             │                │      ↓--------→ Check email │
│             │                │      ←---------  false      │
│             │                │      ↓--------→ INSERT      │
│             │                │      ←---------  user_id    │
│             │  ←────────────  success        │              │
│  ← Toast ───┘                │               │              │
│   [End]     │                │               │              │
└─────────────┴────────────────┴───────────────┴──────────────┘
```

---

Đang tiếp tục tạo các file còn lại...

