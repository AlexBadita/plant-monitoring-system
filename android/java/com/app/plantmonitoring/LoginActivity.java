package com.app.plantmonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckedTextView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.users.User;
import com.app.plantmonitoring.user.NavigationActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // Layout elements
    private AppCompatCheckedTextView backBtn;
    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;
    private TextView resetPassword;
    private TextView register;
    private AppCompatButton confirmBtn;

    // Firebase
    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    // Loading animation
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        statusBarColor();
        setConnections();
        initProgressDialog();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, OpeningScreenActivity.class));
                finish();
            }
        });

        /*
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ));
                finish();
            }
        });
        */

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailInput.setError(null);
                passwordInput.setError(null);

                // Start loading animation
                progressDialog.show();

                // Input values
                String email = emailInput.getEditText().getText().toString();
                String password = passwordInput.getEditText().getText().toString();

                // Login user
                loginUser(email, password);
            }
        });

    }

    // Initialize layout connexions
    private void setConnections() {
        backBtn = findViewById(R.id.back_btn_login);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        resetPassword = findViewById(R.id.reset_password);
        register = findViewById(R.id.register);
        confirmBtn = findViewById(R.id.confirm_btn);
    }

    // Initialize loading animation
    private void initProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void loginUser(String email, String password) {
        databaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    // User found in database
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Get user by id
                        String userId = databaseHelper.getUser().getUid();
                        Query query = databaseHelper.getUserRef().orderByChild("id").equalTo(userId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    // Add user in DatabaseHelper
                                    for(DataSnapshot data : snapshot.getChildren()){
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
                                    // Stop Loading animation
                                    if(progressDialog.isShowing()){
                                        progressDialog.dismiss();
                                    }
                                    // Go to home page
                                    startActivity(new Intent(LoginActivity.this, NavigationActivity.class));
                                    finish();
                                }
                                else {
                                    // Stop Loading animation
                                    if(progressDialog.isShowing()){
                                        progressDialog.dismiss();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Stop Loading animation
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        // Display error
                        emailInput.setError("Invalid");
                        passwordInput.setError("Invalid");
                        Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void statusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
}