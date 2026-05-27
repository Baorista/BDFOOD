package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình chọn địa chỉ giao hàng khi thanh toán.
 * Hiển thị danh sách địa chỉ, người dùng nhấn vào một mục để chọn.
 * Kết quả (mã địa chỉ) được trả về cho màn hình gọi qua setResult().
 *
 * Được gọi từ: CheckoutActivity (module TV3) qua startActivityForResult()
 * CSDL: Đọc addresses/{uid}
 */
public class AddressSelectActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Address> addressList;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_book); // Dùng lại giao diện danh sách địa chỉ

        recyclerView = findViewById(R.id.recyclerViewAddresses);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressList = new ArrayList<>();

        // Khi nhấn vào mục: trả kết quả về Activity gọi
        AddressAdapter adapter = new AddressAdapter(addressList, new AddressAdapter.OnAddressClickListener() {
            @Override
            public void onEditClick(Address address) {
                // Trong chế độ chọn: nhấn vào = chọn địa chỉ này
                Intent result = new Intent();
                result.putExtra("SELECTED_ADDRESS_ID", address.addressId);
                setResult(RESULT_OK, result);
                finish();
            }

            @Override
            public void onDeleteClick(Address address) {
                // Không cho xóa trong chế độ chọn
            }
        });

        recyclerView.setAdapter(adapter);
        btnBack.setOnClickListener(v -> finish());

        loadAddresses(adapter);
    }

    /** Tải danh sách địa chỉ từ Firebase */
    private void loadAddresses(AddressAdapter adapter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("addresses").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        addressList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Address addr = data.getValue(Address.class);
                            if (addr != null) addressList.add(addr);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddressSelectActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
