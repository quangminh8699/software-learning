# Bài 32: Threads Basics — Luồng, vòng đời & race condition

Thread mở ra song song — và ngay lập tức mở ra race condition. Bài này đặt nền cho toàn bộ Module 05.

## 📖 Mô tả
`Thread` là luồng thực thi độc lập, **chia sẻ heap** nhưng có **stack riêng**. Tạo thread qua `Runnable` (ưu tiên) hoặc `extends Thread`. Từ Java 21 có **virtual thread** siêu nhẹ.

## 🔧 Kỹ thuật
- `new Thread(runnable).start()` — tạo & chạy luồng mới. **Không** gọi `run()` trực tiếp.
- `join()` — chờ thread kết thúc.
- `sleep`, `interrupt`, `setDaemon`, `getState`.
- Vòng đời: `NEW → RUNNABLE → (BLOCKED/WAITING/TIMED_WAITING) → TERMINATED`.

## ⚙️ Dưới nắp capo (Under the hood)
```
   TIẾN TRÌNH JVM
   ┌──────────────────────────────────────────────┐
   │  HEAP (chia sẻ mọi thread) ── object, mảng     │
   │                                                │
   │  Thread main   Thread worker-1   Thread w-2     │
   │  [stack riêng] [stack riêng]    [stack riêng]  │  ← biến local KHÔNG chia sẻ
   │  [PC riêng]    [PC riêng]       [PC riêng]      │
   └──────────────────────────────────────────────┘
   Platform thread ─1:1─▶ OS thread (kernel lập lịch)
```
- **Platform thread** ánh xạ **1-1 với OS thread** → tốn ~1MB stack + chi phí context switch của kernel → không tạo được hàng vạn.
- **`start()` vs `run()`**: `start()` yêu cầu JVM/OS tạo luồng mới rồi gọi `run()` trên đó. Gọi `run()` trực tiếp = chạy **tuần tự trên thread hiện tại** (bẫy phỏng vấn).
- **Race condition**: `value++` = **3 thao tác** (load → add → store). Hai thread xen kẽ → ghi đè nhau → **mất cập nhật**. Vì heap chia sẻ mà thao tác không nguyên tử → kết quả < kỳ vọng. (Cách sửa: bài 33.)
- **Lập lịch không xác định**: thứ tự in của các thread khác nhau mỗi lần chạy — đừng giả định thứ tự.
- **Virtual thread (Java 21)**: nhiều virtual thread "ghép" (mount/unmount) lên ít **carrier** (platform) thread; khi blocking I/O thì nhả carrier → tạo **hàng triệu** thread rẻ (bài 35).

## ▶️ Cách dùng
```bash
javac Main.java && java Main      # chạy nhiều lần -> thứ tự & giá trị counter KHÁC nhau
```
Output mẫu (thay đổi mỗi lần):
```
Chạy trong: worker-1
Chạy trong: main
Thread-0 bước 0
Thread-2 bước 0
Thread-1 bước 0
...
Counter KHÔNG đồng bộ (mong đợi 200000): 137480  <- thường NHỎ HƠN do race condition
Thread state trước start: NEW
Virtual thread: VirtualThread[#21,vthread]/runnable@ForkJoinPool-1-worker-1
```

## 🎨 Bản vẽ — race condition trên value++
```
   value = 41
   Thread A: load 41 ┐                ┌ store 42
   Thread B:         └ load 41 ─ +1 ─ store 42   ← cả hai ghi 42
   Kết quả 42 thay vì 43 → MẤT 1 lần tăng
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Đừng `extends Thread`** — chiếm suất kế thừa, trộn "việc cần làm" với "cơ chế chạy". Dùng `Runnable`/`Callable`.
- **Đừng tạo thread thủ công trong code production** — dùng `ExecutorService` (bài 34) để tái dùng + kiểm soát.
- **`value++` không nguyên tử** → cần `synchronized`/`AtomicInteger`/`volatile` tuỳ nhu cầu (bài 33).
- **Không bao giờ giả định thứ tự** thực thi giữa thread.
- `Thread.stop()`/`suspend()` **deprecated** (không an toàn) — dùng cờ + `interrupt()`.

## 🔗 Bài tiếp theo
👉 [33 — Synchronization & Java Memory Model](../33-synchronization)
