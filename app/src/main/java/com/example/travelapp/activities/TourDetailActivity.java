package com.example.travelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.travelapp.R;
import com.example.travelapp.adapters.ItineraryAdapter;
import com.example.travelapp.models.TourModel;
import java.util.HashMap;
import java.util.Map;

public class TourDetailActivity extends AppCompatActivity {
    private ImageView imgDetailThumbnail; private ImageButton btnBack;
    private TextView txtDetailTitle, txtDetailPrice, txtDetailDescription;
    private CheckBox btnFavorite; private RecyclerView rvItinerary; private Button btnBookNow;
    private FirebaseFirestore db; private String tourId, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        db = FirebaseFirestore.getInstance();
        tourId = getIntent().getStringExtra("TOUR_ID");
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        imgDetailThumbnail = findViewById(R.id.imgDetailThumbnail);
        btnBack = findViewById(R.id.btnBack);
        txtDetailTitle = findViewById(R.id.txtDetailTitle);
        txtDetailPrice = findViewById(R.id.txtDetailPrice);
        txtDetailDescription = findViewById(R.id.txtDetailDescription);
        btnFavorite = findViewById(R.id.btnFavorite);
        rvItinerary = findViewById(R.id.rvItinerary);
        btnBookNow = findViewById(R.id.btnBookNow);

        rvItinerary.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite(btnFavorite.isChecked()));

        btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(TourDetailActivity.this, BookingActivity.class);
            intent.putExtra("TOUR_ID", tourId);
            startActivity(intent);
        });

        loadTourDetails();
        checkIfFavoriteExists();
    }

    private void loadTourDetails() {
        db.collection("tours").document(tourId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                TourModel tour = doc.toObject(TourModel.class);
                if (tour != null) {
                    txtDetailTitle.setText(tour.getTitle());
                    txtDetailPrice.setText(String.format("%,d đ", tour.getPriceAdult()));
                    txtDetailDescription.setText(tour.getDescription());
                    Glide.with(this).load(tour.getThumbnail()).into(imgDetailThumbnail);

                    if (tour.getItinerary() != null) {
                        rvItinerary.setAdapter(new ItineraryAdapter(tour.getItinerary()));
                    }
                }
            }
        });
    }

    private void checkIfFavoriteExists() {
        if (userId == null) return;
        db.collection("favorites").document(userId + "_" + tourId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) btnFavorite.setChecked(true);
        });
    }

    private void toggleFavorite(boolean isChecked) {
        if (userId == null) return;
        String favId = userId + "_" + tourId;
        if (isChecked) {
            Map<String, Object> fav = new HashMap<>();
            fav.put("id", favId); fav.put("userId", userId); fav.put("tourId", tourId);
            db.collection("favorites").document(favId).set(fav);
        } else {
            db.collection("favorites").document(favId).delete();
        }
    }
}