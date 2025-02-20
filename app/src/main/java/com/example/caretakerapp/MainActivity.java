package com.example.caretakerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("caretakers");

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();  // This will remove all the data stored in SharedPreferences
//        editor.apply();
//
        String cachedEmail = sharedPreferences.getString("user_email", null);
        String patientEmail = sharedPreferences.getString("patient_email", null);

        // Auto-login if email is cached
        if (cachedEmail != null) {
//            checkCaretakerPatients(cachedEmail);
            if (patientEmail != null) {
                // Redirect to DashboardActivity if both user and patient are available
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            } else {
                // Redirect to AddPatientActivity if user is logged in but patient is not set
                startActivity(new Intent(MainActivity.this, AddPatientActivity.class));
            }
            finish(); // Close MainActivity after redirection
        }

        // Bind UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(email, password);
            }
        });

        // Register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Register.class));
                finish();
            }
        });
    }

    // Function to handle user login
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Cache email
                            sharedPreferences.edit().putString("user_email", email).apply();
                            checkCaretakerPatients(email);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Check if caretaker has patients, redirect accordingly
    private void checkCaretakerPatients(String email) {
        String caretakerKey = email.replace(".", "_");

        databaseReference.child(caretakerKey).child("patients").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, AddPatientActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
