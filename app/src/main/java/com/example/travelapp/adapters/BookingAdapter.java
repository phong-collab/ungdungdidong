package com.example.travelapp.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.example.travelapp.activities.ReviewActivity;
import com.example.travelapp.models.BookingModel;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private final List<BookingModel> list;
    public BookingAdapter(List<BookingModel> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Mã sửa lại
        BookingModel item = list.get(position);
        holder.txtBookingTitle.setText(item.getTourTitle());
        holder.txtBookingPrice.setText(String.format("%,d đ", item.getTotalPrice()));
        
        String depDate = item.getDepartureDate();
        if (depDate == null || depDate.isEmpty()) {
            holder.txtBookingDate.setText("Ngày đi: chưa chọn");
        } else {
            holder.txtBookingDate.setText("Ngày đi: " + depDate);
        }
        
        Glide.with(holder.itemView.getContext()).load(item.getTourThumbnail()).into(holder.imgBookingThum);

        String status = item.getPaymentStatus();
        holder.txtStatusBadge.setText(status);
        if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
            holder.txtStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50"));
            holder.btnReview.setVisibility(View.VISIBLE);
        } else {
            holder.txtStatusBadge.setBackgroundColor(Color.parseColor("#FFC107"));
            holder.btnReview.setVisibility(View.GONE);
        }

        holder.btnReview.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReviewActivity.class);
            intent.putExtra("TOUR_ID", item.getTourId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBookingThum; TextView txtBookingTitle, txtBookingPrice, txtStatusBadge, txtBookingDate; Button btnReview;
        ViewHolder(View itemView) {
            super(itemView);
            imgBookingThum = itemView.findViewById(R.id.imgBookingThum);
            txtBookingTitle = itemView.findViewById(R.id.txtBookingTitle);
            txtBookingPrice = itemView.findViewById(R.id.txtBookingPrice);
            txtBookingDate = itemView.findViewById(R.id.txtBookingDate); // ĐÃ THÊM
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}