package com.example.caretakerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private Context context;
    private ArrayList<MedicineReminder> reminderList;
    private String caretakerEmail, patientKey;
    private DatabaseReference databaseReference;

    public ReminderAdapter(Context context, ArrayList<MedicineReminder> reminderList) {
        this.context = context;
        this.reminderList = reminderList;

        // Initialize Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String patientEmail = sharedPreferences.getString("patient_email", null);

        if (mAuth.getCurrentUser() != null && patientEmail != null) {
            caretakerEmail = mAuth.getCurrentUser().getEmail().replace(".", "_");
            patientKey = patientEmail.replace(".", "_");
            databaseReference = FirebaseDatabase.getInstance().getReference("caretakers");
        }
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        MedicineReminder reminder = reminderList.get(position);

        holder.medicineNameTextView.setText(reminder.getMedicineName());
        holder.reminderTimeTextView.setText(reminder.getReminderTime());

        // Set frequency badge text
        if (reminder.getDays() != null) {
            if (reminder.getDays().size() == 7) {
                holder.frequencyBadge.setText("DAILY");
            } else {
                holder.frequencyBadge.setText(reminder.getDays().size() + " DAYS");
            }
        }

        // Delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (databaseReference != null && reminder.getKey() != null) {
                databaseReference.child(caretakerEmail).child("patients").child(patientKey)
                        .child("medicines").child(reminder.getKey()).removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView medicineNameTextView, reminderTimeTextView, frequencyBadge;
        ImageButton deleteButton;

        public ReminderViewHolder(View itemView) {
            super(itemView);
            medicineNameTextView = itemView.findViewById(R.id.medicineNameTextView);
            reminderTimeTextView = itemView.findViewById(R.id.reminderTimeTextView);
            frequencyBadge = itemView.findViewById(R.id.frequencyBadge);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}