# Bài 02: Variables & Data Types — Kiểu dữ liệu & nơi biến sống

Không chỉ "khai báo biến", mà **biến nằm ở đâu trong bộ nhớ** và **tốn bao nhiêu**.

## 📖 Mô tả
Java có hai họ kiểu: **primitive** (giá trị thuần) và **reference** (con trỏ tới object). Phân biệt được hai họ này là gốc rễ để hiểu NPE, autoboxing, hiệu năng và bố cục bộ nhớ.

## 🔧 Kỹ thuật
8 kiểu nguyên thuỷ:

| Kiểu | Bytes | Phạm vi / Ghi chú |
|------|-------|-------------------|
| `byte` | 1 | -128 .. 127 |
| `short` | 2 | -32,768 .. 32,767 |
| `int` | 4 | ~±2.1 tỉ — **mặc định cho số nguyên** |
| `long` | 8 | ~±9.2 × 10¹⁸ — hậu tố `L` |
| `float` | 4 | ~7 chữ số — hậu tố `f` |
| `double` | 8 | ~15 chữ số — **mặc định cho số thực** |
| `char` | 2 | UTF-16 (0..65535), thực chất là số |
| `boolean` | (JVM-defined) | `true`/`false` |

- **Casting**: *widening* (nhỏ→lớn) tự động; *narrowing* (lớn→nhỏ) phải ép tay, có thể mất dữ liệu.
- **`var`**: suy luận kiểu lúc compile — vẫn là kiểu tĩnh, không phải dynamic typing.

## ⚙️ Dưới nắp capo (Under the hood)
```
        STACK (mỗi thread)              HEAP (chia sẻ, GC quản lý)
   ┌───────────────────────┐        ┌───────────────────────────┐
   │ frame của main()      │        │  [object String "Java"]   │
   │  int    i   = 2.1e9   │        │   header | "Java"         │
   │  double d   = 3.14    │        │                           │
   │  String name ───────────────▶  └───────────────────────────┘
   │  int[]  arr  ───────────────▶  [object array {1,2,3}]      │
   └───────────────────────┘
   primitive: giá trị NẰM THẲNG     reference: chỉ là địa chỉ
```
- **Primitive** nằm trực tiếp trên stack frame → cấp phát/giải phóng = di chuyển con trỏ stack, **gần như miễn phí**, không cần GC.
- **Reference**: biến trên stack giữ địa chỉ; object thật trên heap → cần GC dọn dẹp.
- **Overflow**: `int` là 32-bit bù 2; `Integer.MAX_VALUE + 1` quay vòng về `MIN_VALUE` **không cảnh báo** — nguồn bug kinh điển khi tính toán thời gian/kích thước.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
javap -c -p Main      # xem các biến local được gán slot trên stack frame
```
Output mẫu:
```
int max   = 2147483647
char 'A'  = 65 (mã UTF-16)
MAX_VALUE + 1 = -2147483648 (overflow quay vòng!)
(int)3.99 = 3 (cắt cụt, không làm tròn)
```

## 🎨 Bản vẽ
```
   Widening (tự động, an toàn):
   byte → short → int → long → float → double
                    char ┘
   Narrowing (ép tay, rủi ro mất dữ liệu): chiều ngược lại + (cast)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **`float`/`double` không biểu diễn chính xác số thập phân** (`0.1 + 0.2 != 0.3`). Dùng tiền tệ → `BigDecimal`.
- **Overflow im lặng**: với phép nhân lớn, dùng `Math.multiplyExact` để ném `ArithmeticException` thay vì sai số âm thầm.
- `char` là **số không dấu 16-bit** → có thể làm toán học trên nó; nhưng không bao trùm hết Unicode (ký tự ngoài BMP cần 2 char / surrogate pair).
- Lạm dụng `var` làm giảm khả năng đọc khi kiểu không hiển nhiên từ vế phải.

## 🔗 Bài tiếp theo
👉 [03 — Operators](../03-operators)
