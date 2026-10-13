## WHAT IS REENTRANTLOCK ?

`ReentrantLock` is an explicit, feature-rich **mutex** from `java.util.concurrent.locks`. It provides the same mutual-exclusion guarantee as `synchronized` but adds critical capabilities: **try-lock**, **timed lock**, **interruptible lock**, **fair ordering**, and **multiple condition queues**.

The name *reentrant* means the same thread can acquire the lock multiple times without deadlocking itself — an internal **hold count** tracks each acquisition and must be matched by an equal number of releases.

```
Three locking modes:
1. lock()               — block indefinitely until acquired (like synchronized)
2. tryLock()            — attempt without blocking; return false if unavailable
3. lockInterruptibly()  — block, but wake up if the thread is interrupted
```

---

## 🔑 VÍ DỤ ĐỜI THƯỜNG — Nhà vệ sinh khóa điện tử "thông minh"

Vẫn là nhà vệ sinh 1 buồng (chỉ 1 người vào 1 lúc, giống `synchronized`), nhưng đổi sang **khóa điện tử có màn hình**. Vẫn mutual exclusion, nhưng thêm nhiều lựa chọn:

| Tình huống đời thường | Tên kỹ thuật |
|---|---|
| "Có người trong đó à? Thôi tôi **đi chỗ khác luôn**, không đợi." | `tryLock()` |
| "Tôi **đợi tối đa 5 phút** thôi, quá thì đi." | `tryLock(5, MINUTES)` |
| "Đang đợi mà **có điện thoại khẩn** → bỏ đợi nghe điện." | `lockInterruptibly()` |
| "Phải **xếp hàng đúng thứ tự**, cấm chen ngang." | `new ReentrantLock(true)` — fair |

**Cái giá phải trả:** với khóa điện tử này bạn **phải tự bấm nút mở khóa khi ra**. Quên (hoặc lỗi giữa chừng) → cửa **khóa vĩnh viễn**, không ai dùng được. Vì thế code luôn phải `unlock()` trong khối `finally`.

👉 So sánh: `synchronized` = **chìa cơ, tự động, đơn giản**. `ReentrantLock` = **khóa điện tử, nhiều tính năng, nhưng phải tự nhớ mở**.

> Quy tắc: **mặc định dùng `synchronized`**. Chỉ đổi sang `ReentrantLock` khi thực sự cần mấy tính năng ở bảng trên.

---

## 1. Basic lock / unlock

> "Luôn nhả trong `finally` — nếu có exception giữa lock và unlock, khóa bị giữ mãi mãi."

Hợp đồng lock/unlock là **thủ công**. Quên `unlock()` ở nhánh lỗi là bug phổ biến nhất của `ReentrantLock` — `finally` xóa bỏ rủi ro này.

**Violation (không có finally):**
```java
class Counter {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;

    public void increment() {
        lock.lock();
        count++;            // Nếu ném exception ở đây...
        lock.unlock();      // ...dòng này không bao giờ chạy → deadlock vĩnh viễn
    }
}
```

**Correct:**
```java
import java.util.concurrent.locks.ReentrantLock;

class Counter {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock(); // Đảm bảo chạy kể cả khi count++ ném exception
        }
    }
}
```

---

## 2. tryLock — Non-blocking Attempt

> "Thử lấy; nếu không được thì làm việc khác thay vì đợi mãi."

`tryLock()` trả `true` nếu lấy được, `false` ngay lập tức nếu không. Bản overload `tryLock(timeout, unit)` chờ tối đa một khoảng thời gian.

**Use-case — tránh deadlock giữa hai resource:**
```java
class TransferService {
    private final ReentrantLock lockA = new ReentrantLock();
    private final ReentrantLock lockB = new ReentrantLock();

    public boolean transfer(double amount) {
        try {
            if (lockA.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    if (lockB.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            performTransfer(amount); // Giữ cả 2 khóa — an toàn
                            return true;
                        } finally { lockB.unlock(); }
                    }
                } finally { lockA.unlock(); }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false; // Không lấy được cả hai — caller có thể retry
    }

    private void performTransfer(double amount) { /* ... */ }
}
```

---

## 3. lockInterruptibly — Cancellable Wait

> "Cho phép hủy một thread đang chờ qua `Thread.interrupt()` — điều `synchronized` không làm được."

Thread chặn trên `lock()` **bỏ qua** interrupt. Thread chặn trên `lockInterruptibly()` sẽ **ném `InterruptedException`** khi bị interrupt → cho phép shutdown mượt hoặc hủy task.

```java
class Worker implements Runnable {
    private final ReentrantLock lock;
    Worker(ReentrantLock lock) { this.lock = lock; }

    @Override
    public void run() {
        try {
            lock.lockInterruptibly();   // Đánh thức nếu bị interrupt lúc đang chờ
            try {
                doWork();
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was cancelled");
            Thread.currentThread().interrupt(); // Khôi phục interrupt status
        }
    }

    private void doWork() { /* ... */ }
}
```

---

## 4. Fair vs Unfair Lock

> "Fair trao khóa cho thread chờ lâu nhất; unfair cho phép chen hàng — nhanh hơn nhưng có thể starve."

- **Unfair (mặc định)** `new ReentrantLock()` — JVM có thể để thread mới đến "chen ngang", tăng throughput.
- **Fair** `new ReentrantLock(true)` — phục vụ theo thứ tự FIFO, chống starvation, nhưng chậm hơn.

```java
ReentrantLock unfair = new ReentrantLock();      // throughput cao, có thể starve
ReentrantLock fair   = new ReentrantLock(true);  // FIFO, throughput thấp hơn
```

> Rule of thumb: chỉ dùng fair khi starvation là mối lo thật sự. Phí performance không hề nhỏ.

