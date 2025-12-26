package vn.duytruong.appbandienthoai.activity;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.TonKhoAdapter;
import vn.duytruong.appbandienthoai.model.TonKhoItem;
import vn.duytruong.appbandienthoai.utils.Utils;

public class TonKhoActivity extends AppCompatActivity {
    private static final String TAG = "TonKhoActivity";
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TonKhoAdapter adapter;
    private List<TonKhoItem> list;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Try to set the normal layout; if inflate fails due to missing class, fall back to a safe layout
        try {
            setContentView(R.layout.activity_ton_kho);
        } catch (Throwable t) {
            // Catch InflateException, ClassNotFoundException, NoClassDefFoundError, etc.
            Log.w(TAG, "Failed to inflate activity_ton_kho, falling back to safe layout", t);
            int layoutId = getResources().getIdentifier("activity_ton_kho_fallback", "layout", getPackageName());
            if (layoutId != 0) {
                setContentView(layoutId);
                Log.w(TAG, "Used fallback layout activity_ton_kho_fallback");
            } else {
                // If fallback layout is also missing, try original approach in file (keep existing fallback), else finish
                Log.e(TAG, "Fallback layout activity_ton_kho_fallback not found; finishing activity");
                finish();
                return;
            }
        }

        // Resolve view IDs at runtime to avoid NoSuchFieldError when R.id fields are missing in dex
        int toolbarId = getResources().getIdentifier("toolbar_ton_kho", "id", getPackageName());
        if (toolbarId != 0) {
            toolbar = findViewById(toolbarId);
        } else {
            toolbar = null;
            Log.w(TAG, "toolbar_ton_kho id not found in resources");
        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        } else {
            Log.w(TAG, "toolbar_ton_kho not found in layout");
        }

        int recyclerId = getResources().getIdentifier("recyclerViewTonKho", "id", getPackageName());
        if (recyclerId != 0) {
            recyclerView = findViewById(recyclerId);
        } else {
            recyclerView = null;
            Log.e(TAG, "recyclerViewTonKho id not found in resources");
        }

        // swipeRefresh may be absent in fallback layout; resolve safely
        int swipeId = getResources().getIdentifier("swipe_refresh", "id", getPackageName());
        if (swipeId != 0) {
            try {
                swipeRefresh = findViewById(swipeId);
            } catch (Throwable ignore) {
                swipeRefresh = null;
            }
        } else {
            swipeRefresh = null;
        }

        if (recyclerView == null) {
            Log.e(TAG, "recyclerViewTonKho not found - finishing activity");
            finish();
            return;
        }

