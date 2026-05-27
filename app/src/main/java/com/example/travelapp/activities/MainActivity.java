package com.example.travelapp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.cloudinary.android.MediaManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.travelapp.R;
import com.example.travelapp.fragments.HomeFragment;
import com.example.travelapp.fragments.MyOrdersFragment;
import com.example.travelapp.fragments.ProfileFragment;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Kết nối với file XML giao diện ở trên

        // 1. Khởi tạo cấu hình Cloudinary (Dành cho việc upload ảnh của Admin ở Giai đoạn 7)
        initCloudinary();

        // 2. Ánh xạ thanh điều hướng bằng findViewById()
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // 3. Đặt màn hình mặc định đầu tiên khi mở app là Trang chủ (HomeFragment)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        // 4. Lắng nghe sự kiện click chọn các tab trên thanh Menu để tráo đổi Fragment
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // Kiểm tra ID của tab được bấm để khởi tạo đúng Fragment
                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment(); // Trang chủ (Giai đoạn 3)
                } else if (item.getItemId() == R.id.nav_orders) {
                    selectedFragment = new MyOrdersFragment(); // Lịch sử đơn hàng (Giai đoạn 6)
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment(); // Hồ sơ cá nhân (Giai đoạn 7)
                }

                // Thực hiện tráo đổi Fragment vào ô chứa xml
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            }
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