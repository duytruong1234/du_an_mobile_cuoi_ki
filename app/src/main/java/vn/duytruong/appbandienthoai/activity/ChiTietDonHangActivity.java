package vn.duytruong.appbandienthoai.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.ChiTietDonHangAdapter;
import vn.duytruong.appbandienthoai.model.ChiTietDonHang;
import vn.duytruong.appbandienthoai.utils.Utils;

public class ChiTietDonHangActivity extends AppCompatActivity {
    private static final String TAG = "ChiTietDonHangActivity";
    private Toolbar toolbar;
    private TextView tvMaDonHang, tvNgayDat, tvNgayGiao, tvDiaChi, tvSoDienThoai, tvTongTien, tvTrangThai;
    private RecyclerView recyclerView;
    private ChiTietDonHangAdapter adapter;
    private List<ChiTietDonHang> chiTietList;
    private RequestQueue requestQueue;
    private String madonhang;
    private ProgressBar progressBar;
    private ExecutorService executorService;

    // Admin UI
    private View layoutAdminControls;
    private Spinner spinnerTrangThai;
    private Button btnCapNhatTrangThai;

    // VNPay payment button
    private Button btnContinueVNPay;
    private Button btnCancelOrder;
    private long totalAmount = 0; // Lưu tổng tiền để thanh toán VNPay
    private String currentTrangThai = ""; // Lưu trạng thái hiện tại