        list = new ArrayList<>();
        adapter = new TonKhoAdapter(this, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadData);
        }

        // handle item clicks for editing stock
        adapter.setOnItemClickListener((item, position) -> showEditStockDialog(item, position));

        // initial load
        setSwipeRefreshing(true);
        loadData();
    }

    private void setSwipeRefreshing(final boolean refreshing) {
        try {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(refreshing);
            else if (refreshing) {
                // optional: provide alternative UX, such as a toast when refresh requested but not available
                Log.d(TAG, "SwipeRefresh not available; requested refreshing=" + refreshing);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error setting swipe refreshing", t);
        }
    }

    private void showEditStockDialog(TonKhoItem item, int position) {
        // Hiển thị dialog chọn hành động
        new AlertDialog.Builder(this)
                .setTitle("Quản lý tồn kho")
                .setMessage("Sản phẩm: " + item.getTensp() + "\nTồn kho hiện tại: " + item.getSoluongtonkho())
                .setPositiveButton("Cộng thêm", (dialog, which) -> showAddStockDialog(item, position))
                .setNegativeButton("Đặt lại", (dialog, which) -> showSetStockDialog(item, position))
                .setNeutralButton("Hủy", null)
                .show();
    }

    private void showAddStockDialog(TonKhoItem item, int position) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập số lượng cộng thêm");

        new AlertDialog.Builder(this)
                .setTitle("Cộng thêm tồn kho")
                .setMessage("Sản phẩm: " + item.getTensp() + "\nTồn kho hiện tại: " + item.getSoluongtonkho())
                .setView(input)
                .setPositiveButton("Cộng thêm", (dialog, which) -> {
                    String s = input.getText().toString().trim();
                    if (s.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int addQty;
                    try { addQty = Integer.parseInt(s); } catch (Exception e) {
                        Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (addQty <= 0) {
                        Toast.makeText(this, "Số lượng phải > 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    setSwipeRefreshing(true);
                    sendCapNhatTonKhoRequest(item.getId(), addQty, position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSetStockDialog(TonKhoItem item, int position) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.getSoluongtonkho()));
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Đặt lại tồn kho")
                .setMessage("Sản phẩm: " + item.getTensp())
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String s = input.getText().toString().trim();
                    int newQty;
                    try { newQty = Integer.parseInt(s); } catch (Exception e) {
                        Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newQty < 0) {
                        Toast.makeText(this, "Số lượng phải >= 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    setSwipeRefreshing(true);
                    sendSetTonKhoRequest(item.getId(), newQty, position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendCapNhatTonKhoRequest(int idsp, int soluong, int position) {
        String url = Utils.URL_CAP_NHAT_TON_KHO;
        StringRequest post = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jo = new JSONObject(response);
                        boolean success = jo.optBoolean("success", false);
                        String message = jo.optString("message", "");
                        if (success) {
                            int newStock = jo.optInt("soluongtonkho", 0);
                            // update model and UI
                            TonKhoItem t = list.get(position);
                            t.setSoluongtonkho(newStock);
                            adapter.notifyItemChanged(position);
                            Toast.makeText(this, "Cộng thêm tồn kho thành công! Tồn kho mới: " + newStock, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Phản hồi không hợp lệ", Toast.LENGTH_SHORT).show();
                    } finally {
                        setSwipeRefreshing(false);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error capNhatTonKho", error);
                    Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    setSwipeRefreshing(false);
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> p = new HashMap<>();
                p.put("idsp", String.valueOf(idsp));
                p.put("soluong", String.valueOf(soluong));
                return p;
            }
        };

        requestQueue.add(post);
    }

    private void sendSetTonKhoRequest(int idsp, int soluong, int position) {
        String url = Utils.URL_SET_TON_KHO;
        StringRequest post = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jo = new JSONObject(response);
                        boolean success = jo.optBoolean("success", false);
                        String message = jo.optString("message", "");
                        if (success) {
                            int newStock = jo.optInt("soluongtonkho", soluong);
                            // update model and UI
                            TonKhoItem t = list.get(position);
                            t.setSoluongtonkho(newStock);
                            adapter.notifyItemChanged(position);
                            Toast.makeText(this, "Cập nhật tồn kho thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Phản hồi không hợp lệ", Toast.LENGTH_SHORT).show();
                    } finally {
                        setSwipeRefreshing(false);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error setTonKho", error);
                    Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    setSwipeRefreshing(false);
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> p = new HashMap<>();
                p.put("idsp", String.valueOf(idsp));
                p.put("soluong", String.valueOf(soluong));
                return p;
            }
        };

        requestQueue.add(post);
    }

    private void loadData() {
        // Use the inventory-check endpoint as requested (kiemTraTonKho.php)
        String url = Utils.URL_KIEM_TRA_TON_KHO;
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jo = new JSONObject(response);
                        if (jo.optBoolean("success", false)) {
                            // 'data' may be an array (list) or an object (single product)
                            Object data = jo.opt("data");
                            list.clear();

                            if (data instanceof JSONArray) {
                                JSONArray arr = (JSONArray) data;
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject it = arr.getJSONObject(i);
                                    TonKhoItem item = new TonKhoItem();
                                    item.setId(it.optInt("id", 0));
                                    item.setTensp(it.optString("tensp", ""));
                                    item.setHinhanh(it.optString("hinhanh", ""));
                                    item.setGiasp(it.optString("giasp", "0"));
                                    item.setSoluongtonkho(it.optInt("soluongtonkho", 0));
                                    item.setLoaisanpham(it.optString("loaisanpham", ""));
                                    item.setTinhtrang(it.optString("tinhtrang", ""));
                                    list.add(item);
                                }
                            } else if (data instanceof JSONObject) {
                                JSONObject it = (JSONObject) data;
                                TonKhoItem item = new TonKhoItem();
                                item.setId(it.optInt("id", 0));
                                item.setTensp(it.optString("tensp", ""));
                                item.setHinhanh(it.optString("hinhanh", ""));
                                item.setGiasp(it.optString("giasp", "0"));
                                item.setSoluongtonkho(it.optInt("soluongtonkho", 0));
                                // Some endpoints use different field names (mota vs loaisanpham/tinhtrang). Try both.
                                item.setLoaisanpham(it.optString("loaisanpham", it.optString("mota", "")));
                                item.setTinhtrang(it.optString("tinhtrang", ""));
                                list.add(item);
                            } else {
                                // Unexpected data shape: try to be tolerant and treat as empty
                                Log.w(TAG, "Unexpected data type from inventory API: " + (data == null ? "null" : data.getClass()));
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(TonKhoActivity.this, jo.optString("message", "Lỗi"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parse error", e);
                        Toast.makeText(TonKhoActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    } finally {
                        setSwipeRefreshing(false);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error", error);
                    Toast.makeText(TonKhoActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    setSwipeRefreshing(false);
                });

        requestQueue.add(req);
    }
}
