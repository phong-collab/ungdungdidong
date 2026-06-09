package com.example.travelapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelapp.Api.CreateOrder;
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

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class BookingActivity extends AppCompatActivity {
    private TextView txtBookingTourTitle, txtPriceAdultLabel, txtPriceChildLabel, txtCountAdult, txtCountChild, txtBookingTotalPrice;
    private Button btnMinusAdult, btnPlusAdult, btnMinusChild, btnPlusChild, btnPayZaloPay;
    private ImageButton btnBackBooking;
    private FirebaseFirestore db;
    private String tourId, userId, tourTitle, tourThumbnail;
    private long priceAdult = 0, priceChild = 0, totalAmount = 0;
    private int countAdult = 1, countChild = 0;

    // Biến toàn cục để lưu lại mã tài liệu hóa đơn hiện tại trong phiên thanh toán
    private String currentBookingDocId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        StrictMode.ThreadPolicy policy = new
        StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(553, Environment.SANDBOX);

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
        btnMinusAdult = findViewById(R.id.btnMinusAdult);
        btnPlusAdult = findViewById(R.id.btnPlusAdult);
        btnMinusChild = findViewById(R.id.btnMinusChild);
        btnPlusChild = findViewById(R.id.btnPlusChild);
        btnPayZaloPay = findViewById(R.id.btnPayZaloPay);
        btnBackBooking = findViewById(R.id.btnBackBooking);

        if (btnBackBooking == null || btnPayZaloPay == null || btnPlusAdult == null || btnMinusAdult == null
                || btnPlusChild == null || btnMinusChild == null) {
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

    private void startBookingWorkflow() {
        String appTransId = Helper.getAppTransId();
        currentBookingDocId = "book_" + appTransId; // Lưu mã hóa đơn để chuẩn bị cập nhật sau thanh toán

        Map<String, Object> booking = new HashMap<>();
        booking.put("id", currentBookingDocId);
        booking.put("userId", userId);
        booking.put("tourId", tourId);
        booking.put("tourTitle", tourTitle);
        booking.put("tourThumbnail", tourThumbnail);
        booking.put("totalPrice", totalAmount);
        booking.put("paymentStatus", "PENDING");

        // Lưu số lượng người lớn và trẻ em thực tế lên Firestore
        booking.put("countAdult", (long) countAdult);
        booking.put("countChild", (long) countChild);

        booking.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("bookings").document(currentBookingDocId).set(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đang khởi tạo liên kết ZaloPay...", Toast.LENGTH_SHORT).show();
//                    new RequestZaloPayTokenTask().execute(appTransId, String.valueOf(totalAmount));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Tạo đơn hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        CreateOrder orderApi = new CreateOrder();
        String totalString = String.format("%.0f", totalAmount);
        try {
            JSONObject data = orderApi.createOrder(totalString);
            String code = data.getString("return_code");
            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");
                ZaloPaySDK.getInstance().payOrder(BookingActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {
                        db.collection("bookings").document(tourId)
                                .update("paymentStatus", "SUCCESS")
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(BookingActivity.this, NotificationPayment.class);
                                    intent.putExtra("result", "Successfully");
                                    startActivity(intent);
                                });
                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        Intent intent = new Intent(BookingActivity.this, NotificationPayment.class);
                        intent.putExtra("result", "Canceled this payment");
                        startActivity(intent);
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        Intent intent = new Intent(BookingActivity.this, NotificationPayment.class);
                        intent.putExtra("result", "Something went wrong when paying. Please try again!");
                        startActivity(intent);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm cập nhật trạng thái đơn hàng thành công sau khi nhận tín hiệu phản hồi từ ZaloPay
    private void updatePaymentStatusToSuccess(String bookingDocId) {
        db.collection("bookings").document(bookingDocId)
                .update("paymentStatus", "SUCCESS")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đặt tour và thanh toán hoàn tất thành công!", Toast.LENGTH_LONG).show();

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Intent intent = getIntent();
//        Uri data = intent.getData();
//
//        // Kiểm tra xem ứng dụng được gọi ngược lại bằng Deep Link từ ZaloPay thành công không
//        if (data != null && "travelapp".equals(data.getScheme()) && "payment".equals(data.getHost())) {
//            if (!currentBookingDocId.isEmpty()) {
//                updatePaymentStatusToSuccess(currentBookingDocId);
//                currentBookingDocId = ""; // Giải phóng dữ liệu phiên
//            }
//            intent.setData(null); // Clear liên kết để tránh lặp hàm khi xoay màn hình
//        }
//    }

    // --- TIẾN TRÌNH TRUY VẤN MẠNG GỌI CỔNG THANH TOÁN ZALOPAY ---
//    private class RequestZaloPayTokenTask extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... params) {
//            String appTransId = params[0];
//            String amount = params[1];
//
//            // Sử dụng thời gian thực để tránh lỗi hết hạn mã giao dịch từ hệ thống cổng
//            String appTime = String.valueOf(System.currentTimeMillis());
//            String redirectUrl = Uri.encode("travelapp://payment");
//
//            try {
//                URL url = new URL("https://sb-openapi.zalopay.vn/v2/createorder");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                conn.setDoOutput(true);
//
//                // Cấu trúc chuỗi MAC chuẩn Sandbox: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
//                String data = "2553|" + appTransId + "|Đồ Án Đặt Tour|" + amount + "|" + appTime + "|[]|[]";
//                String mac = Helper.hmacSHA256(data, "9phuAOYhan4Ju9crw3A2u849vN6w68uZ");
//
//                String urlParameters = "app_id=2553"
//                        + "&app_trans_id=" + appTransId
//                        + "&app_user=" + Uri.encode("Đồ Án Đặt Tour")
//                        + "&amount=" + amount
//                        + "&app_time=" + appTime
//                        + "&embed_data=" + Uri.encode("[]")
//                        + "&item=" + Uri.encode("[]")
//                        + "&description=" + Uri.encode("Thanh toan do an dat tour")
//                        + "&redirect_url=" + redirectUrl
//                        + "&mac=" + mac;
//
//                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//                wr.writeBytes(urlParameters);
//                wr.flush();
//                wr.close();
//
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) response.append(inputLine);
//                in.close();
//
//                JSONObject jsonObject = new JSONObject(response.toString());
//                if (jsonObject.has("return_code") && jsonObject.getInt("return_code") == 1) {
//                    return jsonObject.getString("order_url");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String orderUrl) {
//            if (orderUrl != null && !orderUrl.isEmpty()) {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl));
//                startActivity(intent);
//                finish();
//            } else {
//                Toast.makeText(BookingActivity.this, "Không thể lấy cổng liên kết thanh toán ZaloPay! Vui lòng thử lại.", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}