# Bài 22: Generics — An toàn kiểu, wildcard & type erasure

Generics cho an toàn kiểu lúc compile — nhưng **biến mất lúc runtime** (type erasure), và đó là gốc của nhiều giới hạn khó hiểu.

## 📖 Mô tả
Generics tham số hoá kiểu: `List<String>` thay vì `List` thô. Compiler kiểm tra kiểu, tự chèn cast → an toàn + gọn. Nhưng JVM **xoá** thông tin kiểu sau khi biên dịch (**type erasure**) để tương thích ngược.

## 🔧 Kỹ thuật
- **Generic class**: `class Box<T> {...}`
- **Generic method**: `<T> T pick(T a, T b)`
- **Bounded**: `<T extends Number>` — giới hạn trên.
- **Wildcard**: `?`, `? extends T` (đọc), `? super T` (ghi).
- **PECS**: **P**roducer **E**xtends, **C**onsumer **S**uper.

## ⚙️ Dưới nắp capo (Under the hood)
- **Type erasure**: `Box<T>` sau biên dịch → field `T` thành `Object` (hoặc bound nếu có `extends`). `List<String>` và `List<Integer>` runtime là **cùng** class `ArrayList`. Compiler chèn cast tự động ở chỗ dùng.
```
   Compile-time           Runtime (đã erasure)
   Box<String>            Box (T → Object)
   list.get() → (String)  list.get() trả Object, compiler cast
```
- **Hệ quả của erasure** (câu hỏi phỏng vấn senior):
  - Không `new T()`, không `new T[]` (không biết kiểu lúc runtime).
  - Không `instanceof List<String>` (chỉ `instanceof List`).
  - Không overload chỉ khác tham số generic (`f(List<String>)` vs `f(List<Integer>)` → cùng chữ ký sau erasure → lỗi).
  - Static field không dùng được type param của class.
- **PECS** giải thích bằng an toàn kiểu:
  - `? extends Number` (producer): đọc ra `Number` an toàn, **không add** được (vì không biết con cụ thể).
  - `? super Integer` (consumer): add `Integer` an toàn, đọc ra chỉ chắc `Object`.
- **Reifiable vs non-reifiable**: mảng "nhớ" kiểu lúc runtime (reifiable) còn generic thì không → trộn mảng + generic sinh cảnh báo `unchecked`.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -p Box     # field value có kiểu Object (đã erasure)
```
Output mẫu:
```
Box<String>: HELLO
Box<Integer>: 43
max(3,7) = 7
max("a","z") = z
sum ints = 6.0
sum dbls = 4.0
sink sau khi ghi = [1, 2, 3]
ls.getClass() == li.getClass() ? true
```

## 🎨 Bản vẽ — PECS
```
        ┌─────────────────────────────────────┐
        │  List<? extends Number>  → ĐỌC       │  producer (lấy ra Number)
        │  List<? super Integer>   → GHI       │  consumer (bỏ Integer vào)
        └─────────────────────────────────────┘
   "Lấy ra dùng Extends, Bỏ vào dùng Super"
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Raw type** (`List` không tham số) → tắt kiểm tra kiểu, mất an toàn, cảnh báo `unchecked`. Tránh.
- Không tạo `T[]` trực tiếp → dùng `(T[]) new Object[n]` + `@SuppressWarnings` hoặc `Array.newInstance` (Reflection).
- Erasure khiến không phân biệt được `List<String>`/`List<Integer>` lúc runtime → khi cần kiểu runtime, truyền `Class<T>` (type token).
- Wildcard sai chiều (dùng `extends` khi cần add) → lỗi compile khó hiểu; nhớ PECS.
- Heap pollution: trộn generic + varargs (`@SafeVarargs`) cẩn thận.

## 🔗 Bài tiếp theo
👉 [23 — Collections: List](../23-collections-list)
