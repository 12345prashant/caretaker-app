package com.example.caretakerapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPatientActivity extends AppCompatActivity {

    private TextView caretakerEmailText;
    private EditText patientEmailEditText;
    private Button addPatientButton, logoutButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String caretakerEmail;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize UI elements
        caretakerEmailText = findViewById(R.id.caretakerEmailText);
        patientEmailEditText = findViewById(R.id.patientEmailEditText);
        addPatientButton = findViewById(R.id.addPatientButton);
        logoutButton = findViewById(R.id.button3);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Get current caretaker email
        if (currentUser != null) {
            caretakerEmail = currentUser.getEmail();
            caretakerEmailText.setText("Caretaker: " + caretakerEmail);
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("caretakers");

        // Add patient when button is clicked
        addPatientButton.setOnClickListener(v -> addPatient());

        // Logout button click listener
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void addPatient() {
        String patientEmail = patientEmailEditText.getText().toString().trim();

        if (patientEmail.isEmpty()) {
            Toast.makeText(this, "Please enter a patient email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert emails to Firebase-friendly format (replace "." with "_")
        String caretakerKey = caretakerEmail.replace(".", "_");
        String patientKey = patientEmail.replace(".", "_");

        // Check if the patient already exists in Firebase
        databaseReference.child(caretakerKey).child("patients").child(patientKey).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            // Patient already exists, just redirect to dashboard
                            Toast.makeText(AddPatientActivity.this, "Patient already exists, redirecting...", Toast.LENGTH_SHORT).show();
                            sharedPreferences.edit().putString("patient_email", patientEmail).apply();
                            startActivity(new Intent(AddPatientActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // Patient doesn't exist, add new patient
                            saveNewPatient(patientKey, patientEmail);
                        }
                    } else {
                        Toast.makeText(AddPatientActivity.this, "Error checking patient", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveNewPatient(String patientKey, String patientEmail) {
        databaseReference.child(caretakerEmail.replace(".", "_")).child("patients").child(patientKey).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddPatientActivity.this, "Patient added successfully!", Toast.LENGTH_SHORT).show();
                        sharedPreferences.edit().putString("patient_email", patientEmail).apply();
                        startActivity(new Intent(AddPatientActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(AddPatientActivity.this, "Failed to add patient", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logoutUser() {
        // Clear SharedPreferences (remove cached email and patient info)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user_email"); // Remove user email
        editor.remove("patient_email"); // Remove patient email
        editor.apply();  // Save changes

        // Sign out from Firebase
        mAuth.signOut();

        // Redirect to MainActivity (login screen)
        Intent intent = new Intent(AddPatientActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // Close AddPatientActivity
    }
}
