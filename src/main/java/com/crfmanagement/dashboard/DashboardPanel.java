package com.crfmanagement.dashboard;

import com.crfmanagement.settings.SettingsManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final Map<Integer, PSLData> pslDataMap;
    private JTextField filePathField;
    private JButton loadButton;
    private JPanel progressPanel;
    private String currentFilePath;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        pslDataMap = new HashMap<>();
        SettingsManager settingsManager = SettingsManager.getInstance();

        // Set background color from settings
        setBackground(settingsManager.getBackgroundColor());

        // Header (title) 
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(settingsManager.getBackgroundColor());
        JLabel titleLabel = new JLabel("CRF Management Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Panel to hold PSL progress cards
        progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBackground(settingsManager.getBackgroundColor());
        // Make the progress panel scrollable in case of many entries
        JScrollPane scrollPane = new JScrollPane(progressPanel);
        add(scrollPane, BorderLayout.CENTER);

        // File selection and load panel (bottom)
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        filePanel.setBackground(settingsManager.getBackgroundColor());
        JLabel fileLabel = new JLabel("Excel File:");
        filePathField = new JTextField(settingsManager.getSettings().getProperty("dashboard.excel.path", "Select a file..."));
        filePathField.setEditable(false);
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> chooseFile());
        loadButton = new JButton("Load");
        loadButton.setEnabled(!filePathField.getText().equals("Select a file..."));
        loadButton.addActionListener(e -> {
            if (!filePathField.getText().equals("Select a file...")) {
                loadDataFromExcel(filePathField.getText());
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(settingsManager.getBackgroundColor());
        buttonPanel.add(browseButton);
        buttonPanel.add(loadButton);
        filePanel.add(fileLabel, BorderLayout.WEST);
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(buttonPanel, BorderLayout.EAST);
        add(filePanel, BorderLayout.SOUTH);

        // Apply initial settings (theme colors)
        applySettings();
        // Listen for dynamic settings changes
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> applySettings());
        settingsManager.addPropertyChangeListener("dashboard.excel.path", evt -> {
            String newPath = (String) evt.getNewValue();
            filePathField.setText(newPath);
            loadButton.setEnabled(!"Select a file...".equals(newPath));
        });
    }

    /** Opens a file chooser to select the Excel file for PSL data, and saves the selection in settings. */
    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            filePathField.setText(path);
            loadButton.setEnabled(true);
            // Save the path in settings for persistence
            SettingsManager.getInstance().setSetting("dashboard.excel.path", path);
        }
    }

    /** Reads the PSL data from the Excel file and populates the dashboard. */
    private void loadDataFromExcel(String filePath) {
        currentFilePath = filePath;
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            pslDataMap.clear();
            Integer currentPSL = null;
            Integer currentCRF = null;
            String currentDescription = null;
            String currentUTDate = null;
            String currentUADate = null;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header row
                // Assume Excel columns: 0=Release, 1=PSL, 2=CRF, 3=Description, 4=Step, 5=Assignee, 6=Status, 7=UT Date, 8=UA Date
                Cell pslCell = row.getCell(1);
                Cell crfCell = row.getCell(2);
                Cell descriptionCell = row.getCell(3);
                Cell stepCell = row.getCell(4);
                Cell assigneeCell = row.getCell(5);
                Cell statusCell = row.getCell(6);
                Cell utDateCell = row.getCell(7);
                Cell uaDateCell = row.getCell(8);

                // Read PSL and CRF (they might repeat across multiple step rows)
                if (pslCell != null && pslCell.getCellType() == CellType.NUMERIC) {
                    currentPSL = (int) pslCell.getNumericCellValue();
                } else if (pslCell != null && pslCell.getCellType() == CellType.STRING && !pslCell.getStringCellValue().isEmpty()) {
                    currentPSL = Integer.parseInt(pslCell.getStringCellValue());
                } 
                if (currentPSL == null) continue; // skip if PSL not determined
                if (crfCell != null) {
                    if (crfCell.getCellType() == CellType.NUMERIC) {
                        currentCRF = (int) crfCell.getNumericCellValue();
                    } else if (crfCell.getCellType() == CellType.STRING && !crfCell.getStringCellValue().isEmpty()) {
                        currentCRF = Integer.parseInt(crfCell.getStringCellValue());
                    }
                }
                if (descriptionCell != null && descriptionCell.getCellType() == CellType.STRING) {
                    currentDescription = descriptionCell.getStringCellValue();
                }
                // UT Date and UA Date can be string or numeric (date formatted)
                if (utDateCell != null) {
                    currentUTDate = new DataFormatter().formatCellValue(utDateCell);
                }
                if (uaDateCell != null) {
                    currentUADate = new DataFormatter().formatCellValue(uaDateCell);
                }

                // Read step name; skip row if no step (should not happen in well-formed sheet)
                if (stepCell == null || stepCell.getCellType() != CellType.STRING) {
                    continue;
                }
                String stepName = stepCell.getStringCellValue();
                // Determine completion status of this step
                boolean isComplete = false;
                if (statusCell != null) {
                    String statusVal = new DataFormatter().formatCellValue(statusCell);
                    isComplete = statusVal.equalsIgnoreCase("Completed") || statusVal.equalsIgnoreCase("Complete");
                }
                // Get assignee text
                String assignee = (assigneeCell != null) ? new DataFormatter().formatCellValue(assigneeCell) : "Unassigned";

                // Create or fetch PSLData for currentPSL
                pslDataMap.putIfAbsent(currentPSL, new PSLData(currentPSL, currentCRF, currentDescription, currentUTDate, currentUADate));
                PSLData pslData = pslDataMap.get(currentPSL);
                pslData.addStep(stepName, isComplete, (assignee.isEmpty() ? "Unassigned" : assignee));
            }
            // After reading all rows, build UI components for each PSL
            progressPanel.removeAll();
            for (PSLData pslData : pslDataMap.values()) {
                JPanel pslPanel = createProgressPanel(pslData);
                progressPanel.add(pslPanel);
            }
            progressPanel.revalidate();
            progressPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading Excel file: " + e.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Apply current settings (e.g., background color) to this panel and subcomponents. */
    private void applySettings() {
        Color backgroundColor = SettingsManager.getInstance().getBackgroundColor();
        setBackground(backgroundColor);
        progressPanel.setBackground(backgroundColor);
        // Also update file panel and its components
        for (Component comp : getComponents()) {
            comp.setBackground(backgroundColor);
        }
    }

    /** Create a UI panel representing one PSL's progress and details. */
    private JPanel createProgressPanel(PSLData pslData) {
        // Outer panel for PSL entry
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);

        // Title: PSL and CRF
        JLabel titleLabel = new JLabel("PSL: " + pslData.getPsl() + (pslData.getCrf() != null ? " | CRF: " + pslData.getCrf() : ""));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(30, 144, 255)); // Dodger Blue
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        // Description
        JLabel descriptionLabel = new JLabel("Description: " + pslData.getDescription());
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descriptionLabel.setForeground(new Color(34, 139, 34)); // Forest Green
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descriptionLabel);

        // UT Date and UA Date
        JLabel dateLabel = new JLabel("UT Date: " + pslData.getUtDate() + " | UA Date: " + pslData.getUaDate());
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        dateLabel.setForeground(new Color(255, 140, 0)); // Dark Orange
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(dateLabel);

        // Current Step and Assignees
        JLabel currentStepLabel = new JLabel("Current Step: " + pslData.getFirstIncompleteStep() + " | Assignees: " + pslData.getAssignees());
        currentStepLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        currentStepLabel.setForeground(new Color(128, 0, 128)); // Purple
        currentStepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(currentStepLabel);

        // Progress bar indicating how many steps completed out of total
        JProgressBar progressBar = new JProgressBar(0, pslData.getTotalSteps());
        progressBar.setValue(pslData.getCompletedSteps());
        progressBar.setStringPainted(true);
        progressBar.setString("Completed: " + pslData.getCompletedSteps() + " / " + pslData.getTotalSteps());
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(progressBar);

        // Step details label listing all steps and their status
        JLabel stepsLabel = new JLabel("Steps: " + pslData.getStepDetails());
        stepsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stepsLabel.setForeground(new Color(0, 128, 128)); // Teal
        stepsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stepsLabel);

        // NEW: Add interactivity – double-click to mark next incomplete step as completed
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // On double-click, mark the first incomplete step as complete (if any) and refresh UI and Excel
                if (e.getClickCount() == 2) {
                    String stepToComplete = pslData.getFirstIncompleteStep();
                    if (stepToComplete != null && !"None".equals(stepToComplete)) {
                        // Mark in PSLData
                        pslData.markStepComplete(stepToComplete);
                        // Update Excel data source for persistence
                        updateExcelStepStatus(pslData.getPsl(), stepToComplete, true);
                        // Refresh this panel's display
                        progressBar.setValue(pslData.getCompletedSteps());
                        progressBar.setString("Completed: " + pslData.getCompletedSteps() + " / " + pslData.getTotalSteps());
                        currentStepLabel.setText("Current Step: " + pslData.getFirstIncompleteStep() + " | Assignees: " + pslData.getAssignees());
                        stepsLabel.setText("Steps: " + pslData.getStepDetails());
                        // Optionally, visually indicate the update was successful
                        panel.setBackground(new Color(220, 255, 220));
                    }
                }
            }
        });

        return panel;
    }

    /**
     * Update the Excel file to mark a given step of a given PSL as completed.
     * This writes back to the "Status" column of the matching PSL/Step row in the Excel.
     */
    private void updateExcelStepStatus(int pslNumber, String stepName, boolean completed) {
        if (currentFilePath == null) return;
        try (FileInputStream fis = new FileInputStream(currentFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                Cell pslCell = row.getCell(1);
                Cell stepCell = row.getCell(4);
                Cell statusCell = row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                int pslVal;
                if (pslCell == null) continue;
                if (pslCell.getCellType() == CellType.NUMERIC) {
                    pslVal = (int) pslCell.getNumericCellValue();
                } else if (pslCell.getCellType() == CellType.STRING && !pslCell.getStringCellValue().isEmpty()) {
                    pslVal = Integer.parseInt(pslCell.getStringCellValue());
                } else {
                    continue;
                }
                if (pslVal == pslNumber && stepCell != null && stepCell.getStringCellValue().equals(stepName)) {
                    // Mark status as "Completed"
                    statusCell.setCellValue("Completed");
                }
            }
            // Write changes back to file
            try (FileOutputStream fos = new FileOutputStream(currentFilePath)) {
                workbook.write(fos);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update Excel: " + ex.getMessage(),
                                          "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
