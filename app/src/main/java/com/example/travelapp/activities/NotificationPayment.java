package com.example.travelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelapp.R;

public class NotificationPayment extends AppCompatActivity {
    TextView Notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_payment);
        Notification = findViewById(R.id.textViewPaymentResult);
        Intent intent = getIntent();
        Notification.setText(intent.getStringExtra("result"));
    }
}