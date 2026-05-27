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
 * Màn hình đăng nhập. Hỗ trợ đăng nhập tự động.
 * Điều hướng: Đăng ký, Quên mật khẩu, ProfileActivity (sau đăng nhập).
 */
public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kiểm tra phiên đăng nhập hiện tại (Session). 
        // Đặt ở onStart() để đảm bảo mỗi khi Activity này hiển thị lại lên màn hình, 
        // nếu FirebaseUser đã tồn tại, ứng dụng sẽ tự động vào thẳng Profile để tối ưu UX.
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }
/**
     * Kiểm tra phiên đăng nhập hiện tại (Session). 
     * Đặt ở onStart() để đảm bảo mỗi khi Activity này hiển thị lại, 
     * nếu FirebaseUser đã tồn tại, ứng dụng sẽ tự động vào thẳng Profile để tối ưu UX.
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
     * Xử lý luồng đăng nhập email/mật khẩu với Firebase Auth.
     * Tự động khóa UI trong lúc call API để ngăn chặn spam request.
*/
    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        // Vô hiệu hóa nút bấm trong quá trình gọi API Firebase để tránh người dùng nhấn đúp (spam request) gây lỗi luồng.
        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Mở khóa lại nút bấm bất kể kết quả API trả về thành công hay thất bại
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, ProfileActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
