package com.example.caretakerapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmergencyAlertService extends Service {
    private DatabaseReference databaseReference;
    private MediaPlayer mediaPlayer;
    private String caretakerEmail;
    private static final String CHANNEL_ID = "emergency_alerts_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Ensure Foreground Service is properly started
        createNotificationChannel();
        startForeground(1, createNotification());

        mediaPlayer = MediaPlayer.create(this, R.raw.emergency_sound); // Place emergency_sound.mp3 in res/raw

        // Retrieve caretaker email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        caretakerEmail = sharedPreferences.getString("user_email", null);

        if (caretakerEmail == null) {
            Log.e("EmergencyAlertService", "Caretaker email not found!");
            stopSelf();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("emergency_alerts");
        listenForEmergencyAlerts();
    }

    private void listenForEmergencyAlerts() {
        DatabaseReference caretakerRef = FirebaseDatabase.getInstance().getReference("caretakers");
        String caretakerKey = caretakerEmail.replace(".", "_");

        caretakerRef.child(caretakerKey).child("patients").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot patientSnapshot) {
                if (!patientSnapshot.exists()) return;

                List<String> linkedPatients = new ArrayList<>();
                for (DataSnapshot patient : patientSnapshot.getChildren()) {
                    linkedPatients.add(patient.getKey());
                }

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot alertSnapshot) {
                        for (DataSnapshot alert : alertSnapshot.getChildren()) {
                            String patientId = alert.child("patientId").getValue(String.class);
                            if (patientId != null && linkedPatients.contains(patientId)) {
                                showEmergencyNotification();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("EmergencyAlertService", "Failed to read emergency alert", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EmergencyAlertService", "Failed to fetch caretaker's linked patients", error.toException());
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies the caretaker about emergency alerts");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle("Emergency Service Running")
                .setContentText("Listening for emergency alerts...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void showEmergencyNotification() {
        Intent fullScreenIntent = new Intent(this, EmergencyActivity.class);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "emergency_channel",
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Urgent emergency alerts");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "emergency_channel")
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle("Emergency Alert!")
                .setContentText("The patient has triggered an emergency.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true) // Ensures the activity opens in full screen
                .setAutoCancel(true)
                .setContentIntent(fullScreenPendingIntent); // Ensures clicking the notification opens EmergencyActivity

        notificationManager.notify(1, notificationBuilder.build());

        // **Fallback Mechanism for Android 12+ Restrictions**
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(fullScreenIntent);
        }

        if (mediaPlayer != null) {
//            mediaPlayer.start();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
