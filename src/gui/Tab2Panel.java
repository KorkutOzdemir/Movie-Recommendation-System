package gui;

import algorithm.RecommendationEngine;
import model.Movie;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tab 2: Kullanıcının 5 film puanlamasına göre öneri.
 *
 * DÜZELTMELER:
 *   - Rastgele film sayısı 50 → 10 (ödev: "ten randomly selected movies").
 *   - SwingWorker ile hesaplama arka planda; UI donmaz.
 *   - Aynı film iki kez seçilirse açık hata mesajı gösterilir.
 *   - Puan 1–5 aralığı dışındaysa açık hata mesajı.
 *   - X ve K etiketleri ödev PDF'iyle örtüşüyor.
 */
public class Tab2Panel extends JPanel {

    private final List<User>              mainUsers;
    private final HashMap<Integer, String> movieTitles;
    private final List<Movie>             allMovies;

    private List<Movie> randomMovies;
    private final List<JComboBox<Movie>> movieCombos = new ArrayList<>();
    private final List<JTextField> ratingFields = new ArrayList<>();
    private JTextField         xField;          // X = benzer kullanıcı sayısı
    private JTextField         kField;          // K = kullanıcı başına film
    private JTextArea          resultArea;
    private JLabel             statusLabel;
    private JProgressBar       progressBar;
    private JButton            recommendBtn;

    public Tab2Panel(List<User> mainUsers,
                     HashMap<Integer, String> movieTitles,
                     List<Movie> allMovies) {
        this.mainUsers    = mainUsers;
        this.movieTitles  = movieTitles;
        this.allMovies    = allMovies;
        setLayout(new BorderLayout(18, 18));
        setBackground(UIStyle.BG);
        setBorder(new EmptyBorder(16, 18, 18, 18));
        refreshRandomMovies();
        buildUI();
    }

    // ----------------------------------------------------------
    // RASTGELE FİLM SEÇİMİ
    // ----------------------------------------------------------

    /**
     * ÖDEV: "ten randomly selected movies from the file"
     * Panel oluşturulurken allMovies'den 10 farklı film rastgele seçilir.
     */
    private void refreshRandomMovies() {
        List<Movie> shuffled = new ArrayList<>(allMovies);
        Collections.shuffle(shuffled);
        randomMovies = shuffled.size() > 10
                ? new ArrayList<>(shuffled.subList(0, 10))
                : new ArrayList<>(shuffled);
    }

    // ----------------------------------------------------------
    // UI KURULUMU
    // ----------------------------------------------------------

    private void buildUI() {
        add(buildLeftCard(),   BorderLayout.WEST);
        add(buildResultCard(), BorderLayout.CENTER);
    }

