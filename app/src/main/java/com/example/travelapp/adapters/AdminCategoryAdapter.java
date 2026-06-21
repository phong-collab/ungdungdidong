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
import com.example.travelapp.models.CategoryModel;
import java.util.List;

public class AdminCategoryAdapter extends BaseAdapter {
    private List<CategoryModel> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onEditClick(CategoryModel category);
        void onDeleteClick(CategoryModel category);
    }

    public AdminCategoryAdapter(List<CategoryModel> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return categoryList != null ? categoryList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CategoryModel category = categoryList.get(position);
        holder.tvName.setText(category.getName());
        Glide.with(parent.getContext())
                .load(category.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgCategory);

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(category));

        return convertView;
    }

    public static class ViewHolder {
        ImageView imgCategory;
        TextView tvName;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}