package vn.duytruong.appbandienthoai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.GioHangAdapter;
import vn.duytruong.appbandienthoai.model.EventBus.TinhTongEvent;
import vn.duytruong.appbandienthoai.model.GioHang;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;


public class GioHangActivity extends AppCompatActivity {
    LinearLayout giohangtrong;
    TextView tongtien;
    Toolbar toolbar;
    RecyclerView recyclerView;
    Button btnmuahang;
    Button btnXoaToanBo;
    GioHangAdapter adapter;

    long tongtiensp;
    List<GioHang> gioHangList;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    ApiBanHang apiBanHang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gio_hang);

        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);

        // Clear danh sách mua hàng cũ để bắt đầu tính toán mới
        if (Utils.mangmuahang != null) {
            Utils.mangmuahang.clear();
        } else {
            Utils.mangmuahang = new ArrayList<>();
        }

        initView();
        initControl();

        // Load giỏ hàng từ server khi mở app
        loadGioHangFromServer();

        tinhTongTien();

        // Handle system bars to prevent button from being covered
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnmuahang), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
    }
    private void tinhTongTien() {
        // Khởi tạo danh sách mua hàng nếu chưa có
        if (Utils.mangmuahang == null) {
            Utils.mangmuahang = new ArrayList<>();
        }

        // Tính t���ng tiền của các sản phẩm đã check
        if (Utils.mangmuahang.isEmpty()) {
            tongtiensp = 0;
            tongtien.setText("0");
            return;
        }

        long tongtienSp = 0;
        for (int i = 0; i < Utils.mangmuahang.size(); i++) {
            tongtienSp = tongtienSp + (Utils.mangmuahang.get(i).getGiasp() * Utils.mangmuahang.get(i).getSoluong());
        }

        // Gán giá trị tổng vào trường lớp để truyền sang Activity khác
        tongtiensp = tongtienSp;

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        tongtien.setText(decimalFormat.format(tongtienSp) + " ₫");
    }

    private void initControl() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Quay về trang chủ và clear toàn bộ stack
                Intent intent = new Intent(GioHangActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Khởi tạo giỏ hàng nếu chưa có
        if (Utils.manggiohang == null) {
            Utils.manggiohang = new ArrayList<>();
            tongtiensp = 0; // đảm bảo luôn có giá trị mặc định
        }

        if (Utils.manggiohang.isEmpty()) {
            giohangtrong.setVisibility(View.VISIBLE);
        } else {
            adapter = new GioHangAdapter(getApplicationContext(), Utils.manggiohang);
            recyclerView.setAdapter(adapter);
        }
        btnmuahang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.user_current == null) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                } else {
                    // Khởi tạo danh sách mua hàng nếu chưa có
                    if (Utils.mangmuahang == null) {
                        Utils.mangmuahang = new ArrayList<>();
                    }

                    // Kiểm tra xem có sản phẩm nào được chọn không
                    if (Utils.mangmuahang.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Vui lòng chọn sản phẩm cần mua", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Tính tổng tiền dựa trên sản phẩm đã check (mangmuahang)
                    long tong = 0;
                    for (GioHang gh : Utils.mangmuahang) {
                        tong += gh.getGiasp() * gh.getSoluong();
                    }

                    if (tong == 0) {
                        Toast.makeText(getApplicationContext(), "Giá sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ KHÔNG XÓA Ở ĐÂY - Chỉ xóa sau khi đặt hàng thành công
                    // Xóa sẽ được thực hiện trong DatHangActivity sau khi thanh toán thành công

                    // Chuyển sang trang thanh toán
                    Intent intent = new Intent(getApplicationContext(), DatHangActivity.class);
                    intent.putExtra("tongtien", tong);
                    startActivity(intent);
                }
            }
        });
        // ✅ Xử lý nút xóa toàn bộ giỏ hàng
        btnXoaToanBo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xoaToanBoGioHang();
            }
        });



    }


    private void initView() {
        giohangtrong = findViewById(R.id.txtgiohangtrong);
        tongtien = findViewById(R.id.txttongtien);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycleviewgiohang);
        btnXoaToanBo = findViewById(R.id.btnXoaToanBo);
        btnmuahang = findViewById(R.id.btnmuahang);
    }
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void eventTinhTien(TinhTongEvent event) {
        if (event != null) {
            tinhTongTien();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        tinhTongTien();
        checkCartStatus();
    }

    private void checkCartStatus() {
        // Khởi tạo giỏ hàng nếu chưa có
        if (Utils.manggiohang == null) {
            Utils.manggiohang = new ArrayList<>();
        }

        if (Utils.manggiohang.isEmpty()) {
            giohangtrong.setVisibility(View.VISIBLE);
            if (adapter != null) {
                recyclerView.setAdapter(null);
            }
        } else {
            giohangtrong.setVisibility(View.GONE);
            if (adapter == null) {
                adapter = new GioHangAdapter(getApplicationContext(), Utils.manggiohang);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Load giỏ hàng từ server khi mở app
     */
    private void loadGioHangFromServer() {
        // Chỉ load nếu user đã đăng nhập
        if (Utils.user_current == null) {
            android.util.Log.d("GioHangActivity", "User chưa đăng nhập, không load giỏ hàng từ server");
            return;
        }

        android.util.Log.d("GioHangActivity", "Bắt đầu load giỏ hàng từ server cho user: " + Utils.user_current.getId());

        compositeDisposable.add(apiBanHang.getGioHang(
                Utils.user_current.getId()
        ).subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(
             response -> {
                 android.util.Log.d("GioHangActivity", "Response từ server: " + response.getMessage());

                 if (response.isSuccess()) {
                     try {
                         Object resultObj = response.getResult();

                         if (resultObj == null) {
                             android.util.Log.d("GioHangActivity", "Result is null - giỏ hàng trống");
                             return;
                         }

                         // Convert Object to JSON string using Gson
                         String resultJson = new com.google.gson.Gson().toJson(resultObj);
                         android.util.Log.d("GioHangActivity", "Result JSON: " + resultJson);

                         // Parse JSON array
                         org.json.JSONArray jsonArray = new org.json.JSONArray(resultJson);
                         android.util.Log.d("GioHangActivity", " Số sản phẩm từ server: " + jsonArray.length());

                         if (jsonArray.length() == 0) {
                             android.util.Log.d("GioHangActivity", "Giỏ hàng trống");
                             return;
                         }

                         // Khởi tạo giỏ hàng nếu chưa có
                         if (Utils.manggiohang == null) {
                             Utils.manggiohang = new ArrayList<>();
                         }

                         // Clear giỏ hàng cũ
                         Utils.manggiohang.clear();

                         // Parse từng sản phẩm
                         for (int i = 0; i < jsonArray.length(); i++) {
                             org.json.JSONObject item = jsonArray.getJSONObject(i);

                             GioHang gioHang = new GioHang();
                             gioHang.setIdsp(item.getInt("idsp"));
                             gioHang.setTensp(item.getString("tensp"));
                             gioHang.setGiasp(item.getLong("giasp"));
                             gioHang.setHinhsp(item.getString("hinhsp"));
                             gioHang.setSoluong(item.getInt("soluong"));

                             Utils.manggiohang.add(gioHang);

                             android.util.Log.d("GioHangActivity", "✅ Đã load: " + gioHang.getTensp() + " (x" + gioHang.getSoluong() + ")");
                         }

                         // Cập nhật UI
                         checkCartStatus();
                         tinhTongTien();

                         Toast.makeText(this, " Đã tải " + jsonArray.length() + " sản phẩm", Toast.LENGTH_SHORT).show();

                     } catch (Exception e) {
                         android.util.Log.e("GioHangActivity", " Lỗi parse: " + e.getMessage());
                         e.printStackTrace();
                         Toast.makeText(this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                     }
                 } else {
                     android.util.Log.w("GioHangActivity", " Server error: " + response.getMessage());
                 }
             },
             throwable -> {
                 android.util.Log.e("GioHangActivity", " Lỗi load giỏ hàng: " + throwable.getMessage());
                 throwable.printStackTrace();
             }
         ));
    }

    /**
     * Xóa toàn bộ giỏ hàng với dialog xác nhận
     */
    private void xoaToanBoGioHang() {
        // Kiểm tra giỏ hàng có rỗng không
        if (Utils.manggiohang == null || Utils.manggiohang.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog xác nhận
        new android.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa toàn bộ " + Utils.manggiohang.size() + " sản phẩm trong giỏ hàng?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                // Lưu số lượng để hiển thị
                int soLuongXoa = Utils.manggiohang.size();

                // Xóa local
                Utils.manggiohang.clear();
                if (Utils.mangmuahang != null) {
                    Utils.mangmuahang.clear();
                }

                // Cập nhật UI
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                checkCartStatus();
                tinhTongTien();

                // Xóa trên server nếu user đã đăng nhập
                if (Utils.user_current != null && apiBanHang != null) {
                    compositeDisposable.add(apiBanHang.xoaToanBoGioHang(
                            Utils.user_current.getId()
                    ).subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe(
                         response -> {
                             if (response.isSuccess()) {
                                 android.util.Log.d("GioHangActivity", " Đã xóa giỏ hàng trên server");
                             }
                         },
                         throwable -> {
                             android.util.Log.e("GioHangActivity", "Lỗi xóa server: " + throwable.getMessage());
                         }
                     ));
                }

                Toast.makeText(this, "Đã xóa " + soLuongXoa + " sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

}
