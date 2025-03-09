package com.crfmanagement.notes;

import com.crfmanagement.reminder.RemindersPanel;
import com.crfmanagement.settings.SettingsManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MeetingNotesPanel extends JPanel {
    private final Map<String, List<Note>> folderNotes;
    private final JPanel notesContainer;
    private final JComboBox<String> filterDropdown;
    private final JTextField searchField;
    private String currentFolder;
    private final DefaultListModel<String> folderListModel;
    private final JList<String> folderList;
    private final RemindersPanel remindersPanel; // Link to Reminders Panel
    private JLabel selectedCountLabel; // Counter for selected notes


    public MeetingNotesPanel(RemindersPanel remindersPanel) {
        setLayout(new BorderLayout());
        this.remindersPanel = remindersPanel;

        folderNotes = new HashMap<>();
        currentFolder = "General";
        folderNotes.putIfAbsent(currentFolder, new ArrayList<>()); // Default folder

        loadNotes();

        // Folder List Panel
        folderListModel = new DefaultListModel<>();
        folderNotes.keySet().forEach(folderListModel::addElement);
        folderList = new JList<>(folderListModel);
        folderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        folderList.setSelectedValue(currentFolder, true);
        folderList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                switchFolder(folderList.getSelectedValue());
            }
        });
        new DropTarget(folderList, new FolderDropTargetListener());

        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPanel.setPreferredSize(new Dimension(150, 0));
        folderPanel.setBorder(new LineBorder(Color.GRAY, 1));
        folderPanel.add(new JLabel("Folders", SwingConstants.CENTER), BorderLayout.NORTH);

        // Add Folder and Remove Folder Buttons
        JPanel folderButtonPanel = new JPanel(new GridLayout(2, 1));
        JButton addFolderButton = new JButton("Add Folder");
        addFolderButton.addActionListener(e -> addNewFolder());
        folderButtonPanel.add(addFolderButton);

        JButton removeFolderButton = new JButton("Remove Folder");
        removeFolderButton.addActionListener(e -> removeCurrentFolder());
        folderButtonPanel.add(removeFolderButton);

        folderPanel.add(new JScrollPane(folderList), BorderLayout.CENTER);
        folderPanel.add(folderButtonPanel, BorderLayout.SOUTH);
        add(folderPanel, BorderLayout.WEST);

        // Top panel for search, filter, and color picker
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(20);
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> refreshNotesDisplay());
        topPanel.add(searchButton);

        filterDropdown = new JComboBox<>(new String[]{"All", "High Priority", "Medium Priority", "Low Priority"});
        filterDropdown.addActionListener(e -> refreshNotesDisplay());
        topPanel.add(new JLabel("Filter:"));
        topPanel.add(filterDropdown);

        JButton colorPickerButton = new JButton("Pick Color");
        colorPickerButton.addActionListener(e -> changeSelectedNoteColors());
        topPanel.add(colorPickerButton);

        JButton sortByTitleButton = new JButton("Sort by Title");
        sortByTitleButton.addActionListener(e -> sortNotesByTitle());
        topPanel.add(sortByTitleButton);

        JButton sortByDateButton = new JButton("Sort by Date");
        sortByDateButton.addActionListener(e -> sortNotesByDate());
        topPanel.add(sortByDateButton);

        add(topPanel, BorderLayout.NORTH);

        // Notes container
        notesContainer = new JPanel();
        notesContainer.setLayout(new WrapLayout(FlowLayout.LEFT)); // Align notes to the left
        notesContainer.setBackground(SettingsManager.getInstance().getBackgroundColor());
        JScrollPane scrollPane = new JScrollPane(notesContainer);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel for Add, Export, and Reminder buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addNoteButton = new JButton("Add Note");
        addNoteButton.addActionListener(e -> createNewNote());
        bottomPanel.add(addNoteButton);

        JButton exportButton = new JButton("Export Notes");
        exportButton.addActionListener(e -> exportSelectedNotes());
        bottomPanel.add(exportButton);

        JButton reminderButton = new JButton("Add to Reminder");
        reminderButton.addActionListener(e -> addToReminder());
        bottomPanel.add(reminderButton);

        JButton deleteButton = new JButton("Delete Selected Notes");
        deleteButton.addActionListener(e -> deleteSelectedNotes());
        bottomPanel.add(deleteButton);

        add(bottomPanel, BorderLayout.SOUTH);
        selectedCountLabel = new JLabel("Selected: 0"); // Initialize with 0
        bottomPanel.add(selectedCountLabel);
        JButton selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(e -> selectAllNotes());
        bottomPanel.add(selectAllButton);

        JButton deselectAllButton = new JButton("Deselect All");
        deselectAllButton.addActionListener(e -> deselectAllNotes());
        bottomPanel.add(deselectAllButton);

        refreshNotesDisplay();
    }

    private void addToReminder() {
        List<Note> selectedNotes = folderNotes.getOrDefault(currentFolder, new ArrayList<>()).stream()
                .filter(Note::isSelected)
                .collect(Collectors.toList());

        if (selectedNotes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No notes selected for reminders.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (Note note : selectedNotes) {
            remindersPanel.addReminderFromNote(note);
        }

        JOptionPane.showMessageDialog(this, "Selected notes added to reminders.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void createNewNote() {
        Note newNote = new Note("New Note", "", LocalDate.now(), "Low", new String[]{});
        newNote.setColor(SettingsManager.getInstance().getBackgroundColor()); // Apply default color
        folderNotes.get(currentFolder).add(newNote);
        refreshNotesDisplay();
        saveNotes();
    }




    private void refreshNotesDisplay() {
        String searchQuery = searchField.getText().toLowerCase();
        String filter = (String) filterDropdown.getSelectedItem();

        notesContainer.removeAll();

        List<Note> filteredNotes = folderNotes.getOrDefault(currentFolder, new ArrayList<>()).stream()
                .filter(note -> note.getTitle().toLowerCase().contains(searchQuery)
                        || note.getContent().toLowerCase().contains(searchQuery)
                        || String.join(", ", note.getTags()).toLowerCase().contains(searchQuery))
                .filter(note -> {
                    if ("All".equals(filter)) return true;
                    return note.getPriority().equalsIgnoreCase(filter.replace(" Priority", ""));
                })
                .collect(Collectors.toList());

        for (Note note : filteredNotes) {
            JPanel notePanel = createNotePanel(note);
            notesContainer.add(notePanel);
        }

        notesContainer.revalidate();
        notesContainer.repaint();
    }

    private JPanel createNotePanel(Note note) {
        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.setBorder(new LineBorder(Color.GRAY, 1));
        notePanel.setPreferredSize(new Dimension(250, 200));
        notePanel.setBackground(note.getColor());

        // Title Field
        JTextField titleField = new JTextField(note.getTitle());
        titleField.setFont(new Font("Arial", Font.BOLD, 14));
        titleField.setHorizontalAlignment(SwingConstants.CENTER);
        titleField.setBackground(note.getColor());
        titleField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        titleField.addActionListener(e -> {
            note.setTitle(titleField.getText());
            saveNotes();
        });
        notePanel.add(titleField, BorderLayout.NORTH);

        // Content Area
        JTextArea contentArea = new JTextArea(note.getContent());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(note.getColor());
        notePanel.add(contentArea, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(note.getColor());

        JLabel dateLabel = new JLabel("Date: " + note.getDate());
        dateLabel.setHorizontalAlignment(SwingConstants.LEFT);
        footerPanel.add(dateLabel, BorderLayout.WEST);

        JCheckBox selectCheckbox = new JCheckBox("Select");
        selectCheckbox.setSelected(note.isSelected());
        selectCheckbox.addActionListener(e -> note.setSelected(selectCheckbox.isSelected()));
        footerPanel.add(selectCheckbox, BorderLayout.EAST);

        notePanel.add(footerPanel, BorderLayout.SOUTH);

        // Double-click to open editor
        notePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    StickyNoteEditor editor = new StickyNoteEditor(note, MeetingNotesPanel.this::saveNotes);
                    editor.setVisible(true);
                }
                DragSource dragSource = DragSource.getDefaultDragSource();
                dragSource.createDefaultDragGestureRecognizer(notePanel, DnDConstants.ACTION_MOVE, dge -> {
                    DragSourceListener dragSourceListener = new DragSourceAdapter() {};
                    dragSource.startDrag(dge, DragSource.DefaultMoveDrop, new NoteTransferable(note), dragSourceListener);
                });

            }
        });

        // Drag source for note panels
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(notePanel, DnDConstants.ACTION_MOVE, dge -> {
            DragSourceListener dragSourceListener = new DragSourceAdapter() {};
            dragSource.startDrag(dge, DragSource.DefaultMoveDrop, new NoteTransferable(note), dragSourceListener);
        });
        selectCheckbox.addActionListener(e -> {
            note.setSelected(selectCheckbox.isSelected());
            updateSelectedCount();
        });

        return notePanel;
    }

    private void addNewFolder() {
        String folderName = JOptionPane.showInputDialog(this, "Enter Folder Name:");
        if (folderName != null && !folderName.trim().isEmpty()) {
            folderName = folderName.trim();
            if (!folderNotes.containsKey(folderName)) {
                folderNotes.put(folderName, new ArrayList<>());
                folderListModel.addElement(folderName);
            } else {
                JOptionPane.showMessageDialog(this, "Folder already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeCurrentFolder() {
        if ("General".equals(currentFolder)) {
            JOptionPane.showMessageDialog(this, "Cannot remove the default folder.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        folderNotes.remove(currentFolder);
        folderListModel.removeElement(currentFolder);
        currentFolder = "General";
        folderList.setSelectedValue("General", true);
        refreshNotesDisplay();
    }

    private void switchFolder(String folderName) {
        if (folderName != null) {
            currentFolder = folderName;
            refreshNotesDisplay();
        }
    }

    private void changeSelectedNoteColors() {
        Color selectedColor = JColorChooser.showDialog(this, "Pick Note Color", Color.YELLOW);
        if (selectedColor != null) {
            folderNotes.getOrDefault(currentFolder, new ArrayList<>()).forEach(note -> {
                if (note.isSelected()) {
                    note.setColor(selectedColor);
                }
            });
            saveNotes();
            refreshNotesDisplay();
        }
    }

    private void sortNotesByTitle() {
        folderNotes.getOrDefault(currentFolder, new ArrayList<>())
                .sort(Comparator.comparing(Note::getTitle, String.CASE_INSENSITIVE_ORDER));
        refreshNotesDisplay();
    }

    private void sortNotesByDate() {
        folderNotes.getOrDefault(currentFolder, new ArrayList<>())
                .sort(Comparator.comparing(Note::getDate));
        refreshNotesDisplay();
    }

    private void exportSelectedNotes() {
        List<Note> selectedNotes = folderNotes.getOrDefault(currentFolder, new ArrayList<>()).stream()
                .filter(Note::isSelected)
                .collect(Collectors.toList());

        if (selectedNotes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No notes selected for export.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Export File");
        fileChooser.setSelectedFile(new File("ExportedNotes.xhtml"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File exportFile = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile))) {
                writer.write(generateXHTMLContent(selectedNotes));
                JOptionPane.showMessageDialog(this, "Notes exported successfully to " + exportFile.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to export notes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String generateXHTMLContent(List<Note> notes) {
        StringBuilder xhtml = new StringBuilder();
        xhtml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xhtml.append("<!DOCTYPE html>\n");
        xhtml.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        xhtml.append("<head>\n<title>Exported Notes</title>\n</head>\n");
        xhtml.append("<body>\n");

        for (Note note : notes) {
            xhtml.append("<div style=\"border:1px solid #ccc; padding:10px; margin:10px;\">\n");
            xhtml.append("<h1>").append(note.getTitle()).append("</h1>\n");
            xhtml.append("<p><strong>Date:</strong> ").append(note.getDate()).append("</p>\n");
            xhtml.append("<p><strong>Priority:</strong> ").append(note.getPriority()).append("</p>\n");
            xhtml.append("<p><strong>Tags:</strong> ").append(String.join(", ", note.getTags())).append("</p>\n");
            xhtml.append("<div style=\"margin-top:10px;\">").append(note.getContent().replace("\n", "<br/>")).append("</div>\n");
            xhtml.append("</div>\n");
        }

        xhtml.append("</body>\n</html>");
        return xhtml.toString();
    }

    private void deleteSelectedNotes() {
        List<Note> selectedNotes = folderNotes.getOrDefault(currentFolder, new ArrayList<>()).stream()
                .filter(Note::isSelected)
                .collect(Collectors.toList());

        if (selectedNotes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No notes selected for deletion.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        folderNotes.get(currentFolder).removeAll(selectedNotes);
        refreshNotesDisplay();
        saveNotes();
    }

    private void saveNotes() {
        try (FileOutputStream fos = new FileOutputStream("notes.dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(folderNotes);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save notes.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadNotes() {
        try (FileInputStream fis = new FileInputStream("notes.dat");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<String, List<Note>> loadedNotes = (Map<String, List<Note>>) ois.readObject();
            folderNotes.putAll(loadedNotes);
        } catch (Exception e) {
            folderNotes.clear();
            folderNotes.putIfAbsent("General", new ArrayList<>());
        }
    }

private class FolderDropTargetListener extends DropTargetAdapter {
    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            Transferable transferable = dtde.getTransferable();

            if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                // Get the dropped folder from the JList
                Point dropPoint = dtde.getLocation();
                int index = folderList.locationToIndex(dropPoint); // Identify the target folder
                if (index == -1) {
                    JOptionPane.showMessageDialog(MeetingNotesPanel.this,
                            "No valid folder selected.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String droppedFolder = folderList.getModel().getElementAt(index);

                // Ensure the target folder exists
                if (folderNotes.containsKey(droppedFolder)) {
                    String noteTitle = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                    // Find the note by title in the current folder
                    Optional<Note> matchingNote = folderNotes.get(currentFolder).stream()
                            .filter(note -> note.getTitle().equals(noteTitle))
                            .findFirst();

                    if (matchingNote.isPresent()) {
                        Note note = matchingNote.get();

                        // Remove from current folder and add to the target folder
                        folderNotes.get(currentFolder).remove(note);
                        folderNotes.get(droppedFolder).add(note);

                        // Update UI and save changes
                        refreshNotesDisplay();
                        saveNotes();

                        JOptionPane.showMessageDialog(MeetingNotesPanel.this,
                                "Moved note '" + note.getTitle() + "' to folder: " + droppedFolder,
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MeetingNotesPanel.this,
                                "No matching note found in the current folder.",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(MeetingNotesPanel.this,
                            "Invalid target folder.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MeetingNotesPanel.this,
                    "Error occurred while moving the note.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    private class NoteTransferable implements Transferable {
        private final Note note;

        public NoteTransferable(Note note) {
            this.note = note;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.stringFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return note.getTitle(); // Use the note title as the transferable data
        }
    }
    private void updateSelectedCount() {
        int selectedCount = folderNotes.getOrDefault(currentFolder, new ArrayList<>()).stream()
                .filter(Note::isSelected)
                .toList().size();
        selectedCountLabel.setText("Selected: " + selectedCount);
    }
    private void selectAllNotes() {
        folderNotes.getOrDefault(currentFolder, new ArrayList<>()).forEach(note -> note.setSelected(true));
        refreshNotesDisplay();
        updateSelectedCount();
    }

    private void deselectAllNotes() {
        folderNotes.getOrDefault(currentFolder, new ArrayList<>()).forEach(note -> note.setSelected(false));
        refreshNotesDisplay();
        updateSelectedCount();
    }

}

