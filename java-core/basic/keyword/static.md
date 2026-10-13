## WHAT IS `static` ?

`static` mang nghĩa **"thuộc về CLASS, không thuộc về từng object"**. Thành viên `static` **chỉ có một bản duy nhất**, dùng chung cho mọi instance — và tồn tại **ngay cả khi chưa tạo object nào**.

`static` áp được vào **4 chỗ**:

```
1. static FIELD   → biến dùng chung cho cả class (1 bản duy nhất)
2. static METHOD  → gọi qua tên class, KHÔNG cần object
3. static BLOCK   → chạy 1 lần khi class được load (khởi tạo)
4. static NESTED CLASS → class con không cần tham chiếu tới object bao ngoài
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "Trong static method dùng được `this` / truy cập thẳng field thường." **SAI.** `static` method **không có `this`** (vì không gắn với object nào), nên không đụng trực tiếp được instance field/method — phải qua một object cụ thể. Xem "static không có this" bên dưới.

---

## 📋 VÍ DỤ ĐỜI THƯỜNG — Bảng thông báo chung của cả lớp

Coi mỗi học sinh là một **object**, cả lớp là một **class**:

- **Sổ điểm riêng** của từng học sinh → **instance field** (mỗi người một bản).
- **Bảng thông báo treo tường** → **static field**: **cả lớp chung 1 cái**, ai sửa thì mọi người thấy.
- **Đếm sĩ số lớp** → static: không thuộc về học sinh nào cụ thể, mà thuộc về **cả lớp**.

👉 Câu hỏi để quyết định `static`: "Cái này thuộc về **từng object** (mỗi người một bản) hay thuộc về **cả class** (dùng chung)?" — chung thì `static`.

---

## 1. `static` Field — Biến dùng chung

> "Một bản duy nhất, mọi object cùng nhìn thấy và chia sẻ."

```java
class Player {
    static int count = 0;      // CLASS variable — 1 bản chung
    String name;               // INSTANCE variable — mỗi object riêng

    Player(String name) {
        this.name = name;
        count++;               // mọi lần new đều tăng CÙNG một biến count
    }
}

new Player("A");
new Player("B");
System.out.println(Player.count);  // 2 — truy cập qua TÊN CLASS, không cần object
```

> 📌 Truy cập static nên gọi qua **tên class** (`Player.count`), không qua object (`p.count`) — dù Java cho phép, gọi qua object gây hiểu nhầm rằng nó thuộc object.

---

## 2. `static` Method — Không cần object

> "Gọi thẳng qua tên class; không có this nên không đụng instance state."

```java
class MathUtils {
    static int square(int x) { return x * x; }   // không dùng state của object
}

int r = MathUtils.square(5);   // 25 — gọi không cần new MathUtils()
```

**static KHÔNG có `this`:**
```java
class Counter {
    int value;                 // instance field

    static void reset() {
        // value = 0;          // ❌ LỖI — static không có this, không biết object nào
    }

    void resetThis() {
        this.value = 0;        // ✅ instance method có this
    }
}
```

Đây là lý do `main` là `static`: JVM gọi được **mà chưa tạo object nào**.
```java
public static void main(String[] args) { ... }
```

---

## 3. `static` Block — Khởi tạo lúc load class

> "Chạy đúng một lần khi class được nạp, trước khi dùng bất cứ gì."

```java
class Config {
    static final Map<String, String> SETTINGS = new HashMap<>();

    static {                   // static initializer block
        SETTINGS.put("env", "prod");
        SETTINGS.put("version", "1.0");
        // chạy 1 lần khi class Config được load
    }
}
```

---

## 4. `static` Nested Class

> "Class con static không giữ tham chiếu tới object bao ngoài → nhẹ hơn inner class thường."

```java
class Outer {
    static class Builder {     // static nested — tạo được độc lập
        // không cần một Outer instance để tồn tại
    }
}
Outer.Builder b = new Outer.Builder();   // không cần new Outer() trước
```

> Inner class **không static** ngầm giữ tham chiếu tới `Outer.this` → dễ gây **memory leak** (giữ object ngoài sống lâu). Nếu nested class không cần object ngoài → luôn để `static`.

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

### static sống ở đâu, từ khi nào?

Thành viên static được cấp phát **khi class được load** (không phải khi `new`), nằm trong vùng metadata của class (**Metaspace** — xem [Garbage Collection]). Một bản duy nhất, **sống suốt vòng đời class** (thường là suốt app).

```
new object → cấp field thường trên HEAP (mỗi object 1 bản)
class load → cấp field static 1 lần (dùng chung), tồn tại tới khi class unload
```

### static method KHÔNG đa hình (không override — chỉ "hiding")

Method thường được dispatch theo **object thực tế lúc runtime** (đa hình — xem [What is OOP]). Method static được nối theo **kiểu khai báo lúc compile** — gọi là **method hiding**, không phải overriding:

```java
class Parent { static void who() { System.out.println("Parent"); } }
class Child extends Parent { static void who() { System.out.println("Child"); } }

Parent p = new Child();
p.who();   // in "Parent" — quyết định theo KIỂU biến (Parent), KHÔNG theo object thực (Child)
```

> ⚠️ Đây là bẫy phỏng vấn kinh điển: static method **không** tham gia đa hình.

### static field là GC Root — coi chừng leak

Static field là một **GC Root** (xem [Garbage Collection]). Object bị một `static` collection giữ sẽ **không bao giờ bị dọn** chừng nào class còn load → nguồn [Race Condition] và memory leak phổ biến:

```java
static final List<byte[]> CACHE = new ArrayList<>();  // thêm mãi không xóa → leak dần
```

### `static final` = hằng số

Ghép với [final] tạo **hằng số**. Với kiểu nguyên thủy/String, compiler **inline giá trị** thẳng vào nơi dùng:

```java
static final double PI = 3.14159;   // hằng, viết HOA theo quy ước
```

### Thứ tự khởi tạo khi class load

```
1. static field & static block   → chạy MỘT lần, theo thứ tự viết, khi class load
2. instance field & block         → chạy MỖI lần new
3. constructor                    → chạy sau cùng mỗi lần new
```

---

## Summary

```
static = "thuộc về CLASS, không thuộc object" — 1 bản dùng chung, có sẵn khi chưa new

static FIELD  → biến chung cả class          (truy cập qua Tên.class)
static METHOD → gọi không cần object, KHÔNG có this
static BLOCK  → chạy 1 lần khi class load
static NESTED → class con độc lập với object ngoài

⚠️ static method KHÔNG đa hình (method hiding, nối theo kiểu compile-time)
⚠️ static field là GC Root → giữ object → dễ memory leak
```

| Thành viên | Thuộc về | Truy cập | Lưu ý |
|---|---|---|---|
| **static field** | Class (1 bản) | `Class.field` | GC Root — cẩn thận leak |
| **instance field** | Từng object | qua object | Mỗi object một bản |
| **static method** | Class | `Class.method()` | Không có `this`, không đa hình |
| **static block** | Class | tự chạy khi load | Khởi tạo static phức tạp |
| **static nested class** | Class | `Outer.Nested` | Không giữ ref object ngoài |

> Quyết định `static` bằng câu hỏi: **"thuộc về từng object hay cả class?"** Chung cho cả class → static. Ghép với [final] để tạo hằng số. Xem [Variable Scope] cho phạm vi biến static, và [Garbage Collection] cho lý do static field dễ gây leak.
