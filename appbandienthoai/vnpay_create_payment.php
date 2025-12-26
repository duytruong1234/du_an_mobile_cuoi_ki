<?php
/**
 * vnpay_create_payment.php
 * API tạo đơn hàng và trả về link thanh toán VNPay (Sandbox)
 * Yêu cầu: Server có `connect.php` (kết nối DB) và `vnpay_config.php` (config constants)
 *
 * Request: POST
 * - iduser, diachi, sodienthoai, tongtien, soluong, cartItems (JSON), ngaygiaodukien (opt), order_info (opt)
 *
 * Response: JSON
 * - success (bool), message (string), payment_url (string), madonhang (string), iddonhang (int)
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';
require_once __DIR__ . '/vnpay_config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Phương thức không hợp lệ']);
    exit;
}

// --- Nhận dữ liệu ---
$iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;
$diachi = isset($_POST['diachi']) ? $conn->real_escape_string(trim($_POST['diachi'])) : '';
$sodienthoai = isset($_POST['sodienthoai']) ? $conn->real_escape_string(trim($_POST['sodienthoai'])) : '';
$soluong = isset($_POST['soluong']) ? intval($_POST['soluong']) : 0;
$tongtien = isset($_POST['tongtien']) ? floatval($_POST['tongtien']) : 0;
$ngaygiaodukien = isset($_POST['ngaygiaodukien']) ? $conn->real_escape_string(trim($_POST['ngaygiaodukien'])) : '';
$cartItems = isset($_POST['cartItems']) ? json_decode($_POST['cartItems'], true) : [];
$order_info = isset($_POST['order_info']) ? $conn->real_escape_string(trim($_POST['order_info'])) : 'Thanh toan don hang';

// Thông tin voucher (nếu có)
$voucher_id = isset($_POST['voucher_id']) ? intval($_POST['voucher_id']) : null;
$ma_voucher = isset($_POST['ma_voucher']) ? $conn->real_escape_string(trim($_POST['ma_voucher'])) : null;
$gia_tri_giam = isset($_POST['gia_tri_giam']) ? floatval($_POST['gia_tri_giam']) : 0;
$tong_truoc_giam = isset($_POST['tong_truoc_giam']) ? floatval($_POST['tong_truoc_giam']) : 0;

if ($iduser <= 0 || empty($diachi) || empty($sodienthoai) || empty($cartItems) || $tongtien <= 0) {
    echo json_encode(['success' => false, 'message' => 'Thiếu thông tin bắt buộc hoặc tổng tiền không hợp lệ']);
    exit;
}

// Tạo mã đơn hàng
$madonhang = 'DH' . date('YmdHis') . rand(100,999);

// Bắt đầu transaction DB
mysqli_begin_transaction($conn);
try {
    // Kiểm tra tồn kho
    foreach ($cartItems as $item) {
        $idsp = intval($item['idsp']);
        $sl = intval($item['soluong']);
        $res = mysqli_query($conn, "SELECT tensp, soluongtonkho FROM sanphammoi WHERE id = $idsp");
        if (!$res || mysqli_num_rows($res) === 0) {
            throw new Exception("Sản phẩm ID $idsp không tồn tại");
        }
        $row = mysqli_fetch_assoc($res);
        if ($row['soluongtonkho'] < $sl) {
            throw new Exception("Sản phẩm '{$row['tensp']}' chỉ còn {$row['soluongtonkho']} trong kho");
        }
    }

    // Thêm donhang (chưa giảm tồn kho) - trạng thái chờ thanh toán VNPay
    if ($voucher_id > 0) {
        $sqlInsert = "INSERT INTO donhang (madonhang, iduser, diachi, sodienthoai, soluong, tongtien, ngaydat, ngaygiaodukien, trangthai, voucher_id, ma_voucher, gia_tri_giam, tong_truoc_giam)
            VALUES ('{$madonhang}', {$iduser}, '{$diachi}', '{$sodienthoai}', {$soluong}, '{$tongtien}', NOW(), '{$ngaygiaodukien}', 'Chờ thanh toán VNPay', {$voucher_id}, '{$ma_voucher}', {$gia_tri_giam}, {$tong_truoc_giam})";
    } else {
        $sqlInsert = "INSERT INTO donhang (madonhang, iduser, diachi, sodienthoai, soluong, tongtien, ngaydat, ngaygiaodukien, trangthai)
            VALUES ('{$madonhang}', {$iduser}, '{$diachi}', '{$sodienthoai}', {$soluong}, '{$tongtien}', NOW(), '{$ngaygiaodukien}', 'Chờ thanh toán VNPay')";
    }

    if (!mysqli_query($conn, $sqlInsert)) {
        throw new Exception('Lỗi tạo đơn hàng: ' . mysqli_error($conn));
    }
    $iddonhang = mysqli_insert_id($conn);

    // Thêm chi tiết đơn hàng (không giảm tồn kho ở đây - sẽ giảm khi VNPay xác nhận)
    foreach ($cartItems as $item) {
        $idsp = intval($item['idsp']);
        $sl = intval($item['soluong']);
        $gia = isset($item['giasp']) ? floatval($item['giasp']) : 0;
        $sqlDetail = "INSERT INTO chitietdonhang (iddonhang, idsp, soluong, gia) VALUES ({$iddonhang}, {$idsp}, {$sl}, '{$gia}')";
        if (!mysqli_query($conn, $sqlDetail)) {
            throw new Exception('Lỗi thêm chi tiết đơn hàng: ' . mysqli_error($conn));
        }
    }

    // Nếu có sử dụng voucher, lưu lịch sử sử dụng
    if ($voucher_id > 0) {
        // 1. Lưu vào bảng voucher_usage
        $sqlVoucherUsage = "INSERT INTO voucher_usage (voucher_id, user_id, donhang_id, ma_donhang, gia_tri_don_hang, gia_tri_giam, ngay_su_dung)
                           VALUES ({$voucher_id}, {$iduser}, {$iddonhang}, '{$madonhang}', {$tong_truoc_giam}, {$gia_tri_giam}, NOW())";

        if (!mysqli_query($conn, $sqlVoucherUsage)) {
            throw new Exception('Lỗi lưu lịch sử voucher: ' . mysqli_error($conn));
        }

        // Lưu ý: KHÔNG CẦN UPDATE voucher.da_su_dung ở đây
        // Vì đã có TRIGGER 'after_donhang_insert_update_voucher' tự động tăng da_su_dung
        // khi INSERT vào bảng donhang
    }

    mysqli_commit($conn);

    // --- Tạo URL thanh toán VNPay ---
    $vnp_TxnRef = $madonhang;
    $vnp_OrderInfo = $order_info . ' - ' . $madonhang;
    $vnp_OrderType = 'billpayment';
    $vnp_Amount = $tongtien * 100; // nhân 100 theo quy ước VNPay
    $vnp_Locale = 'vn';
    $vnp_IpAddr = $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';

    $inputData = [
        'vnp_Version' => '2.1.0',
        'vnp_TmnCode' => VNPAY_TMN_CODE,
        'vnp_Amount' => (string)$vnp_Amount,
        'vnp_Command' => 'pay',
        'vnp_CreateDate' => date('YmdHis'),
        'vnp_CurrCode' => 'VND',
        'vnp_IpAddr' => $vnp_IpAddr,
        'vnp_Locale' => $vnp_Locale,
        'vnp_OrderInfo' => $vnp_OrderInfo,
        'vnp_OrderType' => $vnp_OrderType,
        'vnp_ReturnUrl' => VNPAY_RETURN_URL,
        'vnp_TxnRef' => $vnp_TxnRef,
        'vnp_BankCode' => 'NCB', // ✅ Thêm mã ngân hàng NCB (Ngân hàng Quốc Dân) để test
    ];

    ksort($inputData);
    $query = '';
    $hashdata = '';
    $i = 0;
    foreach ($inputData as $key => $value) {
        $encodedKey = urlencode($key);
        $encodedValue = urlencode($value);
        if ($i === 0) {
            $hashdata .= $encodedKey . '=' . $encodedValue;
        } else {
            $hashdata .= '&' . $encodedKey . '=' . $encodedValue;
        }
        $i++;
        $query .= $encodedKey . '=' . $encodedValue . '&';
    }

    // ✅ LOGGING DEBUG - Ghi ra file để kiểm tra
    $debugInfo = [
        'timestamp' => date('Y-m-d H:i:s'),
        'madonhang' => $madonhang,
        'VNPAY_TMN_CODE' => VNPAY_TMN_CODE,
        'VNPAY_HASH_SECRET' => substr(VNPAY_HASH_SECRET, 0, 10) . '...', // Chỉ hiện 10 ký tự đầu
        'vnp_Amount' => $inputData['vnp_Amount'],
        'hashdata_raw' => $hashdata,
        'inputData' => $inputData
    ];
    file_put_contents(__DIR__ . '/vnpay_debug_create.txt', json_encode($debugInfo, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) . "\n\n", FILE_APPEND);

    $vnp_Url = VNPAY_URL . '?' . $query;
    $vnpSecureHash = hash_hmac('sha512', $hashdata, VNPAY_HASH_SECRET);
    $vnp_Url .= 'vnp_SecureHash=' . $vnpSecureHash;

    // Log URL cuối cùng (chỉ 200 ký tự đầu để dễ đọc)
    file_put_contents(__DIR__ . '/vnpay_debug_create.txt', "Payment URL (first 200 chars): " . substr($vnp_Url, 0, 200) . "...\n" . str_repeat('-', 80) . "\n\n", FILE_APPEND);

    // Trả về link
    echo json_encode([
        'success' => true,
        'message' => 'Tạo đơn hàng và link thanh toán thành công',
        'payment_url' => $vnp_Url,
        'madonhang' => $madonhang,
        'iddonhang' => $iddonhang
    ]);

} catch (Exception $e) {
    mysqli_rollback($conn);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}

mysqli_close($conn);
?>
