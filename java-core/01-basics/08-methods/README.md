# Bài 08: Methods — Stack frame, overloading, varargs, đệ quy

Mỗi lời gọi method là một **frame** trên stack — hiểu điều này giải thích pass-by-value, StackOverflow và overloading.

## 📖 Mô tả
Method là đơn vị tái dùng code. Bài này nhìn method từ góc **runtime**: stack frame được tạo/huỷ thế nào, Java truyền tham số ra sao (luôn pass-by-value), và overloading được quyết lúc nào.

## 🔧 Kỹ thuật
- **Khai báo**: `[modifier] returnType name(params) { ... }`
- **Overloading**: cùng tên, **khác danh sách tham số** → chọn lúc **compile**.
- **Varargs** `T...`: nhận số tham số tuỳ ý, JVM gói thành mảng `T[]`.
- **Đệ quy**: method tự gọi; cần **base case** để dừng.

## ⚙️ Dưới nắp capo (Under the hood)
**Mỗi lời gọi method → 1 stack frame** trên JVM Stack của thread:
```
   JVM Stack của thread (lớn dần xuống dưới)
   ┌──────────────────────────┐
   │ frame main()             │  biến local: x, arr...
   ├──────────────────────────┤
   │ frame factorial(5)       │  ┐
   ├──────────────────────────┤  │ mỗi lời gọi đệ quy
   │ frame factorial(4)       │  │ đẩy thêm 1 frame
   ├──────────────────────────┤  │
   │ frame factorial(3) ...   │  ┘  quá sâu → StackOverflowError
   └──────────────────────────┘
   Mỗi frame chứa: biến local + operand stack + return address.
```
- **Pass-by-value — Java KHÔNG có pass-by-reference**:
  - primitive: truyền **bản sao giá trị** → sửa trong method không ảnh hưởng ngoài.
  - object: truyền **bản sao của tham chiếu** (địa chỉ). Vẫn trỏ cùng object → sửa **trạng thái** object thì bên ngoài thấy; nhưng gán lại tham chiếu thì không.
- **Overloading = static binding**: compiler nhìn **kiểu tĩnh** của đối số để chọn overload, nhúng thẳng vào bytecode (`invokestatic`/`invokevirtual` tới method cụ thể). Khác **overriding** (chọn lúc runtime — xem bài 13b).
- **Varargs** là đường cú pháp: `sum(1,2,3)` biên dịch thành `sum(new int[]{1,2,3})`.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c Main    # thấy invokestatic describe(I), describe(D)... — overload đã được chốt
```
Output mẫu:
```
primitive sau gọi: 10 (KHÔNG đổi - sao chép giá trị)
array sau gọi: 999 (ĐỔI - chung object qua tham chiếu sao chép)
int: 42
double: 3.14
sum(1,2,3) = 6
factorial(5) = 120
StackOverflowError: stack đã đầy frame (đệ quy không đáy)
```

## 🎨 Bản vẽ — pass-by-value với object
```
   main:  arr ──────────────┐
                            ▼
                      [ {999,2,3} ]  ← object trên heap (chung)
                            ▲
   modifyArray: a ──────────┘   (a là BẢN SAO địa chỉ, cùng trỏ tới object)
   → a[0]=999 sửa object chung → main thấy.
   → nhưng `a = new int[]{...}` chỉ đổi bản sao địa chỉ → main KHÔNG thấy.
```

## ⚠️ Cạm bẫy & lưu ý senior
- Nhầm "Java pass-by-reference" → bug khi tưởng gán lại tham số sẽ đổi biến ngoài. **Không**.
- Overload **mơ hồ**: `describe(null)` có thể không biết chọn `String` hay `Object` → lỗi biên dịch "ambiguous".
- Varargs + autoboxing dễ tạo mảng `Integer[]` tốn kém trong vòng nóng.
- Java **không** tối ưu **tail-call** → đệ quy sâu vẫn tràn stack. Với đệ quy lớn, chuyển sang **vòng lặp** hoặc dùng stack tường minh. Chỉnh độ sâu bằng `-Xss`.

## 🔗 Bài tiếp theo
👉 [09 — Classes & Objects](../../02-oop/09-classes-objects) (bắt đầu Module 02 — OOP)
