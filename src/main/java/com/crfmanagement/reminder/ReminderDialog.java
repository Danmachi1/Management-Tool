package com.crfmanagement.reminder;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReminderDialog extends JDialog {
    private JTextField titleField;
    private JTextField descriptionField;
    private JTextField dueDateField;
    private JTextField timeField;
    private JComboBox<String> priorityComboBox;
    private JComboBox<String> recurrenceComboBox;
    private boolean confirmed;
    
    public ReminderDialog(Reminder reminder) {
        setTitle(reminder == null ? "Add Reminder" : "Edit Reminder");
        setLayout(new GridLayout(7, 2, 10, 10));

        add(new JLabel("Title:"));
        titleField = new JTextField(reminder != null ? reminder.getTitle() : "");
        add(titleField);

        add(new JLabel("Description:"));
        descriptionField = new JTextField(reminder != null ? reminder.getDescription() : "");
        add(descriptionField);

        add(new JLabel("Due Date (YYYY-MM-DD):"));
        dueDateField = new JTextField(reminder != null ? reminder.getDueDate() : "");
        add(dueDateField);

        add(new JLabel("Time (HH:mm):"));
        timeField = new JTextField(reminder != null ? reminder.getTime() : "12:00");
        add(timeField);

        add(new JLabel("Priority:"));
        String[] priorities = {"High", "Medium", "Low"};
        priorityComboBox = new JComboBox<>(priorities);
        priorityComboBox.setSelectedItem(reminder != null ? reminder.getPriority() : "Medium");
        add(priorityComboBox);

        add(new JLabel("Recurrence:"));
        String[] recurrences = {"None", "Daily", "Weekly", "Monthly"};
        recurrenceComboBox = new JComboBox<>(recurrences);
        recurrenceComboBox.setSelectedItem(reminder != null ? reminder.getRecurrence() : "None");
        add(recurrenceComboBox);

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });
        add(confirmButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        pack(); // Automatically sizes the dialog based on components
        setSize(500, 400); // Set a fixed larger size (width: 500px, height: 400px)
        setModal(true);
        setLocationRelativeTo(null); // Center the dialog on the screen
    }

    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (dueDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Due Date is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            LocalDate.parse(dueDateField.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Due Date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!timeField.getText().trim().isEmpty()) {
            try {
                LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid Time format. Use HH:mm.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    public Reminder showDialog() {
        setVisible(true);
        if (confirmed) {
            return new Reminder(
                titleField.getText(),
                descriptionField.getText(),
                dueDateField.getText(),
                timeField.getText(),
                (String) priorityComboBox.getSelectedItem(),
                (String) recurrenceComboBox.getSelectedItem()
            );
        }
        return null;
    }
}
