package vn.duytruong.appbandienthoai.activity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.text.DecimalFormat;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.model.Voucher;
import vn.duytruong.appbandienthoai.model.VoucherCheckResponse;
import vn.duytruong.appbandienthoai.utils.Utils;
import vn.duytruong.appbandienthoai.model.VNPayResponse;

public class ThanhToanActivity extends AppCompatActivity {
    private Toolbar toolbar;
    // 🎫 Voucher UI Components
    private EditText edtVoucher;
    private Button btnApDungVoucher, btnHuyVoucher, btnChonVoucher;
    private LinearLayout layoutVoucherResult;
    private TextView tvVoucherName, tvVoucherDesc, tvTongTruocGiam, tvGiamGia, tvTongSauGiam;

    // 🎫 Voucher Data
    private Voucher voucherDaChon = null;
    private double giaTriGiamVoucher = 0;
    private double tongTruocGiam = 0;
    private double tongSauGiam = 0;

    private TextView txttongtien, txtemail;
    private EditText txtsdt, edtdiachi;
    private Button btndathang, btnVNPay, btnPayPal;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiBanHang apiBanHang;

    private long tongtien;
    private int totalItem;
    private int iddonhang;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("PayPal-DEBUG", "========== ONCREATE BẮT ĐẦU ==========");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dat_hang);
        Log.e("PayPal-DEBUG", "setContentView THÀNH CÔNG");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.d("VNPay", "onCreate: Bắt đầu ThanhToanActivity");

        Log.e("PayPal-DEBUG", "Chuẩn bị gọi initView()...");
        initView();
        Log.e("PayPal-DEBUG", " initView() HOÀN THÀNH");

        Log.e("PayPal-DEBUG", "Chuẩn bị gọi countItem()...");
        countItem();
        Log.e("PayPal-DEBUG", "countItem() HOÀN THÀNH");

        Log.e("PayPal-DEBUG", "Chuẩn bị gọi initControl()...");
        initControl();
        Log.e("PayPal-DEBUG", " initControl() HOÀN THÀNH");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        Uri data = getIntent().getData();
        if (data != null) {
            Log.d("VNPay", "onCreate: Activity khởi động từ deep link: " + data);
            onNewIntent(getIntent());
        }

        Log.e("PayPal-DEBUG", "========== ONCREATE KẾT THÚC ==========");
    }

    private void countItem() {
        totalItem = 0;
        // Bảo vệ khi giỏ hàng chưa được khởi tạo (tránh NPE)
        if (Utils.mangmuahang == null) {
            Log.w("VNPay", "countItem: Utils.mangmuahang là null");
            return;
        }
        for (int i = 0; i < Utils.mangmuahang.size(); i++) {
            totalItem = totalItem + Utils.mangmuahang.get(i).getSoluong();
        }
        Log.d("VNPay", "countItem: Tổng số lượng sản phẩm = " + totalItem);
    }

    private void initControl() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        tongtien = getIntent().getLongExtra("tongtien", 0);
        tongTruocGiam = tongtien; // Lưu tổng tiền gốc
        tongSauGiam = tongtien;

        // Kiểm tra NULL trước khi setText
        if (txttongtien != null) {
            txttongtien.setText(decimalFormat.format(tongtien));
        }

        if (Utils.user_current != null) {
            if (txtemail != null) {
                txtemail.setText(Utils.user_current.getEmail());
            }
            if (txtsdt != null) {
                txtsdt.setText(Utils.user_current.getMobile());
            }
            Log.d("VNPay", "User hiện tại: " + Utils.user_current.getEmail());
        } else {
            Log.e("VNPay", "User null -> chưa đăng nhập");
            Toast.makeText(getApplicationContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🎫 VOUCHER: Nút chọn voucher từ danh sách
        if (btnChonVoucher != null) {
            Log.d("Voucher-DEBUG", " Đang setup listener cho btnChonVoucher");
            btnChonVoucher.setOnClickListener(view -> {
                Toast.makeText(ThanhToanActivity.this, "Đã nhấn nút chọn voucher!", Toast.LENGTH_SHORT).show();
                Log.d("Voucher-DEBUG", "===> NÚT CHỌN VOUCHER ĐƯỢC NHẤN <===");
                Log.d("Voucher-DEBUG", "User ID: " + (Utils.user_current != null ? Utils.user_current.getId() : "NULL"));
                Log.d("Voucher-DEBUG", "Tổng tiền: " + tongSauGiam);

                try {
                    Intent intent = new Intent(ThanhToanActivity.this, ChonVoucherActivity.class);
                    intent.putExtra("tong_tien", tongSauGiam);
                    intent.putExtra("user_id", Utils.user_current.getId());
                    Log.d("Voucher-DEBUG", "Chuẩn bị mở ChonVoucherActivity...");
                    startActivityForResult(intent, 999);
                    Log.d("Voucher-DEBUG", " Đã gọi startActivityForResult");
                } catch (Exception e) {
                    Log.e("Voucher-DEBUG", " LỖI khi mở ChonVoucherActivity: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(ThanhToanActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            Log.d("Voucher-DEBUG", "Đã setup listener xong cho btnChonVoucher");
        } else {
            Log.e("Voucher-DEBUG", " btnChonVoucher là NULL, không thể set listener!");
            Toast.makeText(this, "Lỗi: Không tìm thấy nút chọn voucher", Toast.LENGTH_SHORT).show();
        }

        // 🎫 VOUCHER: Nút áp dụng voucher
        btnApDungVoucher.setOnClickListener(view -> {
            String maVoucher = edtVoucher.getText().toString().trim().toUpperCase();
            if (TextUtils.isEmpty(maVoucher)) {
                Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
                return;
            }
            kiemTraVoucher(maVoucher);
        });

        // 🎫 VOUCHER: Nút hủy voucher
        btnHuyVoucher.setOnClickListener(view -> {
            huyVoucher();
        });

        btndathang.setOnClickListener(view -> {
            Log.d("VNPay", "Nút Đặt hàng thường được bấm");
            String str_diachi = edtdiachi.getText().toString().trim();
            if (TextUtils.isEmpty(str_diachi)) {
                Toast.makeText(getApplicationContext(), "Bạn chưa nhập địa chỉ", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(ThanhToanActivity.this, DatHangActivity.class);
                intent.putExtra("diachi", str_diachi);
                intent.putExtra("tongtien", tongtien);
                startActivity(intent);
            }
        });

        // ✅ Nút thanh toán VNPay
        btnVNPay.setOnClickListener(view -> {
            Log.d("VNPay", "===> Nút VNPay được bấm <===");

            // Kiểm tra kết nối mạng trước khi gọi API
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
            if (activeNetwork == null || !activeNetwork.isConnected()) {
                Log.w("VNPay", "Không có kết nối mạng - hủy thanh toán");
                Toast.makeText(getApplicationContext(), "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
                return;
            }

            String str_diachi = edtdiachi.getText().toString().trim();
            String str_sdt = txtsdt.getText().toString().trim();

            if (TextUtils.isEmpty(str_diachi)) {
                Toast.makeText(getApplicationContext(), "Bạn chưa nhập địa chỉ", Toast.LENGTH_SHORT).show();
                Log.w("VNPay", "Địa chỉ trống, hủy thanh toán");
                return;
            }

            int id = Utils.user_current.getId();
            // Chuyển giỏ hàng an toàn sang JSON (nếu null -> gửi mảng rỗng)
            Object cartSource = (Utils.mangmuahang == null) ? new java.util.ArrayList<>() : Utils.mangmuahang;
            String cartJson = new Gson().toJson(cartSource);
            String orderInfo = "Thanh toan don hang";

            Log.d("VNPay", "Chuẩn bị gọi API createVNPayPayment");
            Log.d("VNPay", "Dữ liệu gửi: iduser=" + id + ", sdt=" + str_sdt + ", diachi=" + str_diachi +
                    ", tongtien=" + tongtien + ", totalItem=" + totalItem);
            Log.d("VNPay", "cartJson = " + cartJson);

            compositeDisposable.add(apiBanHang.createVNPayPayment(
                            id,
                            str_diachi,
                            str_sdt,
                            totalItem,
                            String.valueOf(tongSauGiam), // Sử dụng tổng sau khi giảm voucher
                            cartJson,
                            "",
                            orderInfo,
                            voucherDaChon != null ? voucherDaChon.getId() : null,
                            voucherDaChon != null ? voucherDaChon.getMa_voucher() : null,
                            voucherDaChon != null ? (long) giaTriGiamVoucher : null,
                            voucherDaChon != null ? (long) tongTruocGiam : null
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            (VNPayResponse vnPayResponse) -> {
                                Log.d("VNPay", "==================== RESPONSE DEBUG ====================");
                                Log.d("VNPay", "Phản hồi từ server: " + new Gson().toJson(vnPayResponse));

                                if (vnPayResponse == null) {
                                    Log.e("VNPay", "vnPayResponse là NULL");
                                    Toast.makeText(getApplicationContext(), "Server không trả về dữ liệu", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Log.d("VNPay", "Success: " + vnPayResponse.isSuccess());
                                Log.d("VNPay", "Message: " + vnPayResponse.getMessage());
                                Log.d("VNPay", "Payment URL: " + vnPayResponse.getPayment_url());
                                Log.d("VNPay", "Ma don hang: " + vnPayResponse.getMadonhang());
                                Log.d("VNPay", "ID don hang: " + vnPayResponse.getIddonhang());

                                if (vnPayResponse.isSuccess()) {
                                    String paymentUrl = vnPayResponse.getPayment_url();

                                    if (paymentUrl != null && !paymentUrl.isEmpty()) {
                                        Log.d("VNPay", "Payment URL length: " + paymentUrl.length());
                                        Log.d("VNPay", "Payment URL starts with: " + paymentUrl.substring(0, Math.min(50, paymentUrl.length())));

                                        // Kiểm tra URL có hợp lệ không
                                        if (!paymentUrl.startsWith("http://") && !paymentUrl.startsWith("https://")) {
                                            Log.e("VNPay", "URL KHÔNG HỢP LỆ - không bắt đầu bằng http/https!");
                                            Toast.makeText(getApplicationContext(), "Link thanh toán không hợp lệ: " + paymentUrl, Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        try {
                                            // Mở trình duyệt để thanh toán VNPay (không thêm FLAG để tránh tạo Activity mới)
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                                            startActivity(browserIntent);
                                            Log.d("VNPay", "Đã mở trình duyệt với URL VNPay");
                                            Toast.makeText(getApplicationContext(), "Vui lòng hoàn tất thanh toán trên VNPay", Toast.LENGTH_LONG).show();

                                            //  KHÔNG chuyển màn hình ngay - đợi user thanh toán xong
                                            // VNPay sẽ callback về onNewIntent() sau khi thanh toán

                                        } catch (Exception e) {
                                            Log.e("VNPay", "Lỗi khi mở trình duyệt: " + e.getMessage(), e);
                                            Toast.makeText(getApplicationContext(), "Không thể mở trình duyệt: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Không lấy được link thanh toán", Toast.LENGTH_SHORT).show();
                                        Log.e("VNPay", "Link thanh toán null ho��c rỗng");
                                    }
                                } else {
                                    String msg = vnPayResponse.getMessage();
                                    Log.e("VNPay", "Thanh toán thất bại: " + msg);
                                    Toast.makeText(getApplicationContext(), "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                                }
                                Log.d("VNPay", "==================== END RESPONSE DEBUG ====================");
                            },
                            throwable -> {
                                // Ghi log chi tiết hơn và hiển thị thông điệp ngắn gọn cho người dùng
                                Log.e("VNPay", "Lỗi gọi API: " + throwable.toString(), throwable);
                                String errMsg = (throwable.getMessage() != null) ? throwable.getMessage() : throwable.toString();
                                Toast.makeText(getApplicationContext(), "Lỗi kết nối: " + errMsg, Toast.LENGTH_LONG).show();
                            }
                    ));
        });

        // Nút thanh toán PayPal - Setup trực tiếp không qua method riêng
        Log.d("PayPal-DEBUG", "========== BẮT ĐẦU SETUP PAYPAL LISTENER ==========");
        Log.d("PayPal-DEBUG", "btnPayPal is null? " + (btnPayPal == null));

        if (btnPayPal == null) {
            Log.e("PayPal-DEBUG", "CRITICAL ERROR: btnPayPal is NULL in initControl()!");
            Toast.makeText(this, "LỖI: Nút PayPal không tồn tại!", Toast.LENGTH_LONG).show();
        } else {
            Log.d("PayPal-DEBUG", "btnPayPal OK, đang setup listener...");

            btnPayPal.setOnClickListener(view -> {
                Log.e("PayPal", "===> NÚT PAYPAL ĐƯỢC BẤM - BẮT ĐẦU XỬ LÝ <===");
                Log.e("PayPal", "Thread: " + Thread.currentThread().getName());
                Toast.makeText(getApplicationContext(), "Đang xử lý PayPal...", Toast.LENGTH_SHORT).show();

                // Kiểm tra kết nối mạng trước khi gọi API
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
                if (activeNetwork == null || !activeNetwork.isConnected()) {
                    Log.w("PayPal", "Không có kết nối mạng - hủy thanh toán");
                    Toast.makeText(getApplicationContext(), "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
                    return;
                }

                String str_diachi = edtdiachi.getText().toString().trim();
                String str_sdt = txtsdt.getText().toString().trim();

                if (TextUtils.isEmpty(str_diachi)) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập địa chỉ", Toast.LENGTH_SHORT).show();
                    Log.w("PayPal", "Địa chỉ trống, hủy thanh toán");
                    return;
                }

                int id = Utils.user_current.getId();
                Object cartSource = (Utils.mangmuahang == null) ? new java.util.ArrayList<>() : Utils.mangmuahang;
                String cartJson = new Gson().toJson(cartSource);
                String orderInfo = "Thanh toan don hang qua PayPal";

                Log.d("PayPal", "Chuẩn bị gọi API createPayPalPayment");
                Log.d("PayPal", "Dữ liệu gửi: iduser=" + id + ", sdt=" + str_sdt + ", diachi=" + str_diachi +
                        ", tongtien=" + tongtien + ", totalItem=" + totalItem);
                Log.d("PayPal", "cartJson = " + cartJson);

                compositeDisposable.add(apiBanHang.createPayPalPayment(
                                id,
                                str_diachi,
                                str_sdt,
                                totalItem,
                                String.valueOf(tongSauGiam), // Sử dụng tổng sau khi giảm voucher
                                cartJson,
                                "",
                                orderInfo,
                                voucherDaChon != null ? voucherDaChon.getId() : null,
                                voucherDaChon != null ? voucherDaChon.getMa_voucher() : null,
                                voucherDaChon != null ? (long) giaTriGiamVoucher : null,
                                voucherDaChon != null ? (long) tongTruocGiam : null
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (vn.duytruong.appbandienthoai.model.PayPalResponse payPalResponse) -> {
                                    Log.d("PayPal", "==================== RESPONSE DEBUG ====================");
                                    Log.d("PayPal", "Phản hồi từ server: " + new Gson().toJson(payPalResponse));

                                    if (payPalResponse == null) {
                                        Log.e("PayPal", "payPalResponse là NULL");
                                        Toast.makeText(getApplicationContext(), "Server không trả về dữ liệu", Toast.LENGTH_LONG).show();
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
                                                Toast.makeText(getApplicationContext(), "Link thanh toán không hợp lệ", Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            try {
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(approvalUrl));
                                                startActivity(browserIntent);
                                                Log.d("PayPal", "Đã mở trình duyệt với URL PayPal");
                                                Toast.makeText(getApplicationContext(), "Vui lòng hoàn tất thanh toán trên PayPal", Toast.LENGTH_LONG).show();

                                            } catch (Exception e) {
                                                Log.e("PayPal", "Lỗi khi mở trình duyệt: " + e.getMessage(), e);
                                                Toast.makeText(getApplicationContext(), "Không thể mở trình duyệt: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Không lấy được link thanh toán PayPal", Toast.LENGTH_SHORT).show();
                                            Log.e("PayPal", "Link thanh toán null hoặc rỗng");
                                        }
                                    } else {
                                        String msg = payPalResponse.getMessage();
                                        Log.e("PayPal", "Thanh toán thất bại: " + msg);
                                        Toast.makeText(getApplicationContext(), "Lỗi: " + msg, Toast.LENGTH_LONG).show();
                                    }
                                    Log.d("PayPal", "==================== END RESPONSE DEBUG ====================");
                                },
                                throwable -> {
                                    Log.e("PayPal", "Lỗi gọi API: " + throwable.toString(), throwable);
                                    String errMsg = (throwable.getMessage() != null) ? throwable.getMessage() : throwable.toString();
                                    Toast.makeText(getApplicationContext(), "Lỗi kết nối: " + errMsg, Toast.LENGTH_LONG).show();
                                }
                        ));
            });

            Log.d("PayPal-DEBUG", "SETUP PAYPAL LISTENER THÀNH CÔNG!");
        }

        Log.d("PayPal-DEBUG", "========== KẾT THÚC SETUP PAYPAL LISTENER ==========");
    }

    private void initView() {
        Log.d("PayPal-DEBUG", "========== BẮT ĐẦU initView() ==========");

        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        toolbar = findViewById(R.id.toolbar);
        txttongtien = findViewById(R.id.tvTongTien);
        txtsdt = findViewById(R.id.edtSodienthoai);
        txtemail = findViewById(R.id.tvEmail);
        edtdiachi = findViewById(R.id.edtDiachi);
        btndathang = findViewById(R.id.btnDatHang);
        btnVNPay = findViewById(R.id.btnVNPay);
        btnPayPal = findViewById(R.id.btnPayPal);
        edtVoucher = findViewById(R.id.edtVoucher);
        btnApDungVoucher = findViewById(R.id.btnApDungVoucher);
        btnHuyVoucher = findViewById(R.id.btnHuyVoucher);
        btnChonVoucher = findViewById(R.id.btnChonVoucher);
        layoutVoucherResult = findViewById(R.id.layoutVoucherResult);
        tvVoucherName = findViewById(R.id.tvVoucherName);
        tvVoucherDesc = findViewById(R.id.tvVoucherDesc);
        tvTongTruocGiam = findViewById(R.id.tvTongTruocGiam);
        tvGiamGia = findViewById(R.id.tvGiamGia);
        tvTongSauGiam = findViewById(R.id.tvTongSauGiam);

        Log.d("PayPal-DEBUG", "Đã findViewById cho tất cả views");

        if (btnVNPay == null) {
            Log.e("VNPay", "initView: btnVNPay là NULL — kiểm tra ID trong XML layout!");
        } else {
            btnVNPay.setEnabled(true);
            btnVNPay.setClickable(true);
            Log.d("VNPay", "initView: btnVNPay tìm thấy và bật click OK");
        }

        if (btnPayPal == null) {
            Log.e("PayPal-DEBUG", " CRITICAL: btnPayPal là NULL!");
            Log.e("PayPal-DEBUG", "Layout đang dùng: " + getResources().getResourceName(R.layout.activity_dat_hang));
            Log.e("PayPal-DEBUG", "Kiểm tra file XML có button với id=btnPayPal không!");
        } else {
            Log.d("PayPal-DEBUG", " btnPayPal TÌM THẤY!");
        }

        // Kiểm tra btnChonVoucher
        if (btnChonVoucher == null) {
            Log.e("Voucher-DEBUG", " CRITICAL: btnChonVoucher là NULL!");
            Log.e("Voucher-DEBUG", "Kiểm tra file XML có button với id=btnChonVoucher không!");
        } else {
            Log.d("Voucher-DEBUG", "btnChonVoucher TÌM THẤY!");
            btnChonVoucher.setEnabled(true);
            btnChonVoucher.setClickable(true);
        }

        if (btnPayPal != null) {
            Log.d("PayPal-DEBUG", "Button class: " + btnPayPal.getClass().getName());
            Log.d("PayPal-DEBUG", "Button visibility: " + btnPayPal.getVisibility());
            Log.d("PayPal-DEBUG", "Button isEnabled: " + btnPayPal.isEnabled());
            Log.d("PayPal-DEBUG", "Button isClickable: " + btnPayPal.isClickable());
            Log.d("PayPal-DEBUG", "Button hasOnClickListeners: " + btnPayPal.hasOnClickListeners());

            btnPayPal.setEnabled(true);
            btnPayPal.setClickable(true);

            Log.d("PayPal-DEBUG", "Sau khi set - isEnabled: " + btnPayPal.isEnabled());
            Log.d("PayPal-DEBUG", "Sau khi set - isClickable: " + btnPayPal.isClickable());
        }

        Log.d("PayPal-DEBUG", "========== KẾT THÚC initView() ==========");

        // 🧪 TEST: Tự động click PayPal sau 2 giây để kiểm tra
        if (btnPayPal != null) {
            btnPayPal.postDelayed(() -> {
                Log.d("PayPal-TEST", "⚡ Tự động click PayPal để test...");
                // Bỏ comment dòng dưới nếu muốn test tự động
                // btnPayPal.performClick();
            }, 2000);
        }
    }

    private void pushNotiToUser() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "order_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Đặt hàng thành công")
                .setContentText("Cảm ơn bạn đã đặt hàng. Đơn hàng của bạn đã được ghi nhận.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "order_channel",
                    "Order Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void handlePayPalCallback(String madonhang, String paymentId, String payerId, String status) {
        Log.d("PayPal", "Xử lý PayPal callback với madonhang=" + madonhang);

        // Clear giỏ hàng ngay lập tức
        if (Utils.mangmuahang != null) {
            Utils.mangmuahang.clear();
            Log.d("PayPal", "Đã clear giỏ hàng");
        }

        // Hiển thị thông báo đang xử lý
        Toast.makeText(this, "Đang xác nhận thanh toán PayPal...", Toast.LENGTH_SHORT).show();

        // GỌI API TRƯỚC, CHỜ KẾT QUẢ, RỒI MỚI CHUYỂN MÀN HÌNH
        compositeDisposable.add(apiBanHang.executePayPalPayment(madonhang, paymentId, payerId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(statusResponse -> {
                    Log.d("PayPal", "Kết quả executePayPalPayment: " + new Gson().toJson(statusResponse));

                    //  Finish ThanhToanActivity
                    finish();

                    // Chuyển về màn hình danh sách đơn hàng SAU KHI API hoàn thành
                    Intent intent = new Intent(getApplicationContext(), XemDonActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    // Hiển thị thông báo thành công
                    if (statusResponse.isSuccess()) {
                        Toast.makeText(getApplicationContext(), "Thanh toán PayPal thành công!", Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    Log.e("PayPal", "Lỗi executePayPalPayment: " + throwable.getMessage());

                    //  Vẫn chuyển màn hình dù có lỗi
                    finish();
                    Intent intent = new Intent(getApplicationContext(), XemDonActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    Toast.makeText(getApplicationContext(), "Đã tạo đơn hàng. Vui lòng kiểm tra trạng thái.", Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
        Log.d("VNPay", "onDestroy: Clear compositeDisposable");
    }

    // PayPal sẽ callback qua deep link, xử lý trong onNewIntent()
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null) {
            Log.d("Payment", "Deep link trả về: " + data);

            String madonhang = data.getQueryParameter("madonhang");
            String status = data.getQueryParameter("status");
            String resp = data.getQueryParameter("resp");
            String paymentId = data.getQueryParameter("paymentId");
            String payerId = data.getQueryParameter("PayerID");

            Log.d("Payment", "madonhang=" + madonhang + ", status=" + status + ", resp=" + resp);
            Log.d("Payment", "paymentId=" + paymentId + ", payerId=" + payerId);

            // Xử lý callback từ PayPal
            if (paymentId != null && payerId != null && madonhang != null) {
                handlePayPalCallback(madonhang, paymentId, payerId, status);
                return;
            }

            // Xử lý callback từ VNPay (giữ nguyên logic cũ)

            if (madonhang != null && !madonhang.isEmpty()) {
                compositeDisposable.add(apiBanHang.checkVNPayStatus(madonhang)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(statusResponse -> {
                            Log.d("VNPay", "Kết quả checkVNPayStatus: " + new Gson().toJson(statusResponse));

                            if (statusResponse.isSuccess() && statusResponse.getData() != null) {
                                String trangthai = statusResponse.getData().getTrangthai();
                                Log.d("VNPay", "Trạng thái đơn hàng: " + trangthai);

                                if ("success".equalsIgnoreCase(status) || "00".equals(resp)) {
                                    Toast.makeText(this, "Thanh toán VNPay thành công! Đơn hàng: " + madonhang, Toast.LENGTH_LONG).show();

                                    //  Clear giỏ hàng sau khi thanh toán thành công
                                    if (Utils.mangmuahang != null) {
                                        Utils.mangmuahang.clear();
                                        Log.d("VNPay", "Đã clear giỏ hàng sau thanh toán thành công");
                                    }
                                } else {
                                    Toast.makeText(this, "Thanh toán VNPay không thành công. Trạng thái: " + trangthai, Toast.LENGTH_LONG).show();
                                }
                            }

                            // Chuyển sang màn hình xem đơn hàng
                            Intent i = new Intent(getApplicationContext(), XemDonActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }, throwable -> {
                            Log.e("VNPay", "Lỗi checkVNPayStatus: " + throwable.getMessage());
                            Toast.makeText(this, "Lỗi kiểm tra trạng thái: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                            // Vẫn chuyển sang màn hình xem đơn hàng dù có lỗi
                            Intent i = new Intent(getApplicationContext(), XemDonActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }));
            }
        }
    }

    // 🎫 VOUCHER: Kiểm tra và áp dụng voucher
    private void kiemTraVoucher(String maVoucher) {
        if (Utils.user_current == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("Voucher", "Kiểm tra voucher: " + maVoucher);
        Toast.makeText(this, "Đang kiểm tra mã giảm giá...", Toast.LENGTH_SHORT).show();

        int userId = Utils.user_current.getId();
        double tongTien = tongtien;

        compositeDisposable.add(apiBanHang.checkVoucher(maVoucher, userId, tongTien)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Log.d("Voucher", "Response: " + new Gson().toJson(response));
                            if (response.isSuccess()) {
                                // Voucher hợp lệ
                                voucherDaChon = response.getVoucher();
                                VoucherCheckResponse.TinhToan tinhToan = response.getTinh_toan();

                                giaTriGiamVoucher = tinhToan.getGia_tri_giam();
                                tongTruocGiam = tinhToan.getTong_truoc_giam();
                                tongSauGiam = tinhToan.getTong_sau_giam();

                                hienThiVoucherResult();
                                Toast.makeText(this, "Áp dụng mã giảm giá thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                //  Voucher không hợp lệ
                                Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        },
                        error -> {
                            Log.e("Voucher", "Lỗi: " + error.getMessage());
                            Toast.makeText(this, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    // 🎫 VOUCHER: Hiển thị kết quả voucher
    private void hienThiVoucherResult() {
        DecimalFormat df = new DecimalFormat("###,###,###");

        //  Kiểm tra NULL trước khi sử dụng
        if (layoutVoucherResult != null) {
            layoutVoucherResult.setVisibility(View.VISIBLE);
        }

        if (tvVoucherName != null && voucherDaChon != null) {
            tvVoucherName.setText("Mã: " + voucherDaChon.getMa_voucher());
        }

        if (tvVoucherDesc != null && voucherDaChon != null) {
            tvVoucherDesc.setText(voucherDaChon.getText_giam());
        }

        if (tvTongTruocGiam != null) {
            tvTongTruocGiam.setText(df.format(tongTruocGiam) + " ₫");
        }

        if (tvGiamGia != null) {
            tvGiamGia.setText("-" + df.format(giaTriGiamVoucher) + " ₫");
        }

        if (tvTongSauGiam != null) {
            tvTongSauGiam.setText(df.format(tongSauGiam) + " ₫");
        }

        // Cập nhật tổng tiền hiển thị
        if (txttongtien != null) {
            txttongtien.setText("Tổng: " + df.format(tongSauGiam) + " ₫");
        }

        Log.d("Voucher", "Đã hiển thị kết quả voucher: giảm " + giaTriGiamVoucher);
    }

    // VOUCHER: Hủy voucher
    private void huyVoucher() {
        voucherDaChon = null;
        giaTriGiamVoucher = 0;
        tongSauGiam = tongTruocGiam;

        //  Kiểm tra NULL trước khi sử dụng
        if (layoutVoucherResult != null) {
            layoutVoucherResult.setVisibility(View.GONE);
        }

        if (edtVoucher != null) {
            edtVoucher.setText("");
        }

        DecimalFormat df = new DecimalFormat("###,###,###");
        if (txttongtien != null) {
            txttongtien.setText("Tổng: " + df.format(tongtien) + "đ");
        }

        Toast.makeText(this, "Đã hủy mã giảm giá", Toast.LENGTH_SHORT).show();
        Log.d("Voucher", "Đã hủy voucher");
    }

    //  VOUCHER: Nhận kết quả từ màn hình chọn voucher
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 999 && resultCode == RESULT_OK && data != null) {
            // Nhận mã voucher được chọn
            String maVoucher = data.getStringExtra("ma_voucher");
            if (maVoucher != null && !maVoucher.isEmpty()) {
                edtVoucher.setText(maVoucher);
                kiemTraVoucher(maVoucher);
                Log.d("Voucher", "Đã chọn voucher từ danh sách: " + maVoucher);
            }
        }
    }
}
