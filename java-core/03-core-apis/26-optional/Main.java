/*
 * BÀI 26: OPTIONAL — Mô hình hoá "có thể vắng giá trị" thay cho null
 * ================================================================
 * WHAT  : Optional<T> = hộp chứa 0-hoặc-1 giá trị, buộc người gọi xử lý trường hợp rỗng.
 * WHY   : Làm "khả năng vắng mặt" HIỆN RÕ trong kiểu trả về -> giảm NPE,
 *         tự tài liệu hoá API. Thay vì trả null lén lút.
 * HOW   : Optional bọc giá trị; map/filter/flatMap xử lý theo chuỗi mà không if-null.
 *         Là object -> có chi phí cấp phát nhỏ; không nên lạm dụng ở field/hot path.
 * WHEN  : Dùng cho KIỂU TRẢ VỀ của method có thể không có kết quả (find...).
 * WHICH : Optional (API trả về) vs null (nội bộ/hiệu năng) — KHÔNG dùng Optional cho field.
 */
import java.util.*;

public class Main {
    public static void main(String[] args) {

        Map<String, String> users = Map.of("u1", "Alice", "u2", "Bob");

        // === Phần 1: Tạo & tiêu thụ Optional ===
        Optional<String> found = findUser(users, "u1");
        Optional<String> missing = findUser(users, "u9");

        // ❌ Anti-pattern: get() khi chưa chắc có -> NoSuchElementException.
        // ✅ Dùng orElse / ifPresent / map thay vì isPresent + get.
        System.out.println("u1 -> " + found.orElse("KHÔNG CÓ"));
        System.out.println("u9 -> " + missing.orElse("KHÔNG CÓ"));

        // === Phần 2: map / filter / flatMap — xử lý chuỗi an toàn ===
        // ⚙️ Nếu rỗng, mọi bước map/filter bị BỎ QUA -> không NPE, không if lồng nhau.
        String upper = findUser(users, "u2")
                .map(String::toUpperCase)              // chỉ chạy nếu có giá trị
                .filter(s -> s.startsWith("B"))
                .orElse("(không khớp)");
        System.out.println("u2 -> map+filter -> " + upper);

        // === Phần 3: orElseGet vs orElse — lazy vs eager ===
        // ⚙️ orElse LUÔN tính tham số (eager); orElseGet chỉ chạy supplier KHI rỗng (lazy).
        String v1 = found.orElse(expensiveDefault());      // expensiveDefault() VẪN chạy dù không cần
        String v2 = found.orElseGet(Main::expensiveDefault); // KHÔNG chạy vì found có giá trị
        System.out.println("v1=" + v1 + ", v2=" + v2);

        // === Phần 4: orElseThrow — biến "rỗng" thành exception rõ ràng ===
        try {
            findUser(users, "u9").orElseThrow(() -> new NoSuchElementException("user u9 không tồn tại"));
        } catch (NoSuchElementException e) {
            System.out.println("orElseThrow: " + e.getMessage());
        }

        // === Phần 5: ifPresentOrElse (Java 9+) ===
        findUser(users, "u1").ifPresentOrElse(
                u -> System.out.println("Chào " + u),
                () -> System.out.println("Không tìm thấy user"));

        // === Phần 6: Optional cho primitive -> tránh box ===
        OptionalInt oi = IntSummaryDemo();
        System.out.println("OptionalInt: " + oi.orElse(-1));
    }

    // Trả Optional thay vì null -> caller BUỘC xử lý trường hợp vắng.
    static Optional<String> findUser(Map<String, String> users, String id) {
        return Optional.ofNullable(users.get(id));   // ofNullable: null -> Optional.empty()
    }

    static String expensiveDefault() {
        System.out.println("  (expensiveDefault() được gọi!)");
        return "default-đắt";
    }

    static OptionalInt IntSummaryDemo() {
        return Arrays.stream(new int[]{3, 7, 2}).max();   // có thể rỗng nếu mảng trống
    }
}
