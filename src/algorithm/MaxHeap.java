package algorithm;

import model.HeapNode;

/**
 * Node tabanlı Max-Heap implementasyonu.
 *
 * TASARIM KARARLARI:
 *   - Dizi (array) KULLANILMAZ. Ödev şartı gereği tüm operasyonlar
 *     HeapNode referansları (left, right, parent) üzerinden çalışır.
 *   - Heap özellikleri: her node, çocuklarından büyük ya da eşit similarity değeri taşır.
 *   - Tam ikili ağaç (complete binary tree) özelliği her zaman korunur.
 *
 * POZİSYON HESAPLAMA (temel algoritma):
 *   Complete binary tree'de N numaralı node'a ulaşmak için N'i binary'e yaz,
 *   en soldaki 1'i (MSB) atla; kalan her bit yönü verir: 0=sol, 1=sağ.
 *   Örnek: pos=6 → binary "110" → MSB atla → "10" → sağ→sol
 *
 * ZAMAN KARMAŞıKLIĞI:
 *   insert()      : O(log n)
 *   extractMax()  : O(log n)
 *   peek()        : O(1)
 *   getSize()     : O(1)
 */
public class MaxHeap {

    private HeapNode root;
    private int size;

    // -------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------

    /**
     * Heap'e yeni bir (userId, similarity) çifti ekler.
     * Tam ikili ağacın son pozisyonuna eklendikten sonra
     * heapifyUp ile heap özelliği yeniden sağlanır.
     *
     * @param userId     kullanıcı kimliği
     * @param similarity cosine similarity değeri [0.0, 1.0]
     */
    public void insert(int userId, double similarity) {
        HeapNode newNode = new HeapNode(userId, similarity);

        if (root == null) {
            root = newNode;
            size = 1;
            return;
        }

        // Yeni node'un parent'ını bul (bit manipülasyonu ile)
        HeapNode parent = findParentForNewNode();
        newNode.parent = parent;

        // Son bit: 0 → sol çocuk, 1 → sağ çocuk
        String binary = Integer.toBinaryString(size + 1);
        if (binary.charAt(binary.length() - 1) == '0') {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        size++;
        heapifyUp(newNode);
    }

    /**
     * Heap'teki maksimum elemanı (root) çıkarır ve döndürür.
     * Son node root'a taşınır, son node silinir, ardından
     * heapifyDown ile heap özelliği yeniden sağlanır.
     *
     * @return en yüksek similarity'ye sahip HeapNode, heap boşsa null
     */
    public HeapNode extractMax() {
        if (root == null) return null;

        // Root'un kopyasını sakla (döndürülecek değer)
        HeapNode maxCopy = new HeapNode(root.userId, root.similarity);

        if (size == 1) {
            root = null;
            size = 0;
            return maxCopy;
        }

        // Son node'u bul ve verilerini root'a taşı
        HeapNode lastNode = getNodeAtPosition(size);
        root.userId     = lastNode.userId;
        root.similarity = lastNode.similarity;

        // Son node'u ağaçtan kopar
        HeapNode parent = lastNode.parent;
        if (parent.left == lastNode) {
            parent.left = null;
        } else {
            parent.right = null;
        }

        size--;
        heapifyDown(root);
        return maxCopy;
    }

    /**
     * Root node'u (maksimum eleman) heap'ten çıkarmadan döndürür.
     *
     * @return en yüksek similarity'ye sahip HeapNode, heap boşsa null
     */
    public HeapNode peek() {
        return root;
    }

    /** @return heap'teki eleman sayısı */
    public int getSize() {
        return size;
    }

    /** @return heap boş mu */
    public boolean isEmpty() {
        return size == 0;
    }

    // -------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------

    /**
     * Yeni eklenecek node'un parent'ını bulur.
     * Yeni node size+1. pozisyona gidecek; bu pozisyonun
     * binary gösteriminin son biti yön, geri kalanlar yol verir.
     */
    private HeapNode findParentForNewNode() {
        int pos = size + 1;
        String binary = Integer.toBinaryString(pos);
        HeapNode current = root;
        // MSB (index 0) atlandı, son bit atlandı → parent'a ulaşılan yol
        for (int i = 1; i < binary.length() - 1; i++) {
            current = (binary.charAt(i) == '0') ? current.left : current.right;
        }
        return current;
    }

    /**
     * Heap içindeki pos numaralı node'u döndürür (1-tabanlı).
     * Bit manipülasyonu: binary(pos) → MSB atla → her bit sol/sağ.
     */
    private HeapNode getNodeAtPosition(int pos) {
        if (pos <= 0 || pos > size) return null;
        String binary = Integer.toBinaryString(pos);
        HeapNode current = root;
        for (int i = 1; i < binary.length(); i++) {
            current = (binary.charAt(i) == '0') ? current.left : current.right;
        }
        return current;
    }

    /**
     * Verilen node'u parent'ıyla karşılaştırarak yukarı taşır.
     * Node'un similarity'si parent'tan büyük olduğu sürece devam eder.
     */
    private void heapifyUp(HeapNode node) {
        while (node.parent != null
                && node.similarity > node.parent.similarity) {
            swapData(node, node.parent);
            node = node.parent;
        }
    }

    /**
     * Verilen node'u çocuklarıyla karşılaştırarak aşağı taşır.
     * Heap özelliği sağlanana (node en büyük olana) kadar devam eder.
     */
    private void heapifyDown(HeapNode node) {
        while (node != null) {
            HeapNode largest = node;

            if (node.left  != null && node.left.similarity  > largest.similarity) largest = node.left;
            if (node.right != null && node.right.similarity > largest.similarity) largest = node.right;

            if (largest == node) break; // heap özelliği sağlandı

            swapData(node, largest);
            node = largest;
        }
    }

    /**
     * İki node'un userId ve similarity alanlarını değiş tokuş eder.
     * Node referansları değişmez; yalnızca içerik taşınır.
     */
    private void swapData(HeapNode a, HeapNode b) {
        int    tempUserId     = a.userId;
        double tempSimilarity = a.similarity;
        a.userId     = b.userId;
        a.similarity = b.similarity;
        b.userId     = tempUserId;
        b.similarity = tempSimilarity;
    }
}
