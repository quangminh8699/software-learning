# Bài 05: Loops — for, while, do-while, for-each

Vòng lặp là nơi JIT làm việc cật lực nhất — hiểu nó để viết "hot loop" nhanh.

## 📖 Mô tả
Bốn dạng lặp và sự khác biệt **ngầm** giữa chúng ở mức bytecode: đặc biệt `for-each` trên mảng vs trên collection sinh ra code rất khác nhau.

## 🔧 Kỹ thuật
| Dạng | Khi nào | Đặc điểm |
|------|---------|----------|
| `for(init; cond; step)` | biết số lần / cần index | gọn cho đếm |
| `while(cond)` | lặp theo điều kiện | có thể 0 lần |
| `do{}while(cond)` | cần chạy ít nhất 1 lần | kiểm tra cuối |
| `for(T x : iterable)` | duyệt đọc | ngắn gọn, an toàn |
| `break` / `continue` | thoát / bỏ qua | hỗ trợ **label** cho vòng lồng |

## ⚙️ Dưới nắp capo (Under the hood)
- **for-each trên mảng** → compiler sinh vòng `for` theo chỉ số, truy cập `arr[i]` trực tiếp.
  **Không** tạo object iterator → không rác, rất nhanh.
- **for-each trên Collection** → compiler sinh:
  ```
  Iterator it = list.iterator();   // tạo 1 object trên heap
  while (it.hasNext()) { T x = it.next(); ... }
  ```
  → có chi phí cấp phát iterator + gọi method ảo mỗi vòng.
- **JIT optimizations** trên hot loop:
  - *Loop unrolling*: gộp nhiều lần lặp để giảm chi phí kiểm tra điều kiện.
  - *Bounds-check elimination*: bỏ kiểm tra `i < arr.length` khi chứng minh được không tràn.
  - *Loop-invariant code motion*: đẩy biểu thức không đổi ra ngoài vòng.
- `break`/`continue` biên dịch thành lệnh nhảy `goto` tới nhãn — không có chi phí.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c Main     # so sánh for-each mảng (iload/iaload) vs list (invokeinterface next)
```
Output mẫu:
```
Tổng 1..5 = 15
Collatz từ 8 -> 1 mất 3 bước
Tổng mảng = 60
break outer tại i=2, j=3
Số chẵn 1..10: 2 4 6 8 10
```

## 🎨 Bản vẽ
```
   for-each MẢNG                 for-each LIST
   ┌──────────────┐             ┌────────────────────┐
   │ i=0..len-1   │             │ it = list.iterator()│ ← tạo object
   │ x = arr[i]   │  (no alloc) │ while it.hasNext()  │
   └──────────────┘             │   x = it.next()     │ ← gọi method ảo
                                └────────────────────┘
```

## ⚠️ Cạm bẫy & lưu ý senior
- **ConcurrentModificationException**: sửa collection (add/remove) trong khi for-each → iterator phát hiện modCount đổi. Dùng `Iterator.remove()` hoặc `removeIf`.
- **Off-by-one**: `<=` vs `<` ở điều kiện dừng — bug kinh điển.
- Vòng `while(true)` không thoát → CPU 100%; luôn có điều kiện thoát rõ ràng.
- Đo hiệu năng vòng lặp phải để JIT "warm up" (chạy nhiều lần) — benchmark sai lầm thường đo lúc còn interpreter. Dùng **JMH** cho microbenchmark.

## 🔗 Bài tiếp theo
👉 [06 — Arrays](../06-arrays)
