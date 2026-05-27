package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình sổ địa chỉ - hiển thị danh sách địa chỉ giao hàng.
 * Chức năng: xem danh sách, thêm mới, sửa, xóa.
 * Danh sách tự cập nhật khi dữ liệu thay đổi nhờ addValueEventListener.
 *
 * CSDL: Đọc và xóa tại addresses/{uid}
 * Truy cập từ: ProfileActivity (nhấn "Sổ địa chỉ")
 */
public class AddressBookActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAddresses;
    private AddressAdapter adapter;
    private List<Address> addressList;
    private Button btnAddAddress;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_book);

        recyclerViewAddresses = findViewById(R.id.recyclerViewAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnBack = findViewById(R.id.btnBack);

        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressList = new ArrayList<>();

        // Khởi tạo adapter với 2 callback: sửa và xóa
        adapter = new AddressAdapter(addressList, new AddressAdapter.OnAddressClickListener() {
            @Override
            public void onEditClick(Address address) {
                // Mở AddressFormActivity ở chế độ sửa (có ADDRESS_ID)
                Intent intent = new Intent(AddressBookActivity.this, AddressFormActivity.class);
                intent.putExtra("ADDRESS_ID", address.addressId);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Address address) {
                deleteAddressFromFirebase(address.addressId);
            }
        });

        recyclerViewAddresses.setAdapter(adapter);
        btnBack.setOnClickListener(v -> finish());

        // Mở AddressFormActivity ở chế độ thêm mới (không có ADDRESS_ID)
        btnAddAddress.setOnClickListener(v ->
                startActivity(new Intent(this, AddressFormActivity.class)));

        loadAddressesFromFirebase();
    }

    /**
     * Tải danh sách địa chỉ từ Firebase.
     * Dùng addValueEventListener (không phải addListenerForSingleValueEvent)
     * để danh sách tự cập nhật khi có thay đổi trên CSDL.
     */
    private void loadAddressesFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("addresses").child(user.getUid());
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Address address = data.getValue(Address.class);
                    if (address != null) addressList.add(address);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddressBookActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Xóa địa chỉ theo mã.
     * Gọi removeValue() tại nút addresses/{uid}/{addressId}.
     * Danh sách tự cập nhật nhờ ValueEventListener đã gắn trong loadAddressesFromFirebase().
     */
    private void deleteAddressFromFirebase(String addressId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("addresses")
                .child(user.getUid()).child(addressId)
                .removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show());
    }
}
