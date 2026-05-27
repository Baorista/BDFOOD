package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Bộ điều hợp hiển thị danh sách sản phẩm yêu thích dạng lưới.
 * Mỗi mục hiển thị: ảnh sản phẩm, tên, giá, nút xóa yêu thích, nút thêm giỏ.
 * Giao tiếp với Activity bên ngoài qua giao diện OnWishlistActionListener.
 */
public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Product> productList;
    private OnWishlistActionListener listener;

    /**
     * Giao diện gọi lại cho các hành động trên mục yêu thích.
     * Activity bên ngoài quyết định cách xử lý mỗi hành động.
     */
    public interface OnWishlistActionListener {
        void onRemoveWishlist(Product product);  // Xóa khỏi danh sách yêu thích
        void onAddToCart(Product product);        // Thêm vào giỏ hàng
    }

    public WishlistAdapter(List<Product> productList, OnWishlistActionListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductName.setText(product.name);
        holder.tvProductPrice.setText(String.format("%,.0fđ", product.price));

        // Tải ảnh sản phẩm bằng Glide
        if (product.images != null && !product.images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.images.get(0))
                    .into(holder.imgProduct);
        }

        // Sự kiện xóa khỏi yêu thích
        holder.btnRemoveWishlist.setOnClickListener(v -> listener.onRemoveWishlist(product));

        // Sự kiện thêm vào giỏ hàng
        holder.btnAddToCart.setOnClickListener(v -> listener.onAddToCart(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnRemoveWishlist, btnAddToCart;
        TextView tvProductName, tvProductPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnRemoveWishlist = itemView.findViewById(R.id.btnRemoveWishlist);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
