package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Màn hình quên mật khẩu.
 * Cho phép người dùng nhập email để nhận liên kết đặt lại mật khẩu.
 * Toàn bộ logic đặt lại do Firebase xử lý, ứng dụng chỉ gửi email.
 * Truy cập từ: LoginActivity (nhấn liên kết "Quên mật khẩu")
 * API ngoài: Firebase Auth - sendPasswordResetEmail()
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnResetPassword;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.edtEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);

        // Quay lại màn hình trước (LoginActivity)
        btnBack.setOnClickListener(v -> finish());
        btnResetPassword.setOnClickListener(v -> handleResetPassword());
    }

    /**
     * Gửi yêu cầu đặt lại mật khẩu qua Firebase.
     * Kiểm tra email không rỗng, sau đó gọi Firebase Auth.
     * Firebase tự tạo liên kết và gửi email cho người dùng.
     */
    private void handleResetPassword() {
        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnResetPassword.setEnabled(false);

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                    finish(); // Quay về LoginActivity
                })
                .addOnFailureListener(e -> {
                    btnResetPassword.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
