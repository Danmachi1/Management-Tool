package com.crfmanagement.reminder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Reminder implements Serializable {
    private String title;
    private String description;
    private String dueDate; // Format: YYYY-MM-DD
    private String time;    // Format: HH:mm
    private String priority; // High, Medium, Low
    private String recurrence; // None, Daily, Weekly, Monthly

    public Reminder(String title, String description, String dueDate, String time, String priority, String recurrence) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.time = time;
        this.priority = priority;
        this.recurrence = recurrence;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    // Check if the reminder is due now
    public boolean isDueNow() {
        try {
            LocalTime now = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime reminderTime = LocalTime.parse(this.time, formatter);
            return this.dueDate.equals(LocalDate.now()) && now.equals(reminderTime);
        } catch (Exception e) {
            System.err.println("Error in isDueNow: " + e.getMessage());
            return false;
        }
    }

    public void setTime(LocalTime time) {
        this.time = time.truncatedTo(java.time.temporal.ChronoUnit.MINUTES).format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
