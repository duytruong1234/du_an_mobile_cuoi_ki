package vn.duytruong.appbandienthoai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.SanPhamMoiAdapter;
import vn.duytruong.appbandienthoai.model.SanPhamEvent;
import vn.duytruong.appbandienthoai.model.SanPhamMoi;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class QuanLiActivity extends AppCompatActivity {
    private ImageView img_them;
    private RecyclerView recyclerView;
    private androidx.appcompat.widget.Toolbar toolbar;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiBanHang apiBanHang;

    private List<SanPhamMoi> list;
    private SanPhamMoiAdapter spAdapter;

    private SanPhamMoi sanPhamSuaXoa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // KIỂM TRA QUYỀN ADMIN
        if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập chức năng này! Chỉ admin mới được phép.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_li);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        list = new ArrayList<>();
        EventBus.getDefault().register(this);
        initView();
        initControl();
        getSpMoi();
    }

    private void initView() {
        // Thiết lập toolbar với nút quay trở lại
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Tìm nút thêm sản phẩm
        img_them = findViewById(R.id.img_them);


        recyclerView = findViewById(R.id.recycleview_ql);
        int spanCount = 2; // Số cột, có thể điều chỉnh theo ý muốn
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        // Truyền isAdminMode = true để admin có thể click vào sản phẩm hết hàng
        spAdapter = new SanPhamMoiAdapter(getApplicationContext(), list, true);
        recyclerView.setAdapter(spAdapter);
    }

    private void initControl() {
        // Mở màn hình thêm sản phẩm khi người dùng nhấn nút
        if (img_them != null) {
            img_them.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), ThemSPActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w("QuanLiActivity", "img_them is null in initControl - cannot set listener");
        }
    }

    private void getSpMoi() {
        compositeDisposable.add(apiBanHang.getSpMoi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sanPhamMoiModel -> {
                            if (sanPhamMoiModel.isSuccess()) {
                                int oldSize = list.size();
                                list.clear();
                                list.addAll(sanPhamMoiModel.getResult());
                                if (spAdapter != null) {
                                    if (oldSize == 0) {
                                        // initial insert
                                        spAdapter.notifyItemRangeInserted(0, list.size());
                                    } else {
                                        spAdapter.notifyDataSetChanged();
                                    }
                                }
                                Log.d("QuanLiActivity", "Đã tải " + list.size() + " sản phẩm");
                            }
                        },
                        throwable -> Toast.makeText(getApplicationContext(), "Không kết nối được với server: " + throwable.getMessage(), Toast.LENGTH_LONG).show()
                ));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        CharSequence title = item.getTitle();
        if (title != null && title.equals("Sửa")) {
            suaSanPham();
        } else if (title != null && title.equals("Xóa")) {
            xoaSanPham();
        }
        return super.onContextItemSelected(item);
    }

    private void xoaSanPham() {
        if (sanPhamSuaXoa == null) {
            Toast.makeText(this, "Không có sản phẩm để xóa", Toast.LENGTH_SHORT).show();
            return;
        }
        compositeDisposable.add(apiBanHang.xoaSanPham(sanPhamSuaXoa.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(model -> {
                    Toast.makeText(getApplicationContext(), model.getMessage(), Toast.LENGTH_LONG).show();
                    getSpMoi();
                }, throwable -> Log.d("QuanLiActivity", "Error deleting product: " + (throwable.getMessage() != null ? throwable.getMessage() : "Unknown error"))));
    }

    private void suaSanPham() {
        if (sanPhamSuaXoa == null) {
            Toast.makeText(this, "Không có sản phẩm để sửa", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), ThemSPActivity.class);
        intent.putExtra("sua", sanPhamSuaXoa);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("QuanLiActivity", "onResume called - refreshing product list");
        getSpMoi();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void evenSuaXoa(SanPhamEvent event) {
        if (event != null) {
            sanPhamSuaXoa = event.getSanPhamMoi();
        }
    }
}
