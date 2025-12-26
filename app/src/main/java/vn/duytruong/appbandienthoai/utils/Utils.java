package vn.duytruong.appbandienthoai.utils;

import java.util.ArrayList;
import java.util.List;

import vn.duytruong.appbandienthoai.model.GioHang;
import vn.duytruong.appbandienthoai.model.User;

public class Utils {
    // Dùng cho emulator (10.0.2.2 = localhost của máy host)
    // Nếu chạy trên thiết bị thật, đổi thành IP máy tính (VD: "http://192.168.1.5/appbandienthoai/")
    public static final String BASE_URL = "http://10.0.2.2/appbandienthoai/";

    // API URLs
    public static final String URL_TAO_DON_HANG = BASE_URL + "taoDonHang.php";
    public static final String URL_KIEM_TRA_TON_KHO = BASE_URL + "kiemTraTonKho.php";
    public static final String URL_GET_DON_HANG = BASE_URL + "getDonHang.php";
    public static final String URL_GET_CHI_TIET_DON_HANG = BASE_URL + "getChiTietDonHang.php";
    public static final String URL_CAP_NHAT_TRANG_THAI = BASE_URL + "capNhatTrangThai.php";
    public static final String URL_HUY_DON_HANG = BASE_URL + "huyDonHang.php";
    public static final String URL_GET_TON_KHO = BASE_URL + "getTonKho.php";
    public static final String URL_SET_TON_KHO = BASE_URL + "setTonKho.php";
    public static final String URL_CAP_NHAT_TON_KHO = BASE_URL + "capNhatTonKho.php";

    // VNPay API URLs - ✅ ĐÃ SỬA: Thêm /Server/ vào đường dẫn
    public static final String URL_VNPAY_CREATE_PAYMENT = BASE_URL + "Server/vnpay_create_payment.php";
    public static final String URL_VNPAY_CONTINUE_PAYMENT = BASE_URL + "Server/vnpay_continue_payment.php";
    public static final String URL_VNPAY_CHECK_STATUS = BASE_URL + "Server/vnpay_check_status.php";

    // Giỏ hàng API URLs
    public static final String URL_THEM_GIO_HANG = BASE_URL + "themGioHang.php";
    public static final String URL_GET_GIO_HANG = BASE_URL + "getGioHang.php";
    public static final String URL_XOA_GIO_HANG = BASE_URL + "xoaGioHang.php";
    public static final String URL_CAP_NHAT_GIO_HANG = BASE_URL + "capNhatGioHang.php";
    public static final String URL_XOA_TOAN_BO_GIO_HANG = BASE_URL + "xoaToanBoGioHang.php";

    // Shared labels for order statuses (index 0 -> first label). Keep in sync with server-side conventions.
    public static final String[] STATUS_LABELS = new String[]{"Đang xử lý", "Đã chuẩn bị", "Đang giao", "Thành công", "Đã hủy"};

    // Broadcast action and extras for order status updates
    public static final String ACTION_ORDER_STATUS_UPDATED = "vn.duytruong.appbandienthoai.ACTION_ORDER_STATUS_UPDATED";
    public static final String EXTRA_MA_DONHANG = "madonhang";
    public static final String EXTRA_TRANGTHAI_INDEX = "trangthai_index"; // int index when applicable
    public static final String EXTRA_TRANGTHAI_TEXT = "trangthai_text";   // human readable text

    public static List<GioHang> manggiohang;
    public static List<GioHang> mangmuahang = new ArrayList<>();
    public static User user_current;

    // Convert server 'trangthai' value (may be numeric like "1".."5" or a human-readable string)
    // into the canonical human-readable label from STATUS_LABELS whenever possible.
    public static String formatTrangThai(String trangthai) {
        if (trangthai == null) return "";
        String t = trangthai.trim();
        if (t.isEmpty()) return "";
        try {
            int v = Integer.parseInt(t);
            if (v >= 1 && v <= STATUS_LABELS.length) return STATUS_LABELS[v - 1];
        } catch (Exception ignored) {}

        String lower = t.toLowerCase();

        // Chuẩn hóa: Loại bỏ các suffix phương thức thanh toán như "PayPal", "VNPay"
        // VD: "Đã thanh toán PayPal" -> "Đã thanh toán"
        if (lower.contains("đã thanh toán") || lower.contains("da thanh toan")) {
            return "Đã thanh toán";
        }

        if (lower.contains("hủy") || lower.contains("huy")) return STATUS_LABELS[4];
        if (lower.contains("thành công") || lower.contains("thanh cong")) return STATUS_LABELS[3];
        if (lower.contains("giao")) return STATUS_LABELS[2];
        if (lower.contains("chuẩn bị") || lower.contains("chuan bi")) return STATUS_LABELS[1];

        return t;
    }
}
