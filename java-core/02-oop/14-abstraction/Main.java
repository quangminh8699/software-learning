/*
 * BÀI 14: ABSTRACTION — Abstract class & method, template method
 * ================================================================
 * WHAT  : abstract class = lớp KHÔNG thể new trực tiếp, định nghĩa "khung" gồm
 *         method trừu tượng (chưa thân) + method cụ thể (có thân) dùng chung.
 * WHY   : Gom phần CHUNG (đã cài đặt) + ép lớp con cài phần RIÊNG -> tái dùng +
 *         áp đặt hợp đồng. Khác interface ở chỗ có thể giữ STATE + code chung.
 * HOW   : Compiler chặn new AbstractClass(). Method abstract bắt buộc lớp con
 *         override. JVM vẫn cấp object cho lớp con (chứa phần state của abstract).
 * WHEN  : Khi nhiều lớp con chia sẻ state + thuật toán khung, chỉ khác vài bước.
 * WHICH : abstract class (state + code chung, đơn kế thừa) vs interface (đa kế thừa, 15).
 */
public class Main {
    public static void main(String[] args) {

        // Coffee coffee = new Coffee();  // ❌ KHÔNG được — abstract không new trực tiếp.

        // ⚙️ Template Method: phần KHUNG (prepare) cố định ở lớp cha,
        //    lớp con chỉ điền các BƯỚC riêng (brew, addCondiments).
        Beverage tea = new Tea();
        Beverage coffee = new Coffee();

        System.out.println("=== Pha trà ===");
        tea.prepare();          // gọi thuật toán khung -> tự gọi bản con ở các bước

        System.out.println("\n=== Pha cà phê ===");
        coffee.prepare();

        // Đa hình vẫn áp dụng: biến kiểu abstract giữ object lớp con.
        System.out.println("\ntea là Beverage? " + (tea instanceof Beverage));
    }
}

// abstract class: có STATE chung + thuật toán khung + method bắt buộc con cài.
abstract class Beverage {
    // có thể giữ state (khác interface truyền thống).
    protected int waterMl = 200;

    // === Template Method: cố định KHUNG, final để con không phá thứ tự ===
    public final void prepare() {
        boilWater();
        brew();                 // bước trừu tượng -> con quyết
        pourInCup();
        addCondiments();        // bước trừu tượng -> con quyết
    }

    // method cụ thể dùng chung mọi loại đồ uống:
    private void boilWater()  { System.out.println("Đun " + waterMl + "ml nước"); }
    private void pourInCup()  { System.out.println("Rót ra cốc"); }

    // method TRỪU TƯỢNG: không thân -> lớp con BẮT BUỘC cài đặt.
    protected abstract void brew();
    protected abstract void addCondiments();
}

class Tea extends Beverage {
    @Override protected void brew() { System.out.println("Ngâm túi trà"); }
    @Override protected void addCondiments() { System.out.println("Thêm chanh"); }
}

class Coffee extends Beverage {
    @Override protected void brew() { System.out.println("Lọc cà phê qua phin"); }
    @Override protected void addCondiments() { System.out.println("Thêm sữa & đường"); }
}
