package com.example.travelapp.adapters;

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
import com.example.travelapp.models.BookingModel;
import java.util.List;

public class AdminBookingAdapter extends BaseAdapter {
    private final List<BookingModel> bookingList;
    private final OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onConfirmClick(BookingModel booking);
        void onCancelClick(BookingModel booking);
    }

    public AdminBookingAdapter(List<BookingModel> bookingList, OnBookingActionListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return bookingList != null ? bookingList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BookingModel booking = bookingList.get(position);

        holder.tvBookingId.setText("Mã đơn: " + booking.getId());
        holder.tvBookingTitle.setText(booking.getTourTitle());
        holder.tvBookingUser.setText("Mã KH: " + booking.getUserId());
        holder.tvBookingPassengers.setText("Người lớn: " + booking.getCountAdult() + " - Trẻ em: " + booking.getCountChild());
        
        String depDate = booking.getDepartureDate();
        if (depDate == null || depDate.isEmpty()) {
            holder.tvBookingDate.setText("Ngày đi: chưa chọn");
        } else {
            holder.tvBookingDate.setText("Ngày đi: " + depDate);
        }
        
        holder.tvBookingPrice.setText(String.format("%,d đ", booking.getTotalPrice()));

        Glide.with(parent.getContext())
                .load(booking.getTourThumbnail())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imgBookingThum);

        String status = booking.getPaymentStatus();
        if (status == null || status.isEmpty()) {
            status = "PENDING";
        }
        holder.tvStatusBadge.setText(status.toUpperCase());

        if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            holder.btnConfirm.setVisibility(View.GONE);
        } else {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
            holder.btnConfirm.setVisibility(View.VISIBLE);
        }

        holder.btnConfirm.setOnClickListener(v -> listener.onConfirmClick(booking));
        holder.btnCancel.setOnClickListener(v -> listener.onCancelClick(booking));

        return convertView;
    }

    public static class ViewHolder {
        TextView tvBookingId, tvStatusBadge, tvBookingTitle, tvBookingUser, tvBookingPassengers, tvBookingPrice, tvBookingDate;
        ImageView imgBookingThum;
        Button btnConfirm, btnCancel;

        public ViewHolder(View itemView) {
            tvBookingId = itemView.findViewById(R.id.tvAdminBookingId);
            tvStatusBadge = itemView.findViewById(R.id.tvAdminBookingStatusBadge);
            tvBookingTitle = itemView.findViewById(R.id.tvAdminBookingTitle);
            tvBookingUser = itemView.findViewById(R.id.tvAdminBookingUser);
            tvBookingPassengers = itemView.findViewById(R.id.tvAdminBookingPassengers);
            tvBookingPrice = itemView.findViewById(R.id.tvAdminBookingPrice);
            tvBookingDate = itemView.findViewById(R.id.tvAdminBookingDate);
            imgBookingThum = itemView.findViewById(R.id.imgAdminBookingThum);
            btnConfirm = itemView.findViewById(R.id.btnAdminBookingConfirm);
            btnCancel = itemView.findViewById(R.id.btnAdminBookingCancel);
        }
    }
}
