package com.s22010104.procutnation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button btnSubmit;
    private ImageView backButton;

    private FirebaseAuth mAuth;
    private static final String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        editTextEmail = findViewById(R.id.editTextEmail);
        btnSubmit = findViewById(R.id.btnSubmit);
        backButton = findViewById(R.id.backButton);

        // Set OnClick Listeners
        backButton.setOnClickListener(v -> finish()); // Go back to the previous screen (Login)
        btnSubmit.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address.");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "sendPasswordResetEmail:success");
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset link sent to your email.",
                                Toast.LENGTH_LONG).show();
                        // Optionally finish this activity and go back to login
                        finish();
                    } else {
                        Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Failed to send reset link: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}

