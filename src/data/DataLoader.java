package data;

import model.Movie;
import model.User;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * CSV dosyalarını okur.
 *
 * VERİ YAPISI SEÇİMİ:
 *   - Her kullanıcının puanları HashMap<Integer,Integer> (movieId → rating) olarak tutulur.
 *   - Yalnızca sıfır olmayan puanlar saklanır → bellek verimliliği.
 *   - HashMap O(1) ekleme/erişim sağlar.
 *   - 9018 sütunlu satırlar için seyrek temsil kritik öneme sahiptir:
 *     ortalama kullanıcı ~100 film puanladığından diziye kıyasla ~90x daha az bellek kullanır.
 *
 * DÜZELTME: movieId artık dizi indeksinden (i) değil,
 *            CSV başlık satırındaki gerçek değerden (header[i]) okunur.
 *            Bu, sütun sırası değişse bile doğru eşlemeyi garanti eder.
 */
public class DataLoader {

    /**
     * main_data.csv veya target_user.csv dosyasını okur.
     * Her satır bir kullanıcıya, her sütun bir filme karşılık gelir.
     * Değer 0 olan puanlar saklanmaz (seyrek temsil).
     */
    public static List<User> loadUsers(String filePath) throws IOException {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = reader(filePath)) {

            // Başlık satırını oku ve movieId dizisine çevir
            String headerLine = br.readLine();
            if (headerLine == null) return users;
            String[] header = headerLine.split(",", -1);

            // Her kullanıcı satırını oku
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(",", -1);

                int userId = Integer.parseInt(values[0].trim());
                HashMap<Integer, Integer> ratings = new HashMap<>();

                for (int i = 1; i < values.length && i < header.length; i++) {
                    String val = values[i].trim();
                    if (val.isEmpty()) continue;
                    int rating = Integer.parseInt(val);
                    if (rating > 0) {
                        // DÜZELTME: movieId, başlıktan okunuyor (header[i])
                        int movieId = Integer.parseInt(header[i].trim());
                        ratings.put(movieId, rating);
                    }
                }
                users.add(new User(userId, ratings));
            }
        }
        return users;
    }

    /**
     * movies.csv dosyasını okur.
     * movieId → title eşlemesi döner.
     * Film isimlerinde virgül olabileceğinden ilk ve son virgüle göre ayrıştırılır.
     */
    public static HashMap<Integer, String> loadMovies(String filePath) throws IOException {
        HashMap<Integer, String> movies = new HashMap<>();
        try (BufferedReader br = reader(filePath)) {
            br.readLine(); // başlık satırını atla
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Movie movie = parseMovieLine(line);
                if (movie != null) {
                    movies.put(movie.getMovieId(), movie.getTitle());
                }
            }
        }
        return movies;
    }

    /**
     * movies.csv dosyasını Movie nesneleri listesi olarak yükler.
     * Tab 2'deki rastgele film seçimi için kullanılır.
     */
    public static List<Movie> loadMovieObjects(String filePath) throws IOException {
        List<Movie> movies = new ArrayList<>();
        try (BufferedReader br = reader(filePath)) {
            br.readLine(); // başlık satırını atla
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Movie movie = parseMovieLine(line);
                if (movie != null) movies.add(movie);
            }
        }
        return movies;
    }

    /**
     * Tek bir movies.csv satırını ayrıştırır.
     * Format: movieId,title,genres
     * Film isimlerinde virgül olabilir → ilk virgül=ID sınırı, son virgül=genre sınırı.
     * Tırnak işaretli başlık ve başlık → temizlenir.
     */
    private static Movie parseMovieLine(String line) {
        try {
            int firstComma = line.indexOf(',');
            int lastComma  = line.lastIndexOf(',');
            if (firstComma < 0 || lastComma <= firstComma) return null;

            int    movieId = Integer.parseInt(line.substring(0, firstComma).trim());
            String title   = line.substring(firstComma + 1, lastComma).trim();
            String genres  = line.substring(lastComma + 1).trim();

            // Tırnak içinde virgül olan başlıkları temizle: "title, subtitle" → title, subtitle
            if (title.startsWith("\"") && title.endsWith("\"")) {
                title = title.substring(1, title.length() - 1).replace("\"\"", "\"");
            }
            return new Movie(movieId, title, genres);
        } catch (Exception ex) {
            return null; // hatalı satırı sessizce atla
        }
    }

    private static BufferedReader reader(String filePath) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), StandardCharsets.UTF_8));
    }
}
