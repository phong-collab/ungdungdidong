package com.example.travelapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.travelapp.R;
import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {
    private RatingBar ratingBar; private EditText edtReviewContent; private Button btnSubmitReview;
    private FirebaseFirestore db; private String tourId, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        db = FirebaseFirestore.getInstance();
        tourId = getIntent().getStringExtra("TOUR_ID");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ratingBar = findViewById(R.id.ratingBar);
        edtReviewContent = findViewById(R.id.edtReviewContent);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String content = edtReviewContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReview.setEnabled(false);

        // Kiểm tra trạng thái thanh toán từ Firestore
        db.collection("bookings")
                .whereEqualTo("tourId", tourId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int paidBookingsCount = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("paymentStatus");
                        if ("PAID".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                            paidBookingsCount++;
                        }
                    }

                    if (paidBookingsCount == 0) {
                        Toast.makeText(this, "Bạn chưa thanh toán tour này, không thể viết đánh giá!", Toast.LENGTH_LONG).show();
                        btnSubmitReview.setEnabled(true);
                        return;
                    }

                    // Kiểm tra xem đã đánh giá vượt quá số chuyến đi chưa
                    int finalPaidBookingsCount = paidBookingsCount;
                    db.collection("reviews")
                            .whereEqualTo("tourId", tourId)
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(reviewSnapshots -> {
                                int reviewsCount = reviewSnapshots.size();
                                if (reviewsCount >= finalPaidBookingsCount) {
                                    Toast.makeText(this, "Bạn đã đánh giá cho tất cả các chuyến đi của tour này rồi!", Toast.LENGTH_LONG).show();
                                    btnSubmitReview.setEnabled(true);
                                } else {
                                    // Nếu còn lượt đánh giá, lấy tên người dùng và lưu đánh giá
                                    db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                                        String reviewerName = "Người dùng";
                                        if (documentSnapshot.exists()) {
                                            String name = documentSnapshot.getString("name");
                                            if (name != null && !name.isEmpty()) {
                                                reviewerName = name;
                                            }
                                        }
                                        saveReviewToFirestore(rating, content, reviewerName);
                                    }).addOnFailureListener(e -> {
                                        saveReviewToFirestore(rating, content, "Người dùng");
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi kiểm tra lịch sử đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                btnSubmitReview.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra quyền đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmitReview.setEnabled(true);
                });
    }

    private void saveReviewToFirestore(float rating, String content, String reviewerName) {
        String tourTitle = getIntent().getStringExtra("tourTitle");
        if (tourTitle == null || tourTitle.isEmpty()) {
            tourTitle = getIntent().getStringExtra("TOUR_TITLE");
        }
        if (tourTitle == null) {
            tourTitle = "";
        }

        String reviewId = db.collection("reviews").document().getId();
        Map<String, Object> review = new HashMap<>();
        review.put("id", reviewId); 
        review.put("tourId", tourId); 
        review.put("userId", userId);
        review.put("rating", rating); 
        review.put("content", content);
        review.put("reviewerName", reviewerName);
        review.put("tourTitle", tourTitle);
        review.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("reviews").document(reviewId).set(review).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
            calculateNewRatingAverage();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnSubmitReview.setEnabled(true);
        });
    }

    private void calculateNewRatingAverage() {
        db.collection("reviews").whereEqualTo("tourId", tourId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int total = queryDocumentSnapshots.size(); double sum = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Double ratingVal = doc.getDouble("rating");
                if (ratingVal != null) {
                    sum += ratingVal;
                }
            }
            double avg = total > 0 ? (sum / total) : 0;

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("ratingAverage", avg); updateData.put("totalReviews", total);

            db.collection("tours").document(tourId).update(updateData).addOnSuccessListener(aVoid -> finish());
        });
    }
}