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

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Màn hình chỉnh sửa hồ sơ cá nhân.
 * Tải dữ liệu hiện tại từ Firebase, cho phép sửa tên, SĐT, ảnh đại diện.
 * Ảnh được tải lên Cloudinary, thông tin khác lưu vào Firebase.
 *
 * API ngoài: Cloudinary - MediaManager.upload()
 * CSDL: Đọc và ghi users/{uid}
 */
public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private EditText edtFullName, edtPhone;
    private Button btnSaveProfile;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgAvatar.setImageURI(uri);
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

        // Tải dữ liệu hiện tại từ Firebase
        loadCurrentUserData();

        // Nhấn ảnh -> mở thư viện ảnh
        imgAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Nhấn lưu
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
     * Tải dữ liệu người dùng hiện tại từ Firebase và điền vào form.
     * Ảnh đại diện được tải bằng Glide.
     */
    private void loadCurrentUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String name = snapshot.child("displayName").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String photo = snapshot.child("photoUrl").getValue(String.class);

                        if (name != null) edtFullName.setText(name);
                        if (phone != null) edtPhone.setText(phone);
                        if (photo != null && !photo.isEmpty()) {
                            Glide.with(EditProfileActivity.this).load(photo).into(imgAvatar);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(EditProfileActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
/**
     * Tải ảnh lên Cloudinary. Dùng chung public_id để ghi đè ảnh cũ (tránh đầy dung lượng).
     *
     * * @param userId Public_ID cho file trên Cloudinary (trùng UID của người dùng)
     * * @param name   Họ tên cập nhật
     * * @param phone  Số điện thoại cập nhật
*/
    private void uploadImageAndSaveProfile(String userId, String name, String phone) {
        btnSaveProfile.setEnabled(false);
        MediaManager.get().upload(selectedImageUri)
                .unsigned("avatar_upload_preset")
                .option("public_id", userId)
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
                        btnSaveProfile.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this, "Lỗi tải ảnh lên", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

/**
     * Cập nhật thông tin trên Firebase.
     * Dùng updateChildren() thay vì setValue() để không làm mất các node dữ liệu khác của user (như role, email...).
     *
     * * @param userId   Mã UID người dùng
     * * @param name     Họ tên
     * * @param phone    Số điện thoại
     * * @param imageUrl Link ảnh CDN sau khi up (truyền null nếu người dùng không đổi ảnh)
*/
    private void saveProfileToFirebase(String userId, String name, String phone, String imageUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        Map<String, Object> updates = new HashMap<>();

        if (!name.isEmpty()) updates.put("displayName", name);
        if (!phone.isEmpty()) updates.put("phone", phone);
        if (imageUrl != null) updates.put("photoUrl", imageUrl);
        updates.put("updatedAt", System.currentTimeMillis());

        dbRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại ProfileActivity
                })
                .addOnFailureListener(e -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    /** Nhấn nút Back hệ thống để quay lại */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
