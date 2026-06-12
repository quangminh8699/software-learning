# Bài 25: Iterator & Comparator — Duyệt & sắp xếp

`Iterator` là động cơ thầm lặng sau mọi `for-each`; `Comparator` là cách sắp xếp linh hoạt không cần sửa class.

## 📖 Mô tả
- **Iterator/Iterable**: giao thức duyệt phần tử mà không lộ cấu trúc bên trong. `for-each` chạy được vì collection là `Iterable`.
- **Comparable**: thứ tự "tự nhiên" (1 cái, nằm trong class).
- **Comparator**: thứ tự "bên ngoài" (nhiều cái, tách rời class).

## 🔧 Kỹ thuật
| | Comparable | Comparator |
|--|-----------|-----------|
| Ở đâu | trong class (`compareTo`) | tách riêng (`compare`) |
| Số thứ tự | 1 (tự nhiên) | nhiều |
| Sửa class? | có | không |
| API tiện | — | `comparing`, `thenComparing`, `reversed`, `nullsFirst` |

## ⚙️ Dưới nắp capo (Under the hood)
- **for-each** trên `Iterable` biên dịch thành:
  ```
  Iterator it = c.iterator();
  while (it.hasNext()) { T x = it.next(); ... }
  ```
  → vì vậy class của bạn chỉ cần `implements Iterable` là dùng được for-each.
- **`Iterator.remove()`** là cách **duy nhất** xoá an toàn khi đang duyệt: nó cập nhật `modCount`/`expectedModCount` đồng bộ → không ném `ConcurrentModificationException`.
- **Sắp xếp**: `Collections.sort`/`List.sort` dùng **TimSort** — ổn định (stable), thích nghi, O(n log n) tệ nhất, gần O(n) khi gần như đã sort. Nó gọi `compareTo`/`compare` để quyết thứ tự.
- **`compare` phải nhất quán**: phản đối xứng + bắc cầu. Vi phạm (vd trả về sai khi tràn số: `a - b` overflow) → `IllegalArgumentException: Comparison method violates its general contract`. Dùng `Integer.compare(a,b)` thay `a-b`.
- `Comparator.comparing(...).thenComparing(...)` xây bộ so sánh ghép tầng — đọc như tiếng Anh, an toàn null với `nullsFirst/Last`.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Sau xoá chẵn qua Iterator: [1, 3, 5]
for-each trên Iterable tự viết: 1 2 3
Sort tự nhiên (theo tuổi): [Bình(25), An(30), Cường(30)]
Sort theo tuổi rồi tên: [Bình(25), An(30), Cường(30)]
Sort theo tên giảm dần: [Cường(30), Bình(25), An(30)]
Trẻ nhất (đỉnh heap): Bình(25)
```

## 🎨 Bản vẽ — Iterator protocol
```
   for (x : coll)
        │
        ▼
   it = coll.iterator()
   ┌──────────────┐
   │ hasNext()?   │──no──▶ kết thúc
   │   yes        │
   │ x = next()   │──▶ xử lý ──┐
   └──────────────┘            │
        ▲───────────────────────┘
```

## ⚠️ Cạm bẫy & lưu ý senior
- **`a - b` trong comparator** → tràn số với giá trị lớn → vi phạm hợp đồng. Luôn `Integer.compare`/`Long.compare`.
- Sửa collection trong khi for-each (không qua iterator) → `ConcurrentModificationException`.
- `compareTo` nên **nhất quán với `equals`** (nếu không, `TreeSet`/`TreeMap` coi "bằng" theo compare, bỏ phần tử "khác equals").
- Comparator tạo lambda mới mỗi lần gọi sort trong vòng nóng → cache comparator dùng lại.
- `PriorityQueue`/`TreeMap` phụ thuộc comparator để định thứ tự — đổi comparator = đổi cấu trúc.

## 🔗 Bài tiếp theo
👉 [26 — Optional](../26-optional)
