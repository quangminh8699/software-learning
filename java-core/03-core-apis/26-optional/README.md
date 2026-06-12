# Bài 26: Optional — Mô hình hoá "vắng giá trị" thay cho null

`Optional` không phải để "diệt null khắp nơi" — nó là công cụ thiết kế **API** nói rõ "kết quả có thể không tồn tại".

## 📖 Mô tả
`Optional<T>` là hộp chứa 0 hoặc 1 giá trị. Khi method trả `Optional`, người gọi **bị buộc** nghĩ đến trường hợp rỗng → giảm `NullPointerException`, API tự tài liệu hoá.

## 🔧 Kỹ thuật
| Tạo | `Optional.of(x)`, `ofNullable(x)`, `empty()` |
|-----|----------------------------------------------|
| Tiêu thụ | `orElse`, `orElseGet`, `orElseThrow`, `ifPresent(OrElse)` |
| Biến đổi | `map`, `flatMap`, `filter` |
| Primitive | `OptionalInt/Long/Double` (tránh autobox) |

## ⚙️ Dưới nắp capo (Under the hood)
- `Optional` là **object** trên heap bọc một tham chiếu (`value` hoặc `null`). → có **chi phí cấp phát** nhỏ. Vì vậy **không** dùng cho field/đối số/hot path tạo hàng triệu Optional.
- `map/filter/flatMap` chỉ chạy khi **có giá trị**; nếu rỗng → trả `empty()` luôn → đó là cách **bỏ if-null lồng nhau** thành chuỗi tuyến tính.
- **`orElse` vs `orElseGet`** (bẫy hiệu năng kinh điển):
  - `orElse(expensive())` → tham số **luôn được tính** (eager), kể cả khi Optional có giá trị → lãng phí.
  - `orElseGet(() -> expensive())` → supplier chỉ chạy **khi rỗng** (lazy).
- `Optional` **không Serializable** → không dùng làm field entity/DTO truyền qua mạng.
- Đây là biểu hiện của kiểu **monad** (như `Stream` 1 phần tử): `flatMap` để ghép các Optional lồng nhau.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
u1 -> Alice
u9 -> KHÔNG CÓ
u2 -> map+filter -> BOB
  (expensiveDefault() được gọi!)
v1=Alice, v2=Alice
orElseThrow: user u9 không tồn tại
Chào Alice
OptionalInt: 7
```
*(Lưu ý dòng "expensiveDefault() được gọi!" xuất hiện do `orElse` eager — minh hoạ bẫy.)*

## 🎨 Bản vẽ — chuỗi xử lý
```
   findUser(id) ─▶ Optional[Bob] ─map(upper)▶ Optional[BOB] ─filter(B*)▶ Optional[BOB] ─orElse▶ "BOB"
   findUser(id) ─▶ Optional.empty ─map──▶ empty ─filter──▶ empty ─orElse▶ "(mặc định)"
                     (mọi bước bị bỏ qua khi rỗng — không NPE)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **`Optional.get()` không kiểm tra** = "null check" trá hình → `NoSuchElementException`. Dùng `orElse*`/`map`/`ifPresent`.
- **Đừng dùng Optional cho field/tham số**: tốn bộ nhớ, không Serializable, ý nghĩa mơ hồ. Dùng cho **kiểu trả về**.
- **`orElse` với giá trị đắt** → dùng `orElseGet` (lazy).
- **Không** `Optional<Optional<T>>` hay `Optional<List<T>>` (list rỗng đã đủ "vắng") — over-engineering.
- Optional là API design tool, không thay được kiểm tra null ở ranh giới với code/legacy/JSON trả null.

## 🔗 Bài tiếp theo
👉 [27 — Lambda Expressions](../../04-functional-streams/27-lambda-expressions) (bắt đầu Module 04)
