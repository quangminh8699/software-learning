# Bài 20: Exception Handling — Lỗi, tài nguyên & chi phí ẩn

Xử lý lỗi đúng cách + hiểu vì sao tạo exception **đắt** (chụp stack trace).

## 📖 Mô tả
Exception tách luồng xử lý lỗi khỏi luồng chính. Java chia **checked** (ép xử lý lúc compile) và **unchecked** (`RuntimeException`/`Error`). `try-with-resources` tự đóng tài nguyên an toàn.

## 🔧 Kỹ thuật
| Khái niệm | Mô tả |
|-----------|-------|
| `try/catch/finally` | bắt & dọn dẹp; `finally` luôn chạy |
| `throw` / `throws` | ném / khai báo có thể ném |
| checked | kế thừa `Exception` (không phải Runtime) — compiler ép |
| unchecked | kế thừa `RuntimeException`/`Error` — không ép |
| try-with-resources | tự `close()` mọi `AutoCloseable` |
| chaining | `new X(msg, cause)` giữ nguyên nhân gốc |

## ⚙️ Dưới nắp capo (Under the hood)
```
   Cây phân cấp:
   Throwable
   ├── Error          (lỗi hệ thống nặng: OutOfMemory, StackOverflow) — đừng bắt
   └── Exception
       ├── (checked)  IOException, SQLException...   ← compiler ép
       └── RuntimeException (unchecked) NPE, IllegalArgument...
```
- **Khi `throw`**: JVM bắt đầu **stack unwinding** — pop từng frame, ở mỗi frame tìm `catch` khớp kiểu; không thấy thì pop tiếp tới khi tới đáy (thread chết) → đó là lý do exception "thoát" qua nhiều method.
- **Chi phí lớn nằm ở `fillInStackTrace()`**: khi **tạo** exception, JVM **chụp toàn bộ stack** → tốn. Vì vậy **không** dùng exception cho luồng điều khiển bình thường. (Có thể override `fillInStackTrace` hoặc dùng exception không stack trace cho hot path.)
- **try-with-resources**: compiler sinh `finally` ẩn gọi `close()` theo **thứ tự ngược** khai báo; nếu cả body và close cùng ném → lỗi body là chính, lỗi close vào `getSuppressed()`.
- **`finally` + `return`**: `return` trong `finally` **nuốt** exception/đè giá trị return của try → anti-pattern.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Bắt cụ thể: Index 5 out of bounds for length 2
finally: luôn chạy
Mở DB
Mở File
Dùng DB
Dùng File
Đóng File
Đóng DB
Lỗi: Không đọc được config | nguyên nhân: java.lang.NumberFormatException: For input string: "abc"
Xong.
```

## 🎨 Bản vẽ — stack unwinding
```
   main ── readConfig ── parseInt  ✗ throw
     ▲          ▲           │
     │          │  pop ◄────┘ tìm catch ở parseInt? không
     │  pop ◄───┘ catch ở readConfig? có (NumberFormatException) → bọc lại
   catch ở main (ConfigException) → xử lý
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Nuốt exception**: `catch (Exception e) {}` rỗng → lỗi biến mất, debug địa ngục. Tối thiểu log + cause.
- **Bắt `Throwable`/`Error`**: đừng — `OutOfMemoryError` không nên "xử lý tiếp".
- **Mất cause**: `throw new X(msg)` thay vì `throw new X(msg, cause)` → mất root cause. Luôn chain.
- **Checked exception lạm dụng**: ép caller try-catch tràn lan → nhiều team chọn unchecked + xử lý tập trung. Quyết định kiến trúc, cần nhất quán.
- **Exception cho control flow** (vd kết thúc vòng lặp) → chậm vì chụp stack trace. Dùng giá trị trả về/`Optional`.

## 🔗 Bài tiếp theo
👉 [21 — Wrapper & Autoboxing](../21-wrapper-autoboxing)
