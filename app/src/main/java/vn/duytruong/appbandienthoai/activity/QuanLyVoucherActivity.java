package vn.duytruong.appbandienthoai.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.VoucherAdminAdapter;
import vn.duytruong.appbandienthoai.model.Voucher;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class QuanLyVoucherActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Spinner spinnerStatus, spinnerType;
    private EditText edtSearch;
    private TextView tvTotalVouchers, tvActiveVouchers, tvTotalUsage;

    private VoucherAdminAdapter adapter;
    private List<Voucher> voucherList;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiBanHang apiBanHang;

    private String currentStatus = "all";
    private String currentType = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_voucher);

        // Khởi tạo API
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);

        // Ánh xạ view
        initView();

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Quản Lý Voucher");
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        voucherList = new ArrayList<>();
        adapter = new VoucherAdminAdapter(this, voucherList, new VoucherAdminAdapter.VoucherAdminListener() {
            @Override
            public void onEdit(Voucher voucher) {
                showEditVoucherDialog(voucher);
            }

            @Override
            public void onDelete(Voucher voucher) {
                confirmDeleteVoucher(voucher);
            }

            @Override
            public void onToggle(Voucher voucher) {
                toggleVoucher(voucher);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup Spinners
        setupSpinners();

        // Setup FAB
        fabAdd.setOnClickListener(v -> showAddVoucherDialog());

        // Setup Search
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadVouchers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load dữ liệu
        loadVouchers();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerType = findViewById(R.id.spinnerType);
        edtSearch = findViewById(R.id.edtSearch);
        tvTotalVouchers = findViewById(R.id.tvTotalVouchers);
        tvActiveVouchers = findViewById(R.id.tvActiveVouchers);
        tvTotalUsage = findViewById(R.id.tvTotalUsage);
    }

    private void setupSpinners() {
        // Spinner trạng thái
        String[] statusArray = {"Tất cả trạng thái", "Đang hoạt động", "Hết hạn", "Đã tắt"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusArray);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentStatus = "all"; break;
                    case 1: currentStatus = "active"; break;
                    case 2: currentStatus = "expired"; break;
                    case 3: currentStatus = "disabled"; break;
                }
                loadVouchers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Spinner loại
        String[] typeArray = {"Tất cả loại", "Giảm %", "Giảm cố định", "Free Ship"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeArray);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentType = "all"; break;
                    case 1: currentType = "percent"; break;
                    case 2: currentType = "fixed"; break;
                    case 3: currentType = "freeship"; break;
                }
                loadVouchers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadVouchers() {
        String search = edtSearch.getText().toString().trim();

        compositeDisposable.add(apiBanHang.getAllVouchers(currentStatus, currentType, search)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.isSuccess()) {
                                voucherList.clear();
                                voucherList.addAll(response.getVouchers());
                                adapter.notifyDataSetChanged();
                                updateStatistics();
                            } else {
                                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void updateStatistics() {
        tvTotalVouchers.setText(String.valueOf(voucherList.size()));

        int active = 0;
        int totalUsage = 0;

        for (Voucher v : voucherList) {
            if (v.getTrang_thai() == 1 && isNotExpired(v.getNgay_het_han())) {
                active++;
            }
            totalUsage += v.getDa_su_dung();
        }

        tvActiveVouchers.setText(String.valueOf(active));
        tvTotalUsage.setText(String.valueOf(totalUsage));
    }

    private boolean isNotExpired(String ngayHetHan) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date expiry = sdf.parse(ngayHetHan);
            return expiry != null && expiry.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private void showAddVoucherDialog() {
        showVoucherDialog(null);
    }

    private void showEditVoucherDialog(Voucher voucher) {
        showVoucherDialog(voucher);
    }

    private void showVoucherDialog(Voucher editVoucher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_voucher_form, null);

        // Ánh xạ các view trong dialog
        EditText edtMaVoucher = view.findViewById(R.id.edtMaVoucher);
        EditText edtTenVoucher = view.findViewById(R.id.edtTenVoucher);
        EditText edtMoTa = view.findViewById(R.id.edtMoTa);
        Spinner spinnerLoaiGiam = view.findViewById(R.id.spinnerLoaiGiam);
        EditText edtGiaTriGiam = view.findViewById(R.id.edtGiaTriGiam);
        EditText edtGiamToiDa = view.findViewById(R.id.edtGiamToiDa);
        LinearLayout layoutGiamToiDa = view.findViewById(R.id.layoutGiamToiDa);
        EditText edtDonToiThieu = view.findViewById(R.id.edtDonToiThieu);
        Spinner spinnerApDungCho = view.findViewById(R.id.spinnerApDungCho);
        EditText edtSoLuong = view.findViewById(R.id.edtSoLuong);
        EditText edtGioiHanMoiUser = view.findViewById(R.id.edtGioiHanMoiUser);
        EditText edtNgayBatDau = view.findViewById(R.id.edtNgayBatDau);
        EditText edtNgayHetHan = view.findViewById(R.id.edtNgayHetHan);
        CheckBox cbTrangThai = view.findViewById(R.id.cbTrangThai);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Setup Spinner Loại giảm
        String[] loaiGiamArray = {"Giảm %", "Giảm cố định", "Free Ship"};
        ArrayAdapter<String> loaiGiamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, loaiGiamArray);
        loaiGiamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoaiGiam.setAdapter(loaiGiamAdapter);

        spinnerLoaiGiam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutGiamToiDa.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup Spinner Áp dụng cho
        String[] apDungChoArray = {"Tất cả", "Khách hàng mới", "Khách hàng cũ", "Đơn đầu tiên"};
        ArrayAdapter<String> apDungChoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, apDungChoArray);
        apDungChoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApDungCho.setAdapter(apDungChoAdapter);

        // Setup Date Time Picker
        edtNgayBatDau.setOnClickListener(v -> showDateTimePicker(edtNgayBatDau));
        edtNgayHetHan.setOnClickListener(v -> showDateTimePicker(edtNgayHetHan));

        // Nếu edit, điền dữ liệu cũ
        if (editVoucher != null) {
            edtMaVoucher.setText(editVoucher.getMa_voucher());
            edtMaVoucher.setEnabled(false); // Không cho sửa mã
            edtTenVoucher.setText(editVoucher.getTen_voucher());
            edtMoTa.setText(editVoucher.getMo_ta());

            if ("percent".equals(editVoucher.getLoai_giam())) spinnerLoaiGiam.setSelection(0);
            else if ("fixed".equals(editVoucher.getLoai_giam())) spinnerLoaiGiam.setSelection(1);
            else spinnerLoaiGiam.setSelection(2);

            edtGiaTriGiam.setText(String.valueOf(editVoucher.getGia_tri_giam()));
            if (editVoucher.getGiam_toi_da() != null) {
                edtGiamToiDa.setText(String.valueOf(editVoucher.getGiam_toi_da()));
            }
            edtDonToiThieu.setText(String.valueOf(editVoucher.getDon_toi_thieu()));

            if ("all".equals(editVoucher.getAp_dung_cho())) spinnerApDungCho.setSelection(0);
            else if ("new_user".equals(editVoucher.getAp_dung_cho())) spinnerApDungCho.setSelection(1);
            else if ("old_user".equals(editVoucher.getAp_dung_cho())) spinnerApDungCho.setSelection(2);
            else spinnerApDungCho.setSelection(3);

            if (editVoucher.getSo_luong() != null) {
                edtSoLuong.setText(String.valueOf(editVoucher.getSo_luong()));
            }
            edtGioiHanMoiUser.setText(String.valueOf(editVoucher.getGioi_han_moi_user()));
            edtNgayBatDau.setText(editVoucher.getNgay_bat_dau());
            edtNgayHetHan.setText(editVoucher.getNgay_het_han());
            cbTrangThai.setChecked(editVoucher.getTrang_thai() == 1);
        } else {
            // Set giá trị mặc định
            edtGioiHanMoiUser.setText("1");
            cbTrangThai.setChecked(true);

            // Set ngày mặc định
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            edtNgayBatDau.setText(sdf.format(new Date()));

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 1);
            edtNgayHetHan.setText(sdf.format(cal.getTime()));
        }

        AlertDialog dialog = builder.setView(view).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            // Validate dữ liệu
            if (edtMaVoucher.getText().toString().trim().isEmpty()) {
                edtMaVoucher.setError("Nhập mã voucher");
                edtMaVoucher.requestFocus();
                return;
            }
            if (edtTenVoucher.getText().toString().trim().isEmpty()) {
                edtTenVoucher.setError("Nhập tên voucher");
                edtTenVoucher.requestFocus();
                return;
            }
            if (edtGiaTriGiam.getText().toString().trim().isEmpty()) {
                edtGiaTriGiam.setError("Nhập giá trị giảm");
                edtGiaTriGiam.requestFocus();
                return;
            }
            if (edtDonToiThieu.getText().toString().trim().isEmpty()) {
                edtDonToiThieu.setError("Nhập đơn tối thiểu");
                edtDonToiThieu.requestFocus();
                return;
            }
            if (edtGioiHanMoiUser.getText().toString().trim().isEmpty()) {
                edtGioiHanMoiUser.setError("Nhập giới hạn mỗi user");
                edtGioiHanMoiUser.requestFocus();
                return;
            }

            // Validate số
            double giaTriGiam;
            try {
                giaTriGiam = Double.parseDouble(edtGiaTriGiam.getText().toString().trim());
                if (giaTriGiam <= 0) {
                    edtGiaTriGiam.setError("Giá trị giảm phải > 0");
                    edtGiaTriGiam.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                edtGiaTriGiam.setError("Giá trị giảm phải là số");
                edtGiaTriGiam.requestFocus();
                return;
            }

            double donToiThieu;
            try {
                donToiThieu = Double.parseDouble(edtDonToiThieu.getText().toString().trim());
                if (donToiThieu < 0) {
                    edtDonToiThieu.setError("Đơn tối thiểu phải >= 0");
                    edtDonToiThieu.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                edtDonToiThieu.setError("Đơn tối thiểu phải là số");
                edtDonToiThieu.requestFocus();
                return;
            }

            int gioiHanMoiUser;
            try {
                gioiHanMoiUser = Integer.parseInt(edtGioiHanMoiUser.getText().toString().trim());
                if (gioiHanMoiUser <= 0) {
                    edtGioiHanMoiUser.setError("Giới hạn mỗi user phải > 0");
                    edtGioiHanMoiUser.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                edtGioiHanMoiUser.setError("Giới hạn mỗi user phải là số nguyên");
                edtGioiHanMoiUser.requestFocus();
                return;
            }

            // Lấy dữ liệu
            String maVoucher = edtMaVoucher.getText().toString().trim().toUpperCase();
            String tenVoucher = edtTenVoucher.getText().toString().trim();
            String moTa = edtMoTa.getText().toString().trim();

            String loaiGiam = "";
            int loaiGiamPos = spinnerLoaiGiam.getSelectedItemPosition();
            if (loaiGiamPos == 0) loaiGiam = "percent";
            else if (loaiGiamPos == 1) loaiGiam = "fixed";
            else loaiGiam = "freeship";

            // Validate giá trị giảm theo loại
            if (loaiGiam.equals("percent") && (giaTriGiam <= 0 || giaTriGiam > 100)) {
                edtGiaTriGiam.setError("Giá trị giảm % phải từ 0-100");
                edtGiaTriGiam.requestFocus();
                return;
            }

            Double giamToiDa = null;
            if (!edtGiamToiDa.getText().toString().trim().isEmpty()) {
                try {
                    giamToiDa = Double.parseDouble(edtGiamToiDa.getText().toString().trim());
                    if (giamToiDa <= 0) {
                        edtGiamToiDa.setError("Giảm tối đa phải > 0");
                        edtGiamToiDa.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    edtGiamToiDa.setError("Giảm tối đa phải là số");
                    edtGiamToiDa.requestFocus();
                    return;
                }
            }

            String apDungCho = "";
            int apDungChoPos = spinnerApDungCho.getSelectedItemPosition();
            if (apDungChoPos == 0) apDungCho = "all";
            else if (apDungChoPos == 1) apDungCho = "new_user";
            else if (apDungChoPos == 2) apDungCho = "old_user";
            else apDungCho = "first_order";

            Integer soLuong = null;
            if (!edtSoLuong.getText().toString().trim().isEmpty()) {
                try {
                    soLuong = Integer.parseInt(edtSoLuong.getText().toString().trim());
                    if (soLuong <= 0) {
                        edtSoLuong.setError("Số lượng phải > 0");
                        edtSoLuong.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    edtSoLuong.setError("Số lượng phải là số nguyên");
                    edtSoLuong.requestFocus();
                    return;
                }
            }

            String ngayBatDau = edtNgayBatDau.getText().toString();
            String ngayHetHan = edtNgayHetHan.getText().toString();
            int trangThai = cbTrangThai.isChecked() ? 1 : 0;

            // Gọi API
            if (editVoucher != null) {
                updateVoucher(editVoucher.getId(), maVoucher, tenVoucher, moTa, loaiGiam,
                        giaTriGiam, giamToiDa, donToiThieu, apDungCho, soLuong,
                        gioiHanMoiUser, ngayBatDau, ngayHetHan, trangThai);
            } else {
                addVoucher(maVoucher, tenVoucher, moTa, loaiGiam, giaTriGiam, giamToiDa,
                        donToiThieu, apDungCho, soLuong, gioiHanMoiUser, ngayBatDau,
                        ngayHetHan, trangThai);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDateTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                editText.setText(sdf.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void addVoucher(String maVoucher, String tenVoucher, String moTa, String loaiGiam,
                           double giaTriGiam, Double giamToiDa, double donToiThieu, String apDungCho,
                           Integer soLuong, int gioiHanMoiUser, String ngayBatDau, String ngayHetHan,
                           int trangThai) {
        // Kiểm tra quyền admin
        if (Utils.user_current == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.user_current.getRole() != 1) {
            Toast.makeText(this, "Bạn không có quyền thêm voucher (Role: " + Utils.user_current.getRole() + ")", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("QuanLyVoucher", "Add voucher - Role: " + Utils.user_current.getRole() + ", Email: " + Utils.user_current.getEmail());

        compositeDisposable.add(apiBanHang.addVoucher(Utils.user_current.getRole(), maVoucher, tenVoucher, moTa, loaiGiam, giaTriGiam,
                        giamToiDa, donToiThieu, apDungCho, soLuong, gioiHanMoiUser, ngayBatDau, ngayHetHan, trangThai)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                            if (response.isSuccess()) {
                                loadVouchers();
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void updateVoucher(int id, String maVoucher, String tenVoucher, String moTa, String loaiGiam,
                              double giaTriGiam, Double giamToiDa, double donToiThieu, String apDungCho,
                              Integer soLuong, int gioiHanMoiUser, String ngayBatDau, String ngayHetHan,
                              int trangThai) {
        // Kiểm tra quyền admin
        if (Utils.user_current == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.user_current.getRole() != 1) {
            Toast.makeText(this, "Bạn không có quyền sửa voucher (Role: " + Utils.user_current.getRole() + ")", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("QuanLyVoucher", "Update voucher - Role: " + Utils.user_current.getRole() + ", Email: " + Utils.user_current.getEmail());

        compositeDisposable.add(apiBanHang.updateVoucher(id, Utils.user_current.getRole(), maVoucher, tenVoucher, moTa, loaiGiam, giaTriGiam,
                        giamToiDa, donToiThieu, apDungCho, soLuong, gioiHanMoiUser, ngayBatDau, ngayHetHan, trangThai)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                            if (response.isSuccess()) {
                                loadVouchers();
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void confirmDeleteVoucher(Voucher voucher) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Xóa voucher")
                .setMessage("Bạn có chắc muốn XÓA HOÀN TOÀN voucher \"" + voucher.getMa_voucher() + "\"?\n\n" +
                        "Hành động này sẽ xóa vĩnh viễn và KHÔNG THỂ KHÔI phục!\n\n" +
                        "(Nếu chỉ muốn ẩn tạm thời, hãy dùng nút BẬT/TẮT)")
                .setPositiveButton("Xóa", (dialog, which) -> deleteVoucher(voucher, true))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteVoucher(Voucher voucher, boolean hardDelete) {
        // Kiểm tra quyền admin
        if (Utils.user_current == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Utils.user_current.getRole() != 1) {
            Toast.makeText(this, "Bạn không có quyền xóa voucher (Role: " + Utils.user_current.getRole() + ")", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("QuanLyVoucher", "Delete voucher - Role: " + Utils.user_current.getRole() + ", Email: " + Utils.user_current.getEmail());

        // Luôn xóa cứng (true) vì nút Xóa chỉ để xóa hoàn toàn, nút Tắt để xóa mềm
        compositeDisposable.add(apiBanHang.deleteVoucher(voucher.getId(), Utils.user_current.getRole(), "true")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                            if (response.isSuccess()) {
                                loadVouchers();
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void toggleVoucher(Voucher voucher) {
        compositeDisposable.add(apiBanHang.toggleVoucher(voucher.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                            if (response.isSuccess()) {
                                loadVouchers();
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}

