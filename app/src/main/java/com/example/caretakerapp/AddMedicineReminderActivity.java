package com.example.caretakerapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddMedicineReminderActivity extends AppCompatActivity {

    private TextInputEditText medicineNameEditText, reminderTimeEditText;
    private MaterialButton saveReminderButton;
    private RecyclerView reminderRecyclerView;
    private ReminderAdapter reminderAdapter;
    private ArrayList<MedicineReminder> reminderList;
    private Chip chipDaily, chipWeekly, chipCustom;
    private LinearLayout daysSelector;
    private Chip[] dayChips;

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
        reminderRecyclerView = findViewById(R.id.reminderListView);

        // Frequency chips
        chipDaily = findViewById(R.id.chipDaily);
        chipWeekly = findViewById(R.id.chipWeekly);
        chipCustom = findViewById(R.id.chipCustom);
        daysSelector = findViewById(R.id.daysSelector);

        // Day chips
        dayChips = new Chip[]{
                findViewById(R.id.chipMonday),
                findViewById(R.id.chipTuesday),
                findViewById(R.id.chipWednesday),
                findViewById(R.id.chipThursday),
                findViewById(R.id.chipFriday),
                findViewById(R.id.chipSaturday),
                findViewById(R.id.chipSunday)
        };

        // Set up RecyclerView
        reminderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reminderList = new ArrayList<>();
        reminderAdapter = new ReminderAdapter(this, reminderList);
        reminderRecyclerView.setAdapter(reminderAdapter);

        // Time picker
        reminderTimeEditText.setOnClickListener(v -> showTimePicker());

        // Frequency selection
        setupFrequencyChips();

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

        // Request battery optimization exemption
        requestBatteryOptimizationExemption();

        // Load existing reminders
        loadExistingReminders();

        // Save reminder button click listener
        saveReminderButton.setOnClickListener(v -> saveReminder());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute1);
                    reminderTimeEditText.setText(time);
                },
                hour, minute, true);
        timePickerDialog.show();
    }

    private void setupFrequencyChips() {
        chipDaily.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                daysSelector.setVisibility(View.GONE);
                chipWeekly.setChecked(false);
                chipCustom.setChecked(false);
            }
        });

        chipWeekly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                daysSelector.setVisibility(View.VISIBLE);
                // Select all days by default for weekly
                for (Chip chip : dayChips) {
                    chip.setChecked(true);
                }
                chipDaily.setChecked(false);
                chipCustom.setChecked(false);
            }
        });

        chipCustom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                daysSelector.setVisibility(View.VISIBLE);
                // Clear all selections for custom
                for (Chip chip : dayChips) {
                    chip.setChecked(false);
                }
                chipDaily.setChecked(false);
                chipWeekly.setChecked(false);
            }
        });
    }

    private void requestBatteryOptimizationExemption() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void saveReminder() {
        String medicineName = medicineNameEditText.getText().toString().trim();
        String reminderTime = reminderTimeEditText.getText().toString().trim();

        if (medicineName.isEmpty() || reminderTime.isEmpty()) {
            Toast.makeText(this, "Please enter both medicine name and reminder time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected days
        Map<String, Boolean> days = new HashMap<>();
        if (chipDaily.isChecked()) {
            // Daily means all days are selected
            for (String day : new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}) {
                days.put(day, true);
            }
        } else {
            // Weekly or custom - get selected days
            String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (int i = 0; i < dayChips.length; i++) {
                if (dayChips[i].isChecked()) {
                    days.put(dayNames[i], true);
                }
            }
        }

        if (days.isEmpty() && !chipDaily.isChecked()) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create reminder object
        MedicineReminder reminder = new MedicineReminder(medicineName, reminderTime, days);

        // Save to Firebase
        databaseReference.child(caretakerEmail).child("patients").child(patientKey).child("medicines")
                .push().setValue(reminder)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reminder set successfully!", Toast.LENGTH_SHORT).show();
                        scheduleReminderAlarm(reminder);
                        resetForm();
                        loadExistingReminders();  // Refresh list
                    } else {
                        Toast.makeText(this, "Failed to set reminder", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetForm() {
        medicineNameEditText.setText("");
        reminderTimeEditText.setText("");
        chipDaily.setChecked(true);
        daysSelector.setVisibility(View.GONE);
    }

    private void loadExistingReminders() {
        DatabaseReference remindersRef = databaseReference.child(caretakerEmail).child("patients").child(patientKey).child("medicines");

        remindersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reminderList.clear();
                for (DataSnapshot reminderSnapshot : snapshot.getChildren()) {
                    MedicineReminder reminder = reminderSnapshot.getValue(MedicineReminder.class);
                    if (reminder != null) {
                        reminder.setKey(reminderSnapshot.getKey());
                        reminderList.add(reminder);
                    }
                }
                reminderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddMedicineReminderActivity.this, "Failed to load reminders", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void scheduleReminderAlarm(MedicineReminder reminder) {
//        String reminderTime = reminder.getReminderTime();
//        String[] timeParts = reminderTime.split(":");
//        int hour = Integer.parseInt(timeParts[0]);
//        int minute = Integer.parseInt(timeParts[1]);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, hour);
//        calendar.set(Calendar.MINUTE, minute);
//        calendar.set(Calendar.SECOND, 0);
//
//        if (calendar.before(Calendar.getInstance())) {
//            calendar.add(Calendar.DAY_OF_YEAR, 1);
//        }
//
//        // Create unique request code for each alarm
//        int requestCode = (reminder.getMedicineName() + reminderTime).hashCode();
//
//        Intent intent = new Intent(this, AlarmReceiver.class);
//        intent.putExtra("medicine_name", reminder.getMedicineName());
//        intent.putExtra("request_code", requestCode);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                this,
//                requestCode,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//        if (alarmManager != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
//                        calendar.getTimeInMillis(),
//                        pendingIntent);
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
//                        calendar.getTimeInMillis(),
//                        pendingIntent);
//            } else {
//                alarmManager.set(AlarmManager.RTC_WAKEUP,
//                        calendar.getTimeInMillis(),
//                        pendingIntent);
//            }
//        }
//    }

    private void scheduleReminderAlarm(MedicineReminder reminder) {
        String time = reminder.getReminderTime();
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("medicine_name", reminder.getMedicineName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (reminder.getMedicineName() + time).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}