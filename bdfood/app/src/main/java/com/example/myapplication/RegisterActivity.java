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
 * Màn hình đăng ký. Tạo tài khoản Firebase Auth + lưu thông tin vào CSDL.
 * Điều hướng: LoginActivity (nút Đăng nhập hoặc sau đăng ký thành công).
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister, btnLogin;
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
        btnLogin = findViewById(R.id.btnLogin);

        btnRegister.setOnClickListener(v -> handleRegistration());
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
/**
     * Xử lý sự kiện khi người dùng nhấn nút Đăng ký.
     * Thực hiện kiểm tra tính hợp lệ của dữ liệu (validation) 
     * và gọi API của Firebase Authentication để tạo tài khoản.
*/
    private void handleRegistration() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
// 1. Kiểm tra dữ liệu đầu vào không được để trống
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
// 2. Xác thực mật khẩu nhập lại phải khớp
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }
// 3. Vô hiệu hóa nút bấm để tránh spam request trong lúc chờ API phản hồi
        btnRegister.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 4. Tạo tài khoản trên Firebase Authentication
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) saveUserToDatabase(user.getUid(), email, fullName);
                    } else {
                        // Tạo Auth thất bại, mở khóa lại nút bấm và báo lỗi
                        btnRegister.setEnabled(true);
                        Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
/**
     * Lưu thông tin chi tiết của người dùng vào Firebase Realtime Database
     * sau khi đã khởi tạo thành công bên Authentication.
     * * @param uid      Mã định danh duy nhất của người dùng (từ Firebase Auth)
     * * @param email    Email dùng để đăng nhập
     * * @param fullName Họ và tên hiển thị do người dùng nhập từ form
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
                    // Lưu ý: Cơ chế mặc định Firebase Auth sẽ tự đăng nhập user ở bước này.
                    // Nếu muốn chặn tự động đăng nhập (như trong báo cáo), cần gọi mAuth.signOut() tại đây.
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }
}
