## WHAT IS `record` ?

`record` là một loại **class đặc biệt** (chính thức từ **Java 16**) dùng để tạo ra một **immutable data carrier** — một class có nhiệm vụ duy nhất là **chứa dữ liệu**, không ôm logic phức tạp.

Bạn chỉ khai báo **tên + danh sách field**, còn lại compiler **tự sinh** hết phần boilerplate: constructor, accessor (getter), `equals()`, `hashCode()`, `toString()`.

```
record Point(int x, int y) { }
   ↑          ↑
 tên      "components" (các field) → compiler lo phần còn lại
```

> Hiểu đơn giản: *"Viết 1 dòng, compiler dựng cả class-chứa-data cho bạn — ít code, ít bug, đọc phát hiểu."*

---

## 📦 VÍ DỤ ĐỜI THƯỜNG — Tờ khai thông tin điền sẵn

Tưởng tượng bạn cần một **tờ phiếu ghi thông tin khách** gồm: tên, tuổi, số điện thoại.

- **Cách thủ công (class thường):** bạn tự kẻ bảng, tự viết chỗ điền tên, chỗ điền tuổi, tự viết quy tắc "hai phiếu giống nhau khi cả 3 ô trùng nhau", tự viết cách đọc to nội dung phiếu... → mệt, dài, dễ sai.
- **Cách `record`:** bạn chỉ cần nói *"phiếu này gồm 3 ô: tên, tuổi, sđt"* — nhà in **tự tạo sẵn** phiếu chuẩn: đã có chỗ điền, đã có sẵn cách so sánh 2 phiếu, đã có cách đọc nội dung.

Và quan trọng: phiếu đã điền rồi thì **niêm phong** — không tẩy xóa sửa lại được (immutable).

👉 `record` = **"khai báo cần những ô gì, phần dựng phiếu để máy làm"**.

---

## 😩 VẤN ĐỀ (khi KHÔNG dùng `record`)

Muốn một class chỉ để "gói" vài field, ngày xưa phải viết cả đống boilerplate:

```java
// Cách cũ — dài lê thê 😩
final class Point {
    private final int x;
    private final int y;

    Point(int x, int y) {          // constructor
        this.x = x;
        this.y = y;
    }

    int x() { return x; }          // getter
    int y() { return y; }

    @Override
    public boolean equals(Object o) {           // so sánh theo giá trị
        if (this == o) return true;
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }

    @Override
    public String toString() { return "Point[x=" + x + ", y=" + y + "]"; }
}
```

**Các vấn đề:**
- ~40 dòng chỉ để chứa **2 số**. Thêm 1 field → sửa ở **4-5 chỗ** (field, constructor, equals, hashCode, toString).
- Quên override `equals`/`hashCode` → bug so sánh, dùng sai trong `HashMap`/`HashSet`.
- Code lặp đi lặp lại, nhìn rối, che mất ý định thật sự: "đây chỉ là data thôi".

---

## ✅ GIẢI PHÁP — Toàn bộ ở trên gói gọn 1 dòng

```java
record Point(int x, int y) { }
```

Chỉ vậy. Compiler **tự sinh** cho bạn tất cả:

| Compiler tự tạo | Bạn được dùng luôn |
|---|---|
| **Canonical constructor** | `new Point(3, 4)` |
| **Accessor** (không có tiền tố `get`) | `p.x()`, `p.y()` |
| **`equals()` / `hashCode()`** | so sánh theo **giá trị**, chuẩn cho `HashMap`/`HashSet` |
| **`toString()`** | in ra `Point[x=3, y=4]` — debug cực sướng |
| Field `private final` | dữ liệu **bất biến** sau khi tạo |

```java
Point a = new Point(3, 4);
Point b = new Point(3, 4);

System.out.println(a.x());        // 3  — accessor, KHÔNG phải getX()
System.out.println(a);            // Point[x=3, y=4]  — toString tự có
System.out.println(a.equals(b));  // true  — so sánh theo giá trị, không phải địa chỉ
```

---

### Quay lại ví dụ của bạn — `PeriodView`

```java
private record PeriodView(LocalDate today, LocalDate payday, LocalDate dueDate, LocalDate endDate,
                          BigDecimal net, BigDecimal paidAmount, BigDecimal unallocated, boolean gimoOnly) {
}
```

