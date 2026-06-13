/*
 * BÀI 31: PATTERN MATCHING — instanceof pattern, switch pattern, record deconstruction (Java 21)
 * ================================================================
 * WHAT  : Kết hợp KIỂM TRA KIỂU + ÉP KIỂU + RÚT TRÍCH dữ liệu trong một cú pháp.
 * WHY   : Diệt boilerplate "instanceof rồi cast"; switch trên kiểu an toàn & đầy đủ;
 *         rã record thành component ngay tại chỗ -> code xử lý dữ liệu sạch.
 * HOW   : compiler sinh kiểm tra kiểu + binding biến tự động; với sealed type,
 *         switch được kiểm tra ĐẦY ĐỦ (exhaustive) lúc compile.
 * WHEN  : Khi xử lý cây kiểu/đa hình theo dữ liệu (AST, sự kiện, DTO đa dạng).
 * WHICH : if-instanceof (rẽ vài nhánh) vs switch pattern (nhiều nhánh + exhaustive).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: instanceof pattern (Java 16+) — kiểm tra + bind 1 bước ===
        Object obj = "Xin chào Java";
        // ⚙️ Cũ: if (obj instanceof String) { String s = (String) obj; ... }
        //    Mới: bind thẳng biến s nếu khớp.
        if (obj instanceof String s && s.length() > 5) {   // s dùng được ngay trong &&
            System.out.println("instanceof pattern: độ dài = " + s.length());
        }

        // === Phần 2: switch pattern (Java 21) — rẽ theo KIỂU, có guard ===
        System.out.println(describe(42));
        System.out.println(describe(3.14));
        System.out.println(describe("hi"));
        System.out.println(describe(List_of()));
        System.out.println(describe(null));     // case null tường minh

        // === Phần 3: Record deconstruction — rã record thành component ===
        Object shape = new Circle(new Point(1, 2), 5);
        String info = switch (shape) {
            // ⚙️ Rã thẳng: lấy center(x,y) và r trong một pattern lồng nhau.
            case Circle(Point(var x, var y), var r) ->
                    "Circle tâm(" + x + "," + y + "), bán kính " + r;
            case Rectangle(var w, var h) -> "Rectangle " + w + "x" + h;
            default -> "Hình khác";
        };
        System.out.println("Deconstruction: " + info);

        // === Phần 4: sealed + switch -> exhaustive (không cần default) ===
        for (Expr e : new Expr[]{ new Num(7), new Add(new Num(3), new Num(4)) }) {
            System.out.println("eval = " + eval(e));
        }
    }

    // switch pattern với type patterns + guard (when) + case null.
    static String describe(Object o) {
        return switch (o) {
            case null            -> "null";
            case Integer i when i > 10 -> "Integer lớn: " + i;   // guard: điều kiện thêm
            case Integer i       -> "Integer: " + i;
            case Double d        -> "Double: " + d;
            case String s        -> "String dài " + s.length();
            default              -> "Kiểu khác: " + o.getClass().getSimpleName();
        };
    }

    // sealed -> switch eval KHÔNG cần default; thêm loại Expr mà quên nhánh -> lỗi compile.
    static int eval(Expr e) {
        return switch (e) {
            case Num(var v)      -> v;
            case Add(var l, var r) -> eval(l) + eval(r);   // đệ quy + deconstruction
        };
    }

    static java.util.List<Integer> List_of() { return java.util.List.of(1, 2); }
}

record Point(int x, int y) {}
record Circle(Point center, int r) {}
record Rectangle(int w, int h) {}

// AST nhỏ: sealed -> tập con đóng -> switch exhaustive.
sealed interface Expr permits Num, Add {}
record Num(int v) implements Expr {}
record Add(Expr left, Expr right) implements Expr {}
