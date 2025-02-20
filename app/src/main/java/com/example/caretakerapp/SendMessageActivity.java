package com.example.caretakerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.database.DatabaseError;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

//import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SendMessageActivity extends AppCompatActivity {
    private EditText messageEditText;
    private Button sendButton;
    private ListView messageListView;
    private ArrayAdapter<String> messageAdapter;
    private ArrayList<String> messageList;

    private FirebaseAuth mAuth;
    private DatabaseReference messagesDatabase;
    private String patientEmail, caretakerEmail;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize UI elements
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        messageListView = findViewById(R.id.messageListView);

        // Initialize SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        caretakerEmail = sharedPreferences.getString("user_email", null);
        patientEmail = sharedPreferences.getString("patient_email", null);  // Caretaker's email should be stored during patient login

        if (currentUser == null || patientEmail == null || caretakerEmail == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Format emails to Firebase-friendly keys
        String patientKey = patientEmail.replace(".", "_");
        String caretakerKey = caretakerEmail.replace(".", "_");

        // Generate unique chat room ID for caretaker-patient
        chatRoomId = caretakerKey + "_" + patientKey;
        messagesDatabase = FirebaseDatabase.getInstance().getReference("messages").child(chatRoomId);

        // Initialize message list
        messageList = new ArrayList<>();
        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        messageListView.setAdapter(messageAdapter);

        // Load existing messages
        loadMessages();

        // Send button click listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Create message object
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("sender", "caretaker");
        messageData.put("receiver", "patient");
        messageData.put("text", messageText);
        messageData.put("timestamp", System.currentTimeMillis());

        // Save to Firebase
        messagesDatabase.push().setValue(messageData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageEditText.setText("");  // Clear input field
            } else {
                Toast.makeText(SendMessageActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        messagesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String sender = snapshot.child("sender").getValue(String.class);
                    String text = snapshot.child("text").getValue(String.class);

                    if (sender != null && text != null) {
                        messageList.add(sender + ": " + text);
                    }
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SendMessageActivity.this, "Failed to load messages: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
