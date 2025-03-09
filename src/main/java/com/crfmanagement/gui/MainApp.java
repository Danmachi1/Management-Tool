package com.crfmanagement.gui;

import com.crfmanagement.tracker.PSLTrackerPanel;
import com.crfmanagement.notes.MeetingNotesPanel;
import com.crfmanagement.reminder.RemindersPanel;
import com.crfmanagement.task.TaskManagementPanel;
import com.crfmanagement.dashboard.DashboardPanel;
import com.crfmanagement.settings.SettingsPanel;


import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainApp {
    private static final Map<String, JInternalFrame> frameRegistry = new HashMap<>();

    public static void main(String[] args) {
        // Force modern look and feel
        setModernLookAndFeel();

        JFrame frame = new JFrame("CRF Management Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JDesktopPane desktopPane = new JDesktopPane();
        frame.add(desktopPane, BorderLayout.CENTER);

        // Add tabs
        addInternalFrame(desktopPane, "Dashboard", new DashboardPanel());
        addInternalFrame(desktopPane, "PSL Tracker", new PSLTrackerPanel());
        addInternalFrame(desktopPane, "Reminders", new RemindersPanel());
        addInternalFrame(desktopPane, "Tasks", new TaskManagementPanel());
        RemindersPanel remindersPanel = new RemindersPanel();
        addInternalFrame(desktopPane, "Meeting Notes", new MeetingNotesPanel(remindersPanel));
        addInternalFrame(desktopPane, "Settings", new SettingsPanel());

        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // Menu for reopening tabs and calendar
        JMenu menu = new JMenu("Menu");

        JMenuItem calendarItem = new JMenuItem("Open Calendar");
        calendarItem.addActionListener(e -> openOutlookCalendar());
        menu.add(calendarItem);

        menu.addSeparator();

        for (String title : frameRegistry.keySet()) {
            JMenuItem reopenItem = new JMenuItem("Reopen " + title);
            reopenItem.addActionListener(e -> reopenTab(desktopPane, title));
            menu.add(reopenItem);
        }

        menuBar.add(menu);

        // Window menu for positioning
        JMenu windowMenu = new JMenu("Window");
        JMenu positionMenu = new JMenu("Position");

        JMenuItem cascadeItem = new JMenuItem("Cascade");
        cascadeItem.addActionListener(e -> cascadeWindows(desktopPane));
        positionMenu.add(cascadeItem);

        JMenuItem tileHorizontallyItem = new JMenuItem("Tile Horizontally");
        tileHorizontallyItem.addActionListener(e -> tileWindowsHorizontally(desktopPane));
        positionMenu.add(tileHorizontallyItem);

        JMenuItem tileVerticallyItem = new JMenuItem("Tile Vertically");
        tileVerticallyItem.addActionListener(e -> tileWindowsVertically(desktopPane));
        positionMenu.add(tileVerticallyItem);

        JMenuItem topLeftCornerItem = new JMenuItem("Top Left Corner");
        topLeftCornerItem.addActionListener(e -> positionTopLeft(desktopPane));
        positionMenu.add(topLeftCornerItem);

        JMenuItem bottomRightCornerItem = new JMenuItem("Bottom Right Corner");
        bottomRightCornerItem.addActionListener(e -> positionBottomRight(desktopPane));
        positionMenu.add(bottomRightCornerItem);

        windowMenu.add(positionMenu);
        menuBar.add(windowMenu);

        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    private static void addInternalFrame(JDesktopPane desktopPane, String title, JPanel panel) {
        JInternalFrame internalFrame = frameRegistry.get(title);

        if (internalFrame == null) {
            internalFrame = new JInternalFrame(title, true, true, true, true);
            internalFrame.setSize(400, 300);
            internalFrame.setLocation(desktopPane.getAllFrames().length * 30, desktopPane.getAllFrames().length * 30);
            internalFrame.add(panel, BorderLayout.CENTER);
            internalFrame.setVisible(true);

            // Add the custom buttons to the title bar
            addCustomButtonsToTitleBar(internalFrame, desktopPane, title, panel);

            frameRegistry.put(title, internalFrame);
            desktopPane.add(internalFrame);
        } else if (internalFrame.isClosed()) {
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

   
    private static void addCustomButtonsToTitleBar(JInternalFrame internalFrame, JDesktopPane desktopPane, String title, JPanel panel) {
        BasicInternalFrameUI frameUI = (BasicInternalFrameUI) internalFrame.getUI();
        JComponent titlePane = frameUI.getNorthPane();

        if (titlePane != null) {
            titlePane.setLayout(new BorderLayout());

            // Title Label
            JLabel titleLabel = new JLabel("  " + title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setForeground(Color.BLACK);
            titlePane.add(titleLabel, BorderLayout.WEST);

            // Button Panel for Actions
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonPanel.setOpaque(false);

            // Minimize Button
            JButton minimizeButton = createCustomDrawnButton("Minimize", new Color(255, 140, 0), g -> {
                g.drawLine(5, 10, 15, 10); // Horizontal line for minimize
            });
            minimizeButton.addActionListener(e -> {
                try {
                    internalFrame.setIcon(true); // Minimize the frame
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            buttonPanel.add(minimizeButton);

            // Maximize Button
            JButton maximizeButton = createCustomDrawnButton("Maximize", new Color(60, 179, 113), g -> {
                g.drawRect(4, 4, 12, 12); // Outer rectangle
            });
            maximizeButton.addActionListener(e -> {
                if (internalFrame.isMaximum()) {
                    try {
                        internalFrame.setMaximum(false); // Restore size
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        internalFrame.setMaximum(true); // Maximize frame
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            buttonPanel.add(maximizeButton);

            // Pop-Out Button
            JButton popOutButton = createCustomDrawnButton("Pop Out", new Color(34, 139, 34), g -> {
                g.drawPolygon(new int[]{6, 14, 14, 10, 10, 6}, new int[]{6, 6, 14, 14, 10, 10}, 6); // Outward arrow
            });
            popOutButton.addActionListener(e -> popOutTab(internalFrame, panel, title));
            buttonPanel.add(popOutButton);

            // Close Button
            JButton closeButton = createCustomDrawnButton("Close", new Color(178, 34, 34), g -> {
                g.drawLine(5, 5, 15, 15); // Diagonal line 1 for close
                g.drawLine(15, 5, 5, 15); // Diagonal line 2 for close
            });
            closeButton.addActionListener(e -> internalFrame.dispose());
            buttonPanel.add(closeButton);

            // Add buttons to the title pane
            titlePane.add(buttonPanel, BorderLayout.EAST);

            titlePane.revalidate();
            titlePane.repaint();
        }
    }

    private static JButton createCustomDrawnButton(String tooltip, Color backgroundColor, CustomDrawing drawing) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Set anti-aliasing for smoother drawing
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill background
                g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw custom content, centered
                g2d.setColor(Color.WHITE);
                g2d.translate((getWidth() - 20) / 2, (getHeight() - 20) / 2); // Center drawing
                drawing.draw(g2d);
            }
        };

        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(20, 20)); // Smaller button size
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        return button;
    }

    // Functional interface for custom drawing
    @FunctionalInterface
    private interface CustomDrawing {
        void draw(Graphics2D g);
    }



    private static void popOutTab(JInternalFrame internalFrame, JPanel panel, String title) {
        JFrame popOutFrame = new JFrame(title);
        popOutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        popOutFrame.setSize(800, 600);
        popOutFrame.setLocationRelativeTo(null);
        popOutFrame.add(panel);
        popOutFrame.setVisible(true);

        internalFrame.dispose();
    }

    private static void openOutlookCalendar() {
        try {
            Runtime.getRuntime().exec("cmd /c start outlookcal:");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to open Outlook Calendar.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static void reopenTab(JDesktopPane desktopPane, String title) {
        JInternalFrame frame = frameRegistry.get(title);

        if (frame != null) {
            try {
                if (frame.isClosed()) {
                    // Remove the old frame from the registry
                    frameRegistry.remove(title);

                    // Create a new instance of the frame
                    JPanel newPanel;
                    switch (title) {
                        case "Dashboard":
                            newPanel = new DashboardPanel();
                            break;
                        case "PSL Tracker":
                            newPanel = new PSLTrackerPanel();
                            break;
                        case "Reminders":
                            newPanel = new RemindersPanel();
                            break;
                        case "Meeting Notes":
                            newPanel = new MeetingNotesPanel(new RemindersPanel());
                            break;
                        case "Tasks":
                            newPanel = new TaskManagementPanel();
                            break;
                        case "Settings":
                            newPanel = new SettingsPanel();
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown tab title: " + title);
                    }

                    // Add the new frame to the desktop pane
                    addInternalFrame(desktopPane, title, newPanel);
                } else {
                    // If the frame is not closed, simply make it visible and selected
                    frame.setVisible(true);
                    frame.setIcon(false);
                    frame.setSelected(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    

    private static void cascadeWindows(JDesktopPane desktopPane) {
        int offset = 30;
        int x = 0, y = 0;
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.setLocation(x, y);
            x += offset;
            y += offset;
        }
    }

    private static void tileWindowsHorizontally(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int width = desktopPane.getWidth();
        int height = desktopPane.getHeight() / frames.length;

        int y = 0;
        for (JInternalFrame frame : frames) {
            frame.setSize(width, height);
            frame.setLocation(0, y);
            y += height;
        }
    }

    private static void tileWindowsVertically(JDesktopPane desktopPane) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int width = desktopPane.getWidth() / frames.length;
        int height = desktopPane.getHeight();

        int x = 0;
        for (JInternalFrame frame : frames) {
            frame.setSize(width, height);
            frame.setLocation(x, 0);
            x += width;
        }
    }

    private static void positionTopLeft(JDesktopPane desktopPane) {
        int x = 0, y = 0;
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.setLocation(x, y);
            x += 30;
            y += 30;
        }
    }

    private static void positionBottomRight(JDesktopPane desktopPane) {
        int x = desktopPane.getWidth() - 400; // Assuming 400 is the width of the frame
        int y = desktopPane.getHeight() - 300; // Assuming 300 is the height of the frame
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.setLocation(x, y);
            x -= 30;
            y -= 30;
        }
    }

    private static void setModernLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
