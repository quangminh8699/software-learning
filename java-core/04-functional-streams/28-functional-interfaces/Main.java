/*
 * BÀI 28: FUNCTIONAL INTERFACES — Predicate, Function, Consumer, Supplier...
 * ================================================================
 * WHAT  : Functional interface = interface đúng MỘT abstract method (SAM).
 *         java.util.function cung cấp bộ chuẩn để lambda gắn vào.
 * WHY   : Là "kiểu" của lambda/method reference; chuẩn hoá để API (Stream...) nhận
 *         hành vi. Hiểu rõ để chọn đúng interface + tránh autobox.
 * HOW   : @FunctionalInterface đảm bảo đúng 1 SAM (compiler kiểm). Biến thể primitive
 *         (IntPredicate...) tránh box. default method để ghép (and/or/andThen).
 * WHEN  : Khi thiết kế API nhận callback/strategy; khi dùng Stream.
 * WHICH : Predicate (test->bool), Function (map T->R), Consumer (nhận, void),
 *         Supplier (không nhận, sinh ra), UnaryOperator/BinaryOperator (cùng kiểu).
 */
import java.util.*;
import java.util.function.*;

public class Main {
    public static void main(String[] args) {

        // === Bộ tứ cốt lõi ===
        Predicate<Integer> isEven = n -> n % 2 == 0;       // T -> boolean
        Function<String, Integer> length = String::length; // T -> R
        Consumer<String> log = s -> System.out.println("LOG: " + s); // T -> void
        Supplier<Double> random = Math::random;            // () -> T

        System.out.println("isEven(4): " + isEven.test(4));
        System.out.println("length(\"java\"): " + length.apply("java"));
        log.accept("một thông điệp");
        System.out.println("supplier random < 1: " + (random.get() < 1));

        // === Ghép Predicate: and / or / negate ===
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> evenAndPositive = isEven.and(isPositive);
        System.out.println("6 even&positive? " + evenAndPositive.test(6));
        System.out.println("-2 even? " + isEven.test(-2) + ", even&positive? " + evenAndPositive.test(-2));
        System.out.println("3 NOT even? " + isEven.negate().test(3));

        // === BiFunction & các interface 2 tham số ===
        BiFunction<Integer, Integer, Integer> mul = (a, b) -> a * b;
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Comparator.naturalOrder());
        System.out.println("mul(3,4)=" + mul.apply(3, 4) + ", max(3,4)=" + max.apply(3, 4));

        // === UnaryOperator: T -> T (Function với cùng kiểu) ===
        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
        System.out.println(shout.apply("hello"));

        // === Biến thể PRIMITIVE -> tránh autoboxing ===
        // ⚙️ IntPredicate nhận int trực tiếp -> KHÔNG box thành Integer như Predicate<Integer>.
        IntPredicate isOddPrim = n -> n % 2 != 0;   // không box
        System.out.println("IntPredicate 7 lẻ? " + isOddPrim.test(7));
        IntUnaryOperator inc = n -> n + 1;          // int -> int, không box
        System.out.println("IntUnaryOperator inc(9)=" + inc.applyAsInt(9));

        // === Dùng functional interface làm STRATEGY tham số ===
        System.out.println("Lọc chẵn: " + filter(List.of(1,2,3,4,5,6), isEven));
        System.out.println("Lọc >3 : " + filter(List.of(1,2,3,4,5,6), n -> n > 3));

        // === Functional interface TỰ ĐỊNH NGHĨA ===
        TriFunction<Integer,Integer,Integer,Integer> sum3 = (a,b,c) -> a+b+c;
        System.out.println("sum3(1,2,3)=" + sum3.apply(1,2,3));
    }

    // Nhận Predicate như "chiến lược lọc" -> tách logic lọc khỏi vòng lặp.
    static <T> List<T> filter(List<T> list, Predicate<T> pred) {
        List<T> out = new ArrayList<>();
        for (T x : list) if (pred.test(x)) out.add(x);
        return out;
    }

    // Tự định nghĩa functional interface (JDK không có TriFunction).
    @FunctionalInterface
    interface TriFunction<A, B, C, R> { R apply(A a, B b, C c); }
}
