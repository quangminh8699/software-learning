## TIGHT COUPLING VÀ LOOSE COUPLING LÀ GÌ?

**Coupling** (độ phụ thuộc) là mức độ một `class` phải biết và dựa vào chi tiết của `class` khác để làm việc.

- **Tight coupling** (phụ thuộc chặt): `class` A biết rõ `class` cụ thể của B, tự tạo B bằng `new`, hoặc dùng cả chi tiết bên trong của B. Khi B thay đổi, A rất dễ phải sửa theo.
- **Loose coupling** (phụ thuộc lỏng): A chỉ biết B **có thể làm gì** (thường qua một `interface`), không biết B được cài đặt ra sao. B có thể được thay thế mà A không cần sửa.

> **Hiểu đơn giản:** Tight coupling là "hàn chết", loose coupling là "cắm vào ổ cắm chuẩn".

---

## Ví dụ đời sống 🔌

Hãy so sánh hai cách lắp một chiếc đèn bàn trong phòng:

- **Cách 1 — hàn dây:** Dây điện của đèn được hàn thẳng vào đường điện trong tường. Đèn sáng bình thường. Nhưng muốn đổi sang đèn khác, hoặc cắm tạm cái quạt, bạn phải gọi thợ cắt dây và hàn lại.
- **Cách 2 — ổ cắm chuẩn:** Tường có một ổ cắm theo chuẩn chung. Đèn, quạt, sạc điện thoại… chỉ cần phích cắm đúng chuẩn là dùng được. Đổi thiết bị chỉ mất vài giây, đường điện trong tường không cần đụng tới.

Trong ví dụ này:

| Đời sống | Trong software |
|---|---|
| Đường điện trong tường | `class` sử dụng, ví dụ `OrderService` |
| Đèn, quạt, sạc điện thoại | Các `class` cụ thể, ví dụ `EmailNotifier`, `SmsNotifier` |
| Hàn dây trực tiếp | Gọi `new EmailNotifier()` ngay bên trong `OrderService` |
| Ổ cắm theo chuẩn chung | `interface Notifier` |
| Cắm thiết bị vào ổ | Truyền object vào qua `constructor` (**Dependency Injection**) |

Điểm chung quan trọng là: **hai bên chỉ thống nhất với nhau ở một "chuẩn" chung, không ai cần biết bên kia được làm ra như thế nào.**

---

## Vấn đề của tight coupling

Giả sử bạn viết `OrderService`. Sau khi đặt hàng thành công, hệ thống gửi email báo cho khách.

```java
// Có vấn đề: OrderService bị "hàn chết" với EmailNotifier
class EmailNotifier {
    void sendEmail(String to, String content) {
        System.out.println("Gửi email tới " + to + ": " + content);
    }
}

class OrderService {
    private final EmailNotifier notifier = new EmailNotifier(); // tự tạo dependency

    void placeOrder(String customer) {
        // ... lưu đơn hàng ...
        notifier.sendEmail(customer, "Đặt hàng thành công");
    }
}
```

Code chạy đúng. Vấn đề xuất hiện khi yêu cầu thay đổi:

- **Khó thay đổi:** Sếp muốn gửi SMS thay vì email. Bạn phải mở `OrderService` ra sửa, dù logic đặt hàng không hề đổi.
- **Thay đổi lan truyền:** `EmailNotifier` đổi tên method `sendEmail` thành `send`. Mọi `class` gọi `sendEmail` đều bị lỗi compile.
- **Khó test:** Muốn viết unit test cho `placeOrder` mà không gửi email thật? Không được, vì `OrderService` tự `new EmailNotifier()` bên trong, test không có cách nào thay nó bằng object giả.

Vấn đề cốt lõi không phải là từ khóa `new`, mà là **`OrderService` phụ thuộc vào một cách cài đặt cụ thể thay vì phụ thuộc vào thứ nó thực sự cần: "một cái gì đó gửi được thông báo"**.

---

## Cách chuyển sang loose coupling

1. **Xác định thứ mình thực sự cần:** `OrderService` không cần "email", nó cần "gửi thông báo".
2. **Mô tả nhu cầu đó bằng một `interface`:** đây chính là "ổ cắm chuẩn".
3. **Các `class` cụ thể cài đặt `interface`:** email, SMS… mỗi loại là một "thiết bị".
4. **Nhận dependency từ bên ngoài:** `OrderService` không tự `new`, mà nhận object qua `constructor`. Ai tạo `OrderService` sẽ quyết định cắm thiết bị nào.

