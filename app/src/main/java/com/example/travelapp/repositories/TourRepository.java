package com.example.travelapp.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.travelapp.models.TourModel;
import java.util.ArrayList;
import java.util.List;

public class TourRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<TourModel>> getFeaturedTours() {
        MutableLiveData<List<TourModel>> liveData = new MutableLiveData<>();
        liveData.setValue(new ArrayList<>());
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
                liveData.setValue(list);
            } else {
                liveData.setValue(new ArrayList<>());
            }
        }).addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
        return liveData;
    }
}