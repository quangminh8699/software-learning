/*
 * BÀI 39: REFLECTION — Soi & thao tác class lúc runtime, chi phí & rủi ro
 * ================================================================
 * WHAT  : Reflection cho phép KIỂM TRA và THAO TÁC class/method/field lúc RUNTIME:
 *         tạo object, gọi method, đọc/ghi field — kể cả private.
 * WHY   : Nền của DI (Spring), ORM (Hibernate), serialization (Jackson), test
 *         (JUnit), proxy động. Cho phép code "tổng quát" không biết trước kiểu.
 * HOW   : Mỗi class nạp vào JVM có một object Class<?> mô tả nó (metadata trong
 *         Metaspace). Reflection đọc metadata đó + bỏ qua kiểm tra truy cập (setAccessible).
 * WHEN  : Khi viết framework/thư viện tổng quát; TRÁNH trong code nghiệp vụ thường.
 * WHICH : Reflection (linh hoạt, chậm, mất an toàn kiểu) vs gọi trực tiếp (nhanh, tĩnh).
 */
import java.lang.reflect.*;

public class Main {
    public static void main(String[] args) throws Exception {

        // === Phần 1: Lấy đối tượng Class — 3 cách ===
        Class<?> c1 = Person.class;                       // từ kiểu (compile-time)
        Class<?> c2 = new Person("X", 1).getClass();      // từ object
        Class<?> c3 = Class.forName("Person");            // từ TÊN chuỗi (động, runtime)
        System.out.println("Cùng một Class object? " + (c1 == c2 && c2 == c3));  // true (1 Class/class)

        // === Phần 2: Soi cấu trúc class ===
        System.out.println("Tên: " + c1.getSimpleName() + ", cha: " + c1.getSuperclass().getSimpleName());
        System.out.print("Fields: ");
        for (Field f : c1.getDeclaredFields()) System.out.print(f.getName() + " ");
        System.out.print("\nMethods: ");
        for (Method m : c1.getDeclaredMethods()) System.out.print(m.getName() + " ");
        System.out.println();

        // === Phần 3: Tạo object động qua constructor ===
        Constructor<?> ctor = c1.getConstructor(String.class, int.class);
        Person p = (Person) ctor.newInstance("Alice", 30);   // ~ new Person("Alice", 30)
        System.out.println("Tạo động: " + p);

        // === Phần 4: Gọi method động (kể cả tên method từ chuỗi) ===
        Method greet = c1.getMethod("greet", String.class);
        Object res = greet.invoke(p, "Bob");                 // ~ p.greet("Bob")
        System.out.println("invoke greet: " + res);

        // === Phần 5: PHÁ đóng gói — đọc/ghi field private (sức mạnh & nguy hiểm) ===
        // ⚙️ setAccessible(true) bỏ qua kiểm tra truy cập của JVM -> sửa được private.
        //    Đây là cách Hibernate/Jackson gán field private mà không cần setter.
        Field ageField = c1.getDeclaredField("age");
        ageField.setAccessible(true);                        // tắt kiểm tra private
        System.out.println("age private hiện tại = " + ageField.get(p));
        ageField.set(p, 99);                                 // GHI vào field private
        System.out.println("Sau khi sửa private: " + p);

        // === Phần 6: Chi phí — reflection chậm hơn gọi trực tiếp ===
        long t0 = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) p.greet("x");           // gọi trực tiếp
        long direct = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) greet.invoke(p, "x");   // gọi qua reflection
        long reflect = System.nanoTime() - t0;

        System.out.printf("1tr lời gọi: trực tiếp=%dms, reflection=%dms (reflection chậm hơn ~%.1fx)%n",
                direct / 1_000_000, reflect / 1_000_000, (double) reflect / Math.max(direct, 1));
    }
}

class Person {
    private String name;
    private int age;                       // private -> chỉ reflection (setAccessible) mới sửa ngoài
    public Person(String name, int age) { this.name = name; this.age = age; }
    public String greet(String to) { return name + " chào " + to; }
    @Override public String toString() { return "Person{" + name + ", " + age + "}"; }
}
