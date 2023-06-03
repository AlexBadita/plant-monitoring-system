package com.app.plantmonitoring.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.app.plantmonitoring.LoginActivity;
import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.plants.Plant;
import com.app.plantmonitoring.user.fragments.HomeFragment;
import com.app.plantmonitoring.user.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Layout elements
    private DrawerLayout drawer;
    private Toolbar toolbar;

    // Firebase
    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        statusBarColor();
//        subscribeNotifications();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.menu_drawer_open, R.string.menu_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.home);
        }

        databaseHelper.getPlantRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Plant> plants = new ArrayList<>();
                for(DataSnapshot data : snapshot.getChildren()){
                    plants.add(data.getValue(Plant.class));
                }
                databaseHelper.setAllPlants(plants);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        } else if (item.getItemId() == R.id.profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        } else if (item.getItemId() == R.id.signout) {
            unsubscribeNotifications();
            // Sign out user
            databaseHelper.getAuth().signOut();
            // Go to login page
            Intent intent = new Intent(NavigationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void statusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }

    private void subscribeNotifications(){
        for(String id : databaseHelper.getCurrentUser().getDevices()){
            String topic = "temperature_warning" + id;
            databaseHelper.getFirebaseMessaging().subscribeToTopic(topic);
            topic = "humidity_warning" + id;
            databaseHelper.getFirebaseMessaging().subscribeToTopic(topic);
            topic = "light_warning" + id;
            databaseHelper.getFirebaseMessaging().subscribeToTopic(topic);
        }
    }

    private void unsubscribeNotifications(){
        for(String id : databaseHelper.getCurrentUser().getDevices()){
            String topic = "temperature_warning" + id;
            databaseHelper.getFirebaseMessaging().unsubscribeFromTopic(topic);
            topic = "humidity_warning" + id;
            databaseHelper.getFirebaseMessaging().unsubscribeFromTopic(topic);
            topic = "light_warning" + id;
            databaseHelper.getFirebaseMessaging().unsubscribeFromTopic(topic);
        }
    }
}