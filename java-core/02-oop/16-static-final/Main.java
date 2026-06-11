/*
 * BÀI 16: STATIC & FINAL — Thành viên lớp, hằng, bất biến, nơi lưu trữ
 * ================================================================
 * WHAT  : static = thuộc CLASS (không thuộc object); final = không cho gán lại /
 *         không cho override / không cho kế thừa, tuỳ ngữ cảnh.
 * WHY   : static cho trạng thái & tiện ích dùng chung; final cho an toàn (immutable,
 *         thread-safe) + gợi ý tối ưu cho JIT (constant folding, inline).
 * HOW   : static field nằm ở Metaspace (theo class), khởi tạo lúc class init.
 *         final field gán đúng 1 lần; static final primitive/String -> hằng biên dịch.
 * WHEN  : static cho counter/factory/util; final cho hằng, field bất biến, lớp khoá.
 * WHICH : static method (không cần object) vs instance method (cần state object).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: static field dùng chung mọi object ===
        new Counter(); new Counter(); new Counter();
        System.out.println("Đã tạo " + Counter.count + " counter (static dùng chung)");

        // === Phần 2: static method — gọi không cần object ===
        System.out.println("MathUtil.square(5) = " + MathUtil.square(5));

        // === Phần 3: static final = HẰNG ===
        // ⚙️ Dưới nắp capo: static final primitive/String là "compile-time constant"
        //    -> giá trị được NHÚNG THẲNG vào nơi dùng (constant folding), không tra biến.
        System.out.println("PI = " + MathUtil.PI);
        double circle = MathUtil.PI * 2 * 2;       // PI bị thay bằng 3.14159 lúc compile
        System.out.println("Diện tích = " + circle);

        // === Phần 4: final field — gán đúng 1 lần, đảm bảo bất biến ===
        Point p = new Point(3, 4);
        // p.x = 10;  // ❌ lỗi compile: x là final.
        System.out.println("Point bất biến: (" + p.x + ", " + p.y + ")");

        // === Phần 5: final với reference — KHOÁ tham chiếu, KHÔNG khoá object ===
        // ⚙️ final chỉ chặn gán lại địa chỉ; object trỏ tới vẫn có thể đổi state.
        final StringBuilder sb = new StringBuilder("a");
        sb.append("b");            // ✅ OK: sửa object
        // sb = new StringBuilder(); // ❌ không gán lại tham chiếu final
        System.out.println("final reference, object vẫn đổi được: " + sb);

        // === Phần 6: final method/class (minh hoạ qua Point là final class) ===
        System.out.println("Point là final class? " + java.lang.reflect.Modifier.isFinal(Point.class.getModifiers()));
    }
}

class Counter {
    static int count = 0;          // 1 bản cho cả class
    Counter() { count++; }
}

class MathUtil {
    static final double PI = 3.14159;          // hằng compile-time
    static int square(int x) { return x * x; } // không phụ thuộc object -> static
    private MathUtil() {}                       // chặn tạo object lớp tiện ích
}

// final class: không cho kế thừa -> an toàn + JIT tối ưu (mọi method coi như không override).
final class Point {
    final int x, y;                            // final field -> immutable
    Point(int x, int y) { this.x = x; this.y = y; } // gán final đúng 1 lần trong ctor
}
