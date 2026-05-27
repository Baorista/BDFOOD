package com.example.myapplication;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

/**
 * Màn hình form thêm/sửa địa chỉ giao hàng.
 * Dùng chung cho 2 chế độ, phân biệt bằng Intent extra "ADDRESS_ID":
 *   - Có ADDRESS_ID: chế độ sửa, tải dữ liệu cũ vào form
 *   - Không có: chế độ thêm mới, form trống
 *
 * Xử lý địa chỉ mặc định: khi bật, tắt mặc định của tất cả địa chỉ khác trước khi lưu.
 *
 * CSDL: Đọc và ghi addresses/{uid}/{addressId}
 * Truy cập từ: AddressBookActivity (nhấn thêm hoặc sửa)
 */
public class AddressFormActivity extends AppCompatActivity {

    private EditText edtReceiverName, edtPhone, edtCity, edtDistrict, edtDetailAddress;
    private SwitchCompat switchDefault;
    private Button btnSaveAddress;
    private ImageView btnBack;
    private TextView tvFormTitle;
    private String currentAddressId = null; // null = thêm mới, có giá trị = sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_form);

        // Ánh xạ view
        edtReceiverName = findViewById(R.id.edtReceiverName);
        edtPhone = findViewById(R.id.edtPhone);
        edtCity = findViewById(R.id.edtCity);
        edtDistrict = findViewById(R.id.edtDistrict);
        edtDetailAddress = findViewById(R.id.edtDetailAddress);
        switchDefault = findViewById(R.id.switchDefault);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);
        btnBack = findViewById(R.id.btnBack);
        tvFormTitle = findViewById(R.id.tvFormTitle);

        // Kiểm tra chế độ: có ADDRESS_ID trong Intent thì là sửa
        currentAddressId = getIntent().getStringExtra("ADDRESS_ID");
        if (currentAddressId != null) {
            if (tvFormTitle != null) tvFormTitle.setText("Sửa địa chỉ");
            loadExistingData();
        }

        btnBack.setOnClickListener(v -> finish());
        btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    /**
     * Tải dữ liệu địa chỉ cũ khi ở chế độ sửa.
     * Đọc từ addresses/{uid}/{addressId} và điền vào form.
     */
    private void loadExistingData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("addresses")
                .child(user.getUid()).child(currentAddressId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Address address = snapshot.getValue(Address.class);
                        if (address != null) {
                            edtReceiverName.setText(address.recipientName);
                            edtPhone.setText(address.phone);
                            edtCity.setText(address.province);
                            edtDistrict.setText(address.district);
                            edtDetailAddress.setText(address.detail);
                            switchDefault.setChecked(address.isDefault);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddressFormActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Lưu địa chỉ vào Firebase.
     * Nếu thêm mới: tạo khóa tự động bằng push().getKey().
     * Nếu bật mặc định: gọi resetExistingDefaultAddress() để tắt mặc định cũ trước.
     */
    private void saveAddress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show(); return; }

        String uid = user.getUid();
        String name = edtReceiverName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String district = edtDistrict.getText().toString().trim();
        String detail = edtDetailAddress.getText().toString().trim();
        boolean isDefault = switchDefault.isChecked();

        if (name.isEmpty() || phone.isEmpty() || city.isEmpty() || district.isEmpty() || detail.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("addresses").child(uid);

        // Thêm mới: tạo khóa tự động
        if (currentAddressId == null) currentAddressId = dbRef.push().getKey();

        Address address = new Address();
        address.addressId = currentAddressId;
        address.recipientName = name;
        address.phone = phone;
        address.province = city;
        address.district = district;
        address.ward = "";
        address.detail = detail;
        address.isDefault = isDefault;

        if (isDefault) {
            resetExistingDefaultAddress(dbRef, address);
        } else {
            pushAddressToFirebase(dbRef, address);
        }
    }

    /**
     * Tắt mặc định của tất cả địa chỉ khác trước khi lưu địa chỉ mới là mặc định.
     * Truy vấn tất cả địa chỉ có isDefault = true, set về false (trừ địa chỉ hiện tại).
     * Đảm bảo tại mọi thời điểm chỉ có tối đa 1 địa chỉ mặc định.
     */
    private void resetExistingDefaultAddress(DatabaseReference dbRef, Address newAddress) {
        dbRef.orderByChild("isDefault").equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            if (!data.getKey().equals(newAddress.addressId)) {
                                data.getRef().child("isDefault").setValue(false);
                            }
                        }
                        pushAddressToFirebase(dbRef, newAddress);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddressFormActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Ghi dữ liệu địa chỉ vào Firebase và đóng màn hình. */
    private void pushAddressToFirebase(DatabaseReference dbRef, Address address) {
        dbRef.child(address.addressId).setValue(address)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show());
    }
}
