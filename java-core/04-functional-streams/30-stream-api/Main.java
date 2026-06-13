/*
 * BÀI 30: STREAM API — Pipeline xử lý dữ liệu khai báo, lazy, song song
 * ================================================================
 * WHAT  : Stream = chuỗi xử lý dữ liệu gồm: nguồn -> các phép TRUNG GIAN (lazy)
 *         -> một phép KẾT THÚC (terminal) kích hoạt tính toán.
 * WHY   : Viết xử lý dữ liệu KHAI BÁO (cái gì, không phải vòng lặp thế nào);
 *         dễ đọc, dễ song song hoá; tránh biến trung gian.
 * HOW   : Phép trung gian (map/filter) KHÔNG chạy ngay -> chỉ ghi nhận; terminal
 *         (collect/reduce) mới "kéo" dữ liệu qua pipeline 1 LẦN (lazy + fusion).
 * WHEN  : Biến đổi/lọc/gom tập hợp; không dùng khi cần side-effect phức tạp/hiệu năng cực hạn.
 * WHICH : stream tuần tự vs parallelStream (chỉ khi data lớn + thao tác độc lập + đo đạc).
 */
import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) {

        List<String> names = List.of("An", "Bình", "Cường", "Dũng", "An", "Bình");

        // === Phần 1: Pipeline cơ bản: filter -> map -> collect ===
        List<String> result = names.stream()
                .distinct()                       // bỏ trùng (trung gian, lazy)
                .filter(s -> s.length() >= 4)     // lọc (lazy)
                .map(String::toUpperCase)         // biến đổi (lazy)
                .sorted()                         // sắp xếp
                .collect(Collectors.toList());    // TERMINAL -> giờ mới chạy
        System.out.println("Pipeline: " + result);

        // === Phần 2: LAZY + short-circuit — chứng minh chỉ chạy khi cần ===
        // ⚙️ peek in ra để thấy phần tử nào THỰC SỰ chảy qua pipeline.
        System.out.println("--- Lazy: findFirst dừng sớm ---");
        Optional<Integer> first = Stream.of(1, 2, 3, 4, 5)
                .peek(n -> System.out.println("  xét " + n))   // chỉ in tới khi tìm thấy
                .filter(n -> n % 2 == 0)
                .findFirst();                                   // short-circuit
        System.out.println("findFirst chẵn = " + first.get() + " (không xét hết 5 phần tử)");

        // === Phần 3: reduce — gấp dữ liệu về 1 giá trị ===
        int sum = Stream.of(1, 2, 3, 4, 5).reduce(0, Integer::sum);
        System.out.println("reduce sum = " + sum);

        // === Phần 4: Stream primitive -> tránh autobox + có thống kê sẵn ===
        IntSummaryStatistics stats = IntStream.rangeClosed(1, 10).summaryStatistics();
        System.out.printf("IntStream stats: sum=%d, avg=%.1f, max=%d%n",
                stats.getSum(), stats.getAverage(), stats.getMax());

        // === Phần 5: Collectors mạnh — groupingBy, partitioningBy, joining ===
        // groupingBy: gom theo độ dài tên.
        Map<Integer, List<String>> byLen = names.stream()
                .distinct()
                .collect(Collectors.groupingBy(String::length));
        System.out.println("groupingBy độ dài: " + byLen);

        // groupingBy + downstream counting:
        Map<Integer, Long> countByLen = names.stream()
                .collect(Collectors.groupingBy(String::length, Collectors.counting()));
        System.out.println("đếm theo độ dài (gồm trùng): " + countByLen);

        // partitioningBy: chia 2 nhóm theo predicate.
        Map<Boolean, List<Integer>> parts = IntStream.rangeClosed(1, 6).boxed()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println("partition chẵn/lẻ: " + parts);

        // joining: nối chuỗi.
        String csv = names.stream().distinct().collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining: " + csv);

        // === Phần 6: flatMap — làm phẳng stream-của-stream ===
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
        List<Integer> flat = nested.stream()
                .flatMap(List::stream)        // mỗi list -> stream, rồi gộp phẳng
                .collect(Collectors.toList());
        System.out.println("flatMap: " + flat);

        // === Phần 7: parallelStream — minh hoạ (cẩn thận!) ===
        long count = IntStream.rangeClosed(1, 1_000_000).parallel()
                .filter(n -> n % 2 == 0).count();
        System.out.println("parallel count chẵn (1..1tr): " + count);
    }
}
