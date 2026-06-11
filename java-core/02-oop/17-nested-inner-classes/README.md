# Bài 17: Nested & Inner Classes — 4 loại class lồng nhau

Class lồng class — và vì sao inner class **non-static** là nguồn memory leak kinh điển.

## 📖 Mô tả
Java có 4 kiểu class lồng:
1. **static nested** — không giữ tham chiếu outer.
2. **inner (non-static)** — giữ con trỏ ẩn tới object outer.
3. **local** — khai báo trong method.
4. **anonymous** — vô danh, cài đặt 1 lần.

## 🔧 Kỹ thuật
| Loại | Giữ Outer.this? | Tạo thế nào |
|------|:---------------:|-------------|
| static nested | ❌ | `Outer.StaticNested s = new Outer.StaticNested()` |
| inner | ✅ | `outer.new Inner()` |
| local | ✅ (capture biến) | trong thân method |
| anonymous | ✅ | `new Iface() { ... }` |

## ⚙️ Dưới nắp capo (Under the hood)
- Mỗi loại biên dịch thành **file `.class` riêng**: `Outer$StaticNested.class`, `Outer$Inner.class`, `Main$1.class` (anonymous được đánh số).
- **inner class** có **field ẩn** `this$0` trỏ tới object Outer → đó là cách nó đọc field outer. Hệ quả: **giữ object inner sống = giữ luôn object Outer sống** → nếu inner (vd listener, Runnable) tồn tại lâu → outer **không được GC** → memory leak.
- **local & anonymous** "capture" biến local: compiler **sao chép** biến `effectively final` vào field tổng hợp của class lồng. Vì là bản sao → biến phải bất biến (effectively final) để tránh hai bản lệch nhau.
- **anonymous** cài functional interface → từ Java 8 nên thay bằng **lambda** (gọn hơn, **không** sinh class file riêng, dùng `invokedynamic` — xem bài 27).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
ls *.class        # thấy Outer$Inner.class, Outer$StaticNested.class, Main$1.class
```
Output mẫu:
```
static nested: độc lập, không cần Outer
inner đọc field Outer: DỮ LIỆU NGOÀI
local class: 84
Xin chào từ anonymous
Cùng việc đó, viết bằng lambda
```

## 🎨 Bản vẽ — inner giữ outer (leak)
```
   [Outer object] ◄──── this$0 ──── [Inner object] ◄── listener list (sống lâu)
        ▲                                                     
        └─ không GC được vì Inner còn giữ tham chiếu ngầm → LEAK
   Giải pháp: dùng static nested + truyền dữ liệu cần thiết qua tham số.
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Mặc định ưu tiên `static` nested**: chỉ dùng inner (non-static) khi thật sự cần truy cập instance của outer. Đây là khuyến nghị Effective Java (giảm leak + giảm bộ nhớ thừa `this$0`).
- **Memory leak** kinh điển: `Handler`/`Runnable`/listener là inner class non-static giữ sống Activity/Service/Outer lớn.
- Biến bị capture phải **effectively final** → không gán lại sau khi dùng trong class lồng.
- Anonymous class có `this` trỏ **chính nó**, còn lambda có `this` trỏ **enclosing instance** — khác biệt quan trọng khi dùng `this`.

## 🔗 Bài tiếp theo
👉 [18 — Enums](../18-enums)
