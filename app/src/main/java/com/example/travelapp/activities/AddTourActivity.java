package com.example.travelapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.travelapp.R;
import com.example.travelapp.models.CategoryModel;
import com.example.travelapp.models.TourModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTourActivity extends AppCompatActivity {

    private TextInputEditText edtTitle, edtPrice, edtPriceChild, edtDesc;
    private AutoCompleteTextView spinnerCategory;
    private SwitchMaterial switchFeatured;
    private ImageView imgPreview;
    private MaterialCardView cardSelectImage;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;

    private FirebaseFirestore db;
    private String tourId = null;
    private Uri selectedImageUri;
    private String currentImageUrl = "";
    private List<CategoryModel> categories = new ArrayList<>();
    private String selectedCategoryId = "";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgPreview.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour);

        db = FirebaseFirestore.getInstance();
        initViews();

        tourId = getIntent().getStringExtra("TOUR_ID");
        if (tourId != null) {
            toolbar.setTitle("Edit Tour");
            loadTourData();
        }

        loadCategories();

        cardSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> validateAndSave());
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edtAddTourTitle);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        edtPrice = findViewById(R.id.edtAddTourPrice);
        edtPriceChild = findViewById(R.id.edtAddTourPriceChild);
        edtDesc = findViewById(R.id.edtAddTourDesc);
        switchFeatured = findViewById(R.id.switchFeatured);
        imgPreview = findViewById(R.id.imgAddTourPreview);
        cardSelectImage = findViewById(R.id.cardSelectImage);
        btnSave = findViewById(R.id.btnSaveTour);
        toolbar = findViewById(R.id.toolbarAddTour);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categories.clear();
            List<String> categoryNames = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                CategoryModel cat = doc.toObject(CategoryModel.class);
                if (cat != null) {
                    cat.setId(doc.getId());
                    categories.add(cat);
                    categoryNames.add(cat.getName());
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
            spinnerCategory.setAdapter(adapter);
            spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
                selectedCategoryId = categories.get(position).getId();
            });
        });
    }

    private void loadTourData() {
        db.collection("tours").document(tourId).get().addOnSuccessListener(doc -> {
            TourModel tour = doc.toObject(TourModel.class);
            if (tour != null) {
                edtTitle.setText(tour.getTitle());
                edtPrice.setText(String.valueOf(tour.getPriceAdult()));
                edtPriceChild.setText(String.valueOf(tour.getPriceChild()));
                edtDesc.setText(tour.getDescription());
                switchFeatured.setChecked(tour.isFeatured());
                currentImageUrl = tour.getThumbnail();
                selectedCategoryId = tour.getCategoryId();
                
                Glide.with(this).load(currentImageUrl).into(imgPreview);
                
                // Find and set category name in spinner
                db.collection("categories").document(selectedCategoryId).get().addOnSuccessListener(catDoc -> {
                    if (catDoc.exists()) {
                        spinnerCategory.setText(catDoc.getString("name"), false);
                    }
                });
            }
        });
    }

    private void validateAndSave() {
        String title = edtTitle.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String priceChildStr = edtPriceChild.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();

        if (title.isEmpty() || priceStr.isEmpty() || selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndSave(title, priceStr, priceChildStr, desc);
        } else if (!currentImageUrl.isEmpty()) {
            saveToFirestore(title, priceStr, priceChildStr, desc, currentImageUrl);
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSave(String title, String price, String priceChild, String desc) {
        MediaManager.get().upload(selectedImageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        saveToFirestore(title, price, priceChild, desc, imageUrl);
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(AddTourActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveToFirestore(String title, String price, String priceChild, String desc, String imageUrl) {
        Map<String, Object> tour = new HashMap<>();
        tour.put("title", title);
        tour.put("priceAdult", Long.parseLong(price));
        tour.put("priceChild", priceChild.isEmpty() ? 0 : Long.parseLong(priceChild));
        tour.put("description", desc);
        tour.put("thumbnail", imageUrl);
        tour.put("categoryId", selectedCategoryId);
        tour.put("isFeatured", switchFeatured.isChecked());

        if (tourId == null) {
            db.collection("tours").add(tour).addOnSuccessListener(ref -> finish());
        } else {
            db.collection("tours").document(tourId).update(tour).addOnSuccessListener(aVoid -> finish());
        }
    }
}