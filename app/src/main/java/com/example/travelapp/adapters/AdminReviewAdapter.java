package com.example.travelapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelapp.R;
import com.example.travelapp.models.ReviewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {

    private List<ReviewModel> reviewList;
    private OnReviewDeleteListener listener;

    public interface OnReviewDeleteListener {
        void onDeleteClick(ReviewModel review);
    }

    public AdminReviewAdapter(List<ReviewModel> reviewList, OnReviewDeleteListener listener) {
        this.reviewList = reviewList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewModel review = reviewList.get(position);
        holder.tvUser.setText(review.getReviewerName());
        holder.tvTour.setText(review.getTourId()); // Hoặc fetch tour title nếu cần
        holder.tvComment.setText(review.getContent());
        holder.rbReview.setRating((float) review.getRating());

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(review));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvTour, tvComment;
        RatingBar rbReview;
        MaterialButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvAdminReviewUser);
            tvTour = itemView.findViewById(R.id.tvAdminReviewTour);
            tvComment = itemView.findViewById(R.id.tvAdminReviewComment);
            rbReview = itemView.findViewById(R.id.rbAdminReview);
            btnDelete = itemView.findViewById(R.id.btnDeleteReview);
        }
    }
}