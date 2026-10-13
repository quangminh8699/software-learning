## WHAT IS SEMAPHORE ?

A `Semaphore` is a concurrency primitive that controls access to a **pool of N permits**. Unlike a mutex (which allows exactly one thread at a time), a semaphore allows up to **N** threads to proceed simultaneously — ideal for rate-limiting, connection pools, and bounded resource access.

Conceptually, a semaphore keeps a counter. `acquire()` decrements it (blocks if counter is 0); `release()` increments it. When N = 1, it acts like a mutex — a **binary semaphore**.

```
Two modes by permit count:
1. Binary semaphore (N=1)   — mutual exclusion, like a lock
2. Counting semaphore (N>1) — limits concurrent access to N threads at a time
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "Semaphore để giải quyết race condition." **SAI.** Xem phần "Semaphore KHÔNG chống race condition" bên dưới — đây là điểm quan trọng nhất của bài.

---

## 🅿️ VÍ DỤ ĐỜI THƯỜNG — Bãi giữ xe có N chỗ

Từ nãy giờ mọi công cụ đều "chỉ cho **1** người". Semaphore khác hẳn: cho **N người cùng lúc**.

Bãi giữ xe có **3 chỗ**. Bác bảo vệ giữ **3 tấm vé**.

- Xe vào → bác đưa **1 vé** → còn 2.
- Xe nữa → còn 1.
- Xe thứ 3 → **hết vé**, bãi đầy.
- Xe thứ 4 tới → bác nói "đợi đi" → **xe 4 đứng đợi ngoài cổng**.
- Một xe ra → **trả vé** → bác đưa vé cho xe 4 → xe 4 vào được.

👉 `Semaphore(3)` = **bác bảo vệ với 3 tấm vé** đó. Cho tối đa 3 xe cùng lúc, xe thứ 4 phải đợi.

**Dùng làm gì trong thực tế:**
- **Connection pool:** DB chịu 10 kết nối → `Semaphore(10)`.
- **Rate limiter:** chỉ cho 5 request xử lý cùng lúc.
- **Giới hạn tài nguyên:** quán có 3 nhân viên → tối đa 3 khách được phục vụ.

---

## ❌ SEMAPHORE KHÔNG CHỐNG RACE CONDITION

Đây là điểm dễ nhầm nhất. Việc của Semaphore **chỉ có một**: giới hạn *bao nhiêu người* được vào cùng lúc. Nó **không quan tâm** những người bên trong có phá dữ liệu của nhau không.

Quay lại bài toán hai vợ chồng rút tiền (bài Race Condition). Nếu dùng `Semaphore(2)`:
- Cả **vợ và chồng đều được cấp vé** → cả hai cùng đọc số dư, cùng rút → **vẫn lỗi y như cũ**.

👉 Race condition được sửa bằng nguyên tắc **"với dữ liệu chung, chỉ cho ĐÚNG 1 người đụng vào một lúc"** (mutual exclusion). Con số quan trọng là **1**.

| Công cụ | Ép được "chỉ 1 người"? |
|---|---|
| `synchronized` | ✅ bản chất là 1 |
| `ReentrantLock` | ✅ bản chất là 1 |
| `Semaphore(1)` | ✅ chỉ 1 vé (tình cờ giống lock) |
| `Semaphore(2)` trở lên | ❌ **KHÔNG** — cho nhiều người vào cùng lúc |

**Vậy tại sao rạp phim 100 ghế lại dùng `Semaphore(100)` đúng?** Vì hai bài toán **khác nhau**:

- 🏦 **Ngân hàng:** có một biến CHUNG `balance` mà **mỗi thread tự tay đọc → sửa → ghi**. Bắt buộc chỉ 1 người → cần **lock**.
- 🎬 **Rạp phim:** **tấm vé CHÍNH LÀ cái ghế**. Bạn không tự đếm — **Semaphore đếm hộ** (an toàn sẵn bên trong). Chỉ cần giới hạn tổng số = 100. Người thứ 101 tự động bị chặn.

> **Câu hỏi tự vấn mỗi khi gặp bài toán:** "Mình đang *bảo vệ một dữ liệu chung ai cũng tự sửa*? → dùng lock. Hay chỉ *đếm/giới hạn số lượng*? → dùng Semaphore."

---

## 1. Binary Semaphore — Mutual Exclusion

> "Semaphore 1 permit hành xử như một mutex **non-reentrant**."

Khác `ReentrantLock`, semaphore **không có owner** — bất kỳ thread nào cũng gọi `release()` được, kể cả thread chưa từng `acquire()`. Hữu ích cho signalling, nhưng nguy hiểm nếu lạm dụng như một lock.

```java
import java.util.concurrent.Semaphore;

