# Bài 14: Abstraction — Abstract class & Template Method

Abstract class = "khung có sẵn một nửa": gom code chung, ép lớp con điền phần còn lại.

## 📖 Mô tả
`abstract class` không thể tạo object trực tiếp. Nó định nghĩa **method trừu tượng** (chưa có thân, bắt buộc lớp con cài) lẫn **method cụ thể** + **state** dùng chung. Đây là nền của mẫu **Template Method**.

## 🔧 Kỹ thuật
- `abstract class X` → không `new X()` được.
- `abstract void m();` → không thân; lớp con **phải** override (trừ khi lớp con cũng abstract).
- Có thể có constructor (chạy qua `super()`), field, method thường.
- **Template Method**: method `final` định nghĩa khung thuật toán, gọi các bước abstract.

## ⚙️ Dưới nắp capo (Under the hood)
- `abstract` chỉ là **ràng buộc compile-time**: trình biên dịch chặn `new` trên class abstract và bắt lớp con cài method abstract. Trong `.class`, class/method có **access flag** `ACC_ABSTRACT`.
- Runtime: object thật là của **lớp con**, nhưng chứa luôn phần **state** khai báo ở abstract class (như kế thừa thường — bài 12).
- Method abstract gọi qua `invokevirtual` → dynamic dispatch tới bản của lớp con (đa hình — bài 13).
- `prepare()` để `final` → JVM biết không thể override → có thể inline; đồng thời **bảo vệ thứ tự thuật toán** (lớp con không phá khung).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
=== Pha trà ===
Đun 200ml nước
Ngâm túi trà
Rót ra cốc
Thêm chanh

=== Pha cà phê ===
Đun 200ml nước
Lọc cà phê qua phin
Rót ra cốc
Thêm sữa & đường
```

## 🎨 Bản vẽ — Template Method
```
   Beverage.prepare()  (final, KHUNG cố định)
   ┌──────────────────────────────┐
   │ boilWater()      (chung)      │
   │ brew()           ◄── abstract │──▶ Tea: ngâm trà / Coffee: lọc phin
   │ pourInCup()      (chung)      │
   │ addCondiments()  ◄── abstract │──▶ Tea: chanh    / Coffee: sữa+đường
   └──────────────────────────────┘
   "Hollywood Principle": đừng gọi chúng tôi, chúng tôi sẽ gọi bạn.
```

## ⚠️ Cạm bẫy & lưu ý senior
- **abstract class vs interface**: chọn abstract class khi cần **state chung** + **code chung** + quan hệ is-a chặt; chọn interface khi cần **đa kế thừa hành vi** (bài 15).
- Đừng để khung gọi method **protected/public overridable** rồi lại cho lớp con đổi thứ tự → để method khung `final`.
- Java chỉ **đơn kế thừa class** → kế thừa abstract class "tiêu" mất suất kế thừa duy nhất; cân nhắc kỹ.
- Từ Java 8, interface có `default` method → ranh giới abstract class/interface mờ đi; nhưng interface vẫn **không giữ state khả biến**.

## 🔗 Bài tiếp theo
👉 [15 — Interfaces](../15-interfaces)
