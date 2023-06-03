package com.app.plantmonitoring.user.select_device;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.user.select_device.fragments_item.SettingsFragment;
import com.app.plantmonitoring.user.select_device.fragments_item.ValuesFragment;
import com.app.plantmonitoring.user.select_device.fragments_item.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class PlantActivity extends AppCompatActivity {

    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);

        setConnections();
        statusBarColor();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Check Plant");

        tabLayout.setupWithViewPager(viewPager);

        initAdapter();

    }

    private void initAdapter(){
        Intent intent = getIntent();

        Bundle bundleValues = new Bundle();
        bundleValues.putString("url", intent.getStringExtra("url"));
        id = intent.getStringExtra("id");
        bundleValues.putString("id", id);
        ValuesFragment valuesFragment = new ValuesFragment();
        valuesFragment.setArguments(bundleValues);

        SettingsFragment settingsFragment = new SettingsFragment();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(valuesFragment, "Values");
        adapter.addFragment(settingsFragment, "Settings");
        viewPager.setAdapter(adapter);
    }

    private void setConnections() {
        toolbar = findViewById(R.id.custom_toolbar);
        tabLayout = findViewById(R.id.tab_menu);
        viewPager = findViewById(R.id.tab_content);
    }

    private void statusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
}