Đọc dòng này ra ý nghĩa:
- Đây là **nested record** đặt `private` bên trong một class khác → dùng làm **object tạm nội bộ**, thường để **gom nhiều giá trị trả về** từ một phép tính (ở đây là dữ liệu tính toán của một "kỳ" thanh toán).
- 8 field trong ngoặc = 8 **components**. Compiler tự cho bạn: constructor 8 tham số, 8 accessor (`pv.net()`, `pv.paidAmount()`, `pv.gimoOnly()`...), cùng `equals`/`hashCode`/`toString`.
- Cặp `{ }` để trống là **bình thường** — chỉ cần thêm code vào đó khi muốn viết thêm method hoặc validation.

```java
PeriodView pv = new PeriodView(today, payday, dueDate, endDate,
                               net, paidAmount, unallocated, false);

BigDecimal remain = pv.net().subtract(pv.paidAmount());  // đọc data ra tính toán
// pv.paidAmount = ...   // ❌ không có setter → không sửa được
```

---

### Thêm validation — Compact constructor

Muốn kiểm tra dữ liệu đầu vào, dùng **compact constructor** (không cần liệt kê lại tham số):

```java
record PeriodView(BigDecimal net, BigDecimal paidAmount /* ... */) {
    PeriodView {                          // ← compact constructor
        if (net.signum() < 0)
            throw new IllegalArgumentException("net không được âm");
        // this.net = net; ... compiler tự gán sau khối này
    }
}
```

> `record` không hề "yếu": vẫn thêm được **method, static factory, compact constructor, implements interface**. Nó chỉ bỏ giúp phần boilerplate lặp lại.

---

## 🎯 TẠI SAO TỐT HƠN?

- **Ngắn & rõ ý định:** nhìn là biết ngay "class này chỉ để chứa data".
- **Ít bug:** `equals`/`hashCode` do compiler sinh → luôn đúng và đồng bộ với danh sách field.
- **Immutable sẵn:** mọi field `private final`, không setter → **thread-safe** khi chia sẻ giữa các thread (an toàn *publication*, xem [final]), tránh được [Race condition] mà không cần lock.
- **Dễ bảo trì:** thêm/bớt field chỉ sửa **1 chỗ** (danh sách trong ngoặc).

---

## `record` vs `class` thường

| | `record` | `class` thường |
|---|---|---|
| Mục đích | Chứa data (immutable) | Đa năng, có state + behavior |
| Boilerplate | Compiler tự sinh | Tự viết tay |
| Field | Luôn `private final` | Tùy ý (mutable được) |
| Setter | Không (immutable) | Có thể có |
| Kế thừa | **Không extends được** (ngầm `final`) | Extends thoải mái |
| Accessor | `p.x()` | Tùy quy ước, thường `getX()` |

---

## Khi nào nên dùng?

| Dùng khi... | Ví dụ |
|---|---|
| DTO / Request-Response model | `record LoginRequest(String user, String pass)` |
| Value object (bất biến) | `record Money(BigDecimal amount, String currency)` |
| Gom nhiều giá trị trả về từ 1 method | `PeriodView` trong ví dụ trên |
| Key phức hợp cho `Map` / `Set` | `record Coord(int x, int y)` (equals/hashCode sẵn) |

> ⚠️ **Đừng lạm dụng.** KHÔNG dùng `record` khi:
> - Cần object **mutable** (đổi state sau khi tạo).
> - Cần **kế thừa** class khác — `record` ngầm `final`, không extends được.
> - Làm JPA `@Entity` — Hibernate cần constructor rỗng + setter → không hợp với record.

---

## Summary

```
record = class-chứa-data, compiler tự sinh boilerplate

record Point(int x, int y) { }   ← khai báo 1 dòng
        └── components (field)

Compiler tự tạo:
  ├── constructor          → new Point(3, 4)
  ├── accessor (KHÔNG get)  → p.x(), p.y()
  ├── equals / hashCode     → so sánh theo GIÁ TRỊ
  └── toString              → Point[x=3, y=4]

Đặc tính:
  ├── field private final   → IMMUTABLE, không setter, thread-safe
  └── ngầm final            → KHÔNG extends được

Thêm được: method, static factory, compact constructor (validation), implements interface
Dùng cho:  DTO, value object, gom giá trị trả về, map key
```

> **Một câu tóm gọn:** `record` giúp bạn khai báo class-chứa-data **bất biến** trong một dòng — compiler lo hết constructor/accessor/`equals`/`hashCode`/`toString`, còn bạn chỉ tập trung vào *dữ liệu gồm những gì*. Xem thêm [final] (immutability & thread-safe), [Race condition] (vì sao immutable an toàn đa luồng).
