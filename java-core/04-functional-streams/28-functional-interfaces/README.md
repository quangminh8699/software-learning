# Bài 28: Functional Interfaces — Bộ chuẩn java.util.function

Lambda cần một "kiểu" để gắn vào — đó là functional interface. Biết bộ chuẩn = viết API hàm gọn và đúng.

## 📖 Mô tả
**Functional interface** = interface có **đúng 1 abstract method** (SAM — Single Abstract Method). `@FunctionalInterface` để compiler bảo đảm. `java.util.function` cung cấp bộ interface chuẩn.

## 🔧 Kỹ thuật — bộ cốt lõi
| Interface | Chữ ký | Ý nghĩa |
|-----------|--------|---------|
| `Predicate<T>` | `T -> boolean` | kiểm tra điều kiện |
| `Function<T,R>` | `T -> R` | biến đổi/ánh xạ |
| `Consumer<T>` | `T -> void` | tiêu thụ (side-effect) |
| `Supplier<T>` | `() -> T` | sinh giá trị |
| `UnaryOperator<T>` | `T -> T` | Function cùng kiểu |
| `BinaryOperator<T>` | `(T,T) -> T` | gộp 2 cùng kiểu |
| `BiFunction<T,U,R>` | `(T,U) -> R` | 2 đầu vào |

Ghép: `Predicate.and/or/negate`, `Function.andThen/compose`.

## ⚙️ Dưới nắp capo (Under the hood)
- `@FunctionalInterface` chỉ là **kiểm tra compile-time** (đúng 1 SAM); không bắt buộc nhưng nên có để tránh vô tình thêm method phá lambda.
- **Biến thể primitive** (`IntPredicate`, `IntFunction`, `ToIntFunction`, `IntUnaryOperator`...) tồn tại để **tránh autoboxing**: `Predicate<Integer>` box mỗi `int` thành `Integer` (object trên heap); `IntPredicate` nhận `int` thẳng → 0 rác. Cực quan trọng trong Stream xử lý số lớn.
- `default` method (`and/or/andThen`) cho phép **ghép** functional interface mà giữ nguyên tính "1 SAM".
- Method/constructor reference (bài 29) là cách viết gọn khác cho cùng các interface này.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
isEven(4): true
length("java"): 4
LOG: một thông điệp
supplier random < 1: true
6 even&positive? true
-2 even? true, even&positive? false
3 NOT even? true
mul(3,4)=12, max(3,4)=4
HELLO!
IntPredicate 7 lẻ? true
IntUnaryOperator inc(9)=10
Lọc chẵn: [2, 4, 6]
Lọc >3 : [4, 5, 6]
sum3(1,2,3)=6
```

## 🎨 Bản vẽ — chọn interface theo "hình dạng"
```
   Có input? ─ no ──▶ có output? ─ yes ─▶ Supplier<T>      ()->T
        │ yes
        ▼
   Có output? ─ no ──▶ Consumer<T>                          T->void
        │ yes
        ▼
   output là boolean? ─ yes ─▶ Predicate<T>                 T->boolean
        │ no
        ▼
   Function<T,R>                                             T->R
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Dùng biến thể primitive** trong hot path/Stream số → tránh hàng triệu autobox.
- `Consumer` mang **side-effect** → cẩn thận trong stream song song (không thread-safe).
- Functional interface chuẩn **không** khai báo checked exception → lambda ném checked phải bọc; cân nhắc interface tự định nghĩa.
- Đừng tạo functional interface tự định nghĩa nếu JDK đã có (chỉ tạo khi cần >2 tham số như `TriFunction`).
- Tên method khác nhau (`test/apply/accept/get`) → nhớ đúng để gọi.

## 🔗 Bài tiếp theo
👉 [29 — Method References](../29-method-references)
