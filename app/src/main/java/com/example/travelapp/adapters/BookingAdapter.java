package com.example.travelapp.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.example.travelapp.activities.ReviewActivity;
import com.example.travelapp.models.BookingModel;
import java.util.List;

public class BookingAdapter extends BaseAdapter {
    private final List<BookingModel> list;

    public BookingAdapter(List<BookingModel> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return list != null ? list.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BookingModel item = list.get(position);
        holder.txtBookingTitle.setText(item.getTourTitle());
        holder.txtBookingPrice.setText(String.format("%,d đ", item.getTotalPrice()));
        
        String depDate = item.getDepartureDate();
        if (depDate == null || depDate.isEmpty()) {
            holder.txtBookingDate.setText("Ngày đi: chưa chọn");
        } else {
            holder.txtBookingDate.setText("Ngày đi: " + depDate);
        }
        
        Glide.with(parent.getContext()).load(item.getTourThumbnail()).into(holder.imgBookingThum);

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
            intent.putExtra("tourTitle", item.getTourTitle());
            v.getContext().startActivity(intent);
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView imgBookingThum;
        TextView txtBookingTitle, txtBookingPrice, txtStatusBadge, txtBookingDate;
        Button btnReview;

        ViewHolder(View itemView) {
            imgBookingThum = itemView.findViewById(R.id.imgBookingThum);
            txtBookingTitle = itemView.findViewById(R.id.txtBookingTitle);
            txtBookingPrice = itemView.findViewById(R.id.txtBookingPrice);
            txtBookingDate = itemView.findViewById(R.id.txtBookingDate);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}