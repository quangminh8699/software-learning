## WHAT IS `final` ?

`final` là từ khóa mang nghĩa **"chốt lại, không cho thay đổi nữa"**. Sau khi được gán/định nghĩa, thứ được đánh dấu `final` **không thể bị gán lại, override, hay kế thừa** — tùy vào nó đứng ở đâu.

`final` áp được vào **3 chỗ**, mỗi chỗ một ý nghĩa:

```
1. final VARIABLE  → gán đúng 1 lần, không đổi được nữa (hằng)
2. final METHOD    → subclass KHÔNG được override
3. final CLASS     → KHÔNG class nào extends được (vd String, Integer)
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "`final` object = object bất biến (immutable)." **SAI.** `final` trên một biến tham chiếu chỉ khóa **cái tham chiếu** (không trỏ sang object khác được), **KHÔNG** khóa nội dung bên trong object. `final List` vẫn `add()` phần tử được. Xem mục "final reference" bên dưới — đây là điểm dễ sai nhất.

---

## 🖊️ VÍ DỤ ĐỜI THƯỜNG — Viết bằng bút mực không xóa được

Coi biến thường là **viết bằng bút chì** (tẩy đi ghi lại thoải mái), còn `final` là **viết bằng bút mực + ký tên**:

- Ghi số CMND lên hợp đồng bằng **bút mực** → **ghi 1 lần, không sửa được**. Đó là `final variable`.
- Nhưng nếu hợp đồng ghi "địa chỉ nhà số 5" bằng mực → **số nhà không đổi**, nhưng **đồ đạc trong nhà vẫn thay được**. Đó chính là bẫy `final reference`: khóa "địa chỉ" (tham chiếu), không khóa "nội dung nhà" (object).

👉 `final` = **"chốt cái tên/đường trỏ"**, không phải "đóng băng toàn bộ nội dung".

---

## 1. `final` Variable — Gán đúng một lần

> "Biến final phải được khởi tạo, và sau đó không gán lại được."

```java
final int MAX = 100;
// MAX = 200;          // ❌ LỖI compile — đã final, không gán lại

final int x;           // "blank final" — chưa gán ngay
x = 42;                // ✅ được gán MỘT lần
// x = 43;             // ❌ lần thứ hai là lỗi
```

**Blank final field — gán trong constructor:**
```java
class User {
    private final String id;   // chưa gán ở đây

    User(String id) {
        this.id = id;          // ✅ gán đúng 1 lần trong constructor
    }
    // sau khi object tạo xong, id KHÔNG bao giờ đổi
}
```

**`final` parameter — bảo vệ tham số khỏi bị gán đè trong method:**
```java
void process(final int count) {
    // count = 5;      // ❌ không cho đổi tham số → tránh nhầm lẫn
}
```

---

## 2. `final` Reference — Bẫy quan trọng nhất

> "final khóa ĐƯỜNG TRỎ, không khóa NỘI DUNG object."

```java
final List<String> list = new ArrayList<>();
list.add("a");         // ✅ nội dung object vẫn đổi được thoải mái
list.add("b");         // ✅
// list = new ArrayList<>();  // ❌ chỉ cái này bị chặn — không trỏ sang list khác

final int[] arr = {1, 2, 3};
arr[0] = 99;           // ✅ sửa phần tử OK
// arr = new int[5];   // ❌ đổi cả mảng thì không
```

> 📌 Muốn object **thật sự bất biến (immutable)**, `final` là chưa đủ. Phải: field `private final` + **không có setter** + copy dữ liệu mutable khi nhận vào/trả ra. `final` chỉ là một mảnh của bức tranh immutability.

---

## 3. `final` Method — Cấm override

> "Subclass không được viết lại method final — dùng để khóa hành vi cốt lõi."

```java
class Payment {
    final void validate() {    // subclass không được đổi logic này
        System.out.println("Kiểm tra bảo mật bắt buộc");
    }
}

class CardPayment extends Payment {
    // void validate() { }     // ❌ LỖI — không override method final được
}
```

---

## 4. `final` Class — Cấm kế thừa

> "Không class nào extends được — thiết kế đóng, an toàn."

```java
final class Constants {
    // không ai extends Constants được
}
// class More extends Constants { }   // ❌ LỖI compile
```

`String`, `Integer`, `LocalDate`... đều là `final class`. Điều này giúp chúng **bất biến và an toàn** — không ai tạo subclass để phá vỡ hành vi (vd làm `String` thay đổi được thì hỏng cả bảo mật, hashing...).

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

### Kiểm tra lúc COMPILE

Phần lớn quy tắc `final` do **compiler thực thi** (gán lại/override/extends → lỗi compile, chưa cần chạy). Compiler cũng đảm bảo blank final được gán **đúng một lần trên mọi nhánh code**.

### `static final` = hằng số compile-time (constant inlining)

Khi kết hợp với `static` (xem [static]), một `static final` kiểu nguyên thủy/String được compiler **thay thẳng giá trị vào chỗ dùng** (inline):

```java
static final int LIMIT = 100;
// mọi chỗ dùng LIMIT được compiler thay bằng số 100 luôn trong bytecode
```

> ⚠️ Hệ quả: nếu class A dùng hằng `B.LIMIT`, đổi `LIMIT` trong B rồi **chỉ biên dịch lại B**, class A vẫn giữ giá trị cũ đã inline. Phải **biên dịch lại cả A**.

### `final` field & An toàn đa luồng (JMM)

Java Memory Model có bảo đảm đặc biệt: **các `final` field được gán xong trong constructor sẽ hiển thị đúng với mọi thread** mà không cần đồng bộ hóa thêm (*safe publication*). Đây là lý do object immutable (toàn field final) **an toàn khi chia sẻ giữa các thread** — tránh được [Race Condition] mà không cần lock.

### `final` giúp JIT tối ưu

Vì biết giá trị không đổi, JIT compiler có thể **inline, cache, bỏ kiểm tra thừa** → đôi khi nhanh hơn. (Lợi ích nhỏ, đừng dùng `final` chỉ vì tốc độ — dùng vì tính đúng đắn & rõ ràng.)

---

## Summary

```
final = "chốt lại, không đổi được nữa" — áp vào 3 chỗ:

final VARIABLE → gán 1 lần        (blank final: gán trong constructor)
final METHOD   → cấm override
final CLASS    → cấm extends       (String, Integer...)

⚠️ final REFERENCE chỉ khóa đường trỏ, KHÔNG khóa nội dung object
   → final List vẫn add() được; muốn immutable cần nhiều hơn final
```

| Áp dụng cho | Ý nghĩa | Ví dụ |
|---|---|---|
| **Local variable** | Gán đúng 1 lần | `final int x = 5;` |
| **Field** | Bất biến sau khi tạo object | `private final String id;` (gán trong ctor) |
| **Parameter** | Không gán đè trong method | `void f(final int n)` |
| **Reference** | Không trỏ object khác (nội dung vẫn đổi) | `final List<String> l` |
| **Method** | Subclass không override | `final void validate()` |
| **Class** | Không ai extends | `final class String` |

> Quy tắc thực dụng: **mặc định để field là `final`** khi nó không cần đổi — code rõ ý định, an toàn đa luồng, ít bug gán nhầm. Ghép với [static] để tạo hằng số, và xem [Variable Scope] cho phạm vi biến, [Garbage Collection] cho vòng đời object.
