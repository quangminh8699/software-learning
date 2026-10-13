## WHAT IS RACE CONDITION ?

A **race condition** occurs when two or more threads concurrently access and modify shared mutable data, and the final result depends unpredictably on thread scheduling and timing. Because threads "race" each other without coordination, the outcome is corrupted data, lost updates, or inconsistent state.

```
Two classic patterns:
1. Read-Modify-Write  — read, change, write back without atomicity
2. Check-Then-Act     — check a condition, but state changes before you act
```

> ⚠️ Race condition là **gốc rễ** của mọi bài học lock. `synchronized`, `ReentrantLock`, `Semaphore` sinh ra chỉ để giải quyết vấn đề này.

---

## 🏦 VÍ DỤ ĐỜI THƯỜNG — Hai vợ chồng cùng rút tiền

Vợ chồng dùng **chung một tài khoản** có **1.000.000đ**. Không hẹn mà cùng một giây:

- **Vợ** ra ATM quận 1 → máy hiện số dư **1.000.000đ** → bấm rút **800.000đ**.
- **Chồng** ra ATM quận 7 (cùng lúc) → máy cũng hiện **1.000.000đ** (vì vợ chưa rút xong) → cũng rút **800.000đ**.

Cả hai máy đều nghĩ "còn đủ tiền", nên **cả hai cùng nhả tiền** → rút ra **1.600.000đ** trong khi chỉ có **1.000.000đ**.

👉 Lỗi không nằm ở vợ hay chồng — mỗi người làm đúng. Lỗi nằm ở chỗ **hai người cùng nhìn, cùng làm mà không ai xếp hàng**.

**Vì sao race condition khó bắt?** Nếu vợ chồng rút cách nhau 5 phút thì chẳng bao giờ sai. Chỉ khi **trùng đúng khoảnh khắc** mới lỗi → code chạy 100 lần đúng cả 100, lần 101 mới sai.

---

## 1. Read-Modify-Write

> "Nhiều thread cùng đọc một giá trị, sửa, rồi ghi lại — không có phối hợp."

Ví dụ kinh điển là đếm số. `i++` trông như 1 bước nhưng thực ra là **3 bước**: đọc → cộng 1 → ghi. Nếu 2 thread xen kẽ giữa các bước, một lần cập nhật bị **âm thầm mất đi** (lost update).

**Violation:**
```java
class Counter {
    private int count = 0;

    public void increment() {
        count++; // NOT atomic: read → add 1 → write (3 ops riêng biệt)
    }

    public int getCount() { return count; }
}

// Hai thread mỗi thread gọi increment() 1000 lần
// Mong đợi: 2000 — Thực tế: ít hơn khó đoán (vd 1743)
```

**Correct (synchronized):**
```java
class Counter {
    private int count = 0;

    public synchronized void increment() {
        count++; // Chỉ 1 thread vào tại một thời điểm
    }

    public synchronized int getCount() { return count; }
}
```

**Correct (AtomicInteger — ưu tiên cho biến đếm đơn giản):**
```java
import java.util.concurrent.atomic.AtomicInteger;

class Counter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet(); // Atomic ở tầng phần cứng — không cần khóa
    }

    public int getCount() { return count.get(); }
}
```

---

## 2. Check-Then-Act

> "Một thread kiểm tra điều kiện rồi hành động — nhưng thread khác đổi điều kiện ở giữa."

Khoảng trống giữa lúc *check* và lúc *act* là vùng nguy hiểm. Cả hai thread cùng vượt qua bước check, rồi cùng act → tạo ra kết quả trùng lặp hoặc hỏng.

**Violation:**
```java
class InventoryService {
    private int stock = 1;

    public void purchase(String user) {
        if (stock > 0) {          // Thread A check: stock=1, pass
                                  // Thread B check: stock=1, pass (race ở đây)
            stock--;              // Cả hai cùng trừ → stock = -1
            System.out.println(user + " purchased successfully");
        } else {
            System.out.println(user + " failed — out of stock");
        }
    }
}
```

**Correct:**
```java
class InventoryService {
    private int stock = 1;

    public synchronized void purchase(String user) {
        if (stock > 0) {    // Check + act giờ là atomic — không thread nào chen vào
            stock--;
            System.out.println(user + " purchased successfully");
        } else {
            System.out.println(user + " failed — out of stock");
        }
    }
}
```

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

Race condition thực chất đến từ **hai vấn đề tách biệt** trong Java Memory Model (JMM):

### Vấn đề 1 — Atomicity (tính không thể chia cắt)

Dòng `count++` khi compile ra Java bytecode trở thành **4 lệnh riêng biệt**:

```
getfield count   // đọc count từ bộ nhớ lên stack
iconst_1         // đẩy số 1 lên stack
iadd             // cộng
putfield count   // ghi kết quả ngược lại
```

Bộ lập lịch (scheduler) của HĐH có thể **cắt ngang giữa 4 lệnh này bất cứ lúc nào** để nhường CPU cho thread khác. Hai thread xen kẽ:

```
Thread A: getfield count = 5
Thread B: getfield count = 5   ← đọc trước khi A kịp ghi
Thread A: iadd → 6, putfield 6
Thread B: iadd → 6, putfield 6  ← đè lên, mất 1 lần cộng
```

Đáng lẽ 7, kết quả là 6.

### Vấn đề 2 — Visibility (khả năng nhìn thấy)

Mỗi CPU core có **cache riêng**. Khi thread A sửa `count`, giá trị mới có thể **nằm trong cache của core A**, chưa đẩy xuống RAM chung. Thread B chạy ở core khác có thể vẫn đọc **giá trị cũ** từ cache của nó → không "nhìn thấy" thay đổi của A.

> 📌 Đây là lý do `volatile` một mình **không** sửa được race condition: `volatile` chỉ giải quyết *visibility* (đảm bảo đọc/ghi thẳng RAM), nhưng **không** làm cho `count++` trở nên atomic.

### Vì sao lock / Atomic sửa được?

- **`synchronized` / lock**: ép "chỉ 1 thread một lúc" (giải quyết atomicity), đồng thời lúc vào/ra monitor sẽ *flush cache* (giải quyết visibility). Sửa cả hai vấn đề.
- **`AtomicInteger`**: dùng lệnh CPU đặc biệt tên **CAS (Compare-And-Swap)** — gộp "đọc → so sánh → ghi" thành **1 lệnh phần cứng không thể chia cắt**, nên không cần khóa mà vẫn đúng.

---

## Summary Table

| Pattern | Root Cause | Symptom | Fix |
|---------|-----------|---------|-----|
| **Read-Modify-Write** | Non-atomic compound op | Lost updates, wrong count | `synchronized`, `AtomicInteger` |
| **Check-Then-Act** | Gap between check and action | Duplicate action, negative stock | `synchronized`, `ReentrantLock` |

| Tool | When to use |
|------|------------|
| `synchronized` | Mutual exclusion đơn giản trên method/block |
| `AtomicInteger` / `AtomicReference` | Đếm/tham chiếu 1 biến — lock-free (CAS) |
| `ReentrantLock` | Cần try-lock, timed lock, hoặc nhiều condition |
| `volatile` | **Chỉ** visibility — KHÔNG sửa được race trên compound op |

> Race condition rất "im lặng" — code compile được và thường pass test đơn luồng. Bước 1 luôn là **xác định shared mutable state**; bước 2 là đảm bảo mọi truy cập vào nó đều **atomic** và **visible** giữa các thread.
