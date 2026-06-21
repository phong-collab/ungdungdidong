package com.example.travelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.travelapp.R;
import com.example.travelapp.models.UserModel;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtName, edtEmail, edtPhone, edtPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private android.widget.ImageButton btnBackRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackRegister = findViewById(R.id.btnBackRegister);

        btnBackRegister.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                finish(); // Đóng RegisterActivity để quay lui về LoginActivity
            }
        });
        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            boolean isValid = true;

            if (name.isEmpty()) {
                edtName.setError("Vui lòng nhập họ và tên");
                edtName.requestFocus();
                isValid = false;
            }

            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập địa chỉ email");
                if (isValid) {
                    edtEmail.requestFocus();
                }
                isValid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không đúng định dạng");
                if (isValid) {
                    edtEmail.requestFocus();
                }
                isValid = false;
            }

            if (phone.isEmpty()) {
                edtPhone.setError("Vui lòng nhập số điện thoại");
                if (isValid) {
                    edtPhone.requestFocus();
                }
                isValid = false;
            } else if (!phone.matches("^0[0-9]{9}$")) {
                edtPhone.setError("Số điện thoại không hợp lệ (phải bắt đầu bằng số 0 và có 10 chữ số)");
                if (isValid) {
                    edtPhone.requestFocus();
                }
                isValid = false;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                if (isValid) {
                    edtPassword.requestFocus();
                }
                isValid = false;
            } else if (password.length() < 6) {
                edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                if (isValid) {
                    edtPassword.requestFocus();
                }
                isValid = false;
            }

            if (!isValid) return;

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();
                    UserModel user = new UserModel(uid, name, email, phone, "user");
                    db.collection("users").document(uid).set(user).addOnSuccessListener(aVoid -> {
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Lỗi đăng ký!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}