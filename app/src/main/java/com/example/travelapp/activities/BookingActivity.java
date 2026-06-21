package com.example.travelapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.travelapp.R;
import com.example.travelapp.models.TourModel;
import com.example.travelapp.utils.Helper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {
    private TextView txtBookingTourTitle, txtPriceAdultLabel, txtPriceChildLabel, txtCountAdult, txtCountChild, txtBookingTotalPrice;
    private TextView txtDepartureDate; // ĐÃ THÊM: hiển thị ngày khởi hành đã chọn
    private Button btnMinusAdult, btnPlusAdult, btnMinusChild, btnPlusChild, btnPayZaloPay, btnSelectDate; // ĐÃ THÊM: btnSelectDate
    private ImageButton btnBackBooking;
    private FirebaseFirestore db;
    private String tourId, userId, tourTitle, tourThumbnail;
    private long priceAdult = 0, priceChild = 0, totalAmount = 0;
    private int countAdult = 1, countChild = 0;

    // Biến toàn cục để lưu lại mã tài liệu hóa đơn hiện tại trong phiên thanh toán
    private String currentBookingDocId = "";
    private String selectedDepartureDate = ""; // ĐÃ THÊM: lưu ngày khởi hành được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = FirebaseFirestore.getInstance();

        // Nhận diện linh hoạt cả hai kiểu khóa "tourId" hoặc "TOUR_ID" để tránh bị null
        tourId = getIntent().getStringExtra("tourId");
        if (tourId == null || tourId.isEmpty()) {
            tourId = getIntent().getStringExtra("TOUR_ID");
        }

        // Bảo vệ app: Nếu không tìm thấy Id thì không chạy tiếp để tránh văng app
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Không thể nạp dữ liệu đặt tour!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = "";
        }

        // Ánh xạ các linh kiện giao diện
        txtBookingTourTitle = findViewById(R.id.txtBookingTourTitle);
        txtPriceAdultLabel = findViewById(R.id.txtPriceAdultLabel);
        txtPriceChildLabel = findViewById(R.id.txtPriceChildLabel);
        txtCountAdult = findViewById(R.id.txtCountAdult);
        txtCountChild = findViewById(R.id.txtCountChild);
        txtBookingTotalPrice = findViewById(R.id.txtBookingTotalPrice);
        txtDepartureDate = findViewById(R.id.txtDepartureDate); // ĐÃ THÊM
        btnMinusAdult = findViewById(R.id.btnMinusAdult);
        btnPlusAdult = findViewById(R.id.btnPlusAdult);
        btnMinusChild = findViewById(R.id.btnMinusChild);
        btnPlusChild = findViewById(R.id.btnPlusChild);
        btnPayZaloPay = findViewById(R.id.btnPayZaloPay);
        btnSelectDate = findViewById(R.id.btnSelectDate); // ĐÃ THÊM
        btnBackBooking = findViewById(R.id.btnBackBooking);

        if (btnBackBooking == null || btnPayZaloPay == null || btnPlusAdult == null || btnMinusAdult == null
                || btnPlusChild == null || btnMinusChild == null || btnSelectDate == null || txtDepartureDate == null) {
            Toast.makeText(this, "Loi giao dien: thieu nut trong layout dat tour!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Gọi nạp dữ liệu chi tiết tour lên trang đặt vé
        getTourPriceData();

        btnBackBooking.setOnClickListener(v -> finish());

        // Đăng ký sự kiện tăng giảm số lượng hành khách
        btnPlusAdult.setOnClickListener(v -> { countAdult++; updatePriceUI(); });
        btnMinusAdult.setOnClickListener(v -> { if (countAdult > 1) { countAdult--; updatePriceUI(); } });
        btnPlusChild.setOnClickListener(v -> { countChild++; updatePriceUI(); });
        btnMinusChild.setOnClickListener(v -> { if (countChild > 0) { countChild--; updatePriceUI(); } });
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog()); // ĐÃ THÊM

        btnPayZaloPay.setOnClickListener(v -> {
            if (userId.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập để tiến hành đặt tour!", Toast.LENGTH_SHORT).show();
                return;
            }
            startBookingWorkflow();
        });
    }

    private void getTourPriceData() {
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Khong the nap du lieu dat tour!", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("tours").document(tourId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                TourModel tour = doc.toObject(TourModel.class);
                if (tour != null) {
                    tourTitle = tour.getTitle();
                    tourThumbnail = tour.getThumbnail();
                    priceAdult = tour.getPriceAdult();

                    // Ưu tiên lấy giá trẻ em từ database, nếu bằng 0 thì lấy giá người lớn / 2
                    priceChild = tour.getPriceChild() > 0 ? tour.getPriceChild() : (priceAdult / 2);

                    txtBookingTourTitle.setText(tourTitle);
                    txtPriceAdultLabel.setText(String.format("%,d đ/người", priceAdult));
                    txtPriceChildLabel.setText(String.format("%,d đ/người", priceChild));
                    updatePriceUI();
                }
            } else {
                Toast.makeText(this, "Sản phẩm tour này không tồn tại!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi kết nối máy chủ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePriceUI() {
        txtCountAdult.setText(String.valueOf(countAdult));
        txtCountChild.setText(String.valueOf(countChild));
        totalAmount = (countAdult * priceAdult) + (countChild * priceChild);
        txtBookingTotalPrice.setText(String.format("%,d đ", totalAmount));
    }

    private void showDatePickerDialog() {
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDepartureDate = String.format(java.util.Locale.getDefault(), "%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear);
                    txtDepartureDate.setText(selectedDepartureDate);
                    txtDepartureDate.setTextColor(android.graphics.Color.BLACK);
                    txtDepartureDate.setTypeface(null, android.graphics.Typeface.NORMAL);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void startBookingWorkflow() {
        if (selectedDepartureDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày khởi hành trước khi thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }

        String appTransId = Helper.getAppTransId();
        currentBookingDocId = "book_" + appTransId; // Lưu mã hóa đơn để chuẩn bị cập nhật sau thanh toán

        // Lưu currentBookingDocId vào SharedPreferences đề phòng app bị giải phóng bộ nhớ
        getSharedPreferences("ZaloPayPrefs", MODE_PRIVATE)
                .edit()
                .putString("currentBookingDocId", currentBookingDocId)
                .apply();

        Map<String, Object> booking = new HashMap<>();
        booking.put("id", currentBookingDocId);
        booking.put("userId", userId);
        booking.put("tourId", tourId);
        booking.put("tourTitle", tourTitle);
        booking.put("tourThumbnail", tourThumbnail);
        booking.put("totalPrice", totalAmount);
        booking.put("paymentStatus", "PENDING");
        booking.put("departureDate", selectedDepartureDate); // ĐÃ THÊM

        // Lưu số lượng người lớn và trẻ em thực tế lên Firestore
        booking.put("countAdult", (long) countAdult);
        booking.put("countChild", (long) countChild);

        booking.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("bookings").document(currentBookingDocId).set(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đang khởi tạo liên kết ZaloPay...", Toast.LENGTH_SHORT).show();
                    new RequestZaloPayTokenTask().execute(appTransId, String.valueOf(totalAmount));
                })

                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Tạo đơn hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm cập nhật trạng thái đơn hàng thành công sau khi nhận tín hiệu phản hồi từ ZaloPay
    private void updatePaymentStatusToSuccess(String bookingDocId) {
        db.collection("bookings").document(bookingDocId)
                .update("paymentStatus", "SUCCESS")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đặt tour và thanh toán hoàn tất thành công!", Toast.LENGTH_LONG).show();

                    // Xóa session trong SharedPreferences
                    getSharedPreferences("ZaloPayPrefs", MODE_PRIVATE).edit().remove("currentBookingDocId").apply();
                    currentBookingDocId = "";

                    // Chuyển hướng người dùng về trang chính sau khi thanh toán thành công
                    Intent intent = new Intent(BookingActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật trạng thái hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm cập nhật trạng thái đơn hàng thất bại
    private void updatePaymentStatusToFailed(String bookingDocId) {
        db.collection("bookings").document(bookingDocId)
                .update("paymentStatus", "FAILED")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Thanh toán ZaloPay thất bại hoặc đã bị hủy!", Toast.LENGTH_LONG).show();

                    // Xóa session trong SharedPreferences
                    getSharedPreferences("ZaloPayPrefs", MODE_PRIVATE).edit().remove("currentBookingDocId").apply();
                    currentBookingDocId = "";
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật trạng thái hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật intent mới chứa liên kết gọi về
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Uri data = intent.getData();

        // Phục hồi currentBookingDocId từ SharedPreferences nếu bị trống
        if (currentBookingDocId == null || currentBookingDocId.isEmpty()) {
            currentBookingDocId = getSharedPreferences("ZaloPayPrefs", MODE_PRIVATE)
                    .getString("currentBookingDocId", "");
        }

        // Kiểm tra xem ứng dụng được gọi ngược lại bằng Deep Link từ ZaloPay không
        if (data != null && "travelapp".equals(data.getScheme()) && "payment".equals(data.getHost())) {
            String status = data.getQueryParameter("status");
            if ("1".equals(status)) {
                if (!currentBookingDocId.isEmpty()) {
                    updatePaymentStatusToSuccess(currentBookingDocId);
                }
            } else {
                if (!currentBookingDocId.isEmpty()) {
                    updatePaymentStatusToFailed(currentBookingDocId);
                }
            }
            intent.setData(null); // Clear liên kết để tránh lặp hàm khi xoay màn hình
        } else {
            // Nếu người dùng tự quay lại ứng dụng (bấm Back/Home) mà không qua deep link
            if (!currentBookingDocId.isEmpty()) {
                checkPaymentStatus();
            }
        }
    }

    private void checkPaymentStatus() {
        if (currentBookingDocId == null || currentBookingDocId.isEmpty()) return;
        String appTransId = currentBookingDocId.replace("book_", "");
        new QueryZaloPayStatusTask().execute(appTransId);
    }

    // --- TIẾN TRÌNH TRUY VẤN MẠNG GỌI CỔNG THANH TOÁN ZALOPAY ---
    private class RequestZaloPayTokenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String appTransId = params[0];
            String amount = params[1];

            // Sử dụng thời gian thực để tránh lỗi hết hạn mã giao dịch từ hệ thống cổng
            String appTime = String.valueOf(System.currentTimeMillis());
            String redirectUrl = "travelapp://payment";

            try {
                URL url = new URL("https://sb-openapi.zalopay.vn/v2/create");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                // Cấu hình thông tin ứng dụng đồng nhất với AppInfo
                String appId = String.valueOf(com.example.travelapp.Constant.AppInfo.APP_ID);
                String macKey = com.example.travelapp.Constant.AppInfo.MAC_KEY;
                String appUser = "Đồ Án Đặt Tour";

                // Đưa redirecturl vào embeddata theo đúng chuẩn ZaloPay v2
                String embedData = "{\"redirecturl\":\"" + redirectUrl + "\"}";
                String item = "[]";

                // Cấu trúc chuỗi MAC chuẩn v2: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
                String data = appId + "|" + appTransId + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedData + "|" + item;
                String mac = Helper.hmacSHA256(data, macKey);

                String urlParameters = "app_id=" + appId
                        + "&app_trans_id=" + appTransId
                        + "&app_user=" + Uri.encode(appUser)
                        + "&amount=" + amount
                        + "&app_time=" + appTime
                        + "&embed_data=" + Uri.encode(embedData)
                        + "&item=" + Uri.encode(item)
                        + "&description=" + Uri.encode("Thanh toan do an dat tour")
                        + "&mac=" + mac;

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                if (jsonObject.has("return_code") && jsonObject.getInt("return_code") == 1) {
                    return jsonObject.getString("order_url");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String orderUrl) {
            if (orderUrl != null && !orderUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl));
                startActivity(intent);
            } else {
                Toast.makeText(BookingActivity.this, "Không thể lấy cổng liên kết thanh toán ZaloPay! Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- TIẾN TRÌNH TRUY VẤN MẠNG GỌI CỔNG TRUY VẤN TRẠNG THÁI ZALOPAY ---
    private class QueryZaloPayStatusTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            String appTransId = params[0];
            try {
                URL url = new URL("https://sb-openapi.zalopay.vn/v2/query");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String appId = String.valueOf(com.example.travelapp.Constant.AppInfo.APP_ID);
                String macKey = com.example.travelapp.Constant.AppInfo.MAC_KEY;

                // Cấu trúc chuỗi MAC truy vấn: app_id|app_trans_id|key1
                String data = appId + "|" + appTransId + "|" + macKey;
                String mac = Helper.hmacSHA256(data, macKey);

                String urlParameters = "app_id=" + appId
                        + "&app_trans_id=" + appTransId
                        + "&mac=" + mac;

                byte[] postData = urlParameters.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", String.valueOf(postData.length));

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(postData);
                wr.flush();
                wr.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                if (jsonObject.has("return_code")) {
                    return jsonObject.getInt("return_code");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer returnCode) {
            if (returnCode == 1) {
                // Giao dịch thành công
                updatePaymentStatusToSuccess(currentBookingDocId);
            } else if (returnCode == 2) {
                // Giao dịch thất bại
                updatePaymentStatusToFailed(currentBookingDocId);
            }
        }
    }
}