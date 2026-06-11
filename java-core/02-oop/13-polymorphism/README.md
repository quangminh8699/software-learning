# Bài 13: Polymorphism — Đa hình & dynamic dispatch

"Một giao diện, nhiều cài đặt" — và JVM chọn cài đặt nào **lúc runtime** qua vtable.

## 📖 Mô tả
Đa hình (runtime) cho phép một biến kiểu cha giữ object kiểu con, và lời gọi method sẽ chạy **bản của kiểu thực**. Đây là nền của thiết kế mở rộng được (Open/Closed Principle).

## 🔧 Kỹ thuật
- **Upcasting**: `Shape s = new Circle()` — ngầm, luôn an toàn.
- **Downcasting**: `(Circle) s` — tường minh, có thể `ClassCastException`; dùng `instanceof` (pattern) để an toàn.
- **`@Override`**: bản cài đặt của lớp con thay cho cha.
- **Late binding / dynamic dispatch**: chọn method lúc runtime.

## ⚙️ Dưới nắp capo (Under the hood)
```
   s.area()  →  bytecode: invokevirtual Shape.area()

   Mỗi class có VTABLE (bảng method ảo) trong metadata:
   Circle vtable:    area() → Circle.area
   Rectangle vtable: area() → Rectangle.area

   Object trên heap có con trỏ tới class của nó:
   [Circle obj] ─▶ Circle class ─▶ vtable[area] ─▶ Circle.area  ← chọn lúc RUNTIME
```
- `invokevirtual` không biết trước gọi bản nào; JVM lấy **kiểu thực** của object (qua con trỏ class trong header) → tra **vtable** → nhảy tới cài đặt đúng. Đây là **dynamic dispatch**.
- Khác `invokestatic`/overloading (chốt lúc compile — bài 13b) và `invokeinterface` (cho method interface).
- **Chi phí**: một lần tra bảng gián tiếp. Nhưng JIT có thể **devirtualize** (nếu thực tế chỉ có 1 loại tại điểm gọi → monomorphic) và **inline** → gần như miễn phí. Quá nhiều loại tại một call site (megamorphic) → JIT khó tối ưu.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c Main | grep invokevirtual   # thấy invokevirtual cho area()
```
Output mẫu:
```
Circle     area = 12.57
Rectangle  area = 12.00
Circle     area = 3.14
Tổng diện tích = 27.71
Thêm Triangle area = 6.00 (code cũ không đổi)
Downcast OK, bán kính = 5.0
```

## 🎨 Bản vẽ — Open/Closed
```
   for (Shape s : shapes) s.area();   ← code này KHÔNG đổi
        ▲          ▲          ▲
     Circle    Rectangle   Triangle (thêm mới chỉ cần class mới)
   "Mở để mở rộng, đóng với sửa đổi"
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Downcast bừa** → `ClassCastException`. Nếu code đầy `instanceof` + cast → thường là thiếu một method đa hình; cân nhắc thêm method vào abstraction hoặc Visitor pattern.
- Gọi method overridable **trong constructor cha** → chạy bản con khi con chưa init (xem bài 10).
- Field **không** đa hình: truy cập field quyết bởi **kiểu khai báo** (static), chỉ method mới dynamic dispatch.
- Hiệu năng call site: monomorphic (1 loại) → JIT inline; megamorphic (nhiều loại) → chậm hơn. Thiết kế hot path cần lưu ý.

## 🔗 Bài tiếp theo
👉 [13b — Overloading vs Overriding](../13b-overloading-overriding)
