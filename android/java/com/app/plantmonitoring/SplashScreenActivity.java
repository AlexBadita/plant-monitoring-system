package com.app.plantmonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.users.User;
import com.app.plantmonitoring.user.NavigationActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    // Splash screen lasts 1s
    private static final int SPLASH_SCREEN_TIME = 1000;

    // Database
    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (databaseHelper.getAuth().getCurrentUser() != null) {
            String userId = databaseHelper.getUser().getUid();
            Query query = databaseHelper.getUserRef().orderByChild("id").equalTo(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            databaseHelper.setUserKey(data.getKey());
                            String id = data.child("id").getValue(String.class);
                            String name = data.child("name").getValue(String.class);
                            String email = data.child("email").getValue(String.class);
                            String password = data.child("password").getValue(String.class);
                            List<String> deviceIds = new ArrayList<>();
                            for (DataSnapshot dataSnapshot : data.child("devices").getChildren()) {
                                deviceIds.add(dataSnapshot.getValue(String.class));
                            }
                            databaseHelper.setCurrentUser(new User(id, name, email, password, deviceIds));
                        }
                        startActivity(new Intent(SplashScreenActivity.this, NavigationActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, OpeningScreenActivity.class));
                    finish();
                }
            }, SPLASH_SCREEN_TIME);
        }
    }
}