package com.crfmanagement.reminder;

import com.crfmanagement.settings.SettingsManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RemindersPanel extends JPanel {
    private final DefaultTableModel tableModel;
    private final List<Reminder> reminders;
    private JTable table;
    private final SettingsManager settingsManager;

    public RemindersPanel() {
        super(new BorderLayout());
        this.reminders = new ArrayList<>();
        this.settingsManager = SettingsManager.getInstance();

        // Reminders table setup
        String[] cols = {"Reminder", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons panel for adding and deleting reminders
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Reminder");
        JButton deleteButton = new JButton("Delete Reminder");
        addButton.addActionListener(e -> openReminderDialog(null));
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                reminders.remove(selectedRow);
                refreshTable();
                saveReminders();
            }
        });
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load and display existing reminders
        loadReminders();
        refreshTable();

        // Theming
        setBackground(settingsManager.getBackgroundColor());
        table.getTableHeader().setBackground(settingsManager.getBackgroundColor());
        buttonPanel.setBackground(settingsManager.getBackgroundColor());
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            setBackground(newColor);
            table.getTableHeader().setBackground(newColor);
            buttonPanel.setBackground(newColor);
        });
    }

    /** Refresh table contents from the reminders list. */
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Reminder rem : reminders) {
            tableModel.addRow(new Object[]{rem.getTitle(), rem.getDueDate()});
        }
    }

    /** Open a dialog to add or edit a reminder. */
    private void openReminderDialog(Reminder existing) {
        JDialog dialog = new JDialog((Frame)null, (existing==null?"Add Reminder":"Edit Reminder"), true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        // Reminder text
        dialog.add(new JLabel("Reminder:"));
        JTextField textField = new JTextField(existing != null ? existing.getTitle() : "");
        dialog.add(textField);
        // Date text (could be date picker in a full implementation)
        dialog.add(new JLabel("Date:"));
        JTextField dateField = new JTextField(existing != null ? existing.getDueDate() : "");
        dialog.add(dateField);
        // Buttons
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        saveBtn.addActionListener(e -> {
            String text = textField.getText().trim();
            String date = dateField.getText().trim();
            if (text.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter both reminder text and date.", "Input Required", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (existing != null) {
                existing.setTitle(text);
                existing.setDueDate(date);
            } else {
                reminders.add(new Reminder(text, date, date, date, date, date));
            }
            refreshTable();
            saveReminders();
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.add(saveBtn);
        dialog.add(cancelBtn);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Save reminders to file (reminders.dat). */
    private void saveReminders() {
        try (FileOutputStream fos = new FileOutputStream("reminders.dat");
             java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos)) {
            oos.writeObject(reminders);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Load reminders from file (reminders.dat). */
    @SuppressWarnings("unchecked")
    private void loadReminders() {
        try (FileInputStream fis = new FileInputStream("reminders.dat");
             java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis)) {
            List<Reminder> loaded = (List<Reminder>) ois.readObject();
            reminders.clear();
            reminders.addAll(loaded);
        } catch (Exception e) {
            // if file not found or error, start with empty list
        }
    }

    /** Allow external panels (AssistantPanel or MeetingNotesPanel) to programmatically add a reminder. */
    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
        refreshTable();
        saveReminders();
    }
}
