package com.example.caretakerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EmergencyActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        mediaPlayer = MediaPlayer.create(this, R.raw.emergency_sound);
        mediaPlayer.start();

        TextView alertText = findViewById(R.id.alertText);
        alertText.setText("Your patient needs urgent help!");

        findViewById(R.id.dismissButton).setOnClickListener(v -> {
            stopEmergencyAlert();

            // Retrieve patient email from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String patientEmail = sharedPreferences.getString("patient_email", "");
            Log.d("Login", patientEmail);
            patientEmail = patientEmail.replace(".", "_");

            if (!patientEmail.isEmpty()) {
                // Delete the entry from Firebase
                DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference("emergency_alerts").child(patientEmail);
                emergencyRef.removeValue();
            }

//            navigateToDashboard();
//            super.onDestroy();
            finishAndRemoveTask();
        });

    }

    private void stopEmergencyAlert() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // This will finish the current activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopEmergencyAlert(); // Ensure media player is released if activity is destroyed
    }
}