package com.crfmanagement.reminder;

import com.crfmanagement.notes.Note;
import com.crfmanagement.settings.SettingsManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RemindersPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Reminder> reminders;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JScrollPane scrollPane;

    public RemindersPanel() {
        setLayout(new BorderLayout());

        // Initialize reminders list
        reminders = new ArrayList<>();
        loadRemindersFromFile();

        // Define column names
        String[] columnNames = {"Title", "Description", "Due Date", "Time", "Priority", "Recurrence"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable direct editing
            }
        };
        table = new JTable(tableModel);

        // Enable sorting
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Add custom cell rendering
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String priority = (String) table.getValueAt(row, tableModel.findColumn("Priority"));
                String dueDate = (String) table.getValueAt(row, tableModel.findColumn("Due Date"));

                if (column == tableModel.findColumn("Priority")) {
                    if ("High".equalsIgnoreCase(priority)) {
                        c.setBackground(Color.RED);
                        c.setForeground(Color.WHITE);
                    } else if ("Medium".equalsIgnoreCase(priority)) {
                        c.setBackground(Color.YELLOW);
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(Color.GREEN);
                        c.setForeground(Color.BLACK);
                    }
                } else if (column == tableModel.findColumn("Due Date")) {
                    try {
                        LocalDate date = LocalDate.parse(dueDate, DateTimeFormatter.ISO_DATE);
                        LocalDate today = LocalDate.now();
                        if (date.isEqual(today)) {
                            c.setBackground(Color.RED);
                            c.setForeground(Color.WHITE);
                        } else if (date.isEqual(today.plusDays(1))) {
                            c.setBackground(Color.YELLOW);
                            c.setForeground(Color.BLACK);
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }

                return c;
            }
        });

        // Populate table with existing reminders
        populateTable();

        scrollPane = new JScrollPane(table);

        // Search functionality
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchField = new JTextField();
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }
        });

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Reminder");
        addButton.addActionListener(e -> addReminder());
        buttonPanel.add(addButton);

        JButton editButton = new JButton("Edit Reminder");
        editButton.addActionListener(e -> editReminder());
        buttonPanel.add(editButton);

        JButton deleteButton = new JButton("Delete Reminder");
        deleteButton.addActionListener(e -> deleteReminder());
        buttonPanel.add(deleteButton);

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTableModel());
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        applySettings();

        // Listen for background color changes
        SettingsManager.getInstance().addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            applyBackgroundColor(newColor);
        });
    }

    private void applySettings() {
        Color backgroundColor = SettingsManager.getInstance().getBackgroundColor();
        applyBackgroundColor(backgroundColor);
    }

    private void applyBackgroundColor(Color color) {
        setBackground(color);
        searchField.setBackground(color);
        scrollPane.getViewport().setBackground(color);
        revalidate();
        repaint();
    }

    private void filter() {
        String searchText = searchField.getText();
        if (searchText.trim().isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private void refreshTableModel() {
        loadRemindersFromFile(); // Reload reminders from the file
        tableModel.setRowCount(0); // Clear the table
        populateTable(); // Repopulate the table with the reloaded reminders
    }


    private void populateTable() {
        reminders.forEach(reminder -> tableModel.addRow(new Object[]{
                reminder.getTitle(),
                reminder.getDescription(),
                reminder.getDueDate(),
                reminder.getTime(),
                reminder.getPriority(),
                reminder.getRecurrence()
        }));
    }

    private void addReminder() {
        ReminderDialog dialog = new ReminderDialog(null);
        Reminder reminder = dialog.showDialog();
        if (reminder != null) {
            reminders.add(reminder);
            saveRemindersToFile();
            refreshTableModel(); // Refresh immediately after adding
        }
    }

    private void editReminder() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Reminder reminder = reminders.get(selectedRow);
        ReminderDialog dialog = new ReminderDialog(reminder);
        Reminder updatedReminder = dialog.showDialog();
        if (updatedReminder != null) {
            reminders.set(selectedRow, updatedReminder);
            saveRemindersToFile();
            refreshTableModel(); // Refresh immediately after editing
        }
    }

    private void deleteReminder() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        reminders.remove(selectedRow);
        saveRemindersToFile();
        refreshTableModel(); // Refresh immediately after deleting
    }

    private void saveRemindersToFile() {
        try (FileOutputStream fos = new FileOutputStream("reminders.dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(reminders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRemindersFromFile() {
        try (FileInputStream fis = new FileInputStream("reminders.dat");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            reminders = (List<Reminder>) ois.readObject();
        } catch (Exception e) {
            reminders = new ArrayList<>();
        }
    }


	    // Adding Note integration
	    public void addReminderFromNote(Note note) {
	        Reminder reminder = new Reminder(
	                note.getTitle(),
	                note.getContent(),
	                note.getDate().toString(),
	                LocalTime.now().toString(), // Default time for the reminder
	                note.getPriority(),
	                "None" // Default recurrence
	        );
	        reminders.add(reminder);
	        tableModel.addRow(new Object[]{
	                reminder.getTitle(),
	                reminder.getDescription(),
	                reminder.getDueDate(),
	                reminder.getTime(),
	                reminder.getPriority(),
	                reminder.getRecurrence()
	        });
	        saveRemindersToFile();
	        refreshTableModel(); 
	        
	    }

	
	}
