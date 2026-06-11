# Bài 16: static & final — Thành viên lớp, hằng & bất biến

Hai từ khoá nhỏ nhưng quyết định **nơi dữ liệu sống**, **an toàn thread**, và **cơ hội tối ưu của JIT**.

## 📖 Mô tả
- **`static`**: thành viên thuộc **class**, không thuộc object — dùng chung.
- **`final`**: "không đổi" — tuỳ ngữ cảnh: biến (không gán lại), method (không override), class (không kế thừa).

## 🔧 Kỹ thuật
| Dùng `final` cho | Ý nghĩa |
|------------------|---------|
| biến local / field | gán đúng **1 lần** |
| tham số | không gán lại trong method |
| method | lớp con **không** override được |
| class | **không** kế thừa được |

`static`: field (1 bản/class), method (gọi không cần object), block (init class), nested class.

## ⚙️ Dưới nắp capo (Under the hood)
- **static field** lưu ở **Method Area / Metaspace** theo class (không nhân theo object), khởi tạo lúc **class initialization** (`<clinit>`).
- **`static final` primitive/String** = **compile-time constant**: trình biên dịch **nhúng thẳng giá trị** vào nơi dùng (constant folding). `MathUtil.PI * 4` được tính một phần ngay lúc compile → không có lần đọc field nào lúc runtime.
  - ⚠️ Hệ quả: nếu thư viện khác đã biên dịch với hằng cũ, đổi giá trị hằng mà không build lại bên gọi → vẫn dùng giá trị cũ (nhúng cứng).
- **`final` field** giúp JVM: (1) đảm bảo **an toàn khởi tạo** trong đa thread (final fields có ngữ nghĩa happens-before sau constructor — JMM), (2) JIT có thể coi là bất biến để tối ưu.
- **`final` với reference**: chỉ khoá **biến** (không trỏ object khác), **không** khoá **trạng thái** object.
- **`final` class/method**: JIT biết không bị override → dễ **devirtualize + inline**.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c Main    # thấy PI bị thay bằng hằng số (ldc) thay vì getstatic
```
Output mẫu:
```
Đã tạo 3 counter (static dùng chung)
MathUtil.square(5) = 25
PI = 3.14159
Diện tích = 12.56636
Point bất biến: (3, 4)
final reference, object vẫn đổi được: ab
Point là final class? true
```

## 🎨 Bản vẽ — nơi lưu trữ
```
   Metaspace (theo CLASS)        Heap (theo OBJECT)
   ┌─────────────────────┐       ┌──────────────────┐
   │ Counter.count = 3   │       │ object1, object2 │ ← instance field riêng
   │ MathUtil.PI = 3.14  │       └──────────────────┘
   └─────────────────────┘
   static = chung 1 bản           instance = mỗi object 1 bản
```

## ⚠️ Cạm bẫy & lưu ý senior
- **static mutable state** = kẻ thù của đa thread + test (trạng thái rò rỉ giữa các test). Nếu phải dùng → đồng bộ hoặc dùng `AtomicInteger`/`ConcurrentHashMap`.
- **`final` ≠ immutable**: `final List` vẫn `add` được. Bất biến thật cần: field final + kiểu phần tử bất biến + không lộ tham chiếu khả biến (defensive copy).
- Lạm dụng `static` util cho mọi thứ → khó mock, khó mở rộng; cân nhắc inject dependency.
- `static final` hằng nên đặt ở nơi ổn định; nhớ build lại consumer khi đổi giá trị hằng (inline cứng).

## 🔗 Bài tiếp theo
👉 [17 — Nested & Inner Classes](../17-nested-inner-classes)
