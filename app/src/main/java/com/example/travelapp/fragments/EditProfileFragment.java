package com.example.travelapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.travelapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private TextInputEditText edtProfileName, edtProfilePhone, edtProfileEmail;
    private Button btnSaveProfile;
    private android.widget.ImageButton btnBackEditProfile;
    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "";
        }

        // Ánh xạ linh kiện UI
        edtProfileName = view.findViewById(R.id.edtProfileName);
        edtProfilePhone = view.findViewById(R.id.edtProfilePhone);
        edtProfileEmail = view.findViewById(R.id.edtProfileEmail);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnBackEditProfile = view.findViewById(R.id.btnBackEditProfile);

        if (btnBackEditProfile != null) {
            btnBackEditProfile.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        if (!currentUserId.isEmpty()) {
            loadUserProfileData();
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập hệ thống!", Toast.LENGTH_SHORT).show();
        }

        // Bắt sự kiện nút Lưu thay đổi
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
    }

    // 1. Hàm nạp thông tin người dùng hiện tại lên các ô nhập liệu
    private void loadUserProfileData() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                String phone = doc.getString("phone");
                String email = doc.getString("email");

                edtProfileName.setText(name != null ? name : "");
                edtProfilePhone.setText(phone != null ? phone : "");
                edtProfileEmail.setText(email != null ? email : FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Không thể tải thông tin profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // 2. Hàm gom thông tin mới và cập nhật đồng bộ lên Firestore bảng "users"
    private void saveProfileChanges() {
        String updatedName = edtProfileName.getText().toString().trim();
        String updatedPhone = edtProfilePhone.getText().toString().trim();

        // Kiểm tra validation nhập liệu cơ bản
        if (updatedName.isEmpty()) {
            edtProfileName.setError("Họ tên không được để trống!");
            return;
        }
        if (updatedPhone.isEmpty()) {
            edtProfilePhone.setError("Số điện thoại không được để trống!");
            return;
        }

        // Tạo Map bọc các cột cần sửa đổi (Chỉ cập nhật những trường thay đổi)
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", updatedName);
        updates.put("phone", updatedPhone);

        btnSaveProfile.setEnabled(false); // Khóa nút tạm thời tránh bấm liên tục

        db.collection("users").document(currentUserId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(getContext(), "Cập nhật thông tin tài khoản thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(getContext(), "Lỗi cập nhật dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}