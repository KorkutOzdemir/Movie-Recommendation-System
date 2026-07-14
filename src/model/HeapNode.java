package model;

public class HeapNode {
    public int userId;
    public double similarity;
    public HeapNode left;
    public HeapNode right;
    public HeapNode parent;

    public HeapNode(int userId, double similarity) {
        this.userId = userId;
        this.similarity = similarity;
    }
}
