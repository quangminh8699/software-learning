## WHAT ARE THE TYPES OF LOCKS ?

"Lock" không phải một thứ duy nhất — nó là **cả một họ công cụ đồng bộ**, mỗi loại là một **đánh đổi khác nhau**. Bài này là **bản đồ tổng quan** để phân loại, còn chi tiết từng công cụ cụ thể xem các bài riêng: [Synchronized Lock], [ReentrantLock], [Semaphore].

Cách hiểu tốt nhất: lock được phân loại theo **nhiều trục độc lập** — một lock thực tế là **tổ hợp** của các trục đó. Ví dụ `ReentrantLock` = *pessimistic + mutex + blocking + reentrant + (fair hoặc unfair tùy chọn)*.

```
Các trục phân loại chính:
1. Pessimistic  ⟷  Optimistic     (giả định có tranh chấp hay không)
2. Mutex (1)    ⟷  Semaphore (N)   (cho mấy người vào cùng lúc)
3. Read         ⟷  Write           (chia sẻ đọc, độc quyền ghi)
4. Blocking     ⟷  Spinlock        (chờ bằng cách ngủ hay quay vòng)
5. Fair         ⟷  Unfair          (xếp hàng FIFO hay cho chen)
6. Reentrant    ⟷  Non-reentrant   (vào lại được hay tự khóa mình)
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "Các loại lock là các lựa chọn loại trừ nhau, chọn 1 cái." **SAI.** Chúng là các **thuộc tính chồng lên nhau**. Một lock luôn có giá trị trên *mỗi* trục cùng lúc.

---

## 🅿️ VÍ DỤ ĐỜI THƯỜNG — Các kiểu "giữ chỗ" khác nhau

Mỗi trục phân loại giống một cách "giữ chỗ" trong đời thực:

| Trục | Ẩn dụ |
|---|---|
| **Pessimistic** | Khóa cửa lại rồi mới làm — chắc chắn không ai vào |
| **Optimistic** | Cứ làm, xong mới kiểm tra "vừa nãy có ai đụng không?" — nếu có thì làm lại |
| **Mutex vs Semaphore** | Nhà vệ sinh 1 buồng (1 chìa) ⟷ bãi xe N chỗ (N vé) |
| **Read vs Write** | Nhiều người **cùng đọc** một tấm bảng thông báo được; nhưng khi **sửa bảng** thì đuổi hết ra |
| **Blocking vs Spinlock** | Đợi thì **ngồi ngủ**, ai gọi mới dậy ⟷ **đứng đó liên tục hỏi** "xong chưa? xong chưa?" |
| **Fair vs Unfair** | Xếp hàng đúng thứ tự ⟷ ai nhanh tay thì chen được |

---

## 1. Pessimistic ⟷ Optimistic Locking

> "Bi quan: khóa trước cho chắc. Lạc quan: cứ làm, đụng độ mới xử lý."

**Pessimistic (bi quan)** — *giả định sẽ có tranh chấp*, nên **khóa trước khi đụng dữ liệu**. Người khác phải đợi. Đây là kiểu của [`synchronized`], [`ReentrantLock`].
- ✅ An toàn tuyệt đối, đơn giản để suy luận.
- ❌ Tốn phí chờ; nếu thực ra ít tranh chấp thì lãng phí.

**Optimistic (lạc quan)** — *giả định hiếm khi tranh chấp*, nên **không khóa**; cứ đọc → tính → khi ghi mới **kiểm tra xem dữ liệu có bị đổi giữa chừng không**. Nếu bị đổi → **hủy và thử lại (retry)**.
- ✅ Không có chờ khóa → throughput cao khi ít tranh chấp.
- ❌ Tốn công retry khi tranh chấp nhiều; logic phức tạp hơn.

```java
// Optimistic — nền tảng là CAS (Compare-And-Swap)
AtomicInteger counter = new AtomicInteger(0);
int oldVal, newVal;
do {
    oldVal = counter.get();          // đọc, KHÔNG khóa
    newVal = oldVal + 1;             // tính toán
} while (!counter.compareAndSet(oldVal, newVal)); // ghi CHỈ KHI chưa ai đổi; sai thì lặp lại
```

> 📌 Optimistic locking là nền của `AtomicInteger`, `StampedLock.tryOptimisticRead()`, và cả **versioning trong database** (`WHERE version = ?`). Cốt lõi luôn là **CAS** — xem cơ chế CAS ở [Race Condition] và [ReentrantLock].

---

## 2. Mutex (Exclusive) ⟷ Semaphore (Counting)

> "Mutex: đúng 1 người. Semaphore: tối đa N người."

- **Mutex / Exclusive lock** — cho **đúng 1** thread vào critical section. Đây là mục đích của [`synchronized`] và [`ReentrantLock`]. Con số quan trọng là **1** → đây mới là thứ **chống được [Race Condition]**.
- **Semaphore** — cho tối đa **N** thread cùng lúc (rate limit, connection pool). Xem bài [Semaphore]. Lưu ý: `Semaphore(N>1)` **KHÔNG** chống race condition.

```java
ReentrantLock mutex = new ReentrantLock();  // exclusive: 1 người
Semaphore pool = new Semaphore(10);         // counting: tối đa 10 người
```

> 💡 Điểm khác biệt sâu: mutex có **ownership** (chỉ người khóa mới mở được); semaphore **không có owner** (thread nào cũng release được). Xem "cách hoạt động" trong bài [Semaphore].

---

## 3. Read Lock ⟷ Write Lock (ReadWriteLock)

> "Nhiều người đọc song song được; nhưng ghi thì phải độc quyền."

Với dữ liệu **đọc nhiều, ghi ít**, dùng mutex là phí — hai người chỉ *đọc* thì chẳng đụng nhau, sao phải xếp hàng? **`ReadWriteLock`** tách làm 2 khóa:

| | Read lock | Write lock |
|---|---|---|
| Nhiều thread cùng giữ? | ✅ Có (shared) | ❌ Không (exclusive) |
| Chặn ai? | Chỉ chặn writer | Chặn cả reader lẫn writer |

```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Cache {
    private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    private final Map<String, String> map = new HashMap<>();

    public String get(String key) {
        rw.readLock().lock();            // nhiều reader vào song song
        try { return map.get(key); }
        finally { rw.readLock().unlock(); }
    }

    public void put(String key, String value) {
        rw.writeLock().lock();           // writer độc quyền — đuổi hết reader
        try { map.put(key, value); }
        finally { rw.writeLock().unlock(); }
    }
}
```

> ⚠️ Nếu **ghi nhiều**, ReadWriteLock có thể **chậm hơn** mutex thường (overhead quản lý 2 khóa + reader có thể starve writer). Chỉ dùng khi tỉ lệ đọc **áp đảo** ghi. `StampedLock` (Java 8+) cải tiến thêm với **optimistic read** cho trường hợp đọc cực nhiều.

---

## 4. Blocking Lock ⟷ Spinlock

> "Chờ bằng cách ngủ (nhường CPU) hay đứng quay vòng liên tục?"

- **Blocking lock** — thread không lấy được khóa thì bị **park (ngủ)**, OS chuyển CPU cho việc khác; khi khóa rảnh mới **đánh thức**. Là cơ chế của [`synchronized`] (mức heavyweight) và [`ReentrantLock`].
  - ✅ Không phí CPU khi chờ lâu. ❌ Có chi phí *context switch* (đánh thức/ru ngủ).
- **Spinlock** — thread cứ **quay vòng hỏi liên tục** ("khóa rảnh chưa?") mà không ngủ.
  - ✅ Cực nhanh nếu critical section **rất ngắn** (khỏi tốn context switch). ❌ **Đốt CPU** nếu phải chờ lâu.

```java
// Spinlock đơn giản bằng CAS (minh họa — thực tế hiếm khi tự viết)
class SpinLock {
    private final AtomicBoolean locked = new AtomicBoolean(false);
    void lock()   { while (!locked.compareAndSet(false, true)) { /* spin: quay vòng chờ */ } }
    void unlock() { locked.set(false); }
}
```

> 📌 JVM thực ra **kết hợp cả hai**: khi tranh chấp nhẹ, `synchronized` **spin một chút trước** (adaptive spinning) rồi mới thật sự ngủ — xem "Lock Escalation" trong bài [Synchronized Lock].

---

## 5. Fair ⟷ Unfair Lock

> "Xếp hàng đúng thứ tự (chống starvation) hay cho chen ngang (nhanh hơn)?"

- **Fair** — trao khóa cho thread **chờ lâu nhất** (FIFO). Chống [starvation], nhưng chậm hơn.
- **Unfair (mặc định)** — cho thread mới đến **chen ngang** nếu khóa vừa rảnh → throughput cao hơn, nhưng thread xui có thể bị bỏ đói.

```java
ReentrantLock unfair = new ReentrantLock();      // mặc định — nhanh, có thể starve
ReentrantLock fair   = new ReentrantLock(true);  // FIFO — công bằng, chậm hơn
```

> Chi tiết cơ chế `hasQueuedPredecessors()` phân biệt fair/unfair: xem bài [ReentrantLock] và [Semaphore]. `synchronized` **luôn unfair** — không chọn được.

---

## 6. Reentrant ⟷ Non-reentrant

> "Cùng một thread có được vào lại khóa nó đang giữ không?"

- **Reentrant (tái nhập)** — thread đang giữ khóa **vào lại được** (hold count đếm số lần). Nếu không, một synchronized method gọi synchronized method khác trên cùng object sẽ **tự deadlock chính mình**. Cả [`synchronized`] và [`ReentrantLock`] đều reentrant.
- **Non-reentrant** — vào lại là **tự khóa mình**. `Semaphore(1)` là ví dụ: acquire 2 lần tiêu 2 vé, chỉ có 1 vé → tự chặn.

```java
class Printer {
    public synchronized void printPage() {
        printHeader();   // gọi lại synchronized method khác trên `this`
    }                    // AN TOÀN vì intrinsic lock reentrant — hold count chỉ tăng
    public synchronized void printHeader() { /* ... */ }
}
```

> Xem "Reentrant Behaviour" và cơ chế hold count trong bài [Synchronized Lock] / [ReentrantLock].

---

## 📊 BẢNG TỔNG HỢP — Định vị các công cụ trên các trục

| Công cụ | Pessimistic/Optimistic | Số người | Read/Write | Reentrant | Fair option |
|---|---|---|---|---|---|
| [`synchronized`] | Pessimistic | 1 (mutex) | Exclusive | ✅ | ❌ (luôn unfair) |
| [`ReentrantLock`] | Pessimistic | 1 (mutex) | Exclusive | ✅ | ✅ tùy chọn |
| `ReentrantReadWriteLock` | Pessimistic | N reader / 1 writer | Cả hai | ✅ | ✅ tùy chọn |
| `StampedLock` | Cả optimistic & pessimistic | N reader / 1 writer | Cả hai | ❌ | ❌ |
| [`Semaphore`] | Pessimistic | N (counting) | — | ❌ | ✅ tùy chọn |
| `AtomicInteger` (CAS) | **Optimistic** | lock-free | — | — | — |

---

## Summary

```
"Lock" = một họ công cụ, phân loại theo NHIỀU TRỤC độc lập (không loại trừ nhau):

