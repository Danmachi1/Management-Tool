package com.crfmanagement.settings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.awt.Color;
import java.io.*;
import java.util.Properties;

public class SettingsManager {
    private static SettingsManager instance;
    private final Properties settings;
    private final PropertyChangeSupport propertyChangeSupport;
    private Color backgroundColor;

    private SettingsManager() {
        settings = new Properties();
        propertyChangeSupport = new PropertyChangeSupport(this); // Initialize PropertyChangeSupport
        loadSettings();
    }

    // Singleton Instance Getter
    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    // Properties Accessor
    public Properties getSettings() {
        return settings;
    }

    // Add PropertyChangeListener for Specific Property
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    // Remove PropertyChangeListener for Specific Property
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    // Save Settings to File
    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream("settings.properties")) {
            settings.store(fos, "User Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Set a Setting with Property Change Support
    public void setSetting(String key, String value) {
        Object oldValue = settings.put(key, value);
        Object newValue = value;

        // Handle specific keys like "background.color" and convert to appropriate types
        if ("background.color".equals(key)) {
            try {
                newValue = parseColor(value);
                setBackgroundColor((Color) newValue); // Ensure backgroundColor updates properly
            } catch (Exception e) {
                System.err.println("Invalid color format for property change: " + value);
            }
        }

        propertyChangeSupport.firePropertyChange(key, oldValue, newValue); // Notify listeners
        saveSettings(); // Persist settings
    }

    // Get a Setting with Default Value
    public String getSetting(String key, String defaultValue) {
        return settings.getProperty(key, defaultValue);
    }

    // Load Settings from File
    public void loadSettings() {
        try (FileInputStream fis = new FileInputStream("settings.properties")) {
            settings.load(fis);
            String colorValue = settings.getProperty("background.color");
            if (colorValue != null) {
                this.backgroundColor = parseColor(colorValue); // Initialize backgroundColor from settings
            }
        } catch (IOException e) {
            System.out.println("No settings file found, using defaults.");
        }
    }

    // Get Background Color
    public Color getBackgroundColor() {
        return backgroundColor != null ? backgroundColor : Color.WHITE; // Default to white if null
    }

    // Set Background Color with Property Change Support
    public void setBackgroundColor(Color newColor) {
        Color oldColor = this.backgroundColor;
        this.backgroundColor = newColor;
        settings.setProperty("background.color", String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
        propertyChangeSupport.firePropertyChange("backgroundColor", oldColor, newColor); // Notify listeners
        saveSettings(); // Persist changes
    }

    // Parse Color String to Color Object
    private Color parseColor(String colorValue) {
        if (colorValue.startsWith("#")) {
            return Color.decode(colorValue); // Hexadecimal format
        } else {
            String[] rgb = colorValue.split(",");
            int r = Integer.parseInt(rgb[0].trim());
            int g = Integer.parseInt(rgb[1].trim());
            int b = Integer.parseInt(rgb[2].trim());
            return new Color(r, g, b); // RGB format
        }
    }
}
