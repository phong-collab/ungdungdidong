package com.example.travelapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.travelapp.R;
import com.example.travelapp.models.TourModel;
import java.util.List;

public class ItineraryAdapter extends BaseAdapter {
    private final List<TourModel.ItineraryInner> list;

    public ItineraryAdapter(List<TourModel.ItineraryInner> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return list != null ? list.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_itinerary, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TourModel.ItineraryInner item = list.get(position);
        holder.txtItineraryDay.setText("Ngày " + item.getDay());
        holder.txtItineraryContent.setText(item.getContent());

        return convertView;
    }

    static class ViewHolder {
        TextView txtItineraryDay, txtItineraryContent;

        ViewHolder(View itemView) {
            txtItineraryDay = itemView.findViewById(R.id.txtItineraryDay);
            txtItineraryContent = itemView.findViewById(R.id.txtItineraryContent);
        }
    }
}