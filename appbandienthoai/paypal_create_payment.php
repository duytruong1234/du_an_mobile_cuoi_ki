<?php
ob_start(); // Bắt đầu output buffering

include "connect.php";
include "paypal_config.php";

ob_end_clean(); // Xóa mọi output không mong muốn
header('Content-Type: application/json; charset=utf-8');

// Lấy dữ liệu từ POST
$iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;
$diachi = isset($_POST['diachi']) ? $_POST['diachi'] : '';
$sodienthoai = isset($_POST['sodienthoai']) ? $_POST['sodienthoai'] : '';
$soluong = isset($_POST['soluong']) ? intval($_POST['soluong']) : 0;
$tongtien = isset($_POST['tongtien']) ? $_POST['tongtien'] : '0';
$cartItems = isset($_POST['cartItems']) ? $_POST['cartItems'] : '[]';
$ngaygiaodukien = isset($_POST['ngaygiaodukien']) ? $_POST['ngaygiaodukien'] : '';
$order_info = isset($_POST['order_info']) ? $_POST['order_info'] : 'Thanh toan don hang';

// Thông tin voucher (nếu có)
$voucher_id = isset($_POST['voucher_id']) ? intval($_POST['voucher_id']) : null;
$ma_voucher = isset($_POST['ma_voucher']) ? $conn->real_escape_string(trim($_POST['ma_voucher'])) : null;
$gia_tri_giam = isset($_POST['gia_tri_giam']) ? floatval($_POST['gia_tri_giam']) : 0;
$tong_truoc_giam = isset($_POST['tong_truoc_giam']) ? floatval($_POST['tong_truoc_giam']) : 0;

// Validate dữ liệu
if ($iduser <= 0 || empty($diachi) || empty($sodienthoai) || $soluong <= 0 || floatval($tongtien) <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Thông tin đơn hàng không hợp lệ'
    ]);
    exit;
}

