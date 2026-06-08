package com.example.travelapp.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.travelapp.R;
import com.example.travelapp.adapters.AdminCategoryAdapter;
import com.example.travelapp.models.CategoryModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminCategoriesFragment extends Fragment {

    private RecyclerView rvCategories;
    private FloatingActionButton fabAddCategory;
    private FirebaseFirestore db;
    private List<CategoryModel> categoryList;
    private AdminCategoryAdapter adapter;
    private Uri selectedImageUri;
    private ImageView imgPreview;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (imgPreview != null) {
                        imgPreview.setImageURI(selectedImageUri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_categories, container, false);

        db = FirebaseFirestore.getInstance();
        rvCategories = view.findViewById(R.id.rvAdminCategories);
        fabAddCategory = view.findViewById(R.id.fabAddCategory);

        categoryList = new ArrayList<>();
        adapter = new AdminCategoryAdapter(categoryList, new AdminCategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onEditClick(CategoryModel category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDeleteClick(CategoryModel category) {
                deleteCategory(category);
            }
        });

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setAdapter(adapter);

        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        loadCategories();

        return view;
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        TextInputEditText edtName = dialogView.findViewById(R.id.edtCategoryName);
        imgPreview = dialogView.findViewById(R.id.imgCategoryPreview);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        selectedImageUri = null;

        imgPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadImageToCloudinary(name, dialog);
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn hình ảnh cho danh mục", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void loadCategories() {
        db.collection("categories")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        categoryList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CategoryModel category = doc.toObject(CategoryModel.class);
                            if (category != null) {
                                category.setId(doc.getId());
                                categoryList.add(category);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showEditCategoryDialog(CategoryModel category) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        TextInputEditText edtName = dialogView.findViewById(R.id.edtCategoryName);
        imgPreview = dialogView.findViewById(R.id.imgCategoryPreview);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        selectedImageUri = null;

        edtName.setText(category.getName());
        Glide.with(this).load(category.getImage()).into(imgPreview);

        imgPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) return;

            if (selectedImageUri != null) {
                uploadImageToCloudinary(name, dialog, category.getId());
            } else {
                updateCategoryFirestore(category.getId(), name, category.getImage(), dialog);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void deleteCategory(CategoryModel category) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete " + category.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("categories").document(category.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadImageToCloudinary(String name, AlertDialog dialog) {
        uploadImageToCloudinary(name, dialog, null);
    }

    private void uploadImageToCloudinary(String name, AlertDialog dialog, String existingId) {
        MediaManager.get().upload(selectedImageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        if (existingId == null) {
                            saveCategoryToFirestore(name, imageUrl, dialog);
                        } else {
                            updateCategoryFirestore(existingId, name, imageUrl, dialog);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(getContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void updateCategoryFirestore(String id, String name, String imageUrl, AlertDialog dialog) {
        Map<String, Object> category = new HashMap<>();
        category.put("name", name);
        category.put("image", imageUrl);

        db.collection("categories").document(id)
                .update(category)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    selectedImageUri = null;
                });
    }

    private void saveCategoryToFirestore(String name, String imageUrl, AlertDialog dialog) {
        Map<String, Object> category = new HashMap<>();
        category.put("name", name);
        category.put("image", imageUrl);

        db.collection("categories")
                .add(category)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Category added", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    selectedImageUri = null;
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}