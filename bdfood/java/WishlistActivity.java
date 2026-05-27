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
 * Hiển thị dạng lưới 2 cột các sản phẩm người dùng đã đánh dấu yêu thích.
 * Hỗ trợ: xóa khỏi danh sách, thêm vào giỏ hàng.
 *
 * Dữ liệu được tải 2 bước:
 *   Bước 1: Đọc wishlists/{uid} lấy danh sách mã sản phẩm
 *   Bước 2: Đọc products/ lấy chi tiết từng sản phẩm
 *
 * CSDL: Đọc wishlists/{uid}, products/. Ghi carts/{uid} khi thêm giỏ.
 * Truy cập từ: ProfileActivity (nhấn mục "Sản phẩm yêu thích")
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

        // Lưới 2 cột
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();

        adapter = new WishlistAdapter(productList, new WishlistAdapter.OnWishlistActionListener() {
            @Override
            public void onRemoveWishlist(Product product) {
                removeFromWishlist(product.productId);
            }

            @Override
            public void onAddToCart(Product product) {
                addToCart(product);
            }
        });

        recyclerView.setAdapter(adapter);
        btnBack.setOnClickListener(v -> finish());

        loadWishlist();
    }

    /**
     * Bước 1: Đọc danh sách mã sản phẩm yêu thích từ wishlists/{uid}.
     * Mỗi khóa con là một mã sản phẩm.
     */
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

    /**
     * Bước 2: Với mỗi mã sản phẩm, đọc chi tiết từ products/{productId}.
     * Kết quả được thêm vào danh sách hiển thị.
     */
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

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích.
     * Xóa nút wishlists/{uid}/{productId} trên Firebase.
     * Danh sách tự cập nhật nhờ ValueEventListener trong loadWishlist().
     */
    private void removeFromWishlist(String productId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("wishlists")
                .child(user.getUid()).child(productId).removeValue()
                .addOnSuccessListener(a -> Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show());
    }

    /**
     * Thêm sản phẩm vào giỏ hàng từ danh sách yêu thích.
     * Kiểm tra giỏ hàng: nếu sản phẩm đã có thì tăng số lượng,
     * nếu chưa thì tạo mục mới với số lượng 1.
     * CSDL: Ghi vào carts/{uid}
     */
    private void addToCart(Product product) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("carts").child(user.getUid());

        // Kiểm tra sản phẩm đã có trong giỏ chưa
        cartRef.orderByChild("productId").equalTo(product.productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Đã có: tăng số lượng
                            for (DataSnapshot c : snapshot.getChildren()) {
                                Integer qty = c.child("quantity").getValue(Integer.class);
                                c.getRef().child("quantity").setValue((qty != null ? qty : 0) + 1);
                            }
                        } else {
                            // Chưa có: tạo mục mới
                            String key = cartRef.push().getKey();
                            java.util.Map<String, Object> item = new java.util.HashMap<>();
                            item.put("productId", product.productId);
                            item.put("productName", product.name);
                            item.put("price", product.price);
                            item.put("quantity", 1);
                            item.put("addedAt", System.currentTimeMillis());
                            if (key != null) cartRef.child(key).setValue(item);
                        }
                        Toast.makeText(WishlistActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
