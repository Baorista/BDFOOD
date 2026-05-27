package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình danh sách sản phẩm yêu thích.
 * Hỗ trợ: xem danh sách, xóa khỏi yêu thích.
 * Chức năng thêm vào giỏ hàng từ Wishlist thuộc module thành viên khác.
 *
 * CSDL: Đọc wishlists/{uid}, products/. Ghi (xóa) wishlists/{uid}.
 */
public class WishlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WishlistAdapter adapter;
    private List<Product> productList;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        recyclerView = findViewById(R.id.recyclerViewWishlist);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();

        adapter = new WishlistAdapter(productList, product -> removeFromWishlist(product.productId));

        recyclerView.setAdapter(adapter);
        btnBack.setOnClickListener(v -> finish());

        loadWishlist();
    }

    /** Bước 1: Đọc danh sách mã sản phẩm yêu thích */
    private void loadWishlist() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("wishlists").child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> productIds = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            productIds.add(data.getKey());
                        }
                        if (productIds.isEmpty()) {
                            productList.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(WishlistActivity.this, "Chưa có sản phẩm yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            loadProductsByIds(productIds);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /** Bước 2: Đọc chi tiết từng sản phẩm theo mã */
    private void loadProductsByIds(List<String> ids) {
        productList.clear();
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");

        for (String id : ids) {
            productsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        product.productId = snapshot.getKey();
                        productList.add(product);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    /** Xóa sản phẩm khỏi danh sách yêu thích */
    private void removeFromWishlist(String productId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("wishlists")
                .child(user.getUid()).child(productId).removeValue()
                .addOnSuccessListener(a -> Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show());
    }
}
