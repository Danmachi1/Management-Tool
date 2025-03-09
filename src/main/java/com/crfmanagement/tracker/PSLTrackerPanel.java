package com.crfmanagement.tracker;

import com.crfmanagement.utils.ExcelReader;
import com.crfmanagement.settings.SettingsManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class PSLTrackerPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private String currentFilePath;

    // Panels for dynamic background updates
    private final JPanel searchPanel;
    private final JScrollPane scrollPane;
    private final JPanel buttonPanel;

    public PSLTrackerPanel() {
        setLayout(new BorderLayout());

        // Initialize settings manager
        SettingsManager settingsManager = SettingsManager.getInstance();

        // Load the initial background color from settings
        Color backgroundColor = settingsManager.getBackgroundColor();
        setBackground(backgroundColor);

        // Define column names
        String[] columnNames = {"Release", "PSL", "CRF", "Description", "SDLC/Doc", "Assignee", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table read-only
            }
        };
        table = new JTable(tableModel);

        // Enable sorting
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(backgroundColor); // Table viewport background
        scrollPane.setBackground(backgroundColor); // ScrollPane itself

        // Search panel setup
        searchPanel = new JPanel(new BorderLayout());
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

        // Top-right Refresh button
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadData());
        topRightPanel.add(refreshButton);
        searchPanel.add(topRightPanel, BorderLayout.EAST);

        // Add search panel and scroll pane to the main panel
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel setup
        buttonPanel = new JPanel(new FlowLayout());
        JButton loadButton = new JButton("Load PSL Data");
        loadButton.addActionListener(e -> loadData());
        buttonPanel.add(loadButton);

        JButton editButton = new JButton("Edit Excel");
        editButton.addActionListener(e -> editExcelFile());
        buttonPanel.add(editButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listen for background color changes
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            updateBackgroundColor(newColor);
        });

        // Load the saved file path from settings
        currentFilePath = settingsManager.getSetting("psltracker.excel.path", null);
        if (currentFilePath != null) {
            loadData();
        }

        // Apply initial settings
        updateBackgroundColor(backgroundColor);
    }

    private void filter() {
        String searchText = searchField.getText();
        if (searchText.trim().isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private void loadData() {
        // Check if a file path exists; prompt user otherwise
        if (currentFilePath == null) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                currentFilePath = fileChooser.getSelectedFile().getAbsolutePath();

                // Save the selected file path in settings
                SettingsManager.getInstance().setSetting("psltracker.excel.path", currentFilePath);
            } else {
                JOptionPane.showMessageDialog(this, "No file selected. Unable to load data.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Clear existing data
        tableModel.setRowCount(0);

        // Load data from the current file path
        try {
            List<Object[]> data = ExcelReader.readExcel(currentFilePath);

            // Skip the first row (header)
            for (int i = 1; i < data.size(); i++) {
                tableModel.addRow(data.get(i));
            }

            JOptionPane.showMessageDialog(this, "Data refreshed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editExcelFile() {
        if (currentFilePath == null) {
            JOptionPane.showMessageDialog(this, "No file loaded to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Desktop.getDesktop().open(new File(currentFilePath));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBackgroundColor(Color color) {
        setBackground(color);
        searchPanel.setBackground(color);
        scrollPane.getViewport().setBackground(color); // Update table viewport
        scrollPane.setBackground(color); // Update scroll pane itself
        buttonPanel.setBackground(color);
        revalidate();
        repaint();
    }
}
