package com.crfmanagement.shared;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class HomePanel extends JPanel {
    public HomePanel(ActionListener onHomeClicked) {
        setLayout(new BorderLayout());
        JButton homeButton = new JButton("Home");
        homeButton.addActionListener(onHomeClicked);
        add(homeButton, BorderLayout.WEST);
    }
}
