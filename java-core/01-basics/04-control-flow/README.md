# Bài 04: Control Flow — if/else & switch

Rẽ nhánh điều kiện — và vì sao `switch` đôi khi nhanh hơn chuỗi `if-else`.

## 📖 Mô tả
`if/else` cho điều kiện bất kỳ; `switch` cho việc chọn nhánh theo **một giá trị rời rạc**. Java 14+ thêm **switch expression** an toàn hơn (không fall-through, bắt buộc đầy đủ).

## 🔧 Kỹ thuật
| Cấu trúc | Trả về giá trị? | Fall-through? |
|----------|-----------------|---------------|
| `if/else` | không | — |
| `switch` cổ điển (`case:`) | không | **có** (phải `break`) |
| `switch` expression (`case ->`) | **có** | không |
| `yield` | trả giá trị từ khối `{...}` | — |

switch nhận: `int`, `char`, `String`, `enum`, và (Java 21) **pattern** (xem bài 31).

## ⚙️ Dưới nắp capo (Under the hood)
`switch` trên số/enum biên dịch thành một trong hai lệnh bytecode:
```
  tableswitch  — khi các nhãn LIỀN nhau (1,2,3,4):
       dùng giá trị làm CHỈ SỐ vào bảng địa chỉ -> nhảy O(1)
       [val] ──▶ table[val-min] ──▶ địa chỉ nhánh

  lookupswitch — khi nhãn THƯA (1, 100, 9999):
       cặp (key -> địa chỉ) sắp xếp, tìm nhị phân O(log n)
```
→ Vì vậy `switch` nhiều nhánh thường **nhanh và ổn định hơn** chuỗi `if-else` (vốn kiểm tra tuần tự O(n)).
- `switch` trên `String`: thực chất switch theo `hashCode()` rồi `equals()` xác nhận — vẫn nhanh, nhưng có chi phí hash.
- **switch expression** sinh code không có nhánh "rơi" và buộc bạn xử lý mọi trường hợp → loại cả một lớp bug.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c Main | grep -i switch    # thấy tableswitch / lookupswitch
```
Output mẫu:
```
Buổi chiều
day=3 -> Ngày làm việc
Tháng 4 -> mùa Xuân
code=2 -> BUSY: tải đang cao
```

## 🎨 Bản vẽ — tableswitch
```
   switch(day) với case 1..5 liền nhau
   ┌──────────────────────────────┐
   │ day ─▶ [bảng nhảy]            │
   │        1 → L_work             │   O(1): lấy địa chỉ theo offset
   │        2 → L_work             │   không so sánh tuần tự
   │        ...                    │
   │        default → L_default    │
   └──────────────────────────────┘
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Quên `break`** trong switch cổ điển → fall-through âm thầm. Ưu tiên switch expression `->`.
- `switch(null)` cổ điển ném `NullPointerException`; switch pattern (Java 21) cho phép `case null`.
- Đừng tối ưu sớm: với 2–3 nhánh, `if-else` và `switch` tương đương. `switch` thắng khi **nhiều** nhánh.
- switch trên `String` không "miễn phí" — vẫn hash + equals; với rất ít nhánh, `if-equals` đôi khi đơn giản hơn.

## 🔗 Bài tiếp theo
👉 [05 — Loops](../05-loops)
