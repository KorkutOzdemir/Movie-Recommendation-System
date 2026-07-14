package algorithm;

import model.HeapNode;
import model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Heap tabanlı collaborative filtering ile film önerisi üretir.
 *
 * ÖDEV PDF PARAMETRELERİ (aynen uygulandı):
 *   X = heap'ten çekilecek benzer kullanıcı sayısı
 *   K = her benzer kullanıcıdan alınacak en yüksek puanlı film sayısı
 *   Toplam öneri = X * K
 *
 * DÜZELTMELER:
 *   1. findUserById() O(n) linear aramadan HashMap ile O(1) lookup'a geçildi.
 *   2. Tüm filmleri movies.csv'de bulunmayan kullanıcılar atlanıyor;
 *      heap'ten bir sonraki kullanıcıya geçilerek X geçerli kullanıcı
 *      doldurulana kadar devam ediliyor. (User 602 sorunu çözümü)
 *   3. movieId, movies.csv'de yoksa o film sessizce atlanıp sonraki deneniyor.
 */
public class RecommendationEngine {

    /**
     * Hedef kullanıcıya göre X*K film önerisi üretir.
     *
     * @param targetUser  öneri yapılacak hedef kullanıcı
     * @param mainUsers   tüm kullanıcılar (main_data.csv)
     * @param movieTitles movieId → film adı eşlemesi
     * @param X           heap'ten çekilecek benzer kullanıcı sayısı
     * @param K           her kullanıcıdan alınacak film sayısı
     * @return X*K film adı listesi (sıralı: önce en benzer kullanıcının filmleri)
     */
    public static List<String> recommend(User targetUser,
                                         List<User> mainUsers,
                                         HashMap<Integer, String> movieTitles,
                                         int X, int K) {
        // O(1) kullanıcı erişimi için HashMap; O(n) linear aramayı önler
        HashMap<Integer, User> userMap = new HashMap<>();
        for (User u : mainUsers) {
            userMap.put(u.getUserId(), u);
        }

        // Tüm kullanıcılar için cosine similarity hesapla ve heap'e ekle
        MaxHeap heap = new MaxHeap();
        for (User user : mainUsers) {
            double sim = CosineSimilarity.calculate(
                    targetUser.getRatings(), user.getRatings());
            heap.insert(user.getUserId(), sim);
        }

        return collectRecommendations(heap, userMap, movieTitles, X, K);
    }

    // -------------------------------------------------------
    // PRIVATE
    // -------------------------------------------------------

    /**
     * Heap'ten X geçerli kullanıcı bulana kadar extractMax() yapar.
     * Geçerli film bulunamayan kullanıcılar sayılmaz (User 602 senaryosu).
     */
    private static List<String> collectRecommendations(MaxHeap heap,
                                                        HashMap<Integer, User> userMap,
                                                        HashMap<Integer, String> movieTitles,
                                                        int X, int K) {
        List<String> recommendations = new ArrayList<>();
        int usersCollected = 0;

        while (!heap.isEmpty() && usersCollected < X) {
            HeapNode node = heap.extractMax();
            User similarUser = userMap.get(node.userId); // O(1)
            if (similarUser == null) continue;

            List<String> movies = topKMoviesForUser(similarUser, movieTitles, K);

            // K adet film veremeyen kullanıcıyı sayma; böylece toplam X*K korunur.
            if (movies.size() < K) continue;

            recommendations.addAll(movies);
            usersCollected++;
        }

        return recommendations;
    }

    /**
     * Bir kullanıcının en yüksek puanlı K filmini döndürür.
     * movieId movies.csv'de yoksa o film atlanır; listeye yalnızca
     * gerçek film adları eklenir.
     *
     * @param user        kaynak kullanıcı
     * @param movieTitles movieId → başlık eşlemesi
     * @param K           istenen film sayısı
     * @return en fazla K elemanlı film adı listesi
     */
    private static List<String> topKMoviesForUser(User user,
                                                    HashMap<Integer, String> movieTitles,
                                                    int K) {
        // Kullanıcının puanlarını büyükten küçüğe sırala
        List<Map.Entry<Integer, Integer>> entries =
                new ArrayList<>(user.getRatings().entrySet());

        entries.sort(new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> a,
                               Map.Entry<Integer, Integer> b) {
                int byRating = Integer.compare(b.getValue(), a.getValue());
                if (byRating != 0) return byRating;
                return Integer.compare(a.getKey(), b.getKey());
            }
        });

        List<String> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : entries) {
            String title = movieTitles.get(entry.getKey());
            // movies.csv'de olmayan ID'leri sessizce atla
            if (title != null && !title.trim().isEmpty()) {
                result.add(title);
                if (result.size() == K) break;
            }
        }
        return result;
    }
}
