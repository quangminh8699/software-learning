/*
 * BÀI 24: COLLECTIONS — Set & Map: HashMap nội tại, hashCode/equals, ordering
 * ================================================================
 * WHAT  : Set (không trùng) & Map (key->value). Cài đặt: Hash*, Tree*, Linked*.
 * WHY   : HashMap là cấu trúc dùng nhiều nhất; hiểu bên trong (bucket, hash,
 *         treeify) để dùng đúng + viết hashCode/equals chuẩn.
 * HOW   : HashMap = mảng bucket; key -> hash -> index bucket. Va chạm -> danh sách
 *         liên kết, đủ dài (>=8) -> chuyển CÂY ĐỎ-ĐEN (O(log n)). Cần equals+hashCode.
 * WHEN  : HashMap khi cần tra cứu O(1); TreeMap khi cần SẮP XẾP theo key;
 *         LinkedHashMap khi cần giữ THỨ TỰ chèn / LRU.
 * WHICH : HashMap (nhanh, vô thứ tự) / TreeMap (sorted, O(log n)) / LinkedHashMap (có thứ tự).
 */
import java.util.*;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: Set — khử trùng lặp ===
        Set<String> set = new HashSet<>(List.of("a", "b", "a", "c", "b"));
        System.out.println("HashSet khử trùng: " + set + " (vô thứ tự)");
        System.out.println("TreeSet sorted: " + new TreeSet<>(set));            // sắp xếp
        System.out.println("LinkedHashSet giữ thứ tự chèn: "
                + new LinkedHashSet<>(List.of("z", "a", "m")));

        // === Phần 2: HashMap — tra cứu O(1) trung bình ===
        Map<String, Integer> stock = new HashMap<>();
        stock.put("táo", 10);
        stock.put("cam", 5);
        stock.merge("táo", 3, Integer::sum);     // cập nhật gọn: táo += 3
        System.out.println("stock = " + stock);
        System.out.println("getOrDefault('lê',0) = " + stock.getOrDefault("lê", 0));
        stock.computeIfAbsent("nho", k -> 0);    // chỉ đặt nếu chưa có
        System.out.println("sau computeIfAbsent: " + stock);

        // === Phần 3: Đếm tần suất — pattern kinh điển với merge ===
        String text = "a b a c a b";
        Map<String, Integer> freq = new HashMap<>();
        for (String w : text.split(" ")) freq.merge(w, 1, Integer::sum);
        System.out.println("Tần suất: " + freq);

        // === Phần 4: hashCode/equals — HỢP ĐỒNG bắt buộc khi làm KEY ===
        // ⚙️ Hai object "bằng nhau" PHẢI có cùng hashCode, nếu không HashMap "mất" key.
        Map<PointBad, String> bad = new HashMap<>();
        bad.put(new PointBad(1, 2), "A");
        System.out.println("PointBad tra lại (thiếu equals/hashCode): "
                + bad.get(new PointBad(1, 2)));     // null! vì object khác địa chỉ

        Map<PointGood, String> good = new HashMap<>();
        good.put(new PointGood(1, 2), "A");
        System.out.println("PointGood tra lại (có equals/hashCode): "
                + good.get(new PointGood(1, 2)));   // "A" đúng

        // === Phần 5: TreeMap — sắp xếp theo key + truy vấn khoảng ===
        TreeMap<Integer, String> tm = new TreeMap<>();
        tm.put(3, "ba"); tm.put(1, "một"); tm.put(2, "hai");
        System.out.println("TreeMap sorted: " + tm);
        System.out.println("floorKey(2)=" + tm.floorKey(2) + ", ceilingKey(2)=" + tm.ceilingKey(2));
    }
}

// ❌ Thiếu equals/hashCode -> dùng mặc định Object (theo địa chỉ) -> hỏng khi làm key.
class PointBad {
    int x, y;
    PointBad(int x, int y) { this.x = x; this.y = y; }
}

// ✅ Đúng hợp đồng: equals & hashCode nhất quán theo cùng tập field.
class PointGood {
    int x, y;
    PointGood(int x, int y) { this.x = x; this.y = y; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointGood p)) return false;
        return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
}
