package com.crfmanagement.dashboard;

import com.crfmanagement.settings.SettingsManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private Map<Integer, PSLData> pslDataMap;
    private JTextField filePathField;
    private JButton loadButton;
    private JPanel progressPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        pslDataMap = new HashMap<>();
        SettingsManager settingsManager = SettingsManager.getInstance();

        // Initial Background Color
        setBackground(settingsManager.getBackgroundColor());

        // Header Panel (Placeholder for any header UI, if needed)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(settingsManager.getBackgroundColor());
        headerPanel.add(new JLabel("CRF Management Dashboard"), BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Progress Panel
        progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBackground(settingsManager.getBackgroundColor());
        JScrollPane scrollPane = new JScrollPane(progressPanel);
        add(scrollPane, BorderLayout.CENTER);

        // File Selection Panel
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

        // Apply initial settings
        applySettings();

        // Listen for settings changes
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> {
            applySettings();
        });

        settingsManager.addPropertyChangeListener("dashboard.excel.path", evt -> {
            String newPath = (String) evt.getNewValue();
            filePathField.setText(newPath);
            loadButton.setEnabled(!newPath.equals("Select a file..."));
        });
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            filePathField.setText(path);
            loadButton.setEnabled(true);

            // Save the path in settings
            SettingsManager settingsManager = SettingsManager.getInstance();
            settingsManager.setSetting("dashboard.excel.path", path);
        }
    }

    private void loadDataFromExcel(String filePath) {
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
                if (row.getRowNum() == 0) continue;

                Cell pslCell = row.getCell(1);
                Cell crfCell = row.getCell(2);
                Cell descriptionCell = row.getCell(3);
                Cell stepCell = row.getCell(4);
                Cell statusCell = row.getCell(6);
                Cell assigneeCell = row.getCell(5);
                Cell utDateCell = row.getCell(7);
                Cell uaDateCell = row.getCell(8);

                if (pslCell != null && pslCell.getCellType() == CellType.NUMERIC) {
                    currentPSL = (int) pslCell.getNumericCellValue();
                } else if (currentPSL == null) {
                    continue;
                }

                if (crfCell != null && crfCell.getCellType() == CellType.NUMERIC) {
                    currentCRF = (int) crfCell.getNumericCellValue();
                }

                if (descriptionCell != null && descriptionCell.getCellType() == CellType.STRING) {
                    currentDescription = descriptionCell.getStringCellValue();
                }

                if (utDateCell != null) {
                    if (utDateCell.getCellType() == CellType.STRING) {
                        currentUTDate = utDateCell.getStringCellValue();
                    } else if (utDateCell.getCellType() == CellType.NUMERIC) {
                        currentUTDate = new DataFormatter().formatCellValue(utDateCell);
                    }
                }

                if (uaDateCell != null) {
                    if (uaDateCell.getCellType() == CellType.STRING) {
                        currentUADate = uaDateCell.getStringCellValue();
                    } else if (uaDateCell.getCellType() == CellType.NUMERIC) {
                        currentUADate = new DataFormatter().formatCellValue(uaDateCell);
                    }
                }

                if (stepCell == null || stepCell.getCellType() != CellType.STRING) {
                    continue;
                }

                String step = stepCell.getStringCellValue();
                boolean isComplete = statusCell != null && statusCell.getCellType() == CellType.STRING &&
                                     "Completed".equalsIgnoreCase(statusCell.getStringCellValue());

                pslDataMap.putIfAbsent(currentPSL, new PSLData(currentPSL, currentCRF, currentDescription, currentUTDate, currentUADate));
                PSLData pslData = pslDataMap.get(currentPSL);
                pslData.addStep(step, isComplete, assigneeCell != null && assigneeCell.getCellType() == CellType.STRING ? assigneeCell.getStringCellValue() : "Unassigned");
            }

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

    private void applySettings() {
        Color backgroundColor = SettingsManager.getInstance().getBackgroundColor();
        setBackground(backgroundColor);
        progressPanel.setBackground(backgroundColor);
    }

    private JPanel createProgressPanel(PSLData pslData) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);

        // Title (PSL and CRF)
        JLabel titleLabel = new JLabel("PSL: " + pslData.getPsl() + " | CRF: " + pslData.getCrf());
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
        JLabel currentStepLabel = new JLabel("Current Step: " + pslData.getFirstIncompleteStep() +
                " | Assignees: " + pslData.getAssignees());
        currentStepLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        currentStepLabel.setForeground(new Color(128, 0, 128)); // Purple
        currentStepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(currentStepLabel);

        // Progress Bar
        JProgressBar progressBar = new JProgressBar(0, pslData.getTotalSteps());
        progressBar.setValue(pslData.getCompletedSteps());
        progressBar.setStringPainted(true);
        progressBar.setString("Completed: " + pslData.getCompletedSteps() + " / " + pslData.getTotalSteps());
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(progressBar);

        // Step Details
        JLabel stepsLabel = new JLabel("Steps: " + pslData.getStepDetails());
        stepsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stepsLabel.setForeground(new Color(0, 128, 128)); // Teal
        stepsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stepsLabel);

        return panel;
    }
}
