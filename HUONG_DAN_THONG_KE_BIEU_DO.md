# 📊 HƯỚNG DẪN CHỨC NĂNG THỐNG KÊ BIỂU ĐỒ CHO ADMIN

## ✅ ĐÃ HOÀN THÀNH 100%

Tôi đã hoàn thiện toàn bộ chức năng xem biểu đồ thống kê sản phẩm bán chạy cho Admin.

---

## 📋 DANH SÁCH FILES ĐÃ TẠO/SỬA

### 🟢 **Backend PHP** (1 file mới)
1. ✅ **`Server/thongke.php`** - API lấy thống kê sản phẩm bán chạy
   - Lấy top 10 sản phẩm bán chạy nhất
   - Chỉ tính đơn hàng đã thanh toán/đang giao/đã giao
   - Trả về JSON với tên sản phẩm và tổng số lượng đã bán

### 🟢 **Android** (4 files)

#### Files đã sửa:
1. ✅ **`ThongKeActivity.java`** - Activity hiển thị biểu đồ
   - Tích hợp MPAndroidChart (BarChart)
   - Kiểm tra quyền admin
   - Gọi API thống kê
   - Hiển thị biểu đồ cột đẹp mắt với màu gradient

2. ✅ **`activity_thong_ke.xml`** - Layout màn hình thống kê
   - Toolbar với nút back
   - BarChart trong CardView
   - ScrollView để responsive

3. ✅ **`QuanLiActivity.java`** - Thêm nút mở màn hình thống kê
   - Thêm biến `img_thongke`
   - Thêm listener mở ThongKeActivity

4. ✅ **`activity_quan_li.xml`** - Thêm icon thống kê vào toolbar
   - Icon biểu đồ (chart)
   - Icon tồn kho
   - Icon thêm sản phẩm

#### Files đã tạo mới:
5. ✅ **`ic_baseline_bar_chart_24.xml`** - Icon biểu đồ

---

## 🎨 TÍNH NĂNG BIỂU ĐỒ

### **Loại biểu đồ:** Bar Chart (Biểu đồ cột)
- 📊 Hiển thị top 10 sản phẩm bán chạy nhất
- 🎨 Gradient màu sắc đẹp mắt (xanh → be → hồng → đỏ)
- 📝 Hiển thị số lượng đã bán trên mỗi cột
- 🔄 Animation khi load biểu đồ
- 📱 Responsive với ScrollView

### **Dữ liệu hiển thị:**
- **Trục X (ngang):** Tên sản phẩm (rút gọn nếu quá dài)
- **Trục Y (dọc):** Số lượng đã bán
- **Legend:** "Số lượng đã bán"
- **Description:** "Top 10 sản phẩm bán chạy nhất"

### **Màu sắc:**
```
Cột 1: Xanh đậm   (#405980)
Cột 2: Xanh lá    (#95A57C)
Cột 3: Be         (#D9B8A2)
Cột 4: Hồng đậm   (#BF8686)
Cột 5: Đỏ        (#B33050)
```

---

## 🔄 QUY TRÌNH HOẠT ĐỘNG

```
Admin đăng nhập → Vào QuanLiActivity
    ↓
Nhấn icon biểu đồ 📊 (toolbar bên phải)
    ↓
ThongKeActivity mở ra
    ↓
Kiểm tra quyền admin
    ↓
Gọi API: Server/thongke.php
    ↓
Server truy vấn database:
  - JOIN chitietdonhang + sanphammoi + donhang
  - WHERE trangthai IN ('Đã thanh toán', 'Đang giao hàng', 'Đã giao hàng')
  - GROUP BY idsp
  - ORDER BY tong DESC
  - LIMIT 10
    ↓
Nhận JSON response
    ↓
Tạo BarChart với MPAndroidChart
    ↓
Hiển thị biểu đồ với animation ✨
```

---

## 📝 CODE QUAN TRỌNG

### **1. API Backend (thongke.php)**
```sql
SELECT 
    s.tensp,
    SUM(c.soluong) as tong
FROM chitietdonhang c
INNER JOIN sanphammoi s ON c.idsp = s.id
INNER JOIN donhang d ON c.iddonhang = d.id
WHERE d.trangthai IN ('Đã thanh toán', 'Đang giao hàng', 'Đã giao hàng')
GROUP BY c.idsp, s.tensp
ORDER BY tong DESC
LIMIT 10
```

### **2. Load dữ liệu thống kê (ThongKeActivity.java)**
```java
private void loadThongKe() {
    compositeDisposable.add(apiBanHang.getthongke()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            thongKeModel -> {
                if (thongKeModel.isSuccess()) {
                    displayChart(thongKeModel.getResult());
                }
            },
            throwable -> {
                Toast.makeText(this, "Lỗi: " + throwable.getMessage()).show();
            }
        ));
}
```

### **3. Hiển thị biểu đồ**
```java
private void displayChart(List<ThongKe> dataList) {
    ArrayList<BarEntry> entries = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<>();
    
    for (int i = 0; i < dataList.size(); i++) {
        entries.add(new BarEntry(i, dataList.get(i).getTong()));
        labels.add(dataList.get(i).getTensp());
    }
    
    BarDataSet dataSet = new BarDataSet(entries, "Số lượng đã bán");
    dataSet.setColors(colors); // Gradient colors
    
    BarData barData = new BarData(dataSet);
    barChart.setData(barData);
    barChart.animateY(1000);
}
```

