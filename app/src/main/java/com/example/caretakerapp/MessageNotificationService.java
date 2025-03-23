package com.example.caretakerapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MessageNotificationService extends Service {
    private DatabaseReference databaseReference;
    private String caretakerEmail;
    private static final String CHANNEL_ID = "message_alerts_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Create Notification Channel
        createNotificationChannel();
        startForeground(2, createNotification());

        // Retrieve caretaker email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        caretakerEmail = sharedPreferences.getString("user_email", null);

        if (caretakerEmail == null) {
            Log.e("MessageNotificationService", "Caretaker email not found!");
            stopSelf();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("messages");
        listenForMessages();
    }

    private void listenForMessages() {
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

                // Listen for new messages from linked patients
                for (String patientEmail : linkedPatients) {
                    String chatKey = caretakerKey + "_" + patientEmail;

                    DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatKey);
                    messageRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String sender = snapshot.child("sender").getValue(String.class);
                            String text = snapshot.child("text").getValue(String.class);

                            if (sender != null && sender.equals("patient") && text != null) {
                                showMessageNotification(patientEmail, text);
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("MessageNotificationService", "Failed to read messages", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MessageNotificationService", "Failed to fetch patients", error.toException());
            }
        });
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Message Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies the caretaker about new messages from patients");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Message Service Running")
                .setContentText("Listening for new messages...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void showMessageNotification(String patientId, String messageText) {
        Intent intent = new Intent(this, SendMessageActivity.class);
//        intent.putExtra("patientId", patientId);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Patient sent a message")
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, notificationBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
