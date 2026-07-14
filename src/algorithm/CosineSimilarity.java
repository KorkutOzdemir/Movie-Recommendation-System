package algorithm;

import java.util.HashMap;
import java.util.Map;

public class CosineSimilarity {
    public static double calculate(HashMap<Integer, Integer> a, HashMap<Integer, Integer> b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Map.Entry<Integer, Integer> e : a.entrySet()) {
            int movieId = e.getKey();
            int ratingA = e.getValue();
            if (b.containsKey(movieId)) {
                dotProduct += ratingA * b.get(movieId);
            }
            normA += ratingA * ratingA;
        }

        for (int ratingB : b.values()) {
            normB += ratingB * ratingB;
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