class SafeCounter {
    private final Semaphore semaphore = new Semaphore(1); // 1 permit = binary
    private int count = 0;

    public void increment() throws InterruptedException {
        semaphore.acquire();    // Giảm permit về 0 — người khác bị chặn
        try {
            count++;
        } finally {
            semaphore.release(); // Tăng permit về lại 1
        }
    }
}
```

> Cho mutual exclusion, ưu tiên `ReentrantLock` — binary semaphore không track ownership, nên một bug ở thread này có thể lỡ tay release khóa mà thread khác đang giữ.

---

## 2. Counting Semaphore — Bounded Concurrency

> "Cho tối đa N thread dùng tài nguyên cùng lúc; thread thứ (N+1) chặn tới khi có người xong."

Use-case phổ biến nhất là **connection pool**.

```java
import java.util.concurrent.Semaphore;

class ConnectionPool {
    private final Semaphore semaphore;
    private final String[] connections;

    ConnectionPool(int poolSize) {
        semaphore = new Semaphore(poolSize, true); // fair=true — FIFO
        connections = new String[poolSize];
        for (int i = 0; i < poolSize; i++) connections[i] = "Connection-" + i;
    }

    public String acquire() throws InterruptedException {
        semaphore.acquire();                 // Chặn nếu tất cả kết nối đang bận
        return getConnection();
    }

    public void release(String connection) {
        returnConnection(connection);
        semaphore.release();                 // Báo: một slot vừa trống
    }

    private synchronized String getConnection() {
        for (int i = 0; i < connections.length; i++) {
            if (connections[i] != null) {
                String conn = connections[i];
                connections[i] = null;       // Đánh dấu slot đã lấy
                return conn;
            }
        }
        throw new IllegalStateException("No connection available");
    }

    private synchronized void returnConnection(String conn) {
        for (int i = 0; i < connections.length; i++) {
            if (connections[i] == null) { connections[i] = conn; return; }
        }
    }
}
```

> 📌 Chú ý: semaphore chỉ giới hạn *số lượng*. Việc chọn/trả đúng connection (dữ liệu chung — mảng `connections`) vẫn cần `synchronized` riêng. Đây minh họa rõ: **Semaphore lo số lượng, lock lo dữ liệu chung** — hai việc khác nhau.

---

## 3. tryAcquire — Non-blocking Attempt

> "Thử lấy vé ngay; không có thì trả false thay vì chặn."

Giống `ReentrantLock.tryLock()`. Bản overload `tryAcquire(timeout, unit)` chờ tối đa một khoảng.

```java
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class RateLimiter {
    private final Semaphore semaphore = new Semaphore(5); // Tối đa 5 request đồng thời

    public void handleRequest(String requestId) throws InterruptedException {
        if (!semaphore.tryAcquire(200, TimeUnit.MILLISECONDS)) {
            System.out.println(requestId + " rejected — server is busy");
            return;
        }
        try {
            processRequest(requestId);
        } finally {
            semaphore.release();
        }
    }

    private void processRequest(String requestId) { /* ... */ }
}
```

---

## 4. Cross-thread Signalling

> "Vì semaphore không có owner, một thread có thể ra hiệu cho thread khác — điều lock không làm sạch được."

Đây là khác biệt cốt lõi với mutex. Semaphore khởi tạo **0 permit** hoạt động như một **tín hiệu**: người chờ block trên `acquire()` cho tới khi người khác gọi `release()`.

**Ẩn dụ:** bãi xe ban đầu **khóa cổng, 0 vé**. Khách tới đợi sẵn. Khi bác bảo vệ dọn xong, bác **phát 1 vé** (dù bác không phải khách) → khách đợi sẵn nhận vé và vào.

```java
import java.util.concurrent.Semaphore;

