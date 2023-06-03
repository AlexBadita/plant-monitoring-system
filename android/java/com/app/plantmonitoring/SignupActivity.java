package com.app.plantmonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckedTextView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    // Layout elements
    private AppCompatCheckedTextView backBtn;
    private TextInputLayout nameInput;
    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;
    private TextInputLayout passwordConfirmInput;
    private AppCompatButton registerBtn;
    private TextView login;

    // Firebase
    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    // Created user
    private User user;

    // Input values
    private String name;
    private String email;
    private String password;

    private boolean[] validData = new boolean[]{false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        statusBarColor();
        setConnections();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, OpeningScreenActivity.class));
                finish();
            }
        });

        // Validations
        emailInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordConfirmInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If data is not valid, don't proceed further
                if (!(validData[0] && validData[1] && validData[2])) {
                    return;
                }

                // Get input values
                name = nameInput.getEditText().getText().toString();

                // Create the user
                user = new User(name, email, password);
                // Register user in database
                createUser(email, password);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    // Initialize layout connexions
    private void setConnections() {
        backBtn = findViewById(R.id.back_btn_signup);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        passwordConfirmInput = findViewById(R.id.password_repeat_input);
        registerBtn = findViewById(R.id.register_btn);
        login = findViewById(R.id.login);
    }

    // Validations
    private void validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required!");
            validData[0] = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Invalid email address!");
            validData[0] = false;
        } else {
            emailInput.setError(null);
            validData[0] = true;
        }

        this.email = email;
    }

    private void validatePassword(String password) {
        Pattern UPPERCASE = Pattern.compile("[A-Z]");
        Pattern LOWERCASE = Pattern.compile("[a-z]");
        Pattern DIGIT = Pattern.compile("[0-9]");

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required!");
            validData[1] = false;
        } else if (password.length() < 6) {
            passwordInput.setError("Password must have at least 6 characters!");
            validData[1] = false;
        } else if (!LOWERCASE.matcher(password).find()) {
            passwordInput.setError("Password must have at least a lowercase!");
            validData[1] = false;
        } else if (!UPPERCASE.matcher(password).find()) {
            passwordInput.setError("Password must have at least an uppercase!");
            validData[1] = false;
        } else if (!DIGIT.matcher(password).find()) {
            passwordInput.setError("Password must have at least a number!");
            validData[1] = false;
        } else {
            passwordInput.setError(null);
            validData[1] = true;
        }

        this.password = password;
    }

    private void validateConfirmPassword(String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            passwordConfirmInput.setError("Confirm password is required!");
            validData[2] = false;
        }
        if (!password.equals(confirmPassword)) {
            passwordConfirmInput.setError("Not matching password field!");
            validData[2] = false;
        } else {
            passwordConfirmInput.setError(null);
            validData[2] = true;
        }
    }

    private void createUser(String email, String password) {
        databaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            user.setId(databaseHelper.getAuth().getCurrentUser().getUid());
                            databaseHelper.getUserRef().child(user.getId()).setValue(user);
                            String keyId = user.getId();
                            Log.println(Log.INFO, "User Id", keyId);
                            // Go to login
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
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