```text
Tight:  OrderService ──new──▶ EmailNotifier

Loose:  OrderService ──▶ «interface» Notifier ◀── EmailNotifier
                                              ◀── SmsNotifier
```

Ở sơ đồ loose, mũi tên của cả hai phía đều trỏ vào `Notifier`. `OrderService` và `EmailNotifier` không còn biết nhau trực tiếp.

---

## Ví dụ code

### Bước 1 — Định nghĩa "ổ cắm chuẩn"

```java
interface Notifier {
    void send(String to, String content);
}
```

### Bước 2 — Các "thiết bị" cài đặt chuẩn đó

```java
class EmailNotifier implements Notifier {
    @Override
    public void send(String to, String content) {
        System.out.println("Gửi email tới " + to + ": " + content);
    }
}

class SmsNotifier implements Notifier {
    @Override
    public void send(String to, String content) {
        System.out.println("Gửi SMS tới " + to + ": " + content);
    }
}
```

### Bước 3 — `OrderService` chỉ biết `interface`

```java
class OrderService {
    private final Notifier notifier; // chỉ biết "gửi được thông báo"

    // Nhận dependency từ bên ngoài thay vì tự new
    OrderService(Notifier notifier) {
        this.notifier = notifier;
    }

    void placeOrder(String customer) {
        // ... lưu đơn hàng ...
        notifier.send(customer, "Đặt hàng thành công");
    }
}
```

### Bước 4 — Bên ngoài quyết định cắm gì

```java
public class Main {
    public static void main(String[] args) {
        OrderService emailService = new OrderService(new EmailNotifier());
        emailService.placeOrder("an@mail.com");

        OrderService smsService = new OrderService(new SmsNotifier());
        smsService.placeOrder("0901234567");
    }
}
```

Kết quả:

```text
Gửi email tới an@mail.com: Đặt hàng thành công
Gửi SMS tới 0901234567: Đặt hàng thành công
```

Đổi từ email sang SMS mà **không sửa một dòng nào trong `OrderService`**.

### Lợi ích khi test

Vì `OrderService` nhận `Notifier` từ bên ngoài, test có thể truyền vào một object giả:

```java
// Notifier giả: không gửi gì thật, chỉ ghi lại để kiểm tra
class FakeNotifier implements Notifier {
    String lastMessage;

    @Override
    public void send(String to, String content) {
        lastMessage = content;
    }
}

FakeNotifier fake = new FakeNotifier();
new OrderService(fake).placeOrder("test@mail.com");
System.out.println(fake.lastMessage); // Đặt hàng thành công
```

Điểm cần chú ý:

- `private final Notifier notifier`: kiểu khai báo là `interface`, không phải `class` cụ thể.
- `OrderService(Notifier notifier)`: dependency đi **vào** qua `constructor`. Kỹ thuật này gọi là **Dependency Injection**. Trong Spring, container sẽ tự làm bước "cắm" này cho bạn.

---

## Các dấu hiệu nhận biết tight coupling

| Dấu hiệu | Ví dụ | Hướng nới lỏng |
|---|---|---|
| Tự tạo dependency bên trong | `new EmailNotifier()` trong field hoặc method | Nhận qua `constructor` |
| Khai báo kiểu là `class` cụ thể | `private EmailNotifier notifier` | Khai báo bằng `interface` |
| Đi sâu vào chi tiết bên trong của object khác | `order.getCustomer().getAddress().getCity()` | Hỏi trực tiếp: `order.getShippingCity()` |
| Kiểm tra kiểu cụ thể để rẽ nhánh | `if (notifier instanceof EmailNotifier)` | Đưa hành vi vào chính `interface` |
| Phụ thuộc vào trạng thái `static` dùng chung | `Config.currentUser` được nhiều `class` đọc và ghi | Truyền dữ liệu qua tham số |

---

## So sánh với khái niệm dễ nhầm

Người mới hay nghe cụm **"high cohesion, low coupling"** và nhầm hai khái niệm này với nhau.

| Tiêu chí | Coupling | Cohesion |
|---|---|---|
| Đo cái gì | Mức phụ thuộc **giữa** các `class` | Mức liên quan của các phần **bên trong** một `class` |
| Mục tiêu | Càng thấp (lỏng) càng tốt | Càng cao càng tốt |
| Ví dụ xấu | `OrderService` tự `new EmailNotifier()` | `UserService` vừa lưu DB, vừa gửi email, vừa xuất báo cáo |
| Nguyên lý liên quan | Dependency Inversion Principle | Single Responsibility Principle |

