package com.s22010104.procutnation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // A delayed action will be posted by a handler.
        // The delay is 2.5 seconds (2500 milliseconds).
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if a user is currently signed in with Firebase
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    // If user is already logged in, go to the Home screen
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } else {
                    // If no user is logged in, go to the SignUp screen
                    startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
                }

                // Close the splash activity so the user can't return to it.
                finish();
            }
        }, 2500); // 2.5 second delay
    }
}
