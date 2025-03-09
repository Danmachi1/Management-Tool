package com.crfmanagement.task;

import java.io.Serializable;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L; // Ensure compatibility

    private String date;
    private String description;
    private boolean completed;

    public Task(String date, String description, boolean completed) {
        this.date = date;
        this.description = description;
        this.completed = completed;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
