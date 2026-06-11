# Bài 18: Enums — Hằng kiểu an toàn & enum có hành vi

`enum` không chỉ là "danh sách hằng" — nó là class đầy đủ, mỗi hằng là một singleton, có thể mang dữ liệu và hành vi.

## 📖 Mô tả
`enum` định nghĩa **tập hữu hạn** các hằng có kiểu. Mỗi hằng là một **instance singleton** do JVM tạo. Thay thế "magic number/String" bằng kiểu được compiler kiểm tra.

## 🔧 Kỹ thuật
- `values()`: mảng mọi hằng (theo thứ tự khai báo).
- `valueOf("X")`: tra hằng theo tên.
- `name()` / `ordinal()`: tên / chỉ số.
- enum có thể có **constructor, field, method**, và **override method theo từng hằng**.
- `EnumMap` / `EnumSet`: collection chuyên cho key enum, cực nhanh.

## ⚙️ Dưới nắp capo (Under the hood)
- `enum Day {...}` được biên dịch thành `final class Day extends java.lang.Enum<Day>`, mỗi hằng là `public static final Day MONDAY = new Day(...)` khởi tạo trong `<clinit>` (class init) → **tạo đúng một lần**.
- Vì mỗi hằng là **singleton**, so sánh bằng `==` vừa đúng vừa nhanh (không cần `equals`), và **null-safe** hơn `equals`.
- enum override method theo hằng (`PLUS { ... }`) → mỗi hằng thực ra là **lớp con vô danh** của enum → `Operation$1`, `Operation$2` (dynamic dispatch khi gọi `apply`).
- **`EnumMap`** lưu giá trị trong một **mảng** indexed theo `ordinal()` → tra cứu O(1) **không hash, không va chạm** → nhanh hơn `HashMap` cho key enum. `EnumSet` dùng **bit vector** (1 `long` cho ≤64 hằng) → cực gọn.
- enum là cách cài **Singleton** an toàn nhất (chống reflection + serialize phá vỡ — xem bài 45).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
SATURDAY là cuối tuần? true
EARTH    khối lượng=5.98e+24 kg, trọng lực bề mặt=9.80
MARS     khối lượng=6.42e+23 kg, trọng lực bề mặt=3.71
3 + 4 = 7
3 * 4 = 12
today == SATURDAY ? true
name=SATURDAY, ordinal=5
Kế hoạch: {MONDAY=Họp, SATURDAY=Nghỉ}
```

## 🎨 Bản vẽ — enum là singleton
```
   Metaspace / class Day:
   MONDAY ─▶ [Day@1]   (final, tạo 1 lần)
   SATURDAY ▶ [Day@6]
   today ────┘  (cùng object @6) → today == SATURDAY là TRUE
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Đừng phụ thuộc `ordinal()`** làm dữ liệu bền (DB, protocol): chèn/đổi thứ tự hằng làm lệch ordinal → hỏng dữ liệu. Lưu `name()` hoặc một mã riêng.
- enum **không** kế thừa class khác (đã extends `Enum`), nhưng **implements interface** được → phối hợp đa hình.
- Dùng `EnumMap`/`EnumSet` thay `HashMap`/`HashSet` khi key là enum → nhanh + ít rác.
- enum + `switch` đầy đủ: bật cảnh báo để compiler nhắc khi thêm hằng mới mà quên xử lý.

## 🔗 Bài tiếp theo
👉 [19 — Records & Sealed Classes](../19-records-sealed)
