# Bài 21: Wrapper & Autoboxing — Integer vs int & cạm bẫy ẩn

Autoboxing tiện nhưng giấu chi phí (object trên heap) và hai cái bẫy chết người: **`==` cache** và **NPE khi unbox null**.

## 📖 Mô tả
Mỗi primitive có một **wrapper** object: `int→Integer`, `long→Long`, `double→Double`... **Autoboxing** = chuyển primitive→wrapper tự động; **unboxing** = ngược lại. Cần wrapper vì Collections/Generics chỉ chứa **object**.

## 🔧 Kỹ thuật
| Primitive | Wrapper | Primitive | Wrapper |
|-----------|---------|-----------|---------|
| `int` | `Integer` | `boolean` | `Boolean` |
| `long` | `Long` | `char` | `Character` |
| `double` | `Double` | `byte` | `Byte` |
| `float` | `Float` | `short` | `Short` |

- autobox: `Integer i = 5;` → `Integer.valueOf(5)`
- unbox: `int x = i;` → `i.intValue()`

## ⚙️ Dưới nắp capo (Under the hood)
- **Integer cache**: `Integer.valueOf(int)` trả về **object dùng chung** cho khoảng **[-128, 127]** (mặc định). Trong khoảng → cùng object → `==` cho `true`. Ngoài khoảng → object **mới** mỗi lần → `==` cho `false`.
```
   Integer cache:  [-128 .. 127]  ──▶ tái dùng object
   a1=127, a2=127 → cùng object → ==  true
   b1=128, b2=128 → object mới   → ==  false   ← BẪY
```
- **NPE khi unbox**: `int x = nullInteger;` → gọi `.intValue()` trên `null` → `NullPointerException`. Rất hay gặp khi map/DB trả `null`.
- **Chi phí heap**: mỗi `Integer` là object (~16 byte header + 4 byte giá trị + padding) trên heap, so với `int` 4 byte trên stack. Autobox trong vòng lặp → hàng triệu object rác → áp lực GC + chậm.
- **Generics bị type erasure** (bài 22) nên không thể `List<int>` → buộc dùng `List<Integer>` → autobox khắp nơi. Stream có biến thể primitive (`IntStream`) để né điều này (bài 30).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
java -Djava.lang.Integer.IntegerCache.high=1000 Main   # mở rộng cache để thấy bẫy đổi
```
Output mẫu:
```
boxed=10, unboxed=10
127 == 127 ? true
128 == 128 ? false
equals luôn đúng: true
Unbox null -> NullPointerException (bẫy ẩn!)
sumBoxed = 499999500000  (đã tạo cả triệu object thừa)
sumPrim  = 499999500000  (không tạo object)
List<Integer> phần tử đầu = 5
```

## 🎨 Bản vẽ — int vs Integer trong bộ nhớ
```
   int x = 5         Integer y = 5
   STACK             STACK         HEAP
   [ 5 ]             y ───────────▶ [ header | value=5 ]
   4 byte, no GC     8 byte ref + ~16 byte object, có GC
```

## ⚠️ Cạm bẫy & lưu ý senior
- **LUÔN so wrapper bằng `.equals()`**, không `==` (trừ khi cố ý so identity). Bug ẩn vì 127 "may mắn" đúng.
- **Unbox null → NPE**: cẩn thận với giá trị từ DB/Map/`Optional`. Kiểm tra null trước hoặc dùng primitive default.
- **Đừng dùng wrapper làm accumulator** trong vòng lặp nóng → dùng primitive.
- Trộn `Long`/`long` trong so sánh/phép toán dễ unbox ngầm → NPE hoặc chi phí; rõ ràng kiểu trong hot path.
- `Boolean`/`Byte`/`Short`/`Character` cũng có cache tương tự → cùng bẫy `==`.

## 🔗 Bài tiếp theo
👉 [22 — Generics](../22-generics)
