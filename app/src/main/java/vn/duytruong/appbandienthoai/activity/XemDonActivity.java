package vn.duytruong.appbandienthoai.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.DonHangAdapter;
import vn.duytruong.appbandienthoai.model.DonHang;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class XemDonActivity extends AppCompatActivity {
    private static final String TAG = "XemDonActivity";
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DonHangAdapter adapter;
    private List<DonHang> donHangList;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiBanHang apiBanHang;
    private RequestQueue requestQueue;
    private BroadcastReceiver orderUpdateReceiver;

    // Admin toggle controls
    private android.widget.LinearLayout layoutAdminToggle;
    private android.widget.Button btnMyOrders;
    private android.widget.Button btnAllOrders;
    private boolean viewingAllOrders = false; // false = đơn của tôi, true = tất cả đơn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xem_don);

        initView();
        initControl();
        getDonHang();

        // Xử lý deep link từ VNPay return
        handleVNPayReturn(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Xử lý deep link khi activity đã tồn tại (singleTop mode)
        handleVNPayReturn(intent);
    }


    private void handleVNPayReturn(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        android.net.Uri data = intent.getData();
        if (data == null) return;

        // Kiểm tra scheme và host
        String scheme = data.getScheme();
        String host = data.getHost();

        if ("appbandienthoai".equals(scheme) && "payment_return".equals(host)) {
            String status = data.getQueryParameter("status");
            String madonhang = data.getQueryParameter("madonhang");

            Log.d(TAG, "VNPay return - Status: " + status + ", Order: " + madonhang);

            if ("success".equals(status)) {
                // Thanh toán thành công
                Toast.makeText(this, "Thanh toán thành công! Mã đơn hàng: " + madonhang, Toast.LENGTH_LONG).show();

                // Refresh danh sách đơn hàng
                getDonHang();
            } else if ("failed".equals(status)) {
                // Thanh toán thất bại
                String code = data.getQueryParameter("code");
                Toast.makeText(this, "Thanh toán thất bại! Mã lỗi: " + code, Toast.LENGTH_LONG).show();
            } else if ("error".equals(status)) {
                // Lỗi hệ thống
                String message = data.getQueryParameter("message");
                Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        orderUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                if (!Utils.ACTION_ORDER_STATUS_UPDATED.equals(intent.getAction())) return;
                String madon = intent.getStringExtra(Utils.EXTRA_MA_DONHANG);
                if (madon == null || madon.isEmpty()) return;
                String statusText = intent.getStringExtra(Utils.EXTRA_TRANGTHAI_TEXT);
                // If no text, try index
                if (statusText == null) {
                    int idx = intent.getIntExtra(Utils.EXTRA_TRANGTHAI_INDEX, -1);
                    if (idx >=0 && idx < Utils.STATUS_LABELS.length) statusText = Utils.STATUS_LABELS[idx];
                }

                final String finalStatus = statusText;
                 // find the order in the list and update
                 runOnUiThread(() -> {
                     try {
                         if (donHangList == null || adapter == null) return;
                         for (int i = 0; i < donHangList.size(); i++) {
                             DonHang d = donHangList.get(i);
                             if (d != null && d.getMadonhang() != null && d.getMadonhang().equals(madon)) {
                                if (finalStatus != null) d.setTrangthai(Utils.formatTrangThai(finalStatus));
                                 adapter.notifyItemChanged(i);
                                 break;
                             }
                         }
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

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewDonHang);

        // Admin toggle controls
        layoutAdminToggle = findViewById(R.id.layoutAdminToggle);
        btnMyOrders = findViewById(R.id.btnMyOrders);
        btnAllOrders = findViewById(R.id.btnAllOrders);

        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        donHangList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    private void initControl() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đơn hàng của tôi");
        }
        toolbar.setNavigationOnClickListener(v -> {
            // SỬA: Về màn hình chính thay vì finish() để tránh quay lại trang PayPal
            Intent intent = new Intent(XemDonActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Ensure RecyclerView can adapt to item height changes
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        // Attach a detailed adapter so each item shows the full order card and opens ChiTietDonHangActivity on click
        boolean isAdmin = (Utils.user_current != null && Utils.user_current.isAdmin());
        adapter = new DonHangAdapter(this, donHangList, isAdmin);
        recyclerView.setAdapter(adapter);

        // Hiển thị nút toggle chỉ cho admin
        if (isAdmin) {
            layoutAdminToggle.setVisibility(android.view.View.VISIBLE);

            // Xử lý nút "Đơn của tôi"
            btnMyOrders.setOnClickListener(v -> {
                viewingAllOrders = false;
                updateToggleButtons();
                getDonHang();
            });

            // Xử lý nút "Tất cả đơn hàng"
            btnAllOrders.setOnClickListener(v -> {
                viewingAllOrders = true;
                updateToggleButtons();
                getDonHang();
            });

            updateToggleButtons();
        } else {
            layoutAdminToggle.setVisibility(android.view.View.GONE);
        }
    }

    private void updateToggleButtons() {
        if (viewingAllOrders) {
            // Đang xem tất cả đơn
            btnMyOrders.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCCCCCC));
            btnMyOrders.setTextColor(0xFF666666);
            btnAllOrders.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF36DD07));
            btnAllOrders.setTextColor(0xFFFFFFFF);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Tất cả đơn hàng");
            }
        } else {
            // Đang xem đơn của tôi
            btnMyOrders.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF36DD07));
            btnMyOrders.setTextColor(0xFFFFFFFF);
            btnAllOrders.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCCCCCC));
            btnAllOrders.setTextColor(0xFF666666);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Đơn hàng của tôi");
            }
        }
    }

    private void getDonHang() {
        Log.d(TAG, "Starting getDonHang for user=" + (Utils.user_current!=null?Utils.user_current.getId():"null"));
        if (Utils.user_current == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Call xemDonHang with both required fields: iduser and isadmin (1 if admin else 0)
        int isAdminFlag = (Utils.user_current != null && Utils.user_current.isAdmin()) ? 1 : 0;
        String viewMode = viewingAllOrders ? "all" : "my";

        compositeDisposable.add(apiBanHang.xemDonHang(Utils.user_current.getId(), isAdminFlag, viewMode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        donHangModel -> {
                            if (donHangModel != null && donHangModel.isSuccess() && donHangModel.getResult() != null) {
                                donHangList.clear();
                                donHangList.addAll(donHangModel.getResult());
                                // Normalize trangthai values to human-readable labels
                                try {
                                    for (DonHang dh : donHangList) {
                                        if (dh != null) dh.setTrangthai(Utils.formatTrangThai(dh.getTrangthai()));
                                    }
                                } catch (Exception ignored) {}
                                // notify the current adapter (could be SimpleOrderAdapter)
                                if (recyclerView.getAdapter() != null) recyclerView.getAdapter().notifyDataSetChanged();
                                Log.d(TAG, "Loaded " + donHangList.size() + " orders via Retrofit");
                                // Log order ids for debugging
                                StringBuilder ids = new StringBuilder();
                                for (DonHang d : donHangList) {
                                    ids.append(d.getId()).append(",");
                                }
                                Log.d(TAG, "Order IDs: " + ids.toString());
                                Toast.makeText(this, "Đã tải " + donHangList.size() + " đơn hàng", Toast.LENGTH_SHORT).show();

                                // Update adapter contents instead of replacing with debug adapter
                                if (recyclerView.getAdapter() != null) recyclerView.getAdapter().notifyDataSetChanged();
                                // Bring recycler to front and refresh layout to avoid potential overlap
                                recyclerView.bringToFront();
                                recyclerView.requestLayout();
                                recyclerView.invalidate();
                                if (donHangList.size() > 0) recyclerView.scrollToPosition(0);

                                // If Retrofit returned empty for some reason, fall back to Volley GET
                                if (donHangList.size() == 0) {
                                    Log.w(TAG, "Retrofit returned 0 orders, falling back to Volley");
                                    fetchOrdersWithVolley();
                                }
                            } else {
                                // Try fallback if Retrofit response is not as expected
                                Log.w(TAG, "Retrofit call did not return expected data, falling back to Volley");
                                fetchOrdersWithVolley();
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "Error loading orders via Retrofit", throwable);
                            Toast.makeText(this, "Lỗi tải đơn hàng, thử lại", Toast.LENGTH_SHORT).show();
                            // fallback
                            fetchOrdersWithVolley();
                        }
                ));
    }

    // Fallback using Volley to call getDonHang.php which returns JSON in 'data' key
    private void fetchOrdersWithVolley() {
        try {
            String url = Utils.URL_GET_DON_HANG + "?iduser=" + Utils.user_current.getId();
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            JSONObject jo = new JSONObject(response);
                            if (jo.optBoolean("success", false)) {
                                JSONArray arr = jo.optJSONArray("data");
                                if (arr != null) {
                                    donHangList.clear();
                                    for (int i = 0; i < arr.length(); i++) {
                                        JSONObject item = arr.getJSONObject(i);
                                        DonHang d = new DonHang();
                                        d.setId(Integer.parseInt(item.optString("id", "0")));
                                        d.setMadonhang(item.optString("madonhang", ""));
                                        d.setDiachi(item.optString("diachi", ""));
                                        d.setSodienthoai(item.optString("sodienthoai", ""));
                                        d.setSoluong(Integer.parseInt(item.optString("soluong", "0")));
                                        d.setTongtien(item.optString("tongtien", "0"));
                                        d.setNgaydat(item.optString("ngaydat", ""));
                                        d.setNgaygiaodukien(item.optString("ngaygiaodukien", ""));
                                        d.setTrangthai(Utils.formatTrangThai(item.optString("trangthai", "")));
                                        donHangList.add(d);
                                    }
                                    if (recyclerView.getAdapter() != null) recyclerView.getAdapter().notifyDataSetChanged();
                                    Log.d(TAG, "Loaded " + donHangList.size() + " orders via Volley fallback");
                                    Toast.makeText(this, "Đã tải " + donHangList.size() + " đơn hàng (từ máy chủ)", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Volley response JSON 'data' array is null");
                                }
                            } else {
                                Log.e(TAG, "Volley response JSON success flag is false");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException while parsing Volley response", e);
                        } catch (Exception e) {
                            Log.e(TAG, "Unexpected error while processing Volley response", e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Volley error while fetching orders", error);
                        Toast.makeText(this, "Lỗi kết nối, không thể tải đơn hàng", Toast.LENGTH_SHORT).show();
                    });

            request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                    5000, // Timeout
                    3, // Max retries
                    1.0f // Backoff multiplier
            ));

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Volley request", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    public void onBackPressed() {
        // SỬA: Khi nhấn nút Back vật lý, về màn hình chính thay vì quay lại trang PayPal
        Intent intent = new Intent(XemDonActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
