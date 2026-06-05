package com.example.travelapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelapp.R;
import com.example.travelapp.adapters.TourAdapter;
import com.example.travelapp.viewmodels.TourViewModel;

public class HomeFragment extends Fragment {
    private RecyclerView rvFeaturedTours;
    private TextView txtEmptyHome;
    private TourViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvFeaturedTours = view.findViewById(R.id.rvFeaturedTours);
        txtEmptyHome = view.findViewById(R.id.txtEmptyHome);
        rvFeaturedTours.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFeaturedTours.setAdapter(new TourAdapter(java.util.Collections.emptyList()));

        if (txtEmptyHome != null) {
            txtEmptyHome.setText("Dang tai tour...");
            txtEmptyHome.setVisibility(View.VISIBLE);
        }

        viewModel = new ViewModelProvider(this).get(TourViewModel.class);
        viewModel.getFeaturedToursLiveData().observe(getViewLifecycleOwner(), tours -> {
            boolean hasData = tours != null && !tours.isEmpty();
            if (txtEmptyHome != null) {
                txtEmptyHome.setText(hasData ? "" : "Chua co tour noi bat");
                txtEmptyHome.setVisibility(hasData ? View.GONE : View.VISIBLE);
            }
            rvFeaturedTours.setAdapter(new TourAdapter(tours != null ? tours : java.util.Collections.emptyList()));
        });
    }
}