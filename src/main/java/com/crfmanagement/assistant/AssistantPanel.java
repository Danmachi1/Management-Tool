package com.crfmanagement.assistant;

import javax.swing.*;

import com.crfmanagement.reminder.RemindersPanel;
import com.crfmanagement.task.TaskManagementPanel;

import java.awt.*;

/**
 * AssistantPanel provides a simple interface for tips, help, or guidance
 * inside the CRF Management Tool.
 */
public class AssistantPanel extends JPanel {

    private JTextArea assistantTextArea;
    private JButton refreshTipButton;

    private final String[] tips = {
        "💡 Tip: Break large tasks into smaller steps.",
        "🧠 Reminder: Review your PSL goals weekly.",
        "✅ Productivity: Use the timer to stay focused.",
        "📅 Plan ahead: Schedule tomorrow’s tasks before ending today.",
        "☕ Don’t forget breaks — even 5 minutes helps!"
    };
	private RemindersPanel remindersPanel;
	private TaskManagementPanel taskPanel;

    public AssistantPanel(RemindersPanel remindersPanel, TaskManagementPanel taskPanel) {
        this.remindersPanel = remindersPanel;
        this.taskPanel = taskPanel;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Assistant"));

        assistantTextArea = new JTextArea();
        assistantTextArea.setLineWrap(true);
        assistantTextArea.setWrapStyleWord(true);
        assistantTextArea.setEditable(false);
        assistantTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        assistantTextArea.setText(getRandomTip());

        JScrollPane scrollPane = new JScrollPane(assistantTextArea);
        add(scrollPane, BorderLayout.CENTER);

        refreshTipButton = new JButton("Get Another Tip");
        refreshTipButton.addActionListener(e -> assistantTextArea.setText(getRandomTip()));
        add(refreshTipButton, BorderLayout.SOUTH);
    }

    private String getRandomTip() {
        int index = (int) (Math.random() * tips.length);
        return tips[index];
    }
}
