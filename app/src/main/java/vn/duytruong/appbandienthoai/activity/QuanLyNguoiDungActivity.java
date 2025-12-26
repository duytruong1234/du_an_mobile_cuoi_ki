package vn.duytruong.appbandienthoai.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.adapter.NguoiDungAdapter;
import vn.duytruong.appbandienthoai.model.User;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class QuanLyNguoiDungActivity extends AppCompatActivity {
    private static final String TAG = "QuanLyNguoiDung";

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvUserCount;

    private NguoiDungAdapter adapter;
    private List<User> userList;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ApiBanHang apiBanHang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra quyền admin
        if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập chức năng này!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_quan_ly_nguoi_dung);

        initViews();
        initRecyclerView();
        loadUserList();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản Lý Người Dùng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);

        // Floating Action Button để tạo tài khoản mới
        com.google.android.material.floatingactionbutton.FloatingActionButton fabCreateUser = findViewById(R.id.fabCreateUser);
        fabCreateUser.setOnClickListener(v -> showCreateUserDialog());

        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        userList = new ArrayList<>();
    }

    private void initRecyclerView() {
        adapter = new NguoiDungAdapter(this, userList, new NguoiDungAdapter.OnUserActionListener() {
            @Override
            public void onDeleteUser(User user, int position) {
                deleteUser(user, position);
            }

            @Override
            public void onToggleStatus(User user, int position) {
                toggleUserStatus(user, position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quan_ly_nguoi_dung, menu);

        // Lấy action view chứa badge số lượng users
        MenuItem menuItem = menu.findItem(R.id.action_user_count);
        View actionView = menuItem.getActionView();
        if (actionView != null) {
            tvUserCount = actionView.findViewById(R.id.tvUserCount);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Toast.makeText(this, "Đang tải lại danh sách...", Toast.LENGTH_SHORT).show();
            loadUserList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserList() {
        progressBar.setVisibility(View.VISIBLE);

        compositeDisposable.add(
            apiBanHang.getAllUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    userModel -> {
                        progressBar.setVisibility(View.GONE);
                        if (userModel.isSuccess()) {
                            userList.clear();
                            userList.addAll(userModel.getResult());
                            adapter.notifyDataSetChanged();

                            // Cập nhật số lượng users trong badge
                            if (tvUserCount != null) {
                                tvUserCount.setText(String.valueOf(userList.size()));
                            }

                            Log.d(TAG, "Loaded " + userList.size() + " users");
                        } else {
                            Toast.makeText(this, "Không thể tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                        }
                    },
                    throwable -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error loading users: " + throwable.getMessage());
                        Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                )
        );
    }


    private void deleteUser(User user, int position) {
        // Không cho phép xóa chính mình
        if (user.getId() == Utils.user_current.getId()) {
            Toast.makeText(this, "Không thể xóa tài khoản của chính mình!", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa người dùng " + user.getUsername() + "?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                compositeDisposable.add(
                    apiBanHang.deleteUser(user.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            messageModel -> {
                                if (messageModel.isSuccess()) {
                                    userList.remove(position);
                                    adapter.notifyItemRemoved(position);

                                    // Cập nhật số lượng users trong badge
                                    if (tvUserCount != null) {
                                        tvUserCount.setText(String.valueOf(userList.size()));
                                    }

                                    Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, messageModel.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> {
                                Log.e(TAG, "Error deleting user: " + throwable.getMessage());
                                Toast.makeText(this, "Lỗi xóa người dùng", Toast.LENGTH_SHORT).show();
                            }
                        )
                );
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void toggleUserStatus(User user, int position) {
        int newStatus = user.isActive() ? 0 : 1; // Toggle: Active -> Locked hoặc Locked -> Active

        // Không cho phép khóa chính mình
        if (user.getId() == Utils.user_current.getId()) {
            Toast.makeText(this, "Không thể khóa tài khoản của chính mình!", Toast.LENGTH_SHORT).show();
            // Reset lại switch về trạng thái cũ
            adapter.notifyItemChanged(position);
            return;
        }

        String actionText = newStatus == 1 ? "mở khóa" : "khóa";

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận " + actionText + " tài khoản")
            .setMessage("Bạn có chắc muốn " + actionText + " tài khoản " + user.getUsername() + "?")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                progressBar.setVisibility(View.VISIBLE);

                compositeDisposable.add(
                    apiBanHang.updateUserStatus(user.getId(), newStatus)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            messageModel -> {
                                progressBar.setVisibility(View.GONE);
                                if (messageModel.isSuccess()) {
                                    user.setStatus(newStatus);
                                    adapter.notifyItemChanged(position);
                                    String statusText = newStatus == 1 ? "Đã mở khóa" : "Đã khóa";
                                    Toast.makeText(this, statusText + " tài khoản thành công", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, messageModel.getMessage(), Toast.LENGTH_SHORT).show();
                                    // Reset lại switch về trạng thái cũ nếu thất bại
                                    adapter.notifyItemChanged(position);
                                }
                            },
                            throwable -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e(TAG, "Error updating status: " + throwable.getMessage());
                                Toast.makeText(this, "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                                // Reset lại switch về trạng thái cũ nếu thất bại
                                adapter.notifyItemChanged(position);
                            }
                        )
                );
            })
            .setNegativeButton("Hủy", (dialog, which) -> {
                // Reset lại switch về trạng thái cũ nếu hủy
                adapter.notifyItemChanged(position);
            })
            .setOnCancelListener(dialog -> {
                // Reset lại switch về trạng thái cũ nếu hủy
                adapter.notifyItemChanged(position);
            })
            .show();
    }

    private void showCreateUserDialog() {
        // Tạo dialog với layout tùy chỉnh
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_create_user, null);
        builder.setView(dialogView);

        // Lấy các views
        android.widget.EditText etEmail = dialogView.findViewById(R.id.etEmail);
        android.widget.EditText etUsername = dialogView.findViewById(R.id.etUsername);
        android.widget.EditText etPassword = dialogView.findViewById(R.id.etPassword);
        android.widget.EditText etMobile = dialogView.findViewById(R.id.etMobile);

        builder.setTitle("Tạo Tài Khoản Mới")
            .setPositiveButton("Tạo", null) // Set null trước để xử lý click sau
            .setNegativeButton("Hủy", null);

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Override click listener để không tự động đóng dialog khi validation fail
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            int role = 0; // Luôn tạo với quyền User thường

            // Validate
            if (email.isEmpty()) {
                etEmail.setError("Vui lòng nhập email");
                etEmail.requestFocus();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Email không hợp lệ");
                etEmail.requestFocus();
                return;
            }
            if (username.isEmpty()) {
                etUsername.setError("Vui lòng nhập tên người dùng");
                etUsername.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Vui lòng nhập mật khẩu");
                etPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                etPassword.requestFocus();
                return;
            }
            if (mobile.isEmpty()) {
                etMobile.setError("Vui lòng nhập số điện thoại");
                etMobile.requestFocus();
                return;
            }
            if (!mobile.matches("^[0-9]{10}$")) {
                etMobile.setError("Số điện thoại phải có 10 chữ số");
                etMobile.requestFocus();
                return;
            }

            // Gọi API tạo user
            progressBar.setVisibility(View.VISIBLE);
            compositeDisposable.add(
                apiBanHang.createUser(email, username, password, mobile, role)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        userModel -> {
                            progressBar.setVisibility(View.GONE);
                            if (userModel.isSuccess()) {
                                Toast.makeText(this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                // Reload lại danh sách
                                loadUserList();
                            } else {
                                Toast.makeText(this, userModel.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            progressBar.setVisibility(View.GONE);
                            Log.e(TAG, "Error creating user: " + throwable.getMessage());
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    )
            );
        });
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}

