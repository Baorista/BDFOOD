package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Bộ điều hợp hiển thị danh sách địa chỉ giao hàng trong RecyclerView.
 * Mỗi mục hiển thị: tên người nhận, số điện thoại, địa chỉ chi tiết,
 * nhãn "Mặc định" (nếu có), nút sửa và nút xóa.
 * Giao tiếp với Activity bên ngoài qua giao diện OnAddressClickListener.
 */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private OnAddressClickListener listener;

    /**
     * Giao diện gọi lại cho các hành động trên mục địa chỉ.
     * Activity bên ngoài triển khai giao diện này để xử lý sửa và xóa.
     */
    public interface OnAddressClickListener {
        void onEditClick(Address address);   // Nhấn nút sửa
        void onDeleteClick(Address address); // Nhấn nút xóa
    }

    public AddressAdapter(List<Address> addressList, OnAddressClickListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.tvName.setText("Họ và tên: " + address.recipientName);
        holder.tvPhone.setText("Số điện thoại: " + address.phone);
        holder.tvAddress.setText("Địa chỉ chi tiết: " + address.detail);

        // Hiện/ẩn nhãn mặc định
        holder.tvDefaultBadge.setVisibility(address.isDefault ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(address));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(address));
    }

    @Override
    public int getItemCount() { return addressList.size(); }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvDefaultBadge;
        ImageView btnEdit, btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
