package com.app.plantmonitoring.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.plantmonitoring.OpeningScreenActivity;
import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.devices.Device;
import com.app.plantmonitoring.database.users.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class AddPlantActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION = 1;
    private static final int GALLERY_REQUEST = 2;
    private static final int CAMERA_REQUEST = 3;

    // Layout elements
    private Toolbar toolbar;
    private ImageView plantImage;
    private TextInputLayout plantName;
    private TextInputLayout deviceId;
    private AppCompatButton addBtn;

    // Firebase
    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    // Loading animation
    private ProgressDialog progressDialog;

    private List<Device> allDevices;
    private List<String> allDevicesIds;
    private User user;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        statusBarColor();
        setConnections();
        initProgressDialog();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Add Plant");

        allDevices = databaseHelper.getAllDevices();
        allDevicesIds = databaseHelper.getAllDevicesIds();
        user = databaseHelper.getCurrentUser();

        // Set default image to send to database
        imageUri = (new Uri.Builder())
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(getResources().getResourcePackageName(R.drawable.plant_default))
                .appendPath(getResources().getResourceTypeName(R.drawable.plant_default))
                .appendPath(getResources().getResourceEntryName(R.drawable.plant_default))
                .build();

        plantImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values
                String name = plantName.getEditText().getText().toString();
                String id = deviceId.getEditText().getText().toString();
                // Check if id is correct
                if (!validateId(id)) {
                    return;
                }
                // Display loading animation
                progressDialog.show();
                // Add to database
                databaseHelper.addUserDevice(id, name);
                databaseHelper.getDevicesImageRef().child(id).putFile(imageUri)
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
                                        // Go back to home menu
                                        Toast.makeText(AddPlantActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(AddPlantActivity.this, NavigationActivity.class));
                                        finish();
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
                                Toast.makeText(AddPlantActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // Initialize layout connexions
    private void setConnections() {
        toolbar = findViewById(R.id.custom_toolbar);
        plantImage = findViewById(R.id.plant_img);
        plantName = findViewById(R.id.plant_name);
        deviceId = findViewById(R.id.device_id);
        addBtn = findViewById(R.id.add_btn);
    }

    // Initialize loading animation
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    // Validation
    private boolean validateId(String id) {
        if (id == null || id.length() == 0) {
            deviceId.setError("Please enter an Id!");
            return false;
        }
        for (String devId : allDevicesIds) {
            if (id.equals(devId) && user.getDevices().contains(id)) {
                deviceId.setError("This device is already added!");
                return false;
            }
            if (id.equals(devId)) {
                deviceId.setError(null);
                return true;
            }
        }
        deviceId.setError("This is not a valid device Id!");
        return false;
    }

    private void selectImage() {
        // Create and display choices for uploading image
        final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
        builder.setTitle("Add Photo!");
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
        if (!(ContextCompat.checkSelfPermission(AddPlantActivity.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(AddPlantActivity.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else if (ContextCompat.checkSelfPermission(AddPlantActivity.this,
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
            Toast.makeText(AddPlantActivity.this, "Camera could not be accessed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(AddPlantActivity.this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                Uri selectedImage = data.getData();
                cropImage(selectedImage);
            } else if (requestCode == CAMERA_REQUEST) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                plantImage.setImageBitmap(bitmap);
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),
                            bitmap, "val", null);
                    Uri selectedImage = Uri.parse(path);
                    cropImage(selectedImage);
                }catch (Exception e){
                    Toast.makeText(AddPlantActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    Uri croppedImage = result.getUri();
                    InputStream inputStream = getContentResolver().openInputStream(croppedImage);
                    plantImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                    imageUri = croppedImage;
                } catch (FileNotFoundException e) {
                    Toast.makeText(AddPlantActivity.this, "Something went wrong when cropping!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(AddPlantActivity.this, "Cropping failed!", Toast.LENGTH_SHORT).show();
        }

//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                try {
//                    Uri croppedImage = result.getUri();
//                    InputStream inputStream = getContentResolver().openInputStream(croppedImage);
//                    plantImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));
//                    imageUri = croppedImage;
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//            }
//        }
    }

    // Crop square from image
    private void cropImage(Uri image) {
        CropImage.activity(image)
                .setAspectRatio(1, 1)
                .start(this);
    }

    private void statusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
}