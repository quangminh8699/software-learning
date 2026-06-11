/*
 * BÀI 11: ENCAPSULATION — Đóng gói, access modifier, bảo toàn invariant
 * ================================================================
 * WHAT  : Đóng gói = giấu trạng thái nội bộ (private) + lộ hành vi qua method.
 * WHY   : Bảo vệ INVARIANT (bất biến nghiệp vụ), cho phép đổi cài đặt bên trong
 *         mà không phá code gọi -> giảm coupling, tăng khả năng bảo trì.
 * HOW   : 4 mức truy cập (private/default/protected/public) kiểm soát ai thấy gì,
 *         được compiler enforce (và một phần bytecode/JVM enforce).
 * WHEN  : LUÔN — mặc định để field private, chỉ mở ra điều thật sự cần.
 * WHICH : getter/setter có kiểm soát vs field public trần (gần như không bao giờ public).
 */
public class Main {
    public static void main(String[] args) {

        Temperature t = new Temperature();

        // === Phần 1: Setter có kiểm soát -> bảo vệ invariant ===
        t.setCelsius(25);
        System.out.println("25°C = " + t.getFahrenheit() + "°F");

        // Thử gán giá trị phi vật lý -> bị chặn bởi logic trong setter.
        try {
            t.setCelsius(-500);                 // dưới độ 0 tuyệt đối (-273.15)
        } catch (IllegalArgumentException e) {
            System.out.println("Bị chặn: " + e.getMessage());
        }
        System.out.println("Giá trị vẫn an toàn: " + t.getCelsius() + "°C");

        // === Phần 2: Field private KHÔNG truy cập trực tiếp được ===
        // t.celsius = 999;  // ❌ lỗi biên dịch: celsius có mức private
        // -> Bắt buộc đi qua setCelsius -> mọi thay đổi đều được kiểm soát.

        // === Phần 3: Đổi cài đặt nội bộ KHÔNG ảnh hưởng người dùng ===
        // ⚙️ Temperature lưu nội bộ bằng Celsius, nhưng có thể đổi sang Kelvin
        //    mà code main này KHÔNG cần sửa, vì chỉ phụ thuộc method công khai.
        System.out.println("Lưu nội bộ kiểu gì là chuyện riêng của class.");
    }
}

class Temperature {
    // private: chỉ class này thấy -> "trạng thái nội bộ", không ai sửa lén.
    private double celsius;

    // public method: hợp đồng (contract) lộ ra ngoài.
    public double getCelsius() { return celsius; }

    public void setCelsius(double c) {
        // Validation tập trung tại đây -> invariant luôn đúng dù ai gọi.
        if (c < -273.15) {
            throw new IllegalArgumentException("Nhiệt độ dưới 0 tuyệt đối: " + c);
        }
        this.celsius = c;
    }

    // Giá trị "tính toán" — không lưu field riêng, suy ra từ celsius.
    // ⚙️ Người dùng không biết (và không cần biết) fahrenheit có được lưu hay không.
    public double getFahrenheit() { return celsius * 9 / 5 + 32; }

    // private helper: chi tiết cài đặt, giấu hoàn toàn.
    private double toKelvin() { return celsius + 273.15; }
}
