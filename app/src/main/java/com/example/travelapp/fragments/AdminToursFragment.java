package com.example.travelapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelapp.R;
import com.example.travelapp.activities.AddTourActivity;
import com.example.travelapp.adapters.AdminTourAdapter;
import com.example.travelapp.models.TourModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminToursFragment extends Fragment {

    private ListView rvTours;
    private FloatingActionButton fabAddTour;
    private FirebaseFirestore db;
    private AdminTourAdapter adapter;
    private List<TourModel> tourList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_tours, container, false);

        db = FirebaseFirestore.getInstance();
        rvTours = view.findViewById(R.id.rvAdminTours);
        fabAddTour = view.findViewById(R.id.fabAddTour);

        tourList = new ArrayList<>();
        adapter = new AdminTourAdapter(tourList, new AdminTourAdapter.OnTourClickListener() {
            @Override
            public void onEditClick(TourModel tour) {
                Intent intent = new Intent(getContext(), AddTourActivity.class);
                intent.putExtra("TOUR_ID", tour.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(TourModel tour) {
                deleteTour(tour);
            }
        });

        rvTours.setAdapter(adapter);

        fabAddTour.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddTourActivity.class));
        });

        loadTours();

        return view;
    }

    private void loadTours() {
        db.collection("tours")
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        tourList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            TourModel tour = doc.toObject(TourModel.class);
                            if (tour != null) {
                                tour.setId(doc.getId());
                                tourList.add(tour);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void deleteTour(TourModel tour) {
        db.collection("tours").document(tour.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Tour deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}