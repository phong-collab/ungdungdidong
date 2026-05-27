package com.example.travelapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.travelapp.repositories.TourRepository;
import com.example.travelapp.models.TourModel;
import java.util.List;

public class TourViewModel extends ViewModel {
    private final TourRepository repository = new TourRepository();
    private LiveData<List<TourModel>> featuredTours;

    public LiveData<List<TourModel>> getFeaturedToursLiveData() {
        if (featuredTours == null) featuredTours = repository.getFeaturedTours();
        return featuredTours;
    }
}