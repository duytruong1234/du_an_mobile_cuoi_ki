package vn.duytruong.appbandienthoai.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.DienThoaiAdapter;
import vn.duytruong.appbandienthoai.model.SanPhamMoi;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

import java.util.List;
import java.util.ArrayList;

public class DienThoaiActivity extends AppCompatActivity {
    private static final String TAG = "DienThoaiActivity";
    Toolbar toolbar;
    RecyclerView recyclerView;
    ApiBanHang apiBanHang;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    // ProgressBar progressBar; // Đã xóa - không dùng loading nữa
    TextView txtEmptyView;

    int page = 1;
    int loai;
    String tenLoai = "";
    DienThoaiAdapter adapterDt;
    List<SanPhamMoi> sanPhamMoiList;
    LinearLayoutManager linearLayoutManager;
    Handler handler = new Handler();
    boolean isLoading = false;
    boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dien_thoai);

        // Xóa cache Glide để tải lại hình ảnh mới
        new Thread(() -> {
            com.bumptech.glide.Glide.get(this).clearDiskCache();
        }).start();
        com.bumptech.glide.Glide.get(this).clearMemory();

        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        loai = getIntent().getIntExtra("loai", 1);
        tenLoai = getIntent().getStringExtra("tenloai");
        if (tenLoai == null || tenLoai.isEmpty()) {
            tenLoai = "Sản phẩm";
        }
        Log.d(TAG, "==========================================");
        Log.d(TAG, "onCreate: ID loại = " + loai + ", Tên loại = '" + tenLoai + "'");
        Log.d(TAG, "==========================================");
        AnhXa();
        ActionToolBar();

        // Handle system bars to prevent RecyclerView from being covered
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerview_dt), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
        getData(page);
        addEventLoad();
    }

    private void addEventLoad() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && !isLastPage) {
                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == sanPhamMoiList.size() - 1) {
                        isLoading = true;
                        loadMore();
                    }
                }
            }
        });
    }
    private void loadMore() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // add null
                sanPhamMoiList.add(null);
                adapterDt.notifyItemInserted(sanPhamMoiList.size() - 1);
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // remove null
                sanPhamMoiList.remove(sanPhamMoiList.size() - 1);
                adapterDt.notifyItemRemoved(sanPhamMoiList.size());
                page = page + 1;
                getData(page);
                adapterDt.notifyDataSetChanged();
                isLoading = false;
            }
        }, 2000);
    }


    private void getData(int page) {
        Log.d(TAG, "getData: page=" + page + ", loai=" + loai);

        // Không hiển thị progress bar nữa - load trực tiếp
        // if (page == 1) {
        //     progressBar.setVisibility(android.view.View.VISIBLE);
        //     recyclerView.setVisibility(android.view.View.GONE);
        //     txtEmptyView.setVisibility(android.view.View.GONE);
        // }

        compositeDisposable.add(apiBanHang.getSanPham(page, loai)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sanPhamMoiModel -> {
                            // progressBar.setVisibility(android.view.View.GONE);

                            if (sanPhamMoiModel.isSuccess()) {
                                Log.d(TAG, "getData: Success! Result size = " +
                                    (sanPhamMoiModel.getResult() != null ? sanPhamMoiModel.getResult().size() : 0));

                                if (sanPhamMoiModel.getResult() == null || sanPhamMoiModel.getResult().isEmpty()) {
                                    isLastPage = true;
                                    if (page == 1) {
                                        // Không có sản phẩm nào
                                        txtEmptyView.setText("Không có sản phẩm nào trong danh mục này");
                                        txtEmptyView.setVisibility(android.view.View.VISIBLE);
                                        recyclerView.setVisibility(android.view.View.GONE);
                                        Toast.makeText(getApplicationContext(), "Danh mục này chưa có sản phẩm", Toast.LENGTH_SHORT).show();
                                    }
                                    return;
                                }

                                // Có dữ liệu - hiển thị RecyclerView
                                recyclerView.setVisibility(android.view.View.VISIBLE);
                                txtEmptyView.setVisibility(android.view.View.GONE);

                                if (adapterDt == null) {
                                    sanPhamMoiList = sanPhamMoiModel.getResult();
                                    adapterDt = new DienThoaiAdapter(getApplicationContext(), sanPhamMoiList);
                                    recyclerView.setAdapter(adapterDt);
                                } else {
                                    int vitri = sanPhamMoiList.size() - 1;
                                    int soluongadd = sanPhamMoiModel.getResult().size();
                                    for (int i = 0; i < soluongadd; i++) {
                                        sanPhamMoiList.add(sanPhamMoiModel.getResult().get(i));
                                    }
                                    adapterDt.notifyItemRangeInserted(vitri, soluongadd);
                                }
                            } else {
                                Log.e(TAG, "getData: Failed - " + sanPhamMoiModel.getMessage());
                                if (page == 1) {
                                    txtEmptyView.setText("Không tìm thấy sản phẩm");
                                    txtEmptyView.setVisibility(android.view.View.VISIBLE);
                                    recyclerView.setVisibility(android.view.View.GONE);
                                }
                                Toast.makeText(getApplicationContext(), sanPhamMoiModel.getMessage(), Toast.LENGTH_SHORT).show();
                                isLastPage = true;
                            }
                        },
                        throwable -> {
                            // Đã xóa progressBar - không hiển thị loading nữa
                            Log.e(TAG, "getData: Error", throwable);
                            if (page == 1) {
                                txtEmptyView.setText("Lỗi kết nối server");
                                txtEmptyView.setVisibility(android.view.View.VISIBLE);
                                recyclerView.setVisibility(android.view.View.GONE);
                            }
                            Toast.makeText(getApplicationContext(), "Không kết nối server", Toast.LENGTH_LONG).show();
                        }
                ));
    }


    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void AnhXa() {
        toolbar = findViewById(R.id.toolbar_dt);
        recyclerView = findViewById(R.id.recyclerview_dt);
        // progressBar = findViewById(R.id.progressBar); // Đã xóa
        txtEmptyView = findViewById(R.id.txtEmptyView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        sanPhamMoiList = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
