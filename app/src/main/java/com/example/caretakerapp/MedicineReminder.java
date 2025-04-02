package com.example.caretakerapp;

import java.util.Map;

public class MedicineReminder {
    private String medicineName;
    private String reminderTime;
    private Map<String, Boolean> days;
    private String key; // For Firebase operations

    // Required empty constructor for Firebase
    public MedicineReminder() {}

    public MedicineReminder(String medicineName, String reminderTime, Map<String, Boolean> days) {
        this.medicineName = medicineName;
        this.reminderTime = reminderTime;
        this.days = days;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public Map<String, Boolean> getDays() {
        return days;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}