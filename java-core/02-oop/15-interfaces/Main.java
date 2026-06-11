/*
 * BÀI 15: INTERFACES — Hợp đồng, default/static method, đa kế thừa kiểu
 * ================================================================
 * WHAT  : interface = hợp đồng thuần (chỉ chữ ký method) + (Java 8+) default/static
 *         method, constant. Một class có thể implements NHIỀU interface.
 * WHY   : Tách "cái gì" khỏi "làm thế nào"; cho đa kế thừa HÀNH VI mà không vướng
 *         kim cương state; là nền của lập trình hướng abstraction + DI + test mock.
 * HOW   : Lời gọi qua tham chiếu interface dùng invokeinterface (tra bảng theo
 *         kiểu thực). default method cho phép thêm hành vi mà không phá class cũ.
 * WHEN  : Khi muốn nhiều lớp không liên quan cùng "có khả năng" gì đó (Comparable...).
 * WHICH : interface (đa kế thừa, không state) vs abstract class (state + đơn kế thừa, 14).
 */
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {

        // === Đa kế thừa kiểu: Duck implements NHIỀU interface ===
        Duck duck = new Duck();
        duck.fly();
        duck.swim();

        // === Lập trình theo interface: biến kiểu interface, đổi cài đặt tự do ===
        Flyable f = duck;             // chỉ thấy hành vi fly()
        f.fly();
        System.out.println("default method: " + f.describe());  // default method dùng chung

        // === static method trong interface (Java 8+): tiện ích gắn với interface ===
        System.out.println("Flyable.info(): " + Flyable.info());

        // === Interface có sẵn của JDK: Comparable để sắp xếp ===
        List<Employee> emps = new ArrayList<>(List.of(
                new Employee("An", 1500), new Employee("Bình", 900), new Employee("Cường", 1200)));
        Collections.sort(emps);       // dùng compareTo() — hợp đồng Comparable
        System.out.println("Sắp theo lương: " + emps);
    }
}

interface Flyable {
    int MAX_ALTITUDE = 10000;                 // ngầm là public static final (hằng)
    void fly();                               // method trừu tượng (ngầm public abstract)

    // default method (Java 8+): có thân -> thêm hành vi mà KHÔNG phá class đã implements.
    default String describe() { return "Một vật biết bay, trần bay " + MAX_ALTITUDE + "m"; }

    // static method: tiện ích, gọi qua Flyable.info().
    static String info() { return "Interface Flyable v1"; }
}

interface Swimmable { void swim(); }

// implements NHIỀU interface -> "đa kế thừa hành vi".
class Duck implements Flyable, Swimmable {
    @Override public void fly()  { System.out.println("Vịt bay là đà"); }
    @Override public void swim() { System.out.println("Vịt bơi"); }
}

// Comparable: hợp đồng "tôi biết tự so sánh với đồng loại".
class Employee implements Comparable<Employee> {
    String name; int salary;
    Employee(String name, int salary) { this.name = name; this.salary = salary; }
    @Override public int compareTo(Employee o) { return Integer.compare(this.salary, o.salary); }
    @Override public String toString() { return name + "(" + salary + ")"; }
}
