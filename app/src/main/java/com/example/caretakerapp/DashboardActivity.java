package com.example.caretakerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    private Button logoutButton, addPatient;
    private ImageButton addMedicineReminderButton, sendMessageButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        logoutButton = findViewById(R.id.button2);
        addPatient = findViewById(R.id.button4);
        addMedicineReminderButton = findViewById(R.id.imageButton2);
        sendMessageButton = findViewById(R.id.imageButton);

        // Logout button click listener
        logoutButton.setOnClickListener(v -> logoutUser());

        // Add Medicine Reminder button click listener
        addMedicineReminderButton.setOnClickListener(v -> navigateToAddMedicineReminder());
        addPatient.setOnClickListener((v -> addPatient()));
        sendMessageButton.setOnClickListener((v -> moveToSendMessageActivity()));

    }

    // Function to handle user logout
    private void logoutUser() {
        // Clear the cached email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("user_email").apply();

        // Sign out from Firebase
        mAuth.signOut();

        // Redirect to MainActivity (Login screen)
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void  moveToSendMessageActivity(){
        Intent intent = new Intent(DashboardActivity.this, SendMessageActivity.class);
        startActivity(intent);
        finish();
    }


    // Navigate to AddMedicineReminderActivity
    private void navigateToAddMedicineReminder() {
        Intent intent = new Intent(DashboardActivity.this, AddMedicineReminderActivity.class);
        startActivity(intent);
    }

    // Function to check if the caretaker has any existing reminders
    private void checkMedicineReminders() {
        // Code to fetch reminders from Firebase and update the UI accordingly
    }

    public void addPatient(){
        Intent intent = new Intent(DashboardActivity.this, AddPatientActivity.class);
        startActivity(intent);
        finish();
    }

}
