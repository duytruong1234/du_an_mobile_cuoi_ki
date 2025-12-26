package vn.duytruong.appbandienthoai.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.User;
import vn.duytruong.appbandienthoai.utils.Utils;

public class UpdateProfileActivity extends AppCompatActivity {
    private static final String TAG = "UpdateProfileActivity";

    private Toolbar toolbar;
    private TextView tvUserId, tvPasswordSectionTitle, tvPasswordSectionDesc;
    private TextInputEditText edtUsername, edtEmail, edtMobile, edtPassword, edtConfirmPassword;
    private View dividerPassword;
    private com.google.android.material.textfield.TextInputLayout layoutPassword, layoutConfirmPassword;
    private Button btnUpdate;
    private ProgressBar progressBar;

    private RequestQueue requestQueue;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        initViews();
        initToolbar();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvUserId = findViewById(R.id.tvUserId);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtMobile = findViewById(R.id.edtMobile);

        // Password section views
        dividerPassword = findViewById(R.id.dividerPassword);
        tvPasswordSectionTitle = findViewById(R.id.tvPasswordSectionTitle);
        tvPasswordSectionDesc = findViewById(R.id.tvPasswordSectionDesc);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);

        requestQueue = Volley.newRequestQueue(this);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cập nhật thông tin");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        currentUser = Utils.user_current;

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để cập nhật thông tin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị thông tin hiện tại
        tvUserId.setText("ID: #" + currentUser.getId());
        edtUsername.setText(currentUser.getUsername());
        edtEmail.setText(currentUser.getEmail());
        edtMobile.setText(currentUser.getMobile());

        // Nếu là tài khoản Google, ẩn TOÀN BỘ phần đổi mật khẩu
        if (currentUser.isGoogleAccount()) {
            dividerPassword.setVisibility(View.GONE);
            tvPasswordSectionTitle.setVisibility(View.GONE);
            tvPasswordSectionDesc.setVisibility(View.GONE);
            layoutPassword.setVisibility(View.GONE);
            layoutConfirmPassword.setVisibility(View.GONE);

            // Hiển thị thông báo
            Toast.makeText(this, "Tài khoản Google - Mật khẩu được quản lý bởi Google", Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        btnUpdate.setOnClickListener(v -> validateAndUpdateProfile());
    }

    private void validateAndUpdateProfile() {
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String mobile = edtMobile.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validate username
        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Tên hiển thị không được để trống");
            edtUsername.requestFocus();
            return;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không đúng định dạng");
            edtEmail.requestFocus();
            return;
        }

        // Validate mobile (optional but if provided, should be valid)
        if (!TextUtils.isEmpty(mobile) && mobile.length() < 10) {
            edtMobile.setError("Số điện thoại phải có ít nhất 10 số");
            edtMobile.requestFocus();
            return;
        }

        // Validate password if user wants to change it (chỉ cho tài khoản thường)
        if (!TextUtils.isEmpty(password)) {
            // Không cho phép đổi mật khẩu nếu là tài khoản Google
            if (currentUser.isGoogleAccount()) {
                Toast.makeText(this, "Tài khoản Google không thể đổi mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                edtPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                edtConfirmPassword.requestFocus();
                return;
            }
        }

        // All validation passed, update profile
        updateProfile(username, email, mobile, password);
    }

    private void updateProfile(String username, String email, String mobile, String password) {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        String url = Utils.BASE_URL + "updateProfile.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        if (success) {
                            // Update local user data
                            if (jsonObject.has("user")) {
                                JSONObject userObj = jsonObject.getJSONObject("user");
                                currentUser.setUsername(userObj.getString("username"));
                                currentUser.setEmail(userObj.getString("email"));
                                currentUser.setMobile(userObj.getString("mobile"));

                                // Update global user object
                                Utils.user_current = currentUser;
                            }

                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            edtPassword.setText("");
                            edtConfirmPassword.setText("");

                            // Close activity after 1 second
                            new android.os.Handler().postDelayed(this::finish, 1000);
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Log.e(TAG, "Error updating profile", error);
                    Toast.makeText(this, "Lỗi kết nối. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("iduser", String.valueOf(currentUser.getId()));
                params.put("username", username);
                params.put("email", email);
                params.put("mobile", mobile);

                // Only send password if user wants to change it
                if (!TextUtils.isEmpty(password)) {
                    params.put("password", password);
                }

                return params;
            }
        };

        requestQueue.add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}

