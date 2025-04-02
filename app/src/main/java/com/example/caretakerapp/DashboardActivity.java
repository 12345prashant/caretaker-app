package com.example.caretakerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    private MaterialButton logoutButton;
    private CardView addPatientCard, addMedicineCard, sendMessageCard, viewPatientCard;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        logoutButton = findViewById(R.id.buttonLogout);
        addPatientCard = findViewById(R.id.cardAddPatient);
        addMedicineCard = findViewById(R.id.cardAddMedicine);
        sendMessageCard = findViewById(R.id.cardSendMessage);
        viewPatientCard = findViewById(R.id.cardViewPatient);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Apply animations to cards
        addPatientCard.startAnimation(fadeIn);
        addMedicineCard.startAnimation(fadeIn);
        sendMessageCard.startAnimation(fadeIn);
        viewPatientCard.startAnimation(fadeIn);
        logoutButton.startAnimation(fadeIn);

        // Set click listeners with animations
        addPatientCard.setOnClickListener(v -> {
            v.startAnimation(bounce);
            v.postDelayed(this::addPatient, 100);
        });

        addMedicineCard.setOnClickListener(v -> {
            v.startAnimation(bounce);
            v.postDelayed(this::navigateToAddMedicineReminder, 100);
        });

        sendMessageCard.setOnClickListener(v -> {
            v.startAnimation(bounce);
            v.postDelayed(this::moveToSendMessageActivity, 100);
        });

        viewPatientCard.setOnClickListener(v -> {
            v.startAnimation(bounce);
            v.postDelayed(this::viewPatientActivity, 100);
        });

        logoutButton.setOnClickListener(v -> {
            v.startAnimation(bounce);
            v.postDelayed(this::logoutUser, 100);
        });
    }

    // Function to handle user logout
    private void logoutUser() {
        // Clear the cached email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("user_email").apply();

        // Sign out from Firebase
        mAuth.signOut();

        // Stop the emergency service to remove old data
        stopService(new Intent(this, EmergencyAlertService.class));
        stopService(new Intent(this, MessageNotificationService.class));

        // Redirect to MainActivity (Login screen)
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void moveToSendMessageActivity() {
        Intent intent = new Intent(DashboardActivity.this, SendMessageActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void viewPatientActivity() {
        Intent intent = new Intent(DashboardActivity.this, ViewPatientActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToAddMedicineReminder() {
        Intent intent = new Intent(DashboardActivity.this, AddMedicineReminderActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void addPatient() {
        Intent intent = new Intent(DashboardActivity.this, AddPatientActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}