package com.crfmanagement.tag;

import java.io.Serializable;
import java.awt.Color;

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Color color;  // optional color for the tag label

    public Tag(String name, Color color) {
        this.name = name;
        this.color = color;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
}
