package com.example.myapplication;

/**
 * Lớp mô hình dữ liệu người dùng.
 * Ánh xạ trực tiếp với cấu trúc dữ liệu tại nút users/{uid} trên Firebase Realtime Database.
 * Được tạo khi đăng ký và cập nhật khi chỉnh sửa hồ sơ.
 */
public class User {
    public String uid;           // Mã định danh duy nhất, tạo tự động bởi Firebase Auth
    public String email;         // Email đăng nhập
    public String displayName;   // Họ tên hiển thị
    public String photoUrl;      // Đường dẫn ảnh đại diện trên Cloudinary, null nếu chưa có
    public String phone;         // Số điện thoại (10 số, bắt đầu bằng 0), null nếu chưa nhập
    public String role;          // Vai trò: "customer" (mặc định khi đăng ký)
    public long createdAt;       // Thời điểm tạo tài khoản (millisecond)
    public long updatedAt;       // Thời điểm cập nhật gần nhất (millisecond)

    /** Constructor rỗng bắt buộc cho Firebase deserialization */
    public User() {}
}
