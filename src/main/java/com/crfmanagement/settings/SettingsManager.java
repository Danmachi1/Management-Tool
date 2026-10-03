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
        // Validate before changing either the in-memory or persisted setting.
        Object newValue = "background.color".equals(key) ? parseColor(value) : value;
        Object oldValue = settings.put(key, value);
        if (newValue instanceof Color) {
            setBackgroundColor((Color) newValue);
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
                try {
                    this.backgroundColor = parseColor(colorValue);
                } catch (IllegalArgumentException invalidColor) {
                    this.backgroundColor = null;
                    settings.remove("background.color");
                    System.err.println("Invalid background color in settings; using the default.");
                }
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
        java.util.Objects.requireNonNull(newColor, "Background color must not be null");
        Color oldColor = this.backgroundColor;
        this.backgroundColor = newColor;
        settings.setProperty("background.color", String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
        propertyChangeSupport.firePropertyChange("backgroundColor", oldColor, newColor); // Notify listeners
        saveSettings(); // Persist changes
    }

    // Parse Color String to Color Object
    private Color parseColor(String colorValue) {
        if (colorValue == null) {
            throw new IllegalArgumentException("Background color must not be null");
        }
        String value = colorValue.trim();
        if (value.matches("#[0-9a-fA-F]{6}")) {
            return Color.decode(value);
        }
        String[] rgb = value.split(",", -1);
        if (rgb.length != 3) {
            throw new IllegalArgumentException("Use #RRGGBB or three comma-separated RGB values");
        }
        return new Color(Integer.parseInt(rgb[0].trim()),
                         Integer.parseInt(rgb[1].trim()),
                         Integer.parseInt(rgb[2].trim()));
    }
}
