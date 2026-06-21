package com.example.travelapp.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.travelapp.models.TourModel;
import java.util.ArrayList;
import java.util.List;

public class TourRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface Callback<T> {
        void onResult(T result);
    }

    public void getFeaturedTours(Callback<List<TourModel>> callback) {
        db.collection("tours").whereEqualTo("isFeatured", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<TourModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    TourModel tour = doc.toObject(TourModel.class);
                    if (tour != null) {
                        tour.setId(doc.getId());
                        list.add(tour);
                    }
                }
                callback.onResult(list);
            } else {
                callback.onResult(new ArrayList<>());
            }
        }).addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }
}