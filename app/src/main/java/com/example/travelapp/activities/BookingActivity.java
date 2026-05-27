package com.example.travelapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
    private Button btnMinusAdult, btnPlusAdult, btnMinusChild, btnPlusChild, btnPayZaloPay;
    private FirebaseFirestore db; private String tourId, userId, tourTitle, tourThumbnail;
    private long priceAdult = 0, priceChild = 0, totalAmount = 0; private int countAdult = 1, countChild = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = FirebaseFirestore.getInstance();
        tourId = getIntent().getStringExtra("TOUR_ID");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

        getTourPriceData();

        btnPlusAdult.setOnClickListener(v -> { countAdult++; updatePriceUI(); });
        btnMinusAdult.setOnClickListener(v -> { if(countAdult > 1) { countAdult--; updatePriceUI(); } });
        btnPlusChild.setOnClickListener(v -> { countChild++; updatePriceUI(); });
        btnMinusChild.setOnClickListener(v -> { if(countChild > 0) { countChild--; updatePriceUI(); } });

        btnPayZaloPay.setOnClickListener(v -> startBookingWorkflow());
    }

    private void getTourPriceData() {
        db.collection("tours").document(tourId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                TourModel tour = doc.toObject(TourModel.class);
                if (tour != null) {
                    tourTitle = tour.getTitle(); tourThumbnail = tour.getThumbnail();
                    priceAdult = tour.getPriceAdult(); priceChild = priceAdult / 2;
                    txtBookingTourTitle.setText(tourTitle);
                    txtPriceAdultLabel.setText(String.format("%,d đ/người", priceAdult));
                    txtPriceChildLabel.setText(String.format("%,d đ/người", priceChild));
                    updatePriceUI();
                }
            }
        });
    }

    private void updatePriceUI() {
        txtCountAdult.setText(String.valueOf(countAdult)); txtCountChild.setText(String.valueOf(countChild));
        totalAmount = (countAdult * priceAdult) + (countChild * priceChild);
        txtBookingTotalPrice.setText(String.format("%,d đ", totalAmount));
    }

    private void startBookingWorkflow() {
        String appTransId = Helper.getAppTransId();
        Map<String, Object> booking = new HashMap<>();
        booking.put("id", "book_" + appTransId); booking.put("userId", userId); booking.put("tourId", tourId);
        booking.put("tourTitle", tourTitle); booking.put("tourThumbnail", tourThumbnail);
        booking.put("totalPrice", totalAmount); booking.put("paymentStatus", "PENDING");
        booking.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("bookings").document("book_" + appTransId).set(booking)
                .addOnSuccessListener(aVoid -> new RequestZaloPayTokenTask().execute(appTransId, String.valueOf(totalAmount)));
    }

    private class RequestZaloPayTokenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String appTransId = params[0]; String amount = params[1];
            try {
                URL url = new URL("https://sb-openapi.zalopay.vn/v2/createorder");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST"); conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String data = "2553|" + appTransId + "|Đồ Án Đặt Tour|" + amount + "|123456789||[]";
                String mac = Helper.hmacSHA256(data, "9phuAOYhan4Ju9crw3A2u849vN6w68uZ");

                String urlParameters = "app_id=2553" + "&app_trans_id=" + appTransId + "&app_user=Đồ Án Đặt Tour"
                        + "&amount=" + amount + "&app_time=123456789" + "&embed_data=[]" + "&item=[]"
                        + "&description=Thanh toan do an" + "&mac=" + mac;

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(urlParameters); wr.flush(); wr.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine; StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                if (jsonObject.getInt("return_code") == 1) return jsonObject.getString("order_url");
            } catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(String orderUrl) {
            if (orderUrl != null) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl)));
                finish();
            }
        }
    }
}