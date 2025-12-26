package vn.duytruong.appbandienthoai.activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.retrofit.ApiBanHang;
import vn.duytruong.appbandienthoai.retrofit.RetrofitClient;
import vn.duytruong.appbandienthoai.utils.Utils;

public class DangNhapActivity extends AppCompatActivity {
    private static final String TAG = "DangNhapActivity";
    private static final int RC_SIGN_IN = 9001;

    TextView txtdangki, txtresetpass;
    EditText email, pass;
    Button btndangnhap, btnGoogleSignIn;
    ApiBanHang apiBanHang;
    GoogleSignInClient mGoogleSignInClient;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        try {
            setContentView(R.layout.activity_dang_nhap);
            Log.d(TAG, "setContentView success");
            initView();
            Log.d(TAG, "initView success");
            initControl();
            Log.d(TAG, "initControl success");
        } catch (Exception e) {
            Log.e(TAG, "onCreate error: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage() + "\nKiểm tra Logcat để biết chi tiết", Toast.LENGTH_LONG).show();
            // Không finish() để user có thể thấy thông báo lỗi
        }
    }

    private void initView() {
        try {
            Paper.init(this);
            apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
            btndangnhap = findViewById(R.id.btnDangNhap);
            btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
            txtdangki = findViewById(R.id.txtdangki);
            txtresetpass = findViewById(R.id.txtresetpass);
            email = findViewById(R.id.email);
            pass = findViewById(R.id.pass);

            // Kiểm tra null để tránh crash
            if (btndangnhap == null || btnGoogleSignIn == null || email == null || pass == null) {
                Log.e(TAG, "Some views are null! Check layout file.");
                Toast.makeText(this, "Lỗi tải giao diện. Vui lòng khởi động lại ứng dụng.", Toast.LENGTH_LONG).show();
                return;
            }

            // Cấu hình Google Sign-In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("123992685047-9ih70tan9l5a1d6t7dad52grqcibm7nk.apps.googleusercontent.com") // Web Client ID
                    .requestEmail()
                    .requestProfile()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            // read data - CHỈ điền sẵn email và pass, KHÔNG tự động đăng nhập
            if (Paper.book().read("email") != null && Paper.book().read("pass") != null){
                email.setText(Paper.book().read("email"));
                pass.setText(Paper.book().read("pass"));
            }
        } catch (Exception e) {
            Log.e(TAG, "initView error: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initControl() {
        txtdangki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DangNhapActivity.this, DangKiActivity.class);
                startActivity(intent);
            }
        });

        txtresetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DangNhapActivity.this, ResetPassActivity.class);
                startActivity(intent);
            }
        });

        btndangnhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_email = email.getText().toString().trim();
                String str_pass = pass.getText().toString().trim();
                if (TextUtils.isEmpty(str_email)) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập Email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(str_pass)) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập Pass", Toast.LENGTH_SHORT).show();
                } else {
                    Paper.book().write("email", str_email);
                    Paper.book().write("pass", str_pass);
                    dangNhap(str_email, str_pass);
                }
            }
        });

        // Nút đăng nhập Google
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    }

    private void dangNhap(String email, String pass) {
        compositeDisposable.add(apiBanHang.dangNhap(email, pass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            if (userModel != null) {
                                if (userModel.isSuccess() && userModel.getResult() != null && !userModel.getResult().isEmpty()) {
                                    isLogin = true;
                                    Paper.book().write("islogin", isLogin);
                                    Utils.user_current = userModel.getResult().get(0);

                                    // LƯU USER VÀO PAPER.DB - QUAN TRỌNG!
                                    Paper.book().write("user", Utils.user_current);

                                    // Log để debug
                                    Log.d(TAG, "Login success: email=" + Utils.user_current.getEmail() +
                                              ", role=" + Utils.user_current.getRole() +
                                              ", isAdmin=" + Utils.user_current.isAdmin());

                                    Toast.makeText(getApplicationContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), userModel.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Đăng nhập thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void signInWithGoogle() {
        Log.d(TAG, "Bắt đầu Google Sign-In...");

        // Sign out trước để đảm bảo hiện popup chọn tài khoản
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Log.d(TAG, "Đã sign out, bắt đầu sign in intent...");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Đăng nhập thành công với Google
            String googleEmail = account.getEmail();
            String googleName = account.getDisplayName();
            String googleId = account.getId();

            Log.d(TAG, "Google Sign-In thành công:");
            Log.d(TAG, "Email: " + googleEmail);
            Log.d(TAG, "Name: " + googleName);
            Log.d(TAG, "ID: " + googleId);

            // Gọi API đăng ký/đăng nhập với Google
            dangNhapGoogle(googleEmail, googleName);

        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            Log.e(TAG, "Error code: " + e.getStatusCode());
            Log.e(TAG, "Error message: " + e.getMessage());

            // Hiển thị thông báo lỗi chi tiết
            String errorMessage;
            switch (e.getStatusCode()) {
                case 10: // DEVELOPER_ERROR
                    errorMessage = "Lỗi cấu hình Google Sign-In. Kiểm tra:\n" +
                            "1. SHA-1 fingerprint trong Firebase Console\n" +
                            "2. google-services.json đã được cập nhật\n" +
                            "3. Package name khớp với Firebase project";
                    Log.e(TAG, "DEVELOPER_ERROR: Kiểm tra cấu hình Firebase");
                    Log.e(TAG, "SHA-1 hiện tại từ debug keystore cần được thêm vào Firebase Console");
                    break;
                case 7: // NETWORK_ERROR
                    errorMessage = "Lỗi kết nối mạng. Kiểm tra internet và thử lại.";
                    break;
                case 12500: // Sign in cancelled
                    errorMessage = "Đăng nhập bị hủy bởi người dùng";
                    return; // Không hiện toast cho trường hợp này
                case 12501: // Sign in currently in progress
                    errorMessage = "Đăng nhập đang trong quá trình xử lý";
                    break;
                default:
                    errorMessage = "Đăng nhập Google thất bại\nMã lỗi: " + e.getStatusCode() +
                                 "\nChi tiết: " + e.getMessage();
                    break;
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void dangNhapGoogle(String googleEmail, String googleName) {
        // Tạo mật khẩu mặc định cho tài khoản Google (vì backend cần password)
        String defaultPassword = "google_" + Math.abs(googleEmail.hashCode());

        // Tạo số điện thoại mặc định (vì backend yêu cầu không được rỗng)
        String defaultMobile = "0000000000"; // Số điện thoại mặc định cho tài khoản Google

        Log.d(TAG, "Đang xử lý đăng nhập Google với email: " + googleEmail);
        Log.d(TAG, "Mật khẩu tạo: " + defaultPassword);
        Log.d(TAG, "Tạo/kiểm tra tài khoản trên server...");

        // Đăng ký tài khoản Google nếu chưa tồn tại
        compositeDisposable.add(apiBanHang.dangKi(googleEmail, defaultPassword, googleName, defaultMobile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            Log.d(TAG, "Đăng ký thành công: " + userModel.getMessage());
                            // Sau khi đăng ký thành công, đăng nhập
                            dangNhap(googleEmail, defaultPassword);
                        },
                        throwable -> {
                            // Nếu lỗi "Email đã tồn tại", thử đăng nhập luôn
                            Log.d(TAG, "Lỗi đăng ký (có thể đã tồn tại): " + throwable.getMessage());
                            Log.d(TAG, "Thử đăng nhập với tài khoản đã tồn tại...");
                            dangNhap(googleEmail, defaultPassword);
                        }
                ));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}

