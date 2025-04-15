package com.crfmanagement.notes;

import com.crfmanagement.reminder.Reminder;
import com.crfmanagement.reminder.RemindersPanel;
import com.crfmanagement.notes.StickyNoteEditor;
import com.crfmanagement.notes.*;
import com.crfmanagement.settings.SettingsManager;
import com.crfmanagement.utils.DocumentListenerAdapter;
import com.crfmanagement.utils.WrapLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MeetingNotesPanel extends JPanel {
    private final List<Note> notes;
    private final JPanel notesContainer;
    private final RemindersPanel remindersPanel;  // reference to RemindersPanel to add reminders
    private final JTextField searchField;

    public MeetingNotesPanel(RemindersPanel remindersPanel) {
        super(new BorderLayout());
        this.remindersPanel = remindersPanel;
        this.notes = new ArrayList<>();
        SettingsManager settingsManager = SettingsManager.getInstance();

        // Top panel with search field for notes
        JPanel topPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.setToolTipText("Search notes...");
        topPanel.add(new JLabel("Search Notes: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        searchField.getDocument().addDocumentListener(new DocumentListenerAdapter(this::filterNotes));
        add(topPanel, BorderLayout.NORTH);

        // Container for notes (using a WrapLayout so notes flow)
        notesContainer = new JPanel(new WrapLayout(FlowLayout.LEFT));
        notesContainer.setBackground(settingsManager.getBackgroundColor());
        JScrollPane scrollPane = new JScrollPane(notesContainer);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with Add Note button
        JButton addNoteButton = new JButton("New Note");
        addNoteButton.addActionListener(e -> createNewNote());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(addNoteButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load existing notes from file
        loadNotes();
        displayAllNotes();

        // Theming: respond to background color changes
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            notesContainer.setBackground(newColor);
            setBackground(newColor);
        });
    }

    /** Creates a new note and opens the StickyNoteEditor for entering text. */
    private void createNewNote () {
        Note note = new Note("");
        notes.add(note);
        openEditor(note);
    }

    /** Opens the StickyNoteEditor dialog for a given note. */
    private void openEditor(Note note) {
        StickyNoteEditor editor = new StickyNoteEditor(note, null);
        editor.setLocationRelativeTo(this);
        editor.setVisible(true);
        if (editor.isSaved()) {
            String newText = editor.getText();
            if (newText.trim().isEmpty()) {
                // If the note text was cleared or empty, remove the note
                notes.remove(note);
            } else {
                note.setText(newText);
            }
            saveNotes();
            displayAllNotes();
        }
    }

    /** Refresh the note display by clearing and re-adding all notes (filtered by search if applicable). */
    private void displayAllNotes() {
        notesContainer.removeAll();
        String query = searchField.getText().trim().toLowerCase();
        for (Note note : notes) {
            if (query.isEmpty() || note.getText().toLowerCase().contains(query)) {
                JComponent noteComponent = createNoteComponent(note);
                notesContainer.add(noteComponent);
            }
        }
        notesContainer.revalidate();
        notesContainer.repaint();
    }

    /** Filter notes based on search field. */
    private void filterNotes() {
        displayAllNotes();  // simply redisplay with the search query applied in displayAllNotes
    }

    /** Create a visual component for a single note, with interaction (edit on double-click, context menu for reminder). */
    private JComponent createNoteComponent(Note note) {
        JTextArea noteArea = new JTextArea(note.getText());
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setEditable(false);
        noteArea.setBackground(new Color(255, 255, 204)); // light yellow background for notes
        noteArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        noteArea.setPreferredSize(new Dimension(200, 100));
        // Double-click to edit note
        noteArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditor(note);
                }
            }
        });
        // Right-click context menu to create a reminder from this note
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem remindItem = new JMenuItem("Add Reminder for this Note");
        remindItem.addActionListener(ev -> {
            // Create a new reminder using the note text (first line as title maybe)
            String noteText = note.getText();
            String title = noteText.length() > 30 ? noteText.substring(0, 30) + "..." : noteText;
            Reminder newReminder = new Reminder(title, "No date set", "No date set", "No date set", "No date set", "No date set"); // default date as none
            remindersPanel.addReminder(newReminder);  // add to reminders list
            JOptionPane.showMessageDialog(thisContainer(), "Reminder added from note.", "Reminder Created", JOptionPane.INFORMATION_MESSAGE);
        });
        contextMenu.add(remindItem);
        noteArea.setComponentPopupMenu(contextMenu);
        return noteArea;
    }

    private Component thisContainer() {
        return this;
    }

    /** Save all notes to a file (notes.dat) */
    private void saveNotes() {
        try (FileOutputStream fos = new FileOutputStream("notes.dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(notes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Load notes from file (notes.dat) if exists */
    @SuppressWarnings("unchecked")
    private void loadNotes() {
        try (FileInputStream fis = new FileInputStream("notes.dat");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            List<Note> loadedNotes = (List<Note>) ois.readObject();
            notes.clear();
            notes.addAll(loadedNotes);
        } catch (Exception e) {
            // no notes file or error reading, start fresh
        }
    }
}
