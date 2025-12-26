<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Voucher - Admin</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
            padding: 20px;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        h1 {
            color: #333;
            margin-bottom: 30px;
            padding-bottom: 15px;
            border-bottom: 3px solid #4CAF50;
        }

        .toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            gap: 15px;
            flex-wrap: wrap;
        }

        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
            transition: all 0.3s;
        }

        .btn-primary {
            background: #4CAF50;
            color: white;
        }

        .btn-primary:hover {
            background: #45a049;
        }

        .btn-danger {
            background: #f44336;
            color: white;
        }

        .btn-warning {
            background: #ff9800;
            color: white;
        }

        .btn-info {
            background: #2196F3;
            color: white;
        }

        .btn-sm {
            padding: 5px 10px;
            font-size: 12px;
        }

        .filters {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            align-items: center;
        }

        .filters select, .filters input {
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
        }

        .table-container {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        th {
            background: #4CAF50;
            color: white;
            font-weight: bold;
        }

        tr:hover {
            background: #f5f5f5;
        }

        .badge {
            padding: 5px 10px;
            border-radius: 3px;
            font-size: 12px;
            font-weight: bold;
        }

        .badge-success {
            background: #4CAF50;
            color: white;
        }

        .badge-danger {
            background: #f44336;
            color: white;
        }

        .badge-warning {
            background: #ff9800;
            color: white;
        }

        .badge-secondary {
            background: #666;
            color: white;
        }

        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            z-index: 1000;
        }

        .modal-content {
            background: white;
            margin: 50px auto;
            padding: 30px;
            border-radius: 10px;
            max-width: 600px;
            max-height: 90vh;
            overflow-y: auto;
        }

        .modal-header {
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #4CAF50;
        }

        .close {
            float: right;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
            color: #999;
        }

        .close:hover {
            color: #333;
        }

        .form-group {
            margin-bottom: 15px;
        }

        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #333;
        }

        .form-group input, .form-group select, .form-group textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
        }

        .form-group textarea {
            resize: vertical;
            min-height: 80px;
        }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }

        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
            display: none;
        }

        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }

        .stat-card h3 {
            font-size: 14px;
            margin-bottom: 10px;
            opacity: 0.9;
        }

        .stat-card .number {
            font-size: 32px;
            font-weight: bold;
        }

        .loading {
            text-align: center;
            padding: 20px;
            display: none;
        }

        .spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #4CAF50;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 0 auto;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🎫 Quản Lý Voucher / Mã Giảm Giá</h1>

        <!-- Alert Messages -->
        <div id="alertSuccess" class="alert alert-success"></div>
        <div id="alertError" class="alert alert-error"></div>

        <!-- Statistics -->
        <div class="stats">
            <div class="stat-card">
                <h3>Tổng số voucher</h3>
                <div class="number" id="totalVouchers">0</div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <h3>Đang hoạt động</h3>
                <div class="number" id="activeVouchers">0</div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <h3>Tổng lượt sử dụng</h3>
                <div class="number" id="totalUsage">0</div>
            </div>
            <div class="stat-card" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
                <h3>Tổng tiền giảm</h3>
                <div class="number" id="totalDiscount">0đ</div>
            </div>
        </div>

        <!-- Toolbar -->
        <div class="toolbar">
            <button class="btn btn-primary" onclick="openAddModal()">
                ➕ Thêm Voucher Mới
            </button>

            <div class="filters">
                <select id="filterStatus" onchange="loadVouchers()">
                    <option value="all">Tất cả trạng thái</option>
                    <option value="active">Đang hoạt động</option>
                    <option value="expired">Hết hạn</option>
                    <option value="disabled">Đã tắt</option>
                </select>

                <select id="filterType" onchange="loadVouchers()">
                    <option value="all">Tất cả loại</option>
                    <option value="percent">Giảm %</option>
                    <option value="fixed">Giảm cố định</option>
                    <option value="freeship">Free Ship</option>
                </select>

                <input type="text" id="searchBox" placeholder="🔍 Tìm mã voucher..."
                       onkeyup="searchVoucher()" style="min-width: 200px;">

                <button class="btn btn-info btn-sm" onclick="loadVouchers()">🔄 Làm mới</button>
            </div>
        </div>

        <!-- Loading -->
        <div class="loading" id="loading">
            <div class="spinner"></div>
            <p>Đang tải dữ liệu...</p>
        </div>

        <!-- Table -->
        <div class="table-container">
            <table id="voucherTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Mã Voucher</th>
                        <th>Tên Voucher</th>
                        <th>Loại</th>
                        <th>Giá Trị</th>
                        <th>Đã Dùng / Tổng</th>
                        <th>Hết Hạn</th>
                        <th>Trạng Thái</th>
                        <th>Hành Động</th>
                    </tr>
                </thead>
                <tbody id="voucherTableBody">
                    <!-- Data will be loaded here -->
                </tbody>
            </table>
        </div>
    </div>

    <!-- Modal Add/Edit Voucher -->
    <div id="voucherModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <span class="close" onclick="closeModal()">&times;</span>
                <h2 id="modalTitle">Thêm Voucher Mới</h2>
            </div>

            <form id="voucherForm" onsubmit="saveVoucher(event)">
                <input type="hidden" id="voucherId" name="id">

                <div class="form-row">
                    <div class="form-group">
                        <label>Mã Voucher *</label>
                        <input type="text" id="maVoucher" name="ma_voucher" required
                               placeholder="VD: NEWUSER20" style="text-transform: uppercase;">
                    </div>

                    <div class="form-group">
                        <label>Loại Giảm *</label>
                        <select id="loaiGiam" name="loai_giam" required onchange="toggleGiamToiDa()">
                            <option value="percent">Giảm theo %</option>
                            <option value="fixed">Giảm cố định</option>
                            <option value="freeship">Free Ship</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label>Tên Voucher *</label>
                    <input type="text" id="tenVoucher" name="ten_voucher" required
                           placeholder="VD: Giảm 20% cho khách hàng mới">
                </div>

                <div class="form-group">
                    <label>Mô Tả</label>
                    <textarea id="moTa" name="mo_ta"
                              placeholder="Mô tả chi tiết về voucher..."></textarea>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Giá Trị Giảm *</label>
                        <input type="number" id="giaTriGiam" name="gia_tri_giam" required
                               placeholder="VD: 20 (cho 20%) hoặc 100000 (cho 100k)" step="0.01">
                    </div>

                    <div class="form-group" id="giamToiDaGroup">
                        <label>Giảm Tối Đa (cho %)</label>
                        <input type="number" id="giamToiDa" name="giam_toi_da"
                               placeholder="VD: 200000" step="0.01">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Đơn Tối Thiểu *</label>
                        <input type="number" id="donToiThieu" name="don_toi_thieu" required
                               placeholder="VD: 500000" step="0.01">
                    </div>

                    <div class="form-group">
                        <label>Áp Dụng Cho *</label>
                        <select id="apDungCho" name="ap_dung_cho" required>
                            <option value="all">Tất cả</option>
                            <option value="new_user">Khách hàng mới</option>
                            <option value="old_user">Khách hàng cũ</option>
                            <option value="first_order">Đơn đầu tiên</option>
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Số Lượng (để trống = không giới hạn)</label>
                        <input type="number" id="soLuong" name="so_luong"
                               placeholder="VD: 100">
                    </div>

                    <div class="form-group">
                        <label>Giới Hạn / User *</label>
                        <input type="number" id="gioiHanMoiUser" name="gioi_han_moi_user" required
                               value="1" min="1">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Ngày Bắt Đầu *</label>
                        <input type="datetime-local" id="ngayBatDau" name="ngay_bat_dau" required>
                    </div>

                    <div class="form-group">
                        <label>Ngày Hết Hạn *</label>
                        <input type="datetime-local" id="ngayHetHan" name="ngay_het_han" required>
                    </div>
                </div>

                <div class="form-group">
                    <label>
                        <input type="checkbox" id="trangThai" name="trang_thai" value="1" checked>
                        Kích hoạt ngay
                    </label>
                </div>

                <div style="margin-top: 20px; text-align: right;">
                    <button type="button" class="btn btn-secondary" onclick="closeModal()">Hủy</button>
                    <button type="submit" class="btn btn-primary">💾 Lưu Voucher</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        // Global variables
        let vouchers = [];

        // Load vouchers on page load
        window.onload = function() {
            loadVouchers();
            loadStats();
        };

        // Load all vouchers
        async function loadVouchers() {
            showLoading(true);

            const status = document.getElementById('filterStatus').value;
            const type = document.getElementById('filterType').value;
            const search = document.getElementById('searchBox').value;

            try {
                const response = await fetch(`getAllVouchers.php?status=${status}&loai_giam=${type}&search=${search}`);
                const data = await response.json();

                if (data.success) {
                    vouchers = data.vouchers;
                    renderTable(vouchers);
                } else {
                    showAlert(data.message, 'error');
                }
            } catch (error) {
                showAlert('Lỗi kết nối: ' + error.message, 'error');
            }

            showLoading(false);
        }

        // Load statistics
        async function loadStats() {
            try {
                const response = await fetch('getVoucherStats.php');
                const data = await response.json();

                if (data.success) {
                    document.getElementById('totalVouchers').textContent = data.summary.total_vouchers;
                    document.getElementById('totalUsage').textContent = data.summary.total_usage;
                    document.getElementById('totalDiscount').textContent =
                        new Intl.NumberFormat('vi-VN').format(data.summary.total_discount_amount) + 'đ';

                    // Count active vouchers
                    const active = vouchers.filter(v => v.voucher_status === 'Hoạt động').length;
                    document.getElementById('activeVouchers').textContent = active;
                }
            } catch (error) {
                console.error('Error loading stats:', error);
            }
        }

        // Render table
        function renderTable(data) {
            const tbody = document.getElementById('voucherTableBody');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="9" style="text-align: center;">Không có dữ liệu</td></tr>';
                return;
            }

            data.forEach(voucher => {
                const row = document.createElement('tr');

                // Badge status
                let statusBadge = '';
                if (voucher.voucher_status === 'Hoạt động') {
                    statusBadge = '<span class="badge badge-success">Hoạt động</span>';
                } else if (voucher.voucher_status === 'Hết hạn') {
                    statusBadge = '<span class="badge badge-danger">Hết hạn</span>';
                } else if (voucher.voucher_status === 'Hết lượt') {
                    statusBadge = '<span class="badge badge-warning">Hết lượt</span>';
                } else {
                    statusBadge = '<span class="badge badge-secondary">Đã tắt</span>';
                }

                // Số lượng
                const soLuongText = voucher.so_luong ?
                    `${voucher.da_su_dung} / ${voucher.so_luong}` :
                    `${voucher.da_su_dung} / ∞`;

                row.innerHTML = `
                    <td>${voucher.id}</td>
                    <td><strong>${voucher.ma_voucher}</strong></td>
                    <td>${voucher.ten_voucher}</td>
                    <td>${voucher.loai_giam}</td>
                    <td>${voucher.text_giam}</td>
                    <td>${soLuongText}</td>
                    <td>${new Date(voucher.ngay_het_han).toLocaleDateString('vi-VN')}</td>
                    <td>${statusBadge}</td>
                    <td>
                        <button class="btn btn-info btn-sm" onclick="editVoucher(${voucher.id})">✏️ Sửa</button>
                        <button class="btn btn-warning btn-sm" onclick="toggleVoucher(${voucher.id}, ${voucher.trang_thai})">
                            ${voucher.trang_thai == 1 ? '🔴 Tắt' : '🟢 Bật'}
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="deleteVoucher(${voucher.id})">🗑️ Xóa</button>
                    </td>
                `;

                tbody.appendChild(row);
            });

            loadStats();
        }

        // Search voucher
        function searchVoucher() {
            loadVouchers();
        }

        // Open add modal
        function openAddModal() {
            document.getElementById('modalTitle').textContent = 'Thêm Voucher Mới';
            document.getElementById('voucherForm').reset();
            document.getElementById('voucherId').value = '';

            // Set default dates
            const now = new Date();
            document.getElementById('ngayBatDau').value = now.toISOString().slice(0, 16);

            const endDate = new Date();
            endDate.setMonth(endDate.getMonth() + 1);
            document.getElementById('ngayHetHan').value = endDate.toISOString().slice(0, 16);

            document.getElementById('voucherModal').style.display = 'block';
        }

        // Edit voucher
        async function editVoucher(id) {
            const voucher = vouchers.find(v => v.id === id);
            if (!voucher) return;

            document.getElementById('modalTitle').textContent = 'Sửa Voucher';
            document.getElementById('voucherId').value = voucher.id;
            document.getElementById('maVoucher').value = voucher.ma_voucher;
            document.getElementById('tenVoucher').value = voucher.ten_voucher;
            document.getElementById('moTa').value = voucher.mo_ta || '';
            document.getElementById('loaiGiam').value = voucher.loai_giam;
            document.getElementById('giaTriGiam').value = voucher.gia_tri_giam;
            document.getElementById('giamToiDa').value = voucher.giam_toi_da || '';
            document.getElementById('donToiThieu').value = voucher.don_toi_thieu;
            document.getElementById('apDungCho').value = voucher.ap_dung_cho;
            document.getElementById('soLuong').value = voucher.so_luong || '';
            document.getElementById('gioiHanMoiUser').value = voucher.gioi_han_moi_user;
            document.getElementById('ngayBatDau').value = voucher.ngay_bat_dau.replace(' ', 'T');
            document.getElementById('ngayHetHan').value = voucher.ngay_het_han.replace(' ', 'T');
            document.getElementById('trangThai').checked = voucher.trang_thai == 1;

            toggleGiamToiDa();
            document.getElementById('voucherModal').style.display = 'block';
        }

        // Close modal
        function closeModal() {
            document.getElementById('voucherModal').style.display = 'none';
        }

        // Toggle "Giảm tối đa" field
        function toggleGiamToiDa() {
            const loaiGiam = document.getElementById('loaiGiam').value;
            const giamToiDaGroup = document.getElementById('giamToiDaGroup');

            if (loaiGiam === 'percent') {
                giamToiDaGroup.style.display = 'block';
            } else {
                giamToiDaGroup.style.display = 'none';
                document.getElementById('giamToiDa').value = '';
            }
        }

        // Save voucher (Add or Update)
        async function saveVoucher(event) {
            event.preventDefault();

            const formData = new FormData(document.getElementById('voucherForm'));
            formData.set('trang_thai', document.getElementById('trangThai').checked ? '1' : '0');

            // Convert datetime-local to MySQL format
            const ngayBatDau = document.getElementById('ngayBatDau').value.replace('T', ' ') + ':00';
            const ngayHetHan = document.getElementById('ngayHetHan').value.replace('T', ' ') + ':00';
            formData.set('ngay_bat_dau', ngayBatDau);
            formData.set('ngay_het_han', ngayHetHan);

            const isEdit = document.getElementById('voucherId').value !== '';
            const url = isEdit ? 'updateVoucher.php' : 'addVoucher.php';

            showLoading(true);

            try {
                const response = await fetch(url, {
                    method: 'POST',
                    body: formData
                });

                const data = await response.json();

                if (data.success) {
                    showAlert(data.message, 'success');
                    closeModal();
                    loadVouchers();
                } else {
                    showAlert(data.message, 'error');
                }
            } catch (error) {
                showAlert('Lỗi: ' + error.message, 'error');
            }

            showLoading(false);
        }

        // Toggle voucher status
        async function toggleVoucher(id, currentStatus) {
            const action = currentStatus == 1 ? 'tắt' : 'bật';
            if (!confirm(`Bạn có chắc muốn ${action} voucher này?`)) return;

            const formData = new FormData();
            formData.append('id', id);

            try {
                const response = await fetch('toggleVoucher.php', {
                    method: 'POST',
                    body: formData
                });

                const data = await response.json();

                if (data.success) {
                    showAlert(data.message, 'success');
                    loadVouchers();
                } else {
                    showAlert(data.message, 'error');
                }
            } catch (error) {
                showAlert('Lỗi: ' + error.message, 'error');
            }
        }

        // Delete voucher
        async function deleteVoucher(id) {
            if (!confirm('Bạn có chắc muốn xóa voucher này?\n(Sẽ chuyển sang trạng thái "Đã tắt")')) return;

            const formData = new FormData();
            formData.append('id', id);
            formData.append('hard_delete', 'false');

            try {
                const response = await fetch('deleteVoucher.php', {
                    method: 'POST',
                    body: formData
                });

                const data = await response.json();

                if (data.success) {
                    showAlert(data.message, 'success');
                    loadVouchers();
                } else {
                    showAlert(data.message, 'error');
                }
            } catch (error) {
                showAlert('Lỗi: ' + error.message, 'error');
            }
        }

        // Show alert
        function showAlert(message, type) {
            const alertId = type === 'success' ? 'alertSuccess' : 'alertError';
            const alertEl = document.getElementById(alertId);

            alertEl.textContent = message;
            alertEl.style.display = 'block';

            setTimeout(() => {
                alertEl.style.display = 'none';
            }, 5000);
        }

        // Show/hide loading
        function showLoading(show) {
            document.getElementById('loading').style.display = show ? 'block' : 'none';
        }

        // Close modal when clicking outside
        window.onclick = function(event) {
            const modal = document.getElementById('voucherModal');
            if (event.target === modal) {
                closeModal();
            }
        }
    </script>
</body>
</html>

