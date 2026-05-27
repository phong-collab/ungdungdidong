package com.example.travelapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.example.travelapp.activities.TourDetailActivity;
import com.example.travelapp.models.TourModel;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {
    private final List<TourModel> list;

    public TourAdapter(List<TourModel> list) { this.list = list; }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_featured, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        TourModel item = list.get(position);
        holder.txtTourTitle.setText(item.getTitle());
        holder.txtTourPrice.setText(String.format("%,d đ", item.getPriceAdult()));
        Glide.with(holder.itemView.getContext()).load(item.getThumbnail()).into(holder.imgTourThumbnail);

        // Chuyển màn hình sang Giai đoạn 4 khi bấm vào ô Tour
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TourDetailActivity.class);
            intent.putExtra("TOUR_ID", item.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTourThumbnail; TextView txtTourTitle, txtTourPrice;
        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTourThumbnail = itemView.findViewById(R.id.imgTourThumbnail);
            txtTourTitle = itemView.findViewById(R.id.txtTourTitle);
            txtTourPrice = itemView.findViewById(R.id.txtTourPrice);
        }
    }
}