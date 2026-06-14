# Bài 33: Synchronization & Java Memory Model

Đây là bài "khó nhất" của Java core: không chỉ atomicity mà cả **visibility** và **reordering** — gốc rễ là **Java Memory Model (JMM)**.

## 📖 Mô tả
Đa thread sai vì hai lý do: (1) **atomicity** — thao tác ghép bị xen kẽ; (2) **visibility/ordering** — thread không thấy thay đổi của nhau do **cache CPU** và **reorder lệnh**. Các công cụ: `synchronized`, `volatile`, `Atomic*`, `Lock`. Lý thuyết nền: **happens-before**.

## 🔧 Kỹ thuật
| Công cụ | Atomicity | Visibility | Ghi chú |
|---------|:---------:|:----------:|---------|
| `synchronized` | ✅ | ✅ | mutual exclusion + monitor |
| `volatile` | ❌ (chỉ đọc/ghi đơn) | ✅ | cờ, double-checked |
| `AtomicInteger` (CAS) | ✅ | ✅ | lock-free, nhanh cho counter |
| `ReentrantLock` | ✅ | ✅ | tryLock, fair, nhiều Condition |

## ⚙️ Dưới nắp capo (Under the hood) — JMM
- **Vấn đề visibility**: mỗi core có **cache riêng**. Thread A ghi `running=false` vào cache của nó; thread B đọc từ cache cũ → **không bao giờ thấy** → vòng lặp vô hạn. JMM cho phép điều này **trừ khi** có quan hệ happens-before.
```
   Core 1 (Thread A)        Core 2 (Thread B)
   [L1 cache: running]      [L1 cache: running]   ← hai bản khác nhau!
            \                      /
             ───── RAM ──────────
   volatile/synchronized chèn MEMORY BARRIER → đồng bộ về RAM.
```
- **`volatile`**: mỗi đọc → lấy từ bộ nhớ chính; mỗi ghi → đẩy ra ngay + chèn **memory barrier** cấm reorder quanh nó. Đảm bảo **visibility** + **ordering**, **nhưng KHÔNG** đảm bảo atomicity của thao tác ghép (`v++` vẫn hỏng).
- **`synchronized`**: lấy **monitor** → khi **acquire**: invalidate cache (đọc giá trị mới nhất); khi **release**: flush mọi thay đổi ra RAM. Vừa mutual exclusion vừa visibility.
- **happens-before** (xương sống JMM): nếu A happens-before B thì B **thấy** mọi thay đổi của A. Các quy tắc chính:
  - Trong cùng thread: theo thứ tự chương trình.
  - **unlock** happens-before **lock** sau đó của cùng monitor.
  - ghi **volatile** happens-before đọc **volatile** sau đó.
  - `Thread.start()` happens-before mọi thứ trong thread; mọi thứ trong thread happens-before `join()` trả về.
  - khởi tạo **final field** happens-before khi constructor kết thúc.
- **CAS (Compare-And-Swap)**: `AtomicInteger.incrementAndGet` dùng lệnh CPU nguyên tử (lock-free), thử-lại nếu giá trị đã đổi → nhanh hơn khoá khi tranh chấp vừa phải.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
synchronized counter (mong đợi 200000): 200000
AtomicInteger (CAS) counter: 200000
ReentrantLock counter: 200000
Worker dừng sau khi thấy cờ, đếm = 18452301
Tất cả counter ĐÚNG vì synchronized/Atomic/Lock thiết lập happens-before.
```

## 🎨 Bản vẽ — happens-before qua khoá
```
   Thread A: ... ghi x=1 ... unlock(M) ─────┐ happens-before
                                            ▼
   Thread B:               lock(M) ... đọc x  → THẤY x=1
```

## ⚠️ Cạm bẫy & lưu ý senior
- **`volatile` không đủ cho `count++`**: nó visibility, không atomicity ghép. Dùng `Atomic*`/`synchronized`.
- **Deadlock**: hai thread khoá theo thứ tự ngược nhau → treo. Quy ước **thứ tự khoá** nhất quán; dùng `tryLock` timeout.
- **Quên `unlock` trong finally** với `ReentrantLock` → khoá kẹt vĩnh viễn.
- **Khoá quá rộng** → mất song song (serialize); **quá hẹp** → race. Khoá đúng vùng dữ liệu chung.
- **Double-checked locking** cần `volatile` cho instance, nếu không thấy object "nửa khởi tạo" (xem bài 45 Singleton).
- Ưu tiên `java.util.concurrent` (Atomic, ConcurrentHashMap — bài 35) hơn tự khoá tay.

## 🔗 Bài tiếp theo
👉 [34 — Executor Framework](../34-executor-framework)
