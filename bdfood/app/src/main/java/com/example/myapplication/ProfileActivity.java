package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Màn hình hồ sơ cá nhân - trung tâm điều hướng chính của module.
 * Hiển thị thông tin người dùng (tên, email, ảnh đại diện) và các mục menu:
 * chỉnh sửa hồ sơ, sổ địa chỉ, lịch sử mua hàng, sản phẩm yêu thích,
 * đổi mật khẩu, đăng xuất.
 * 
 * Dữ liệu được tải lại mỗi khi màn hình hiển thị (onResume) để đảm bảo
 * thông tin luôn cập nhật sau khi chỉnh sửa.
 * 
 * CSDL: Đọc từ users/{uid}
 * Truy cập từ: LoginActivity (sau đăng nhập thành công)
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvUserName, tvUserEmail;
    private TextView btnEditProfile, btnAddressBook, btnOrderHistory;
    private TextView btnWishlist, btnChangePassword, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ view
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnAddressBook = findViewById(R.id.btnAddressBook);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnWishlist = findViewById(R.id.btnWishlist);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        // Thiết lập sự kiện cho các mục menu
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại thông tin mỗi khi quay lại màn hình
        loadUserInfo();
    }

    /**
     * Tải thông tin người dùng từ Firebase Realtime Database.
     * Đọc các trường displayName, email, photoUrl tại nút users/{uid}.
     * Ảnh đại diện được tải bằng thư viện Glide.
     */
    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String name = snapshot.child("displayName").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String photo = snapshot.child("photoUrl").getValue(String.class);

                        if (name != null) tvUserName.setText(name);
                        if (email != null) tvUserEmail.setText(email);
                        if (photo != null && !photo.isEmpty()) {
                            Glide.with(ProfileActivity.this).load(photo).into(imgAvatar);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Thiết lập sự kiện cho các mục menu trên giao diện hồ sơ.
     * Mỗi mục mở một Activity tương ứng qua startActivity.
     * Riêng đăng xuất gọi FirebaseAuth.signOut() rồi quay về LoginActivity
     * với cờ xóa toàn bộ lịch sử quay lại.
     */
    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnAddressBook.setOnClickListener(v ->
                startActivity(new Intent(this, AddressBookActivity.class)));

        // Lịch sử mua hàng thuộc module TV4, chưa tích hợp
        btnOrderHistory.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng thuộc module khác", Toast.LENGTH_SHORT).show());

        btnWishlist.setOnClickListener(v ->
                startActivity(new Intent(this, WishlistActivity.class)));

        btnChangePassword.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        // Đăng xuất: xóa phiên + xóa back stack
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
