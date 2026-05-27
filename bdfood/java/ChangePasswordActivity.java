package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Màn hình đổi mật khẩu.
 * Yêu cầu nhập mật khẩu cũ để xác thực lại trước khi cho phép đổi.
 * Quy trình 2 bước: reauthenticate (xác thực lại) -> updatePassword (cập nhật).
 * Truy cập từ: ProfileActivity (nhấn mục "Đổi mật khẩu")
 * API ngoài: Firebase Auth - reauthenticate(), updatePassword()
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtOldPassword, edtNewPassword, edtConfirmPassword;
    private Button btnChangePassword;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    /**
     * Xử lý đổi mật khẩu.
     * Bước 1: Kiểm tra đầu vào (không rỗng, mật khẩu mới >= 6 ký tự, xác nhận khớp)
     * Bước 2: Xác thực lại bằng mật khẩu cũ (reauthenticate)
     * Bước 3: Cập nhật mật khẩu mới (updatePassword)
     */
    private void handleChangePassword() {
        String oldPass = edtOldPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        // Kiểm tra đầu vào
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        btnChangePassword.setEnabled(false);

        // Tạo chứng chỉ xác thực từ email + mật khẩu cũ
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

        // Bước 2: Xác thực lại - Firebase yêu cầu cho thao tác nhạy cảm
        user.reauthenticate(credential).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                // Bước 3: Cập nhật mật khẩu mới
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    btnChangePassword.setEnabled(true);
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi cập nhật mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnChangePassword.setEnabled(true);
                Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
