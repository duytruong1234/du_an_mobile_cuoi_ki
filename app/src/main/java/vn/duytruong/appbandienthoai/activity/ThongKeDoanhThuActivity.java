package vn.duytruong.appbandienthoai.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.ThongKe;
import vn.duytruong.appbandienthoai.model.ThongKeDoanhThu;
import vn.duytruong.appbandienthoai.model.TongQuanDoanhThu;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

/**
 * ThongKeDoanhThuActivity - Hiển thị biểu đồ thống kê doanh thu cho Admin
 */
public class ThongKeDoanhThuActivity extends AppCompatActivity {
    private static final String TAG = "ThongKeDoanhThu";

    private Toolbar toolbar;
    private BarChart barChartDoanhThu;
    private BarChart barChartSanPham;
    private TextView tvTongDoanhThu, tvTongDonHang, tvTongKhachHang;
    private Spinner spinnerYear;
    private ApiBanHang apiBanHang;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private int selectedYear;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra quyền admin
        if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
            Toast.makeText(this, "Chỉ admin mới có quyền xem thống kê doanh thu!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_thong_ke_doanh_thu);

        initView();
        initControl();
        setupYearSpinner();
        loadThongKeSanPham();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        barChartDoanhThu = findViewById(R.id.barChartDoanhThu);
        barChartSanPham = findViewById(R.id.barChartSanPham);
        tvTongDoanhThu = findViewById(R.id.tvTongDoanhThu);
        tvTongDonHang = findViewById(R.id.tvTongDonHang);
        tvTongKhachHang = findViewById(R.id.tvTongKhachHang);
        spinnerYear = findViewById(R.id.spinnerYear);
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);

        selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    }

    private void initControl() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thống kê tổng hợp");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Cấu hình biểu đồ
        setupBarChart();
        setupBarChartSanPham();
    }

    private void setupYearSpinner() {
        // Tạo danh sách năm từ 2020 đến năm hiện tại
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int year = currentYear; year >= 2020; year--) {
            years.add(String.valueOf(year));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(years.get(position));
                loadThongKeDoanhThu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupBarChart() {
        // Cấu hình Description
        Description description = new Description();
        description.setText("Doanh thu theo tháng (VNĐ)");
        description.setTextSize(10f);
        description.setTextColor(Color.GRAY);
        barChartDoanhThu.setDescription(description);

        // Cấu hình chung
        barChartDoanhThu.setDrawGridBackground(false);
        barChartDoanhThu.setDrawBarShadow(false);
        barChartDoanhThu.setDrawValueAboveBar(true);
        barChartDoanhThu.setPinchZoom(true);
        barChartDoanhThu.setScaleEnabled(true);
        barChartDoanhThu.setDragEnabled(true);
        barChartDoanhThu.setExtraBottomOffset(15f);
        barChartDoanhThu.setExtraTopOffset(30f);
        barChartDoanhThu.setExtraLeftOffset(10f);
        barChartDoanhThu.setExtraRightOffset(10f);
        barChartDoanhThu.setFitBars(true);

        // Cấu hình Legend
        Legend legend = barChartDoanhThu.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(11f);
        legend.setYOffset(10f);

        // Cấu hình trục X (tháng)
        XAxis xAxis = barChartDoanhThu.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelRotationAngle(0f);
        xAxis.setLabelCount(12);
        xAxis.setYOffset(5f);

        // Cấu hình trục Y bên trái (doanh thu)
        YAxis leftAxis = barChartDoanhThu.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(9f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return decimalFormat.format(value / 1000000) + "M";
                } else if (value >= 1000) {
                    return decimalFormat.format(value / 1000) + "K";
                }
                return decimalFormat.format(value);
            }
        });

        // Tắt trục Y bên phải
        YAxis rightAxis = barChartDoanhThu.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void loadThongKeDoanhThu() {
        Log.d(TAG, "Đang tải dữ liệu thống kê doanh thu năm " + selectedYear);

        compositeDisposable.add(apiBanHang.getThongKeDoanhThu("month", selectedYear)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.isSuccess() && response.getResult() != null) {
                                Log.d(TAG, "Tải thống kê doanh thu thành công: " + response.getResult().size() + " tháng");
                                displayTongQuan(response.getTongquan());
                                displayChart(response.getResult());
                            } else {
                                Toast.makeText(this, "Không có dữ liệu thống kê", Toast.LENGTH_SHORT).show();
                                clearData();
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "Lỗi tải thống kê: " + throwable.getMessage());
                            Toast.makeText(this, "Lỗi kết nối: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            clearData();
                        }
                ));
    }

    private void displayTongQuan(TongQuanDoanhThu tongQuan) {
        if (tongQuan != null) {
            tvTongDoanhThu.setText(decimalFormat.format(tongQuan.getTong_doanhthu()) + "đ");
            tvTongDonHang.setText(String.valueOf(tongQuan.getTong_donhang()));
            tvTongKhachHang.setText(String.valueOf(tongQuan.getTong_khachhang()));
        }
    }

    private void displayChart(List<ThongKeDoanhThu> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            Toast.makeText(this, "Chưa có dữ liệu để hiển thị", Toast.LENGTH_SHORT).show();
            barChartDoanhThu.clear();
            return;
        }

        // Tạo mảng 12 tháng với giá trị mặc định là 0
        float[] doanhThuThang = new float[12];
        for (ThongKeDoanhThu item : dataList) {
            int thang = item.getThang() - 1; // Array index từ 0-11
            if (thang >= 0 && thang < 12) {
                doanhThuThang[thang] = (float) item.getDoanhthu();
            }
        }

        // Tạo danh sách BarEntry
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, doanhThuThang[i]));
            labels.add("T" + (i + 1));
        }

        // Tạo BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (VNĐ)");

        // Gradient màu xanh đến cam
        int[] colors = new int[]{
                Color.rgb(33, 150, 243),   // Xanh
                Color.rgb(3, 169, 244),
                Color.rgb(0, 188, 212),
                Color.rgb(0, 150, 136),
                Color.rgb(76, 175, 80),    // Xanh lá
                Color.rgb(139, 195, 74),
                Color.rgb(205, 220, 57),
                Color.rgb(255, 235, 59),   // Vàng
                Color.rgb(255, 193, 7),
                Color.rgb(255, 152, 0),    // Cam
                Color.rgb(255, 87, 34),
                Color.rgb(244, 67, 54)     // Đỏ
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(8f);
        dataSet.setValueTextColor(Color.rgb(66, 66, 66));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "";
                if (value >= 1000000) {
                    return decimalFormat.format(value / 1000000) + "M";
                } else if (value >= 1000) {
                    return decimalFormat.format(value / 1000) + "K";
                }
                return decimalFormat.format(value);
            }
        });

        // Tạo BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        // Cấu hình trục X với labels
        XAxis xAxis = barChartDoanhThu.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(12);

        // Hiển thị dữ liệu
        barChartDoanhThu.setData(barData);
        barChartDoanhThu.animateY(1000);
        barChartDoanhThu.invalidate();
    }

    private void clearData() {
        tvTongDoanhThu.setText("0đ");
        tvTongDonHang.setText("0");
        tvTongKhachHang.setText("0");
        barChartDoanhThu.clear();
    }

    private void setupBarChartSanPham() {
        // Cấu hình Description
        Description description = new Description();
        description.setText("Top 10 sản phẩm bán chạy nhất");
        description.setTextSize(10f);
        description.setTextColor(Color.GRAY);
        barChartSanPham.setDescription(description);

        // Cấu hình chung
        barChartSanPham.setDrawGridBackground(false);
        barChartSanPham.setDrawBarShadow(false);
        barChartSanPham.setDrawValueAboveBar(true);
        barChartSanPham.setPinchZoom(true);
        barChartSanPham.setScaleEnabled(true);
        barChartSanPham.setDragEnabled(true);
        barChartSanPham.setExtraBottomOffset(60f); // Tăng để hiển thị label xoay
        barChartSanPham.setExtraTopOffset(30f);
        barChartSanPham.setExtraLeftOffset(10f);
        barChartSanPham.setExtraRightOffset(10f);
        barChartSanPham.setFitBars(true);

        // Cấu hình Legend
        Legend legend = barChartSanPham.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(11f);
        legend.setYOffset(10f);

        // Cấu hình trục X (tên sản phẩm)
        XAxis xAxis = barChartSanPham.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setYOffset(5f);

        // Cấu hình trục Y bên trái (số lượng)
        YAxis leftAxis = barChartSanPham.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(9f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Tắt trục Y bên phải
        YAxis rightAxis = barChartSanPham.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void loadThongKeSanPham() {
        Log.d(TAG, "Đang tải dữ liệu sản phẩm bán chạy...");

        compositeDisposable.add(apiBanHang.getthongke()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        thongKeModel -> {
                            if (thongKeModel.isSuccess() && thongKeModel.getResult() != null) {
                                Log.d(TAG, "Tải sản phẩm bán chạy thành công: " + thongKeModel.getResult().size() + " sản phẩm");
                                displayChartSanPham(thongKeModel.getResult());
                            } else {
                                Toast.makeText(this, "Không có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "Lỗi tải sản phẩm bán chạy: " + throwable.getMessage());
                        }
                ));
    }

    private void displayChartSanPham(List<ThongKe> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            barChartSanPham.clear();
            return;
        }

        // Tạo danh sách BarEntry
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            ThongKe item = dataList.get(i);
            entries.add(new BarEntry(i, item.getTong()));

            // Rút gọn tên sản phẩm nếu quá dài
            String tenSP = item.getTensp();
            if (tenSP.length() > 15) {
                tenSP = tenSP.substring(0, 12) + "...";
            }
            labels.add(tenSP);
        }

        // Tạo BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, "Số lượng đã bán");

        // Tạo gradient màu đẹp mắt (màu từ xanh sang đỏ)
        int[] colors = new int[]{
                Color.rgb(64, 89, 128),   // Xanh đậm
                Color.rgb(82, 109, 130),
                Color.rgb(100, 129, 132),
                Color.rgb(118, 149, 134),
                Color.rgb(149, 165, 124), // Xanh lá nhạt
                Color.rgb(183, 174, 143),
                Color.rgb(217, 184, 162), // Be
                Color.rgb(204, 159, 148),
                Color.rgb(191, 134, 134), // Hồng đậm
                Color.rgb(179, 48, 80)    // Đỏ
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(Color.rgb(66, 66, 66));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Tạo BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        // Gán labels cho trục X
        XAxis xAxis = barChartSanPham.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        // Hiển thị biểu đồ
        barChartSanPham.setData(barData);
        barChartSanPham.animateY(1200);
        barChartSanPham.invalidate();

        Log.d(TAG, "Đã hiển thị biểu đồ sản phẩm với " + entries.size() + " sản phẩm");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

