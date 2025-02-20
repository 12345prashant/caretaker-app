package com.example.caretakerapp;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.os.PowerManager;
import android.provider.Settings;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class AddMedicineReminderActivity extends AppCompatActivity {

    private EditText medicineNameEditText, reminderTimeEditText;
    private Button saveReminderButton;
    private ListView reminderListView;
    private ArrayAdapter<String> reminderAdapter;
    private ArrayList<String> reminderList;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String caretakerEmail, patientKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine_reminder);

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Request exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("caretakers");

        // Bind UI elements
        medicineNameEditText = findViewById(R.id.medicineNameEditText);
        reminderTimeEditText = findViewById(R.id.reminderTimeEditText);
        saveReminderButton = findViewById(R.id.saveReminderButton);
        reminderListView = findViewById(R.id.reminderListView);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        // Get caretaker email and patient email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String patientEmail = sharedPreferences.getString("patient_email", null);

        if (mAuth.getCurrentUser() == null || patientEmail == null) {
            Toast.makeText(this, "Authentication failed or no patient found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        caretakerEmail = mAuth.getCurrentUser().getEmail().replace(".", "_");
        patientKey = patientEmail.replace(".", "_");

        // Initialize ListView and Adapter
        reminderList = new ArrayList<>();
        reminderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reminderList);
        reminderListView.setAdapter(reminderAdapter);

        // Load existing reminders
        loadExistingReminders();

        // Save reminder button click listener
        saveReminderButton.setOnClickListener(v -> saveReminder());
    }

    private void saveReminder() {
        String medicineName = medicineNameEditText.getText().toString().trim();
        String reminderTime = reminderTimeEditText.getText().toString().trim();

        if (medicineName.isEmpty() || reminderTime.isEmpty()) {
            Toast.makeText(this, "Please enter both medicine name and reminder time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create reminder object
        MedicineReminder reminder = new MedicineReminder(medicineName, reminderTime);

        // Save to Firebase
        databaseReference.child(caretakerEmail).child("patients").child(patientKey).child("medicines")
                .push().setValue(reminder)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reminder set successfully!", Toast.LENGTH_SHORT).show();
                        medicineNameEditText.setText("");
                        reminderTimeEditText.setText("");

                        // Schedule the alarm for the reminder time
                        scheduleReminderAlarm(reminder);
                        loadExistingReminders();  // Refresh list
                    } else {
                        Toast.makeText(this, "Failed to set reminder", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadExistingReminders() {
        // Clear existing list before fetching new data
        reminderList.clear();

        DatabaseReference remindersRef = databaseReference.child(caretakerEmail).child("patients").child(patientKey).child("medicines");

        remindersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    MedicineReminder reminder = snapshot.getValue(MedicineReminder.class);
                    if (reminder != null) {
                        reminderList.add(reminder.getMedicineName() + " at " + reminder.getReminderTime());
                    }
                }
                // Notify adapter about dataset change
                reminderAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "No reminders found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch reminders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void scheduleReminderAlarm(MedicineReminder reminder) {
        String reminderTime = reminder.getReminderTime();
        String[] timeParts = reminderTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);  // Schedule for the next day if time has passed
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("medicine_name", reminder.getMedicineName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    Toast.makeText(this, "Exact alarms not allowed. Enable in settings.", Toast.LENGTH_LONG).show();
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
    }
