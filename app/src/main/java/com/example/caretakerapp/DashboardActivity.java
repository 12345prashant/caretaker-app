package com.example.caretakerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DashboardActivity extends AppCompatActivity {

    private MaterialButton logoutButton;
    private CardView addPatientCard, addMedicineCard, sendMessageCard, viewPatientCard;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference videocallsRef;
    private String pushedKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        videocallsRef = database.getReference("videocalls");

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
            v.postDelayed(() -> {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String patientEmail = sharedPreferences.getString("patient_email", "");

                if (!patientEmail.isEmpty()) {
                    // Push to Firebase and store the key
                    pushedKey = videocallsRef.push().getKey();
                    videocallsRef.child(pushedKey).setValue(patientEmail)
                            .addOnSuccessListener(aVoid -> {
                                // Pass the key to ViewPatientActivity
                                Intent intent = new Intent(DashboardActivity.this, ViewPatientActivity.class);
                                intent.putExtra("pushedKey", pushedKey);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to start call", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "No patient email found", Toast.LENGTH_SHORT).show();
                }
            }, 100);
        });

        logoutButton.setOnClickListener(v -> {
            v.startAnimation(bounce);
            logoutUser();

        });
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // When returning from ViewPatientActivity, delete the entry
//        if (pushedKey != null) {
//            videocallsRef.child(pushedKey).removeValue()
//                    .addOnSuccessListener(aVoid -> {
//                        // Successfully deleted
//                        pushedKey = null; // Reset key
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//        }
//    }

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