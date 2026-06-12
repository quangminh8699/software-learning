/*
 * BÀI 25: ITERATOR & COMPARATOR — Duyệt trừu tượng & sắp xếp tuỳ biến
 * ================================================================
 * WHAT  : Iterator = giao thức duyệt phần tử không lộ cấu trúc nội bộ.
 *         Comparable = thứ tự "tự nhiên"; Comparator = thứ tự "bên ngoài", tuỳ biến.
 * WHY   : for-each chạy được nhờ Iterator (Iterable). Comparator cho phép sắp xếp
 *         theo nhiều tiêu chí mà không sửa class -> linh hoạt.
 * HOW   : for-each biên dịch thành iterator().hasNext()/next(). sort() dùng
 *         compareTo/compare để quyết thứ tự (TimSort - ổn định, O(n log n)).
 * WHEN  : Iterator để duyệt bất kỳ nguồn nào; Comparator khi cần sắp xếp đa tiêu chí.
 * WHICH : Comparable (1 thứ tự tự nhiên, sửa class) vs Comparator (N thứ tự, tách rời).
 */
import java.util.*;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: Iterator tường minh + xoá an toàn ===
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> it = nums.iterator();
        while (it.hasNext()) {
            int v = it.next();
            if (v % 2 == 0) it.remove();      // ⚙️ Iterator.remove an toàn (cập nhật modCount)
        }
        System.out.println("Sau xoá chẵn qua Iterator: " + nums);

        // === Phần 2: Iterable tự định nghĩa -> dùng được for-each ===
        Range range = new Range(1, 4);
        // ⚙️ for-each gọi range.iterator() -> hasNext()/next() do ta định nghĩa.
        StringBuilder sb = new StringBuilder();
        for (int x : range) sb.append(x).append(' ');
        System.out.println("for-each trên Iterable tự viết: " + sb.toString().trim());

        // === Phần 3: Comparable — thứ tự tự nhiên ===
        List<Person> people = new ArrayList<>(List.of(
                new Person("An", 30), new Person("Bình", 25), new Person("Cường", 30)));
        Collections.sort(people);     // dùng compareTo (theo tuổi) -> thứ tự "tự nhiên"
        System.out.println("Sort tự nhiên (theo tuổi): " + people);

        // === Phần 4: Comparator — thứ tự tuỳ biến, ghép nhiều tiêu chí ===
        // ⚙️ thenComparing: tuổi tăng dần, cùng tuổi thì theo tên; có thể đảo chiều.
        people.sort(Comparator.comparingInt((Person p) -> p.age)
                              .thenComparing(p -> p.name));
        System.out.println("Sort theo tuổi rồi tên: " + people);

        people.sort(Comparator.comparing((Person p) -> p.name).reversed());
        System.out.println("Sort theo tên giảm dần: " + people);

        // Comparator còn dùng cho TreeMap/PriorityQueue:
        PriorityQueue<Person> pq = new PriorityQueue<>(Comparator.comparingInt(p -> p.age));
        pq.addAll(people);
        System.out.println("Trẻ nhất (đỉnh heap): " + pq.peek());
    }
}

// Iterable tự định nghĩa: phải cung cấp iterator().
class Range implements Iterable<Integer> {
    private final int from, toExclusive;
    Range(int from, int toExclusive) { this.from = from; this.toExclusive = toExclusive; }

    @Override public Iterator<Integer> iterator() {
        return new Iterator<>() {            // anonymous Iterator
            private int cur = from;
            @Override public boolean hasNext() { return cur < toExclusive; }
            @Override public Integer next() { return cur++; }
        };
    }
}

class Person implements Comparable<Person> {
    String name; int age;
    Person(String name, int age) { this.name = name; this.age = age; }
    // thứ tự tự nhiên = theo tuổi.
    @Override public int compareTo(Person o) { return Integer.compare(this.age, o.age); }
    @Override public String toString() { return name + "(" + age + ")"; }
}
