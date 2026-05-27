package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Màn hình đăng ký tài khoản mới.
 * Thực hiện 2 thao tác liên tiếp trên 2 dịch vụ:
 *   1. Tạo tài khoản xác thực trên Firebase Auth (email + mật khẩu)
 *   2. Lưu thông tin bổ sung vào Realtime Database tại nút users/{uid}
 *
 * Sau đăng ký thành công, chuyển về LoginActivity để người dùng đăng nhập lần đầu.
 *
 * API ngoài: Firebase Auth - createUserWithEmailAndPassword()
 * CSDL: Ghi vào users/{uid}
 * Truy cập từ: LoginActivity (nhấn "Tạo tài khoản mới")
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    /**
     * Xử lý đăng ký.
     * Kiểm tra đầu vào: các trường không rỗng, mật khẩu khớp.
     * Gọi Firebase Auth tạo tài khoản, nếu thành công thì lưu thông tin vào CSDL.
     */
    private void handleRegistration() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase(user.getUid(), email, fullName);
                        }
                    } else {
                        btnRegister.setEnabled(true);
                        Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Lưu thông tin người dùng vào Realtime Database tại nút users/{uid}.
     * Các trường: uid, email, displayName, photoUrl, phone, role, createdAt, updatedAt.
     * photoUrl và phone ban đầu là null, được cập nhật khi chỉnh sửa hồ sơ.
     * role mặc định là "customer".
     */
    private void saveUserToDatabase(String uid, String email, String fullName) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        long currentTime = System.currentTimeMillis();

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("displayName", fullName);
        userData.put("photoUrl", null);
        userData.put("phone", null);
        userData.put("role", "customer");
        userData.put("createdAt", currentTime);
        userData.put("updatedAt", currentTime);

        dbRef.setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                });
    }
}