> **Cách phân biệt nhanh:** Coupling nhìn **ra ngoài** (tôi dựa vào người khác bao nhiêu), cohesion nhìn **vào trong** (các phần của tôi có cùng mục đích không).

Xem thêm hai nguyên lý trên trong bài [What is SOLID](../solid/What%20is%20SOLID.md).

---

## Khi nào nên dùng loose coupling?

Nên nới lỏng khi:

- Phần phụ thuộc **có khả năng thay đổi**: cách gửi thông báo, cổng thanh toán, nơi lưu dữ liệu.
- Phần phụ thuộc **chạm tới thế giới bên ngoài**: database, gọi API, gửi email, đọc file. Đây là những thứ cần thay bằng object giả khi test.
- Cần **nhiều cách cài đặt** cho cùng một nhu cầu. Đây cũng là ý tưởng của [Strategy Pattern](../../design-pattern/behavioral/Strategy%20Pattern.md).

Không nên lạm dụng khi:

- Phụ thuộc vào thứ ổn định, gần như không bao giờ đổi: `String`, `List`, `LocalDate`, các `class` tiện ích của JDK. Không ai tạo `interface` bọc `String`.
- Chỉ có một cách cài đặt và không có lý do thực tế để thay. Tạo `interface` cho mọi `class` sẽ làm code nhiều file hơn và khó đọc hơn, trong khi không đem lại lợi ích.
- Các `class` vốn thuộc cùng một khối nhỏ, luôn thay đổi cùng nhau. Tách chúng ra chỉ tạo thêm tầng gián tiếp.

> ⚠️ **Đừng lạm dụng:** Loose coupling có chi phí: thêm `interface`, thêm tầng gián tiếp, khó lần theo code hơn khi đọc. Chỉ trả chi phí này ở những chỗ thực sự cần linh hoạt.

---

## Hiểu nhầm thường gặp

> ⚠️ **Hiểu nhầm:** "Loose coupling nghĩa là các `class` không phụ thuộc vào nhau."
>
> **Thực tế:** Phần mềm luôn cần các `class` làm việc cùng nhau, nên phụ thuộc là không tránh được. Loose coupling chỉ có nghĩa là phụ thuộc vào **thứ ổn định và tối thiểu** (một `interface` nhỏ) thay vì vào chi tiết cài đặt dễ thay đổi.

> ⚠️ **Hiểu nhầm:** "Cứ thêm `interface` là đã loose coupling."
>
> **Thực tế:** Nếu `OrderService` khai báo `Notifier` nhưng vẫn tự gọi `new EmailNotifier()` bên trong, hoặc vẫn ép kiểu `(EmailNotifier) notifier` để gọi method riêng, thì nó vẫn bị trói vào `EmailNotifier`. Điều quan trọng là `class` sử dụng **không còn biết** cài đặt cụ thể là gì.

### Lỗi phổ biến

- **`interface` quá to:** `interface` có hàng chục method buộc mọi `class` cài đặt phải biết quá nhiều. Nên giữ `interface` nhỏ, chỉ chứa thứ bên sử dụng thật sự cần.
- **Thay `new` bằng `static` getter:** `Notifier n = NotifierHolder.getInstance()` vẫn là phụ thuộc ẩn. Test vẫn khó thay object giả. Nên truyền dependency qua `constructor` để nó hiện rõ.

---

## Tóm tắt

```text
COUPLING
├── Là gì         → Mức độ một class phụ thuộc vào chi tiết của class khác
├── Tight         → Tự new class cụ thể, biết chi tiết bên trong
│                   → Khó đổi, thay đổi lan truyền, khó test
├── Loose         → Chỉ biết interface, nhận dependency từ bên ngoài
│                   → Dễ thay thế, dễ test, thay đổi được cô lập
├── Cách làm      → interface + Dependency Injection (qua constructor)
├── Nên dùng      → Chỗ hay đổi, chỗ chạm DB / API / email
└── Cẩn thận      → Không bọc interface cho mọi thứ; loose ≠ không phụ thuộc
```

> **Một câu ghi nhớ:** Hãy phụ thuộc vào **điều bạn cần** (gửi được thông báo), đừng phụ thuộc vào **người làm việc đó** (`EmailNotifier`).
