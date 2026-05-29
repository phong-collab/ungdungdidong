package com.example.travelapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private RecyclerView rvFavorites;
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
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));

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

    private class FavoriteAdapter extends RecyclerView.Adapter<FavoriteViewHolder> {
        private final List<TourModel> list;

        public FavoriteAdapter(List<TourModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_favorite, parent, false);
            return new FavoriteViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
            // Sử dụng holder.getAdapterPosition() thay cho biến position cũ để tuyệt đối không bị lệch index mảng
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            TourModel tour = list.get(currentPos);
            holder.txtFavTitle.setText(tour.getTitle());
            holder.txtFavPrice.setText(String.format("%,d đ/người", tour.getPriceAdult()));

            String thumbUrl = tour.getThumbnail();
            if (thumbUrl == null || thumbUrl.trim().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(android.R.drawable.ic_menu_gallery)
                        .into(holder.imgFavThumbnail);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(thumbUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.imgFavThumbnail);
            }

            // Bấm vào nguyên dòng để xem chi tiết
            holder.itemView.setOnClickListener(v -> {
                int p = holder.getAdapterPosition();
                if (p != RecyclerView.NO_POSITION) {
                    TourModel clickedTour = list.get(p);
                    Intent intent = new Intent(getContext(), TourDetailActivity.class);
                    intent.putExtra("tourId", clickedTour.getId());
                    startActivity(intent);
                }
            });

            // Bấm nút xóa (thùng rác)
            holder.btnRemoveFav.setOnClickListener(v -> {
                int p = holder.getAdapterPosition();
                if (p != RecyclerView.NO_POSITION) {
                    TourModel clickedTour = list.get(p);
                    String favDocId = clickedTour.getDescription();
                    if (favDocId != null && !favDocId.isEmpty()) {
                        db.collection("favorites").document(favDocId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFavThumbnail;
        TextView txtFavTitle, txtFavPrice;
        android.widget.ImageButton btnRemoveFav;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFavThumbnail = itemView.findViewById(R.id.imgFavThumbnail);
            txtFavTitle = itemView.findViewById(R.id.txtFavTitle);
            txtFavPrice = itemView.findViewById(R.id.txtFavPrice);
            btnRemoveFav = itemView.findViewById(R.id.btnRemoveFav);
        }
    }
}