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
        if (content.isEmpty()) return;

        String reviewId = db.collection("reviews").document().getId();
        Map<String, Object> review = new HashMap<>();
        review.put("id", reviewId); review.put("tourId", tourId); review.put("userId", userId);
        review.put("rating", rating); review.put("content", content);

        db.collection("reviews").document(reviewId).set(review).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
            calculateNewRatingAverage();
        });
    }

    private void calculateNewRatingAverage() {
        db.collection("reviews").whereEqualTo("tourId", tourId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int total = queryDocumentSnapshots.size(); double sum = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) sum += doc.getDouble("rating");
            double avg = total > 0 ? (sum / total) : 0;

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("ratingAverage", avg); updateData.put("totalReviews", total);

            db.collection("tours").document(tourId).update(updateData).addOnSuccessListener(aVoid -> finish());
        });
    }
}