try {
    // Tạo mã đơn hàng unique
    $madonhang = 'DH' . time() . rand(1000, 9999);

    // Chuyển đổi VND sang USD
    $tongtien_vnd = floatval($tongtien);
    $tongtien_usd = round($tongtien_vnd / VND_TO_USD_RATE, 2);

    // Tạo đơn hàng trong database với thông tin voucher
    if ($voucher_id > 0) {
        $sql = "INSERT INTO donhang (madonhang, iduser, diachi, sodienthoai, soluong, tongtien, ngaydat, ngaygiaodukien, trangthai, thoigian, voucher_id, ma_voucher, gia_tri_giam, tong_truoc_giam)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, 'Chờ thanh toán PayPal', NOW(), ?, ?, ?, ?)";

        $stmt = $conn->prepare($sql);

        if ($stmt === false) {
            $error = $conn->error;
            file_put_contents(__DIR__ . '/paypal_error.log',
                date('Y-m-d H:i:s') . " - SQL Error: " . $error . "\n" .
                "SQL: " . $sql . "\n\n",
                FILE_APPEND
            );
            throw new Exception("Loi database: " . $error);
        }

        // Bind params với voucher: madonhang(s), iduser(i), diachi(s), sodienthoai(s), soluong(i), tongtien(d), ngaygiaodukien(s), voucher_id(i), ma_voucher(s), gia_tri_giam(d), tong_truoc_giam(d)
        $stmt->bind_param("sissiisisdd", $madonhang, $iduser, $diachi, $sodienthoai, $soluong, $tongtien_vnd, $ngaygiaodukien, $voucher_id, $ma_voucher, $gia_tri_giam, $tong_truoc_giam);
    } else {
        $sql = "INSERT INTO donhang (madonhang, iduser, diachi, sodienthoai, soluong, tongtien, ngaydat, ngaygiaodukien, trangthai, thoigian)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, 'Chờ thanh toán PayPal', NOW())";

        $stmt = $conn->prepare($sql);

        if ($stmt === false) {
            $error = $conn->error;
            file_put_contents(__DIR__ . '/paypal_error.log',
                date('Y-m-d H:i:s') . " - SQL Error: " . $error . "\n" .
                "SQL: " . $sql . "\n\n",
                FILE_APPEND
            );
            throw new Exception("Loi database: " . $error);
        }

        // Bind params: madonhang(s), iduser(i), diachi(s), sodienthoai(s), soluong(i), tongtien(d), ngaygiaodukien(s)
        $stmt->bind_param("sissids", $madonhang, $iduser, $diachi, $sodienthoai, $soluong, $tongtien_vnd, $ngaygiaodukien);
    }

    if (!$stmt->execute()) {
        throw new Exception("Khong the tao don hang");
    }

    $iddonhang = $conn->insert_id;
    $stmt->close();

    // ✅ THÊM CHI TIẾT ĐƠN HÀNG - Lưu thông tin sản phẩm
    $cartItemsArray = json_decode($cartItems, true);
    if (!empty($cartItemsArray)) {
        foreach ($cartItemsArray as $item) {
            $idsp = intval($item['idsp']);
            $sl = intval($item['soluong']);
            $gia = floatval($item['giasp']);

            $sqlDetail = "INSERT INTO chitietdonhang (iddonhang, idsp, soluong, gia) VALUES (?, ?, ?, ?)";
            $stmtDetail = $conn->prepare($sqlDetail);
            $stmtDetail->bind_param("iiid", $iddonhang, $idsp, $sl, $gia);

            if (!$stmtDetail->execute()) {
                throw new Exception("Loi them chi tiet don hang: " . $stmtDetail->error);
            }
            $stmtDetail->close();
        }
    }

    // Nếu có sử dụng voucher, lưu lịch sử sử dụng
    if ($voucher_id > 0) {
        // 1. Lưu vào bảng voucher_usage
        $sqlVoucherUsage = "INSERT INTO voucher_usage (voucher_id, user_id, donhang_id, ma_donhang, gia_tri_don_hang, gia_tri_giam, ngay_su_dung)
                           VALUES (?, ?, ?, ?, ?, ?, NOW())";
        $stmtVoucher = $conn->prepare($sqlVoucherUsage);
        $stmtVoucher->bind_param("iiisdd", $voucher_id, $iduser, $iddonhang, $madonhang, $tong_truoc_giam, $gia_tri_giam);

        if (!$stmtVoucher->execute()) {
            throw new Exception('Loi luu lich su voucher: ' . $stmtVoucher->error);
        }
        $stmtVoucher->close();

        // Lưu ý: KHÔNG CẦN UPDATE voucher.da_su_dung ở đây
        // Vì đã có TRIGGER 'after_donhang_insert_update_voucher' tự động tăng da_su_dung
        // khi INSERT vào bảng donhang
    }

    // Lấy access token từ PayPal
    $accessToken = getPayPalAccessToken();

    if (!$accessToken) {
        throw new Exception("Khong the ket noi voi PayPal");
    }

    // Tạo payment order với PayPal
    $orderData = [
        'intent' => 'CAPTURE',
        'purchase_units' => [
            [
                'reference_id' => $madonhang,
                'description' => $order_info,
                'amount' => [
                    'currency_code' => PAYPAL_CURRENCY,
                    'value' => strval($tongtien_usd)
                ]
            ]
        ],
        'application_context' => [
            'return_url' => PAYPAL_RETURN_URL . '?madonhang=' . $madonhang,
            'cancel_url' => PAYPAL_CANCEL_URL . '?madonhang=' . $madonhang,
            'brand_name' => 'Shop Dong Ho',
            'landing_page' => 'BILLING',
            'user_action' => 'PAY_NOW'
        ]
    ];

    $ch = curl_init(PAYPAL_API_URL . '/v2/checkout/orders');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($orderData));
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Authorization: Bearer ' . $accessToken
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode != 201) {
        throw new Exception("Loi tao PayPal order");
    }

    $paypalOrder = json_decode($response, true);

    // Lấy approval URL
    $approvalUrl = '';
    foreach ($paypalOrder['links'] as $link) {
        if ($link['rel'] == 'approve') {
            $approvalUrl = $link['href'];
            break;
        }
    }

    // Lưu PayPal order ID vào database
    $paypalOrderId = $paypalOrder['id'];
    $stmt = $conn->prepare("UPDATE donhang SET paypal_order_id = ? WHERE madonhang = ?");
    $stmt->bind_param("ss", $paypalOrderId, $madonhang);
    $stmt->execute();
    $stmt->close();

    echo json_encode([
        'success' => true,
        'message' => 'Tao don hang PayPal thanh cong',
        'order_id' => $paypalOrderId,
        'approval_url' => $approvalUrl,
        'madonhang' => $madonhang,
        'iddonhang' => $iddonhang
    ]);

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Loi: ' . str_replace(['"', "'", "\n", "\r"], '', $e->getMessage())
    ]);
}

function getPayPalAccessToken() {
    $ch = curl_init(PAYPAL_API_URL . '/v1/oauth2/token');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, 'grant_type=client_credentials');
    curl_setopt($ch, CURLOPT_USERPWD, PAYPAL_CLIENT_ID . ':' . PAYPAL_CLIENT_SECRET);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Accept: application/json',
        'Accept-Language: en_US'
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode != 200) {
        return false;
    }

    $data = json_decode($response, true);
    return isset($data['access_token']) ? $data['access_token'] : false;
}
?>
