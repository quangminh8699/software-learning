/*
 * BÀI 22: GENERICS — Generic class/method, bounded, wildcard, type erasure
 * ================================================================
 * WHAT  : Tham số hoá kiểu (List<T>) -> an toàn kiểu lúc COMPILE, không cần cast.
 * WHY   : Bắt lỗi kiểu sớm; tái dùng code cho nhiều kiểu; bỏ cast thủ công.
 * HOW   : TYPE ERASURE — generic chỉ tồn tại lúc compile; runtime bị "xoá" về
 *         kiểu thô (Object hoặc bound). JVM không biết T là gì lúc chạy.
 * WHEN  : Khi viết container/algorithm dùng được cho nhiều kiểu mà vẫn an toàn.
 * WHICH : extends (đọc - producer) vs super (ghi - consumer) — quy tắc PECS.
 */
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: Generic class — an toàn kiểu, không cần cast ===
        Box<String> sBox = new Box<>("hello");
        String s = sBox.get();              // KHÔNG cần (String) cast
        System.out.println("Box<String>: " + s.toUpperCase());

        Box<Integer> iBox = new Box<>(42);
        System.out.println("Box<Integer>: " + (iBox.get() + 1));

        // === Phần 2: Generic method + bounded type (T extends ...) ===
        // ⚙️ <T extends Comparable<T>> ép T phải so sánh được -> gọi compareTo an toàn.
        System.out.println("max(3,7) = " + max(3, 7));
        System.out.println("max(\"a\",\"z\") = " + max("a", "z"));

        // === Phần 3: Wildcard — PECS: Producer Extends, Consumer Super ===
        List<Integer> ints = List.of(1, 2, 3);
        List<Double> dbls = List.of(1.5, 2.5);
        // ? extends Number -> ĐỌC (producer): chấp nhận List của bất kỳ con Number nào.
        System.out.println("sum ints = " + sumOfList(ints));
        System.out.println("sum dbls = " + sumOfList(dbls));

        // ? super Integer -> GHI (consumer): nơi nhận Integer (Integer hoặc cha của nó).
        List<Number> sink = new ArrayList<>();
        addNumbers(sink);
        System.out.println("sink sau khi ghi = " + sink);

        // === Phần 4: TYPE ERASURE — bằng chứng generic bị xoá lúc runtime ===
        List<String> ls = new ArrayList<>();
        List<Integer> li = new ArrayList<>();
        // ⚙️ Runtime cả hai đều là ArrayList "thô" -> getClass() GIỐNG nhau.
        System.out.println("ls.getClass() == li.getClass() ? "
                + (ls.getClass() == li.getClass()));   // true — kiểu T đã bị erasure
    }

    // Generic method với bounded type parameter.
    static <T extends Comparable<T>> T max(T a, T b) {
        return (a.compareTo(b) >= 0) ? a : b;
    }

    // Producer: ? extends Number -> chỉ ĐỌC ra Number (không add được, trừ null).
    static double sumOfList(List<? extends Number> list) {
        double sum = 0;
        for (Number n : list) sum += n.doubleValue();
        return sum;
    }

    // Consumer: ? super Integer -> ĐƯỢC add Integer vào.
    static void addNumbers(List<? super Integer> list) {
        for (int i = 1; i <= 3; i++) list.add(i);
    }
}

// Generic class: T là tham số kiểu, quyết khi tạo object.
class Box<T> {
    private final T value;        // runtime: field này thực ra kiểu Object (erasure)
    Box(T value) { this.value = value; }
    T get() { return value; }
}
