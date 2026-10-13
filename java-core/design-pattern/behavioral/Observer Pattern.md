## OBSERVER PATTERN LÀ GÌ?
Observer Pattern là một **behavioral design pattern** (mẫu thiết kế thuộc nhóm hành vi).
Nó định nghĩa một mối quan hệ **một-nhiều (one-to-many)** giữa các object: khi **một object (subject)**
thay đổi trạng thái, **tất cả các object phụ thuộc (observers)** sẽ được **thông báo và cập nhật tự động**.

> Hiểu đơn giản: *"Khi tôi có tin mới, tất cả những ai đăng ký theo dõi sẽ được báo ngay — tôi không cần biết họ là ai."*

---

## Ví dụ đời thường 📺

Tưởng tượng một **kênh YouTube**. Khi bạn **subscribe (đăng ký)**, mỗi lần kênh **đăng video mới**,
bạn sẽ nhận được **thông báo** tự động. Kênh không cần biết cụ thể từng người đăng ký là ai — nó chỉ
cần "phát thông báo cho tất cả subscriber".

- **Kênh YouTube** = **Subject** (đối tượng được theo dõi).
- **Người đăng ký** = **Observer** (người theo dõi).
- **Subscribe / Unsubscribe** = đăng ký / hủy đăng ký nhận thông báo.
- **Đăng video mới** = trạng thái thay đổi → tất cả observer được báo.

Đó chính là Observer Pattern.

---

## Vấn đề (khi KHÔNG dùng Observer)

Giả sử một `WeatherStation` (trạm thời tiết) cần báo nhiệt độ mới cho nhiều màn hình hiển thị.
Nếu subject phải tự gọi từng màn hình một, nó bị **gắn chặt** vào các màn hình cụ thể:

```java
class WeatherStation {
    private PhoneDisplay phone;
    private TvDisplay tv;

    public void setTemperature(int temp) {
        phone.update(temp); // phải biết đích danh từng màn hình
        tv.update(temp);
        // Thêm một màn hình mới → lại phải sửa class này 😩
    }
}
```

**Các vấn đề:**
- Subject bị **tight coupling** vào từng observer cụ thể.
- Thêm/bớt một observer buộc phải sửa code của subject.
- Không linh hoạt: không thể đăng ký/hủy lúc runtime.

---

## Giải pháp (khi dùng Observer)

### Bước 1 — Định nghĩa interface Observer (người theo dõi)

```java
interface Observer {
    void update(int temperature);
}
```

### Bước 2 — Định nghĩa Subject (đối tượng được theo dõi)

```java
import java.util.ArrayList;
import java.util.List;

class WeatherStation {
    private List<Observer> observers = new ArrayList<>(); // danh sách người theo dõi
    private int temperature;

    public void subscribe(Observer o)   { observers.add(o); }     // đăng ký
    public void unsubscribe(Observer o) { observers.remove(o); }  // hủy đăng ký

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        notifyAllObservers(); // trạng thái đổi → báo cho tất cả
    }

    private void notifyAllObservers() {
        for (Observer o : observers) {
            o.update(temperature); // subject không cần biết observer là loại gì
        }
    }
}
```

### Bước 3 — Tạo các concrete observer

```java
class PhoneDisplay implements Observer {
    public void update(int temperature) {
        System.out.println("📱 Điện thoại: nhiệt độ hiện tại là " + temperature + "°C");
    }
}

class TvDisplay implements Observer {
    public void update(int temperature) {
        System.out.println("📺 TV: nhiệt độ hiện tại là " + temperature + "°C");
    }
}
```

### Bước 4 — Cách dùng: đăng ký rồi nhận thông báo tự động

```java
WeatherStation station = new WeatherStation();

Observer phone = new PhoneDisplay();
Observer tv    = new TvDisplay();

station.subscribe(phone); // đăng ký theo dõi
station.subscribe(tv);

station.setTemperature(30);
// 📱 Điện thoại: nhiệt độ hiện tại là 30°C
// 📺 TV: nhiệt độ hiện tại là 30°C

station.unsubscribe(tv);  // TV hủy theo dõi

station.setTemperature(25);
// 📱 Điện thoại: nhiệt độ hiện tại là 25°C   (chỉ còn điện thoại nhận)
```

> Subject chỉ biết đến interface `Observer`. Muốn thêm màn hình mới (ví dụ `WebDisplay`), bạn **không cần sửa `WeatherStation`** — chỉ cần tạo class mới và `subscribe`.

---

## Tại sao cách này tốt hơn?

- **Loose coupling (phụ thuộc lỏng):** Subject không cần biết observer là loại gì, chỉ biết chúng có method `update()`.
- **Linh hoạt lúc runtime:** Đăng ký / hủy đăng ký bất cứ lúc nào.
- **Dễ mở rộng:** Thêm observer mới không đụng vào subject (Open/Closed Principle).

---

## Bạn đã gặp Observer ở đâu rồi? 🌍

Observer có mặt ở rất nhiều nơi bạn dùng hằng ngày:

- **Nút "Like/Subscribe"** trên YouTube, Facebook → nhận thông báo.
- **Event listener** trong lập trình UI: `button.addEventListener("click", ...)` — nút bấm là subject, hàm xử lý là observer.
- **Spring `ApplicationEvent`** và `@EventListener` — cơ chế publish/subscribe.
- **Reactive programming** (RxJava, Reactor): `Observable` và `Subscriber` chính là tên gọi từ pattern này.

---

## Khi nào nên dùng?

| Dùng Observer khi... | Ví dụ |
|----------------------|-------|
| Một thay đổi cần cập nhật nhiều nơi | Giá cổ phiếu đổi → nhiều biểu đồ cập nhật |
| Số lượng "người nghe" thay đổi lúc runtime | Người dùng bật/tắt nhận thông báo |
| Muốn subject và observer tách rời nhau | Hệ thống event / notification |

> ⚠️ **Lưu ý:** Nếu có quá nhiều observer, việc thông báo có thể chậm. Ngoài ra nhớ **unsubscribe** khi không cần nữa để tránh **memory leak** (observer bị giữ lại mãi trong danh sách).

---

## Tóm tắt

```
Observer Pattern
├── Ý tưởng    → Subject đổi trạng thái → tự động báo cho mọi Observer đã đăng ký
├── Ví dụ      → Kênh YouTube đăng video → tất cả subscriber nhận thông báo
├── Thành phần
│   ├── Subject             → WeatherStation (giữ danh sách + phát thông báo)
│   ├── Observer interface  → Observer.update()
│   └── Concrete observers  → PhoneDisplay, TvDisplay, ...
│
├── Thao tác   → subscribe / unsubscribe / notify
└── Lợi ích
    ├── Loose coupling giữa subject và observer
    ├── Đăng ký / hủy linh hoạt lúc runtime
    └── Thêm observer mới không sửa subject (Open/Closed)
```

> **Một câu tóm gọn:** Observer Pattern cho phép một object phát thông báo tới nhiều object theo dõi mà không cần biết chúng là ai, giúp các thành phần tự động cập nhật khi có thay đổi mà vẫn tách rời nhau.
