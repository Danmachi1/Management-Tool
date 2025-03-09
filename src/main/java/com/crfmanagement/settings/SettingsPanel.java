package com.crfmanagement.settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SettingsPanel extends JPanel {
    private final JTextField backgroundColorField;
    private final JButton colorPickerButton;
    private final JButton saveButton;
    private final JTextField excelPathField;
    private final JButton browseButton;

    public SettingsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        SettingsManager settingsManager = SettingsManager.getInstance();

        // Background Color Section
        JPanel backgroundPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backgroundPanel.setBorder(BorderFactory.createTitledBorder("Background Color"));
        JLabel backgroundColorLabel = new JLabel("Color (Hex or RGB):");
        backgroundColorField = new JTextField(settingsManager.getSetting("background.color", "#FFFFFF"), 10);
        colorPickerButton = new JButton("Pick Color");
        colorPickerButton.addActionListener(this::openColorPicker);
        backgroundPanel.add(backgroundColorLabel);
        backgroundPanel.add(backgroundColorField);
        backgroundPanel.add(colorPickerButton);

        // Excel File Path Section
        JPanel excelPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        excelPathPanel.setBorder(BorderFactory.createTitledBorder("Default Excel File Path"));
        JLabel excelPathLabel = new JLabel("Excel Path:");
        excelPathField = new JTextField(settingsManager.getSetting("excel.file.path", "Not Set"), 20);
        browseButton = new JButton("Browse");
        browseButton.addActionListener(this::openFileChooser);
        excelPathPanel.add(excelPathLabel);
        excelPathPanel.add(excelPathField);
        excelPathPanel.add(browseButton);

        // Placeholder for Additional Settings
        JPanel additionalSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        additionalSettingsPanel.setBorder(BorderFactory.createTitledBorder("Additional Settings"));
        JCheckBox enableNotifications = new JCheckBox("Enable Notifications");
        enableNotifications.setSelected(Boolean.parseBoolean(settingsManager.getSetting("enable.notifications", "true")));
        additionalSettingsPanel.add(enableNotifications);

        // Save Button
        saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> applySettings(enableNotifications.isSelected()));

        // Add Components
        add(backgroundPanel);
        add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        add(excelPathPanel);
        add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        add(additionalSettingsPanel);
        add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        add(saveButton);
    }

    private void openColorPicker(ActionEvent e) {
        Color initialColor = SettingsManager.getInstance().getBackgroundColor();
        Color selectedColor = JColorChooser.showDialog(this, "Pick a Background Color", initialColor);
        if (selectedColor != null) {
            // Convert the selected color to hex format
            String colorHex = String.format("#%02x%02x%02x", selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue());
            backgroundColorField.setText(colorHex); // Update the text field with the selected color
        }
    }

    private void openFileChooser(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            excelPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void applySettings(boolean notificationsEnabled) {
        try {
            // Save Background Color
            String colorHex = backgroundColorField.getText();
            Color selectedColor = Color.decode(colorHex); // Convert hex to Color
            SettingsManager.getInstance().setBackgroundColor(selectedColor); // Save the color

            // Save Excel Path
            String excelPath = excelPathField.getText();
            SettingsManager.getInstance().setSetting("excel.file.path", excelPath);

            // Save Additional Settings
            SettingsManager.getInstance().setSetting("enable.notifications", String.valueOf(notificationsEnabled));

            JOptionPane.showMessageDialog(this, "Settings saved and applied successfully!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid color format. Please use a valid hex color code.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
