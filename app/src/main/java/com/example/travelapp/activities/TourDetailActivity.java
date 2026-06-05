package com.example.travelapp.activities;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.travelapp.R;
import com.example.travelapp.adapters.ItineraryAdapter;
import com.example.travelapp.models.TourModel;
import com.example.travelapp.models.ReviewModel;
import com.example.travelapp.adapters.TourReviewAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class TourDetailActivity extends AppCompatActivity {
    private ImageView imgDetailThumbnail;
    private ImageButton btnBack;
    private TextView txtDetailTitle, txtDetailPrice, txtDetailDescription;
    private CheckBox btnFavorite;
    private RecyclerView rvItinerary;
    private Button btnBookNow;
    private FirebaseFirestore db;
    private String tourId, userId;

    private RecyclerView rvTourReviews;
    private TextView txtReviewsHeader, txtNoReviews;
    private Button btnWriteReview;
    private List<ReviewModel> reviewList;
    private TourReviewAdapter tourReviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        // 1. Khởi tạo Firebase và lấy thông tin User
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        // 2. Nhận chính xác mã tourId từ Intent truyền sang (Thống nhất dùng khóa "tourId" viết thường)
        tourId = getIntent().getStringExtra("tourId");
        if (tourId == null || tourId.isEmpty()) {
            // Sơ cua nếu màn hình nào đó trong nhóm vô tình truyền khóa viết hoa TOUR_ID
            tourId = getIntent().getStringExtra("TOUR_ID");
        }

        // Kiểm tra nếu hoàn toàn không có mã tourId thì đóng màn hình
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin tour!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Ánh xạ các thành phần giao diện chuẩn khớp 100% với file XML của bạn
        imgDetailThumbnail = findViewById(R.id.imgDetailThumbnail);
        btnBack = findViewById(R.id.btnBack);
        txtDetailTitle = findViewById(R.id.txtDetailTitle);
        txtDetailPrice = findViewById(R.id.txtDetailPrice);
        txtDetailDescription = findViewById(R.id.txtDetailDescription);
        btnFavorite = findViewById(R.id.btnFavorite);
        rvItinerary = findViewById(R.id.rvItinerary);
        btnBookNow = findViewById(R.id.btnBookNow);

        // 4. Cấu hình RecyclerView hiển thị lịch trình tour
        rvItinerary.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo các thành phần giao diện đánh giá
        rvTourReviews = findViewById(R.id.rvTourReviews);
        txtReviewsHeader = findViewById(R.id.txtReviewsHeader);
        txtNoReviews = findViewById(R.id.txtNoReviews);
        btnWriteReview = findViewById(R.id.btnWriteReview);

        rvTourReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        tourReviewAdapter = new TourReviewAdapter(reviewList);
        rvTourReviews.setAdapter(tourReviewAdapter);

        btnWriteReview.setOnClickListener(v -> {
            Intent intent = new Intent(TourDetailActivity.this, ReviewActivity.class);
            intent.putExtra("TOUR_ID", tourId);
            intent.putExtra("tourTitle", txtDetailTitle.getText().toString());
            startActivity(intent);
        });

        // 5. Cài đặt các sự kiện click nút điều hướng cơ bản
        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite(btnFavorite.isChecked()));

        // 6. Chạy các hàm tải dữ liệu
        loadTourDetailsFromFirestore();
        checkIfFavoriteExists();
        checkUserPaymentStatus();
        loadTourReviews();
    }

    // Hàm duy nhất làm nhiệm vụ kéo toàn bộ thông tin Chi tiết Tour + Lịch trình + Cấu hình nút Đặt vé
    private void loadTourDetailsFromFirestore() {
        db.collection("tours").document(tourId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        TourModel tour = documentSnapshot.toObject(TourModel.class);

                        if (tour != null) {
                            // Đổ dữ liệu chữ lên giao diện
                            txtDetailTitle.setText(tour.getTitle());
                            txtDetailPrice.setText(String.format("%,d đ/người", tour.getPriceAdult()));
                            txtDetailDescription.setText(tour.getDescription());

                            // Đổ hình ảnh lớn lên Header bằng Glide
                            Glide.with(TourDetailActivity.this)
                                    .load(tour.getThumbnail())
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .into(imgDetailThumbnail);

                            // Đổ danh sách lịch trình con vào RecyclerView (Nếu có)
                            if (tour.getItinerary() != null) {
                                rvItinerary.setAdapter(new ItineraryAdapter(tour.getItinerary()));
                            }

                            // Cấu hình truyền dữ liệu sang màn hình Đặt vé (BookingActivity) khi ấn nút Book Now
                            btnBookNow.setOnClickListener(v -> {
                                Intent intent = new Intent(TourDetailActivity.this, BookingActivity.class);
                                intent.putExtra("tourId", tourId);
                                intent.putExtra("tourTitle", tour.getTitle());
                                intent.putExtra("priceAdult", tour.getPriceAdult());
                                intent.putExtra("priceChild", tour.getPriceChild());
                                startActivity(intent);
                            });
                        }
                    } else {
                        Toast.makeText(TourDetailActivity.this, "Tour không tồn tại hoặc đã bị xóa!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TourDetailActivity.this, "Lỗi kết nối dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Kiểm tra xem tour này tài khoản hiện tại đã bấm thích chưa để bật sáng ngôi sao
    private void checkIfFavoriteExists() {
        if (userId == null || tourId == null) return;
        db.collection("favorites").document(userId + "_" + tourId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                btnFavorite.setChecked(true);
            }
        });
    }

    // Xử lý bật/tắt yêu thích thời gian thực
    private void toggleFavorite(boolean isChecked) {
        if (userId == null || tourId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
            btnFavorite.setChecked(false);
            return;
        }

        String favId = userId + "_" + tourId;
        if (isChecked) {
            Map<String, Object> fav = new HashMap<>();
            fav.put("id", favId);
            fav.put("userId", userId);
            fav.put("tourId", tourId);
            fav.put("createdAt", com.google.firebase.Timestamp.now()); // Thêm mốc thời gian sắp xếp dữ liệu

            db.collection("favorites").document(favId).set(fav)
                    .addOnSuccessListener(aVoid -> Toast.makeText(TourDetailActivity.this, "Đã lưu vào mục yêu thích!", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("favorites").document(favId).delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(TourDetailActivity.this, "Đã bỏ yêu thích!", Toast.LENGTH_SHORT).show());
        }
    }

    private void checkUserPaymentStatus() {
        if (userId == null || tourId == null) {
            btnWriteReview.setVisibility(View.GONE);
            return;
        }
        db.collection("bookings")
                .whereEqualTo("tourId", tourId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean hasPaid = false;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("paymentStatus");
                        if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                            hasPaid = true;
                            break;
                        }
                    }
                    if (hasPaid) {
                        btnWriteReview.setVisibility(View.VISIBLE);
                    } else {
                        btnWriteReview.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    btnWriteReview.setVisibility(View.GONE);
                });
    }

    private void loadTourReviews() {
        if (tourId == null) return;
        db.collection("reviews")
                .whereEqualTo("tourId", tourId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        reviewList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ReviewModel review = doc.toObject(ReviewModel.class);
                            if (review != null) {
                                review.setId(doc.getId());
                                reviewList.add(review);
                            }
                        }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            reviewList.sort((r1, r2) -> {
                                if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                                if (r1.getCreatedAt() == null) return 1;
                                if (r2.getCreatedAt() == null) return -1;
                                return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                            });
                        }
                        tourReviewAdapter.notifyDataSetChanged();

                        if (reviewList.isEmpty()) {
                            txtNoReviews.setVisibility(View.VISIBLE);
                            txtReviewsHeader.setText("Đánh giá (0.0 / 5 ★) - 0 đánh giá");
                        } else {
                            txtNoReviews.setVisibility(View.GONE);
                            double sum = 0;
                            for (ReviewModel r : reviewList) {
                                sum += r.getRating();
                            }
                            double avg = sum / reviewList.size();
                            txtReviewsHeader.setText(String.format(Locale.getDefault(), "Đánh giá (%.1f / 5 ★) - %d đánh giá", avg, reviewList.size()));
                        }
                    }
                });
    }
}