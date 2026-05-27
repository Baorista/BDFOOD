package com.example.myapplication;

/**
 * Lớp mô hình dữ liệu địa chỉ giao hàng.
 * Ánh xạ với cấu trúc tại nút addresses/{uid}/{addressId} trên Firebase.
 * Mỗi người dùng có thể có nhiều địa chỉ, tối đa 1 địa chỉ mặc định.
 */
public class Address {
    public String addressId;      // Mã duy nhất, tạo bởi push().getKey()
    public String recipientName;  // Tên người nhận hàng
    public String phone;          // Số điện thoại người nhận
    public String province;       // Tỉnh/thành phố
    public String district;       // Quận/huyện
    public String ward;           // Phường/xã
    public String detail;         // Địa chỉ chi tiết (số nhà, đường)
    public boolean isDefault;     // Đánh dấu mặc định, tối đa 1 tại mọi thời điểm

    /** Constructor rỗng bắt buộc cho Firebase deserialization */
    public Address() {}
}
