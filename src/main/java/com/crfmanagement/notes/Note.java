package com.crfmanagement.notes;

import java.awt.Color;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;

public class Note implements Serializable {
    private String title;
    private String content;
    private LocalDate date;
    private String priority;
    private String[] tags;
    private Color color; // Add color field
    private boolean selected; // For selection

    public Note(String title, String content, LocalDate date, String priority, String[] tags) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.priority = priority;
        this.tags = tags;
        this.color = Color.YELLOW; // Default color
        this.selected = false; // Default selection state
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getTagsAsString() {
        return String.join(", ", tags);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", date=" + date +
                ", priority='" + priority + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", color=" + color +
                '}';
    }
}