    // Broadcast receiver for order status updates
    private BroadcastReceiver orderUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don_hang);

        try {
            madonhang = getIntent().getStringExtra("madonhang");

            initView();

            // Defensive: if key views are null, log and finish to avoid NPE crash
            if (toolbar == null || recyclerView == null) {
                Toast.makeText(this, "Lỗi giao diện, không thể mở chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // If madonhang is missing, open the orders list instead of crashing
            if (madonhang == null || madonhang.trim().isEmpty()) {
                Toast.makeText(this, "Không tìm thấy mã đơn hàng. Mở danh sách đơn.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, XemDonActivity.class));
                finish();
                return;
            }

            initControl();

            // Show admin controls only when current user is admin
            if (Utils.user_current != null && Utils.user_current.isAdmin()) {
                layoutAdminControls.setVisibility(View.VISIBLE);
                setupAdminControls();
            } else {
                layoutAdminControls.setVisibility(View.GONE);
            }

            getChiTiet();
        } catch (Throwable t) {
            // Catch any unexpected runtime errors to prevent crash and log for debugging
            Log.e(TAG, "Unexpected error in onCreate", t);
            Toast.makeText(this, "Đã xảy ra lỗi. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            // Optionally fall back to order list
            try {
                startActivity(new Intent(this, XemDonActivity.class));
            } catch (Exception ex) {
                Log.e(TAG, "Failed to start XemDonActivity", ex);
            }
            finish();
        }
    }

    private void initControl() {
        // Guard against null toolbar (shouldn't happen if layout correct)
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            androidx.appcompat.app.ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setTitle(getString(R.string.title_chi_tiet_don_hang));
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        requestQueue = Volley.newRequestQueue(this);
        chiTietList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChiTietDonHangAdapter(this, chiTietList);
        recyclerView.setAdapter(adapter);

        // Executor for background parsing
        executorService = Executors.newSingleThreadExecutor();

        // Xử lý click nút tiếp tục thanh toán VNPay
        btnContinueVNPay.setOnClickListener(v -> {
            continueVNPayPayment();
        });

        // Xử lý click nút hủy đơn hàng
        btnCancelOrder.setOnClickListener(v -> {
            showCancelOrderConfirmDialog();
        });
    }

    private void setupAdminControls() {
        // Populate spinner with labels matching server-side statuses
        String[] labels = new String[]{"Đang xử lý", "Đã chuẩn bị", "Đang giao", "Thành công", "Đã hủy"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrangThai.setAdapter(spinnerAdapter);

        btnCapNhatTrangThai.setOnClickListener(v -> {
            int selected = spinnerTrangThai.getSelectedItemPosition();
            updateTrangThaiOnServer(selected);
        });
    }

    // Now accept spinner index (0-based) and send 1-based value to server
    private void updateTrangThaiOnServer(int spinnerIndex) {
        if (madonhang == null) return;
        progressBar.setVisibility(View.VISIBLE);
        String url = Utils.URL_CAP_NHAT_TRANG_THAI;

        // server expects 1..N, spinnerIndex is 0..N-1
        final int serverValue = spinnerIndex + 1;

        StringRequest post = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject jo = new JSONObject(response);
                        boolean success = jo.optBoolean("success", false);
                        String message = jo.optString("message", "");
                        Toast.makeText(ChiTietDonHangActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            // Notify other parts of app that order status updated
                            try {
                                Intent b = new Intent(Utils.ACTION_ORDER_STATUS_UPDATED);
                                b.putExtra(Utils.EXTRA_MA_DONHANG, madonhang);
                                // Keep broadcasting spinner index (0-based) so receivers can map to labels easily
                                b.putExtra(Utils.EXTRA_TRANGTHAI_INDEX, spinnerIndex);
                                // Put a human-readable string for convenience
                                String[] labels = new String[]{"Đang xử lý", "Đã chuẩn bị", "Đang giao", "Thành công", "Đã hủy"};
                                String txt = (spinnerIndex >=0 && spinnerIndex < labels.length) ? labels[spinnerIndex] : String.valueOf(serverValue);
                                b.putExtra(Utils.EXTRA_TRANGTHAI_TEXT, txt);
                                // Send local broadcast so other components in the app can update immediately
                                try {
                                    LocalBroadcastManager.getInstance(ChiTietDonHangActivity.this).sendBroadcast(b);
                                } catch (Exception ignored) {}
                            } catch (Exception ignored) {}
                            getChiTiet();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Invalid JSON from update", e);
                        Toast.makeText(ChiTietDonHangActivity.this, "Phản hồi không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Volley error updating trangthai", error);
                    Toast.makeText(ChiTietDonHangActivity.this, "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("madonhang", madonhang);
                // send serverValue (1-based) to match DB conventions
                p.put("trangthai", String.valueOf(serverValue));
                return p;
            }
        };

        requestQueue.add(post);
    }

    private void getChiTiet() {
        String encodedMadonhang = madonhang;
        try {
            encodedMadonhang = URLEncoder.encode(madonhang, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            Log.w(TAG, "Failed to encode madonhang, using raw value", e);
        }

        String url = Utils.URL_GET_CHI_TIET_DON_HANG + "?madonhang=" + encodedMadonhang;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    executorService.submit(() -> {
                        List<ChiTietDonHang> parsed = new ArrayList<>();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                JSONObject donhang = jsonObject.optJSONObject("donhang");
                                final String madon = (donhang != null) ? donhang.optString("madonhang", "") : "";
                                final String ngaydat = (donhang != null) ? donhang.optString("ngaydat", "") : "";
                                final String ngaygiao = (donhang != null) ? donhang.optString("ngaygiaodukien", "") : "";
                                final String diachi = (donhang != null) ? donhang.optString("diachi", "") : "";
                                final String sdt = (donhang != null) ? donhang.optString("sodienthoai", "") : "";
                                final String trangthai = (donhang != null) ? donhang.optString("trangthai", "") : "";
                                final String vnpayTransactionNo = (donhang != null) ? donhang.optString("vnpay_transaction_no", "") : "";
                                final String vnpayBankCode = (donhang != null) ? donhang.optString("vnpay_bank_code", "") : "";
                                final String vnpayPayDate = (donhang != null) ? donhang.optString("vnpay_pay_date", "") : "";
                                final long[] tongtien = new long[]{0L};
                                try {
                                    tongtien[0] = Long.parseLong(donhang != null ? donhang.optString("tongtien", "0") : "0");
                                } catch (Exception ex) {
                                    Log.w(TAG, "Failed to parse tongtien", ex);
                                    tongtien[0] = 0L;
                                }

                                JSONArray items = jsonObject.optJSONArray("items");
                                if (items != null) {
                                    for (int i = 0; i < items.length(); i++) {
                                        JSONObject item = items.getJSONObject(i);

                                        ChiTietDonHang chiTiet = new ChiTietDonHang();
                                        chiTiet.setId(item.optInt("idchitiet", 0));
                                        chiTiet.setIdsp(item.optInt("idsp", 0));
                                        chiTiet.setTensp(item.optString("tensp", ""));
                                        chiTiet.setHinhanh(item.optString("hinhanh", ""));
                                        chiTiet.setSoluong(item.optInt("soluong", 0));
                                        chiTiet.setGia(item.optString("gia", "0"));
                                        chiTiet.setThanhtien(item.optString("thanhtien", "0"));

                                        parsed.add(chiTiet);
                                    }
                                }

                                runOnUiThread(() -> {
                                    try {
                                        tvMaDonHang.setText(getString(R.string.label_ma_donhang, madon));
                                        tvNgayDat.setText(getString(R.string.label_ngay_dat, ngaydat));
                                        tvNgayGiao.setText(getString(R.string.label_ngay_giao, ngaygiao));
                                        tvDiaChi.setText(getString(R.string.label_dia_chi, diachi));
                                        tvSoDienThoai.setText(getString(R.string.label_sdt, sdt));

                                        // Lưu tổng tiền và trạng thái
                                        totalAmount = tongtien[0];
                                        currentTrangThai = trangthai;

                                        // Show a human-readable label for trangthai when possible
                                        String[] labels = new String[]{"Đang xử lý", "Đã chuẩn bị", "Đang giao", "Thành công", "Đã hủy"};
                                        String displayTrangThai = trangthai;
                                        int parsedServerValue = Integer.MIN_VALUE;
                                        try {
                                            parsedServerValue = Integer.parseInt(trangthai);
                                            if (parsedServerValue >= 1 && parsedServerValue <= labels.length) {
                                                displayTrangThai = labels[parsedServerValue - 1];
                                            }
                                        } catch (Exception ignored) {}

                                        // Kiểm tra và hiển thị nút tiếp tục thanh toán VNPay
                                        // CHỈ hiển thị nút khi:
                                        // 1. Trạng thái là "0" (Chờ thanh toán VNPay)
                                        // 2. VÀ vnpay_transaction_no rỗng hoặc null (chưa có giao dịch VNPay nào)
                                        // 3. VÀ vnpay_pay_date rỗng hoặc null (chưa thanh toán)
                                        // Đơn hàng COD sẽ có vnpay_transaction_no = null và không được hiển thị nút này
                                        boolean isWaitingVNPayPayment = trangthai.equalsIgnoreCase("0") &&
                                                                       (vnpayTransactionNo.trim().isEmpty() || vnpayTransactionNo.equalsIgnoreCase("null")) &&
                                                                       (vnpayPayDate.trim().isEmpty() || vnpayPayDate.equalsIgnoreCase("null"));

                                        if (isWaitingVNPayPayment && btnContinueVNPay != null) {
                                            btnContinueVNPay.setVisibility(View.VISIBLE);
                                        } else if (btnContinueVNPay != null) {
                                            btnContinueVNPay.setVisibility(View.GONE);
                                        }

                                        // Kiểm tra và hiển thị nút hủy đơn hàng
                                        // Cho phép hủy đơn ở trạng thái: "Chờ thanh toán", "Đang xử lý", "Đã thanh toán"
                                        // KHÔNG cho phép hủy: "Đã chuẩn bị", "Đang giao", "Thành công", "Đã hủy"
                                        boolean canCancel = trangthai.equalsIgnoreCase("0") ||
                                                          trangthai.equalsIgnoreCase("1") ||
                                                          trangthai.toLowerCase().contains("đang xử lý") ||
                                                          trangthai.toLowerCase().contains("chờ thanh toán") ||
                                                          trangthai.toLowerCase().contains("đã thanh toán") ||
                                                          displayTrangThai.toLowerCase().contains("đang xử lý") ||
                                                          displayTrangThai.toLowerCase().contains("chờ") ||
                                                          displayTrangThai.toLowerCase().contains("thanh toán");

                                        // Loại trừ các trạng thái KHÔNG được phép hủy
                                        boolean cannotCancel = trangthai.toLowerCase().contains("chuẩn bị") ||
                                                             trangthai.toLowerCase().contains("đã giao") ||
                                                             trangthai.toLowerCase().contains("thành công") ||
                                                             (trangthai.toLowerCase().contains("hủy") && !trangthai.toLowerCase().contains("chưa")) ||
                                                             displayTrangThai.toLowerCase().contains("chuẩn bị") ||
                                                             displayTrangThai.toLowerCase().contains("giao") ||
                                                             displayTrangThai.toLowerCase().contains("thành công");

                                        if (canCancel && !cannotCancel && btnCancelOrder != null) {
                                            btnCancelOrder.setVisibility(View.VISIBLE);
                                        } else if (btnCancelOrder != null) {
                                            btnCancelOrder.setVisibility(View.GONE);
                                        }

                                        tvTrangThai.setText(getString(R.string.label_trang_thai, displayTrangThai));

                                        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
                                        tvTongTien.setText(getString(R.string.label_tong_tien, decimalFormat.format(tongtien[0])));

                                        if (layoutAdminControls.getVisibility() == View.VISIBLE) {
                                            // Kiểm tra nếu đơn hàng đã bị hủy
                                            boolean isCancelled = trangthai.toLowerCase().contains("hủy") ||
                                                                trangthai.toLowerCase().contains("huy") ||
                                                                displayTrangThai.toLowerCase().contains("hủy") ||
                                                                displayTrangThai.toLowerCase().contains("huy");

                                            if (isCancelled) {
                                                // Ẩn/disable các control admin nếu đơn hàng đã bị hủy
                                                if (spinnerTrangThai != null) spinnerTrangThai.setEnabled(false);
                                                if (btnCapNhatTrangThai != null) {
                                                    btnCapNhatTrangThai.setEnabled(false);
                                                    btnCapNhatTrangThai.setAlpha(0.5f);
                                                    btnCapNhatTrangThai.setText("Không thể cập nhật (Đã hủy)");
                                                }
                                            } else {
                                                // Enable lại nếu chưa bị hủy
                                                if (spinnerTrangThai != null) spinnerTrangThai.setEnabled(true);
                                                if (btnCapNhatTrangThai != null) {
                                                    btnCapNhatTrangThai.setEnabled(true);
                                                    btnCapNhatTrangThai.setAlpha(1.0f);
                                                    btnCapNhatTrangThai.setText("Cập nhật");
                                                }
                                            }

                                            int selectedIndex;
                                            try {
                                                // Server stores 1..N, spinner index is 0..N-1
                                                int serverInt = Integer.parseInt(trangthai);
                                                selectedIndex = serverInt - 1;
                                            } catch (Exception ex) {
                                                String lower = trangthai.toLowerCase();
                                                if (lower.contains("hủy") || lower.contains("huy")) selectedIndex = 4;
                                                else if (lower.contains("thành công") || lower.contains("thanh cong")) selectedIndex = 3;
                                                else if (lower.contains("giao")) selectedIndex = 2;
                                                else if (lower.contains("chuẩn bị") || lower.contains("chuan bi")) selectedIndex = 1;
                                                else selectedIndex = 0;
                                            }
                                            try {
                                                spinnerTrangThai.setSelection(Math.max(0, Math.min(selectedIndex, spinnerTrangThai.getCount()-1)));
                                            } catch (Exception ignored) {}
                                        }

                                        chiTietList.clear();
                                        chiTietList.addAll(parsed);
                                        adapter.notifyDataSetChanged();
                                    } catch (Exception ex) {
                                        Log.e(TAG, "Error updating UI after parsing", ex);
                                    } finally {
                                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                final String msg = jsonObject.optString("message", "Lỗi");
                                runOnUiThread(() -> {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ChiTietDonHangActivity.this, msg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error in background", e);
                            runOnUiThread(() -> {
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                Toast.makeText(ChiTietDonHangActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                },
                error -> {
                    Log.e(TAG, "Volley error when fetching chi tiet", error);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChiTietDonHangActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register receiver to update UI if status changed elsewhere
        orderUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                if (!Utils.ACTION_ORDER_STATUS_UPDATED.equals(intent.getAction())) return;
                String madon = intent.getStringExtra(Utils.EXTRA_MA_DONHANG);
                if (madon == null) return;
                if (!madon.equals(madonhang)) return; // only update if it's this order
                String statusText = intent.getStringExtra(Utils.EXTRA_TRANGTHAI_TEXT);
                // Determine display text: prefer provided human text, otherwise map index -> label
                String displayText = null;
                if (statusText != null) {
                    displayText = statusText;
                } else {
                    // Try int extra first (we broadcast 0-based spinner index)
                    int idx = intent.getIntExtra(Utils.EXTRA_TRANGTHAI_INDEX, Integer.MIN_VALUE);
                    if (idx != Integer.MIN_VALUE) {
                        String[] labels = new String[]{"Đang xử lý", "Đã chuẩn bị", "Đang giao", "Thành công", "Đã hủy"};
                        if (idx >= 0 && idx < labels.length) displayText = labels[idx];
                        else displayText = String.valueOf(idx);
                    } else {
                        // Fallback: maybe the sender put a string extra under the same key
                        String idxStr = intent.getStringExtra(Utils.EXTRA_TRANGTHAI_INDEX);
                        if (idxStr != null) displayText = idxStr;
                    }
                }

                final String s = displayText;
                runOnUiThread(() -> {
                    try {
                        if (tvTrangThai != null && s != null) tvTrangThai.setText(getString(R.string.label_trang_thai, s));
                    } catch (Exception ignored) {}
                });
             }
         };

        try {
            IntentFilter f = new IntentFilter(Utils.ACTION_ORDER_STATUS_UPDATED);
            LocalBroadcastManager.getInstance(this).registerReceiver(orderUpdateReceiver, f);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (orderUpdateReceiver != null) LocalBroadcastManager.getInstance(this).unregisterReceiver(orderUpdateReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (executorService != null) executorService.shutdownNow();
        } catch (Exception e) {
            Log.w(TAG, "Error shutting down executor", e);
        }
    }


    private void continueVNPayPayment() {
        if (madonhang == null || madonhang.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalAmount <= 0) {
            Toast.makeText(this, "Số tiền thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Tiếp tục thanh toán VNPay cho đơn hàng: " + madonhang);
        Log.d(TAG, "Số tiền: " + totalAmount);

        // Hiển thị progress
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Gọi API tiếp tục thanh toán VNPay (API mới dành riêng cho đơn hàng đã tồn tại)
        String url = Utils.URL_VNPAY_CONTINUE_PAYMENT;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.optBoolean("success", false);

                        if (success) {
                            String paymentUrl = jsonObject.optString("payment_url", "");

                            if (!paymentUrl.isEmpty()) {
                                Log.d(TAG, "VNPay Payment URL: " + paymentUrl);

                                // Mở link thanh toán VNPay bằng trình duyệt
                                try {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(android.net.Uri.parse(paymentUrl));
                                    startActivity(browserIntent);

                                    Toast.makeText(this, "Đang chuyển đến trang thanh toán VNPay...", Toast.LENGTH_SHORT).show();

                                    // Đóng activity sau khi mở trình duyệt để người dùng quay lại danh sách đơn hàng
                                    finish();
                                } catch (Exception e) {
                                    Log.e(TAG, "Lỗi mở link thanh toán", e);
                                    Toast.makeText(this, "Không thể mở trang thanh toán. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Không thể tạo link thanh toán", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String message = jsonObject.optString("message", "Lỗi tạo thanh toán VNPay");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "VNPay error: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        Toast.makeText(this, "Lỗi xử lý dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Volley error creating VNPay payment", error);
                    Toast.makeText(this, "Lỗi kết nối đến server thanh toán", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("madonhang", madonhang);
                params.put("tongtien", String.valueOf(totalAmount));

                return params;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Hiển thị dialog xác nhận hủy đơn hàng
     */
    private void showCancelOrderConfirmDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?\n\n" +
                           "Mã đơn hàng: " + madonhang + "\n" +
                           "Lưu ý: Đơn hàng đã hủy không thể khôi phục.")
                .setPositiveButton("Hủy đơn hàng", (dialog, which) -> {
                    // Hiển thị dialog nhập lý do hủy
                    showCancelReasonDialog();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    /**
     * Hiển thị dialog nhập lý do hủy đơn hàng
     */
    private void showCancelReasonDialog() {
        // Tạo EditText để nhập lý do
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập lý do hủy đơn (không bắt buộc)");
        input.setPadding(50, 30, 50, 30);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Lý do hủy đơn hàng")
                .setMessage("Vui lòng cho chúng tôi biết lý do bạn hủy đơn hàng để cải thiện dịch vụ:")
                .setView(input)
                .setPositiveButton("Xác nhận hủy", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reason = "Người dùng hủy đơn";
                    }
                    cancelOrder(reason);
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    /**
     * Gọi API hủy đơn hàng
     */
    private void cancelOrder(String reason) {
        if (madonhang == null || madonhang.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.user_current == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để hủy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Đang hủy đơn hàng: " + madonhang);
        Log.d(TAG, "Lý do: " + reason);

        // Hiển thị progress
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Gọi API hủy đơn hàng
        String url = Utils.URL_HUY_DON_HANG;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.optBoolean("success", false);
                        String message = jsonObject.optString("message", "");

                        if (success) {
                            Toast.makeText(this, "Đã hủy đơn hàng thành công", Toast.LENGTH_LONG).show();

                            // Ẩn nút hủy và nút thanh toán
                            if (btnCancelOrder != null) btnCancelOrder.setVisibility(View.GONE);
                            if (btnContinueVNPay != null) btnContinueVNPay.setVisibility(View.GONE);

                            // Cập nhật trạng thái hiển thị
                            tvTrangThai.setText(getString(R.string.label_trang_thai, "Đã hủy"));

                            // Broadcast để cập nhật danh sách đơn hàng
                            try {
                                Intent broadcast = new Intent(Utils.ACTION_ORDER_STATUS_UPDATED);
                                broadcast.putExtra(Utils.EXTRA_MA_DONHANG, madonhang);
                                broadcast.putExtra(Utils.EXTRA_TRANGTHAI_TEXT, "Đã hủy");
                                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
                            } catch (Exception ignored) {}

                            // Tự động đóng activity sau 2 giây
                            new android.os.Handler().postDelayed(() -> finish(), 2000);
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Volley error canceling order", error);
                    Toast.makeText(this, "Lỗi kết nối đến server: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("madonhang", madonhang);
                params.put("iduser", String.valueOf(Utils.user_current.getId()));
                params.put("lydo", reason);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        tvMaDonHang = findViewById(R.id.tvMaDonHang);
        tvNgayDat = findViewById(R.id.tvNgayDat);
        tvNgayGiao = findViewById(R.id.tvNgayGiao);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        tvSoDienThoai = findViewById(R.id.tvSoDienThoai);
        tvTongTien = findViewById(R.id.tvTongTien);
        tvTrangThai = findViewById(R.id.tvTrangThai);
        recyclerView = findViewById(R.id.recyclerViewChiTiet);
        progressBar = findViewById(R.id.progressBarLoading);

        layoutAdminControls = findViewById(R.id.layoutAdminControls);
        spinnerTrangThai = findViewById(R.id.spinnerTrangThai);
        btnCapNhatTrangThai = findViewById(R.id.btnCapNhatTrangThai);

        // Khởi tạo nút tiếp tục thanh toán VNPay
        btnContinueVNPay = findViewById(R.id.btnContinueVNPay);

        // Khởi tạo nút hủy đơn hàng
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
    }
}