---

## 5. Condition Variables — Nhiều "phòng chờ"

> "Nhiều wait-queue trên cùng một khóa — thread chờ những điều kiện khác nhau một cách độc lập."

`synchronized` chỉ có **một** wait-set (`wait()` / `notifyAll()`). `ReentrantLock` tạo được **nhiều** `Condition`, mỗi cái một hàng đợi riêng → không đánh thức nhầm thread chưa thể chạy.

**Ẩn dụ:** Producer-Consumer như một quán ăn có kho. Người **sản xuất** chờ ở phòng "hết chỗ trống", người **tiêu thụ** chờ ở phòng "hết hàng" — đánh thức đúng người cần, không làm phiền người khác.

```java
import java.util.concurrent.locks.*;

class BoundedBuffer<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull  = lock.newCondition(); // Producer chờ đây
    private final Condition notEmpty = lock.newCondition(); // Consumer chờ đây

    private final Object[] items;
    private int count, putIdx, takeIdx;

    BoundedBuffer(int capacity) { items = new Object[capacity]; }

    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length) notFull.await(); // Chờ tới khi có chỗ trống
            items[putIdx] = item;
            putIdx = (putIdx + 1) % items.length;
            count++;
            notEmpty.signal(); // Đánh thức một consumer
        } finally { lock.unlock(); }
    }

    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) notEmpty.await(); // Chờ tới khi có hàng
            T item = (T) items[takeIdx];
            takeIdx = (takeIdx + 1) % items.length;
            count--;
            notFull.signal(); // Đánh thức một producer
            return item;
        } finally { lock.unlock(); }
    }
}
```

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

`ReentrantLock` không dùng monitor như `synchronized`. Nó được xây trên một lớp nền tảng tên **AQS (AbstractQueuedSynchronizer)** — cũng là nền của `Semaphore`, `CountDownLatch`...

### AQS gồm 2 phần

```
1. Một biến int `state`  → chính là HOLD COUNT (giữ khóa mấy lần)
2. Một hàng đợi FIFO      → các thread đang chờ, được "park" (ngủ) trong đó
```

Ngoài ra `ReentrantLock` còn lưu **`exclusiveOwnerThread`** — thread nào đang giữ khóa (để biết reentrancy và cấm thread khác unlock).

### `lock()` làm gì?

```
1. Thử CAS state: 0 → 1.
   - Thành công → tôi là owner. Xong.
2. Nếu thất bại nhưng owner đúng là tôi (đã giữ rồi):
   - state++ (reentrancy). Xong, không chặn.
3. Nếu owner là thread khác:
   - Đưa tôi vào hàng đợi FIFO, rồi LockSupport.park() → tôi "ngủ".
```

> CAS = Compare-And-Swap: lệnh CPU nguyên tử "nếu state đang là X thì đổi thành Y", dùng để giành khóa mà không cần khóa khác.

### `unlock()` làm gì?

```
1. state-- (giảm hold count).
2. Nếu state về 0:
   - Xóa owner, rồi LockSupport.unpark() thread kế tiếp trong hàng đợi → nó thức dậy giành khóa.
3. Nếu state > 0:
   - Vẫn đang giữ (do reentrancy), chưa nhả.
```

Đây chính là lý do bạn phải `unlock()` đủ số lần đã `lock()` — state phải về 0 khóa mới thực sự nhả.

### Fair vs Unfair khác nhau ở đâu trong code?

- **Unfair:** khi `lock()`, thử CAS state ngay lập tức (chen ngang), bất kể hàng đợi có ai. Nếu trúng thì lấy luôn.
- **Fair:** trước khi CAS, kiểm tra `hasQueuedPredecessors()` — "có ai đứng chờ trước tôi không?". Có thì tự vào cuối hàng, không chen.

### Condition hoạt động thế nào?

Mỗi `Condition` có **hàng đợi riêng** (condition queue). Khi gọi `await()`:
```
1. Thread nhả khóa (state về 0) và rời hàng đợi chính.
2. Nó được đưa sang condition queue riêng, rồi ngủ.
3. Khi thread khác gọi signal(), thread được chuyển NGƯỢC từ condition queue
   về hàng đợi chính để chờ giành lại khóa.
```

Vì thế `notFull` và `notEmpty` là hai hàng đợi tách biệt — `signal()` trên cái này không đụng cái kia.

---

## Summary Table

| Feature | `synchronized` | `ReentrantLock` |
|---------|---------------|----------------|
| Syntax | Keyword built-in | Thủ công `lock()` / `unlock()` |
| Try-lock | No | `tryLock()` / `tryLock(timeout, unit)` |
| Interruptible wait | No | `lockInterruptibly()` |
| Fairness control | No | `new ReentrantLock(true)` |
| Multiple conditions | No (1 wait-set) | `lock.newCondition()` — không giới hạn |
| Forget to unlock | Impossible | Possible — luôn dùng `finally` |
| Nền tảng | Object monitor | AQS (state + FIFO queue) |

| Method | Behaviour |
|--------|-----------|
| `lock()` | Chặn tới khi lấy được |
| `tryLock()` | Trả về ngay — true nếu lấy được |
| `tryLock(t, unit)` | Chờ tối đa `t`; false nếu hết giờ |
| `lockInterruptibly()` | Chặn, nhưng ném `InterruptedException` khi bị interrupt |
| `unlock()` | Nhả; giảm hold count |
| `newCondition()` | Tạo một wait-queue mới trên khóa này |

> Dùng `ReentrantLock` khi cần timeout, interruption, fairness, hoặc nhiều condition queue. Cho mutual exclusion đơn giản, `synchronized` ngắn hơn và đúng y hệt.
