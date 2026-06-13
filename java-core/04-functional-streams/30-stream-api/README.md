# Bài 30: Stream API — Pipeline xử lý dữ liệu khai báo

Stream biến vòng lặp thành pipeline khai báo — nhưng "lazy" và "parallel" giấu nhiều điều senior phải nắm.

## 📖 Mô tả
Stream là chuỗi xử lý: **nguồn** → các phép **trung gian** (lazy) → một phép **kết thúc** (terminal, kích hoạt tính toán). Tư duy "cái gì cần làm" thay vì "lặp thế nào".

## 🔧 Kỹ thuật
| Loại | Ví dụ | Đặc điểm |
|------|-------|----------|
| Nguồn | `collection.stream()`, `Stream.of`, `IntStream.range` | tạo stream |
| Trung gian (lazy) | `filter map sorted distinct limit peek flatMap` | trả Stream, chưa chạy |
| Kết thúc (eager) | `collect reduce forEach count findFirst anyMatch` | kích hoạt pipeline |
| Collectors | `toList groupingBy partitioningBy joining counting` | gom kết quả |

## ⚙️ Dưới nắp capo (Under the hood)
- **Lazy evaluation**: phép trung gian chỉ **ghi nhận** (xây pipeline), **không chạy**. Chỉ khi gặp **terminal**, dữ liệu mới được "kéo" qua. Bằng chứng: `peek` chỉ in các phần tử thực sự chảy qua.
- **Loop fusion (stream fusion)**: nhiều phép trung gian được gộp duyệt **một lần** qua dữ liệu — không tạo collection trung gian cho mỗi bước (khác việc `for` + list tạm). `filter().map().collect()` = **1 vòng**.
- **Short-circuit**: `findFirst`, `anyMatch`, `limit` dừng ngay khi đủ → không xử lý hết nguồn (kể cả stream vô hạn `Stream.iterate`).
- **Stream primitive** (`IntStream`/`LongStream`/`DoubleStream`): tránh autoboxing + có `sum/average/summaryStatistics` sẵn.
- **parallelStream**: tách dữ liệu (spliterator) chạy trên **ForkJoinPool.commonPool**. Chỉ nhanh khi: dữ liệu **lớn**, thao tác **độc lập & không side-effect**, nguồn **chia tách rẻ** (ArrayList tốt, LinkedList tệ). Đo đạc trước khi dùng — thường **chậm hơn** với data nhỏ.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu (rút gọn):
```
Pipeline: [BÌNH, CƯỜNG, DŨNG]
--- Lazy: findFirst dừng sớm ---
  xét 1
  xét 2
findFirst chẵn = 2 (không xét hết 5 phần tử)
reduce sum = 15
IntStream stats: sum=55, avg=5.5, max=10
groupingBy độ dài: {2=[An], 4=[Bình, Dũng], 5=[Cường]}
đếm theo độ dài (gồm trùng): {2=2, 4=3, 5=1}
partition chẵn/lẻ: {false=[1, 3, 5], true=[2, 4, 6]}
joining: [An, Bình, Cường, Dũng]
flatMap: [1, 2, 3, 4, 5]
parallel count chẵn (1..1tr): 500000
```

## 🎨 Bản vẽ — pipeline lazy
```
   source ─▶ filter ─▶ map ─▶ sorted ─▶ [collect] ◄── chỉ chạy khi tới đây
              (lazy)   (lazy)  (lazy)     TERMINAL
   Dữ liệu chảy qua MỘT lần (fusion), không tạo list trung gian từng bước.
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Stream dùng một lần**: tiêu thụ rồi (terminal) → tái dùng → `IllegalStateException`.
- **Side-effect trong map/filter** (sửa biến ngoài) → sai trong parallel + khó đọc. Stream nên "thuần".
- **`forEach` trên parallel** không đảm bảo thứ tự; cần thứ tự dùng `forEachOrdered` (mất lợi ích song song).
- **parallelStream là cái bẫy**: data nhỏ/thao tác rẻ → chậm hơn do chi phí chia + đồng bộ; dùng chung commonPool có thể đói thread. Luôn benchmark.
- **Không dùng stream cho mọi thứ**: vòng lặp đơn giản đôi khi rõ + nhanh hơn. Stream tỏa sáng ở biến đổi/gom phức tạp.
- `peek` chỉ để debug, đừng dùng cho logic chính.

## 🔗 Bài tiếp theo
👉 [31 — Pattern Matching](../31-pattern-matching)
