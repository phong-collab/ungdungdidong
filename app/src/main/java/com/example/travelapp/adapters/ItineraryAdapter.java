package com.example.travelapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelapp.R;
import com.example.travelapp.models.TourModel;
import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ViewHolder> {
    private final List<TourModel.ItineraryInner> list;
    public ItineraryAdapter(List<TourModel.ItineraryInner> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_itinerary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourModel.ItineraryInner item = list.get(position);
        holder.txtItineraryDay.setText("Ngày " + item.getDay());
        holder.txtItineraryContent.setText(item.getContent());
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtItineraryDay, txtItineraryContent;
        ViewHolder(View itemView) {
            super(itemView);
            txtItineraryDay = itemView.findViewById(R.id.txtItineraryDay);
            txtItineraryContent = itemView.findViewById(R.id.txtItineraryContent);
        }
    }
}