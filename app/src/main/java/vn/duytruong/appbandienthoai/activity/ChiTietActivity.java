package vn.duytruong.appbandienthoai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.nex3z.notificationbadge.NotificationBadge;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.GioHang;
import vn.duytruong.appbandienthoai.model.SanPhamMoi;
import vn.duytruong.appbandienthoai.model.TonKhoResponse;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class ChiTietActivity extends AppCompatActivity {
    TextView tensp, giasp, mota, txtTonKho;
    Button btnthem;
    FrameLayout btnCong, btnTru;
    ImageView imghinhanh;
    TextView txtSoLuong;
    Toolbar toolbar;
    SanPhamMoi sanPhamMoi;
    NotificationBadge notificationBadge;
    int soLuong = 1;
    int tonKhoHienTai = 0;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    ApiBanHang apiBanHang;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet);
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        initView();
        initData();
        ActionToolBar();
        initControl();
    }

    private void initControl() {
        btnthem.setOnClickListener(view -> themGioHang());

        // Nút trừ số lượng
        btnTru.setOnClickListener(view -> {
            if (soLuong > 1) {
                soLuong--;
                txtSoLuong.setText(String.valueOf(soLuong));
            } else {
                Toast.makeText(ChiTietActivity.this, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            }
        });

        btnCong.setOnClickListener(view -> {
            if (soLuong < 99) {
                soLuong++;
                txtSoLuong.setText(String.valueOf(soLuong));
            } else {
                Toast.makeText(ChiTietActivity.this, "Số lượng tối đa là 99", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void themGioHang() {
        if (sanPhamMoi == null) return;
        if (tonKhoHienTai <= 0) {
            Toast.makeText(this, "Sản phẩm hiện đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        int soLuongDaCo = 0;
        if (Utils.manggiohang != null) {
            for (GioHang item : Utils.manggiohang) {
                if (item.getIdsp() == sanPhamMoi.getId()) {
                    soLuongDaCo = item.getSoluong();
                    break;
                }
            }
        }
        if (soLuongDaCo + soLuong > tonKhoHienTai) {
            Toast.makeText(this, "Không đủ hàng trong kho. Còn lại: " + (tonKhoHienTai - soLuongDaCo) + " sản phẩm", Toast.LENGTH_LONG).show();
            return;
        }
        if (Utils.manggiohang == null) {
            Utils.manggiohang = new ArrayList<>();
        }

        if (!Utils.manggiohang.isEmpty()) {
            boolean flag = false;
            for (int i = 0; i < Utils.manggiohang.size(); i++) {
                if (Utils.manggiohang.get(i).getIdsp() == sanPhamMoi.getId()) {
                    Utils.manggiohang.get(i).setSoluong(soLuong + Utils.manggiohang.get(i).getSoluong());
                    flag = true;
                }
            }
            if (!flag) {
                long giaDonVi;
                try {
                    String raw = sanPhamMoi.getGiasp() == null ? "" : sanPhamMoi.getGiasp();
                    double price = Double.parseDouble(raw.trim());
                    giaDonVi = (long) price;
                    Log.d("ChiTietActivity", "Add to cart - Raw price: '" + raw + "' -> Final: " + giaDonVi);
                } catch (Exception ex) {
                    giaDonVi = 0;
                    Log.w("ChiTietActivity", "Không thể parse giá khi thêm giỏ hàng, fallback=0 (raw='" + sanPhamMoi.getGiasp() + "')", ex);
                }
                GioHang gioHang = new GioHang();
                gioHang.setGiasp(giaDonVi);
                gioHang.setSoluong(soLuong);
                gioHang.setIdsp(sanPhamMoi.getId());
                gioHang.setTensp(sanPhamMoi.getTensp());
                gioHang.setHinhsp(sanPhamMoi.getHinhanh());
                Utils.manggiohang.add(gioHang);
            }
        } else {
            long giaDonVi;
            try {
                String raw = sanPhamMoi.getGiasp() == null ? "" : sanPhamMoi.getGiasp();
                double price = Double.parseDouble(raw.trim());
                giaDonVi = (long) price;
                Log.d("ChiTietActivity", "Add to cart (empty list) - Raw price: '" + raw + "' -> Final: " + giaDonVi);
            } catch (Exception ex) {
                giaDonVi = 0;
                Log.w("ChiTietActivity", "Không thể parse giá khi thêm giỏ hàng (mảng rỗng), fallback=0 (raw='" + sanPhamMoi.getGiasp() + "')", ex);
            }
            GioHang gioHang = new GioHang();
            gioHang.setGiasp(giaDonVi);
            gioHang.setSoluong(soLuong);
            gioHang.setIdsp(sanPhamMoi.getId());
            gioHang.setTensp(sanPhamMoi.getTensp());
            gioHang.setHinhsp(sanPhamMoi.getHinhanh());
            Utils.manggiohang.add(gioHang);
        }
        int totalItem = 0;
        for (int i = 0; i < Utils.manggiohang.size(); i++){
            totalItem = totalItem + Utils.manggiohang.get(i).getSoluong();
        }

        notificationBadge.setText(String.valueOf(totalItem));
        Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        Log.d("ChiTietActivity", "Sản phẩm đã được thêm vào giỏ hàng: " + sanPhamMoi.getTensp());
        syncGioHangToServer();
    }

    private void syncGioHangToServer() {
        if (Utils.user_current == null) {
            Log.d("ChiTietActivity", "User chưa đăng nhập, chỉ lưu local");
            return;
        }

        if (sanPhamMoi == null) return;

        long giaDonVi;
        try {
            String raw = sanPhamMoi.getGiasp() == null ? "" : sanPhamMoi.getGiasp();
            double price = Double.parseDouble(raw.trim());
            giaDonVi = (long) price;
        } catch (Exception ex) {
            giaDonVi = 0;
            Log.w("ChiTietActivity", "Không parse được giá khi sync server", ex);
        }

        compositeDisposable.add(apiBanHang.themGioHang(
                Utils.user_current.getId(),
                sanPhamMoi.getId(),
                sanPhamMoi.getTensp(),
                giaDonVi,
                sanPhamMoi.getHinhanh(),
                soLuong
        ).subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(
             messageModel -> {
                 if (messageModel.isSuccess()) {
                     Log.d("ChiTietActivity", "Đã đồng bộ giỏ hàng lên server: " + messageModel.getMessage());
                 } else {
                     Log.w("ChiTietActivity", "⚠Server response: " + messageModel.getMessage());
                 }
             },
             throwable -> {
                 Log.e("ChiTietActivity", " Lỗi đồng bộ giỏ hàng: " + throwable.getMessage());
             }
         ));
    }


    private void initData() {
        sanPhamMoi = (SanPhamMoi) getIntent().getSerializableExtra("chitiet");


        if (sanPhamMoi == null) {
            Log.e("ChiTietActivity", "Dữ liệu sản phẩm bị null. Kiểm tra Intent hoặc key 'chitiet'.");
            imghinhanh.setImageResource(R.drawable.ic_media_24);
            tensp.setText(R.string.no_product_data);
            mota.setText("");
            giasp.setText("");
            return;
        }

        Log.d("ChiTietActivity", "=== CHI TIẾT SẢN PHẨM ===");
        Log.d("ChiTietActivity", "Tên: " + sanPhamMoi.getTensp());
        Log.d("ChiTietActivity", "URL ảnh gốc: " + sanPhamMoi.getHinhanh());


        tensp.setText(sanPhamMoi.getTensp());
        mota.setText(sanPhamMoi.getMota());

        String imageUrl = sanPhamMoi.getHinhanh();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            int lastHttp = imageUrl.lastIndexOf("http://");
            int lastHttps = imageUrl.lastIndexOf("https://");
            if (lastHttp > 0) {
                imageUrl = imageUrl.substring(lastHttp);
            } else if (lastHttps > 0) {
                imageUrl = imageUrl.substring(lastHttps);
            } else if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                // URL tương đối, cần thêm BASE_URL
                if (imageUrl.startsWith("images/")) {
                    imageUrl = imageUrl.substring(7);
                }
                imageUrl = Utils.BASE_URL + "images/" + imageUrl;
            }
        } else {
            imageUrl = "";
            Log.w("ChiTietActivity", "Image URL is null or empty");
        }

        final String finalImageUrl = imageUrl;
        Log.d("ChiTietActivity", "URL ảnh cuối cùng: " + finalImageUrl);


        Glide.with(getApplicationContext())
                .load(finalImageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_media_24)
                .error(R.drawable.ic_media_24)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        Log.e("ChiTietActivity", "FAILED to load image: " + finalImageUrl, e);
                        if (e != null) {
                            Log.e("ChiTietActivity", "Error causes: " + e.getCauses());
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                   com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d("ChiTietActivity", "SUCCESS loaded image: " + finalImageUrl);
                        return false;
                    }
                })
                .into(imghinhanh);
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        String rawPrice = sanPhamMoi.getGiasp();
        Log.d("ChiTietActivity", "Raw price from model: '" + rawPrice + "'");
        String searchPlaceholder = getString(R.string.place_holder_search);
        String searchLabel = getString(R.string.search);
        if (rawPrice != null) {
            rawPrice = rawPrice.replace('\u2026', ' ').trim();
        }


        boolean isPlaceholder = false;
        if (rawPrice == null || rawPrice.trim().isEmpty()) {
            isPlaceholder = true;
        } else {
            String lower = rawPrice.toLowerCase();
            boolean containsPlaceholderWords = lower.contains(searchPlaceholder.toLowerCase()) || lower.contains(searchLabel.toLowerCase()) || lower.contains("tìm kiếm");
            boolean hasDigit = rawPrice.matches(".*\\d.*");
            if (containsPlaceholderWords && !hasDigit) {
                isPlaceholder = true;
            } else if (!hasDigit) {

                isPlaceholder = true;
            }
        }

        if (isPlaceholder) {
            if (giasp != null) giasp.setText(getStringByName("loading", "Đang tải..."));
            Log.w("ChiTietActivity", "Giá sản phẩm rỗng hoặc chứa placeholder tìm kiếm, sẽ fetch từ server (raw='" + sanPhamMoi.getGiasp() + "')");
            // Fallback: lấy lại chi tiết sản phẩm (bao gồm giasp) từ server bằng Volley
            fetchProductDetailWithVolley(sanPhamMoi.getId());
        } else {
            try {
                String cleaned = rawPrice.trim();

                Log.d("ChiTietActivity", "Raw price: '" + rawPrice + "' -> Cleaned: '" + cleaned + "'");

                if (cleaned.isEmpty()) {
                    giasp.setText(getString(R.string.price_na));
                    Log.w("ChiTietActivity", "Giá sản phẩm rỗng");
                } else {
                    double gia = Double.parseDouble(cleaned);
                    String formatted = decimalFormat.format(gia);
                    giasp.setText(getString(R.string.price_format, formatted));
                    Log.d("ChiTietActivity", "Final formatted price: " + formatted);
                }
            } catch (NumberFormatException e) {
                giasp.setText(getString(R.string.price_na));
                Log.e("ChiTietActivity", "Lỗi định dạng giá ('" + rawPrice + "'): " + e.getMessage());
            }
        }


        soLuong = 1;
        txtSoLuong.setText(String.valueOf(soLuong));

        loadTonKho();
    }


    private void initView() {
        tensp = findViewById(R.id.txttensp);
        giasp = findViewById(R.id.txtgiasp);
        mota = findViewById(R.id.txtmotachitiet);
        int idTxtTonKho = getResources().getIdentifier("txtTonKho", "id", getPackageName());
        if (idTxtTonKho != 0) {
            txtTonKho = findViewById(idTxtTonKho);
        } else {
            txtTonKho = null;
            Log.w("ChiTietActivity", "Resource id txtTonKho not found at runtime; using null fallback");
        }
        btnthem = findViewById(R.id.btnthemvaogiohang);
        txtSoLuong = findViewById(R.id.txtSoLuong);
        btnCong = findViewById(R.id.btnCong);
        btnTru = findViewById(R.id.btnTru);
        imghinhanh = findViewById(R.id.imgchitiet);
        toolbar = findViewById(R.id.toolbar);
        notificationBadge = findViewById(R.id.menu_sl);
        FrameLayout framelayoutgiohang = findViewById(R.id.framegiohang);
        framelayoutgiohang.setOnClickListener(view -> {
            Intent giohang = new Intent(getApplicationContext(), GioHangActivity.class);
            startActivity(giohang);
        });

        if (Utils.manggiohang != null){
            int totalItem = 0;
            for (int i = 0; i < Utils.manggiohang.size(); i++){
                totalItem = totalItem + Utils.manggiohang.get(i).getSoluong();
            }
            notificationBadge.setText(String.valueOf(totalItem));
        }
    }

    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.manggiohang != null) {
            int totalItem = 0;
            for (int i = 0; i < Utils.manggiohang.size(); i++) {
                totalItem = totalItem + Utils.manggiohang.get(i).getSoluong();
            }
            notificationBadge.setText(String.valueOf(totalItem));
        }
    }

    private String getStringByName(String name, String fallback) {
        int id = getResources().getIdentifier(name, "string", getPackageName());
        if (id != 0) {
            try {
                return getString(id);
            } catch (Exception e) {
                Log.w("ChiTietActivity", "getString failed for " + name + ": " + e.getMessage());
            }
        }
        return fallback;
    }

    private void loadTonKho() {
        if (sanPhamMoi == null) return;

        if (txtTonKho != null) txtTonKho.setText(getStringByName("loading", "Đang tải..."));

        compositeDisposable.add(apiBanHang.kiemTraTonKho(sanPhamMoi.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response != null && response.isSuccess()) {
                                TonKhoResponse.TonKhoData data = response.getData();
                                if (data != null) {
                                    tonKhoHienTai = data.getSoluongtonkho();
                                    sanPhamMoi.setSoluongtonkho(tonKhoHienTai);

                                    String serverPrice = data.getGiasp();
                                    boolean currentHasDigit = sanPhamMoi.getGiasp() != null && sanPhamMoi.getGiasp().matches(".*\\d.*");
                                    boolean serverHasDigit = serverPrice != null && serverPrice.matches(".*\\d.*");
                                    if (!currentHasDigit && serverHasDigit) {
                                        sanPhamMoi.setGiasp(serverPrice);
                                        try {
                                            DecimalFormat df = new DecimalFormat("###,###,###");
                                            double gia = Double.parseDouble(serverPrice.trim());
                                            String formatted = df.format(gia);
                                            if (giasp != null) giasp.setText(getString(R.string.price_format, formatted));
                                            Log.d("ChiTietActivity", "Updated price from server: " + serverPrice + " -> " + formatted);
                                        } catch (Exception ex) {
                                            if (giasp != null) giasp.setText(getString(R.string.price_na));
                                            Log.w("ChiTietActivity", "Không thể format giá từ server: " + serverPrice, ex);
                                        }
                                    }

                                    if (txtTonKho != null) {
                                        txtTonKho.setText(String.valueOf(tonKhoHienTai));

                                        if (tonKhoHienTai <= 0) {
                                            txtTonKho.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                                            btnthem.setEnabled(false);
                                            btnthem.setText(getStringByName("out_of_stock", "Hết hàng"));
                                        } else if (tonKhoHienTai <= 5) {
                                            txtTonKho.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                                        } else {
                                            txtTonKho.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                                        }
                                    } else {
                                        Log.w("ChiTietActivity", "txtTonKho view is null; skipping UI update for stock");
                                    }

                                    Log.d("ChiTietActivity", "Tồn kho: " + tonKhoHienTai);
                                } else {
                                    if (txtTonKho != null) {
                                        txtTonKho.setText(getStringByName("stock_na", "N/A"));
                                        txtTonKho.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                                    }
                                }
                            } else {
                                String message = response != null ? response.getMessage() : "Không có phản hồi";
                                if (txtTonKho != null) {
                                    txtTonKho.setText(getStringByName("stock_na", "N/A"));
                                    txtTonKho.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                                }
                                Log.e("ChiTietActivity", "Lỗi kiểm tra tồn kho: " + message);
                            }
                        },
                        throwable -> {
                            if (txtTonKho != null) {
                                txtTonKho.setText(getStringByName("stock_na", "N/A"));
                                txtTonKho.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                            }
                            Log.e("ChiTietActivity", "Lỗi kết nối API tồn kho: " + throwable.getMessage());
                        }
                ));
    }

    private void fetchProductDetailWithVolley(int idsp) {
        try {
            String url = Utils.URL_KIEM_TRA_TON_KHO + "?idsp=" + idsp;
            StringRequest req = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            org.json.JSONObject jo = new org.json.JSONObject(response);
                            if (jo.optBoolean("success", false)) {
                                org.json.JSONObject data = jo.optJSONObject("data");
                                if (data != null) {
                                    String serverPrice = data.optString("giasp", "");
                                    if (serverPrice != null && serverPrice.matches(".*\\d.*")) {
                                        // cập nhật model và UI
                                        sanPhamMoi.setGiasp(serverPrice);
                                        // Parse trực tiếp - Double.parseDouble tự động xử lý "3290000.00"
                                        try {
                                            double gia = Double.parseDouble(serverPrice.trim());
                                            java.text.DecimalFormat df = new java.text.DecimalFormat("###,###,###");
                                            String formatted = df.format(gia);
                                            if (giasp != null) giasp.setText(getString(R.string.price_format, formatted));
                                            Log.d("ChiTietActivity", "Volley updated price: " + serverPrice + " -> " + formatted);
                                        } catch (NumberFormatException nfe) {
                                            Log.w("ChiTietActivity", "Cannot parse serverPrice: " + serverPrice, nfe);
                                        }
                                    }
                                }
                            } else {
                                Log.w("ChiTietActivity", "fetchProductDetailWithVolley: server returned no data");
                            }
                        } catch (Exception e) {
                            Log.e("ChiTietActivity", "fetchProductDetailWithVolley parse error", e);
                        }
                    },
                    error -> {
                        Log.e("ChiTietActivity", "fetchProductDetailWithVolley network error", error);
                    });
            requestQueue.add(req);
        } catch (Exception ex) {
            Log.e("ChiTietActivity", "fetchProductDetailWithVolley failed", ex);
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
