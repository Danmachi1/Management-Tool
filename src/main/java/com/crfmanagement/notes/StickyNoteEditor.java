package com.crfmanagement.notes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;

public class StickyNoteEditor extends JDialog {
    private JTextField titleField;
    private JTextPane contentPane;
    private JTextField dateField;
    private JComboBox<String> priorityDropdown;
    private JTextField tagsField;
    private boolean isMaximized = false;
    private Dimension previousSize;
    private Point previousLocation;

    public StickyNoteEditor(Note note, Runnable saveCallback) {
        setUndecorated(true);
        setLayout(new BorderLayout());
        setSize(500, 600);
        setLocationRelativeTo(null);

        // Title bar with drag functionality
        JPanel dragPanel = createDragPanel(note, saveCallback);
        add(dragPanel, BorderLayout.NORTH);

        // Content area with styling support
        contentPane = createContentPane(note, saveCallback);
        JScrollPane contentScrollPane = new JScrollPane(contentPane);
        contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(contentScrollPane, BorderLayout.CENTER);

        // Footer with toolbar and details
        JPanel footerPanel = createFooterPanel(note, saveCallback);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createDragPanel(Note note, Runnable saveCallback) {
        JPanel dragPanel = new JPanel(new BorderLayout());
        dragPanel.setBackground(note.getColor());
        dragPanel.setBorder(new LineBorder(new Color(255, 204, 102), 2));

        // Title field
        titleField = new JTextField(note.getTitle());
        titleField.setFont(new Font("Verdana", Font.BOLD, 18));
        titleField.setBackground(note.getColor());
        titleField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        titleField.getDocument().addDocumentListener(new DocumentListenerAdapter(() -> {
            note.setTitle(titleField.getText());
            saveCallback.run();
        }));
        dragPanel.add(titleField, BorderLayout.CENTER);

        // Window control buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonsPanel.setBackground(note.getColor());
        buttonsPanel.add(createHeaderButton("-", e -> setVisible(false))); // Minimize
        buttonsPanel.add(createHeaderButton("[ ]", e -> toggleMaximize())); // Maximize
        buttonsPanel.add(createHeaderButton("X", e -> dispose())); // Close
        dragPanel.add(buttonsPanel, BorderLayout.EAST);

        return dragPanel;
    }

    private JTextPane createContentPane(Note note, Runnable saveCallback) {
        JTextPane pane = new JTextPane();
        pane.setEditorKit(new RTFEditorKit()); // Enable RTF support
        pane.setBackground(note.getColor());
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Load RTF content
        try {
            new RTFEditorKit().read(new java.io.StringReader(note.getContent()), pane.getStyledDocument(), 0);
        } catch (Exception e) {
            pane.setText(note.getContent());
        }

        // Save RTF content
        pane.getDocument().addDocumentListener(new DocumentListenerAdapter(() -> {
            try (java.io.StringWriter writer = new java.io.StringWriter()) {
                new RTFEditorKit().write(writer, pane.getStyledDocument(), 0, pane.getStyledDocument().getLength());
                note.setContent(writer.toString());
                saveCallback.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        return pane;
    }

    private JPanel createFooterPanel(Note note, Runnable saveCallback) {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(note.getColor());

        // Formatting toolbar
        JToolBar toolbar = createFormattingToolbar();
        footerPanel.add(toolbar, BorderLayout.NORTH);

        // Details (date, priority, tags)
        JPanel detailsPanel = createDetailsPanel(note, saveCallback);
        footerPanel.add(detailsPanel, BorderLayout.CENTER);

        return footerPanel;
    }

    private JToolBar createFormattingToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Font family
        JComboBox<String> fontDropdown = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames());
        fontDropdown.setSelectedItem("Verdana");
        fontDropdown.addActionListener(e -> changeFontFamily((String) fontDropdown.getSelectedItem()));
        toolbar.add(fontDropdown);

        // Font size
        JComboBox<Integer> fontSizeDropdown = new JComboBox<>(new Integer[]{10, 12, 14, 16, 18, 20, 24, 30, 36});
        fontSizeDropdown.setSelectedItem(14);
        fontSizeDropdown.addActionListener(e -> changeFontSize((int) fontSizeDropdown.getSelectedItem()));
        toolbar.add(fontSizeDropdown);

        // Formatting buttons
        toolbar.add(createFormattingButton("B", e -> toggleStyle(StyleConstants.Bold)));
        toolbar.add(createFormattingButton("I", e -> toggleStyle(StyleConstants.Italic)));
        toolbar.add(createFormattingButton("U", e -> toggleStyle(StyleConstants.Underline)));

        // Text alignment
        toolbar.add(createFormattingButton("L", e -> setAlignment(StyleConstants.ALIGN_LEFT)));
        toolbar.add(createFormattingButton("C", e -> setAlignment(StyleConstants.ALIGN_CENTER)));
        toolbar.add(createFormattingButton("R", e -> setAlignment(StyleConstants.ALIGN_RIGHT)));

        // Text color
        JButton colorButton = new JButton("Color");
        colorButton.addActionListener(e -> changeTextColor());
        toolbar.add(colorButton);

        // Insert elements
        toolbar.add(createFormattingButton("Img", this::insertImage));
        toolbar.add(createFormattingButton("Link", this::insertLink));
        toolbar.add(createFormattingButton("Table", this::insertTable));

        return toolbar;
    }

    private JPanel createDetailsPanel(Note note, Runnable saveCallback) {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.setBackground(note.getColor());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Date field
        panel.add(new JLabel("Date:"));
        dateField = new JTextField(note.getDate().toString());
        dateField.getDocument().addDocumentListener(new DocumentListenerAdapter(() -> {
            try {
                note.setDate(LocalDate.parse(dateField.getText()));
                saveCallback.run();
            } catch (Exception ignored) {
            }
        }));
        panel.add(dateField);

        // Priority dropdown
        panel.add(new JLabel("Priority:"));
        priorityDropdown = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        priorityDropdown.setSelectedItem(note.getPriority());
        priorityDropdown.addActionListener(e -> {
            note.setPriority((String) priorityDropdown.getSelectedItem());
            saveCallback.run();
        });
        panel.add(priorityDropdown);

        // Tags field
        panel.add(new JLabel("Tags:"));
        tagsField = new JTextField(String.join(", ", note.getTags()));
        tagsField.getDocument().addDocumentListener(new DocumentListenerAdapter(() -> {
            note.setTags(tagsField.getText().split(","));
            saveCallback.run();
        }));
        panel.add(tagsField);

        return panel;
    }

    private JButton createHeaderButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.addActionListener(actionListener); // Add action listener
        return button;
    }


    private JButton createFormattingButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Color.GRAY));
        button.addActionListener(actionListener);
        return button;
    }


    private void toggleStyle(Object styleConstant) {
        StyledDocument doc = contentPane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        if (styleConstant == StyleConstants.Bold) {
            StyleConstants.setBold(attributes, true);
        } else if (styleConstant == StyleConstants.Italic) {
            StyleConstants.setItalic(attributes, true);
        } else if (styleConstant == StyleConstants.Underline) {
            StyleConstants.setUnderline(attributes, true);
        }
        doc.setCharacterAttributes(contentPane.getSelectionStart(),
                contentPane.getSelectionEnd() - contentPane.getSelectionStart(),
                attributes,
                false);
    }

    private void changeFontFamily(String fontFamily) {
        StyledDocument doc = contentPane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attributes, fontFamily);
        doc.setCharacterAttributes(contentPane.getSelectionStart(),
                contentPane.getSelectionEnd() - contentPane.getSelectionStart(),
                attributes,
                false);
    }

    private void changeFontSize(int fontSize) {
        StyledDocument doc = contentPane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributes, fontSize);
        doc.setCharacterAttributes(contentPane.getSelectionStart(),
                contentPane.getSelectionEnd() - contentPane.getSelectionStart(),
                attributes,
                false);
    }

    private void setAlignment(int alignment) {
        StyledDocument doc = contentPane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributes, alignment);
        doc.setParagraphAttributes(contentPane.getSelectionStart(),
                contentPane.getSelectionEnd() - contentPane.getSelectionStart(),
                attributes,
                false);
    }

    private void changeTextColor() {
        Color selectedColor = JColorChooser.showDialog(this, "Choose Text Color", Color.BLACK);
        if (selectedColor != null) {
            StyledDocument doc = contentPane.getStyledDocument();
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setForeground(attributes, selectedColor);
            doc.setCharacterAttributes(contentPane.getSelectionStart(),
                    contentPane.getSelectionEnd() - contentPane.getSelectionStart(),
                    attributes,
                    false);
        }
    }

    private void insertImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIcon imageIcon = new ImageIcon(file.getAbsolutePath());
                contentPane.insertIcon(imageIcon); // Properly insert the image as an icon
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to insert image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void insertLink(ActionEvent e) {
        String linkText = JOptionPane.showInputDialog(this, "Enter Link Text:");
        String linkUrl = JOptionPane.showInputDialog(this, "Enter URL:");

        if (linkText != null && !linkText.isEmpty() && linkUrl != null && !linkUrl.isEmpty()) {
            StyledDocument doc = contentPane.getStyledDocument();
            SimpleAttributeSet linkAttributes = new SimpleAttributeSet();
            StyleConstants.setForeground(linkAttributes, Color.BLUE); // Hyperlink color
            StyleConstants.setUnderline(linkAttributes, true); // Underline
            linkAttributes.addAttribute("link", linkUrl); // Custom attribute to store URL

            try {
                doc.insertString(contentPane.getCaretPosition(), linkText, linkAttributes);
            } catch (BadLocationException ex) {
                JOptionPane.showMessageDialog(this, "Failed to insert link: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void insertTable(ActionEvent e) {
        String rowsInput = JOptionPane.showInputDialog(this, "Enter number of rows:");
        String colsInput = JOptionPane.showInputDialog(this, "Enter number of columns:");

        try {
            int rows = Integer.parseInt(rowsInput);
            int cols = Integer.parseInt(colsInput);

            StringBuilder tableBuilder = new StringBuilder("<table border='1' cellpadding='4'>");
            for (int i = 0; i < rows; i++) {
                tableBuilder.append("<tr>");
                for (int j = 0; j < cols; j++) {
                    tableBuilder.append("<td>&nbsp;</td>");
                }
                tableBuilder.append("</tr>");
            }
            tableBuilder.append("</table>");

            contentPane.replaceSelection(tableBuilder.toString());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input for rows or columns.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void toggleMaximize() {
        if (isMaximized) {
            // Restore to previous size and location
            setSize(previousSize);
            setLocation(previousLocation);
        } else {
            // Save the current size and location before maximizing
            previousSize = getSize();
            previousLocation = getLocation();

            // Maximize to the screen size
            setSize(Toolkit.getDefaultToolkit().getScreenSize());
            setLocation(0, 0);
        }
        isMaximized = !isMaximized; // Toggle the maximized state
    }

}
