# Bài 31: Pattern Matching — instanceof, switch & record deconstruction

Java 21 đưa pattern matching đầy đủ: kiểm tra kiểu + ép kiểu + rã dữ liệu trong một cú pháp, an toàn lúc compile.

## 📖 Mô tả
Pattern matching gộp ba việc hay đi cùng nhau: **kiểm tra kiểu**, **ép kiểu**, **rút trích/bind** dữ liệu. Gồm: `instanceof` pattern (16+), `switch` pattern + record deconstruction (21).

## 🔧 Kỹ thuật
- **Type pattern**: `obj instanceof String s` → bind `s`.
- **switch pattern**: `case Integer i ->` rẽ theo kiểu.
- **Guard**: `case Integer i when i > 10 ->` thêm điều kiện.
- **`case null`**: xử lý null tường minh (switch cổ điển ném NPE).
- **Record deconstruction**: `case Circle(Point(var x, var y), var r) ->` rã lồng nhau.
- **Exhaustiveness**: với `sealed`, compiler buộc phủ hết → không cần `default`.

## ⚙️ Dưới nắp capo (Under the hood)
- `instanceof String s` biên dịch thành kiểm tra kiểu + **bind có điều kiện**: biến `s` chỉ "trong phạm vi" ở nhánh kiểu đúng. Trong `&&`, `s` dùng được ở vế phải vì compiler biết tới đó đã chắc kiểu (flow scoping).
- **switch pattern** dùng `invokedynamic` + bootstrap `SwitchBootstraps.typeSwitch` để chọn nhánh theo kiểu runtime hiệu quả (không phải chuỗi `if-instanceof` tuyến tính).
- **Record deconstruction** gọi các **accessor** của record (`x()`, `y()`, `r()`) để rã component — chỉ hợp lệ trên record (có cấu trúc component rõ ràng).
- **Exhaustiveness checking** với `sealed`: compiler biết tập con đóng (`permits`) → nếu switch thiếu nhánh → **lỗi compile**. Đây là an toàn kiểu mạnh hơn `default` (vốn nuốt mọi trường hợp quên xử lý).

## ▶️ Cách dùng
```bash
javac Main.java && java Main      # cần JDK 21
```
Output mẫu:
```
instanceof pattern: độ dài = 13
Integer lớn: 42
Double: 3.14
String dài 2
Kiểu khác: ListN
null
Deconstruction: Circle tâm(1,2), bán kính 5
eval = 7
eval = 7
```

## 🎨 Bản vẽ — record deconstruction
```
   Circle(Point(var x, var y), var r)
      │      │        │          │
      │      └ x=1    └ y=2      └ r=5
      └ khớp kiểu Circle → rã center → rã Point → lấy x,y; lấy r
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Thứ tự `case`** quan trọng: nhánh cụ thể/có guard phải **trước** nhánh tổng quát, nếu không "dominated" → lỗi compile.
- **`case null`** nếu không khai báo: switch pattern hiện đại ném NPE với null (như cũ) trừ khi có `case null`.
- Pattern matching + `sealed` + `record` = mô hình hoá dữ liệu kiểu functional (sum/product type) — rất hợp cho AST/interpreter/state machine, nhưng **đừng** thay mọi đa hình bằng switch (đôi khi method ảo sạch hơn — xem bài 13).
- Guard `when` chỉ là điều kiện boolean — giữ nó thuần, không side-effect.

## 🔗 Bài tiếp theo
👉 [32 — Threads Basics](../../05-concurrency/32-threads-basics) (bắt đầu Module 05 — Concurrency)
