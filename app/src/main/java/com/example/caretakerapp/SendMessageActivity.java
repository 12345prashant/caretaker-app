//    package com.example.caretakerapp;
//
//    import androidx.appcompat.app.AppCompatActivity;
//    import androidx.appcompat.widget.Toolbar;
//
//    import android.content.Context;
//    import android.content.SharedPreferences;
//    import android.os.Bundle;
//    import android.view.View;
//    import android.widget.ArrayAdapter;
//    import android.widget.Button;
//    import android.widget.EditText;
//    import android.widget.ListView;
//    import android.widget.Toast;
//
//    import com.google.firebase.auth.FirebaseAuth;
//    import com.google.firebase.auth.FirebaseUser;
//    import com.google.firebase.database.DataSnapshot;
//    import com.google.firebase.database.DatabaseError;
//    import com.google.firebase.database.DatabaseReference;
//    import com.google.firebase.database.FirebaseDatabase;
//    import com.google.firebase.database.ValueEventListener;
//
//    import java.util.ArrayList;
//    import java.util.HashMap;
//    import java.util.Map;
//
//    public class SendMessageActivity extends AppCompatActivity {
//
//        // UI Elements
//        private EditText messageEditText;
//        private Button sendButton;
//        private ListView messageListView;
//
//        // Data
//        private ArrayAdapter<String> messageAdapter;
//        private ArrayList<String> messageList;
//
//        // Firebase
//        private FirebaseAuth mAuth;
//        private DatabaseReference messagesDatabase;
//        private String patientEmail, caretakerEmail;
//        private String chatRoomId;
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_send_message);
//
//            // Initialize Toolbar
//            Toolbar toolbar = findViewById(R.id.toolbar);
//            setSupportActionBar(toolbar);
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                getSupportActionBar().setTitle("Messages");
//            }
//
//            // Initialize UI Elements
//            messageEditText = findViewById(R.id.messageEditText);
//            sendButton = findViewById(R.id.sendButton);
//            messageListView = findViewById(R.id.messageListView);
//
//            // Initialize Firebase Auth
//            mAuth = FirebaseAuth.getInstance();
//            FirebaseUser currentUser = mAuth.getCurrentUser();
//
//            // Get user data from SharedPreferences
//            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
//            caretakerEmail = sharedPreferences.getString("user_email", null);
//            patientEmail = sharedPreferences.getString("patient_email", null);
//
//            // Validate user data
//            if (currentUser == null || patientEmail == null || caretakerEmail == null) {
//                Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
//                finish();
//                return;
//            }
//
//            // Create chat room ID
//            String patientKey = patientEmail.replace(".", "_");
//            String caretakerKey = caretakerEmail.replace(".", "_");
//            chatRoomId = caretakerKey + "_" + patientKey;
//            messagesDatabase = FirebaseDatabase.getInstance().getReference("messages").child(chatRoomId);
//
//            // Initialize message list
//            messageList = new ArrayList<>();
//            messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
//            messageListView.setAdapter(messageAdapter);
//
//            // Load existing messages
//            loadMessages();
//
//            // Set send button click listener
//            sendButton.setOnClickListener(v -> sendMessage());
//        }
//
//        private void sendMessage() {
//            String messageText = messageEditText.getText().toString().trim();
//            if (messageText.isEmpty()) {
//                return;
//            }
//
//            // Create message object
//            Map<String, Object> messageData = new HashMap<>();
//            messageData.put("sender", "caretaker");
//            messageData.put("receiver", "patient");
//            messageData.put("text", messageText);
//            messageData.put("timestamp", System.currentTimeMillis());
//
//            // Save to Firebase
//            messagesDatabase.push().setValue(messageData).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    messageEditText.setText("");  // Clear input field
//                } else {
//                    Toast.makeText(SendMessageActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//        private void loadMessages() {
//            messagesDatabase.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    messageList.clear();
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        String sender = snapshot.child("sender").getValue(String.class);
//                        String text = snapshot.child("text").getValue(String.class);
//
//                        if (sender != null && text != null) {
//                            messageList.add(sender + ": " + text);
//                        }
//                    }
//                    messageAdapter.notifyDataSetChanged();
//
//                    // Scroll to bottom after loading messages
//                    messageListView.post(() -> {
//                        messageListView.setSelection(messageAdapter.getCount() - 1);
//                    });
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    Toast.makeText(SendMessageActivity.this,
//                            "Failed to load messages: " + databaseError.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//        @Override
//        public boolean onSupportNavigateUp() {
//            onBackPressed();
//            return true;
//        }
//    }
//


package com.example.caretakerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendMessageActivity extends AppCompatActivity {

    // UI Elements
    private EditText messageEditText;
    private Button sendButton;
    private RecyclerView messageRecyclerView;

    // Adapter
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    // Firebase
    private DatabaseReference messagesDatabase;
    private String currentUserEmail;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        messageRecyclerView = findViewById(R.id.messageListView);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setAdapter(messageAdapter);

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserEmail = currentUser.getEmail();

        // Get chat participants
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String caretakerEmail = sharedPreferences.getString("user_email", "");
        String patientEmail = sharedPreferences.getString("patient_email", "");
        Log.d("Login", patientEmail);
        // Create chat room ID
        chatRoomId = generateChatRoomId(caretakerEmail, patientEmail);
        messagesDatabase = FirebaseDatabase.getInstance().getReference("messages").child(chatRoomId);

        // Load messages
        loadMessages();

        // Send button click listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private String generateChatRoomId(String email1, String email2) {
        String email1Key = email1.replace(".", "_");
        String email2Key = email2.replace(".", "_");
        return email1Key.compareTo(email2Key) < 0 ?
                email1Key + "_" + email2Key :
                email2Key + "_" + email1Key;
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Create message data
        Map<String, Object> messageData = new HashMap<>();
        String messageId = messagesDatabase.push().getKey();

        messageData.put("id", messageId);
        messageData.put("sender", currentUserEmail);
        messageData.put("text", messageText);
        messageData.put("timestamp", System.currentTimeMillis());
        messageData.put("status", "sent");

        // Save to Firebase
        messagesDatabase.child(messageId).setValue(messageData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageEditText.setText("");
                    } else {
                        Toast.makeText(SendMessageActivity.this,
                                "Failed to send message",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        messagesDatabase.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            messageList.add(message);
                        }
                    } catch (Exception e) {
                        Log.e("Firebase", "Error parsing message", e);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                scrollToBottom();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SendMessageActivity.this,
                        "Failed to load messages",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        }
    }
}