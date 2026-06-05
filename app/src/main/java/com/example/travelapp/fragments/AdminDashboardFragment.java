package com.example.travelapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardFragment extends Fragment {

    private ImageButton btnBack;
    private TextView txtTotalRevenue, txtTotalUsers, txtTotalTours, txtNoPopularTours;
    private RecyclerView rvPopularTours;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        db = FirebaseFirestore.getInstance();

        btnBack = view.findViewById(R.id.btnBackDashboard);
        txtTotalRevenue = view.findViewById(R.id.txtTotalRevenue);
        txtTotalUsers = view.findViewById(R.id.txtTotalUsers);
        txtTotalTours = view.findViewById(R.id.txtTotalTours);
        txtNoPopularTours = view.findViewById(R.id.txtNoPopularTours);
        rvPopularTours = view.findViewById(R.id.rvPopularTours);

        rvPopularTours.setLayoutManager(new LinearLayoutManager(getContext()));

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        View cardTotalUsers = view.findViewById(R.id.cardTotalUsers);
        View cardTotalTours = view.findViewById(R.id.cardTotalTours);
        cardTotalUsers.setOnClickListener(v -> showUsersDialog());
        cardTotalTours.setOnClickListener(v -> showToursDialog());

        loadStatistics();

        return view;
    }

    private void loadStatistics() {
        // 1. Thống kê Doanh thu và Tour đặt nhiều nhất
        db.collection("bookings").get().addOnSuccessListener(queryDocumentSnapshots -> {
            long totalRevenue = 0;
            Map<String, PopularTourModel> popularToursMap = new HashMap<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String status = doc.getString("paymentStatus");
                Long price = doc.getLong("totalPrice");
                String tourId = doc.getString("tourId");
                String tourTitle = doc.getString("tourTitle");

                // Tính tổng doanh thu từ các đơn đặt tour thành công
                if (price != null && ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status))) {
                    totalRevenue += price;
                }

                // Nhóm số lượng đặt tour theo ID tour
                if (tourId != null && !tourId.isEmpty()) {
                    PopularTourModel popularTour = popularToursMap.get(tourId);
                    if (popularTour == null) {
                        popularTour = new PopularTourModel(tourId, tourTitle != null ? tourTitle : "Tour " + tourId, 0);
                    }
                    popularTour.incrementCount();
                    popularToursMap.put(tourId, popularTour);
                }
            }

            txtTotalRevenue.setText(String.format("%,d đ", totalRevenue));

            // Sắp xếp danh sách tour theo số lượt đặt giảm dần
            List<PopularTourModel> popularToursList = new ArrayList<>(popularToursMap.values());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                popularToursList.sort((t1, t2) -> Integer.compare(t2.bookingCount, t1.bookingCount));
            }

            // Lấy ra top 5 tour được đặt nhiều nhất
            List<PopularTourModel> topToursList = new ArrayList<>();
            for (int i = 0; i < Math.min(5, popularToursList.size()); i++) {
                topToursList.add(popularToursList.get(i));
            }

            if (topToursList.isEmpty()) {
                txtNoPopularTours.setVisibility(View.VISIBLE);
                rvPopularTours.setVisibility(View.GONE);
            } else {
                txtNoPopularTours.setVisibility(View.GONE);
                rvPopularTours.setVisibility(View.VISIBLE);
                rvPopularTours.setAdapter(new PopularTourAdapter(topToursList));
            }

        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Không thể tải thống kê đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // 2. Thống kê Số lượng Khách hàng đăng ký
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            int usersCount = queryDocumentSnapshots.size();
            txtTotalUsers.setText(String.valueOf(usersCount));
        }).addOnFailureListener(e -> {
            txtTotalUsers.setText("Lỗi");
        });

        // 3. Thống kê Tổng số Tour đang hoạt động
        db.collection("tours").get().addOnSuccessListener(queryDocumentSnapshots -> {
            int toursCount = queryDocumentSnapshots.size();
            txtTotalTours.setText(String.valueOf(toursCount));
        }).addOnFailureListener(e -> {
            txtTotalTours.setText("Lỗi");
        });
    }

    // --- MODEL NỘI BỘ CHO TOUR ĐƯỢC ĐẶT NHIỀU NHẤT ---
    public static class PopularTourModel {
        String tourId;
        String tourTitle;
        int bookingCount;

        public PopularTourModel(String tourId, String tourTitle, int bookingCount) {
            this.tourId = tourId;
            this.tourTitle = tourTitle;
            this.bookingCount = bookingCount;
        }

        public void incrementCount() {
            this.bookingCount++;
        }
    }

    // --- ADAPTER NỘI BỘ HIỂN THỊ DANH SÁCH BẢNG XẾP HẠNG ---
    private static class PopularTourAdapter extends RecyclerView.Adapter<PopularTourAdapter.ViewHolder> {
        private final List<PopularTourModel> list;

        public PopularTourAdapter(List<PopularTourModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_tour, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PopularTourModel item = list.get(position);
            holder.tvRank.setText(String.valueOf(position + 1));
            holder.tvTourTitle.setText(item.tourTitle);
            holder.tvBookingCount.setText(item.bookingCount + " lượt đặt");

            // Nổi bật top 3 thứ hạng đầu bằng màu sắc badge khác biệt
            if (position == 0) {
                holder.tvRank.setBackgroundColor(Color_Parse("#FFD700")); // Vàng - hạng 1
            } else if (position == 1) {
                holder.tvRank.setBackgroundColor(Color_Parse("#C0C0C0")); // Bạc - hạng 2
            } else if (position == 2) {
                holder.tvRank.setBackgroundColor(Color_Parse("#CD7F32")); // Đồng - hạng 3
            } else {
                holder.tvRank.setBackgroundColor(Color_Parse("#E0E0E0")); // Xám - hạng dưới
            }
        }

        // Hàm parse màu an toàn tránh crash nếu không tìm thấy thư viện hoặc cấu hình
        private int Color_Parse(String colorString) {
            try {
                return android.graphics.Color.parseColor(colorString);
            } catch (Exception e) {
                return android.graphics.Color.GRAY;
            }
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvTourTitle, tvBookingCount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRank = itemView.findViewById(R.id.tvRank);
                tvTourTitle = itemView.findViewById(R.id.tvTourTitle);
                tvBookingCount = itemView.findViewById(R.id.tvBookingCount);
            }
        }
    }

    // --- DIALOG VÀ ADAPTER CHO DANH SÁCH CHI TIẾT ---

    private void showUsersDialog() {
        if (db == null || getContext() == null) return;
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, String>> data = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Map<String, String> item = new HashMap<>();
                item.put("title", doc.getString("name"));
                item.put("subtitle1", "Email: " + doc.getString("email"));
                item.put("subtitle2", "SĐT: " + doc.getString("phone"));
                item.put("imageUrl", "");
                data.add(item);
            }
            showListDialog("Danh sách Khách hàng", data, false);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Không thể tải danh sách khách hàng", Toast.LENGTH_SHORT).show();
        });
    }

    private void showToursDialog() {
        if (db == null || getContext() == null) return;
        db.collection("tours").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, String>> data = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Map<String, String> item = new HashMap<>();
                item.put("title", doc.getString("title"));

                Long priceAdult = doc.getLong("priceAdult");
                Long priceChild = doc.getLong("priceChild");
                String priceAdultStr = priceAdult != null ? String.format("%,d đ", priceAdult) : "0 đ";
                String priceChildStr = priceChild != null ? String.format("%,d đ", priceChild) : "0 đ";

                item.put("subtitle1", "Giá người lớn: " + priceAdultStr);
                item.put("subtitle2", "Giá trẻ em: " + priceChildStr);
                item.put("imageUrl", doc.getString("thumbnail"));
                data.add(item);
            }
            showListDialog("Danh sách Tour", data, true);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Không thể tải danh sách tour", Toast.LENGTH_SHORT).show();
        });
    }

    private void showListDialog(String title, List<Map<String, String>> data, boolean hasImage) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_list, null);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        RecyclerView rvList = view.findViewById(R.id.rvDialogList);

        tvTitle.setText(title);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setAdapter(new GenericDialogAdapter(data, hasImage));

        builder.setView(view);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class GenericDialogAdapter extends RecyclerView.Adapter<GenericDialogAdapter.ViewHolder> {
        private final List<Map<String, String>> list;
        private final boolean hasImage;

        public GenericDialogAdapter(List<Map<String, String>> list, boolean hasImage) {
            this.list = list;
            this.hasImage = hasImage;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, String> item = list.get(position);
            holder.tvTitle.setText(item.get("title"));
            holder.tvSubtitle1.setText(item.get("subtitle1"));
            holder.tvSubtitle2.setText(item.get("subtitle2"));

            if (hasImage) {
                holder.cardImage.setVisibility(View.VISIBLE);
                String imageUrl = item.get("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(holder.imgRow);
                } else {
                    holder.imgRow.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.cardImage.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View cardImage;
            android.widget.ImageView imgRow;
            TextView tvTitle, tvSubtitle1, tvSubtitle2;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardImage = itemView.findViewById(R.id.cardRowImage);
                imgRow = itemView.findViewById(R.id.imgRow);
                tvTitle = itemView.findViewById(R.id.tvRowTitle);
                tvSubtitle1 = itemView.findViewById(R.id.tvRowSubtitle1);
                tvSubtitle2 = itemView.findViewById(R.id.tvRowSubtitle2);
            }
        }
    }
}
