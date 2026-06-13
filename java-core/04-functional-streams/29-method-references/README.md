# Bài 29: Method References — 4 dạng tham chiếu method

`::` là "lambda chỉ gọi một method" viết gọn lại — rõ ý định hơn, ít nhiễu hơn.

## 📖 Mô tả
Khi một lambda chỉ **gọi đúng một method** sẵn có, dùng **method reference** (`Class::method`) thay cho lambda. Cùng cơ chế bên dưới, chỉ khác cú pháp.

## 🔧 Kỹ thuật — 4 dạng
| # | Dạng | Cú pháp | Lambda tương đương |
|---|------|---------|--------------------|
| 1 | Static | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| 2 | Instance của object **cụ thể** | `System.out::println` | `s -> System.out.println(s)` |
| 3 | Instance của **đối số đầu** | `String::toUpperCase` | `s -> s.toUpperCase()` |
| 4 | Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

## ⚙️ Dưới nắp capo (Under the hood)
- Method reference biên dịch **giống lambda**: `invokedynamic` + `LambdaMetafactory` tạo cài đặt functional interface lúc runtime. Không có "ma thuật" runtime thêm — chỉ là đường cú pháp.
- **Khác biệt tinh tế dạng 2 vs dạng 3**:
  - Dạng 2 `obj::method`: **bắt** object `obj` (capture) — method gọi trên object cố định, tham số functional interface thành **đối số** của method.
  - Dạng 3 `Class::method`: **không** bắt object; object nhận method chính là **đối số đầu tiên** truyền vào lúc gọi. `String::toUpperCase` → `(String s) -> s.toUpperCase()`.
- **Constructor reference** `Type::new`: chọn constructor theo functional interface đích (`Supplier` → ctor rỗng, `Function<Integer,int[]>` → `int[]::new` tạo mảng kích thước n).
- Lợi ích hiệu năng nhỏ: dạng không-capture (1,3,4 thường) có thể tái dùng instance như lambda không-capture.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Dạng 1 (static): 123
Dạng 2 (instance object cụ thể): in qua System.out::println
Dạng 2: Hello World
Dạng 3 (instance của đối số): [CHUỐI, TÁO, KIWI]
Dạng 3 sort: [CHUỐI, KIWI, TÁO]
Dạng 4 (constructor): [tạo qua constructor reference]
Dạng 4 mảng: độ dài = 5
Stream + method ref: A,B,C
toCollection(TreeSet::new): [1, 2, 3]
```

## 🎨 Bản vẽ — dạng 2 vs dạng 3
```
   Dạng 2: prefix::concat       object ĐÃ BIẾT (prefix), x là đối số
           x ─▶ prefix.concat(x)

   Dạng 3: String::toUpperCase  object LÀ đối số đầu
           s ─▶ s.toUpperCase()
```

## ⚠️ Cạm bẫy & lưu ý senior
- Nhầm dạng 2/3 khi method vừa khớp cả hai → compiler có thể báo mơ hồ; viết lại thành lambda nếu cần rõ.
- Method reference tới method **overload** → trình biên dịch chọn theo functional interface đích; đôi khi mơ hồ.
- Đừng ép dùng `::` khi lambda có thêm logic (vd null-check) — lúc đó lambda rõ hơn.
- `this::method` / `super::method` cũng hợp lệ (dạng 2 với object hiện tại).
- Constructor reference rất hợp với `Collectors.toCollection`, `Stream.toArray(Type[]::new)`.

## 🔗 Bài tiếp theo
👉 [30 — Stream API](../30-stream-api)
