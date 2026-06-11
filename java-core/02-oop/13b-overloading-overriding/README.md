# Bài 13b: Overloading vs Overriding — Static vs Dynamic binding

Hai khái niệm dễ nhầm nhất trong OOP Java. Hiểu rõ = trả lời được câu hỏi phỏng vấn + tránh bug tinh vi.

## 📖 Mô tả
- **Overloading**: cùng **tên** method, **khác** danh sách tham số. Chọn lúc **compile** theo **kiểu khai báo**.
- **Overriding**: lớp con cung cấp cài đặt mới cho method cha **cùng chữ ký**. Chọn lúc **runtime** theo **kiểu thực**.

## 🔧 Kỹ thuật — bảng so sánh
| Tiêu chí | Overloading | Overriding |
|----------|-------------|------------|
| Quan hệ | cùng class (hoặc kế thừa) | giữa cha–con |
| Chữ ký | **khác** tham số | **giống** hệt |
| Kiểu trả về | có thể khác | giống hoặc covariant |
| Binding | **static** (compile-time) | **dynamic** (runtime) |
| Dựa vào | **kiểu khai báo** của đối số | **kiểu thực** của object |
| Bytecode | method cố định | tra vtable |
| `@Override` áp dụng? | không | có |

## ⚙️ Dưới nắp capo (Under the hood)
```
   OVERLOADING (static binding)            OVERRIDING (dynamic binding)
   p.print(o)  với o khai báo Object        a.sound()  với a thực ra là Cat
        │ compiler nhìn KIỂU KHAI BÁO            │ JVM nhìn KIỂU THỰC lúc runtime
        ▼                                        ▼
   chốt cứng → print(Object) trong .class    tra vtable[Cat] → Cat.sound
   (không đổi dù runtime o là String)        (đổi theo object thật)
```
- **Overload** được trình biên dịch giải quyết và **nhúng method đích cố định** vào bytecode (`invokevirtual`/`invokestatic` trỏ tới một method cụ thể đã chốt). Runtime **không** chọn lại.
- **Override** dùng `invokevirtual` + **vtable**: JVM lấy kiểu thực của object → chọn bản đúng lúc chạy.
- **Hệ quả bẫy**: `print(o)` với `o` khai báo `Object` (dù thực là `String`) luôn gọi `print(Object)` — vì overload chốt theo kiểu **khai báo**, không phải kiểu thực.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c Main    # overload: invokevirtual Printer.print(Object) đã cố định
```
Output mẫu:
```
Meo
→ override chọn theo kiểu THỰC (Cat) lúc runtime

print(int): 5
print(double): 5.0
print(Object): text
→ overload chọn theo kiểu KHAI BÁO (Object) lúc compile

x.equals(y) = false  (gọi Object.equals vì DogEquals chỉ OVERLOAD, không OVERRIDE)
```

## 🎨 Bản vẽ
```
   "Cùng tên, KHÁC tham số"  =  OVERLOAD  =  compile-time  =  kiểu khai báo
   "Cha→Con, GIỐNG chữ ký"   =  OVERRIDE  =  runtime       =  kiểu thực
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Bẫy `equals`**: viết `equals(Dog)` thay vì `equals(Object)` → chỉ **overload**, không override → `HashMap`/`List.contains` dùng `Object.equals` (so địa chỉ) → bug logic im lặng. **Luôn** `@Override public boolean equals(Object o)`.
- **`@Override` là lưới an toàn**: nếu bạn nghĩ đang override mà sai chữ ký, compiler báo lỗi ngay. Luôn dùng.
- Overload với autoboxing/widening/varargs có **thứ tự ưu tiên** phức tạp: widening > boxing > varargs → dễ chọn nhầm overload ngoài ý muốn.
- `private`, `static`, `final` method **không** được override (static method có thể bị "hide" — khác override).

## 🔗 Bài tiếp theo
👉 [14 — Abstraction](../14-abstraction)
