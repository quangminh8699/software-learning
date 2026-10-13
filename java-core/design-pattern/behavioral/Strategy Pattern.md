## STRATEGY PATTERN LÀ GÌ?
Strategy Pattern là một **behavioral design pattern** (mẫu thiết kế thuộc nhóm hành vi).
Nó cho phép bạn định nghĩa một **họ các thuật toán (nhiều cách làm cùng một việc)**, đặt mỗi cách
vào một class riêng, và làm cho chúng **có thể thay thế lẫn nhau (interchangeable)** lúc runtime.

Object sử dụng thuật toán không cần quan tâm nó chạy *như thế nào* — chỉ cần gọi. Bạn có thể **đổi hành vi**
mà không cần sửa chính object đó.

> Hiểu đơn giản: *"Cùng một mục tiêu, nhưng tôi có thể đổi CÁCH LÀM bất cứ lúc nào."*

---

## Ví dụ đời thường 🗺️

Tưởng tượng bạn muốn đi từ nhà đến công ty. Có nhiều **strategy (cách đi)** khác nhau:

- 🚶 Đi bộ (rẻ, chậm)
- 🚌 Xe buýt (trung bình)
- 🚕 Taxi (nhanh, đắt)

**Mục tiêu vẫn giống nhau** ("đến công ty"), nhưng **cách làm có thể đổi**. Buổi sáng bạn có thể đi
taxi (vì trễ giờ), buổi tối bạn đi bộ (vì trời mát). Bạn — người đi — không đổi; chỉ *cách di chuyển* đổi.

Đó chính là Strategy Pattern: **một mục tiêu, nhiều cách thực hiện thay thế được cho nhau.**

---

## Vấn đề (khi KHÔNG dùng Strategy)

Khi nhồi hết mọi hành vi vào một class bằng `if-else`, class sẽ phình to và khó mở rộng.

```java
class Traveler {
    public void travel(String mode) {
        if (mode.equals("walk")) {
            System.out.println("Đi bộ đến công ty 🚶");
        } else if (mode.equals("bus")) {
            System.out.println("Đi xe buýt 🚌");
        } else if (mode.equals("taxi")) {
            System.out.println("Đi taxi 🚕");
        }
        // Thêm "xe đạp" nghĩa là lại phải sửa class này 😩
    }
}
```

**Các vấn đề:**
- Mỗi cách đi mới buộc bạn phải sửa class `Traveler`.
- Class ôm quá nhiều hành vi không liên quan → khó đọc, khó test.
- Vi phạm **Open/Closed Principle** (cứ phải sửa code có sẵn hoài).

---

## Giải pháp (khi dùng Strategy)

### Bước 1 — Định nghĩa strategy interface (kiểu "cách làm" chung)

```java
interface TravelStrategy {
    void go();
}
```

### Bước 2 — Tạo các concrete strategy (mỗi thuật toán một class riêng)

```java
class WalkStrategy implements TravelStrategy {
    public void go() { System.out.println("Đi bộ đến công ty 🚶"); }
}

class BusStrategy implements TravelStrategy {
    public void go() { System.out.println("Đi xe buýt 🚌"); }
}

class TaxiStrategy implements TravelStrategy {
    public void go() { System.out.println("Đi taxi 🚕"); }
}
```

### Bước 3 — Context giữ một strategy và có thể đổi nó

```java
class Traveler {
    private TravelStrategy strategy;

    // Chọn strategy lúc khởi tạo, hoặc đổi sau này
    public Traveler(TravelStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(TravelStrategy strategy) {
        this.strategy = strategy; // đổi hành vi ngay lúc runtime!
    }

    public void startTrip() {
        strategy.go(); // Traveler không cần biết CÁCH LÀM — chỉ ủy quyền (delegate)
    }
}
```

### Bước 4 — Cách dùng: chọn và đổi hành vi thoải mái

```java
Traveler traveler = new Traveler(new TaxiStrategy());
traveler.startTrip();  // Đi taxi 🚕  (đang trễ giờ!)

traveler.setStrategy(new WalkStrategy());
traveler.startTrip();  // Đi bộ đến công ty 🚶  (trời mát)
```

