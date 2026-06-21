package com.example.travelapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.example.travelapp.activities.TourDetailActivity;
import com.example.travelapp.models.TourModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {
    private ListView rvFavorites;
    private FavoriteAdapter adapter;
    private List<TourModel> favoriteToursList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        rvFavorites = view.findViewById(R.id.rvFavorites);

        favoriteToursList = new ArrayList<>();
        adapter = new FavoriteAdapter(favoriteToursList);
        rvFavorites.setAdapter(adapter);

        if (!currentUserId.isEmpty()) {
            loadFavoriteTours();
        }
    }

    private void loadFavoriteTours() {
        db.collection("favorites")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    favoriteToursList.clear();
                    if (value.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String tourId = doc.getString("tourId");
                        String favoriteDocId = doc.getId();

                        if (tourId != null) {
                            db.collection("tours").document(tourId).get()
                                    .addOnSuccessListener(tourDoc -> {
                                        if (tourDoc.exists()) {
                                            TourModel tour = tourDoc.toObject(TourModel.class);
                                            if (tour != null) {
                                                tour.setId(tourDoc.getId());
                                                // Lưu mã id của bảng favorites vào description tạm thời để xóa
                                                tour.setDescription(favoriteDocId);

                                                // Kiểm tra tránh trùng lặp item khi Firestore cập nhật real-time
                                                boolean isExist = false;
                                                for(TourModel t : favoriteToursList) {
                                                    if(t.getId().equals(tour.getId())) {
                                                        isExist = true;
                                                        break;
                                                    }
                                                }
                                                if(!isExist) {
                                                    favoriteToursList.add(tour);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private class FavoriteAdapter extends BaseAdapter {
        private final List<TourModel> list;

        public FavoriteAdapter(List<TourModel> list) {
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
            FavoriteViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_favorite, parent, false);
                holder = new FavoriteViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (FavoriteViewHolder) convertView.getTag();
            }

            TourModel tour = list.get(position);
            holder.txtFavTitle.setText(tour.getTitle());
            holder.txtFavPrice.setText(String.format("%,d đ/người", tour.getPriceAdult()));

            String thumbUrl = tour.getThumbnail();
            if (thumbUrl == null || thumbUrl.trim().isEmpty()) {
                Glide.with(parent.getContext())
                        .load(android.R.drawable.ic_menu_gallery)
                        .into(holder.imgFavThumbnail);
            } else {
                Glide.with(parent.getContext())
                        .load(thumbUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.imgFavThumbnail);
            }

            // Bấm vào nguyên dòng để xem chi tiết
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), TourDetailActivity.class);
                intent.putExtra("tourId", tour.getId());
                // Cũng truyền "TOUR_ID" cho đồng bộ
                intent.putExtra("TOUR_ID", tour.getId());
                startActivity(intent);
            });

            // Bấm nút xóa (thùng rác)
            holder.btnRemoveFav.setOnClickListener(v -> {
                String favDocId = tour.getDescription();
                if (favDocId != null && !favDocId.isEmpty()) {
                    db.collection("favorites").document(favDocId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                            });
                }
            });

            return convertView;
        }
    }

    private static class FavoriteViewHolder {
        ImageView imgFavThumbnail;
        TextView txtFavTitle, txtFavPrice;
        android.widget.ImageButton btnRemoveFav;

        public FavoriteViewHolder(@NonNull View itemView) {
            imgFavThumbnail = itemView.findViewById(R.id.imgFavThumbnail);
            txtFavTitle = itemView.findViewById(R.id.txtFavTitle);
            txtFavPrice = itemView.findViewById(R.id.txtFavPrice);
            btnRemoveFav = itemView.findViewById(R.id.btnRemoveFav);
        }
    }
}