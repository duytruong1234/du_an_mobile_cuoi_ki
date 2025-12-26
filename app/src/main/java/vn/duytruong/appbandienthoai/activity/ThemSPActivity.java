package vn.duytruong.appbandienthoai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.databinding.ActivityThemspBinding;
import vn.duytruong.appbandienthoai.model.SanPhamMoi;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;
import android.util.Log;
import android.view.ViewGroup;
import androidx.appcompat.widget.Toolbar;

public class ThemSPActivity extends AppCompatActivity {
    private static final String TAG = "ThemSPActivity";
    Spinner spinner;
    int loai = 0;

    // local reference to initial stock input (avoid binding.soluongtonkho which may not be present in generated binding)
    private TextInputEditText edtSoluong;

    ApiBanHang apiBanHang;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    ActivityThemspBinding binding;

    SanPhamMoi sanPhamSua;

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // KIỂM TRA QUYỀN ADMIN
        if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập chức năng n��y! Chỉ admin mới được phép.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        binding = ActivityThemspBinding.inflate(getLayoutInflater());
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        setContentView(binding.getRoot());
        // Setup Toolbar as ActionBar and enable Up navigation (simple, no manual inset handling)
        try {
            setSupportActionBar(binding.toobar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            // Ensure a navigation icon is present (some themes may not show it automatically)
            try {
                binding.toobar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            } catch (Throwable ignored) {
                try { binding.toobar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel); } catch (Throwable ignored2) {}
            }
            // Ensure nav icon is visible and accessible
            try {
                if (binding.toobar.getNavigationIcon() != null) {
                    binding.toobar.getNavigationIcon().setTint(getResources().getColor(android.R.color.white));
                }
            } catch (Throwable ignored) {}
            binding.toobar.setNavigationContentDescription("Quay lại");
            binding.toobar.setNavigationOnClickListener(view -> finish());
        } catch (Throwable t) {
            Log.w(TAG, "Toolbar setup failed", t);
        }
        initView();
        initData();
        Intent intent = getIntent();
        sanPhamSua = (SanPhamMoi) intent.getSerializableExtra("sua");
        if (sanPhamSua == null) {
            // them moi
            flag = false;
        } else {
            // sua
            flag = true;
            binding.btnthem.setText("Sửa sản phẩm");
            // populate fields
            binding.tensp.setText(sanPhamSua.getTensp());
            binding.giasp.setText(sanPhamSua.getGiasp());
            String hinhAnh = sanPhamSua.getHinhanh();
            if (hinhAnh != null && !hinhAnh.isEmpty()) {
                // Xử lý trường hợp URL bị trùng lặp, lấy phần cuối cùng bắt đầu bằng http
                int lastHttp = hinhAnh.lastIndexOf("http://");
                if (lastHttp > 0) {
                    hinhAnh = hinhAnh.substring(lastHttp);
                } else if (hinhAnh.lastIndexOf("https://") > 0) {
                    int lastHttps = hinhAnh.lastIndexOf("https://");
                    hinhAnh = hinhAnh.substring(lastHttps);
                }
            }
            binding.hinhanh.setText(hinhAnh);
            binding.mota.setText(sanPhamSua.getMota());
            // populate stock if the view exists
            try {
                if (edtSoluong != null) {
                    edtSoluong.setText(String.valueOf(sanPhamSua.getSoluongtonkho()));
                }
            } catch (Throwable ignored) {}
            // set spinner
            loai = sanPhamSua.getLoai();
            spinner.setSelection(loai);
        }
    }
    private void editProduct() {
        String str_ten = textOf(binding.tensp);
        String str_gia = textOf(binding.giasp);
        String str_mota = textOf(binding.mota);
        String str_hinhanh = textOf(binding.hinhanh);

        // Lấy số lượng tồn kho từ edtSoluong
        if (edtSoluong == null) {
            Toast.makeText(this, "Lỗi: không tìm thấy ô 'Số lượng tồn kho'. Không thể sửa.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "editProduct: edtSoluong is null");
            return;
        }
        String str_soluong = textOf(edtSoluong);
        Log.e(TAG, "EDIT_DEBUG_SOLUONG = '" + str_soluong + "'");
        int int_soluongtonkho = 0;
        if (!TextUtils.isEmpty(str_soluong)) {
            try {
                int_soluongtonkho = Integer.parseInt(str_soluong.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                int_soluongtonkho = 0;
            }
            if (int_soluongtonkho < 0) int_soluongtonkho = 0;
        }
        Log.e(TAG, "EDIT_PARSED_SOLUONG = " + int_soluongtonkho);

        if (TextUtils.isEmpty(str_ten) || TextUtils.isEmpty(str_gia) || TextUtils.isEmpty(str_mota) || TextUtils.isEmpty(str_hinhanh) || loai == 0) {
            Toast.makeText(getApplicationContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_LONG).show();
        } else {
            compositeDisposable.add(apiBanHang.update(str_ten, str_gia, str_hinhanh, str_mota, sanPhamSua.getId(), loai, int_soluongtonkho, int_soluongtonkho)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(messageModel -> {
                        if (messageModel.isSuccess()) {
                            Toast.makeText(getApplicationContext(), messageModel.getMessage(), Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), messageModel.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }, throwable -> {
                        Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }));
        }
    }

    private void initData() {
        List<String> stringList = new ArrayList<>();
        stringList.add("Vui lòng chọn loại");
        stringList.add("Loại 1");
        stringList.add("Loại 2");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stringList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loai = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        binding.btnthem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flag) {
                    addProduct();
                } else {
                    editProduct();
                }
            }
        });

    }

    // small helper to safely read text from a TextInputEditText to avoid NPE warnings
    private String textOf(TextInputEditText et) {
        if (et == null) return "";
        CharSequence cs = et.getText();
        return (cs == null) ? "" : cs.toString().trim();
    }

    private void initView() {
        spinner = findViewById(R.id.spinner_loai);
        // Avoid referencing generated binding field (binding.soluongtonkho) directly because
        // if the R field does not exist at runtime it causes NoSuchFieldError.
        // Instead, resolve the id at runtime using getIdentifier and fall back to a hint-based search.
        edtSoluong = null;
        try {
            int idRes = getResources().getIdentifier("soluongtonkho", "id", getPackageName());
            Log.d(TAG, "initView: getIdentifier('soluongtonkho') returned idRes=" + idRes);
            if (idRes != 0) {
                View found = binding.getRoot().findViewById(idRes);
                if (found instanceof TextInputEditText) {
                    edtSoluong = (TextInputEditText) found;
                    Log.d(TAG, "initView: found soluongtonkho by id");
                } else if (found != null) {
                    Log.d(TAG, "initView: view with id exists but is not TextInputEditText: " + found.getClass().getName());
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "initView: exception while using getIdentifier/finding view", t);
        }
        if (edtSoluong == null) {
            // fallback: search the view hierarchy for a TextInputEditText with matching hint
            try {
                edtSoluong = findTextInputByHint(binding.getRoot(), "Số lượng tồn kho");
                if (edtSoluong != null) {
                    Log.d(TAG, "initView: found soluongtonkho by hint fallback");
                } else {
                    Log.w(TAG, "initView: soluongtonkho not found (will default to null and send 0)");
                }
            } catch (Throwable t) {
                Log.w(TAG, "initView: exception while searching by hint", t);
            }
        }
    }

    // Recursively search a view hierarchy for a TextInputEditText whose hint matches the given text.
    private TextInputEditText findTextInputByHint(View root, String hint) {
        if (root instanceof TextInputEditText) {
            CharSequence h = ((TextInputEditText) root).getHint();
            if (h != null && h.toString().trim().equalsIgnoreCase(hint.trim())) return (TextInputEditText) root;
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                TextInputEditText found = findTextInputByHint(child, hint);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void addProduct() {
        String str_ten = textOf(binding.tensp);
        String str_gia = textOf(binding.giasp);
        String str_mota = textOf(binding.mota);
        String str_hinhanh = textOf(binding.hinhanh);

        // Lấy số lượng tồn kho
        String str_soluong = "0";
        if (edtSoluong != null) {
            try {
                str_soluong = textOf(edtSoluong);
            } catch (Exception ignored) {}
        }

        // Debug logs to verify what the app reads from the EditText before sending to server
        android.util.Log.e(TAG, "DEBUG_SOLUONG = '" + str_soluong + "'");

        int int_soluongtonkho = 0;
        if (!TextUtils.isEmpty(str_soluong)) {
            try {
                int_soluongtonkho = Integer.parseInt(str_soluong.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                int_soluongtonkho = 0;
            }
            if (int_soluongtonkho < 0) int_soluongtonkho = 0;
        }

        android.util.Log.e(TAG, "PARSED_SOLUONG = " + int_soluongtonkho);

        if (TextUtils.isEmpty(str_ten) || TextUtils.isEmpty(str_gia) ||
                TextUtils.isEmpty(str_mota) || TextUtils.isEmpty(str_hinhanh) || loai == 0) {

            Toast.makeText(getApplicationContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_LONG).show();
            return;
        }

        //  Kiểm tra giá trị gửi
        Log.d(TAG, "addProduct: tensp=" + str_ten + ", gia=" + str_gia +
                ", loai=" + loai + ", soluong=" + int_soluongtonkho);
        Toast.makeText(this, "Gửi số lượng tồn kho: " + int_soluongtonkho, Toast.LENGTH_SHORT).show();

        compositeDisposable.add(apiBanHang.insertSp(
                        str_ten,
                        str_gia,
                        str_hinhanh,
                        str_mota,
                        loai,
                        int_soluongtonkho,  // soluongtonkho
                        int_soluongtonkho   // soluong (duplicate for compatibility)
                 )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        messageModel -> {
                            if (messageModel.isSuccess()) {
                                Toast.makeText(getApplicationContext(), messageModel.getMessage(), Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), messageModel.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "insertSp throwable", throwable);
                            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ));
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