1. Pessimistic ⟷ Optimistic   → khóa trước (synchronized) vs CAS + retry (Atomic)
2. Mutex (1)   ⟷ Semaphore (N) → độc quyền chống race vs giới hạn số lượng
3. Read        ⟷ Write         → ReadWriteLock: đọc song song, ghi độc quyền
4. Blocking    ⟷ Spinlock      → ngủ chờ vs quay vòng chờ (critical section ngắn)
5. Fair        ⟷ Unfair        → FIFO chống starvation vs cho chen (nhanh hơn)
6. Reentrant   ⟷ Non-reentrant → vào lại được vs tự khóa mình

Một lock thực tế = TỔ HỢP các trục:
  ReentrantLock = pessimistic + mutex + blocking + reentrant + (fair tùy chọn)
```

> Khi chọn lock, đừng hỏi "loại nào tốt nhất" mà hỏi **"dữ liệu của tôi có đặc điểm gì"**: đọc nhiều ghi ít → ReadWriteLock; chỉ đếm/giới hạn → Semaphore; tranh chấp hiếm → optimistic/Atomic; cần đúng 1 người sửa dữ liệu chung → mutex ([`synchronized`]/[`ReentrantLock`]). Chi tiết từng công cụ xem các bài riêng đã liên kết ở trên. Cẩn thận nhiều khóa cùng lúc → đọc [Deadlock].
