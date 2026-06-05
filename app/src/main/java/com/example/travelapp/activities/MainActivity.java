package com.example.travelapp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.cloudinary.android.MediaManager;
import com.example.travelapp.fragments.FavoritesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.travelapp.R;
import com.example.travelapp.fragments.HomeFragment;
import com.example.travelapp.fragments.MyOrdersFragment;
import com.example.travelapp.fragments.ProfileFragment;
import java.util.HashMap;
import java.util.Map;

import com.example.travelapp.fragments.AdminCategoriesFragment;
import com.example.travelapp.fragments.AdminReviewsFragment;
import com.example.travelapp.fragments.AdminToursFragment;
import com.example.travelapp.fragments.AdminBookingsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initCloudinary();
        bottomNavigation = findViewById(R.id.bottom_navigation);

        checkUserRole();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (id == R.id.nav_orders) {
                selectedFragment = new MyOrdersFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (id == R.id.nav_admin_tours) {
                selectedFragment = new AdminToursFragment();
            } else if (id == R.id.nav_admin_categories) {
                selectedFragment = new AdminCategoriesFragment();
            } else if (id == R.id.nav_admin_reviews) {
                selectedFragment = new AdminReviewsFragment();
            } else if (id == R.id.nav_admin_bookings) {
                selectedFragment = new AdminBookingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }

    private void checkUserRole() {
        if (mAuth.getCurrentUser() == null) {
            bottomNavigation.getMenu().clear();
            bottomNavigation.inflateMenu(R.menu.menu_client);
            return;
        }

        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    bottomNavigation.getMenu().clear();
                    if ("admin".equals(role)) {
                        bottomNavigation.inflateMenu(R.menu.menu_admin);
                    } else {
                        bottomNavigation.inflateMenu(R.menu.menu_client);
                    }
                })
                .addOnFailureListener(e -> {
                    bottomNavigation.getMenu().clear();
                    bottomNavigation.inflateMenu(R.menu.menu_client);
                });
    }

    // Hàm thiết lập thông số Cloudinary
    private void initCloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dne0g3snv");
        config.put("api_key", "473146493373359");
        config.put("api_secret", "3Bo8lWBLGDJtd2pnIRDIdU9pCYk");
        try {
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Tránh lỗi crash app khi hệ thống cố tình init lại nhiều lần
        }
    }
}