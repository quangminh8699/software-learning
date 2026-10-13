## WHAT IS DEADLOCK ?

A **deadlock** is a state where two or more threads are **blocked forever**, each waiting for a resource (lock) that another thread in the group is holding. Không ai chịu nhả khóa trước, không ai đi tiếp được — cả nhóm **đứng hình vĩnh viễn**.

Khác với [Race Condition] (chạy *sai* kết quả), deadlock làm chương trình **treo đơ, không sai cũng không đúng** — nó đơn giản là **không bao giờ xong**.

```
Kịch bản kinh điển — vòng chờ (circular wait):
  Thread A: đang giữ Lock 1  →  đang chờ Lock 2
  Thread B: đang giữ Lock 2  →  đang chờ Lock 1
  → A chờ B, B chờ A → treo mãi mãi
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "Cứ dùng lock cẩn thận là hết deadlock." **CHƯA ĐỦ.** Deadlock không đến từ *một* khóa dùng sai, mà từ **thứ tự nhiều khóa được giành nhau**. Bạn có thể viết mỗi hàm hoàn toàn đúng mà ghép lại vẫn deadlock. Xem "4 điều kiện Coffman" bên dưới.

---

## 🚗 VÍ DỤ ĐỜI THƯỜNG — Hai xe trong con hẻm một chiều

Một con hẻm **chỉ đủ 1 xe đi**. Hai đầu hẻm có hai xe cùng lúc tiến vào:

- **Xe A** đi được nửa hẻm → gặp **Xe B** đi ngược tới, cũng nửa hẻm.
- Xe A nghĩ: "B lùi đi thì tôi mới qua được." → **đứng đợi**.
- Xe B nghĩ: "A lùi đi thì tôi mới qua được." → **cũng đứng đợi**.

Không ai chịu lùi → **kẹt cứng vô thời hạn**. Còi bấm cỡ nào cũng vô ích, vì mỗi tài xế đều đang "đợi người kia làm trước".

👉 Deadlock y hệt vậy: mỗi thread **giữ một thứ người kia cần**, và **chờ một thứ người kia đang giữ**. Không có ai phá vòng chờ → treo mãi.

**Cách đời thường "phá deadlock":**
- **Quy ước ai vào hẻm trước** (lock ordering — luôn giành khóa theo một thứ tự cố định).
- **"Đợi 30 giây không được thì lùi ra"** ([`tryLock` với timeout] trong ReentrantLock).
- **Có cảnh sát giao thông điều phối** (một khóa tổng duy nhất).

---

## 🔒 BỐN ĐIỀU KIỆN COFFMAN — Deadlock chỉ xảy ra khi có ĐỦ CẢ 4

Đây là phần quan trọng nhất. Deadlock **cần đủ đồng thời 4 điều kiện**. **Phá bỏ chỉ 1 trong 4** → deadlock không thể xảy ra. Đây chính là kim chỉ nam để phòng chống.

| # | Điều kiện | Nghĩa là | Trong ví dụ con hẻm |
|---|---|---|---|
| 1 | **Mutual Exclusion** | Tài nguyên chỉ 1 người giữ tại một lúc | Hẻm chỉ đủ 1 xe |
| 2 | **Hold and Wait** | Đang giữ 1 khóa mà vẫn đòi thêm khóa khác | Xe giữ nửa hẻm, còn đòi nửa kia |
| 3 | **No Preemption** | Không ai cướp khóa của người khác được | Không thể ép xe kia lùi |
| 4 | **Circular Wait** | A chờ B, B chờ A (thành vòng tròn) | Hai xe chờ vòng nhau |

> 💡 **Phá deadlock = phá ít nhất 1 điều kiện.** Thực tế dễ phá nhất là **#4 Circular Wait** (bằng lock ordering) và **#2 Hold-and-Wait** (bằng tryLock/timeout).

---

## 💥 TÁI HIỆN DEADLOCK BẰNG CODE

Ví dụ kinh điển: chuyển tiền giữa 2 tài khoản, mỗi tài khoản một khóa. Hai thread chuyển **ngược chiều nhau** → giành khóa **ngược thứ tự** → deadlock.

**Violation — hai thread giành khóa ngược thứ tự:**
```java
class Account {
    final Object lock = new Object();
    double balance;
}

class BankService {
    // Thread 1 gọi transfer(A, B): giành lock A → rồi lock B
    // Thread 2 gọi transfer(B, A): giành lock B → rồi lock A
    void transfer(Account from, Account to, double amount) {
        synchronized (from.lock) {              // (1) giữ khóa `from`
            // ...một khoảng thời gian ngắn...
            synchronized (to.lock) {            // (2) đòi thêm khóa `to`
                from.balance -= amount;
                to.balance   += amount;
            }
        }
    }
}
// transfer(A, B) giữ A, chờ B  |  transfer(B, A) giữ B, chờ A  → DEADLOCK
```

**Correct — Lock Ordering (phá Circular Wait #4):** luôn giành khóa theo **một thứ tự toàn cục cố định** (ví dụ theo `id` tăng dần), bất kể chiều chuyển tiền.
```java
class Account {
    final int id;                 // định danh để so sánh thứ tự
    final Object lock = new Object();
    double balance;
    Account(int id) { this.id = id; }
}

