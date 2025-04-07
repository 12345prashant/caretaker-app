package com.example.caretakerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewPatientActivity extends AppCompatActivity {
    private static final String TAG = "CaretakerApp";
//    private static final String PATIENT_ID = "patient_unique_id"; // Use same ID as patient app

    private ImageView videoView;
    private Button startViewingButton;
    private Button stopViewingButton;
    private TextView statusTextView;
    private DatabaseReference firebaseRef;
    private ValueEventListener frameListener;
    private Handler handler;
    private boolean isViewing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient);

        videoView = findViewById(R.id.video_view);
        startViewingButton = findViewById(R.id.start_viewing_button);
        stopViewingButton = findViewById(R.id.stop_viewing_button);
        statusTextView = findViewById(R.id.status_text);

        // Initialize Firebase reference
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("patient_email", null);


        if (userEmail != null) {
            // Format email for Firebase path (replace dots with commas)
            String patientId = userEmail.replace(".", ",");
            Log.d("Frame of ", userEmail);
            // Initialize Firebase reference using the email as patient ID
            firebaseRef = FirebaseDatabase.getInstance().getReference()
                    .child("patient_frames").child(patientId);
        } else {
            // Handle the case where email is not found
            updateStatus("User email not found. Please log in again.");
            // Consider redirecting to login screen
        }
//        firebaseRef = FirebaseDatabase.getInstance().getReference()
//                .child("patients").child(PATIENT_ID);

        handler = new Handler(getMainLooper());

        setupButtons();
        checkStreamingStatus();
    }

    private void setupButtons() {
        startViewingButton.setOnClickListener(v -> startViewing());

        stopViewingButton.setOnClickListener(v -> stopViewing());
        stopViewingButton.setEnabled(false);
    }

    private void checkStreamingStatus() {
        firebaseRef.child("streaming_active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                if (isActive != null && isActive) {
                    updateStatus("Patient camera is active. Ready to view.");
                    startViewingButton.setEnabled(true);
                } else {
                    updateStatus("Patient camera is offline.");
                    startViewingButton.setEnabled(false);
                    stopViewing();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateStatus("Error checking streaming status: " + error.getMessage());
            }
        });
    }

    private void startViewing() {
        if (isViewing) return;

        isViewing = true;
        startViewingButton.setEnabled(false);
        stopViewingButton.setEnabled(true);
        updateStatus("Connecting to patient camera...");

        // Start listening for frames
        frameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()) {
                        String base64Image = snapshot.child("image").getValue(String.class);
                        if (base64Image != null) {
                            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                            if (bitmap != null) {
                                handler.post(() -> {
                                    videoView.setImageBitmap(bitmap);
                                    updateStatus("Viewing patient camera");
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing frame: " + e.getMessage());
                    updateStatus("Error displaying frame");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateStatus("Connection error: " + error.getMessage());
                stopViewing();
            }
        };

        firebaseRef.child("current_frame").addValueEventListener(frameListener);
    }

    private void stopViewing() {
//        if (!isViewing) return;
//
//        isViewing = false;
        startViewingButton.setEnabled(true);
        stopViewingButton.setEnabled(false);

        if (frameListener != null) {
            firebaseRef.child("current_frame").removeEventListener(frameListener);
            frameListener = null;
        }
        Intent intent = new Intent(ViewPatientActivity.this, DashboardActivity.class);
        startActivity(intent);
        updateStatus("Stopped viewing");
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> {
            statusTextView.setText(message);
            Log.d(TAG, message);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopViewing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopViewing();
    }
}
