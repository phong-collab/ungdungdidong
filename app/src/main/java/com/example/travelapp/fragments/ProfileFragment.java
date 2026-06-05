package com.example.travelapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.travelapp.R;
import com.example.travelapp.activities.AddTourActivity;
import com.example.travelapp.activities.LoginActivity;

public class ProfileFragment extends Fragment {
    private TextView txtProfileName, txtProfileEmail, txtProfilePhone;
    private Button btnAdminDashboard, btnLogout;

    // ĐÃ THÊM: Nút điều hướng sang trang chỉnh sửa thông tin
    private Button btnEditProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout giao diện xml vào fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ toàn bộ View từ file xml bằng findViewById()
        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);
        txtProfilePhone = view.findViewById(R.id.txtProfilePhone);
        btnAdminDashboard = view.findViewById(R.id.btnAdminDashboard);
        btnLogout = view.findViewById(R.id.btnLogout);

        // ĐÃ THÊM: Ánh xạ nút Chỉnh sửa thông tin
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Kiểm tra chắc chắn xem người dùng đã đăng nhập chưa
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            loadUserData(userId);
        } else {
            redirectToLogin();
        }

        // 2. Xử lý sự kiện nút Đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Xóa trạng thái đăng nhập trên Firebase Auth
            Toast.makeText(getContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        // 3. Xử lý sự kiện bấm vào nút Admin (Dẫn thẳng qua trang Thống Kê & Báo Cáo Doanh Thu)
        btnAdminDashboard.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminDashboardFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 4. ĐÃ THÊM: Xử lý sự kiện chuyển sang trang sửa Profile khi click nút
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        // Thay thế vùng chứa bằng EditProfileFragment mới tạo ở bước trước
                        // LƯU Ý: Đảm bảo R.id.fragment_container khớp 100% với ID FrameLayout bọc Fragment trong activity_main.xml của bạn
                        .replace(R.id.fragment_container, new EditProfileFragment())
                        // Đẩy vào BackStack để khi bấm Back trên điện thoại sẽ lùi lại trang Profile thay vì đóng app
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    // Hàm lấy dữ liệu User động từ Cloud Firestore
    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Kéo từng chuỗi thông tin về
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String role = documentSnapshot.getString("role");

                        // Đổ dữ liệu chữ thay thế cho cụm "Đang tải..."
                        txtProfileName.setText("Họ và tên: " + name);
                        txtProfileEmail.setText("Email: " + email);
                        txtProfilePhone.setText("Số điện thoại: " + phone);

                        // KIỂM TRA PHÂN QUYỀN ADMIN (Giai đoạn 7)
                        // Nếu trường role trên Firestore có chữ "admin", nút ẩn sẽ hiện lên
                        if ("admin".equals(role)) {
                            btnAdminDashboard.setText("BÁO CÁO & THỐNG KÊ DOANH THU");
                            btnAdminDashboard.setVisibility(View.VISIBLE);
                        } else {
                            btnAdminDashboard.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm điều hướng quay lại màn hình Đăng nhập
    private void redirectToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish(); // Đóng luôn MainActivity để không bấm Back quay lại được
        }
    }
}