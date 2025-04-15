package com.crfmanagement.utils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A simplified adapter for DocumentListener to use a single lambda or method reference for all events.
 */
public class DocumentListenerAdapter implements DocumentListener {
    private final Runnable callback;

    public DocumentListenerAdapter(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        callback.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        callback.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        callback.run();
    }
}
