package gui;

import data.DataLoader;
import model.Movie;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class MainFrame extends JFrame {
    private List<User> mainUsers;
    private List<User> targetUsers;
    private HashMap<Integer, String> movieTitles;
    private List<Movie> movieList;

    public MainFrame() {
        UIStyle.applyGlobalStyle();
        setTitle("Movie Recommendation System");
        setSize(1050, 720);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        loadDataAndBuildTabs();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(22, 32, 56));
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel title = new JLabel("Movie Recommendation System");
        title.setForeground(Color.WHITE);
        title.setFont(UIStyle.titleFont());

        JLabel subtitle = new JLabel("Heap-based collaborative filtering with cosine similarity");
        subtitle.setForeground(new Color(198, 210, 230));
        subtitle.setFont(UIStyle.subtitleFont());

        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 4));
        texts.setOpaque(false);
        texts.add(title);
        texts.add(subtitle);

        JLabel badge = new JLabel("Java Swing • Node-Based Max Heap");
        badge.setForeground(Color.WHITE);
        badge.setFont(UIStyle.labelFont());
        badge.setBorder(new EmptyBorder(8, 12, 8, 12));

        header.add(texts, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private void loadDataAndBuildTabs() {
        String basePath = System.getProperty("user.dir") + File.separator;
        try {
            mainUsers = DataLoader.loadUsers(basePath + "main_data.csv");
            targetUsers = DataLoader.loadUsers(basePath + "target_user.csv");
            movieTitles = DataLoader.loadMovies(basePath + "movies.csv");
            movieList = DataLoader.loadMovieObjects(basePath + "movies.csv");

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Target User Recommendations", new Tab1Panel(mainUsers, targetUsers, movieTitles));
            tabs.addTab("Custom Rating Recommendations", new Tab2Panel(mainUsers, movieTitles, movieList));

            JPanel tabContainer = new JPanel(new BorderLayout());
            tabContainer.setBackground(UIStyle.BG);
            tabContainer.setBorder(new EmptyBorder(16, 16, 16, 16));
            tabContainer.add(tabs, BorderLayout.CENTER);
            add(tabContainer, BorderLayout.CENTER);
        } catch (Exception ex) {
            add(createErrorPanel(ex.getMessage(), basePath), BorderLayout.CENTER);
        }
    }

    private JPanel createErrorPanel(String error, String basePath) {
        JPanel panel = UIStyle.card();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        JLabel title = new JLabel("CSV files could not be loaded");
        title.setFont(UIStyle.titleFont());
        title.setForeground(UIStyle.TEXT);
        JTextArea info = new JTextArea(
                "Please put these files into the project root folder:\n\n" +
                "main_data.csv\nmovies.csv\ntarget_user.csv\n\n" +
                "Expected folder:\n" + basePath + "\n\n" +
                "Error detail:\n" + error
        );
        info.setEditable(false);
        info.setFont(UIStyle.resultFont());
        info.setBackground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);
        return panel;
    }
}
