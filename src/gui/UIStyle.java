package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIStyle {
    public static final Color BG = new Color(245, 247, 251);
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = new Color(44, 92, 246);
    public static final Color PRIMARY_DARK = new Color(32, 70, 190);
    public static final Color TEXT = new Color(31, 41, 55);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(225, 229, 235);

    public static Font titleFont() { return new Font("Segoe UI", Font.BOLD, 24); }
    public static Font sectionTitleFont() { return new Font("Segoe UI", Font.BOLD, 18); }
    public static Font subtitleFont() { return new Font("Segoe UI", Font.PLAIN, 13); }
    public static Font labelFont() { return new Font("Segoe UI", Font.BOLD, 13); }
    public static Font normalFont() { return new Font("Segoe UI", Font.PLAIN, 13); }
    public static Font resultFont() { return new Font("Consolas", Font.PLAIN, 14); }

    public static void applyGlobalStyle() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.font", normalFont());
        UIManager.put("Button.font", labelFont());
        UIManager.put("TextField.font", normalFont());
        UIManager.put("ComboBox.font", normalFont());
        UIManager.put("TabbedPane.font", labelFont());
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(24, 18, 20, 18)
        ));
        return panel;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(TEXT);
        button.setBackground(new Color(238, 242, 247));
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(labelFont());
        return label;
    }

    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(sectionTitleFont());
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(0, 0, 10, 0));
        return label;
    }

    public static JTextField input(String text) {
        JTextField field = new JTextField(text);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
}
