# Bài 27: Lambda Expressions — Hàm như giá trị

Lambda đưa Java vào kỷ nguyên functional — và bên dưới nó **không** phải "anonymous class viết gọn" như nhiều người tưởng.

## 📖 Mô tả
Lambda là **hàm vô danh** dùng làm cài đặt cho **functional interface** (interface đúng 1 method trừu tượng). Cho phép truyền **hành vi** như dữ liệu.

## 🔧 Kỹ thuật
- Cú pháp: `(tham số) -> biểu_thức` hoặc `(tham số) -> { khối; }`
- Chỉ gán được cho **functional interface** (1 abstract method).
- **Capture**: dùng biến local phải **effectively final**.
- Ghép hàm: `andThen`, `compose`.

## ⚙️ Dưới nắp capo (Under the hood)
- **Lambda KHÁC anonymous class ở mức bytecode**:
  - Anonymous class → sinh **file `.class` riêng** (`Main$1.class`) lúc compile, `new` mỗi lần.
  - Lambda → compiler sinh một **`invokedynamic`** + method tổng hợp; lần chạy đầu, **`LambdaMetafactory`** tạo cài đặt (thường là một lớp ẩn/`MethodHandle`). → **không** rải class file, lambda **không capture** có thể được **tái dùng (cache) như singleton** → nhẹ hơn.
```
   anonymous: new Runnable(){...}  → Main$1.class, new mỗi lần
   lambda:    () -> ...            → invokedynamic → LambdaMetafactory (1 lần) → tái dùng
```
- **Capture**: lambda **không-capture** (không dùng biến ngoài) → rẻ nhất, có thể là instance dùng chung. Lambda **capture** biến → mỗi lần tạo phải gói biến → có chi phí.
- **`this`**: trong lambda `this` = **enclosing instance** (object chứa nó). Trong anonymous class `this` = **chính object anonymous**. Khác biệt quan trọng khi truy cập `this`.
- Biến capture phải **effectively final** vì JVM gói **bản sao**; cho sửa → hai bản lệch nhau.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c -p Main | grep invokedynamic    # thấy invokedynamic cho lambda
ls *.class                                # thấy Main$1.class (anonymous) nhưng KHÔNG có class riêng cho lambda
```
Output mẫu:
```
Anonymous class
Lambda
Sort theo độ dài: [táo, kiwi, cam, chuối]
'cam' ngắn? true
Lambda this.name = instance-Main
square(5) = 25
supplier = Xin chào
consumer in dòng này
add(3,4) = 7
square.andThen(plus1)(5) = 26
square.compose(plus1)(5) = 36
```

## 🎨 Bản vẽ — andThen vs compose
```
   square.andThen(plus1)(5):  5 ─square─▶ 25 ─plus1─▶ 26   (square TRƯỚC)
   square.compose(plus1)(5):  5 ─plus1──▶ 6  ─square─▶ 36   (plus1 TRƯỚC)
```

## ⚠️ Cạm bẫy & lưu ý senior
- Lambda dùng biến phải **effectively final** — cố sửa → lỗi compile.
- Lambda **capture** trong vòng nóng (tạo hàng triệu) → vẫn có chi phí cấp phát; lambda không-capture được tái dùng.
- `this` trong lambda ≠ anonymous class → đừng copy-paste mù.
- Lambda quá dài/nhiều dòng → mất tính đọc; tách thành method rồi dùng **method reference** (bài 29).
- Exception checked trong lambda của functional interface chuẩn (Function...) không khai báo được → phải bọc hoặc dùng interface tự định nghĩa.

## 🔗 Bài tiếp theo
👉 [28 — Functional Interfaces](../28-functional-interfaces)
