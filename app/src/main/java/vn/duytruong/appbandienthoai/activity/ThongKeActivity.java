package vn.duytruong.appbandienthoai.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.ThongKe;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

/**
 * ThongKeActivity - Hiển thị biểu đồ thống kê sản phẩm bán chạy cho Admin
 */
public class ThongKeActivity extends AppCompatActivity {
    private static final String TAG = "ThongKeActivity";

    private Toolbar toolbar;
    private BarChart barChart;
    private ApiBanHang apiBanHang;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra quyền admin
        if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
            Toast.makeText(this, "Chỉ admin mới có quyền xem thống kê!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_thong_ke);

        initView();
        initControl();
        loadThongKe();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        barChart = findViewById(R.id.barChart);
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
    }

    private void initControl() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thống kê sản phẩm bán chạy");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Cấu hình biểu đồ
        setupBarChart();
    }

    private void setupBarChart() {
        // Cấu hình Description
        Description description = new Description();
        description.setText("Top 10 sản phẩm bán chạy nhất");
        description.setTextSize(12f);
        description.setTextColor(Color.BLACK);
        barChart.setDescription(description);

        // Cấu hình chung
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setExtraBottomOffset(10f);
        barChart.setExtraTopOffset(20f);

        // Cấu hình Legend
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);

        // Cấu hình trục X (tên sản phẩm)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);

        // Cấu hình trục Y bên trái (số lượng)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Tắt trục Y bên phải
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void loadThongKe() {
        Log.d(TAG, "Đang tải dữ liệu thống kê...");

        compositeDisposable.add(apiBanHang.getthongke()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        thongKeModel -> {
                            if (thongKeModel.isSuccess() && thongKeModel.getResult() != null) {
                                Log.d(TAG, "Tải thống kê thành công: " + thongKeModel.getResult().size() + " sản phẩm");
                                displayChart(thongKeModel.getResult());
                            } else {
                                Toast.makeText(this, "Không có dữ liệu thống kê", Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "Lỗi tải thống kê: " + throwable.getMessage());
                            Toast.makeText(this, "Lỗi kết nối: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ));
    }

    private void displayChart(List<ThongKe> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            Toast.makeText(this, "Chưa có dữ liệu để hiển thị", Toast.LENGTH_SHORT).show();
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

        // Tạo gradient màu đẹp mắt
        int[] colors = new int[]{
                Color.rgb(64, 89, 128),   // Xanh đậm
                Color.rgb(149, 165, 124), // Xanh lá nhạt
                Color.rgb(217, 184, 162), // Be
                Color.rgb(191, 134, 134), // Hồng đậm
                Color.rgb(179, 48, 80)    // Đỏ
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Tạo BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        // Gán labels cho trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        // Hiển thị biểu đồ
        barChart.setData(barData);
        barChart.animateY(1000);
        barChart.invalidate();

        Log.d(TAG, "Đã hiển thị biểu đồ với " + entries.size() + " sản phẩm");
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
