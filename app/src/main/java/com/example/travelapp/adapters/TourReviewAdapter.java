package com.example.travelapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.travelapp.R;
import com.example.travelapp.models.ReviewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TourReviewAdapter extends BaseAdapter {
    private final List<ReviewModel> reviewList;

    public TourReviewAdapter(List<ReviewModel> reviewList) {
        this.reviewList = reviewList;
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_review, parent, false);
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
        holder.rbReview.setRating((float) review.getRating());
        holder.tvComment.setText(review.getContent());

        if (review.getCreatedAt() != null) {
            Date date = review.getCreatedAt().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(date));
        } else {
            holder.tvDate.setText("");
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView tvUser, tvDate, tvComment;
        RatingBar rbReview;

        public ViewHolder(View itemView) {
            tvUser = itemView.findViewById(R.id.tvReviewUser);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            rbReview = itemView.findViewById(R.id.rbReviewStar);
        }
    }
}
