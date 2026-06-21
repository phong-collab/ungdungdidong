package com.example.travelapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.example.travelapp.models.TourModel;
import java.util.List;

public class AdminTourAdapter extends BaseAdapter {
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

    @Override
    public int getCount() {
        return tourList != null ? tourList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return tourList != null ? tourList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_tour, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TourModel tour = tourList.get(position);
        holder.tvTitle.setText(tour.getTitle());
        holder.tvPrice.setText(String.format("%,d đ", tour.getPriceAdult()));
        
        Glide.with(parent.getContext())
                .load(tour.getThumbnail())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgTour);

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(tour));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(tour));

        return convertView;
    }

    public static class ViewHolder {
        ImageView imgTour;
        TextView tvTitle, tvPrice;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            imgTour = itemView.findViewById(R.id.imgAdminTour);
            tvTitle = itemView.findViewById(R.id.tvAdminTourTitle);
            tvPrice = itemView.findViewById(R.id.tvAdminTourPrice);
            btnEdit = itemView.findViewById(R.id.btnEditAdminTour);
            btnDelete = itemView.findViewById(R.id.btnDeleteAdminTour);
        }
    }
}