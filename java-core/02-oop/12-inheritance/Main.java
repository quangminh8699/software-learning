/*
 * BÀI 12: INHERITANCE — Kế thừa, super, override, bố cục object lớp con
 * ================================================================
 * WHAT  : Lớp con (subclass) kế thừa field + method của lớp cha (superclass).
 * WHY   : Tái dùng code + mô hình quan hệ "is-a" (Dog IS-A Animal).
 * HOW   : Object lớp con chứa LUÔN phần dữ liệu của cha (xếp chồng trong cùng 1
 *         object trên heap). super gọi method/constructor cha. Mọi class kế thừa Object.
 * WHEN  : Khi có quan hệ "is-a" thật sự + muốn chia sẻ hành vi.
 * WHICH : Kế thừa (is-a, chặt) vs Composition (has-a, lỏng) -> ưu tiên composition.
 */
public class Main {
    public static void main(String[] args) {

        Dog dog = new Dog("Rex", "Husky");
        // ⚙️ Dưới nắp capo: object dog trên heap chứa CẢ field của Animal (name)
        //    LẪN field của Dog (breed) — "xếp chồng" trong cùng một vùng nhớ.
        dog.eat();                 // method kế thừa từ Animal
        dog.bark();                // method riêng của Dog
        dog.describe();            // override + gọi super

        System.out.println("dog instanceof Animal? " + (dog instanceof Animal)); // true (is-a)
        System.out.println("Mọi class đều kế thừa Object: " + dog.getClass().getSuperclass().getSuperclass());

        // === Composition để so sánh: Car HAS-A Engine (không kế thừa) ===
        Car car = new Car();
        car.start();
    }
}

// Lớp cha.
class Animal {
    protected String name;                    // protected: lớp con truy cập được
    Animal(String name) { this.name = name; }
    void eat() { System.out.println(name + " đang ăn."); }
    void describe() { System.out.println("Tôi là động vật tên " + name); }
}

// Lớp con: extends -> kế thừa name, eat(), describe().
class Dog extends Animal {
    private String breed;

    Dog(String name, String breed) {
        super(name);                          // PHẢI gọi constructor cha trước
        this.breed = breed;
    }

    void bark() { System.out.println(name + " sủa: Gâu gâu!"); }

    @Override                                 // override: thay hành vi describe của cha
    void describe() {
        super.describe();                     // vẫn tái dùng phần của cha
        System.out.println("...và là chó giống " + breed);
    }
}

// Composition: Car "có" Engine thay vì "là" Engine.
class Engine { void run() { System.out.println("Engine: vroom"); } }
class Car {
    private final Engine engine = new Engine(); // HAS-A
    void start() { System.out.print("Car khởi động -> "); engine.run(); }
}
