# Bài 34: Executor Framework — Thread pool, Callable & Future

Trong production **không ai** `new Thread()` thủ công — họ dùng `ExecutorService`. Bài này giải thích vì sao và cơ chế bên trong pool.

## 📖 Mô tả
`ExecutorService` tách **"việc cần làm" (task)** khỏi **"cách chạy" (thread)**. Một **pool** thread tái dùng nhận task qua hàng đợi. `Callable` trả kết quả; `Future` là "tay cầm" cho kết quả tương lai.

## 🔧 Kỹ thuật
| API | Vai trò |
|-----|---------|
| `Executors.newFixedThreadPool(n)` | n luồng cố định |
| `newCachedThreadPool()` | co giãn theo tải |
| `newVirtualThreadPerTaskExecutor()` | 1 virtual thread/task (Java 21) |
| `submit(Callable)` → `Future` | chạy + nhận tay cầm kết quả |
| `invokeAll` | chạy nhiều, chờ hết |
| `shutdown` / `awaitTermination` | tắt graceful |

## ⚙️ Dưới nắp capo (Under the hood)
```
   submit(task) ─▶ [BlockingQueue task] ◄── worker threads (pool) lấy ra chạy
                          │                     │ điền kết quả
                          ▼                     ▼
                     hàng đợi chờ          Future ← future.get() CHẶN tới khi xong

   ThreadPoolExecutor: corePoolSize, maxPoolSize, keepAlive, workQueue, rejectHandler
```
- **Vì sao pool**: tạo platform thread ~1MB stack + syscall → đắt. Pool **tái dùng** thread cho nhiều task → biên độ tạo/huỷ biến mất; **giới hạn** số luồng → bảo vệ CPU/RAM khỏi quá tải (tránh "thread explosion").
- **`ThreadPoolExecutor`** (đằng sau các factory): khi task tới — nếu < core → tạo worker; nếu đầy core → vào **queue**; queue đầy & < max → tạo thêm; vượt max & queue đầy → **RejectedExecutionHandler**.
- **`Future.get()`** chặn (blocking) tới khi task xong; có bản timeout. `Callable` được phép ném checked exception → gói vào `ExecutionException` khi `get`.
- **Virtual-thread-per-task (Java 21)**: mỗi task một virtual thread (rẻ) → không cần pool để "tiết kiệm thread"; hợp với I/O-bound (hàng vạn kết nối). CPU-bound vẫn dùng pool cố định kích thước ≈ số core.
- `ExecutorService` là `AutoCloseable` (Java 19+) → dùng try-with-resources tự `shutdown` + chờ.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu (rút gọn):
```
Đã submit, làm việc khác trong lúc chờ...
Kết quả Future = 42
Tổng bình phương 1..5 = 55
  chạy trên pool-1-thread-1
  chạy trên pool-1-thread-2
  ... (tái dùng 3 thread cho 6 task)
Pool đã tắt: true
Chạy sau 100ms (scheduled)
Virtual executor: xong trên VirtualThread[#..]/runnable@...
```

## 🎨 Bản vẽ — vòng đời shutdown
```
   RUNNING ──shutdown()──▶ SHUTDOWN (chạy nốt queue) ──hết──▶ TERMINATED
        └──shutdownNow()──▶ STOP (ngắt task đang chạy, bỏ queue)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Luôn `shutdown`** — pool không tắt giữ thread sống → JVM không thoát / rò rỉ thread.
- **`Executors.newFixedThreadPool` dùng queue KHÔNG giới hạn** (`LinkedBlockingQueue`) → task dồn vô hạn → OOM. Production nên tự cấu hình `ThreadPoolExecutor` với **bounded queue** + reject policy.
- **`Future.get()` không timeout** → treo mãi nếu task kẹt. Dùng `get(timeout)`.
- **Kích thước pool**: CPU-bound ≈ số core; I/O-bound lớn hơn (hoặc dùng virtual threads).
- **Đừng nuốt exception trong task**: lỗi trong `submit` nằm trong `Future`; lỗi trong `execute(Runnable)` có thể biến mất nếu không có handler.
- Chia sẻ commonPool với parallel stream → cẩn thận đói thread.

## 🔗 Bài tiếp theo
👉 [35 — Concurrent Utilities & Virtual Threads](../35-concurrent-utilities)
