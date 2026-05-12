package com.krishiscan.app.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.krishiscan.app.R;
import com.krishiscan.app.fragments.ChatFragment;
import com.krishiscan.app.fragments.ProfileFragment;
import com.krishiscan.app.fragments.ScanFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        if (savedInstanceState == null) show(new ScanFragment());

        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_scan) return show(new ScanFragment());
            if (item.getItemId() == R.id.nav_chat) return show(new ChatFragment());
            if (item.getItemId() == R.id.nav_profile) return show(new ProfileFragment());
            return false;
        });
    }

    private boolean show(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainNavHost, fragment)
                .commit();
        return true;
    }
}
