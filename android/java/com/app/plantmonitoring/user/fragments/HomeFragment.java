package com.app.plantmonitoring.user.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.app.plantmonitoring.user.AddPlantActivity;
import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.devices.Device;
import com.app.plantmonitoring.user.recyclerview.PlantViewModel;
import com.app.plantmonitoring.database.users.User;
import com.app.plantmonitoring.user.recyclerview.PlantAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // Layout elements
    private View view;
    private RecyclerView recyclerView;
    private FloatingActionButton addBtn;

    // Firebase
    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    // Loading animation
    private ProgressDialog progressDialog;

    // For displaying the list of devices
    private PlantAdapter adapter;
    private List<PlantViewModel> plantViewModels = new ArrayList<>();

    private List<Device> allDevices = new ArrayList<>();
    private List<String> allDevicesIds = new ArrayList<>();
    private User user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle("Home");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plants, container, false);

        setConnections();
//        initProgressDialog();

        user = databaseHelper.getCurrentUser();

        initRecyclerView();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddPlantActivity.class));
            }
        });

        return view;
    }

    // Initialize layout connexions
    private void setConnections() {
        recyclerView = view.findViewById(R.id.recycler_view);
        addBtn = view.findViewById(R.id.add_plant_btn);
    }

    // Initialize loading animation
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void initRecyclerView() {
        databaseHelper.getDeviceRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allDevices.clear();
                allDevicesIds.clear();
                plantViewModels.clear();
                // Get all devices from database
                for (DataSnapshot data : snapshot.getChildren()) {
                    allDevices.add(data.getValue(Device.class));
                    allDevicesIds.add(allDevices.get(allDevices.size() - 1).getId());
                }
                // Store all devices in DatabaseHelper
                databaseHelper.setAllDevices(allDevices);
                databaseHelper.setAllDevicesIds(allDevicesIds);
                // Get all user devices
                for (String id : user.getDevices()) {
                    if (allDevicesIds.contains(id)) {
                        int index = allDevicesIds.indexOf(id);
                        databaseHelper.getDeviceRef().child(id).child("url").getRef()
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    // Add device for displaying in recycler view
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            plantViewModels.add(new PlantViewModel(id, allDevices.get(index).getName(),
                                                    snapshot.getValue(String.class)));
                                        } else {
                                            plantViewModels.add(new PlantViewModel(id, allDevices.get(index).getName()));
                                        }
                                        // Recycler view initialize
                                        recyclerView.setLayoutManager(
                                                new StaggeredGridLayoutManager(2,
                                                        StaggeredGridLayoutManager.VERTICAL));
                                        adapter = new PlantAdapter(view.getContext());
                                        recyclerView.setAdapter(adapter);
                                        adapter.setPlants(plantViewModels);
                                        // Stop loading animation
//                                        if (progressDialog.isShowing()) {
//                                            progressDialog.dismiss();
//                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Search button
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }
}