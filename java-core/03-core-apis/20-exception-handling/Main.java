/*
 * BÀI 20: EXCEPTION HANDLING — checked/unchecked, try-with-resources, chi phí
 * ================================================================
 * WHAT  : Cơ chế báo & xử lý lỗi: try/catch/finally, throw/throws, custom exception,
 *         try-with-resources, exception chaining.
 * WHY   : Tách luồng lỗi khỏi luồng chính; ép xử lý lỗi quan trọng (checked);
 *         dọn tài nguyên an toàn (close) dù có lỗi.
 * HOW   : Khi throw, JVM "thả ngược" stack tìm catch khớp (stack unwinding),
 *         pop từng frame. Tạo exception phải chụp stack trace -> ĐẮT.
 * WHEN  : Lỗi ngoại lệ thật sự, không dùng cho luồng điều khiển thông thường.
 * WHICH : checked (lỗi khôi phục được, ép xử lý) vs unchecked (lỗi lập trình).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: try/catch/finally + thứ tự catch (cụ thể -> tổng quát) ===
        try {
            int[] a = new int[2];
            a[5] = 1;                       // ném ArrayIndexOutOfBoundsException
        } catch (ArrayIndexOutOfBoundsException e) {   // catch cụ thể TRƯỚC
            System.out.println("Bắt cụ thể: " + e.getMessage());
        } catch (RuntimeException e) {                  // tổng quát SAU
            System.out.println("Bắt tổng quát: " + e);
        } finally {
            // ⚙️ finally LUÔN chạy (kể cả có return/throw) -> nơi dọn dẹp truyền thống.
            System.out.println("finally: luôn chạy");
        }

        // === Phần 2: try-with-resources — tự đóng tài nguyên (AutoCloseable) ===
        // ⚙️ Dưới nắp capo: compiler sinh finally ẩn gọi close() theo thứ tự NGƯỢC,
        //    và nén lỗi phụ vào "suppressed" nếu cả body lẫn close đều lỗi.
        try (Resource r1 = new Resource("DB");
             Resource r2 = new Resource("File")) {
            r1.use(); r2.use();
        } // r2.close() rồi r1.close() tự động ở đây

        // === Phần 3: Exception chaining — giữ nguyên nhân gốc ===
        try {
            readConfig();
        } catch (ConfigException e) {
            System.out.println("Lỗi: " + e.getMessage() + " | nguyên nhân: " + e.getCause());
        }

        // === Phần 4: checked vs unchecked ===
        // checked: compiler ÉP khai báo throws / try-catch (vd IOException).
        // unchecked (RuntimeException): không ép -> thường là bug lập trình (NPE, IllegalArgument).
        System.out.println("Xong.");
    }

    // checked exception -> phải khai báo throws (ép caller xử lý).
    static void readConfig() throws ConfigException {
        try {
            Integer.parseInt("abc");                 // ném NumberFormatException (unchecked)
        } catch (NumberFormatException root) {
            // Bọc lại với nguyên nhân gốc -> không mất dấu vết.
            throw new ConfigException("Không đọc được config", root);
        }
    }
}

// Tài nguyên tự đóng: implements AutoCloseable -> dùng được trong try-with-resources.
class Resource implements AutoCloseable {
    private final String name;
    Resource(String name) { this.name = name; System.out.println("Mở " + name); }
    void use() { System.out.println("Dùng " + name); }
    @Override public void close() { System.out.println("Đóng " + name); }
}

// custom checked exception: kế thừa Exception (không phải RuntimeException).
class ConfigException extends Exception {
    ConfigException(String msg, Throwable cause) { super(msg, cause); } // truyền cause
}
