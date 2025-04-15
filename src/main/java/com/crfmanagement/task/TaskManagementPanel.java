package com.crfmanagement.task;

import com.crfmanagement.settings.SettingsManager;
import com.crfmanagement.tag.Tag;
import com.crfmanagement.tag.TagManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TaskManagementPanel extends JPanel {
    private final DefaultTableModel tableModel;
    private final List<Task> tasks;
    private JTable table;
    private final SettingsManager settingsManager;
    private final TagManager tagManager;
    private JTextField searchField;
    private JComboBox<String> tagFilterCombo;

    public TaskManagementPanel() {
        super(new BorderLayout());
        this.tasks = new ArrayList<>();
        this.settingsManager = SettingsManager.getInstance();
        this.tagManager = TagManager.getInstance(); // Global TagManager for tags

        // Define table columns including new "Tag"
        String[] columnNames = {"Date", "Description", "Completed", "Tag"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);

        // Panel for search and filter controls
        JPanel topPanel = new JPanel(new BorderLayout());
        // Text search field
        searchField = new JTextField();
        searchField.setToolTipText("Search tasks...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTasks(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTasks(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTasks(); }
        });
        topPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        // Tag filter dropdown
        tagFilterCombo = new JComboBox<>();
        tagFilterCombo.addItem("All Tags");
        for (Tag tag : tagManager.getTags()) {
            tagFilterCombo.addItem(tag.getName());
        }
        tagFilterCombo.addActionListener(e -> filterTasks());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("Filter by Tag:"));
        filterPanel.add(tagFilterCombo);
        topPanel.add(filterPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Button panel for add/edit/delete actions
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addTaskButton = new JButton("Add Task");
        JButton editTaskButton = new JButton("Edit Task");
        JButton deleteTaskButton = new JButton("Delete Task");
        addTaskButton.addActionListener(e -> openTaskDialog(null));
        editTaskButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Task selectedTask = tasks.get(selectedRow);
                openTaskDialog(selectedTask);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to edit.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteTaskButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete selected task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tasks.remove(selectedRow);
                    refreshTable();
                    saveTasks();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to delete.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(addTaskButton);
        buttonPanel.add(editTaskButton);
        buttonPanel.add(deleteTaskButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load tasks from file
        loadTasks();
        refreshTable();

        // Theming: apply background from settings
        setBackground(settingsManager.getBackgroundColor());
        table.getTableHeader().setBackground(settingsManager.getBackgroundColor());
        topPanel.setBackground(settingsManager.getBackgroundColor());
        filterPanel.setBackground(settingsManager.getBackgroundColor());
        buttonPanel.setBackground(settingsManager.getBackgroundColor());
        // Listen for theme change
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            setBackground(newColor);
            table.getTableHeader().setBackground(newColor);
            topPanel.setBackground(newColor);
            filterPanel.setBackground(newColor);
            buttonPanel.setBackground(newColor);
            revalidate();
            repaint();
        });
    }

    /** Refreshes the task table to reflect the current list (with any filters currently applied). */
    private void refreshTable() {
        tableModel.setRowCount(0); // clear table
        for (Task task : tasks) {
            tableModel.addRow(new Object[]{
                task.getDate(),
                task.getDescription(),
                task.isCompleted() ? "Yes" : "No",
                (task.getTag() != null ? task.getTag() : "")
            });
        }
    }

    /** Opens a dialog to add or edit a Task. If existingTask is null, it creates a new task on save. */
    private void openTaskDialog(Task existingTask) {
        JDialog dialog = new JDialog((Frame) null, (existingTask == null ? "Add Task" : "Edit Task"), true);
        dialog.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        // Date input (simple text or structured date input)
        JLabel dateLabel = new JLabel("Date:");
        JTextField dateField = new JTextField(existingTask != null ? existingTask.getDate() : "");
        inputPanel.add(dateLabel);
        inputPanel.add(dateField);

        // Description input
        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField(existingTask != null ? existingTask.getDescription() : "");
        inputPanel.add(descriptionLabel);
        inputPanel.add(descriptionField);

        // Completed checkbox
        JLabel completedLabel = new JLabel("Completed:");
        JCheckBox completedCheckBox = new JCheckBox();
        completedCheckBox.setSelected(existingTask != null && existingTask.isCompleted());
        inputPanel.add(completedLabel);
        inputPanel.add(completedCheckBox);

        // Tag selection (dropdown of available tags)
        JLabel tagLabel = new JLabel("Tag:");
        JComboBox<String> tagCombo = new JComboBox<>();
        tagCombo.addItem("(None)");
        for (Tag tag : tagManager.getTags()) {
            tagCombo.addItem(tag.getName());
        }
        if (existingTask != null && existingTask.getTag() != null) {
            tagCombo.setSelectedItem(existingTask.getTag());
        } else {
            tagCombo.setSelectedItem("(None)");
        }
        inputPanel.add(tagLabel);
        inputPanel.add(tagCombo);

        dialog.add(inputPanel, BorderLayout.CENTER);

        // Save/Cancel buttons
        JPanel dialogButtonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String date = dateField.getText().trim();
            String description = descriptionField.getText().trim();
            boolean completed = completedCheckBox.isSelected();
            String selectedTag = (String) tagCombo.getSelectedItem();
            if (selectedTag != null && selectedTag.equals("(None)")) {
                selectedTag = null;
            }
            if (date.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Date and Description are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (existingTask != null) {
                // Update existing task
                existingTask.setDate(date);
                existingTask.setDescription(description);
                existingTask.setCompleted(completed);
                existingTask.setTag(selectedTag);
            } else {
                // Create new task and add to list
                Task newTask = new Task(date, description, completed, selectedTag);
                tasks.add(newTask);
            }
            // After changes, refresh table and save to file
            refreshTable();
            saveTasks();
            dialog.dispose();
        });
        dialogButtonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        dialogButtonPanel.add(cancelButton);
        dialog.add(dialogButtonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Filter tasks by search text and/or selected tag. */
    private void filterTasks() {
        String query = searchField.getText().trim().toLowerCase();
        String tagFilter = (String) tagFilterCombo.getSelectedItem();
        tableModel.setRowCount(0);
        for (Task task : tasks) {
            boolean matchesText = query.isEmpty() 
                                   || task.getDescription().toLowerCase().contains(query)
                                   || task.getDate().toLowerCase().contains(query);
            boolean matchesTag = (tagFilter == null || "All Tags".equals(tagFilter))
                                   || (task.getTag() != null && task.getTag().equals(tagFilter))
                                   || (task.getTag() == null && "All Tags".equals(tagFilter));
            if (matchesText && matchesTag) {
                tableModel.addRow(new Object[]{
                    task.getDate(),
                    task.getDescription(),
                    task.isCompleted() ? "Yes" : "No",
                    (task.getTag() != null ? task.getTag() : "")
                });
            }
        }
    }

    /** Save tasks list to file (tasks.dat) using object serialization. */
    @SuppressWarnings("unchecked")
    public void saveTasks() {
        try (FileOutputStream fos = new FileOutputStream("tasks.dat");
             java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos)) {
            oos.writeObject(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Load tasks list from file (tasks.dat) if exists. */
    @SuppressWarnings("unchecked")
    private void loadTasks() {
        try (FileInputStream fis = new FileInputStream("tasks.dat");
             java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis)) {
            List<Task> loadedTasks = (List<Task>) ois.readObject();
            tasks.clear();
            tasks.addAll(loadedTasks);
        } catch (Exception e) {
            // If file not found or error, start with empty task list
        }
    }

    /** Provide access to the internal task list (for Assistant panel to read tasks, for example). */
    public List<Task> getTasks() {
        return tasks;
    }
}
