package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Màn hình đăng nhập - điểm vào đầu tiên của ứng dụng.
 * Chức năng chính:
 *   - Đăng nhập bằng email/mật khẩu qua Firebase Authentication
 *   - Đăng nhập tự động: kiểm tra phiên tại onStart(), nếu còn hạn thì
 *     chuyển thẳng vào ProfileActivity mà không cần nhập lại
 *   - Điều hướng sang RegisterActivity và ForgotPasswordActivity
 *
 * API ngoài: Firebase Auth - signInWithEmailAndPassword()
 * CSDL: Không truy cập trực tiếp (Firebase Auth tự quản lý phiên)
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Lấy thể hiện Firebase Auth (singleton, dùng chung toàn ứng dụng)
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view từ layout
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Sự kiện đăng nhập
        btnLogin.setOnClickListener(v -> handleLogin());

        // Chuyển sang màn hình đăng ký
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // Chuyển sang màn hình quên mật khẩu
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(this, ForgotPasswordActivity.class)));
        }
    }

    /**
     * Kiểm tra phiên đăng nhập khi màn hình hiển thị.
     * Firebase SDK tự lưu mã phiên trong bộ nhớ nội bộ ứng dụng.
     * Nếu getCurrentUser() != null: người dùng đã đăng nhập trước đó,
     * chuyển thẳng vào ProfileActivity và đóng LoginActivity.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
    }

    /**
     * Xử lý đăng nhập.
     * Bước 1: Kiểm tra email và mật khẩu không rỗng
     * Bước 2: Vô hiệu hóa nút đăng nhập tránh gửi yêu cầu trùng
     * Bước 3: Gọi Firebase Auth signInWithEmailAndPassword()
     * Bước 4: Nếu thành công -> chuyển sang ProfileActivity, đóng LoginActivity
     *         Nếu thất bại -> hiện thông báo lỗi chung (bảo mật, không nói rõ lỗi gì)
     */
    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, ProfileActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
