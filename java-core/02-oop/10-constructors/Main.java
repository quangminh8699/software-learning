/*
 * BÀI 10: CONSTRUCTORS — Khởi tạo object, overloading, chaining, thứ tự init
 * ================================================================
 * WHAT  : Constructor là method đặc biệt chạy khi tạo object để thiết lập trạng thái.
 * WHY   : Đảm bảo object "ra đời" ở trạng thái HỢP LỆ (invariant) ngay từ đầu.
 * HOW   : Khi `new`, JVM chạy: static init (1 lần/class) -> field init + instance
 *         initializer -> constructor. this(...) gọi constructor khác cùng class;
 *         super(...) gọi constructor lớp cha (luôn chạy TRƯỚC thân constructor con).
 * WHEN  : Mọi class đều cần (nếu không viết, compiler thêm constructor mặc định).
 * WHICH : Nhiều overload cho nhiều cách tạo; this() để tránh lặp code khởi tạo.
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: Constructor overloading — nhiều cách tạo object ===
        Pizza p1 = new Pizza();                       // mặc định
        Pizza p2 = new Pizza("L");                    // chỉ size
        Pizza p3 = new Pizza("L", true);              // size + phô mai

        p1.describe(); p2.describe(); p3.describe();

        // === Phần 2: Thứ tự khởi tạo — quan sát qua log ===
        // ⚙️ Dưới nắp capo, thứ tự khi `new Child()`:
        //    1) static block (chỉ lần ĐẦU class được nạp)
        //    2) super() — constructor cha chạy TRƯỚC
        //    3) instance initializer + field init của con (theo thứ tự viết)
        //    4) thân constructor con
        System.out.println("\n--- Tạo Child lần 1 ---");
        new Child();
        System.out.println("--- Tạo Child lần 2 (static KHÔNG chạy lại) ---");
        new Child();
    }
}

class Pizza {
    private String size;
    private boolean cheese;

    // Constructor mặc định: ủy quyền cho constructor khác bằng this(...).
    Pizza() { this("M", false); }                  // this(...) PHẢI là câu lệnh đầu tiên

    Pizza(String size) { this(size, false); }      // chaining để tránh lặp

    // Constructor "đầy đủ" — nơi tập trung mọi logic khởi tạo.
    Pizza(String size, boolean cheese) {
        this.size = size;
        this.cheese = cheese;
    }

    void describe() {
        System.out.println("Pizza size=" + size + ", cheese=" + cheese);
    }
}

class Parent {
    // static initializer: chạy MỘT lần khi class Parent được nạp.
    static { System.out.println("[static] Parent được nạp"); }
    // instance initializer: chạy mỗi lần tạo object, TRƯỚC thân constructor.
    { System.out.println("[init ] Parent instance initializer"); }
    Parent() { System.out.println("[ctor ] Parent()"); }
}

class Child extends Parent {
    static { System.out.println("[static] Child được nạp"); }
    { System.out.println("[init ] Child instance initializer"); }
    Child() {
        // super() được chèn ngầm ở đây nếu không viết -> Parent() chạy trước dòng dưới.
        System.out.println("[ctor ] Child()");
    }
}
