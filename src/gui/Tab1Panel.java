package gui;

import algorithm.RecommendationEngine;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

/**
 * Tab 1: Hedef kullanıcıya göre film önerisi.
 *
 * Ödev PDF parametre kuralı:
 *   X = heap'ten çekilecek benzer kullanıcı sayısı
 *   K = her benzer kullanıcıdan alınacak film sayısı
 *   Toplam = X * K film
 *
 * DÜZELTMELER:
 *   - SwingWorker ile hesaplama arka planda çalışır; UI donmaz.
 *   - X ve K etiketleri ödev PDF'iyle tam örtüşüyor.
 *   - İlerleme çubuğu (JProgressBar) hesaplama sırasında görünür.
 *   - Sonuç panelinde kullanıcı bazlı gruplandırma gösteriliyor.
 */
public class Tab1Panel extends JPanel {

    private final List<User> mainUsers;
    private final List<User> targetUsers;
    private final HashMap<Integer, String> movieTitles;

    private JComboBox<User> targetCombo;
    private JTextField      xField;        // X = benzer kullanıcı sayısı
    private JTextField      kField;        // K = kullanıcı başına film
    private JTextArea       resultArea;
    private JLabel          statusLabel;
    private JProgressBar    progressBar;
    private JButton         recommendBtn;

    public Tab1Panel(List<User> mainUsers,
                     List<User> targetUsers,
                     HashMap<Integer, String> movieTitles) {
        this.mainUsers    = mainUsers;
        this.targetUsers  = targetUsers;
        this.movieTitles  = movieTitles;
        setLayout(new BorderLayout(18, 18));
        setBackground(UIStyle.BG);
        setBorder(new EmptyBorder(28, 18, 18, 18));
        buildUI();
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
        card.setPreferredSize(new Dimension(340, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1; g.insets = new Insets(8, 0, 8, 0);

        // Başlık
        JLabel title = UIStyle.sectionTitle("Target User Settings");
        g.gridy = 0; card.add(title, g);

        // Hedef kullanıcı seçimi
        g.gridy++; card.add(UIStyle.label("Select Target User"), g);
        targetCombo = new JComboBox<>(createTargetUserModel());
        g.gridy++; card.add(targetCombo, g);

        // X parametresi — ÖDEV PDF: "most similar X user from the heap"
        g.gridy++; card.add(UIStyle.label("X  –  Number of Similar Users"), g);
        xField = UIStyle.input("3");
        g.gridy++; card.add(xField, g);

        // K parametresi — ÖDEV PDF: "K highest rated movies"
        g.gridy++; card.add(UIStyle.label("K  –  Movies per Similar User"), g);
        kField = UIStyle.input("5");
        g.gridy++; card.add(kField, g);

        // İlerleme çubuğu
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        g.gridy++; g.insets = new Insets(12, 0, 4, 0);
        card.add(progressBar, g);

        // Öneri butonu
        recommendBtn = UIStyle.primaryButton("Get Recommendations");
        recommendBtn.addActionListener(e -> startRecommendation());
        g.gridy++; g.insets = new Insets(6, 0, 8, 0);
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

        JLabel title = UIStyle.sectionTitle("Recommendation Results");

        resultArea = new JTextArea(
            "Select a target user, set X and K, then click Get Recommendations.\n\n" +
            "  X = number of most similar users extracted from heap\n" +
            "  K = highest-rated movies taken from each similar user\n" +
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

    private DefaultComboBoxModel<User> createTargetUserModel() {
        DefaultComboBoxModel<User> model = new DefaultComboBoxModel<>();
        for (User user : targetUsers) {
            model.addElement(user);
        }
        return model;
    }

    // ----------------------------------------------------------
    // HESAPLAMA (SwingWorker — arka plan iş parçacığı)
    // ----------------------------------------------------------

    private void startRecommendation() {
        // Giriş doğrulama
        final int X, K;
        final User target;
        try {
            X      = parsePositive(xField.getText(), "X");
            K      = parsePositive(kField.getText(), "K");
            target = (User) targetCombo.getSelectedItem();
            if (target == null) throw new IllegalArgumentException("Please select a target user.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // UI'ı hesaplama moduna al
        recommendBtn.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Computing similarities for " + mainUsers.size() + " users...");
        resultArea.setText("Calculating, please wait...");

        // Arka plan iş parçacığı — EDT donmasını önler
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return RecommendationEngine.recommend(target, mainUsers, movieTitles, X, K);
            }

            @Override
            protected void done() {
                try {
                    List<String> results = get();
                    displayResults(results, target, X, K);
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

    /** Sonuçları sonuç alanına yazar. */
    private void displayResults(List<String> results, User target, int X, int K) {
        StringBuilder sb = new StringBuilder();
        sb.append("Top X * K = ").append(X).append(" * ").append(K)
          .append(" = ").append(X * K).append(" Recommendations\n");
        sb.append("Target User: ").append(target).append("\n");
        sb.append(divider()).append("\n\n");

        if (results.isEmpty()) {
            sb.append("No recommendations found.\n");
            sb.append("(The target user's rated movies may not be in movies.csv)");
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
