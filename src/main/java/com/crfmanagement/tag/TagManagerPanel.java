package com.crfmanagement.tag;

import com.crfmanagement.task.TaskManagementPanel;

import javax.swing.*;
import java.awt.*;

public class TagManagerPanel extends JPanel {
    private final TagManager tagManager;
    private final DefaultListModel<String> tagListModel;
    private final JList<String> tagList;
    private final TaskManagementPanel taskPanelRef;

    public TagManagerPanel(TaskManagementPanel taskPanelRef) {
        super(new BorderLayout());
        this.tagManager = TagManager.getInstance();
        this.taskPanelRef = taskPanelRef;
        // List model and JList for tags
        tagListModel = new DefaultListModel<>();
        tagList = new JList<>(tagListModel);
        add(new JScrollPane(tagList), BorderLayout.CENTER);

        // Populate list with existing tags
        for (Tag tag : tagManager.getTags()) {
            tagListModel.addElement(tag.getName());
        }

        // Controls for adding/removing tags
        JPanel controlPanel = new JPanel(new FlowLayout());
        JTextField newTagField = new JTextField(15);
        JButton addButton = new JButton("Add Tag");
        JButton removeButton = new JButton("Delete Tag");
        controlPanel.add(new JLabel("New Tag:"));
        controlPanel.add(newTagField);
        controlPanel.add(addButton);
        controlPanel.add(removeButton);
        add(controlPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            String tagName = newTagField.getText().trim();
            if (!tagName.isEmpty()) {
                // Add tag
                Tag newTag = new Tag(tagName, pickColorForTag(tagName));
                tagManager.addTag(newTag);
                tagListModel.addElement(tagName);
                newTagField.setText("");
                // Update Task panel's tag filter and dropdown
                taskPanelRef.getTasks().forEach(t -> {}); // (No direct action needed here, tags will appear for new tasks)
            }
        });
        removeButton.addActionListener(e -> {
            int index = tagList.getSelectedIndex();
            if (index >= 0) {
                String tagName = tagListModel.get(index);
                // Confirm deletion
                int confirm = JOptionPane.showConfirmDialog(this, "Delete tag '" + tagName + "'? This will remove the tag from all tasks.", 
                                                            "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Remove tag
                    Tag toRemove = null;
                    for (Tag tag : tagManager.getTags()) {
                        if (tag.getName().equals(tagName)) {
                            toRemove = tag; break;
                        }
                    }
                    if (toRemove != null) {
                        tagManager.removeTag(toRemove);
                        tagListModel.remove(index);
                        // Remove tag from any tasks that had it
                        taskPanelRef.getTasks().forEach(task -> {
                            if (tagName.equals(task.getTag())) {
                                task.setTag(null);
                            }
                        });
                        taskPanelRef.saveTasks();
                        taskPanelRef.repaint();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tag to delete.", "No Tag Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /** Simple strategy to pick a color for a tag (could use a hash or cycle preset colors). */
    private Color pickColorForTag(String tagName) {
        // Assign a color based on hash of name for consistency
        int hash = Math.abs(tagName.hashCode());
        float hue = (hash % 360) / 360f;
        return Color.getHSBColor(hue, 0.5f, 0.9f);
    }
}
