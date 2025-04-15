package com.crfmanagement.settings;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Singleton for managing application settings (preferences).
 * Stores settings in a properties file and notifies listeners on changes.
 */
public class SettingsManager {
    private static SettingsManager instance;
    private final Properties settings;
    private final PropertyChangeSupport pcs;
    private Color backgroundColor;

    private SettingsManager() {
        settings = new Properties();
        pcs = new PropertyChangeSupport(this);
        // Default background color
        backgroundColor = Color.WHITE;
        // Load from properties file if exists
        try (FileInputStream fis = new FileInputStream("settings.properties")) {
            settings.load(fis);
            // If a background color was saved, load it
            String bgColor = settings.getProperty("backgroundColor");
            if (bgColor != null) {
                backgroundColor = Color.decode(bgColor);
            }
        } catch (Exception e) {
            // No settings file, using defaults
        }
    }

    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public Properties getSettings() {
        return settings;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color newColor) {
        Color oldColor = this.backgroundColor;
        this.backgroundColor = newColor;
        settings.setProperty("backgroundColor", String.format("#%06x", newColor.getRGB() & 0xFFFFFF));
        saveSettings();
        pcs.firePropertyChange("backgroundColor", oldColor, newColor);
    }

    public String getSetting(String key, String defaultValue) {
        return settings.getProperty(key, defaultValue);
    }

    public void setSetting(String key, String value) {
        String oldVal = settings.getProperty(key);
        settings.setProperty(key, value);
        saveSettings();
        pcs.firePropertyChange(key, oldVal, value);
    }

    private void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream("settings.properties")) {
            settings.store(fos, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }
}
