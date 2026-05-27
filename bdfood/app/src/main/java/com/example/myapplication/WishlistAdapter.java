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
 * Mỗi mục hiển thị: ảnh, tên, giá, nút xóa yêu thích.
 * Chức năng thêm vào giỏ hàng thuộc module thành viên khác.
 */
public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Product> productList;
    private OnWishlistActionListener listener;

    public interface OnWishlistActionListener {
        void onRemoveWishlist(Product product);
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

        if (product.images != null && !product.images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.images.get(0))
                    .into(holder.imgProduct);
        }

        holder.btnHeart.setOnClickListener(v -> listener.onRemoveWishlist(product));

        // Ẩn nút giỏ hàng vì chức năng thêm vào giỏ thuộc module khác
        if (holder.btnAddToCart != null) {
            holder.btnAddToCart.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnHeart, btnAddToCart;
        TextView tvProductName, tvProductPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnHeart = itemView.findViewById(R.id.btnHeart);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
