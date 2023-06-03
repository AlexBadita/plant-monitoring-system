package com.app.plantmonitoring.user.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class ProfileFragment extends Fragment {

    // Layout elements
    private View view;
    private TextInputLayout name;
    private TextInputLayout email;
    private TextInputLayout password;
    private AppCompatButton saveBtn;

    // Firebase
    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    // Loading animation
    private ProgressDialog progressDialog;

    private boolean validPassword = true;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Profile");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile, container, false);

        setConnections();
        initProgressDialog();
        setProfile();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validPassword){
                    return;
                }
                updateUser();
            }
        });

        return view;
    }

    // Initialize layout connexions
    private void setConnections(){
        name = view.findViewById(R.id.profile_name);
        email = view.findViewById(R.id.profile_email);
        password = view.findViewById(R.id.profile_password);
        saveBtn = view.findViewById(R.id.profile_save_btn);
    }

    // Initialize loading animation
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Saving...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    // Set current profile values
    private void setProfile(){
        user = databaseHelper.getCurrentUser();
        name.getEditText().setText(user.getName());
        email.getEditText().setText(user.getEmail());
        password.getEditText().setText(user.getPassword());

        password.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    // Validation
    private void validatePassword(String pass) {
        Pattern UPPERCASE = Pattern.compile("[A-Z]");
        Pattern LOWERCASE = Pattern.compile("[a-z]");
        Pattern DIGIT = Pattern.compile("[0-9]");

        if (TextUtils.isEmpty(pass)) {
            password.setError("Password is required!");
            validPassword = false;
        } else if (pass.length() < 6) {
            password.setError("Password must have at least 6 characters!");
            validPassword = false;
        } else if (!LOWERCASE.matcher(pass).find()) {
            password.setError("Password must have at least a lowercase!");
            validPassword = false;
        } else if (!UPPERCASE.matcher(pass).find()) {
            password.setError("Password must have at least an uppercase!");
            validPassword = false;
        } else if (!DIGIT.matcher(pass).find()) {
            password.setError("Password must have at least a number!");
            validPassword = false;
        } else {
            password.setError(null);
            validPassword = true;
        }
    }

    private void updateUser(){
        progressDialog.show();

        String newName = name.getEditText().getText().toString();
        String newPassword = password.getEditText().getText().toString();

        if(!newPassword.equals(user.getPassword())){
            databaseHelper.getUser().updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        user.setPassword(newPassword);
                        databaseHelper.getUserRef().child(databaseHelper.getUserKey()).child("password").setValue(newPassword);
                    }
                    else {
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        Toast.makeText(getContext(), "Saving failed1", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if(progressDialog.isShowing()){
            if(!newName.equals(user.getName())){
                user.setName(newName);
                databaseHelper.getUserRef().child(databaseHelper.getUserKey()).child("name").setValue(newName);
            }
            progressDialog.dismiss();
        }

        if(!user.equals(databaseHelper.getCurrentUser())){
            databaseHelper.setCurrentUser(user);
        }
    }
}