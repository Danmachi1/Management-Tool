package com.crfmanagement.tracker;

import com.crfmanagement.settings.SettingsManager;
import com.crfmanagement.utils.ExcelReader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

public class PSLTrackerPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JPanel searchPanel;
    private JPanel buttonPanel;
    private String currentFilePath;
    private final SettingsManager settingsManager;
    private JScrollPane scrollPane;

    public PSLTrackerPanel() {
        super(new BorderLayout());
        settingsManager = SettingsManager.getInstance();
        setBackground(settingsManager.getBackgroundColor());

        // Define column names for PSL detail table
        String[] columnNames = {"Release", "PSL", "CRF", "Description", "SDLC/Doc", "Assignee", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false; // make table read-only
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        // Enable sorting on table rows
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Scroll pane for table
        scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(settingsManager.getBackgroundColor());
        scrollPane.setBackground(settingsManager.getBackgroundColor());

        // Search panel at top
        searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchField = new JTextField();
        searchPanel.add(searchField, BorderLayout.CENTER);
        // Filter table as user types
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // Refresh button on top-right
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadData());
        topRightPanel.setOpaque(false);
        topRightPanel.add(refreshButton);
        searchPanel.add(topRightPanel, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with Load and Edit buttons
        buttonPanel = new JPanel(new FlowLayout());
        JButton loadButton = new JButton("Load PSL Data");
        loadButton.addActionListener(e -> loadData());
        buttonPanel.add(loadButton);
        JButton editButton = new JButton("Edit Excel");
        editButton.addActionListener(e -> editExcelFile());
        buttonPanel.add(editButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // React to background color changes (for theme switching)
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            updateBackgroundColor(newColor);
        });

        // Load previously used file path from settings, if any
        currentFilePath = settingsManager.getSetting("psltracker.excel.path", null);
        if (currentFilePath != null) {
            loadData();
        }

        // Apply initial background setting
        updateBackgroundColor(settingsManager.getBackgroundColor());
    }

    /** Filter the table rows based on the search field text (case-insensitive). */
    private void filter() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    /** Load PSL data from Excel using the ExcelReader utility and populate the table. */
    private void loadData() {
        // If no file chosen yet, prompt user to choose
        if (currentFilePath == null) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                currentFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                // Save this path for next time
                settingsManager.setSetting("psltracker.excel.path", currentFilePath);
            } else {
                JOptionPane.showMessageDialog(this, "No file selected. Unable to load data.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        // Clear current data
        tableModel.setRowCount(0);
        try {
            List<Object[]> data = ExcelReader.readExcel(currentFilePath);
            // Skip header row in data list (assuming ExcelReader included it)
            for (int i = 1; i < data.size(); i++) {
                tableModel.addRow(data.get(i));
            }
            JOptionPane.showMessageDialog(this, "Data refreshed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Open the Excel file in the default system application (e.g., Microsoft Excel) for direct editing. */
    private void editExcelFile() {
        if (currentFilePath == null) {
            JOptionPane.showMessageDialog(this, "No file loaded to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Desktop.getDesktop().open(new File(currentFilePath));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Update UI components backgrounds to match new theme color. */
    private void updateBackgroundColor(Color color) {
        setBackground(color);
        searchPanel.setBackground(color);
        scrollPane.getViewport().setBackground(color);
        scrollPane.setBackground(color);
        buttonPanel.setBackground(color);
        revalidate();
        repaint();
    }
}
