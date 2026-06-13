/*
 * BÀI 27: LAMBDA EXPRESSIONS — Hàm như giá trị, capture, invokedynamic
 * ================================================================
 * WHAT  : Lambda = một hàm vô danh viết gọn, là cài đặt của functional interface.
 * WHY   : Truyền HÀNH VI như tham số -> code khai báo (declarative), gọn hơn
 *         anonymous class; nền của Stream/functional Java.
 * HOW   : Lambda KHÔNG biên dịch thành class riêng như anonymous; nó dùng
 *         invokedynamic + LambdaMetafactory -> sinh cài đặt lúc runtime (rẻ hơn).
 * WHEN  : Khi cần truyền một mẩu logic ngắn (callback, comparator, mapper...).
 * WHICH : Lambda (functional interface, this = ngoài) vs anonymous class (this = chính nó).
 */
import java.util.*;
import java.util.function.*;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: Lambda là cài đặt của functional interface ===
        // Trước Java 8 (anonymous class):
        Runnable oldWay = new Runnable() {
            @Override public void run() { System.out.println("Anonymous class"); }
        };
        // Java 8+ (lambda) — cùng ý nghĩa, gọn hơn:
        Runnable newWay = () -> System.out.println("Lambda");
        oldWay.run();
        newWay.run();

        // === Phần 2: Lambda truyền hành vi như tham số ===
        List<String> words = new ArrayList<>(List.of("chuối", "táo", "kiwi", "cam"));
        // Comparator là functional interface -> lambda điền compare().
        words.sort((a, b) -> a.length() - b.length());      // sắp theo độ dài
        System.out.println("Sort theo độ dài: " + words);

        // === Phần 3: Capture biến — "closure" ===
        // ⚙️ Lambda capture biến local PHẢI effectively final (giống anonymous class).
        int threshold = 3;
        Predicate<String> isShort = s -> s.length() <= threshold;  // bắt biến threshold
        System.out.println("'cam' ngắn? " + isShort.test("cam"));

        // === Phần 4: this trong lambda KHÁC anonymous class ===
        new Main().demoThis();

        // === Phần 5: Lambda với các functional interface chuẩn ===
        Function<Integer, Integer> square = x -> x * x;
        Supplier<String> greet = () -> "Xin chào";
        Consumer<String> printer = System.out::println;
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

        System.out.println("square(5) = " + square.apply(5));
        System.out.println("supplier = " + greet.get());
        printer.accept("consumer in dòng này");
        System.out.println("add(3,4) = " + add.apply(3, 4));

        // Ghép hàm: andThen / compose
        Function<Integer, Integer> plus1 = x -> x + 1;
        System.out.println("square.andThen(plus1)(5) = " + square.andThen(plus1).apply(5)); // 26
        System.out.println("square.compose(plus1)(5) = " + square.compose(plus1).apply(5)); // 36
    }

    private String name = "instance-Main";
    void demoThis() {
        // ⚙️ Trong lambda, `this` trỏ tới object Main (enclosing) -> đọc được name.
        Runnable r = () -> System.out.println("Lambda this.name = " + this.name);
        r.run();
    }
}