> Để ý: muốn thêm chế độ "Bike", bạn chỉ cần tạo class `BikeStrategy`. **Bạn không hề đụng vào class `Traveler`.**

---

## Một ví dụ kinh điển — Phương thức thanh toán 💳

Một shop online cho khách trả bằng Credit Card, PayPal, hoặc Cash. Cùng một hành động ("thanh toán"), khác cách làm.

```java
interface PaymentStrategy {
    void pay(double amount);
}

class CreditCardPayment implements PaymentStrategy {
    public void pay(double amount) { System.out.println("Đã trả $" + amount + " bằng Credit Card 💳"); }
}

class PayPalPayment implements PaymentStrategy {
    public void pay(double amount) { System.out.println("Đã trả $" + amount + " qua PayPal 🅿️"); }
}

class CashPayment implements PaymentStrategy {
    public void pay(double amount) { System.out.println("Đã trả $" + amount + " bằng tiền mặt 💵"); }
}

class ShoppingCart {
    private PaymentStrategy payment;

    public void setPayment(PaymentStrategy payment) { this.payment = payment; }

    public void checkout(double amount) {
        payment.pay(amount); // ủy quyền cho strategy đã chọn
    }
}

// Cách dùng
ShoppingCart cart = new ShoppingCart();
cart.setPayment(new PayPalPayment());
cart.checkout(59.99); // Đã trả $59.99 qua PayPal 🅿️
```

Khách chọn phương thức thanh toán lúc runtime; giỏ hàng không cần quan tâm là cái nào.

---

## Factory vs Strategy — Đừng nhầm lẫn

Cả hai đều dùng interface, nhưng giải quyết **hai vấn đề khác nhau**:

| | Factory Pattern | Strategy Pattern |
|---|-----------------|------------------|
| Nhóm | Creational (về việc **tạo** object) | Behavioral (về việc **chọn hành vi**) |
| Trả lời câu hỏi | *"Tôi nên TẠO object NÀO?"* | *"Tôi nên LÀM việc này NHƯ THẾ NÀO?"* |
| Ví dụ | Gọi pizza theo tên | Chọn cách đi lại |
| Trọng tâm | Giấu việc khởi tạo object | Đổi thuật toán lúc runtime |

> Chúng thường **kết hợp với nhau**: một factory có thể tạo ra đúng strategy object cho bạn.

---

## Khi nào nên dùng?

| Dùng Strategy khi... | Ví dụ |
|----------------------|-------|
| Có nhiều cách làm cùng một việc | Sorting, nén dữ liệu, định tuyến |
| Muốn đổi hành vi lúc runtime | Đổi cách thanh toán hoặc cách tính giảm giá |
| Bạn cứ viết `if-else` để chọn hành vi | Thay mỗi nhánh bằng một strategy class |
| Muốn test từng thuật toán độc lập | Test riêng `TaxiStrategy` |

> ⚠️ **Đừng lạm dụng.** Nếu chỉ có một hai hành vi cố định không bao giờ đổi thì một method đơn giản hoặc `if-else` là đủ. Strategy tỏa sáng khi hành vi **tăng dần** hoặc cần **thay đổi linh hoạt**.

---

## Tóm tắt

```
Strategy Pattern
├── Ý tưởng    → Đặt mỗi thuật toán vào class riêng; thay thế lẫn nhau được
├── Ví dụ      → Đi làm: đi bộ / xe buýt / taxi (cùng mục tiêu, khác cách làm)
├── Thành phần
│   ├── Strategy interface   → TravelStrategy.go()
│   ├── Concrete strategies  → WalkStrategy, BusStrategy, TaxiStrategy
│   └── Context              → Traveler (giữ một strategy, ủy quyền cho nó)
│
└── Lợi ích
    ├── Đổi hành vi lúc runtime (setStrategy)
    ├── Thêm hành vi mới mà không sửa context (Open/Closed)
    └── Mỗi thuật toán độc lập, dễ test
```

> **Một câu tóm gọn:** Strategy Pattern thay một khối `if-else` lớn của các hành vi bằng những class thay thế được cho nhau, cho phép bạn chọn hoặc đổi *cách* làm một việc lúc runtime mà không cần sửa code đang sử dụng nó.
