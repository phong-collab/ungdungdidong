package com.example.travelapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelapp.R;
import com.example.travelapp.adapters.AdminReviewAdapter;
import com.example.travelapp.models.ReviewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReviewsFragment extends Fragment {

    private RecyclerView rvReviews;
    private FirebaseFirestore db;
    private AdminReviewAdapter adapter;
    private List<ReviewModel> reviewList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_reviews, container, false);

        db = FirebaseFirestore.getInstance();
        rvReviews = view.findViewById(R.id.rvAdminReviews);

        reviewList = new ArrayList<>();
        adapter = new AdminReviewAdapter(reviewList, review -> {
            deleteReview(review);
        });

        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);

        loadReviews();

        return view;
    }

    private void loadReviews() {
        db.collection("reviews")
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
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void deleteReview(ReviewModel review) {
        String tourId = review.getTourId();
        db.collection("reviews").document(review.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Review deleted", Toast.LENGTH_SHORT).show();
                    if (tourId != null && !tourId.isEmpty()) {
                        recalculateRatingAverageForTour(tourId);
                    }
                });
    }

    private void recalculateRatingAverageForTour(String tourId) {
        db.collection("reviews").whereEqualTo("tourId", tourId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int total = queryDocumentSnapshots.size();
            double sum = 0;
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Double ratingVal = doc.getDouble("rating");
                if (ratingVal != null) {
                    sum += ratingVal;
                }
            }
            double avg = total > 0 ? (sum / total) : 0;

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("ratingAverage", avg);
            updateData.put("totalReviews", total);

            db.collection("tours").document(tourId).update(updateData);
        });
    }
}