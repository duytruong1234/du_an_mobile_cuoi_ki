package vn.duytruong.appbandienthoai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.VoucherUserAdapter;
import vn.duytruong.appbandienthoai.model.Voucher;
import vn.duytruong.appbandienthoai.model.VoucherCheckResponse;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;


public class ChonVoucherActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private EditText edtMaVoucher;
    private Button btnApDung;
    private TextView tvTongTien, tvSoVoucherApDung, tvNoVoucher;
    private LinearLayout layoutVoucherApplicable, layoutVoucherNotApplicable;
    private RecyclerView recyclerViewApplicable, recyclerViewNotApplicable;
    private ProgressBar progressBar;

    // Adapters
    private VoucherUserAdapter adapterApplicable;
    private VoucherUserAdapter adapterNotApplicable;
    private List<Voucher> voucherListApplicable = new ArrayList<>();
    private List<Voucher> voucherListNotApplicable = new ArrayList<>();

    // API
    private ApiBanHang apiBanHang;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Data từ màn hình trước
    private int userId;
    private double tongTien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_voucher);

        // Nhận dữ liệu từ Intent
        userId = getIntent().getIntExtra("user_id", 0);
        tongTien = getIntent().getDoubleExtra("tong_tien", 0);

        // Khởi tạo API
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);

        // Ánh xạ views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerViews();

        // Setup buttons
        setupListeners();

        // Hiển thị tổng tiền
        updateTongTien();

        // Load danh sách voucher
        loadVouchers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        edtMaVoucher = findViewById(R.id.edtMaVoucher);
        btnApDung = findViewById(R.id.btnApDung);
        tvTongTien = findViewById(R.id.tvTongTien);
        tvSoVoucherApDung = findViewById(R.id.tvSoVoucherApDung);
        tvNoVoucher = findViewById(R.id.tvNoVoucher);
        layoutVoucherApplicable = findViewById(R.id.layoutVoucherApplicable);
        layoutVoucherNotApplicable = findViewById(R.id.layoutVoucherNotApplicable);
        recyclerViewApplicable = findViewById(R.id.recyclerViewApplicable);
        recyclerViewNotApplicable = findViewById(R.id.recyclerViewNotApplicable);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chọn mã giảm giá");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        // RecyclerView cho voucher có thể dùng
        adapterApplicable = new VoucherUserAdapter(this, voucherListApplicable, true, voucher -> {
            // Khi user chọn voucher, trả về kết quả
            returnVoucherResult(voucher);
        });
        recyclerViewApplicable.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewApplicable.setAdapter(adapterApplicable);

        // RecyclerView cho voucher không thể dùng
        adapterNotApplicable = new VoucherUserAdapter(this, voucherListNotApplicable, false, null);
        recyclerViewNotApplicable.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotApplicable.setAdapter(adapterNotApplicable);
    }

    private void setupListeners() {
        // Nút áp dụng mã thủ công
        btnApDung.setOnClickListener(v -> {
            String maVoucher = edtMaVoucher.getText().toString().trim().toUpperCase();
            if (TextUtils.isEmpty(maVoucher)) {
                Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
                return;
            }
            checkVoucherManually(maVoucher);
        });
    }

    private void updateTongTien() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvTongTien.setText("Tổng tiền: " + formatter.format(tongTien) + " ₫");
    }

    /**
     * Load danh sách voucher từ API
     */
    private void loadVouchers() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoVoucher.setVisibility(View.GONE);
        layoutVoucherApplicable.setVisibility(View.GONE);
        layoutVoucherNotApplicable.setVisibility(View.GONE);

        compositeDisposable.add(apiBanHang.getVouchers(userId, tongTien)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccess()) {
                                handleVoucherResponse(response);
                            } else {
                                tvNoVoucher.setVisibility(View.VISIBLE);
                                tvNoVoucher.setText(response.getMessage());
                            }
                        },
                        throwable -> {
                            progressBar.setVisibility(View.GONE);
                            tvNoVoucher.setVisibility(View.VISIBLE);
                            tvNoVoucher.setText("Lỗi kết nối: " + throwable.getMessage());
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    /**
     * Xử lý kết quả trả về từ API getVouchers
     */
    private void handleVoucherResponse(vn.duytruong.appbandienthoai.model.VoucherListResponse response) {
        // Voucher có thể dùng
        if (response.getVouchers_applicable() != null && !response.getVouchers_applicable().isEmpty()) {
            voucherListApplicable.clear();
            voucherListApplicable.addAll(response.getVouchers_applicable());
            adapterApplicable.notifyDataSetChanged();
            layoutVoucherApplicable.setVisibility(View.VISIBLE);

            tvSoVoucherApDung.setText(voucherListApplicable.size() + " voucher có thể dùng");
        } else {
            layoutVoucherApplicable.setVisibility(View.GONE);
        }

        // Voucher chưa đủ điều kiện
        if (response.getVouchers_not_applicable() != null && !response.getVouchers_not_applicable().isEmpty()) {
            voucherListNotApplicable.clear();
            voucherListNotApplicable.addAll(response.getVouchers_not_applicable());
            adapterNotApplicable.notifyDataSetChanged();
            layoutVoucherNotApplicable.setVisibility(View.VISIBLE);
        } else {
            layoutVoucherNotApplicable.setVisibility(View.GONE);
        }

        // Không có voucher nào
        if (voucherListApplicable.isEmpty() && voucherListNotApplicable.isEmpty()) {
            tvNoVoucher.setVisibility(View.VISIBLE);
            tvNoVoucher.setText("Không có voucher khả dụng");
        }
    }

    /**
     * Kiểm tra voucher khi user nhập mã thủ công
     */
    private void checkVoucherManually(String maVoucher) {
        progressBar.setVisibility(View.VISIBLE);

        compositeDisposable.add(apiBanHang.checkVoucher(maVoucher, userId, tongTien)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccess()) {
                                // Tạo object Voucher từ response
                                Voucher voucher = response.getVoucher();
                                if (voucher != null) {
                                    // Thêm thông tin tính toán
                                    voucher.setCo_the_dung(true);
                                    returnVoucherResult(voucher);
                                } else {
                                    Toast.makeText(this, "Lỗi: Không nhận được thông tin voucher", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
                                edtMaVoucher.setError(response.getMessage());
                            }
                        },
                        throwable -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    /**
     * Trả về kết quả voucher đã chọn
     */
    private void returnVoucherResult(Voucher voucher) {
        Intent intent = new Intent();
        intent.putExtra("voucher_id", voucher.getId());
        intent.putExtra("ma_voucher", voucher.getMa_voucher());
        intent.putExtra("ten_voucher", voucher.getTen_voucher());
        intent.putExtra("loai_giam", voucher.getLoai_giam());
        intent.putExtra("gia_tri_giam_voucher", voucher.getGia_tri_giam());

        // Gửi kèm thông tin để tính toán
        intent.putExtra("tong_truoc_giam", tongTien);

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}

