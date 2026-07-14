package model;

import java.util.HashMap;

public class User {
    private int userId;
    private HashMap<Integer, Integer> ratings;

    public User(int userId, HashMap<Integer, Integer> ratings) {
        this.userId = userId;
        this.ratings = ratings;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public HashMap<Integer, Integer> getRatings() { return ratings; }
    public void setRatings(HashMap<Integer, Integer> ratings) { this.ratings = ratings; }

    @Override
    public String toString() {
        return "User " + userId;
    }
}
