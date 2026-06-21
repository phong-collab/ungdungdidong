package com.example.travelapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelapp.R;
import com.example.travelapp.adapters.TourAdapter;
import com.example.travelapp.repositories.TourRepository;
import java.util.Collections;

public class HomeFragment extends Fragment {
    private RecyclerView rvFeaturedTours;
    private TextView txtEmptyHome;
    private final TourRepository repository = new TourRepository();

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
        
        // Sử dụng GridLayoutManager để hiển thị dạng lưới (Grid) thay vì danh sách dọc
        // Số 2 ở đây là số cột (spanCount)
        rvFeaturedTours.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvFeaturedTours.setAdapter(new TourAdapter(Collections.emptyList()));

        if (txtEmptyHome != null) {
            txtEmptyHome.setText("Dang tai tour...");
            txtEmptyHome.setVisibility(View.VISIBLE);
        }

        // Direct fetch from Model/Repository (MVC pattern)
        repository.getFeaturedTours(tours -> {
            if (getActivity() == null) return;
            boolean hasData = tours != null && !tours.isEmpty();
            if (txtEmptyHome != null) {
                txtEmptyHome.setText(hasData ? "" : "Chua co tour noi bat");
                txtEmptyHome.setVisibility(hasData ? View.GONE : View.VISIBLE);
            }
            rvFeaturedTours.setAdapter(new TourAdapter(tours != null ? tours : Collections.emptyList()));
        });
    }
}