## FACTORY PATTERN LÀ GÌ?
Factory Pattern là một **creational design pattern** (mẫu thiết kế thuộc nhóm khởi tạo đối tượng).
Thay vì tạo object trực tiếp bằng `new`, bạn nhờ một object đặc biệt gọi là **factory** tạo giúp.
Factory sẽ quyết định **tạo ra concrete class nào**, nên phần code gọi không cần biết chi tiết bên trong.

> Hiểu đơn giản: *"Nói cho tôi bạn muốn CÁI GÌ, đừng bận tâm nó được TẠO RA như thế nào."*

---

## Ví dụ đời thường 🍕

Tưởng tượng bạn bước vào một **tiệm pizza**. Bạn không tự vào bếp lấy bột, phô mai, lò nướng để
tự làm bánh. Bạn chỉ cần nói với **thu ngân (chính là factory)**: *"Cho tôi một cái pizza phô mai."*

- **Bạn (phần code gọi)** chỉ cần biết *tên* món mình muốn.
- **Nhà bếp (factory)** biết công thức và tạo ra đúng loại pizza.
- Nếu tiệm thêm món "pizza rau củ" mới, **cách bạn gọi món không đổi** — bạn chỉ đọc tên món mới.

Đó chính xác là những gì Factory Pattern làm trong code.

---

## Vấn đề (khi KHÔNG dùng Factory)

Khi tạo object trực tiếp, phần code gọi phải biết mọi concrete class và cách tạo chúng.
Thêm một loại mới buộc bạn phải sửa code ở khắp nơi.

```java
// Caller phải biết TẤT CẢ concrete class
Pizza pizza;
if (type.equals("cheese")) {
    pizza = new CheesePizza();
} else if (type.equals("veggie")) {
    pizza = new VeggiePizza();
} else if (type.equals("pepperoni")) {
    pizza = new PepperoniPizza();
}
// Đoạn if-else xấu xí này bị copy-paste ở mọi nơi cần tạo pizza 😩
```

**Các vấn đề:**
- Logic khởi tạo bị lặp lại ở nhiều chỗ.
- Thêm một loại pizza mới nghĩa là sửa mọi khối `if-else` → dễ sót.
- Caller bị **tight coupling** (phụ thuộc chặt) vào các concrete class.

---

## Giải pháp (khi dùng Factory)

### Bước 1 — Định nghĩa một kiểu chung (interface)

```java
interface Pizza {
    void prepare();
}
```

### Bước 2 — Tạo các concrete product (sản phẩm cụ thể)

```java
class CheesePizza implements Pizza {
    public void prepare() { System.out.println("Đang làm Cheese Pizza 🧀"); }
}

class VeggiePizza implements Pizza {
    public void prepare() { System.out.println("Đang làm Veggie Pizza 🥦"); }
}

class PepperoniPizza implements Pizza {
    public void prepare() { System.out.println("Đang làm Pepperoni Pizza 🍖"); }
}
```

### Bước 3 — Xây dựng Factory (người thu ngân)

```java
class PizzaFactory {
    public Pizza createPizza(String type) {
        switch (type) {
            case "cheese":    return new CheesePizza();
            case "veggie":    return new VeggiePizza();
            case "pepperoni": return new PepperoniPizza();
            default: throw new IllegalArgumentException("Không có loại pizza: " + type);
        }
    }
}
```

### Bước 4 — Caller trở nên gọn gàng, sạch sẽ

```java
PizzaFactory factory = new PizzaFactory();

Pizza pizza = factory.createPizza("cheese"); // chỉ cần gọi theo tên
pizza.prepare();                             // Đang làm Cheese Pizza 🧀
```

> Đoạn `if-else` / `switch` giờ nằm ở **MỘT chỗ duy nhất** (bên trong factory). Caller không bao giờ thấy nó.

---

## Tại sao cách này tốt hơn?

- **Chỉ sửa một chỗ:** Thêm pizza mới = thêm 1 class + 1 dòng trong factory. Code của caller không đổi.
- **Loose coupling (phụ thuộc lỏng):** Caller chỉ phụ thuộc vào interface `Pizza`, không phụ thuộc concrete class.
- **Tái sử dụng:** Mọi nơi trong app đều tạo pizza theo cùng một cách.

Điều này hỗ trợ trực tiếp cho **Open/Closed Principle** trong SOLID — mở để mở rộng (thêm pizza), đóng với việc sửa đổi (caller giữ nguyên).

---

## Một ví dụ đời thường khác 🚗

Một **nhà máy xe hơi**: bạn đặt một chiếc "SUV" hoặc "Sedan". Bạn không tự lắp động cơ và bánh xe.

```java
interface Car {
    void drive();
}

class Sedan implements Car {
    public void drive() { System.out.println("Đang lái một chiếc Sedan êm ái"); }
}

class SUV implements Car {
    public void drive() { System.out.println("Đang lái một chiếc SUV to lớn"); }
}

class CarFactory {
    public Car createCar(String type) {
        if (type.equals("sedan")) return new Sedan();
        if (type.equals("suv"))   return new SUV();
        throw new IllegalArgumentException("Không có loại xe: " + type);
    }
}

// Cách dùng
Car car = new CarFactory().createCar("suv");
car.drive(); // Đang lái một chiếc SUV to lớn
```

---

## Khi nào nên dùng?

| Dùng Factory khi... | Ví dụ |
|---------------------|-------|
| Việc tạo object phức tạp hoặc lặp lại nhiều | Đọc config → tạo ra nhiều loại object khác nhau |
| Muốn giấu concrete class khỏi caller | Chỉ để lộ ra interface |
| Loại object chính xác được quyết định lúc runtime | Dựa trên input người dùng, config, hoặc loại file |
| Bạn cứ copy-paste `new X()` kèm `if-else` | Gom hết vào một factory |

> ⚠️ **Đừng lạm dụng.** Nếu bạn chỉ tạo một object đơn giản duy nhất thì dùng `new` bình thường là đủ. Factory chỉ đáng giá khi có **nhiều loại object** hoặc **logic khởi tạo phức tạp**.

---

## Tóm tắt

```
Factory Pattern
├── Ý tưởng    → Nhờ factory tạo object giúp (không dùng `new` trực tiếp)
├── Ví dụ      → Gọi pizza: bạn nói tên, nhà bếp làm bánh
├── Thành phần
│   ├── Product interface   → Pizza (kiểu chung)
│   ├── Concrete products   → CheesePizza, VeggiePizza, ...
│   └── Factory             → PizzaFactory.createPizza("cheese")
│
└── Lợi ích
    ├── Logic khởi tạo gom vào MỘT chỗ
    ├── Caller tách rời khỏi concrete class
    └── Dễ thêm loại mới (Open/Closed Principle)
```

> **Một câu tóm gọn:** Factory Pattern chuyển đám logic khởi tạo lộn xộn (`new` + `if-else`) vào một class chuyên trách, để phần còn lại của code chỉ cần gọi theo tên thứ nó muốn.
