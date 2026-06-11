# Bài 12: Inheritance — Kế thừa & quan hệ "is-a"

Kế thừa mạnh nhưng dễ bị lạm dụng — senior phải biết khi nào dùng và khi nào chọn composition.

## 📖 Mô tả
`class Con extends Cha` cho phép lớp con **kế thừa** field + method của cha, mô hình hoá quan hệ **is-a**. Mọi class trong Java ngầm kế thừa `java.lang.Object`.

## 🔧 Kỹ thuật
- `extends`: kế thừa (Java chỉ **đơn kế thừa** class).
- `super`: gọi constructor (`super(...)`) hoặc method (`super.foo()`) của cha.
- `@Override`: ghi đè method cha (annotation giúp compiler bắt lỗi sai chữ ký).
- `protected`: cho lớp con (kể cả khác package) truy cập.

## ⚙️ Dưới nắp capo (Under the hood)
```
   Object dog trên HEAP (các phần xếp chồng trong CÙNG 1 object):
   ┌──────────────────────────────┐
   │ object header                │
   │ [phần Animal]  name = "Rex"   │  ← field kế thừa nằm cùng object
   │ [phần Dog]     breed = "Husky"│
   └──────────────────────────────┘
        ▲ một tham chiếu Dog/Animal đều trỏ tới đây
```
- Object lớp con chứa **toàn bộ** dữ liệu lớp cha trong cùng vùng nhớ → vì vậy `super(...)` phải init phần cha trước.
- Chuỗi kế thừa kết thúc ở `Object` → `equals`, `hashCode`, `toString`, `getClass` luôn có sẵn.
- JVM lưu thông tin lớp cha trong class metadata; lời gọi method ảo dùng **vtable** để phân giải (xem bài 13/13b).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Rex đang ăn.
Rex sủa: Gâu gâu!
Tôi là động vật tên Rex
...và là chó giống Husky
dog instanceof Animal? true
Mọi class đều kế thừa Object: class java.lang.Object
Car khởi động -> Engine: vroom
```

## 🎨 Bản vẽ — is-a vs has-a
```
   KẾ THỪA (is-a)              COMPOSITION (has-a)
   Animal                      Car
     ▲                          │ field
     │ extends                  ▼
   Dog                        Engine
   "Dog LÀ Animal"            "Car CÓ Engine"
```

## ⚠️ Cạm bẫy & lưu ý senior
- **"Ưu tiên composition hơn inheritance"** (Effective Java): kế thừa phá đóng gói — lớp con phụ thuộc chi tiết cài đặt của cha, dễ vỡ khi cha thay đổi (fragile base class).
- **Đơn kế thừa**: Java không cho `extends` nhiều class (tránh kim cương kế thừa); dùng interface cho đa năng (bài 15).
- Kế thừa chỉ để **tái dùng code** mà không có quan hệ is-a thật → thiết kế sai (vd `Stack extends Vector` là lỗi thiết kế nổi tiếng của JDK).
- Lớp không định cho kế thừa → đánh dấu `final` để chặn (an toàn + cho JIT tối ưu).

## 🔗 Bài tiếp theo
👉 [13 — Polymorphism](../13-polymorphism)