class BankService {
    void transfer(Account from, Account to, double amount) {
        // LUÔN khóa tài khoản có id nhỏ trước → mọi thread cùng một thứ tự → không thể có vòng chờ
        Account first  = from.id < to.id ? from : to;
        Account second = from.id < to.id ? to   : from;

        synchronized (first.lock) {
            synchronized (second.lock) {
                from.balance -= amount;
                to.balance   += amount;
            }
        }
    }
}
```

**Correct — tryLock + timeout (phá Hold-and-Wait #2):** nếu không lấy đủ cả 2 khóa trong thời hạn thì **nhả hết ra**, đợi rồi thử lại — không "giữ khư khư mà chờ".
```java
// Xem chi tiết pattern này trong bài [ReentrantLock — mục tryLock]
if (lockA.tryLock(100, MILLISECONDS)) {
    try {
        if (lockB.tryLock(100, MILLISECONDS)) {
            try { /* an toàn — có cả 2 khóa */ }
            finally { lockB.unlock(); }
        }
        // không lấy được lockB → thoát, NHẢ lockA → không giữ-và-chờ
    } finally { lockA.unlock(); }
}
```

---

## 🧭 PHÁT HIỆN DEADLOCK — Đọc thread dump

Khi app treo, chụp **thread dump** để tìm deadlock. JVM tự phát hiện và báo thẳng:

```bash
jstack <pid>          # in stack trace mọi thread
# hoặc: jcmd <pid> Thread.print
```

Trong output, tìm dòng:
```
Found one Java-level deadlock:
=============================
"Thread-1":
  waiting to lock monitor 0x... (object 0x..., a Account),
  which is held by "Thread-2"
"Thread-2":
  waiting to lock monitor 0x... (object 0x..., a Account),
  which is held by "Thread-1"
```

> 📌 JVM tự dò được deadlock kiểu "khóa vòng nhau" trên monitor và `ReentrantLock`. Nhưng nó **không** dò được thread treo vì lý do khác (chờ I/O, chờ `Condition`, livelock) — những cái đó phải tự đọc stack.

---

## ⚠️ HỌ HÀNG CỦA DEADLOCK — Livelock & Starvation

Deadlock không phải kiểu "kẹt" duy nhất:

| Vấn đề | Mô tả | Ẩn dụ |
|---|---|---|
| **Deadlock** | Các thread **đứng im**, chờ nhau vĩnh viễn | Hai xe đứng chôn chân trong hẻm |
| **Livelock** | Các thread **vẫn chạy** nhưng cứ nhường nhau nên không ai tiến được | Hai người trong hành lang cứ né cùng một bên, mãi không qua được nhau |
| **Starvation** | Một thread **không bao giờ** tới lượt vì thread khác luôn được ưu tiên | Người hiền lành mãi không chen được vào hàng, luôn bị người khác cắt ngang |

> Livelock nguy hiểm hơn deadlock ở chỗ CPU **vẫn bận 100%** (đang "chạy") nên nhìn qua tưởng bình thường. Starvation liên quan tới **fairness** — xem [Fair vs Unfair trong bài Types of Locks / ReentrantLock].

---

## 🛡️ CHIẾN LƯỢC PHÒNG CHỐNG — Tóm tắt

| Chiến lược | Phá điều kiện nào | Cách làm |
|---|---|---|
| **Lock Ordering** | #4 Circular Wait | Luôn giành nhiều khóa theo **một thứ tự cố định** (theo id, hashCode...) |
| **tryLock + timeout** | #2 Hold-and-Wait | Không lấy đủ khóa thì **nhả hết**, đợi rồi retry |
| **Gộp về 1 khóa** | #4 Circular Wait | Dùng **một khóa tổng** thay vì nhiều khóa nhỏ (đổi lại: giảm song song) |
| **Giảm phạm vi khóa** | #2 Hold-and-Wait | Giữ khóa **ngắn nhất có thể**, không gọi hàm lạ khi đang giữ khóa |
| **Dùng cấu trúc lock-free** | #1 Mutual Exclusion | `AtomicInteger`, `ConcurrentHashMap`... không cần khóa |

> ⚠️ **Nguyên tắc vàng:** **KHÔNG bao giờ gọi một method "lạ" (callback, method của class khác, code override) trong khi đang giữ khóa** — bạn không biết nó có đi giành khóa khác không → dễ tạo circular wait ngoài ý muốn.

---

## Summary

```
Deadlock = ≥2 thread chờ nhau vĩnh viễn, mỗi thread giữ khóa mà thread kia cần

CẦN ĐỦ CẢ 4 điều kiện Coffman:
  1. Mutual Exclusion   — khóa chỉ 1 người giữ
  2. Hold and Wait      — giữ khóa này, đòi thêm khóa khác
  3. No Preemption      — không cướp khóa được
  4. Circular Wait      — A→B→A thành vòng

PHÁ 1 trong 4 là hết deadlock. Thực tế hay dùng:
  → Lock Ordering        (phá #4) — giành khóa theo thứ tự cố định
  → tryLock + timeout    (phá #2) — không được thì nhả hết, retry

Phát hiện: jstack / jcmd Thread.print → "Found one Java-level deadlock"

Họ hàng: Livelock (chạy nhưng không tiến) · Starvation (mãi không tới lượt)
```

> Deadlock im lặng và khó tái hiện — thường chỉ treo khi timing "trùng đúng khoảnh khắc", giống [Race Condition]. Cách phòng đáng tin cậy nhất là **thiết kế thứ tự khóa nhất quán** ngay từ đầu, thay vì đi gỡ deadlock sau khi nó xảy ra.
