/*
 * BÀI 29: METHOD REFERENCES — 4 dạng tham chiếu method, khi nào dùng
 * ================================================================
 * WHAT  : Method reference (::) = cách viết gọn lambda khi lambda CHỈ gọi 1 method.
 * WHY   : Ngắn, rõ ý định ("dùng method này"), giảm nhiễu so với lambda dài.
 * HOW   : Cùng cơ chế lambda (invokedynamic + LambdaMetafactory); chỉ là cú pháp.
 * WHEN  : Khi lambda thân chỉ là một lời gọi method/khởi tạo, không thêm logic.
 * WHICH : 4 dạng: Static / Instance-của-object-cụ-thể / Instance-của-đối-số / Constructor.
 */
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {

        // === Dạng 1: Tham chiếu STATIC method -> ClassName::staticMethod ===
        // lambda: s -> Integer.parseInt(s)  ≡  Integer::parseInt
        Function<String, Integer> parse = Integer::parseInt;
        System.out.println("Dạng 1 (static): " + parse.apply("123"));

        // === Dạng 2: Instance method của một OBJECT CỤ THỂ -> object::method ===
        // lambda: s -> System.out.println(s)  ≡  System.out::println
        Consumer<String> printer = System.out::println;
        printer.accept("Dạng 2 (instance object cụ thể): in qua System.out::println");

        String prefix = "Hello ";
        // bắt object prefix, gọi prefix.concat(x):
        Function<String, String> greeter = prefix::concat;
        System.out.println("Dạng 2: " + greeter.apply("World"));

        // === Dạng 3: Instance method của ĐỐI SỐ ĐẦU TIÊN -> ClassName::instanceMethod ===
        // ⚙️ Khác dạng 2: object NHẬN method chính là tham số được truyền vào lúc gọi.
        // lambda: s -> s.toUpperCase()  ≡  String::toUpperCase
        List<String> words = new ArrayList<>(List.of("chuối", "táo", "kiwi"));
        words.replaceAll(String::toUpperCase);            // mỗi phần tử .toUpperCase()
        System.out.println("Dạng 3 (instance của đối số): " + words);

        // sort dùng method reference cho comparator:
        words.sort(String::compareTo);                    // (a,b) -> a.compareTo(b)
        System.out.println("Dạng 3 sort: " + words);

        // === Dạng 4: CONSTRUCTOR reference -> ClassName::new ===
        // lambda: () -> new ArrayList<>()  ≡  ArrayList::new
        Supplier<List<String>> listMaker = ArrayList::new;
        List<String> fresh = listMaker.get();
        fresh.add("tạo qua constructor reference");
        System.out.println("Dạng 4 (constructor): " + fresh);

        // Constructor reference có tham số: n -> new int[n] tạo mảng
        Function<Integer, int[]> arrayMaker = int[]::new;
        System.out.println("Dạng 4 mảng: độ dài = " + arrayMaker.apply(5).length);

        // === Ứng dụng thực tế trong Stream: gọn & rõ ===
        String joined = Stream.of("a", "b", "c")
                .map(String::toUpperCase)        // dạng 3
                .collect(Collectors.joining(","));
        System.out.println("Stream + method ref: " + joined);

        // Constructor reference để gom vào kiểu cụ thể:
        TreeSet<Integer> sorted = Stream.of(3, 1, 2)
                .collect(Collectors.toCollection(TreeSet::new));   // dạng 4
        System.out.println("toCollection(TreeSet::new): " + sorted);
    }
}
