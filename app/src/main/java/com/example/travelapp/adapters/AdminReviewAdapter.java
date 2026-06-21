package com.example.travelapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.travelapp.R;
import com.example.travelapp.models.ReviewModel;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class AdminReviewAdapter extends BaseAdapter {
    private List<ReviewModel> reviewList;
    private OnReviewDeleteListener listener;

    public interface OnReviewDeleteListener {
        void onDeleteClick(ReviewModel review);
    }

    public AdminReviewAdapter(List<ReviewModel> reviewList, OnReviewDeleteListener listener) {
        this.reviewList = reviewList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return reviewList != null ? reviewList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_review, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ReviewModel review = reviewList.get(position);
        
        String reviewer = review.getReviewerName();
        if (reviewer == null || reviewer.trim().isEmpty()) {
            reviewer = "Người dùng ẩn danh";
        }
        holder.tvUser.setText(reviewer);

        String tourTitle = review.getTourTitle();
        if (tourTitle == null || tourTitle.trim().isEmpty()) {
            tourTitle = "Mã Tour: " + review.getTourId();
        }
        holder.tvTour.setText(tourTitle);

        holder.tvComment.setText(review.getContent());
        holder.rbReview.setRating((float) review.getRating());

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(review));

        return convertView;
    }

    public static class ViewHolder {
        TextView tvUser, tvTour, tvComment;
        RatingBar rbReview;
        MaterialButton btnDelete;

        public ViewHolder(View itemView) {
            tvUser = itemView.findViewById(R.id.tvAdminReviewUser);
            tvTour = itemView.findViewById(R.id.tvAdminReviewTour);
            tvComment = itemView.findViewById(R.id.tvAdminReviewComment);
            rbReview = itemView.findViewById(R.id.rbAdminReview);
            btnDelete = itemView.findViewById(R.id.btnDeleteReview);
        }
    }
}