class StartupCoordinator {
    private final Semaphore ready = new Semaphore(0); // Bắt đầu 0 — người chờ sẽ block

    public void startWorker() {
        new Thread(() -> {
            initialize();
            ready.release(); // Báo: setup xong
        }).start();
    }

    public void waitForReady() throws InterruptedException {
        ready.acquire();     // Chặn tới khi release() được gọi
        System.out.println("Server is ready — accepting requests");
    }

    private void initialize() { /* ... */ }
}
```

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

`Semaphore` cũng được xây trên **AQS (AbstractQueuedSynchronizer)** — cùng nền với `ReentrantLock`. Khác biệt nằm ở **ý nghĩa của biến `state`**:

```
ReentrantLock: state = hold count (giữ khóa mấy lần), có exclusiveOwnerThread
Semaphore:     state = SỐ PERMIT còn lại,             KHÔNG có owner
```

### `acquire()` làm gì?

```
Lặp lại:
  1. Đọc state hiện tại (số permit còn).
  2. Nếu state == 0 → không còn vé → vào hàng đợi FIFO, park() (ngủ).
  3. Nếu state > 0 → thử CAS: state → state - 1.
     - Thành công → lấy được vé, đi tiếp.
     - Thất bại (thread khác vừa cướp) → quay lại bước 1.
```

### `release()` làm gì?

```
1. CAS: state → state + 1 (trả vé lại).
2. unpark() thread kế tiếp trong hàng đợi → nó thức dậy thử acquire lại.
```

### Vì sao "không có owner"?

Để ý: trong `acquire()`/`release()` ở trên **không hề ghi lại thread nào đang giữ vé**. AQS của Semaphore dùng chế độ **shared** (chia sẻ) và **không** đụng tới `exclusiveOwnerThread`.

Hệ quả trực tiếp:
- **Bất kỳ thread nào cũng release() được** → dùng được cho signalling (thread A acquire, thread B release).
- **Không reentrant** → cùng một thread gọi `acquire()` hai lần sẽ tiêu 2 vé; nếu chỉ có 1 vé, nó **tự chặn chính mình**.
- **Nguy hiểm nếu dùng như lock** → không có gì ngăn release nhầm, dễ "over-count" số permit.

Đối chiếu: `ReentrantLock` luôn kiểm tra `exclusiveOwnerThread == currentThread` trước khi cho unlock, nên an toàn hơn cho mục đích khóa.

### Fair vs Unfair

Giống `ReentrantLock`: **fair** kiểm tra `hasQueuedPredecessors()` (có ai chờ trước không) trước khi giành vé; **unfair** thử CAS chen ngang ngay.

---

## Summary Table

| Feature | `synchronized` / `ReentrantLock` | `Semaphore` |
|---------|----------------------------------|-------------|
| Max concurrent threads | 1 | N (configurable) |
| Ownership | Yes — chỉ acquirer mới release | **No** — thread nào cũng release được |
| Reentrancy | Yes (ReentrantLock) | No |
| Cross-thread signal | No | Yes (start 0, release để đánh thức) |
| Chống race condition | ✅ (bản chất là 1) | ✅ chỉ khi N=1; ❌ khi N>1 |
| Nền tảng | Monitor / AQS (exclusive) | AQS (shared, state = permit) |

| Method | Behaviour |
|--------|-----------|
| `acquire()` | Chặn tới khi có permit; giảm count |
| `acquire(n)` | Chặn tới khi có n permit |
| `release()` | Tăng count; đánh thức một thread đang chờ |
| `tryAcquire()` | Trả về ngay — true nếu có permit |
| `tryAcquire(t, unit)` | Chờ tối đa `t`; false nếu hết giờ |
| `availablePermits()` | Số permit hiện tại (xấp xỉ — đừng dùng để rẽ nhánh) |
| `drainPermits()` | Lấy hết permit còn lại một cách atomic |

> Dùng `Semaphore` khi cần **giới hạn số thread đồng thời** — connection pool, rate limiter, resource quota. Cho **mutual exclusion / chống race condition**, ưu tiên `ReentrantLock` hoặc `synchronized` vì chúng track ownership và khó dùng sai hơn.
