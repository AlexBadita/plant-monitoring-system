package com.app.plantmonitoring.user.select_device.fragments_item;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.user.AddPlantActivity;
import com.app.plantmonitoring.user.NavigationActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ValuesFragment extends Fragment {

    private static final int CAMERA_PERMISSION = 1;
    private static final int GALLERY_REQUEST = 2;
    private static final int CAMERA_REQUEST = 3;

    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    private View view;

    private ImageView plantImg;
    private TextView temperature;
    private TextView humidity;
    private TextView light;
    private TextView moisture;

    private Bundle bundle;
    private String imageUrl;
    private String id;

    private ProgressDialog progressDialog;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_values, container, false);

        setConnections();
        initProgressDialog();

        bundle = this.getArguments();
        imageUrl = bundle.getString("url");
        id = bundle.getString("id");
        databaseHelper.setCurrentDeviceId(id);

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.plant_default)
                .into(plantImg);

        readFromFirebase();

        plantImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        return view;
    }

    private void setConnections() {
        plantImg = view.findViewById(R.id.selected_plant_img);
        temperature = view.findViewById(R.id.temperature);
        humidity = view.findViewById(R.id.humidity);
        light = view.findViewById(R.id.light);
        moisture = view.findViewById(R.id.soil_moisture);
    }

    // Initialize loading animation
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("Uploading...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void readFromFirebase() {
        databaseHelper.getDeviceRef().child(id).child("Measurements").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> values = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            values.add(data.getValue(String.class));
                        }
                        humidity.setText(values.get(0) + "%");
                        light.setText(values.get(1) + "%");
                        moisture.setText(values.get(2) + "%");
                        temperature.setText(values.get(3) + " C");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(view.getContext(), "Retrieving data failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void selectImage() {
        // Create and display choices for uploading image
        final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Change Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Take Photo")) {
                    cameraPermission();
                } else if (options[which].equals("Choose From Gallery")) {
                    openGallery();
                } else if (options[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    // Get image from gallery
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    // Camera permission
    private void cameraPermission() {
        if (!(ContextCompat.checkSelfPermission(view.getContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions((Activity) view.getContext(),
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else if (ContextCompat.checkSelfPermission(view.getContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }

    // Get image from camera
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, CAMERA_REQUEST);
        } catch (Exception e) {
            Toast.makeText(view.getContext(), "Camera could not be accessed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(view.getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                Uri selectedImage = data.getData();
                cropImage(selectedImage);
            } else if (requestCode == CAMERA_REQUEST) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
//                plantImg.setImageBitmap(bitmap);
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(view.getContext().getContentResolver(),
                            bitmap, "val", null);
                    Uri selectedImage = Uri.parse(path);
                    cropImage(selectedImage);
                } catch (Exception e) {
                    Toast.makeText(view.getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    Uri croppedImage = result.getUri();
                    InputStream inputStream = view.getContext().getContentResolver().openInputStream(croppedImage);
                    plantImg.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                    saveImageInFirebase(croppedImage);
                } catch (FileNotFoundException e) {
                    Toast.makeText(view.getContext(), "Something went wrong when cropping!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(view.getContext(), "Cropping failed!", Toast.LENGTH_SHORT).show();
        }
    }

    // Crop square from image
    private void cropImage(Uri image) {
        CropImage.activity(image)
                .setAspectRatio(1, 1)
                .start(view.getContext(), this);
    }

    private void saveImageInFirebase(Uri uri) {
        progressDialog.show();
        databaseHelper.getDevicesImageRef().child(id).putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Add image url to database
                                databaseHelper.getDeviceRef().child(id).child("url").setValue(uri.toString());
                                // Stop loading animation
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(view.getContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Stop loading animation
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        // Upload failed
                        Toast.makeText(view.getContext(), "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}