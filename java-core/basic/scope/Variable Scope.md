## WHAT IS VARIABLE SCOPE ?

**Scope** của một biến là **vùng code mà tại đó biến "nhìn thấy được"** (dùng được bằng tên). Ra khỏi vùng đó, cái tên biến **không còn tồn tại** với compiler — dùng tới sẽ báo lỗi.

Đi kèm scope là **lifetime (vòng đời)**: biến *sống* (chiếm bộ nhớ) trong bao lâu. Scope trả lời "**nhìn thấy ở đâu**", lifetime trả lời "**tồn tại tới khi nào**" — hai thứ liên quan nhưng không giống nhau.

```
Java có các tầng scope, từ RỘNG tới HẸP:
1. Class / Static scope   — field static, dùng chung cả class
2. Instance scope         — field thường, thuộc từng object
3. Method / Local scope   — biến khai báo trong method
4. Block scope            — biến trong { } của if/for/while
5. Parameter scope        — tham số của method (một dạng local)
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "Đã khai báo biến thì chỗ nào trong class cũng xài được." **SAI.** Biến local/block chỉ sống trong đúng cặp `{ }` khai báo nó. Ra khỏi ngoặc là **biến mất** — compile báo `cannot find symbol`. Chỉ field (class/instance) mới dùng được rộng khắp class.

---

## 🏠 VÍ DỤ ĐỜI THƯỜNG — Đồ đạc trong các phòng của một ngôi nhà

Coi ngôi nhà là **class**, mỗi phòng là một **block `{ }`**.

- Cái **tủ lạnh ở bếp chung** (đặt ngoài mọi phòng) → **ai ở phòng nào cũng dùng được** = **field của class**.
- Cái **bàn chải trong phòng tắm** → **chỉ dùng trong phòng tắm**, ra ngoài không có = **biến khai báo trong block**.
- Khi bạn **rời khỏi phòng** (ra khỏi `{ }`), mọi đồ "để tạm trong phòng đó" **coi như mất** — lần sau vào phòng lại phải lấy đồ mới.

👉 Nguyên tắc: **đồ càng để ở phòng riêng (scope càng hẹp) càng gọn gàng, ít lẫn lộn.** Cùng lý do, code nên khai báo biến ở scope **hẹp nhất có thể**.

---

## 1. Class / Static & Instance Scope — Field

> "Field sống cùng class (static) hoặc cùng object (instance) — dùng được trong toàn bộ class."

```java
class Player {
    static int playerCount = 0;   // CLASS scope — dùng chung mọi Player, sống suốt app
    String name;                  // INSTANCE scope — mỗi object một bản riêng

    Player(String name) {
        this.name = name;
        playerCount++;            // truy cập được cả static lẫn instance field ở đây
    }

    void printInfo() {
        // name và playerCount đều "nhìn thấy" trong mọi method của class
        System.out.println(name + " — tổng số player: " + playerCount);
    }
}
```

- **Static field** (`playerCount`): **một bản duy nhất** dùng chung, truy cập qua `Player.playerCount`.
- **Instance field** (`name`): **mỗi object một bản**, sống chừng nào object còn được tham chiếu.

---

## 2. Method (Local) & Parameter Scope

> "Biến local và tham số chỉ sống bên trong method, mất ngay khi method kết thúc."

```java
class Calculator {
    int add(int a, int b) {       // a, b là PARAMETER scope — chỉ trong method add
        int sum = a + b;          // sum là LOCAL scope — chỉ trong method add
        return sum;
    }                             // ra khỏi đây: a, b, sum đều biến mất

    void show() {
        // System.out.println(sum); // ❌ LỖI compile — sum không thuộc scope này
    }
}
```

> 📌 Biến local **bắt buộc phải gán giá trị trước khi dùng** (không có giá trị mặc định như field). `int x; System.out.println(x);` → lỗi compile.

---

## 3. Block Scope — if / for / while / `{ }`

> "Biến khai báo trong một block chỉ tồn tại trong block đó."

```java
void demo(int[] arr) {
    if (arr.length > 0) {
        int first = arr[0];       // `first` chỉ sống trong block if
        System.out.println(first);
    }
    // System.out.println(first); // ❌ LỖI — first đã ra khỏi scope

    for (int i = 0; i < arr.length; i++) {  // `i` chỉ sống trong vòng for
        System.out.println(arr[i]);
    }
    // System.out.println(i);     // ❌ LỖI — i đã ra khỏi scope
}
```

**Lợi ích của block scope:** khai báo biến sát chỗ dùng → dễ đọc, tránh dùng nhầm, và biến được thu hồi sớm.

---

## 4. Variable Shadowing — Biến che khuất

> "Biến scope hẹp trùng tên với biến scope rộng hơn → biến hẹp 'che' biến rộng."

Khi một biến local/parameter trùng tên với field, **tên đó ưu tiên trỏ tới biến gần nhất (hẹp nhất)**. Field bị "che". Dùng `this.<field>` để chỉ rõ field.

```java
class Account {
    double balance = 100;         // field (instance scope)

