package vn.duytruong.appbandienthoai.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import com.google.gson.JsonParseException;
import com.nex3z.notificationbadge.NotificationBadge;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.LoaiSpAdapter;
import vn.duytruong.appbandienthoai.adapter.SanPhamMoiAdapter;
import vn.duytruong.appbandienthoai.model.LoaiSp;
import vn.duytruong.appbandienthoai.model.SanPhamMoi;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Toolbar toolbar;
    ViewFlipper viewFlipper;
    RecyclerView recyclerViewManHinhChinh;
    NavigationView navigationView;
    ListView listViewManHinhChinh;
    DrawerLayout drawerLayout;
    LoaiSpAdapter loaiSpAdapter;

    List<LoaiSp> mangloaisp;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    ApiBanHang apiBanHang;
    List<SanPhamMoi> mangSpMoi;
    SanPhamMoiAdapter spAdapter;
    NotificationBadge notificationBadge;
    FrameLayout frameLayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
            Paper.init(this);
            if (Paper.book().read("user") != null){
                Utils.user_current = Paper.book().read("user");
                Log.d(TAG, "========== USER DEBUG INFO ==========");
                Log.d(TAG, "Loaded user from Paper: email=" + Utils.user_current.getEmail());
                Log.d(TAG, "Role (raw int): " + Utils.user_current.getRole());
                Log.d(TAG, "isAdmin() returns: " + Utils.user_current.isAdmin());
                Log.d(TAG, "Expected: role=1 → isAdmin=true, role=0 → isAdmin=false");
                Log.d(TAG, "Test: (role == 1) = " + (Utils.user_current.getRole() == 1));
                Log.d(TAG, "======================================");
            } else {
                Log.d(TAG, "No user found in Paper storage");
            }
            Anhxa();
            ActionBar();

            // Xóa cache Glide trong background để không chặn UI thread
            new Thread(() -> {
                try {
                    com.bumptech.glide.Glide.get(this).clearDiskCache();
                } catch (Exception e) {
                    Log.e(TAG, "Error clearing Glide cache: " + e.getMessage());
                }
            }).start();

            // Load data sau khi UI đã hiển thị để tránh lag
            recyclerViewManHinhChinh.post(() -> {
                if (isConnected(this)){
                    Log.d(TAG, "Internet connected, loading products...");
                    ActionViewFlipper();
                    getLoaiSanPham();
                    getSpMoi();
                    getEventClick();
                }else{
                    Log.e(TAG, "No internet connection");
                    Toast.makeText(getApplicationContext(), "Không có kết nối internet", Toast.LENGTH_LONG).show();
                }
            });

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                return insets;
            });
            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Lấy token
                    String token = task.getResult();
                    Log.d("FCM_TOKEN", token);
                });
        } catch (Exception e) {
            Log.e(TAG, "onCreate error: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo MainActivity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Không finish() để user có thể thấy lỗi
        }
    }

    private void getEventClick() {
        listViewManHinhChinh.setOnItemClickListener((adapterView, view, i, l) -> {
            // Lấy tên loại sản phẩm được click
            String tenLoai = mangloaisp.get(i).getTensanpham();
            int idLoai = mangloaisp.get(i).getId();

            Log.d(TAG, "Click vào mục: position=" + i + ", tên='" + tenLoai + "', id=" + idLoai);

            // Kiểm tra nếu click vào "Đăng xuất"
            if (tenLoai.equals("Đăng xuất")) {
                Paper.book().delete("user");
                Utils.user_current = null;
                Intent dangnhap = new Intent(getApplicationContext(), DangNhapActivity.class);
                startActivity(dangnhap);
                finish();
                return;
            }

            // Kiểm tra nếu click vào "Quản lí sản phẩm"
            if (tenLoai.contains("Quản lí sản phẩm") || tenLoai.contains("quản lí sản phẩm")) {
                Intent quanli = new Intent(getApplicationContext(), QuanLiActivity.class);
                startActivity(quanli);
                drawerLayout.closeDrawer(GravityCompat.START);
                return;
            }

            // Kiểm tra nếu click vào "Quản lý người dùng"
            if (tenLoai.contains("Quản lý người dùng") || tenLoai.contains("quản lý người dùng")) {
                // Kiểm tra quyền admin
                if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
                    Toast.makeText(getApplicationContext(), "Bạn không có quyền truy cập!", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), QuanLyNguoiDungActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return;
            }

            // Kiểm tra nếu click vào "Quản lý Voucher"
            if (tenLoai.contains("Quản lý Voucher") || tenLoai.contains("quản lý voucher") ||
                tenLoai.contains("Voucher") || tenLoai.contains("voucher")) {
                // Kiểm tra quyền admin
                if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
                    Toast.makeText(getApplicationContext(), "Bạn không có quyền truy cập!", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), QuanLyVoucherActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return;
            }

            // Kiểm tra nếu click vào "Thống kê tổng hợp"
            if (tenLoai.contains("Thống kê")) {
                // Kiểm tra quyền admin
                if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
                    Toast.makeText(getApplicationContext(), "Bạn không có quyền truy cập!", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), ThongKeDoanhThuActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return;
            }

            // Kiểm tra nếu click vào "Quản lí" (cũ - để tương thích ngược)
            if (tenLoai.contains("Quản lí") || tenLoai.contains("quản lí")) {
                Intent quanli = new Intent(getApplicationContext(), QuanLiActivity.class);
                startActivity(quanli);
                drawerLayout.closeDrawer(GravityCompat.START);
                return;
            }

            // Kiểm tra nếu click vào "Thông tin cá nhân"
            if (tenLoai.contains("Thông tin") || tenLoai.contains("thông tin") ||
                tenLoai.contains("Cá nhân") || tenLoai.contains("Profile")) {
                // Nếu chưa đăng nhập thì chuyển tới màn hình đăng nhập
                if (Utils.user_current == null) {
                    Intent dangnhap = new Intent(getApplicationContext(), DangNhapActivity.class);
                    startActivity(dangnhap);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }

                Intent updateProfile = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                startActivity(updateProfile);
                drawerLayout.closeDrawer(GravityCompat.START);
                return;
            }

            // Kiểm tra nếu click vào "Đơn hàng"
            if (tenLoai.contains("Đơn hàng") || tenLoai.contains("đơn hàng")) {
                // Nếu chưa đăng nhập thì chuyển t��i màn hình đăng nhập
                if (Utils.user_current == null) {
                    Intent dangnhap = new Intent(getApplicationContext(), DangNhapActivity.class);
                    startActivity(dangnhap);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }

                // Thử lấy danh sách đơn hàng của user và mở chi tiết đơn hàng đầu tiên (mới nhất)
                // Debug log: in user id and role so we can confirm client sends isadmin
                if (Utils.user_current != null) {
                    Log.d(TAG, "xemDonHang request by user id=" + Utils.user_current.getId() + " role=" + Utils.user_current.getRole());
                } else {
                    Log.d(TAG, "xemDonHang request by anonymous user");
                }

                compositeDisposable.add(apiBanHang.xemDonHang(
                        (Utils.user_current != null ? Utils.user_current.getId() : 0),
                        (Utils.user_current != null && Utils.user_current.isAdmin()) ? 1 : 0,
                        "my"
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                donHangModel -> {
                                    // Open the orders list for all users (show the full list instead of immediately opening the latest order)
                                    Intent donhang = new Intent(getApplicationContext(), XemDonActivity.class);
                                    startActivity(donhang);
                                    drawerLayout.closeDrawer(GravityCompat.START);
                                },
                                throwable -> {
                                    // Nếu lỗi khi gọi API thì mở danh sách đơn hàng như fallback
                                    Intent donhang = new Intent(getApplicationContext(), XemDonActivity.class);
                                    startActivity(donhang);
                                    drawerLayout.closeDrawer(GravityCompat.START);
                                }
                        ));

                return;
            }

            // Kiểm tra nếu click vào "Trang chủ"
            if (tenLoai.contains("Trang chủ") || tenLoai.contains("trang chủ") || i == 0) {
                // Nếu đang ở MainActivity thì chỉ đóng drawer
                drawerLayout.closeDrawer(GravityCompat.START);
                return;
            }

            // Xử lý các loại sản phẩm - CHỈ CHẤP NHẬN ID HỢP LỆ TỪ SERVER
            if (idLoai <= 0) {
                Log.e(TAG, "ID không hợp lệ từ server: id=" + idLoai + ", tên='" + tenLoai + "'");
                Toast.makeText(getApplicationContext(), "Mục này chưa có sản phẩm", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return;
            }

            // MAPPING: Chuyển đổi ID từ bảng loaisp sang ID thực tế trong bảng sanpham
            // ID 2 (Đồng hồ cơ) → query loai=1
            // ID 3 (Đồng hồ điện tử) → query loai=2
            int loaiQuery = idLoai;
            if (idLoai == 2) {
                loaiQuery = 1; // Đồng hồ cơ → loại 1
            } else if (idLoai == 3) {
                loaiQuery = 2; // Đồng hồ điện tử → loại 2
            }

            Log.d(TAG, "Mở DienThoaiActivity: ID menu=" + idLoai + " → query loai=" + loaiQuery + ", tên='" + tenLoai + "'");
            Intent intent = new Intent(getApplicationContext(), DienThoaiActivity.class);
            intent.putExtra("loai", loaiQuery);
            intent.putExtra("tenloai", tenLoai); // Thêm tên loại để hiển thị trên toolbar
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }


    private void getSpMoi() {
        compositeDisposable.add(apiBanHang.getSpMoi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sanPhamMoiModel -> {
                            if (sanPhamMoiModel.isSuccess()) {
                                mangSpMoi = sanPhamMoiModel.getResult();

                                // Debug: log count and each product's raw price to trace incorrect values
                                Log.d(TAG, "Loaded products count: " + (mangSpMoi == null ? 0 : mangSpMoi.size()));
                                if (mangSpMoi != null) {
                                    for (int i = 0; i < mangSpMoi.size(); i++) {
                                        SanPhamMoi sp = mangSpMoi.get(i);
                                        Log.d(TAG, "Product[" + i + "] name='" + sp.getTensp() + "' raw giasp='" + sp.getGiasp() + "'");
                                    }
                                }

                                spAdapter = new SanPhamMoiAdapter(getApplicationContext(), mangSpMoi);
                                recyclerViewManHinhChinh.setAdapter(spAdapter);
                            }
                        },
                        throwable -> {
                            // Log full throwable for debugging, but show friendly message to user
                            Log.e(TAG, "Error in getSpMoi", throwable);
                            String message = getFriendlyErrorMessage(throwable);
                            Toast.makeText(
                                    getApplicationContext(),
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
        );
    }


    private void getLoaiSanPham() {
        Log.d(TAG, "Calling API: " + Utils.BASE_URL + "getloaisp.php");
        compositeDisposable.add(apiBanHang.getLoaiSp()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        loaiSpModel -> {
                            Log.d(TAG, "API Response received");
                            if (loaiSpModel.isSuccess()) {
                                Log.d(TAG, "Success! Products count: " + loaiSpModel.getResult().size());
                                mangloaisp.clear();
                                // Thêm mục Trang chủ đầu tiên (chỉ 1 lần)
                                mangloaisp.add(new LoaiSp("Trang chủ","https://cdn-icons-png.flaticon.com/512/25/25694.png"));
                                // Thêm các loại sản phẩm từ server (Đồng hồ cơ, Đồng hồ điện tử)
                                for (LoaiSp loai : loaiSpModel.getResult()) {
                                    // Exclude reserved menu items so they are controlled locally
                                    String ten = loai.getTensanpham();
                                    if (ten == null) continue;
                                    if (ten.equalsIgnoreCase("Trang chủ") ||
                                        ten.equalsIgnoreCase("Đơn hàng") ||
                                        ten.equalsIgnoreCase("Quản lí") ||
                                        ten.equalsIgnoreCase("Quản lí sản phẩm") ||
                                        ten.equalsIgnoreCase("Quản lý người dùng") ||
                                        ten.equalsIgnoreCase("Thống kê")) {
                                        continue;
                                    }
                                    mangloaisp.add(loai);
                                }
                                // Log chi tiết từng loại sản phẩm
                                for (int i = 0; i < mangloaisp.size(); i++) {
                                    LoaiSp loai = mangloaisp.get(i);
                                    Log.d(TAG, "Loại " + i + ": id=" + loai.getId() + ", tên='" + loai.getTensanpham() + "'");
                                }
                                Log.d(TAG, "=========================================");
                                // Thêm mục Thông tin cá nhân (nếu đã đăng nhập)
                                if (Utils.user_current != null) {
                                    mangloaisp.add(new LoaiSp("Thông tin cá nhân", "https://cdn-icons-png.flaticon.com/512/1077/1077114.png"));
                                }

                                // Thêm mục Đơn hàng
                                mangloaisp.add(new LoaiSp("Đơn hàng", "https://cdn-icons-png.flaticon.com/512/679/679922.png"));

                                // Thêm các mục quản lý CHỈ CHO ADMIN
                                Log.d(TAG, "Checking admin permissions: user_current=" + (Utils.user_current != null ? Utils.user_current.getEmail() : "null"));
                                if (Utils.user_current != null) {
                                    Log.d(TAG, "User role=" + Utils.user_current.getRole() + ", isAdmin()=" + Utils.user_current.isAdmin());
                                }

                                if (Utils.user_current != null && Utils.user_current.isAdmin()) {
                                    Log.d(TAG, " User IS admin - adding admin menus...");
                                    mangloaisp.add(new LoaiSp("Quản lí sản phẩm", "https://cdn-icons-png.flaticon.com/512/3039/3039386.png"));
                                    mangloaisp.add(new LoaiSp("Quản lý người dùng", "https://cdn-icons-png.flaticon.com/512/1077/1077063.png"));
                                    mangloaisp.add(new LoaiSp("Quản lý Voucher", "https://cdn-icons-png.flaticon.com/512/3514/3514447.png"));
                                    mangloaisp.add(new LoaiSp("Thống kê tổng hợp", "https://cdn-icons-png.flaticon.com/512/3135/3135706.png"));
                                } else {
                                    Log.d(TAG, "User is NOT admin - skipping admin menus");
                                }

                                // Thêm mục Đăng xuất cuối cùng
                                mangloaisp.add(new LoaiSp("Đăng xuất", "https://cdn-icons-png.flaticon.com/512/1828/1828490.png"));

                                loaiSpAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "Tải thành công " + loaiSpModel.getResult().size() + " loại sản phẩm", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "API returned success=false: " + loaiSpModel.getMessage());
                                Toast.makeText(getApplicationContext(), "Lỗi: " + loaiSpModel.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            // Log full throwable for debugging, but show friendly message to user
                            Log.e(TAG, "Error loading products", throwable);
                            String message = getFriendlyErrorMessage(throwable);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                ));
    }


    private void ActionViewFlipper() {
        List<String> mangquangcao = new ArrayList<>();
        // Using placeholder images from picsum.photos (reliable and always available)
        mangquangcao.add("https://img.pikbest.com/origin/06/43/44/88JpIkbEsTsAQ.jpg!w700wp"); // Phone banner 1
        mangquangcao.add("https://mir-s3-cdn-cf.behance.net/projects/404/17ba20134130953.Y3JvcCw4NjQsNjc2LDI0OCww.jpg"); // Phone banner 2
        mangquangcao.add("https://img.pikbest.com/origin/06/43/40/598pIkbEsTQmz.jpg!w700wp"); // Phone banner 3
        mangquangcao.add("https://upcontent.vn/wp-content/uploads/2024/06/mau-banner-dong-ho-20-1024x640.jpg"); // Phone banner 3

        for (int i = 0; i < mangquangcao.size(); i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            Glide.with(getApplicationContext())
                    .load(mangquangcao.get(i))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(imageView);
        }

        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
        Animation slide_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right);
        Animation slide_out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_right);
        viewFlipper.setInAnimation(slide_in);
        viewFlipper.setOutAnimation(slide_out);

    }

    private void ActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_sort_by_size);
        toolbar.setNavigationOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void Anhxa() {
        try {
            toolbar = findViewById(R.id.toolbarmanhinhchinh);
            viewFlipper = findViewById(R.id.viewFlipper);
            recyclerViewManHinhChinh = findViewById(R.id.recycView);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
            recyclerViewManHinhChinh.setLayoutManager(layoutManager);
            recyclerViewManHinhChinh.setHasFixedSize(true);
            listViewManHinhChinh = findViewById(R.id.listviewmanhinhchinh);
            navigationView = findViewById(R.id.navigationview);
            drawerLayout = findViewById(R.id.drawerlayout);
            notificationBadge = findViewById(R.id.menu_sl);
            frameLayout = findViewById(R.id.framegiohang);
            mangSpMoi = new ArrayList<>();
            spAdapter = new SanPhamMoiAdapter(getApplicationContext(), mangSpMoi);
            // khoi tao list
            mangloaisp = new ArrayList<>();
            // khoi tao adapter
            loaiSpAdapter = new LoaiSpAdapter(getApplicationContext(), mangloaisp);
            listViewManHinhChinh.setAdapter(loaiSpAdapter);
            if (Utils.manggiohang == null) {
                Utils.manggiohang = new ArrayList<>();
            }else{
                int totalItem = 0;
                for (int i = 0; i < Utils.manggiohang.size(); i++){
                    totalItem = totalItem + Utils.manggiohang.get(i).getSoluong();
                }

                notificationBadge.setText(String.valueOf(totalItem));
            }
            frameLayout.setOnClickListener(view -> {
                Intent giohang = new Intent(getApplicationContext(), GioHangActivity.class);
                startActivity(giohang);
            });
            FrameLayout imgsearch = findViewById(R.id.imgsearch);
            imgsearch.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            Log.e(TAG, "Anhxa error: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        int totalItem = 0;
        for (int i = 0; i < Utils.manggiohang.size(); i++) {
            totalItem = totalItem + Utils.manggiohang.get(i).getSoluong();
        }
        notificationBadge.setText(String.valueOf(totalItem));
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    /**
     * Create a user-friendly message for network / server errors.
     */
    private String getFriendlyErrorMessage(Throwable throwable) {
        if (throwable instanceof IOException) {
            return "Không thể kết nối tới server. Vui lòng kiểm tra kết nối mạng.";
        } else if (throwable instanceof HttpException) {
            int code = ((HttpException) throwable).code();
            return "Lỗi máy chủ (HTTP " + code + "). Vui lòng thử lại sau.";
        } else if (throwable instanceof JsonParseException) {
            return "Dữ liệu phản hồi không hợp lệ. Vui lòng thử lại sau.";
        } else {
            // fallback: log full message but show short text to user
            return "Đã xảy ra lỗi. Vui lòng thử lại sau.";
        }
    }


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
