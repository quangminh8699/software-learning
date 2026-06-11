# Bài 15: Interfaces — Hợp đồng & đa kế thừa hành vi

Interface là "hợp đồng" — công cụ quan trọng nhất để thiết kế hệ thống lỏng lẻo, dễ test, dễ mở rộng.

## 📖 Mô tả
`interface` định nghĩa **cái gì** phải làm, không nói **làm thế nào**. Một class `implements` nhiều interface → đa kế thừa **kiểu/hành vi**. Java 8+ thêm `default`/`static` method.

## 🔧 Kỹ thuật
- Field interface: ngầm `public static final` (hằng).
- Method: ngầm `public abstract` (trừ `default`/`static`/`private`).
- `default`: method có thân — thêm hành vi mà không phá class đã implements.
- `static`: tiện ích gọi qua `Interface.method()`.
- `private` (Java 9+): helper dùng chung giữa các default method.
- Một class implements **nhiều** interface (khác `extends` chỉ 1 class).

## ⚙️ Dưới nắp capo (Under the hood)
- Gọi method qua tham chiếu interface → bytecode `invokeinterface`. Khác `invokevirtual` ở chỗ class không có vị trí cố định trong vtable cho mọi interface → JVM dùng **itable**/tìm kiếm; JIT vẫn tối ưu mạnh (inline cache).
- **`default` method ra đời (Java 8)** để **tiến hoá interface** mà không phá hàng nghìn class đã implements (vd thêm `Collection.stream()`). Trước đó, thêm method vào interface = vỡ mọi class cũ.
- **Đa kế thừa "an toàn"**: interface không mang **state khả biến** → tránh bài toán kim cương về dữ liệu. Nếu hai interface có `default` cùng tên → compiler **bắt** bạn override để giải quyết xung đột.
- Interface là nền của **Dependency Inversion**: code phụ thuộc abstraction, nhờ đó mock được khi test, thay cài đặt lúc runtime (DI).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Vịt bay là đà
Vịt bơi
Vịt bay là đà
default method: Một vật biết bay, trần bay 10000m
Flyable.info(): Interface Flyable v1
Sắp theo lương: [Bình(900), Cường(1200), An(1500)]
```

## 🎨 Bản vẽ — abstract class vs interface
```
                 abstract class         interface
   State (field) │ có (khả biến)        │ chỉ hằng (static final)
   Kế thừa       │ đơn (extends 1)      │ đa (implements N)
   Constructor   │ có                   │ không
   Method có thân│ có                   │ default/static/private
   Quan hệ       │ "is-a" chặt          │ "can-do" / capability
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Constant interface anti-pattern**: nhét hằng vào interface rồi `implements` để "dùng cho gọn" → lạm dụng. Dùng `enum`/`final class` chứa hằng.
- Xung đột `default` từ 2 interface → phải override tường minh, gọi `Interface.super.method()`.
- Đừng biến interface thành "abstract class nghèo": nếu cần state + vòng đời phức tạp → abstract class.
- Thiết kế hệ thống lớn: interface nhỏ, tách biệt (Interface Segregation) tốt hơn interface "thần thánh" nhiều method.

## 🔗 Bài tiếp theo
👉 [16 — static & final](../16-static-final)
