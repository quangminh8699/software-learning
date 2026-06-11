/*
 * BÀI 13: POLYMORPHISM — Đa hình, dynamic dispatch, vtable
 * ================================================================
 * WHAT  : Cùng một lời gọi method nhưng chạy bản cài đặt KHÁC nhau tuỳ kiểu THỰC.
 * WHY   : Viết code theo abstraction (Shape) mà chạy đúng hành vi cụ thể (Circle...)
 *         -> mở rộng thêm loại mới KHÔNG cần sửa code gọi (Open/Closed Principle).
 * HOW   : Lời gọi method ảo dùng invokevirtual -> JVM tra VTABLE của kiểu THỰC
 *         lúc RUNTIME để chọn cài đặt (dynamic dispatch / late binding).
 * WHEN  : Khi muốn xử lý đồng nhất nhiều loại qua một kiểu chung.
 * WHICH : Runtime polymorphism (override) vs compile-time (overload — xem 13b).
 */
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // === Đa hình runtime: biến kiểu CHA giữ object kiểu CON ===
        // ⚙️ Kiểu KHAI BÁO là Shape, nhưng kiểu THỰC quyết định method nào chạy.
        List<Shape> shapes = List.of(new Circle(2), new Rectangle(3, 4), new Circle(1));

        double total = 0;
        for (Shape s : shapes) {
            // ⚙️ Dưới nắp capo: s.area() biên dịch thành `invokevirtual Shape.area()`.
            //    Lúc chạy, JVM tra vtable của object THỰC (Circle/Rectangle) -> gọi đúng bản.
            System.out.printf("%-10s area = %.2f%n", s.name(), s.area());
            total += s.area();
        }
        System.out.printf("Tổng diện tích = %.2f%n", total);

        // === Lợi ích: thêm Triangle mà KHÔNG sửa vòng lặp trên ===
        Shape tri = new Triangle(6, 2);
        System.out.printf("Thêm %s area = %.2f (code cũ không đổi)%n", tri.name(), tri.area());

        // === Upcasting (ngầm) & downcasting (tường minh) ===
        Shape any = new Circle(5);          // upcast Circle -> Shape (an toàn)
        if (any instanceof Circle c) {      // pattern matching (Java 16+) kiểm tra + ép kiểu
            System.out.println("Downcast OK, bán kính = " + c.radius());
        }
    }
}

// Kiểu trừu tượng chung — "hợp đồng" area().
abstract class Shape {
    abstract double area();
    String name() { return getClass().getSimpleName(); }
}

class Circle extends Shape {
    private final double r;
    Circle(double r) { this.r = r; }
    double radius() { return r; }
    @Override double area() { return Math.PI * r * r; }   // bản cài đặt riêng
}

class Rectangle extends Shape {
    private final double w, h;
    Rectangle(double w, double h) { this.w = w; this.h = h; }
    @Override double area() { return w * h; }
}

class Triangle extends Shape {
    private final double base, height;
    Triangle(double base, double height) { this.base = base; this.height = height; }
    @Override double area() { return 0.5 * base * height; }
}
