package com.crfmanagement.task;

import com.crfmanagement.settings.SettingsManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TaskManagementPanel extends JPanel {
    private final DefaultTableModel tableModel;
    private final List<Task> tasks;
    private JTable table;
    private JScrollPane scrollPane;
    private JPanel buttonPanel;

    public TaskManagementPanel() {
        setLayout(new BorderLayout());
        tasks = new ArrayList<>();
        loadTasks();

        // Create table model
        String[] columnNames = {"Date", "Description", "Completed"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only the "Completed" column is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : String.class; // Checkbox for "Completed"
            }
        };

        // Create and configure table
        table = new JTable(tableModel);
        table.setRowHeight(40);

        // Create scroll pane and configure background
        scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(SettingsManager.getInstance().getBackgroundColor()); // Table area background
        scrollPane.setBackground(SettingsManager.getInstance().getBackgroundColor()); // Scroll pane background

        add(scrollPane, BorderLayout.CENTER);

        // Add buttons for task management
        buttonPanel = new JPanel(new FlowLayout());
        JButton addTaskButton = new JButton("Add Task");
        JButton editTaskButton = new JButton("Edit Task");
        JButton deleteTaskButton = new JButton("Delete Task");

        addTaskButton.addActionListener(e -> openTaskDialog(null));
        editTaskButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                Task selectedTask = tasks.get(selectedRow);
                openTaskDialog(selectedTask);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        deleteTaskButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                tasks.remove(selectedRow);
                refreshTable();
                saveTasks();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(addTaskButton);
        buttonPanel.add(editTaskButton);
        buttonPanel.add(deleteTaskButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Populate table with existing tasks
        refreshTable();

        // Dynamic Background Color Updates
        SettingsManager.getInstance().addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            setBackground(newColor);
            buttonPanel.setBackground(newColor);
            scrollPane.getViewport().setBackground(newColor); // Scroll pane viewport
            scrollPane.setBackground(newColor); // Scroll pane itself
            table.setBackground(newColor);

            // Update table header background
            JTableHeader tableHeader = table.getTableHeader();
            tableHeader.setBackground(newColor);

            revalidate();
            repaint();
        });

        // Apply initial settings
        applySettings();
    }

    private void refreshTable() {
        tableModel.setRowCount(0); // Clear the table
        for (Task task : tasks) {
            tableModel.addRow(new Object[]{
                task.getDate(),
                task.getDescription(),
                task.isCompleted()
            });
        }
    }

    private void openTaskDialog(Task existingTask) {
        JDialog dialog = new JDialog((Frame) null, "Add/Edit Task", true);
        dialog.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel dateLabel = new JLabel("Date:");
        JPanel datePicker = createDatePicker(existingTask != null ? existingTask.getDate() : null);
        inputPanel.add(dateLabel);
        inputPanel.add(datePicker);

        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField(existingTask != null ? existingTask.getDescription() : "");
        inputPanel.add(descriptionLabel);
        inputPanel.add(descriptionField);

        JLabel completedLabel = new JLabel("Completed:");
        JCheckBox completedCheckBox = new JCheckBox();
        completedCheckBox.setSelected(existingTask != null && existingTask.isCompleted());
        inputPanel.add(completedLabel);
        inputPanel.add(completedCheckBox);

        dialog.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String date = getSelectedDate(datePicker);
                String description = descriptionField.getText();
                boolean completed = completedCheckBox.isSelected();

                if (date.isEmpty() || description.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Date and Description are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (existingTask != null) {
                    existingTask.setDate(date);
                    existingTask.setDescription(description);
                    existingTask.setCompleted(completed);
                } else {
                    tasks.add(new Task(date, description, completed));
                }
                refreshTable();
                saveTasks();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date or description.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private JPanel createDatePicker(String existingDate) {
        JPanel panel = new JPanel(new FlowLayout());
        JComboBox<Integer> dayCombo = new JComboBox<>();
        JComboBox<String> monthCombo = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });
        JComboBox<Integer> yearCombo = new JComboBox<>();

        for (int i = 1; i <= 31; i++) dayCombo.addItem(i);
        int currentYear = java.time.LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) yearCombo.addItem(i);

        if (existingDate != null) {
            String[] parts = existingDate.split("-");
            yearCombo.setSelectedItem(Integer.parseInt(parts[0]));
            monthCombo.setSelectedIndex(Integer.parseInt(parts[1]) - 1);
            dayCombo.setSelectedItem(Integer.parseInt(parts[2]));
        }

        panel.add(dayCombo);
        panel.add(monthCombo);
        panel.add(yearCombo);
        return panel;
    }

    private String getSelectedDate(JPanel datePicker) {
        JComboBox<Integer> dayCombo = (JComboBox<Integer>) datePicker.getComponent(0);
        JComboBox<String> monthCombo = (JComboBox<String>) datePicker.getComponent(1);
        JComboBox<Integer> yearCombo = (JComboBox<Integer>) datePicker.getComponent(2);

        int day = (Integer) dayCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();

        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save tasks.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("tasks.dat"))) {
            tasks.addAll((List<Task>) ois.readObject());
        } catch (FileNotFoundException e) {
            // No tasks to load
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load tasks.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applySettings() {
        Color backgroundColor = SettingsManager.getInstance().getBackgroundColor();
        setBackground(backgroundColor);
        buttonPanel.setBackground(backgroundColor);
        scrollPane.getViewport().setBackground(backgroundColor); // Scroll pane viewport
        scrollPane.setBackground(backgroundColor); // Scroll pane itself
        table.setBackground(backgroundColor);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(backgroundColor);

        repaint();
    }
}
