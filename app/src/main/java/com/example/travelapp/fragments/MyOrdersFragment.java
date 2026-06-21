package com.example.travelapp.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.example.travelapp.models.BookingModel;
import com.example.travelapp.models.TourModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersFragment extends Fragment {
    private OrderAdapter adapter;
    private List<BookingModel> bookingList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        ListView rvOrders = view.findViewById(R.id.rvOrders);

        if (rvOrders != null) {
            bookingList = new ArrayList<>();
            adapter = new OrderAdapter(bookingList);
            rvOrders.setAdapter(adapter);

            if (!currentUserId.isEmpty()) {
                loadMyBookings();
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem chuyến đi!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMyBookings() {
        db.collection("bookings")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        bookingList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            BookingModel booking = doc.toObject(BookingModel.class);
                            if (booking != null) {
                                booking.setId(doc.getId());
                                bookingList.add(booking);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private class OrderAdapter extends BaseAdapter {
        private final List<BookingModel> list;

        public OrderAdapter(List<BookingModel> list) {
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
            OrderViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
                holder = new OrderViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (OrderViewHolder) convertView.getTag();
            }

            BookingModel booking = list.get(position);

            if (holder.txtBookingTitle != null) {
                holder.txtBookingTitle.setText(booking.getTourTitle());
            }
            if (holder.txtBookingPrice != null) {
                holder.txtBookingPrice.setText(String.format("Tổng tiền: %,d đ", booking.getTotalPrice()));
            }
            if (holder.txtBookingDate != null) {
                String depDate = booking.getDepartureDate();
                if (depDate == null || depDate.isEmpty()) {
                    holder.txtBookingDate.setText("Ngày đi: chưa chọn");
                } else {
                    holder.txtBookingDate.setText("Ngày đi: " + depDate);
                }
            }

            if (holder.txtBookingDesc != null) {
                holder.txtBookingDesc.setText(booking.getTourDescription() != null ? booking.getTourDescription() : "");
                if ((booking.getTourDescription() == null || booking.getTourDescription().isEmpty())
                        && booking.getTourId() != null && !booking.getTourId().isEmpty()) {
                    db.collection("tours").document(booking.getTourId()).get().addOnSuccessListener(doc -> {
                        TourModel tour = doc.toObject(TourModel.class);
                        if (tour != null && tour.getDescription() != null) {
                            holder.txtBookingDesc.setText(tour.getDescription());
                        }
                    });
                }
            }

            String status = booking.getPaymentStatus();
            if (status == null || status.isEmpty()) {
                status = "Pending";
            }
            if (holder.txtStatusBadge != null) {
                holder.txtStatusBadge.setText(status);
                if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
                    holder.txtStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50"));
                } else {
                    holder.txtStatusBadge.setBackgroundColor(Color.parseColor("#FF9800"));
                }
            }
            if (holder.btnCancelBooking != null) {
                if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
                    holder.btnCancelBooking.setVisibility(View.GONE);
                } else {
                    holder.btnCancelBooking.setVisibility(View.VISIBLE);
                }
            }

            Glide.with(parent.getContext())
                    .load(booking.getTourThumbnail())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imgBookingThum);

            if (holder.btnReview != null) {
                if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
                    holder.btnReview.setVisibility(View.VISIBLE);
                } else {
                    holder.btnReview.setVisibility(View.GONE);
                }

                holder.btnReview.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), com.example.travelapp.activities.ReviewActivity.class);
                    intent.putExtra("TOUR_ID", booking.getTourId());
                    intent.putExtra("tourTitle", booking.getTourTitle());
                    startActivity(intent);
                });
            }

            convertView.setOnClickListener(v -> {
                Intent intentDetail = new Intent(getContext(), com.example.travelapp.activities.OrderDetailActivity.class);
                intentDetail.putExtra("BOOKING_ID", booking.getId());
                startActivity(intentDetail);
            });

            if (holder.btnCancelBooking != null) {
                holder.btnCancelBooking.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("Xác nhận hủy chuyến đi")
                            .setMessage("Bạn có chắc chắn muốn hủy đặt tour '" + booking.getTourTitle() + "' này không?")
                            .setPositiveButton("Đồng ý hủy", (dialog, which) -> {
                                db.collection("bookings").document(booking.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Đã hủy chuyến đi thành công!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Lỗi, không thể hủy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Đóng lại", (dialog, which) -> dialog.dismiss())
                            .show();
                });
            }

            return convertView;
        }
    }

    private static class OrderViewHolder {
        ImageView imgBookingThum;
        TextView txtBookingTitle, txtBookingPrice, txtStatusBadge, txtBookingDesc, txtBookingDate;
        Button btnReview, btnCancelBooking;

        public OrderViewHolder(@NonNull View itemView) {
            imgBookingThum = itemView.findViewById(R.id.imgBookingThum);
            txtBookingTitle = itemView.findViewById(R.id.txtBookingTitle);
            txtBookingPrice = itemView.findViewById(R.id.txtBookingPrice);
            txtBookingDate = itemView.findViewById(R.id.txtBookingDate);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
            btnReview = itemView.findViewById(R.id.btnReview);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);

            int descId = itemView.getResources().getIdentifier("txtBookingDesc", "id", itemView.getContext().getPackageName());
            if (descId != 0) {
                txtBookingDesc = itemView.findViewById(descId);
            } else {
                txtBookingDesc = null;
            }
        }
    }
}