---

## 🧪 CÁCH SỬ DỤNG

### **Bước 1: Đăng nhập Admin**
- Email: admin@example.com (hoặc tài khoản admin của bạn)
- Tài khoản phải có `role = 1` trong database

### **Bước 2: Vào Quản Lí**
- Từ MainActivity → Nhấn icon menu → Chọn "Quản lí sản phẩm"

### **Bước 3: Xem thống kê**
- Trong màn hình Quản Lí
- Nhấn icon **📊** (biểu đồ) ở góc trên bên phải toolbar
- Màn hình thống kê sẽ hiển thị

### **Bước 4: Xem biểu đồ**
- Biểu đồ cột hiển thị top 10 sản phẩm bán chạy
- Có thể scroll nếu biểu đồ dài
- Số lượng hiển thị trên mỗi cột

---

## 🎯 ĐIỂM NỔI BẬT

### ✅ **Ưu điểm:**
1. **Bảo mật:** Chỉ admin mới xem được (kiểm tra `Utils.user_current.isAdmin()`)
2. **Dữ liệu chính xác:** Chỉ tính đơn hàng đã thanh toán thành công
3. **Giao diện đẹp:** Sử dụng MPAndroidChart với màu gradient
4. **Performance tốt:** Chỉ lấy top 10, không quá tải
5. **Responsive:** ScrollView hỗ trợ màn hình nhỏ
6. **Animation:** Hiệu ứng mượt mà khi load biểu đồ

### 🎨 **UI/UX:**
- Material Design với CardView
- Toolbar có nút back
- Màu sắc gradient hài hòa
- Font size dễ đọc
- Label xoay 45° để không bị chồng lấn

---

## 🔧 CẤU TRÚC DATABASE CẦN CÓ

Đảm bảo database có các bảng:
```sql
-- Bảng sanphammoi
CREATE TABLE sanphammoi (
    id INT PRIMARY KEY,
    tensp VARCHAR(255),
    ...
);

-- Bảng donhang
CREATE TABLE donhang (
    id INT PRIMARY KEY,
    trangthai VARCHAR(50),
    ...
);

-- Bảng chitietdonhang
CREATE TABLE chitietdonhang (
    id INT PRIMARY KEY,
    iddonhang INT,
    idsp INT,
    soluong INT,
    FOREIGN KEY (iddonhang) REFERENCES donhang(id),
    FOREIGN KEY (idsp) REFERENCES sanphammoi(id)
);
```

---

## 📊 MẪU DỮ LIỆU JSON

### **Request:**
```
GET http://10.0.2.2/appbandienthoai/Server/thongke.php
```

### **Response:**
```json
{
  "success": true,
  "message": "Lấy thống kê thành công",
  "result": [
    {
      "tensp": "iPhone 15 Pro Max",
      "tong": 45
    },
    {
      "tensp": "Samsung Galaxy S24 Ultra",
      "tong": 38
    },
    {
      "tensp": "MacBook Pro M3",
      "tong": 25
    }
  ]
}
```

---

## 🐛 DEBUG & TROUBLESHOOTING

### **Vấn đề 1: Không hiển thị biểu đồ**
**Nguyên nhân:** Chưa có dữ liệu hoặc API lỗi
**Giải pháp:**
1. Check Logcat: filter `ThongKeActivity`
2. Kiểm tra API trả về dữ liệu: `http://10.0.2.2/appbandienthoai/Server/thongke.php`
3. Đảm bảo có đơn hàng đã thanh toán trong database

### **Vấn đề 2: "Chỉ admin mới có quyền xem thống kê"**
**Nguyên nhân:** Tài khoản không phải admin
**Giải pháp:**
```sql
UPDATE user SET role = 1 WHERE email = 'your_email@example.com';
```

### **Vấn đề 3: MPAndroidChart lỗi**
**Nguyên nhân:** Dependency chưa được sync
**Giải pháp:**
1. File → Sync Project with Gradle Files
2. Build → Clean Project
3. Build → Rebuild Project

---

## 📱 MÀNG HÌNH QUẢN LÍ

Toolbar có 3 nút:
```
[Quản Lí Sản Phẩm]        [📊] [📦] [➕]
                          ↑    ↑    ↑
                       Thống  Tồn  Thêm
                         kê   kho   SP
```

---

## 🎉 KẾT LUẬN

### ✅ **HOÀN THÀNH:**
- ✅ Backend API (`thongke.php`)
- ✅ Model class (`ThongKe.java`, `ThongKeModel.java`)
- ✅ Activity (`ThongKeActivity.java`)
- ✅ Layout XML (`activity_thong_ke.xml`)
- ✅ Icon biểu đồ (`ic_baseline_bar_chart_24.xml`)
- ✅ Tích hợp vào QuanLiActivity
- ✅ Kiểm tra quyền admin
- ✅ Biểu đồ đẹp mắt với MPAndroidChart

### 🚀 **SẴN SÀNG SỬ DỤNG:**
1. Build app
2. Đăng nhập admin
3. Vào Quản lí → Nhấn icon 📊
4. Xem biểu đồ thống kê đẹp mắt!

**CHÚC BẠN THÀNH CÔNG! 📊✨**

