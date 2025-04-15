package com.crfmanagement.task;

import java.io.Serializable;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;  // Ensure compatibility across versions
    private String date;
    private String description;
    private boolean completed;
    private String tag;  // NEW: optional tag for categorization

    public Task(String date, String description, boolean completed) {
        this(date, description, completed, null);
    }

    // NEW: Overloaded constructor with tag
    public Task(String date, String description, boolean completed, String tag) {
        this.date = date;
        this.description = description;
        this.completed = completed;
        this.tag = tag;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}
