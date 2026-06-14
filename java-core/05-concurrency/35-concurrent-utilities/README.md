# Bài 35: Concurrent Utilities & Virtual Threads

`java.util.concurrent` là "vũ khí hạng nặng": async pipeline (`CompletableFuture`), cấu trúc thread-safe, và virtual threads (Java 21) thay đổi cách viết I/O song song.

## 📖 Mô tả
Thay vì tự khoá tay, dùng các tiện ích cấp cao: `CompletableFuture` (bất đồng bộ, ghép pipeline), `ConcurrentHashMap`/`LongAdder` (thread-safe ít tranh chấp), `CountDownLatch` (đồng bộ), và **virtual threads** (triệu task rẻ).

## 🔧 Kỹ thuật
| Công cụ | Dùng khi |
|---------|----------|
| `CompletableFuture` | chuỗi async không chặn, gọi nhiều service |
| `ConcurrentHashMap` | map chia sẻ nhiều thread |
| `LongAdder` | counter ghi nhiều, đọc ít |
| `CountDownLatch` | chờ N việc xong |
| `Semaphore` | giới hạn số truy cập đồng thời |
| Virtual threads | hàng vạn task I/O-bound |

`CompletableFuture`: `thenApply` (map), `thenCompose` (flatMap), `thenCombine` (zip 2 future), `exceptionally`/`handle` (lỗi).

## ⚙️ Dưới nắp capo (Under the hood)
- **CompletableFuture** là máy trạng thái: mỗi `thenX` đăng ký **callback** chạy khi stage trước hoàn thành (trên executor mặc định `commonPool`, hoặc `*Async(executor)` chỉ định). → **không chặn** thread chờ; ghép pipeline phản ứng theo sự kiện.
  - `thenCompose` = flatMap → tránh `CompletableFuture<CompletableFuture<T>>` lồng nhau.
  - `thenCombine` chạy 2 nhánh **song song** rồi gộp.
- **ConcurrentHashMap**: thay vì 1 khoá toàn map (`Collections.synchronizedMap`), nó khoá ở mức **bin/node** (CAS cho ô trống, synchronized cho bin có va chạm) → nhiều thread ghi các bin khác nhau **không** chặn nhau → throughput cao. `merge`/`compute` là **nguyên tử**.
- **LongAdder vs AtomicLong**: AtomicLong CAS một biến → khi nhiều thread tranh chấp, CAS fail + retry liên tục (cache line ping-pong). LongAdder **tách ra nhiều cell** (mỗi thread cập nhật cell riêng) → `sum()` cộng lại → nhanh hơn nhiều khi ghi nóng.
- **Virtual threads (Project Loom)**: virtual thread "mount" lên một **carrier** (platform) thread khi chạy; khi gặp blocking (I/O, sleep) nó **unmount** → nhả carrier cho virtual thread khác → JVM lập lịch hàng **triệu** virtual thread trên ít OS thread. Stack lưu trên heap, lớn lên theo nhu cầu → rẻ. → Viết code blocking **kiểu tuần tự** mà vẫn scale như async.
```
   1 triệu virtual threads ─ghép─▶ ~số-core carrier threads ─▶ OS
   blocking I/O → unmount carrier (không lãng phí OS thread)
```

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu (rút gọn):
```
CompletableFuture: DỮ-LIỆU-đã-xử-lý
thenCompose: 20
thenCombine (song song): 7
exceptionally: phục hồi: java.lang.RuntimeException: hỏng
ConcurrentHashMap count (mong đợi 40000): 40000
LongAdder (4x100k): 400000
  task 0 xong
  task 1 xong
  task 2 xong
Tất cả 3 task đã xong (latch)
10.000 virtual threads, tổng id = 49995000
```

## 🎨 Bản vẽ — CompletableFuture pipeline
```
   supplyAsync ──▶ thenApply ──▶ thenApply ──▶ get()
       (async)      (map)         (map)
            \                        exceptionally ◄── nếu lỗi
             thenCombine(other) ──▶ gộp 2 nhánh song song
```

## ⚠️ Cạm bẫy & lưu ý senior
- **CompletableFuture nuốt lỗi**: quên `exceptionally`/`handle` → lỗi im lặng tới khi `get` ném `ExecutionException`. Luôn xử lý nhánh lỗi.
- **Dùng commonPool cho task blocking** → đói thread (commonPool nhỏ ≈ core). Cấp executor riêng cho I/O, hoặc dùng virtual threads.
- **ConcurrentHashMap**: thao tác đơn nguyên tử, nhưng **gộp nhiều thao tác** (get rồi put) không nguyên tử → dùng `compute`/`merge`.
- **Virtual threads + `synchronized`**: khi pin (giữ) trên synchronized chứa blocking → có thể "pin" carrier (đang cải thiện qua các bản). Ưu tiên `ReentrantLock` trong code virtual-thread nặng I/O.
- **Đừng pool virtual threads** — chúng đã rẻ; tạo mới mỗi task (`newVirtualThreadPerTaskExecutor`).
- `size()` của ConcurrentHashMap chỉ gần đúng dưới tải đồng thời.

## 🔗 Bài tiếp theo
👉 [36 — File I/O](../../06-advanced/36-file-io) (bắt đầu Module 06)
