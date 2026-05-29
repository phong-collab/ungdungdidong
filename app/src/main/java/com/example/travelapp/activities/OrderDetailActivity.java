package com.example.travelapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.travelapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrderDetailActivity extends AppCompatActivity {
    private ImageView imgDetailThum;
    private TextView txtDetailTourTitle, txtDetailOrderId, txtDetailStatus, txtDetailCountAdult, txtDetailCountChild, txtDetailTotalPrice;
    private ImageButton btnBackDetail;
    private FirebaseFirestore db;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        db = FirebaseFirestore.getInstance();
        bookingId = getIntent().getStringExtra("BOOKING_ID");

        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy dữ liệu chuyến đi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ linh kiện từ XML sang Java
        imgDetailThum = findViewById(R.id.imgDetailThum);
        txtDetailTourTitle = findViewById(R.id.txtDetailTourTitle);
        txtDetailOrderId = findViewById(R.id.txtDetailOrderId);
        txtDetailStatus = findViewById(R.id.txtDetailStatus);
        txtDetailCountAdult = findViewById(R.id.txtDetailCountAdult);
        txtDetailCountChild = findViewById(R.id.txtDetailCountChild);
        txtDetailTotalPrice = findViewById(R.id.txtDetailTotalPrice);
        btnBackDetail = findViewById(R.id.btnBackDetail);

        btnBackDetail.setOnClickListener(v -> finish());

        // Gọi hàm kéo dữ liệu chi tiết hóa đơn từ Firestore
        loadBookingDetail();
    }

    private void loadBookingDetail() {
        db.collection("bookings").document(bookingId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String title = doc.getString("tourTitle");
                String thumbnail = doc.getString("tourThumbnail");
                String status = doc.getString("paymentStatus");
                Long totalPrice = doc.getLong("totalPrice");

                // Phòng trường hợp trong đơn có lưu số lượng hành khách (Nếu không có mặc định hiện theo model)
                Long countAdult = doc.getLong("countAdult") != null ? doc.getLong("countAdult") : 1L;
                Long countChild = doc.getLong("countChild") != null ? doc.getLong("countChild") : 0L;

                // Set thông tin lên UI
                txtDetailTourTitle.setText(title);
                txtDetailOrderId.setText(bookingId);
                txtDetailCountAdult.setText("x" + countAdult);
                txtDetailCountChild.setText("x" + countChild);
                txtDetailTotalPrice.setText(String.format("%,d đ", totalPrice != null ? totalPrice : 0));

                if (status == null || status.isEmpty()) status = "PENDING";
                txtDetailStatus.setText(status);

                // Ép đổi màu trạng thái trực quan
                if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
                    txtDetailStatus.setTextColor(Color.parseColor("#4CAF50")); // Màu xanh lá
                } else {
                    txtDetailStatus.setTextColor(Color.parseColor("#FF9800")); // Màu cam chờ xử lý
                }

                // Load ảnh đại diện chuyến đi
                Glide.with(this)
                        .load(thumbnail)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(imgDetailThum);
            } else {
                Toast.makeText(this, "Đơn hàng không tồn tại trên hệ thống!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi kết nối Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}