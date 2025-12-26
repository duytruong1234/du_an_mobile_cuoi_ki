package vn.duytruong.appbandienthoai.activity;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.GioHang;
import vn.duytruong.appbandienthoai.model.VNPayResponse;
import vn.duytruong.appbandienthoai.model.PayPalResponse;
import vn.duytruong.appbandienthoai.model.VoucherCheckResponse;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class DatHangActivity extends AppCompatActivity {
    private static final String TAG = "DatHangActivity";

    // UI Components
    private Toolbar toolbar;
    private EditText edtDiachi, edtSodienthoai, edtNgayGiao, edtVoucher;
    private Button btnDatHang, btnVNPay, btnPayPal, btnApDungVoucher, btnHuyVoucher, btnChonVoucher;
    private TextView tvTongTien, tvSoLuong;
    private TextView tvVoucherName, tvVoucherDesc, tvTongTruocGiam, tvGiamGia, tvTongSauGiam;
    private View layoutVoucherResult;

    // Data
    private RequestQueue requestQueue;
    private long tongtien = 0;
    private long tongtienGoc = 0;
    private long giatrigiam = 0;
    private int tongSoLuong = 0;
    private Calendar calendar;
    private ApiBanHang apiBanHang;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Voucher info
    private int voucherId = 0;
    private String maVoucher = "";
    private String tenVoucher = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat_hang);

        initView();
        initControl();

        // Nếu activity được gọi kèm địa chỉ từ màn hình trước
        String diachiTruoc = getIntent().getStringExtra("diachi");
        if (diachiTruoc != null && !diachiTruoc.isEmpty()) {
            edtDiachi.setText(diachiTruoc);
        }

        if (Utils.user_current != null && Utils.user_current.getMobile() != null) {
            edtSodienthoai.setText(Utils.user_current.getMobile());
        }


        tinhTongTien();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        edtDiachi = findViewById(R.id.edtDiachi);
        edtSodienthoai = findViewById(R.id.edtSodienthoai);
        edtNgayGiao = findViewById(R.id.edtNgayGiao);
        edtVoucher = findViewById(R.id.edtVoucher);
        btnDatHang = findViewById(R.id.btnDatHang);
        btnVNPay = findViewById(R.id.btnVNPay);
        btnPayPal = findViewById(R.id.btnPayPal);
        btnApDungVoucher = findViewById(R.id.btnApDungVoucher);
        btnHuyVoucher = findViewById(R.id.btnHuyVoucher);
        btnChonVoucher = findViewById(R.id.btnChonVoucher);
        tvTongTien = findViewById(R.id.tvTongTien);
        tvSoLuong = findViewById(R.id.tvSoLuong);

        // Voucher result views
        layoutVoucherResult = findViewById(R.id.layoutVoucherResult);
        tvVoucherName = findViewById(R.id.tvVoucherName);
        tvVoucherDesc = findViewById(R.id.tvVoucherDesc);
        tvTongTruocGiam = findViewById(R.id.tvTongTruocGiam);
        tvGiamGia = findViewById(R.id.tvGiamGia);
        tvTongSauGiam = findViewById(R.id.tvTongSauGiam);

        requestQueue = Volley.newRequestQueue(this);
        calendar = Calendar.getInstance();
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);

        Log.d("PayPal-DEBUG", " DatHangActivity - btnPayPal initialized: " + (btnPayPal != null));
    }

    private void initControl() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Default delivery date = today + 3 days
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        updateNgayGiao();

        // Open date picker when clicking the date field
        edtNgayGiao.setOnClickListener(v -> showDatePicker());

        // Place order when button clicked
        btnDatHang.setOnClickListener(v -> datHang());
        // 🎫 Chọn voucher từ danh sách
        if (btnChonVoucher != null) {
            btnChonVoucher.setOnClickListener(v -> {
                Toast.makeText(this, "Đang mở danh sách voucher...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DatHangActivity.this, ChonVoucherActivity.class);
                intent.putExtra("tong_tien", (double) tongtien);
                intent.putExtra("user_id", Utils.user_current != null ? Utils.user_current.getId() : 0);
                startActivityForResult(intent, 999);
            });
        }


        // Áp dụng voucher
        if (btnApDungVoucher != null) {
            btnApDungVoucher.setOnClickListener(v -> apDungVoucher());
        }
        // Hủy voucher
        if (btnHuyVoucher != null) {
            btnHuyVoucher.setOnClickListener(v -> huyVoucher());
        }


        // Thanh toán VNPay ngay tại đây (không chuyển Activity)
        if (btnVNPay != null) {
            btnVNPay.setVisibility(View.VISIBLE);
            btnVNPay.setOnClickListener(v -> thanhToanVNPay());
        }

        // Thanh toán PayPal
        if (btnPayPal != null) {
            btnPayPal.setVisibility(View.VISIBLE);
            btnPayPal.setOnClickListener(v -> {
                Log.e("PayPal", "===>  NÚT PAYPAL ĐƯỢC BẤM (DatHangActivity) <===");
                Toast.makeText(this, "Đang xử lý PayPal...", Toast.LENGTH_SHORT).show();
                thanhToanPayPal();
            });
            Log.d("PayPal-DEBUG", " PayPal listener đã được setup trong DatHangActivity");
        } else {
            Log.e("PayPal-DEBUG", "btnPayPal is NULL trong DatHangActivity!");
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateNgayGiao();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Disable past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateNgayGiao() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        edtNgayGiao.setText(sdf.format(calendar.getTime()));
    }

    private void apDungVoucher() {
        String maVoucherInput = edtVoucher.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(maVoucherInput)) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tongtienGoc == 0) {
            tongtienGoc = tongtien;
        }

        Log.d(TAG, "Kiểm tra voucher: " + maVoucherInput);
        Log.d(TAG, "User ID: " + (Utils.user_current != null ? Utils.user_current.getId() : "null"));
        Log.d(TAG, "Tổng tiền: " + tongtienGoc);

        // Gọi API kiểm tra voucher
        compositeDisposable.add(apiBanHang.checkVoucher(
                maVoucherInput,
                Utils.user_current != null ? Utils.user_current.getId() : 0,
                tongtienGoc
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                response -> {
                    Log.d(TAG, "Voucher response: " + new Gson().toJson(response));

                    if (response != null && response.isSuccess()) {
                        // Lưu thông tin voucher
                        if (response.getVoucher() != null) {
                            voucherId = response.getVoucher().getId();
                            maVoucher = response.getVoucher().getMa_voucher();
                            tenVoucher = response.getVoucher().getTen_voucher();
                        }

                        if (response.getTinh_toan() != null) {
                            giatrigiam = (long) response.getTinh_toan().getGia_tri_giam();
                            tongtien = (long) response.getTinh_toan().getTong_sau_giam();
                        }

                        // Hiển thị kết quả
                        hienThiKetQuaVoucher(response);

                        Toast.makeText(this, "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "Lỗi không xác định";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Voucher error: " + errorMsg);
                    }
                },
                throwable -> {
                    Log.e(TAG, "Lỗi gọi API voucher: " + throwable.getMessage());
                    Toast.makeText(this, "Lỗi kết nối: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
        ));
    }

    private void hienThiKetQuaVoucher(VoucherCheckResponse response) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        // Hiển thị thông tin voucher
        if (response.getVoucher() != null) {
            tvVoucherName.setText(String.format("Mã: %s", response.getVoucher().getMa_voucher()));
            tvVoucherDesc.setText(response.getVoucher().getTen_voucher());
        }

        if (response.getTinh_toan() != null) {
            tvTongTruocGiam.setText(String.format("%s ₫", decimalFormat.format(response.getTinh_toan().getTong_truoc_giam())));
            tvGiamGia.setText(String.format("-%s ₫", decimalFormat.format(response.getTinh_toan().getGia_tri_giam())));
            tvTongSauGiam.setText(String.format("%s ₫", decimalFormat.format(response.getTinh_toan().getTong_sau_giam())));
        }

        // Hiển thị layout kết quả
        layoutVoucherResult.setVisibility(View.VISIBLE);

        // Cập nhật lại tổng tiền hiển thị
        capNhatHienThiTongTien();
    }
    private void huyVoucher() {
        // Reset lại tất cả thông tin voucher
        voucherId = 0;
        maVoucher = "";
        tenVoucher = "";
        giatrigiam = 0;
        tongtien = tongtienGoc; // Khôi phục về tổng tiền gốc

        // Ẩn khu vực hiển thị kết quả voucher
        layoutVoucherResult.setVisibility(View.GONE);

        // Xóa mã đã nhập
        edtVoucher.setText("");

        // Cập nhật lại tổng tiền
        capNhatHienThiTongTien();

        Toast.makeText(this, "Đã hủy mã giảm giá", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Voucher đã được hủy, tổng tiền khôi phục: " + tongtien);
    }


    private void capNhatHienThiTongTien() {
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        tvTongTien.setText(String.format(java.util.Locale.getDefault(), "Tổng tiền: %s ₫", decimalFormat.format(tongtien)));
    }

    private void datHang() {
        String diachi = edtDiachi.getText().toString().trim();
        String sodienthoai = edtSodienthoai.getText().toString().trim();
        String ngaygiao = edtNgayGiao.getText().toString().trim();

        if (TextUtils.isEmpty(diachi)) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(sodienthoai)) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sodienthoai.length() < 10) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.mangmuahang == null || Utils.mangmuahang.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String url = Utils.URL_TAO_DON_HANG;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            String madonhang = jsonObject.optString("madonhang", "");
                            Toast.makeText(DatHangActivity.this,
                                    "Đặt hàng thành công! Mã đơn hàng: " + madonhang,
                                    Toast.LENGTH_LONG).show();

                            //  Xóa các sản phẩm đã mua khỏi giỏ hàng
                            xoaSanPhamDaMuaKhoiGioHang();

                            // Clear danh sách mua hàng
                            Utils.mangmuahang.clear();

                            // Chuyển đến trang Kho hàng (Xem đơn hàng)
                            Intent intent = new Intent(DatHangActivity.this, XemDonActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DatHangActivity.this,
                                    jsonObject.optString("message", "Có lỗi xảy ra"),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        Toast.makeText(DatHangActivity.this,
                                "Lỗi xử lý dữ liệu",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error", error);
                    Toast.makeText(DatHangActivity.this,
                            "Lỗi kết nối: " + (error.getMessage() != null ? error.getMessage() : ""),
                            Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("iduser", Utils.user_current != null ? String.valueOf(Utils.user_current.getId()) : "0");
                params.put("diachi", diachi);
                params.put("sodienthoai", sodienthoai);
                params.put("soluong", String.valueOf(tongSoLuong));
                params.put("tongtien", String.valueOf(tongtien));
                params.put("ngaygiaodukien", ngaygiao);
                params.put("cartItems", new Gson().toJson(Utils.mangmuahang));

                // Thêm thông tin voucher nếu có
                if (voucherId > 0) {
                    params.put("voucher_id", String.valueOf(voucherId));
                    params.put("ma_voucher", maVoucher);
                    params.put("gia_tri_giam", String.valueOf(giatrigiam));
                    params.put("tong_truoc_giam", String.valueOf(tongtienGoc));
                }

                return params;
            }
        };

        requestQueue.add(request);
    }

    private void tinhTongTien() {
        tongtien = 0;
        tongSoLuong = 0;

        if (Utils.mangmuahang != null) {
            for (GioHang gioHang : Utils.mangmuahang) {
                tongtien += gioHang.getGiasp() * gioHang.getSoluong();
                tongSoLuong += gioHang.getSoluong();
            }
        }

        tongtienGoc = tongtien;

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        tvTongTien.setText(String.format(java.util.Locale.getDefault(), "Tổng tiền: %s ₫", decimalFormat.format(tongtien)));
        tvSoLuong.setText(String.format(java.util.Locale.getDefault(), "Số lượng: %d sản phẩm", tongSoLuong));
    }

    private void thanhToanVNPay() {
        Log.d(TAG, "===> Nút VNPay được bấm <===");

        String diachi = edtDiachi.getText().toString().trim();
        String sodienthoai = edtSodienthoai.getText().toString().trim();
        String ngaygiao = edtNgayGiao.getText().toString().trim();

        if (TextUtils.isEmpty(diachi)) {
            Toast.makeText(this, "Bạn chưa nhập địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(sodienthoai)) {
            Toast.makeText(this, "Bạn chưa nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.user_current == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.mangmuahang == null || Utils.mangmuahang.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = Utils.user_current.getId();
        String cartJson = new Gson().toJson(Utils.mangmuahang);
        String orderInfo = "Thanh toan don hang";

        Log.d(TAG, "Gọi API createVNPayPayment");
        Log.d(TAG, "iduser=" + id + ", sdt=" + sodienthoai + ", diachi=" + diachi + ", tongtien=" + tongtien);

        compositeDisposable.add(apiBanHang.createVNPayPayment(
                        id,
                        diachi,
                        sodienthoai,
                        tongSoLuong,
                        String.valueOf(tongtien),
                        cartJson,
                        ngaygiao,
                        orderInfo,
                        voucherId > 0 ? voucherId : null,
                        voucherId > 0 ? maVoucher : null,
                        voucherId > 0 ? giatrigiam : null,
                        voucherId > 0 ? tongtienGoc : null
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        vnPayResponse -> {
                            Log.d(TAG, "Response: " + new Gson().toJson(vnPayResponse));

                            if (vnPayResponse != null && vnPayResponse.isSuccess()) {
                                String paymentUrl = vnPayResponse.getPayment_url();

                                if (paymentUrl != null && !paymentUrl.isEmpty()) {
                                    Log.d(TAG, "Mở trình duyệt VNPay");

                                    try {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                                        startActivity(browserIntent);
                                        Toast.makeText(this, "Vui lòng hoàn tất thanh toán trên VNPay", Toast.LENGTH_LONG).show();

                                        //  Xóa toàn bộ giỏ hàng sau khi chuyển sang VNPay
                                        xoaToanBoGioHang();

                                    } catch (Exception e) {
                                        Log.e(TAG, "Lỗi mở trình duyệt: " + e.getMessage());
                                        Toast.makeText(this, "Không thể mở trình duyệt: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Không lấy được link thanh toán", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String msg = vnPayResponse != null ? vnPayResponse.getMessage() : "Lỗi không xác định";
                                Toast.makeText(this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "Lỗi gọi API: " + throwable.getMessage());
                            Toast.makeText(this, "Lỗi kết nối: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ));
    }

    private void thanhToanPayPal() {
        Log.e("PayPal", "thanhToanPayPal() được gọi");

        String diachi = edtDiachi.getText().toString().trim();
        String sodienthoai = edtSodienthoai.getText().toString().trim();
        String ngaygiao = edtNgayGiao.getText().toString().trim();

        if (TextUtils.isEmpty(diachi)) {
            Toast.makeText(this, "Bạn chưa nhập địa chỉ", Toast.LENGTH_SHORT).show();
            Log.w("PayPal", "Địa chỉ trống");
            return;
        }

        if (TextUtils.isEmpty(sodienthoai)) {
            Toast.makeText(this, "Bạn chưa nhập số điện thoại", Toast.LENGTH_SHORT).show();
            Log.w("PayPal", "Số điện thoại trống");
            return;
        }

        if (Utils.user_current == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            Log.e("PayPal", "User chưa đăng nhập");
            return;
        }

        if (Utils.mangmuahang == null || Utils.mangmuahang.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            Log.w("PayPal", "Giỏ hàng trống");
            return;
        }

        int id = Utils.user_current.getId();
        String cartJson = new Gson().toJson(Utils.mangmuahang);
        String orderInfo = "Thanh toan don hang qua PayPal";

        Log.d("PayPal", "Chuẩn bị gọi API createPayPalPayment");
        Log.d("PayPal", "Dữ liệu: iduser=" + id + ", sdt=" + sodienthoai + ", diachi=" + diachi + ", tongtien=" + tongtien);

        try {
            compositeDisposable.add(apiBanHang.createPayPalPayment(
                            id,
                            diachi,
                            sodienthoai,
                            tongSoLuong,
                            String.valueOf(tongtien),
                            cartJson,
                            ngaygiao,
                            orderInfo,
                            voucherId > 0 ? voucherId : null,
                            voucherId > 0 ? maVoucher : null,
                            voucherId > 0 ? giatrigiam : null,
                            voucherId > 0 ? tongtienGoc : null
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            payPalResponse -> {
                                Log.d("PayPal", "==================== RESPONSE DEBUG ====================");
                                Log.d("PayPal", "Phản hồi từ server: " + new Gson().toJson(payPalResponse));

                                if (payPalResponse == null) {
                                    Log.e("PayPal", "payPalResponse là NULL");
                                    Toast.makeText(this, "Server không trả về dữ liệu", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Log.d("PayPal", "Success: " + payPalResponse.isSuccess());
                                Log.d("PayPal", "Message: " + payPalResponse.getMessage());
                                Log.d("PayPal", "Approval URL: " + payPalResponse.getApprovalUrl());

                                if (payPalResponse.isSuccess()) {
                                    String approvalUrl = payPalResponse.getApprovalUrl();

                                    if (approvalUrl != null && !approvalUrl.isEmpty()) {
                                        if (!approvalUrl.startsWith("http://") && !approvalUrl.startsWith("https://")) {
                                            Log.e("PayPal", "URL KHÔNG HỢP LỆ!");
                                            Toast.makeText(this, "Link thanh toán không hợp lệ", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        try {
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(approvalUrl));

                                            // Xóa toàn bộ giỏ hàng sau khi chuyển sang PayPal
                                            xoaToanBoGioHang();
                                            startActivity(browserIntent);
                                            Log.d("PayPal", "Đã mở trình duyệt với URL PayPal");
                                            Toast.makeText(this, "Vui lòng hoàn tất thanh toán trên PayPal", Toast.LENGTH_LONG).show();

                                        } catch (Exception e) {
                                            Log.e("PayPal", "Lỗi khi mở trình duyệt: " + e.getMessage(), e);
                                            Toast.makeText(this, "Không thể mở trình duyệt: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(this, "Không lấy được link thanh toán PayPal", Toast.LENGTH_SHORT).show();
                                        Log.e("PayPal", "Link thanh toán null hoặc rỗng");
                                    }
                                } else {
                                    String msg = payPalResponse.getMessage();
                                    Log.e("PayPal", "Thanh toán thất bại: " + msg);
                                    Toast.makeText(this, "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                                }
                                Log.d("PayPal", "==================== END RESPONSE DEBUG ====================");
                            },
                            throwable -> {
                                Log.e("PayPal", "LỖI GỌI API PAYPAL ");
                                Log.e("PayPal", "Error class: " + throwable.getClass().getName());
                                Log.e("PayPal", "Error message: " + throwable.getMessage());
                                Log.e("PayPal", "Stack trace:", throwable);

                                String errMsg = "Lỗi kết nối";
                                if (throwable.getMessage() != null) {
                                    errMsg = throwable.getMessage();
                                }

                                Toast.makeText(this, "Lỗi: " + errMsg, Toast.LENGTH_LONG).show();
                            }
                    ));
        } catch (Exception e) {
            Log.e("PayPal", "EXCEPTION khi setup RxJava: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    // 🎫 Nhận kết quả từ màn hình chọn voucher
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 999 && resultCode == RESULT_OK && data != null) {
            String maVoucherDaChon = data.getStringExtra("ma_voucher");
            if (maVoucherDaChon != null && !maVoucherDaChon.isEmpty()) {
                edtVoucher.setText(maVoucherDaChon);
                // Tự động áp dụng voucher đã chọn
                apDungVoucher();
                Log.d(TAG, "Đã chọn voucher từ danh sách: " + maVoucherDaChon);
            }
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng (local + server)
     * Gọi sau khi thanh toán VNPay/PayPal
     */
    private void xoaToanBoGioHang() {
        Log.d(TAG, "Xóa toàn bộ giỏ hàng");

        // Xóa local
        if (Utils.manggiohang != null) {
            int soLuongXoa = Utils.manggiohang.size();
            Utils.manggiohang.clear();
            Log.d(TAG, "Đã xóa " + soLuongXoa + " sản phẩm khỏi giỏ hàng local");
        }

        if (Utils.mangmuahang != null) {
            Utils.mangmuahang.clear();
            Log.d(TAG, "Đã xóa danh sách mua hàng");
        }

        // Xóa trên server nếu user đã đăng nhập
        if (Utils.user_current != null && apiBanHang != null) {
            compositeDisposable.add(apiBanHang.xoaToanBoGioHang(
                    Utils.user_current.getId()
            ).subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe(
                 response -> {
                     if (response.isSuccess()) {
                         Log.d(TAG, "Đã xóa toàn bộ giỏ hàng trên server");
                     } else {
                         Log.w(TAG, "Server response: " + response.getMessage());
                     }
                 },
                 throwable -> {
                     Log.e(TAG, "Lỗi xóa giỏ hàng trên server: " + throwable.getMessage());
                     // Không hiển thị lỗi cho user vì đã xóa local thành công
                 }
             ));
        }
    }

    /**
     * Xóa các sản phẩm đã mua khỏi giỏ hàng
     * Gọi sau khi đặt hàng thành công
     */
    private void xoaSanPhamDaMuaKhoiGioHang() {
        if (Utils.mangmuahang == null || Utils.mangmuahang.isEmpty()) {
            Log.d(TAG, "Không có sản phẩm nào để xóa");
            return;
        }

        if (Utils.manggiohang == null) {
            Utils.manggiohang = new java.util.ArrayList<>();
            return;
        }

        // Xóa từng sản phẩm đã mua khỏi giỏ hàng
        for (GioHang sanPhamMua : Utils.mangmuahang) {
            for (int i = Utils.manggiohang.size() - 1; i >= 0; i--) {
                if (Utils.manggiohang.get(i).getIdsp() == sanPhamMua.getIdsp()) {
                    Log.d(TAG, "Xóa sản phẩm khỏi giỏ: " + sanPhamMua.getTensp());
                    Utils.manggiohang.remove(i);

                    // Đồng bộ xóa trên server nếu user đã đăng nhập
                    if (Utils.user_current != null && apiBanHang != null) {
                        xoaGioHangTrenServer(sanPhamMua.getIdsp());
                    }
                    break;
                }
            }
        }

        Log.d(TAG, "Đã xóa " + Utils.mangmuahang.size() + " sản phẩm khỏi giỏ hàng");
    }

    /**
     * Xóa sản phẩm trên server (không chặn UI)
     */
    private void xoaGioHangTrenServer(int idsp) {
        compositeDisposable.add(apiBanHang.xoaGioHang(
                Utils.user_current.getId(),
                idsp
        ).subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(
             response -> {
                 if (response.isSuccess()) {
                     Log.d(TAG, "Đã xóa sản phẩm " + idsp + " trên server");
                 }
             },
             throwable -> {
                 Log.e(TAG, "Lỗi xóa trên server: " + throwable.getMessage());
                 // Không hiển thị lỗi cho user vì đã xóa local thành công
             }
         ));
    }
}

