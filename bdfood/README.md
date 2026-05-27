# BD Food - Module cá nhân: Nguyễn Kim Bảo (B22DCAT031)

## Cách mở trong Android Studio
1. Mở Android Studio -> File -> Open -> chọn thư mục BDFood
2. Đợi Gradle sync xong
3. Thay file app/google-services.json bằng file thật từ Firebase Console
4. Run trên thiết bị/máy ảo

## Lưu ý quan trọng
- File google-services.json hiện là placeholder, CẦN THAY bằng file thật
- Cần bật Firebase Auth (Email/Password) và Realtime Database trên Firebase Console
- Cần tài khoản Cloudinary, tạo upload preset "avatar_upload_preset" (unsigned)
- Font Nunito đã bị xóa khỏi layout để tránh lỗi, có thể thêm lại sau

## Danh sách file
- 16 file Java trong app/src/main/java/com/example/myapplication/
- 10 file layout XML trong app/src/main/res/layout/
- Tất cả đã được comment đầy đủ
