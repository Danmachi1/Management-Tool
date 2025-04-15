package com.crfmanagement.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for logging user actions to a file.
 */
public class ActivityLogger {
    private static final String LOG_FILE = "app.log";
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Append a message to the application log with a timestamp.
     * Each log entry is written on a new line in app.log.
     */
    public static void log(String message) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(TIMESTAMP_FMT);
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(timestamp + " - " + message);
        } catch (IOException e) {
            // If logging fails, print to console as fallback
            e.printStackTrace();
        }
    }
}
