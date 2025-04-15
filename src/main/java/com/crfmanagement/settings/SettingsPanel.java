package com.crfmanagement.settings;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final SettingsManager settingsManager;
    private final JRadioButton lightThemeRadio;
    private final JRadioButton darkThemeRadio;
    private final JButton bgColorButton;

    public SettingsPanel() {
        super(new GridBagLayout());
        settingsManager = SettingsManager.getInstance();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Theme selection
        JLabel themeLabel = new JLabel("Theme:");
        lightThemeRadio = new JRadioButton("Light");
        darkThemeRadio = new JRadioButton("Dark");
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightThemeRadio);
        themeGroup.add(darkThemeRadio);
        // Set initial selection based on current LookAndFeel
        boolean isDark = UIManager.getLookAndFeel() instanceof FlatDarkLaf;
        lightThemeRadio.setSelected(!isDark);
        darkThemeRadio.setSelected(isDark);

        // Theme change listeners
        lightThemeRadio.addActionListener(e -> switchTheme(false));
        darkThemeRadio.addActionListener(e -> switchTheme(true));

        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.add(themeLabel);
        themePanel.add(lightThemeRadio);
        themePanel.add(darkThemeRadio);
        add(themePanel, gbc);

        // Background color selection
        gbc.gridy++;
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("Background Color:"));
        bgColorButton = new JButton("Choose...");
        bgColorButton.addActionListener(e -> {
            Color initialColor = settingsManager.getBackgroundColor();
            Color chosen = JColorChooser.showDialog(this, "Choose Background Color", initialColor);
            if (chosen != null) {
                settingsManager.setBackgroundColor(chosen);
            }
        });
        colorPanel.add(bgColorButton);
        add(colorPanel, gbc);

        // Fill remaining space
        gbc.weighty = 1.0;
        gbc.gridy++;
        add(new JPanel(), gbc); // spacer

        setBackground(settingsManager.getBackgroundColor());
        themePanel.setBackground(settingsManager.getBackgroundColor());
        colorPanel.setBackground(settingsManager.getBackgroundColor());
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            setBackground(newColor);
            themePanel.setBackground(newColor);
            colorPanel.setBackground(newColor);
        });
    }

    /** Switch between light and dark FlatLaf themes and update UI. */
    private void switchTheme(boolean dark) {
        try {
            if (dark) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            // Update all existing UI components to new L&F
            SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(this));
            // Persist theme choice if desired (this example does not explicitly save theme preference)
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
