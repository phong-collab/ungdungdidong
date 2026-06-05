package com.example.travelapp.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        // Nạp file giao diện chính fragment_my_orders.xml (chứa duy nhất RecyclerView trống)
        return inflater.inflate(R.layout.fragment_my_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // 1. Ánh xạ danh sách cuộn RecyclerView từ fragment_my_orders.xml
        RecyclerView rvOrders = view.findViewById(R.id.rvOrders);

        if (rvOrders != null) {
            // Cài đặt hiển thị danh sách theo hàng dọc
            rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

            // Khởi tạo bộ nhớ mảng và nạp Adapter
            bookingList = new ArrayList<>();
            adapter = new OrderAdapter(bookingList);
            rvOrders.setAdapter(adapter);

            // 2. Kiểm tra trạng thái đăng nhập để kéo dữ liệu từ Firestore
            if (!currentUserId.isEmpty()) {
                loadMyBookings();
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem chuyến đi!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Hàm lắng nghe thay đổi thời gian thực (Real-time) từ Firestore collection "bookings"
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
                                booking.setId(doc.getId()); // Gán ID document gốc để thao tác xóa/hủy
                                bookingList.add(booking);
                            }
                        }
                        // Cập nhật làm mới giao diện hiển thị danh sách chuyến đi
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // --- ADAPTER NỘI BỘ: ĐỔ DỮ LIỆU VÀ XỬ LÝ SỰ KIỆN HỦY/ĐÁNH GIÁ TOUR ---
    private class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {
        private final List<BookingModel> list;

        public OrderAdapter(List<BookingModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Bơm chính xác khuôn mẫu ô CardView từ tệp item_booking.xml
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
            return new OrderViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            BookingModel booking = list.get(position);

            if (holder.txtBookingTitle != null) {
                holder.txtBookingTitle.setText(booking.getTourTitle());
            }
            if (holder.txtBookingPrice != null) {
                holder.txtBookingPrice.setText(String.format("Tổng tiền: %,d đ", booking.getTotalPrice()));
            }

            // Xử lý nạp mô tả ngắn của Tour bất đồng bộ (Kiểm tra tránh crash nếu View null hoặc không có trong XML)
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

            // 2. Nhận diện trạng thái đơn để thay màu sắc linh hoạt cho nhãn Badge
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

            // 3. Nạp ảnh đại diện mượt mà bằng thư viện Glide
            Glide.with(holder.itemView.getContext())
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

                // 4. Xử lý sự kiện khi bấm nút "Review" chuẩn
                holder.btnReview.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), com.example.travelapp.activities.ReviewActivity.class);
                    intent.putExtra("TOUR_ID", booking.getTourId());
                    intent.putExtra("tourTitle", booking.getTourTitle());
                    startActivity(intent);
                });
            }

            // 5. XỬ LÝ SỰ KIỆN CLICK ITEM: Bấm vào bất kỳ đâu trên ô đơn hàng để xem chi tiết chuyến đi
            holder.itemView.setOnClickListener(v -> {
                Intent intentDetail = new Intent(getContext(), com.example.travelapp.activities.OrderDetailActivity.class);
                // Truyền mã đơn hàng BOOKING_ID (Id document trên Firestore) sang màn hình chi tiết
                intentDetail.putExtra("BOOKING_ID", booking.getId());
                startActivity(intentDetail);
            });

            if (holder.btnCancelBooking != null) {
                // 6. Xử lý logic nghiệp vụ bấm nút "Hủy" chuyến đi
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
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    // Lớp ViewHolder lưu giữ hạ tầng linh kiện (Ánh xạ an toàn, không lo crash nếu thiếu txtBookingDesc)
    private static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBookingThum;
        TextView txtBookingTitle, txtBookingPrice, txtStatusBadge, txtBookingDesc;
        Button btnReview, btnCancelBooking;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBookingThum = itemView.findViewById(R.id.imgBookingThum);
            txtBookingTitle = itemView.findViewById(R.id.txtBookingTitle);
            txtBookingPrice = itemView.findViewById(R.id.txtBookingPrice);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
            btnReview = itemView.findViewById(R.id.btnReview);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);

            // Kiểm tra an toàn: Chỉ ánh xạ txtBookingDesc nếu ID này có tồn tại trong file xml layout của bạn
            int descId = itemView.getResources().getIdentifier("txtBookingDesc", "id", itemView.getContext().getPackageName());
            if (descId != 0) {
                txtBookingDesc = itemView.findViewById(descId);
            } else {
                txtBookingDesc = null;
            }
        }
    }
}