    void deposit(double balance) { // parameter TRÙNG TÊN field → che field
        // balance ở đây là THAM SỐ, không phải field
        this.balance += balance;   // this.balance = field; balance = tham số
        //   ^^^^^^^^^^^^          ^^^^^^^
        //   field bị che          tham số
    }
}
```

> ⚠️ Đây là lý do constructor hay viết `this.name = name;` — vế trái `this.name` là **field**, vế phải `name` là **tham số** cùng tên. Quên `this.` → gán tham số cho chính nó, field không đổi (bug âm thầm).

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

### Scope được quyết định lúc COMPILE, bộ nhớ được cấp lúc RUNTIME

**Scope là chuyện của compiler** — nó dựa vào cặp `{ }` để biết cái tên nào hợp lệ ở đâu (gọi là *lexical scope* / *static scope*). Sai scope → **lỗi compile**, chưa cần chạy.

Nhưng **biến nằm ở đâu trong bộ nhớ** mới quyết định **lifetime**:

```
Loại biến        | Nằm ở đâu               | Sống khi nào
-----------------|-------------------------|----------------------------------
Local / param    | STACK frame của method  | Từ lúc khai báo → method return
Instance field   | Bên trong object (HEAP) | Chừng nào object còn reachable
Static field     | Class metadata / heap   | Từ lúc class được load → hết app
```

### Vì sao biến local "mất" khi method kết thúc?

Mỗi lần gọi method, JVM tạo một **stack frame** chứa các biến local của lần gọi đó. Khi method `return`, **cả frame bị pop (xóa)** → biến local biến mất tức thì. Đây là lý do local var không cần GC dọn — chúng tự mất theo stack.

```
Gọi add(2, 3):
  [ stack frame của add ]  ← a=2, b=3, sum=5  sống ở đây
Khi add return:
  [ frame bị pop ]         ← a, b, sum biến mất ngay
```

### Liên hệ với Garbage Collection

Biến local là một dạng **GC Root** (xem [Garbage Collection]). Khi một biến local trỏ tới object trên heap:

```java
void f() {
    Object o = new Object();  // o (trên stack) → object (trên heap)
}                             // f return → o biến mất → object mất tham chiếu → thành RÁC
```

Object không tự chết khi hết scope — nó chỉ **mất tham chiếu**, rồi **GC dọn sau**. Scope kết thúc = cắt tham chiếu = điều kiện để GC thu hồi.

---

## Summary

```
Variable Scope = vùng code biến "nhìn thấy được" (do compiler quyết định theo { })

Từ RỘNG → HẸP:
Class/Static ──┐ dùng chung cả class, sống suốt app
Instance ──────┤ mỗi object một bản, sống theo object (heap)
Method/Local ──┤ trong method, sống theo stack frame
Block ─────────┤ trong { } của if/for/while
Parameter ─────┘ tham số, một dạng local

Shadowing: biến hẹp trùng tên che biến rộng → dùng this.<field> để chỉ rõ
```

| Scope | Khai báo ở đâu | Sống khi nào | Truy cập từ đâu |
|---|---|---|---|
| **Static** | Ngoài method, có `static` | Class load → hết app | Toàn class + `Class.field` |
| **Instance** | Ngoài method, không `static` | Object còn reachable | Mọi method của class (qua `this`) |
| **Local** | Trong method | Đến khi method return | Chỉ trong method |
| **Block** | Trong `{ }` (if/for...) | Đến khi thoát block | Chỉ trong block đó |
| **Parameter** | Trong dấu `( )` của method | Suốt lần gọi method | Chỉ trong method |

> Quy tắc vàng: **khai báo biến ở scope hẹp nhất có thể**. Scope hẹp → dễ đọc, tránh shadowing ngoài ý muốn, và object được GC thu hồi sớm hơn. Xem thêm cơ chế dọn bộ nhớ ở [Garbage Collection] và tính đóng gói ở [What is OOP].