    private JPanel buildLeftCard() {
        JPanel card = UIStyle.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(440, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1; g.insets = new Insets(4, 0, 4, 0);

        // Başlık
        JLabel title = UIStyle.sectionTitle("Rate 5 Movies");
        g.gridy = 0; card.add(title, g);

        // İpucu
        JLabel hint = UIStyle.label("Select 5 different movies and rate each from 1 to 5.");
        hint.setForeground(UIStyle.MUTED);
        hint.setFont(UIStyle.subtitleFont());
        g.gridy++; card.add(hint, g);

        // Sütun başlıkları
        JPanel headers = new JPanel(new BorderLayout(8, 0));
        headers.setOpaque(false);
        JLabel movieHeader  = UIStyle.label("Movie");
        JLabel ratingHeader = UIStyle.label("Rating");
        ratingHeader.setPreferredSize(new Dimension(60, 20));
        ratingHeader.setHorizontalAlignment(SwingConstants.CENTER);
        headers.add(movieHeader,  BorderLayout.CENTER);
        headers.add(ratingHeader, BorderLayout.EAST);
        g.gridy++; card.add(headers, g);

        // 5×2 ızgara: combo + rating field
        for (int i = 0; i < 5; i++) {
            JComboBox<Movie> movieCombo = createMovieCombo();
            if (i < randomMovies.size()) {
                movieCombo.setSelectedIndex(i);
            }
            JTextField ratingField = UIStyle.input(String.valueOf(5 - i));
            ratingField.setPreferredSize(new Dimension(60, 34));
            ratingField.setHorizontalAlignment(SwingConstants.CENTER);

            movieCombos.add(movieCombo);
            ratingFields.add(ratingField);

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.add(movieCombo, BorderLayout.CENTER);
            row.add(ratingField, BorderLayout.EAST);
            g.gridy++; card.add(row, g);
        }

        // X ve K giriş alanları
        JPanel params = new JPanel(new GridLayout(2, 2, 8, 6));
        params.setOpaque(false);
        params.add(UIStyle.label("X  –  Number of Similar Users"));
        params.add(UIStyle.label("K  –  Movies per Similar User"));
        xField = UIStyle.input("3");
        kField = UIStyle.input("5");
        params.add(xField);
        params.add(kField);
        g.gridy++; g.insets = new Insets(10, 0, 4, 0);
        card.add(params, g);

        // İlerleme çubuğu
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        g.gridy++; g.insets = new Insets(4, 0, 2, 0);
        card.add(progressBar, g);

        // Öneri butonu
        recommendBtn = UIStyle.primaryButton("Get Recommendations");
        recommendBtn.addActionListener(e -> startRecommendation());
        g.gridy++; g.insets = new Insets(4, 0, 4, 0);
        card.add(recommendBtn, g);

        // Durum etiketi
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(UIStyle.MUTED);
        statusLabel.setFont(UIStyle.subtitleFont());
        g.gridy++; card.add(statusLabel, g);

        g.gridy++; g.weighty = 1;
        card.add(Box.createVerticalGlue(), g);
        return card;
    }

    private JPanel buildResultCard() {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(10, 10));

        JLabel title = UIStyle.sectionTitle("Custom Recommendation Results");

        resultArea = new JTextArea(
            "Select 5 different movies, rate each 1–5,\n" +
            "set X and K, then click Get Recommendations.\n\n" +
            "  X = number of most similar users from heap\n" +
            "  K = top-rated movies per similar user\n" +
            "  Total movies = X * K"
        );
        resultArea.setEditable(false);
        resultArea.setFont(UIStyle.resultFont());
        resultArea.setForeground(UIStyle.TEXT);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));

        card.add(title,  BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JComboBox<Movie> createMovieCombo() {
        DefaultComboBoxModel<Movie> model = new DefaultComboBoxModel<>();
        for (Movie movie : randomMovies) {
            model.addElement(movie);
        }
        return new JComboBox<>(model);
    }

    // ----------------------------------------------------------
    // HESAPLAMA (SwingWorker)
    // ----------------------------------------------------------

    private void startRecommendation() {
        // Giriş doğrulama
        final HashMap<Integer, Integer> ratings = new LinkedHashMap<>();
        final int X, K;
        try {
            Set<Integer> selectedIds = new HashSet<>();
            for (int i = 0; i < 5; i++) {
                Movie movie = (Movie) movieCombos.get(i).getSelectedItem();
                if (movie == null)
                    throw new IllegalArgumentException("Please select all 5 movies.");

                if (!selectedIds.add(movie.getMovieId()))
                    throw new IllegalArgumentException(
                        "Duplicate movie detected in row " + (i + 1) + ":\n\"" +
                        movie.getTitle() + "\"\nPlease select 5 different movies.");

                String ratingText = ratingFields.get(i).getText().trim();
                int rating = Integer.parseInt(ratingText);
                if (rating < 1 || rating > 5)
                    throw new IllegalArgumentException(
                        "Row " + (i + 1) + ": Rating must be between 1 and 5 (got " + rating + ").");

                ratings.put(movie.getMovieId(), rating);
            }
            X = parsePositive(xField.getText(), "X");
            K = parsePositive(kField.getText(), "K");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "X, K and ratings must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // UI → hesaplama modu
        recommendBtn.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Computing similarities...");
        resultArea.setText("Calculating, please wait...");

        User customUser = new User(9999, ratings);

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return RecommendationEngine.recommend(customUser, mainUsers, movieTitles, X, K);
            }

            @Override
            protected void done() {
                try {
                    List<String> results = get();
                    displayResults(results, X, K, ratings);
                } catch (Exception ex) {
                    resultArea.setText("An error occurred:\n" + ex.getMessage());
                    statusLabel.setText("Error.");
                } finally {
                    recommendBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                }
            }
        }.execute();
    }

    private void displayResults(List<String> results, int X, int K,
                                HashMap<Integer, Integer> inputRatings) {
        StringBuilder sb = new StringBuilder();
        sb.append("Top X * K = ").append(X).append(" * ").append(K)
          .append(" = ").append(X * K).append(" Recommendations\n");
        sb.append("Based on your ratings:\n");

        // Kullanıcının girdiği filmleri ve puanları göster
        for (Map.Entry<Integer, Integer> e : inputRatings.entrySet()) {
            String t = movieTitles.getOrDefault(e.getKey(), "MovieId " + e.getKey());
            sb.append("  • ").append(t).append(" → ").append(e.getValue()).append("/5\n");
        }
        sb.append(divider()).append("\n\n");

        if (results.isEmpty()) {
            sb.append("No recommendations found.");
        } else {
            for (int i = 0; i < results.size(); i++) {
                sb.append(String.format("%2d. %s%n", i + 1, results.get(i)));
            }
        }
        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
        statusLabel.setText("Done: " + results.size() + " movie(s) listed.");
    }

    // ----------------------------------------------------------
    // YARDIMCI
    // ----------------------------------------------------------

    private int parsePositive(String text, String name) {
        try {
            int v = Integer.parseInt(text.trim());
            if (v <= 0) throw new IllegalArgumentException(name + " must be a positive integer.");
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a positive integer.");
        }
    }

    private String divider() {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            line.append("-");
        }
        return line.toString();
    }
}
