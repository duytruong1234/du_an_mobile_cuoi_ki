package vn.duytruong.appbandienthoai.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.DienThoaiAdapter;
import vn.duytruong.appbandienthoai.model.SanPhamMoi;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class SearchActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    DienThoaiAdapter adapterDt;
    EditText edtsearch;
    Button btnSearchSortPriceDesc, btnSearchSortPriceAsc;
    List<SanPhamMoi> sanPhamMoiList;
    ApiBanHang apiBanHang;
    CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        // Xóa cache Glide để tải lại hình ảnh mới
        new Thread(() -> {
            com.bumptech.glide.Glide.get(this).clearDiskCache();
        }).start();
        com.bumptech.glide.Glide.get(this).clearMemory();

        initView();
        ActionToolBar();

        // Tải tất cả sản phẩm khi mở trang
        getAllProducts();
        setupFilterButtons();

        // Handle system bars to prevent RecyclerView from being covered
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recycleview_search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
    }

    private void initView() {
        sanPhamMoiList = new ArrayList<>();
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        edtsearch = findViewById(R.id.edtsearch);
        toolbar = findViewById(R.id.toobar);
        recyclerView = findViewById(R.id.recycleview_search);
        btnSearchSortPriceDesc = findViewById(R.id.btnSearchSortPriceDesc);
        btnSearchSortPriceAsc = findViewById(R.id.btnSearchSortPriceAsc);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    // Khi xóa hết text, hiển thị lại tất cả sản phẩm
                    getAllProducts();
                } else {
                    getDataSearch(charSequence.toString());
                }
            }


            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void getDataSearch(String s) {
        compositeDisposable.add(apiBanHang.search(s)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sanPhamMoiModel -> {
                            if (sanPhamMoiModel.isSuccess()) {
                                sanPhamMoiList.clear();
                                sanPhamMoiList.addAll(sanPhamMoiModel.getResult());
                                // Always set a fresh adapter to avoid broad notify warning
                                adapterDt = new DienThoaiAdapter(getApplicationContext(), sanPhamMoiList);
                                recyclerView.setAdapter(adapterDt);
                            }
                        },
                        throwable -> Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show()
                )
        );
    }

    private void getAllProducts() {
        compositeDisposable.add(apiBanHang.getSpMoi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sanPhamMoiModel -> {
                            if (sanPhamMoiModel.isSuccess()) {
                                sanPhamMoiList.clear();
                                sanPhamMoiList.addAll(sanPhamMoiModel.getResult());
                                // Always set a fresh adapter to avoid broad notify warning
                                adapterDt = new DienThoaiAdapter(getApplicationContext(), sanPhamMoiList);
                                recyclerView.setAdapter(adapterDt);
                            }
                        },
                        throwable -> Toast.makeText(getApplicationContext(), "Lỗi tải sản phẩm: " + throwable.getMessage(), Toast.LENGTH_SHORT).show()
                )
        );
    }

    private void setupFilterButtons() {
        // Nút lọc giá cao đến thấp
        btnSearchSortPriceDesc.setOnClickListener(v -> {
            sortProductsByPrice(false); // false = giảm dần
            Toast.makeText(SearchActivity.this, "Đã lọc: Giá cao → thấp", Toast.LENGTH_SHORT).show();
        });

        // Nút lọc giá thấp đến cao
        btnSearchSortPriceAsc.setOnClickListener(v -> {
            sortProductsByPrice(true); // true = tăng dần
            Toast.makeText(SearchActivity.this, "Đã lọc: Giá thấp → cao", Toast.LENGTH_SHORT).show();
        });
    }

    private void sortProductsByPrice(boolean ascending) {
        if (sanPhamMoiList == null || sanPhamMoiList.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm để lọc", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("SearchActivity", "Trước khi lọc - số lượng: " + sanPhamMoiList.size());

        // In ra giá của 3 sản phẩm đầu tiên trước khi lọc
        for (int i = 0; i < Math.min(3, sanPhamMoiList.size()); i++) {
            SanPhamMoi sp = sanPhamMoiList.get(i);
            if (sp != null) {
                android.util.Log.d("SearchActivity", "Trước lọc [" + i + "]: " + sp.getTensp() + " - Giá: " + sp.getGiasp());
            }
        }

        // Loại bỏ các phần tử null trước khi sắp xếp
        sanPhamMoiList.removeAll(java.util.Collections.singleton(null));

        // Sắp xếp danh sách theo giá - use List.sort with lambda
        sanPhamMoiList.sort((sp1, sp2) -> {
            if (sp1 == null && sp2 == null) return 0;
            if (sp1 == null) return 1;
            if (sp2 == null) return -1;

            try {
                String price1Str = sp1.getGiasp();
                String price2Str = sp2.getGiasp();

                if (price1Str == null || price1Str.isEmpty()) return 1;
                if (price2Str == null || price2Str.isEmpty()) return -1;

                double price1 = Double.parseDouble(price1Str);
                double price2 = Double.parseDouble(price2Str);

                return ascending ? Double.compare(price1, price2) : Double.compare(price2, price1);
            } catch (NumberFormatException e) {
                android.util.Log.e("SearchActivity", "Lỗi parse giá: " + e.getMessage());
                return 0;
            }
        });

        // In ra giá của 3 sản phẩm đầu tiên sau khi lọc
        android.util.Log.d("SearchActivity", "Sau khi lọc (" + (ascending ? "tăng dần" : "giảm dần") + "):");
        for (int i = 0; i < Math.min(3, sanPhamMoiList.size()); i++) {
            SanPhamMoi sp = sanPhamMoiList.get(i);
            if (sp != null) {
                android.util.Log.d("SearchActivity", "Sau lọc [" + i + "]: " + sp.getTensp() + " - Giá: " + sp.getGiasp());
            }
        }

        // Cập nhật adapter bằng adapter mới
        adapterDt = new DienThoaiAdapter(getApplicationContext(), sanPhamMoiList);
        recyclerView.setAdapter(adapterDt);
        android.util.Log.d("SearchActivity", "Đã cập nhật adapter mới");
    }

    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

}
