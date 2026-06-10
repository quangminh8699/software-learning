# Bài 06: Arrays — Mảng & bố cục bộ nhớ

Mảng là cấu trúc dữ liệu nền tảng — và là "ruột" của `ArrayList`, `StringBuilder`, `HashMap`.

## 📖 Mô tả
Mảng: tập phần tử **cùng kiểu**, **kích thước cố định**, nằm **liên tục** trên heap. Truy cập theo index là O(1) nhờ số học địa chỉ. Bài này còn làm rõ mảng 2D của Java thực ra là "mảng của mảng".

## 🔧 Kỹ thuật
- Khai báo: `int[] a = new int[n];` hoặc `int[] a = {...};`
- `a.length` là **trường** (không có dấu ngoặc), khác `String.length()` là method.
- `java.util.Arrays`: `sort`, `binarySearch`, `copyOf`, `fill`, `equals`, `toString`, `stream`.
- Giá trị mặc định: số → `0`, boolean → `false`, object → `null`.

## ⚙️ Dưới nắp capo (Under the hood)
```
   int[] primes = {2,3,5,7,11};  trên HEAP:
   ┌────────┬────────┬──────┬──────┬──────┬──────┬──────┐
   │ header │ length=5│  2   │  3   │  5   │  7   │  11  │
   └────────┴────────┴──────┴──────┴──────┴──────┴──────┘
            ▲ địa chỉ gốc
   primes[i] = *(gốc + offset_data + i * 4 byte)   ← O(1), 1 phép nhân + cộng
```
- **Truy cập O(1)** vì biết địa chỉ gốc + kích thước phần tử → tính thẳng địa chỉ, không cần duyệt.
- **Bounds-check**: JVM chèn kiểm tra `0 <= i < length` trước mỗi truy cập → an toàn bộ nhớ (không buffer overflow như C). JIT loại bỏ check này khi chứng minh được an toàn.
- **Mảng 2D** `int[][]` = mảng các **tham chiếu** tới mảng 1D:
```
   grid ─▶ [ ref, ref, ref ]
            │    │    │
            ▼    ▼    ▼
          {1,2,3}{4,5}{6,7,8,9}   ← các hàng rải rác, độ dài có thể khác (jagged)
```
  → khác C (mảng 2D liền khối). Hệ quả: duyệt theo hàng (cache-friendly) nhanh hơn theo cột.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
primes[2] = 5 (truy cập O(1))
length    = 5 (trường, không phải method)
Bị chặn truy cập ngoài biên: Index 10 out of bounds for length 5
grid[2][3] = 9
Đã sort: [1, 2, 3, 5, 8, 9]
a1 == a2          : false
Arrays.equals     : true
```

## 🎨 Bản vẽ — mảng động (ArrayList) lớn lên
```
   đầy → tạo mảng mới ~1.5x → Arrays.copyOf → bỏ mảng cũ
   [1,2,3,4]  ──grow──▶  [1,2,3,4,_,_]   (amortized O(1) cho add)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **`==` so địa chỉ**, không so nội dung → dùng `Arrays.equals` (1D) / `Arrays.deepEquals` (2D+).
- `Arrays.binarySearch` chỉ đúng khi mảng **đã sort**; trên mảng chưa sort → kết quả vô nghĩa.
- Kích thước **cố định**: cần thay đổi → dùng `ArrayList` (tự `copyOf` khi đầy, amortized O(1)).
- `Arrays.asList(intArray)` với mảng **primitive** tạo `List<int[]>` 1 phần tử (bẫy autobox) — dùng `Arrays.stream(intArray).boxed()` thay thế.
- Mảng 2D lớn: duyệt **theo hàng** thân thiện cache CPU hơn theo cột.

## 🔗 Bài tiếp theo
👉 [07 — Strings](../07-strings)
