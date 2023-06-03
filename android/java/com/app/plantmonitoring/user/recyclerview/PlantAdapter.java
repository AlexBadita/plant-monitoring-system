package com.app.plantmonitoring.user.recyclerview;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.plantmonitoring.user.select_device.PlantActivity;
import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.devices.Device;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantViewHolder> implements Filterable {

    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    private List<PlantViewModel> plantViewModels;
    private List<PlantViewModel> allPlantViewModels;
    private Context context;

    public PlantAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.plant_item, parent, false);
        return new PlantViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantViewModel currentPlantViewModel = plantViewModels.get(position);
        holder.getTextView().setText(currentPlantViewModel.getName());
        Picasso.get()
                .load(currentPlantViewModel.getImage())
                .placeholder(R.drawable.plant_default)
                .into(holder.getImageView());

        holder.getCardView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(v.getContext());
                progressDialog.setMessage("Loading...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
                databaseHelper.getDeviceRef().child(currentPlantViewModel.getId()).getRef()
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    databaseHelper.setSelectedDevice(snapshot.getValue(Device.class));
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(v.getContext(), PlantActivity.class);
                                    intent.putExtra("url", currentPlantViewModel.getImage());
                                    intent.putExtra("id", currentPlantViewModel.getId());
                                    v.getContext().startActivity(intent);
                                }
                                else {
                                    progressDialog.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
            }
        });

        holder.getCardView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to remove this item?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        allPlantViewModels.remove(holder.getPosition());
                        plantViewModels.remove(holder.getPosition());
                        databaseHelper.getUserRef().child(databaseHelper.getUserKey()).child("devices").setValue(allPlantViewModels);
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if(plantViewModels != null){
            return plantViewModels.size();
        }
        return 0;
    }

    public void setPlants(List<PlantViewModel> plantViewModels){
        this.plantViewModels = plantViewModels;
        this.allPlantViewModels = new ArrayList<>(plantViewModels);
    }

    @Override
    public Filter getFilter() {
        return plantFilter;
    }

    private final Filter plantFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<PlantViewModel> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(allPlantViewModels);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(PlantViewModel plantViewModel : allPlantViewModels){
                    if(plantViewModel.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(plantViewModel);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            plantViewModels.clear();
            plantViewModels.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
