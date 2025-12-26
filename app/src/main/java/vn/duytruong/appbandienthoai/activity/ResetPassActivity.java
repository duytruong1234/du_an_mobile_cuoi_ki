package vn.duytruong.appbandienthoai.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class ResetPassActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText edtEmail, edtOTP, edtNewPassword, edtConfirmPassword;
    Button btnSendOTP, btnResetPassword;
    TextView tvOTPSent;
    ProgressBar progressBar;

    ApiBanHang apiBanHang;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String currentEmail = "";
    private boolean otpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);

        initView();
        initControl();
    }

    private void initView() {
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);

        toolbar = findViewById(R.id.toolbar);
        edtEmail = findViewById(R.id.edtEmail);
        edtOTP = findViewById(R.id.edtOTP);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvOTPSent = findViewById(R.id.tvOTPSent);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ẩn các trường nhập OTP và mật khẩu ban đầu
        hideOTPFields();
    }

    private void initControl() {
        btnSendOTP.setOnClickListener(v -> sendOTP());
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void sendOTP() {
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSendOTP.setEnabled(false);

        compositeDisposable.add(apiBanHang.sendResetOTP(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            progressBar.setVisibility(View.GONE);
                            btnSendOTP.setEnabled(true);

                            if (userModel.isSuccess()) {
                                currentEmail = email;
                                otpSent = true;
                                showOTPFields();
                                Toast.makeText(this, userModel.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, userModel.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            progressBar.setVisibility(View.GONE);
                            btnSendOTP.setEnabled(true);
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void resetPassword() {
        if (!otpSent) {
            Toast.makeText(this, "Vui lòng nhấn 'Gửi mã OTP' trước", Toast.LENGTH_SHORT).show();
            return;
        }

        String otp = edtOTP.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (otp.length() != 6) {
            Toast.makeText(this, "Mã OTP phải có 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setEnabled(false);

        compositeDisposable.add(apiBanHang.verifyOTPAndResetPassword(currentEmail, otp, newPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            progressBar.setVisibility(View.GONE);
                            btnResetPassword.setEnabled(true);

                            if (userModel.isSuccess()) {
                                Toast.makeText(this, userModel.getMessage(), Toast.LENGTH_LONG).show();
                                finish(); // Quay lại màn hình đăng nhập
                            } else {
                                Toast.makeText(this, userModel.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            progressBar.setVisibility(View.GONE);
                            btnResetPassword.setEnabled(true);
                            Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void hideOTPFields() {
        edtOTP.setVisibility(View.GONE);
        edtNewPassword.setVisibility(View.GONE);
        edtConfirmPassword.setVisibility(View.GONE);
        btnResetPassword.setVisibility(View.GONE);
        tvOTPSent.setVisibility(View.GONE);
    }

    private void showOTPFields() {
        edtOTP.setVisibility(View.VISIBLE);
        edtNewPassword.setVisibility(View.VISIBLE);
        edtConfirmPassword.setVisibility(View.VISIBLE);
        btnResetPassword.setVisibility(View.VISIBLE);
        tvOTPSent.setVisibility(View.VISIBLE);
        edtEmail.setEnabled(false); // Khóa trường email sau khi gửi OTP
        btnSendOTP.setText("Gửi lại OTP");
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}

