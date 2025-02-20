package com.example.caretakerapp;

public class MedicineReminder {
    private String medicineName;
    private String reminderTime;

    // Required empty constructor for Firebase
    public MedicineReminder() {}

    public MedicineReminder(String medicineName, String reminderTime) {
        this.medicineName = medicineName;
        this.reminderTime = reminderTime;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getReminderTime() {
        return reminderTime;
    }
}
