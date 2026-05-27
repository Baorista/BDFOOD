package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Màn hình chỉnh sửa hồ sơ cá nhân.
 * Cho phép cập nhật: họ tên, số điện thoại, ảnh đại diện.
 * Ảnh đại diện được tải lên Cloudinary, các thông tin khác lưu vào Firebase.
 *
 * Luồng xử lý khi lưu:
 *   - Nếu có ảnh mới: tải ảnh lên Cloudinary -> nhận URL -> lưu vào Firebase
 *   - Nếu không có ảnh mới: lưu trực tiếp vào Firebase
 *
 * API ngoài: Cloudinary - MediaManager.upload() (tải ảnh)
 * CSDL: Đọc và ghi users/{uid}
 * Truy cập từ: ProfileActivity (nhấn "Chỉnh sửa hồ sơ")
 */
public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private EditText edtFullName, edtPhone;
    private Button btnSaveProfile;
    private Uri selectedImageUri; // Đường dẫn ảnh đã chọn từ thư viện, null nếu chưa chọn

    /**
     * Bộ xử lý kết quả chọn ảnh từ thư viện.
     * Khi người dùng chọn ảnh, lưu URI và hiển thị tạm trên giao diện.
     */
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgAvatar.setImageURI(uri); // Hiển thị tạm trên giao diện
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Nhấn ảnh đại diện -> mở thư viện ảnh
        imgAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Nhấn lưu -> kiểm tra và xử lý
        btnSaveProfile.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = user.getUid();
            String name = edtFullName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            if (selectedImageUri != null) {
                uploadImageAndSaveProfile(userId, name, phone);
            } else {
                saveProfileToFirebase(userId, name, phone, null);
            }
        });
    }

    /**
     * Tải ảnh lên Cloudinary rồi lưu hồ sơ.
     * Sử dụng upload preset "avatar_upload_preset" (không cần xác thực).
     * public_id đặt bằng userId để ảnh cũ bị ghi đè khi cập nhật.
     * Khi tải thành công, nhận secure_url rồi gọi saveProfileToFirebase().
     */
    private void uploadImageAndSaveProfile(String userId, String name, String phone) {
        MediaManager.get().upload(selectedImageUri)
                .unsigned("avatar_upload_preset")
                .option("public_id", userId)
                .option("overwrite", true)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        saveProfileToFirebase(userId, name, phone, imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(EditProfileActivity.this, "Lỗi tải ảnh lên", Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    /**
     * Lưu thông tin hồ sơ vào Firebase Realtime Database.
     * Dùng updateChildren() thay vì setValue() để chỉ cập nhật các trường thay đổi,
     * không ghi đè toàn bộ bản ghi.
     * Trường updatedAt luôn được cập nhật để ghi nhận thời điểm sửa.
     */
    private void saveProfileToFirebase(String userId, String name, String phone, String imageUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        Map<String, Object> updates = new HashMap<>();

        if (!name.isEmpty()) updates.put("displayName", name);
        if (!phone.isEmpty()) updates.put("phone", phone);
        if (imageUrl != null) updates.put("photoUrl", imageUrl);
        updates.put("updatedAt", System.currentTimeMillis());

        dbRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show());
    }
}
