# Bài 07: Strings — Immutability, String Pool, StringBuilder

Chuỗi là kiểu dùng nhiều nhất — và là nơi lập trình viên hay viết code chậm O(n²) mà không biết.

## 📖 Mô tả
`String` trong Java **bất biến** (immutable): một khi tạo, nội dung không đổi. Mọi "sửa đổi" đều tạo object mới. Bài này giải thích vì sao thiết kế vậy, **String Pool** là gì, và khi nào phải dùng `StringBuilder`.

## 🔧 Kỹ thuật
- **Immutable**: `toUpperCase`, `substring`, `replace`... đều trả về `String` **mới**.
- **String Pool**: literal được "intern" → các literal giống nhau dùng chung 1 object.
- `==` so **địa chỉ**; `.equals()` so **nội dung** — luôn dùng `.equals()` cho nội dung.
- `StringBuilder`: bộ đệm `char[]`/`byte[]` **đột biến**, dùng để nối nhiều mảnh.
- **Text block** `"""..."""` (Java 15+): chuỗi nhiều dòng.

## ⚙️ Dưới nắp capo (Under the hood)
```
   HEAP
   ┌─────────────── String Pool ───────────────┐
   │  "Java"  ◄── a ◄── b   (cùng object)       │
   └────────────────────────────────────────────┘
   ┌─ object riêng (do `new`) ─┐
   │  "Java"  ◄── c            │  c.intern() ──▶ kéo về pool
   └───────────────────────────┘
```
- **Vì sao immutable?**
  1. **An toàn thread**: chia sẻ giữa nhiều thread không cần khoá.
  2. **Cache hashCode**: `String` lưu sẵn hash (tính 1 lần) → key HashMap nhanh.
  3. **Pool an toàn**: vì không đổi, mới chia sẻ chung được.
  4. **Bảo mật**: tham số (đường dẫn, URL) không bị sửa lén sau khi kiểm tra.
- **Nối chuỗi**: `s += x` trong vòng lặp → mỗi lần tạo `StringBuilder` ẩn + `String` mới → sao chép lại toàn bộ → **O(n²)** và sinh rác lớn. `StringBuilder` giữ một mảng, chỉ mở rộng khi đầy → **O(n)**.
- Từ Java 9, `String` lưu `byte[]` + cờ mã hoá (Latin-1/UTF-16) thay vì `char[]` → tiết kiệm ~một nửa bộ nhớ cho chuỗi ASCII (**Compact Strings**).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
a == b (pool)      : true
a == c (new)       : false
a.equals(c) (nội dung): true
a == c.intern()    : true
s gốc giữ nguyên   : hello
StringBuilder: 01234
```

## 🎨 Bản vẽ — vì sao `+=` chậm
```
   "" + 0  → "0"          (copy 1)
   "0" + 1 → "01"         (copy 2)
   "01" + 2 → "012"       (copy 3)   tổng copy = 1+2+3+... = O(n²)

   StringBuilder: [0][1][2]...  chỉ ghi thêm vào cuối → O(n)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Đừng so sánh chuỗi bằng `==`** (trừ khi cố ý so identity). Bug ẩn vì literal pool đôi khi cho `true` "may mắn".
- `String.intern()` dùng bừa có thể gây áp lực lên pool (vùng đặc biệt trong heap) — chỉ intern khi thật sự cần khử trùng lặp.
- `substring` (Java 7+) sao chép mảng con → không còn rò rỉ giữ tham chiếu chuỗi gốc như Java 6.
- Với chuỗi nhạy cảm (mật khẩu), `String` bất biến nằm lại trong bộ nhớ tới khi GC → dùng `char[]` rồi xoá thủ công.
- Nối chuỗi đơn giản một dòng (`a + b + c`) thì compiler tự dùng `StringBuilder`/`invokedynamic` → không cần tối ưu tay; chỉ lo khi **nối trong vòng lặp**.

## 🔗 Bài tiếp theo
👉 [08 — Methods](../08-methods)
