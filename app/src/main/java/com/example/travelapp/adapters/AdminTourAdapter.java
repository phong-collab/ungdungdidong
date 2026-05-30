package com.example.travelapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.example.travelapp.models.TourModel;

import java.util.List;

public class AdminTourAdapter extends RecyclerView.Adapter<AdminTourAdapter.ViewHolder> {

    private List<TourModel> tourList;
    private OnTourClickListener listener;

    public interface OnTourClickListener {
        void onEditClick(TourModel tour);
        void onDeleteClick(TourModel tour);
    }

    public AdminTourAdapter(List<TourModel> tourList, OnTourClickListener listener) {
        this.tourList = tourList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_tour, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourModel tour = tourList.get(position);
        holder.tvTitle.setText(tour.getTitle());
        holder.tvPrice.setText(String.format("$%d", tour.getPriceAdult()));
        
        Glide.with(holder.itemView.getContext())
                .load(tour.getThumbnail())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgTour);

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(tour));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(tour));
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTour;
        TextView tvTitle, tvPrice;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTour = itemView.findViewById(R.id.imgAdminTour);
            tvTitle = itemView.findViewById(R.id.tvAdminTourTitle);
            tvPrice = itemView.findViewById(R.id.tvAdminTourPrice);
            btnEdit = itemView.findViewById(R.id.btnEditAdminTour);
            btnDelete = itemView.findViewById(R.id.btnDeleteAdminTour);
        }
    }
}