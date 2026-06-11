/*
 * BÀI 17: NESTED & INNER CLASSES — static nested, inner, local, anonymous
 * ================================================================
 * WHAT  : 4 loại class lồng nhau: static nested, inner (non-static),
 *         local (trong method), anonymous (vô danh).
 * WHY   : Gom class chỉ phục vụ 1 mục đích vào gần nơi dùng -> tăng tính đóng gói.
 *         Inner class giữ tham chiếu ngầm tới object ngoài (nguồn memory leak!).
 * HOW   : inner class chứa con trỏ ẩn Outer.this; anonymous class biên dịch thành
 *         Outer$1.class. Compiler "capture" biến local effectively-final vào field.
 * WHEN  : static nested cho helper không cần outer; inner khi cần truy cập state outer;
 *         anonymous cho cài đặt 1-lần (trước Java 8, nay lambda thay nhiều chỗ).
 * WHICH : static nested (không giữ outer, ưu tiên) vs inner (giữ outer, cẩn thận leak).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: static nested — KHÔNG giữ tham chiếu Outer ===
        Outer.StaticNested sn = new Outer.StaticNested();   // tạo không cần Outer instance
        sn.hello();

        // === Phần 2: inner (non-static) — CẦN một object Outer ===
        // ⚙️ Dưới nắp capo: inner giữ con trỏ ẩn tới Outer.this -> truy cập field outer.
        Outer outer = new Outer("DỮ LIỆU NGOÀI");
        Outer.Inner inner = outer.new Inner();             // cú pháp đặc biệt: outer.new
        inner.show();                                       // đọc được field private của Outer

        // === Phần 3: local class — khai báo trong method ===
        outer.demoLocal();

        // === Phần 4: anonymous class — cài đặt interface ngay tại chỗ ===
        // ⚙️ Biên dịch thành Main$1.class; "capture" biến msg (effectively final).
        String msg = "Xin chào từ anonymous";
        Greeter g = new Greeter() {
            @Override public void greet() { System.out.println(msg); } // dùng biến bị capture
        };
        g.greet();

        // So sánh: từ Java 8, anonymous cài functional interface = lambda gọn hơn:
        Greeter g2 = () -> System.out.println("Cùng việc đó, viết bằng lambda");
        g2.greet();
    }
}

interface Greeter { void greet(); }

class Outer {
    private String data;
    Outer() {}
    Outer(String data) { this.data = data; }

    // static nested: như class thường nhưng đặt trong Outer, KHÔNG truy cập instance field.
    static class StaticNested {
        void hello() { System.out.println("static nested: độc lập, không cần Outer"); }
    }

    // inner (non-static): GẮN với một object Outer, đọc được field private của nó.
    class Inner {
        void show() { System.out.println("inner đọc field Outer: " + data); }
    }

    void demoLocal() {
        int localVar = 42;                  // effectively final -> bị capture
        // local class: phạm vi chỉ trong method này.
        class LocalCalc {
            int doubled() { return localVar * 2; }
        }
        System.out.println("local class: " + new LocalCalc().doubled());
    }
}
