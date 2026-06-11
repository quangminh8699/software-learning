/*
 * BÀI 13b: OVERLOADING vs OVERRIDING — Static binding vs Dynamic binding
 * ================================================================
 * WHAT  : Hai khái niệm hay nhầm: overload (cùng tên, khác tham số) vs
 *         override (lớp con thay method cha, cùng chữ ký).
 * WHY   : Đây là câu hỏi phỏng vấn senior kinh điển; hiểu sai gây bug tinh vi
 *         khi trộn overload + đa hình.
 * HOW   : Overload -> chọn lúc COMPILE theo KIỂU KHAI BÁO (static binding,
 *         invokestatic/invokevirtual tới method cố định).
 *         Override -> chọn lúc RUNTIME theo KIỂU THỰC (dynamic binding, vtable).
 * WHEN  : Overload để có nhiều "biến thể" cùng ý niệm; override để chuyên biệt hành vi.
 * WHICH : Cần đa hình -> override; cần tiện dụng nhiều kiểu đối số -> overload.
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: OVERRIDING — quyết theo KIỂU THỰC (runtime) ===
        Animal a = new Cat();        // kiểu khai báo Animal, kiểu thực Cat
        a.sound();                   // -> "Meo" (bản Cat) — DYNAMIC binding
        System.out.println("→ override chọn theo kiểu THỰC (Cat) lúc runtime\n");

        // === Phần 2: OVERLOADING — quyết theo KIỂU KHAI BÁO (compile) ===
        // ⚙️ Cùng tên print, compiler chọn overload theo KIỂU TĨNH của đối số.
        Printer p = new Printer();
        int i = 5; double d = 5.0; Object o = "text";
        p.print(i);                  // -> print(int)
        p.print(d);                  // -> print(double)
        p.print(o);                  // -> print(Object), DÙ o thực ra là String!
        System.out.println("→ overload chọn theo kiểu KHAI BÁO (Object) lúc compile\n");

        // === Phần 3: CẠM BẪY trộn overload + null ===
        // p.print(null); // ❌ mơ hồ nếu có nhiều overload reference -> lỗi compile "ambiguous"

        // === Phần 4: Bẫy kinh điển — "tưởng override, hoá ra overload" ===
        // equals(Dog) KHÔNG override equals(Object) vì khác chữ ký tham số!
        Object x = new Dog("Rex");
        Object y = new Dog("Rex");
        System.out.println("x.equals(y) = " + x.equals(y)
                + "  (gọi Object.equals vì DogEquals chỉ OVERLOAD, không OVERRIDE)");
    }
}

class Animal { void sound() { System.out.println("..."); } }
class Cat extends Animal {
    @Override void sound() { System.out.println("Meo"); }   // OVERRIDE: cùng chữ ký
}

class Printer {
    void print(int v)    { System.out.println("print(int): " + v); }
    void print(double v) { System.out.println("print(double): " + v); }
    void print(Object v) { System.out.println("print(Object): " + v); }  // OVERLOAD
}

// Minh hoạ bẫy: viết equals(Dog) tưởng là override nhưng thực ra là overload.
class Dog {
    String name;
    Dog(String name) { this.name = name; }
    // ⚠️ Đây KHÔNG phải override Object.equals(Object) — tham số là Dog, không phải Object.
    public boolean equals(Dog other) { return this.name.equals(other.name); }
    // Vì vậy Object.equals(Object) (so địa chỉ) vẫn được dùng khi kiểu khai báo là Object.
}
