package com.crfmanagement.gui;

import com.crfmanagement.dashboard.DashboardPanel;
import com.crfmanagement.reminder.RemindersPanel;
import com.crfmanagement.notes.MeetingNotesPanel;
import com.crfmanagement.task.TaskManagementPanel;
import com.crfmanagement.tracker.PSLTrackerPanel;
import com.crfmanagement.settings.SettingsPanel;
import com.crfmanagement.stats.StatsPanel;        // NEW: Statistics panel for charts
import com.crfmanagement.assistant.AssistantPanel;  // NEW: Assistant panel for aggregated info
import com.crfmanagement.tag.TagManagerPanel;    // NEW: Tag Manager panel for managing tags
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainApp {
    private static final Map<String, JInternalFrame> frameRegistry = new HashMap<>();

    public static void main(String[] args) {
        // Apply modern FlatLaf look and feel (default to Light theme, user can switch in Settings)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf look and feel");
        }

        // Create main application frame
        JFrame frame = new JFrame("CRF Management Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        // Main desktop pane to hold internal frames (multi-document interface)
        JDesktopPane desktopPane = new JDesktopPane();
        frame.add(desktopPane, BorderLayout.CENTER);

        // Initialize core panels (as JInternalFrame content)
        DashboardPanel dashboardPanel = new DashboardPanel();
        PSLTrackerPanel pslTrackerPanel = new PSLTrackerPanel();
        RemindersPanel remindersPanel = new RemindersPanel(); 
        TaskManagementPanel taskPanel = new TaskManagementPanel();
        MeetingNotesPanel notesPanel = new MeetingNotesPanel(remindersPanel);
        SettingsPanel settingsPanel = new SettingsPanel();
        StatsPanel statsPanel = new StatsPanel();             // NEW: shows charts and stats
        AssistantPanel assistantPanel = new AssistantPanel(remindersPanel, taskPanel); // NEW: uses reminders and tasks data
        TagManagerPanel tagManagerPanel = new TagManagerPanel(taskPanel);             // NEW: manage global tags

        // Add each panel as an internal frame (title, panel)
        addInternalFrame(desktopPane, "Dashboard", dashboardPanel);
        addInternalFrame(desktopPane, "PSL Tracker", pslTrackerPanel);
        addInternalFrame(desktopPane, "Reminders", remindersPanel);
        addInternalFrame(desktopPane, "Tasks", taskPanel);
        addInternalFrame(desktopPane, "Meeting Notes", notesPanel);
        addInternalFrame(desktopPane, "Statistics", statsPanel);       // NEW: Statistics internal frame
        addInternalFrame(desktopPane, "Assistant", assistantPanel);    // NEW: Assistant internal frame
        addInternalFrame(desktopPane, "Tag Manager", tagManagerPanel); // NEW: Tag Manager internal frame
        addInternalFrame(desktopPane, "Settings", settingsPanel);

        // Menu bar setup with "Menu" and "Window" menus
        JMenuBar menuBar = new JMenuBar();

        // "Menu" for general actions (Open Calendar, reopen closed tabs)
        JMenu menu = new JMenu("Menu");
        menu.setMnemonic(KeyEvent.VK_M); // Alt+M for Menu
        JMenuItem calendarItem = new JMenuItem("Open Calendar");
        calendarItem.setMnemonic(KeyEvent.VK_C); // Alt+C to open calendar
        calendarItem.addActionListener(e -> openOutlookCalendar());
        menu.add(calendarItem);
        menu.addSeparator();
        // Dynamically add items to reopen any closed internal frames
        for (String title : frameRegistry.keySet()) {
            JMenuItem reopenItem = new JMenuItem("Reopen " + title);
            reopenItem.addActionListener(e -> reopenTab(desktopPane, title));
            menu.add(reopenItem);
        }
        menuBar.add(menu);

        // "Window" menu for arranging internal frames
        JMenu windowMenu = new JMenu("Window");
        windowMenu.setMnemonic(KeyEvent.VK_W); // Alt+W for Window menu
        JMenu positionMenu = new JMenu("Position");
        // Cascade windows
        JMenuItem cascadeItem = new JMenuItem("Cascade");
        cascadeItem.addActionListener(e -> cascadeWindows(desktopPane));
        positionMenu.add(cascadeItem);
        // Tile horizontally
        JMenuItem tileHItem = new JMenuItem("Tile Horizontally");
        tileHItem.addActionListener(e -> tileWindowsHorizontally(desktopPane));
        positionMenu.add(tileHItem);
        // Tile vertically
        JMenuItem tileVItem = new JMenuItem("Tile Vertically");
        tileVItem.addActionListener(e -> tileWindowsVertically(desktopPane));
        positionMenu.add(tileVItem);
        // Arrange to top-left corner
        JMenuItem topLeftItem = new JMenuItem("Top Left Corner");
        topLeftItem.addActionListener(e -> positionTopLeft(desktopPane));
        positionMenu.add(topLeftItem);
        // Arrange to bottom-right corner
        JMenuItem bottomRightItem = new JMenuItem("Bottom Right Corner");
        bottomRightItem.addActionListener(e -> positionBottomRight(desktopPane));
        positionMenu.add(bottomRightItem);
        windowMenu.add(positionMenu);
        menuBar.add(windowMenu);

        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    /** 
     * Helper to create or reopen an internal frame for a given panel and title. 
     * Frames are registered in a map for easy reopen if closed.
     */
    private static void addInternalFrame(JDesktopPane desktopPane, String title, JPanel panel) {
        JInternalFrame internalFrame = frameRegistry.get(title);
        if (internalFrame == null) {
            // Create new internal frame with given panel
            internalFrame = new JInternalFrame(title, true, true, true, true);
            internalFrame.setSize(400, 300);
            // Stagger new frames slightly
            int offset = desktopPane.getAllFrames().length * 30;
            internalFrame.setLocation(offset, offset);
            internalFrame.add(panel, BorderLayout.CENTER);
            internalFrame.setVisible(true);
            // Customize title bar with extra control buttons (minimize, maximize, pop-out, close)
            addCustomButtonsToTitleBar(internalFrame, desktopPane, title, panel);
            frameRegistry.put(title, internalFrame);
            desktopPane.add(internalFrame);
        } else if (internalFrame.isClosed()) {
            // Reopen an existing frame if it was closed
            desktopPane.add(internalFrame);
            internalFrame.setVisible(true);
            try {
                internalFrame.setIcon(false);
                internalFrame.setSelected(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds custom drawn buttons (minimize, maximize/restore, pop-out, close) to the title bar of an internal frame.
     * This enhances the usability of JInternalFrame beyond the default controls.
     */
    private static void addCustomButtonsToTitleBar(JInternalFrame internalFrame, 
                                                   JDesktopPane desktopPane, String title, JPanel panel) {
        BasicInternalFrameUI frameUI = (BasicInternalFrameUI) internalFrame.getUI();
        JComponent titlePane = frameUI.getNorthPane();
        if (titlePane != null) {
            titlePane.setLayout(new BorderLayout());
            // Title label on the left
            JLabel titleLabel = new JLabel(" " + title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setForeground(Color.BLACK);
            titlePane.add(titleLabel, BorderLayout.WEST);
            // Panel for custom buttons on the right
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonPanel.setOpaque(false);

            // Minimize button
            JButton minimizeButton = createCustomDrawnButton("Minimize", new Color(255, 140, 0), g -> {
                g.drawLine(5, 10, 15, 10); // simple horizontal line
            });
            minimizeButton.addActionListener(e -> {
                try {
                    internalFrame.setIcon(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            buttonPanel.add(minimizeButton);

            // Maximize/Restore button
            JButton maximizeButton = createCustomDrawnButton("Maximize", new Color(60, 179, 113), g -> {
                g.drawRect(4, 4, 12, 12);
            });
            maximizeButton.addActionListener(e -> {
                if (internalFrame.isMaximum()) {
                    try { internalFrame.setMaximum(false); } catch (Exception ex) { ex.printStackTrace(); }
                } else {
                    try { internalFrame.setMaximum(true); } catch (Exception ex) { ex.printStackTrace(); }
                }
            });
            buttonPanel.add(maximizeButton);

            // Pop-out button (detaches the panel into an external window)
            JButton popOutButton = createCustomDrawnButton("Pop Out", new Color(34, 139, 34), g -> {
                // Draw an outward arrow icon
                g.drawPolygon(new int[]{6, 14, 14, 10, 10, 6}, new int[]{6, 6, 14, 14, 10, 10}, 6);
            });
            popOutButton.addActionListener(e -> popOutTab(internalFrame, panel, title));
            buttonPanel.add(popOutButton);

            // Close button
            JButton closeButton = createCustomDrawnButton("Close", new Color(178, 34, 34), g -> {
                g.drawLine(5, 5, 15, 15);
                g.drawLine(15, 5, 5, 15);
            });
            closeButton.addActionListener(e -> internalFrame.dispose());
            buttonPanel.add(closeButton);

            titlePane.add(buttonPanel, BorderLayout.EAST);
            titlePane.revalidate();
            titlePane.repaint();
        }
    }

    /** Utility to create a custom JButton with a drawn icon via a provided drawing lambda. */
    private static JButton createCustomDrawnButton(String tooltip, Color backgroundColor, CustomDrawing drawing) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the custom icon
                g.setColor(backgroundColor);
                drawing.draw((Graphics2D) g);
            }
        };
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(20, 20));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }

    /** Pops out the content of an internal frame into a separate external JFrame. */
    private static void popOutTab(JInternalFrame internalFrame, JPanel panel, String title) {
        try {
            internalFrame.setClosed(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create external frame
        JFrame externalFrame = new JFrame(title);
        externalFrame.setSize(800, 600);
        externalFrame.add(panel);
        externalFrame.setLocationRelativeTo(null);
        externalFrame.setVisible(true);
        externalFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // When external frame is closed, re-add content back as internal frame
                panel.remove(externalFrame);
                addInternalFrame((JDesktopPane) internalFrame.getDesktopPane(), title, panel);
            }
        });
    }

    /** Re-open a closed tab by title using the registry of internal frames. */
    private static void reopenTab(JDesktopPane desktopPane, String title) {
        JInternalFrame frame = frameRegistry.get(title);
        if (frame != null) {
            try {
                if (frame.isIcon()) frame.setIcon(false);
                if (frame.isClosed()) {
                    desktopPane.add(frame);
                    frame.setVisible(true);
                }
                frame.setSelected(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Arrange all open internal frames in a cascading overlapping fashion. */
    private static void cascadeWindows(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int x = 0, y = 0;
        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) { // ignore minimized frames
                frame.setLocation(x, y);
                x += 30; y += 30;
            }
        }
    }

    /** Tile all open internal frames horizontally (split vertical space). */
    private static void tileWindowsHorizontally(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        if (frames.length == 0) return;
        int frameHeight = desktopPane.getHeight() / frames.length;
        int y = 0;
        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) {
                frame.setSize(desktopPane.getWidth(), frameHeight);
                frame.setLocation(0, y);
                y += frameHeight;
            }
        }
    }

    /** Tile all open internal frames vertically (split horizontal space). */
    private static void tileWindowsVertically(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        if (frames.length == 0) return;
        int frameWidth = desktopPane.getWidth() / frames.length;
        int x = 0;
        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) {
                frame.setSize(frameWidth, desktopPane.getHeight());
                frame.setLocation(x, 0);
                x += frameWidth;
            }
        }
    }

    /** Position the first internal frame in the top-left corner of the desktop and others cascading from there. */
    private static void positionTopLeft(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int offset = 0;
        for (JInternalFrame frame : frames) {
            if (!frame.isIcon()) {
                frame.setLocation(10 + offset, 10 + offset);
                offset += 30;
            }
        }
    }

    /** Position the last internal frame in the bottom-right corner of the desktop. */
    private static void positionBottomRight(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        if (frames.length == 0) return;
        JInternalFrame lastFrame = frames[frames.length - 1];
        if (!lastFrame.isIcon()) {
            int x = desktopPane.getWidth() - lastFrame.getWidth() - 10;
            int y = desktopPane.getHeight() - lastFrame.getHeight() - 10;
            lastFrame.setLocation(x, y);
        }
    }

    /** Opens the default Outlook calendar application (if installed) for quick scheduling access. */
    private static void openOutlookCalendar() {
        try {
            // This tries to open Microsoft Outlook Calendar via a URI (Outlook must handle outlook:calendar)
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new java.net.URI("outlook://calendar"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Unable to open Outlook Calendar. Please open your calendar manually.",
                "Calendar Open Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Inner interface for custom drawing logic used by createCustomDrawnButton
    private interface CustomDrawing {
        void draw(Graphics2D g);
    }
}
