/*
 * BÀI 19: RECORDS & SEALED CLASSES — Dữ liệu bất biến + phân cấp đóng (Java 17/21)
 * ================================================================
 * WHAT  : record = lớp DỮ LIỆU bất biến, compiler tự sinh ctor/getter/equals/
 *         hashCode/toString. sealed = giới hạn DANH SÁCH lớp con được phép.
 * WHY   : record diệt boilerplate cho DTO/value object; sealed mô hình hoá
 *         "tổng kiểu" (sum type) -> switch pattern kiểm tra ĐẦY ĐỦ lúc compile.
 * HOW   : record tự sinh thành phần từ "components"; field là final. sealed khai
 *         báo `permits` -> compiler biết tập con đóng -> switch không cần default.
 * WHEN  : record cho value object/DTO bất biến; sealed cho phân cấp biết trước.
 * WHICH : record (immutable data) vs class thường (có vòng đời/định danh).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: record — 1 dòng thay cho ~50 dòng boilerplate ===
        Point p1 = new Point(3, 4);
        Point p2 = new Point(3, 4);

        // ⚙️ Dưới nắp capo: compiler tự sinh equals() so theo GIÁ TRỊ các component.
        System.out.println("p1 = " + p1);                 // toString tự sinh
        System.out.println("p1.equals(p2) = " + p1.equals(p2)); // true (so giá trị)
        System.out.println("x=" + p1.x() + ", y=" + p1.y());    // accessor tên = tên component
        System.out.println("hashCode bằng nhau? " + (p1.hashCode() == p2.hashCode()));

        // record có thể thêm method + compact constructor để validate:
        Range r = new Range(1, 10);
        System.out.println("Range độ rộng = " + r.width());
        try { new Range(10, 1); } catch (IllegalArgumentException e) {
            System.out.println("Validate trong record: " + e.getMessage());
        }

        // === Phần 2: sealed + switch pattern (Java 21) — xử lý ĐẦY ĐỦ, an toàn ===
        Shape[] shapes = { new Circle(2), new Square(3), new Circle(1) };
        for (Shape s : shapes) {
            // ⚙️ Vì Shape là sealed (chỉ Circle/Square), switch KHÔNG cần default:
            //    compiler tự biết đã phủ hết -> thêm loại mới mà quên xử lý -> LỖI COMPILE.
            double area = switch (s) {
                case Circle c -> Math.PI * c.r() * c.r();
                case Square sq -> sq.side() * sq.side();
            };
            System.out.printf("%-8s area = %.2f%n", s.getClass().getSimpleName(), area);
        }
    }
}

// record: bất biến, tự sinh mọi thứ. "components" = (x, y).
record Point(int x, int y) {}

// record + compact constructor để validate (giữ invariant).
record Range(int from, int to) {
    Range {                                  // compact constructor: không cần khai báo lại tham số
        if (to < from) throw new IllegalArgumentException("to < from");
    }
    int width() { return to - from; }
}

// sealed: chỉ Circle & Square được phép kế thừa Shape.
sealed interface Shape permits Circle, Square {}
record Circle(double r) implements Shape {}
record Square(double side) implements Shape {}
