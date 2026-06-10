# Bài 03: Operators — Toán tử số học, logic, bitwise, ternary

Toán tử là nơi code gần CPU nhất — hiểu chúng giúp viết logic đúng và tối ưu bit-level.

## 📖 Mô tả
Các nhóm toán tử trong Java và quan trọng hơn: **chúng dịch thành lệnh gì**, **short-circuit hoạt động ra sao**, và **bitwise dùng để làm gì trong thực tế**.

## 🔧 Kỹ thuật
| Nhóm | Toán tử | Ghi chú |
|------|---------|---------|
| Số học | `+ - * / %` | `/` giữa hai `int` là chia nguyên |
| So sánh | `== != < > <= >=` | với object, `==` so **địa chỉ**, không so nội dung |
| Logic | `&& \|\| !` | `&&`/`\|\|` **short-circuit** |
| Bitwise | `& \| ^ ~` | thao tác từng bit |
| Shift | `<< >> >>>` | `>>` giữ dấu, `>>>` điền 0 |
| Gán | `= += -= ...` | |
| Ternary | `cond ? a : b` | là **expression** |

## ⚙️ Dưới nắp capo (Under the hood)
- Phép số học map gần như 1-1 sang bytecode ALU: `iadd`, `isub`, `imul`, `idiv`, `irem`...
  rồi JIT dịch tiếp sang lệnh CPU.
- **Short-circuit** sinh **nhánh rẽ (branch)** trong bytecode: `&&` nhảy bỏ qua vế phải nếu vế trái false.
  → vừa tránh tính thừa, vừa là kỹ thuật **null-guard**: `o != null && o.ok()`.
- **Bitwise** trên số **bù 2 (two's complement)**:
```
   12 = 0000 1100
   10 = 0000 1010
   &  = 0000 1000 = 8     |  = 0000 1110 = 14     ^ = 0000 0110 = 6
   << 1: dịch trái, điền 0 vào phải  -> nhân 2
   >> 1: dịch phải, GIỮ bit dấu       -> chia 2 (số âm vẫn âm)
   >>>1: dịch phải, ĐIỀN 0 vào trái   -> coi như không dấu
```
- `>>` vs `>>>` chỉ khác nhau với **số âm** (bit dấu = 1). Đây là lý do `HashMap` dùng `>>>` khi trộn hash.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c Main         # xem iadd/idiv/iand/ishl... tương ứng
```
Output mẫu (rút gọn):
```
7 / 2   = 3
7 % 2   = 1
12 & 10 = 8
12 << 1 = 24
-8 >>> 1 = 2147483644
Có quyền WRITE? true
```

## 🎨 Bản vẽ — Bitmask quyền
```
   bit:   ... 4   2   1
   EXEC ───┘   │   │
   WRITE ──────┘   │
   READ ───────────┘
   perms = READ | WRITE = 0b011 = 3
   kiểm tra:  perms & WRITE  != 0  -> đang bật
```

## ⚠️ Cạm bẫy & lưu ý senior
- `==` trên object so **reference**, không so giá trị → dùng `.equals()` cho nội dung (xem bài String).
- Chia cho 0: **số nguyên** ném `ArithmeticException`; **số thực** cho `Infinity`/`NaN` (không ném).
- Trộn `int`/`long` trong dịch bit: `1 << 40` cho kết quả sai vì `1` là `int` (dịch lấy mod 32). Dùng `1L << 40`.
- `a++ ` vs `++a`: nhớ post-increment trả giá trị **trước khi** tăng — bẫy trong biểu thức phức tạp.

## 🔗 Bài tiếp theo
👉 [04 — Control Flow](../04-control-flow)
