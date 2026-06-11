/*
 * BÀI 18: ENUMS — Hằng kiểu an toàn, enum có hành vi, EnumMap, state machine
 * ================================================================
 * WHAT  : enum = tập HỮU HẠN các hằng có kiểu; thực chất là class đặc biệt,
 *         mỗi hằng là một SINGLETON instance của enum đó.
 * WHY   : Thay "magic number/String" bằng kiểu an toàn (compiler kiểm tra),
 *         gắn được dữ liệu + hành vi vào từng hằng; switch đầy đủ; singleton an toàn.
 * HOW   : Mỗi hằng được JVM tạo MỘT lần (static final) lúc class init -> so sánh
 *         bằng == an toàn. enum kế thừa java.lang.Enum, không kế thừa class khác.
 * WHEN  : Khi miền giá trị cố định, biết trước (trạng thái, ngày, hành tinh...).
 * WHICH : enum thường vs enum có constructor/field vs enum override method theo hằng.
 */
import java.util.EnumMap;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: enum cơ bản — an toàn kiểu, dùng trong switch ===
        Day today = Day.SATURDAY;
        System.out.println(today + " là cuối tuần? " + today.isWeekend());

        // === Phần 2: enum CÓ field + constructor (gắn dữ liệu vào hằng) ===
        for (Planet p : Planet.values()) {        // values(): mảng mọi hằng theo thứ tự
            System.out.printf("%-8s khối lượng=%.2e kg, trọng lực bề mặt=%.2f%n",
                    p, p.mass(), p.surfaceGravity());
        }

        // === Phần 3: enum override method THEO TỪNG HẰNG (constant-specific body) ===
        // ⚙️ Mỗi hằng là một lớp con vô danh -> có thể có cài đặt riêng.
        System.out.println("3 + 4 = " + Operation.PLUS.apply(3, 4));
        System.out.println("3 * 4 = " + Operation.TIMES.apply(3, 4));

        // === Phần 4: == an toàn + ordinal/name ===
        // ⚙️ Mỗi hằng là singleton -> so == đúng & nhanh (không cần equals).
        System.out.println("today == SATURDAY ? " + (today == Day.SATURDAY));
        System.out.println("name=" + today.name() + ", ordinal=" + today.ordinal());

        // === Phần 5: EnumMap — Map cực nhanh khi key là enum ===
        // ⚙️ Dưới nắp capo: EnumMap dùng MẢNG indexed theo ordinal -> O(1), không hash.
        EnumMap<Day, String> plan = new EnumMap<>(Day.class);
        plan.put(Day.MONDAY, "Họp");
        plan.put(Day.SATURDAY, "Nghỉ");
        System.out.println("Kế hoạch: " + plan);
    }
}

enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    boolean isWeekend() { return this == SATURDAY || this == SUNDAY; }
}

// enum với field + constructor: mỗi hằng mang dữ liệu riêng.
enum Planet {
    EARTH(5.976e24, 6.37814e6),
    MARS (6.421e23, 3.3972e6);

    private final double mass, radius;
    Planet(double mass, double radius) { this.mass = mass; this.radius = radius; }

    double mass() { return mass; }
    double surfaceGravity() { return 6.67300e-11 * mass / (radius * radius); }
}

// enum với hành vi riêng từng hằng (constant-specific method body).
enum Operation {
    PLUS  { @Override int apply(int a, int b) { return a + b; } },
    TIMES { @Override int apply(int a, int b) { return a * b; } };
    abstract int apply(int a, int b);
}
