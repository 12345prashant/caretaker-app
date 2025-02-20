package com.example.caretakerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private Context context;
    private ArrayList<MedicineReminder> reminderList;

    public ReminderAdapter(Context context, ArrayList<MedicineReminder> reminderList) {
        this.context = context;
        this.reminderList = reminderList;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item view
        View view = LayoutInflater.from(context).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        MedicineReminder reminder = reminderList.get(position);
        holder.medicineNameTextView.setText(reminder.getMedicineName());
        holder.reminderTimeTextView.setText(reminder.getReminderTime());
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    // ViewHolder class to hold the views for each item
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView medicineNameTextView, reminderTimeTextView;

        public ReminderViewHolder(View itemView) {
            super(itemView);
            medicineNameTextView = itemView.findViewById(R.id.medicineNameTextView);
            reminderTimeTextView = itemView.findViewById(R.id.reminderTimeTextView);
        }
    }
}
