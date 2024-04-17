package com.example.integrated;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity{

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Set HomeFragment as the default selected item in the BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

    }


//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if ("ProfileFragment".equals(intent.getStringExtra("OpenFragment"))) {
//            loadProfileFragment();
//        }
//    }

//    private void loadProfileFragment() {
//        ProfileFragment profileFragment = new ProfileFragment();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, profileFragment)
//                .addToBackStack(null)
//                .commit();
//    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    }
                    else if (itemId == R.id.nav_activity) {
                        selectedFragment = new ActivityFragment();
                    }
                    else if (itemId == R.id.nav_score) {
                        selectedFragment = new ScoreFragment();
                    }
                    else if (itemId == R.id.nav_profile) {
                        selectedFragment = new ProfileFragment();
                    }
//                    else if (itemId == R.id.nav_forum) {
//                        selectedFragment = new ForumFragment();
//                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedFragment).commit();
                    }

                    return true;
                }
            };


}
