package com.krishiscan.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.krishiscan.app.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent i = FirebaseAuth.getInstance().getCurrentUser() == null
                    ? new Intent(this, AuthActivity.class)
                    : new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }, 700);
    }
}
