## WHAT IS SYNCHRONIZED LOCK ?

Java's `synchronized` keyword provides the simplest form of **mutual exclusion** — it uses every object's built-in **monitor lock** (also called *intrinsic lock*) to ensure only one thread executes a critical section at a time.

When a thread enters a `synchronized` block or method, it acquires the monitor of the target object. All other threads trying to acquire the same monitor are **blocked** until the first thread releases it (exits the block/method).

```
Two forms of synchronized:
1. synchronized method   — locks the entire method on `this` (or the Class for static)
2. synchronized block    — locks only the chosen section on any specified object
```

---

## 🚻 VÍ DỤ ĐỜI THƯỜNG — Nhà vệ sinh 1 buồng, 1 chìa khóa

Quán cà phê có **1 nhà vệ sinh, 1 chìa khóa** treo ở quầy.

- Ai muốn dùng → ra quầy **lấy chìa**.
- Đang có người bên trong → người sau **đứng đợi**, không cách nào vào được.
- Xong việc → ra ngoài **trả chìa** → người kế tiếp mới lấy được.

Áp vào chuyện ngân hàng (bài Race Condition): nếu ATM buộc phải "lấy chìa khóa tài khoản" trước khi rút, thì khi vợ đang rút, **chồng phải đứng đợi**. Vợ rút xong còn 200.000đ, chồng mới vào → thấy không đủ → bị từ chối. **Hết lỗi.**

👉 `synchronized` = **cây chìa khóa duy nhất** đó. Chỉ 1 người dùng tại một thời điểm, **tự động** lấy khi vào, **tự động** trả khi ra.

- ✅ **Điểm hay:** không cần nhớ trả chìa — Java tự mở khóa khi ra khỏi khối `synchronized` (kể cả khi có lỗi).
- ❌ **Điểm dở:** chìa rất "cứng đầu" — không thể nói "đợi 5 phút thôi", không thể bị gọi ra giữa chừng. Đợi là đợi vô thời hạn.

---

## 1. Synchronized Method

> "Đánh dấu cả method — khóa được lấy trên `this` khi vào, nhả khi ra."

Compiler bọc thân method trong một cặp monitor-enter / monitor-exit ngầm. Đơn giản để viết, nhưng khóa cả method dù chỉ một phần đụng shared state.

```java
class BankAccount {
    private double balance = 1000.0;

    public synchronized void withdraw(double amount) {
        if (balance >= amount) {    // Check + act là atomic — không thread nào chen vào
            balance -= amount;
            System.out.println("Withdrew " + amount + ", balance: " + balance);
        } else {
            System.out.println("Insufficient funds");
        }
    }

    public synchronized double getBalance() {
        return balance;             // Read cũng phải synchronized để đảm bảo visibility
    }
}
```

---

## 2. Synchronized Block

> "Chỉ khóa đúng đoạn code đụng shared state — thu nhỏ critical section."

Synchronized block cho bạn chọn chính xác **khóa vào object nào** và **những dòng nào** được bảo vệ. Giữ phần logic không liên quan nằm ngoài khóa → giảm tranh chấp (contention).

```java
class OrderProcessor {
    private final List<String> orders = new ArrayList<>();
    private int processedCount = 0;

    public void process(String order) {
        // Chuẩn bị tốn thời gian — chưa đụng shared state, không cần khóa
        String prepared = prepare(order);

        synchronized (this) {           // Chỉ khóa khi ghi shared state
            orders.add(prepared);
            processedCount++;
        }
    }

    private String prepare(String order) {
        return order.toUpperCase();     // Chạy song song giữa các thread
    }
}
```

**Khóa trên private object (best practice):**
```java
class Cache {
    private final Object lock = new Object(); // Khóa riêng — người ngoài không lỡ tay khóa nhầm
    private final Map<String, String> map = new HashMap<>();

    public void put(String key, String value) {
        synchronized (lock) { map.put(key, value); }
    }

    public String get(String key) {
        synchronized (lock) { return map.get(key); }
    }
}
```

---

## 3. Static Synchronized

> "Static synchronized khóa trên `Class` object — dùng chung cho mọi instance."

Method synchronized thường khóa trên `this` (instance). Method static synchronized khóa trên `ClassName.class`. **Hai khóa này độc lập** — không chặn nhau.

```java
class IdGenerator {
    private static int nextId = 0;

    // Khóa trên IdGenerator.class — an toàn dù có nhiều instance
    public static synchronized int generate() {
        return ++nextId;
    }
}
```

**Pitfall — trộn instance lock và static lock:**
```java
class Broken {
    private static int counter = 0;

    public synchronized void increment() {   // Khóa trên `this`
        counter++;                           // Nhưng counter là static!
    }

    public static synchronized void reset() { // Khóa trên Broken.class
        counter = 0;                          // Khóa khác → KHÔNG loại trừ lẫn nhau
    }
}
// increment() và reset() có thể chạy đồng thời → hỏng dữ liệu
```

---

## 4. Reentrant Behaviour

> "Thread đang giữ monitor có thể vào lại bất kỳ synchronized block nào cùng monitor đó."

Intrinsic lock của Java **reentrant** (tái nhập) từ thiết kế. Nếu không, một synchronized method gọi synchronized method khác trên `this` sẽ **tự khóa chính nó** (deadlock).

```java
class Printer {
    public synchronized void printPage(String content) {
        printHeader();           // Gọi synchronized method khác trên `this`
        System.out.println(content);
    }

    public synchronized void printHeader() {
        System.out.println("=== HEADER ===");
        // An toàn — cùng thread đã giữ khóa; hold count chỉ tăng lên
    }
}
```

---

## ⚙️ CÁCH HOẠT ĐỘNG BÊN DƯỚI

### Monitor nằm ở đâu?

Mỗi object trong Java có một phần ẩn gọi là **object header**, trong đó có **Mark Word** — vài byte lưu thông tin khóa (ai đang giữ, hold count...). "Chìa khóa" chính là thông tin nằm trong Mark Word này.

### Bytecode: monitorenter / monitorexit

Khi compile, `synchronized (obj) { ... }` biến thành 2 lệnh bytecode:

```
monitorenter    // giành monitor của obj (chặn nếu đã có thread khác giữ)
   ... code ...
monitorexit     // nhả monitor
```

Với `synchronized method`, không có 2 lệnh này mà method được đánh cờ `ACC_SYNCHRONIZED` — JVM tự động enter/exit monitor khi vào/ra method. Đây là lý do bạn **không bao giờ quên "trả chìa"**: JVM lo việc đó, kể cả khi method ném exception.

### Reentrancy hoạt động thế nào?

Monitor lưu 2 thứ: **owner thread** (thread nào đang giữ) và **hold count** (giữ mấy lần).

- Thread vào lần 1 → owner = nó, hold count = 1.
- Cùng thread vào lần nữa → thấy owner đúng là mình → hold count = 2 (không chặn).
- Mỗi lần ra → hold count giảm 1. Về **0** thì mới thực sự nhả khóa.

### Lock Escalation — JVM tối ưu ngầm

`synchronized` không phải lúc nào cũng "nặng". JVM nâng cấp khóa theo mức độ tranh chấp:

```
Biased Lock      → chỉ 1 thread từng dùng: gần như không tốn phí
     ↓ (có thread thứ 2)
Lightweight Lock → tranh chấp nhẹ: dùng CAS + spin (quay vòng chờ), không ngủ
     ↓ (tranh chấp nặng)
Heavyweight Lock → nhiều thread giành: thread bị OS cho "ngủ" (block), tốn phí nhất
```

Vì thế `synchronized` ở trường hợp ít tranh chấp **rất nhanh**, chỉ chậm khi thật sự đông thread giành nhau.

### Visibility được đảm bảo miễn phí

Theo Java Memory Model, `monitorexit` tạo quan hệ **happens-before** với `monitorenter` sau đó. Cụ thể: khi ra khỏi khối synchronized, mọi thay đổi được **đẩy xuống RAM**; khi thread khác vào, nó **đọc lại từ RAM**. Nên các field được khóa bảo vệ **không cần thêm `volatile`**.

---

## Summary Table

| Feature | synchronized method | synchronized block |
|---------|--------------------|--------------------|
| Lock target | `this` (hoặc `Class`) | Object bất kỳ bạn chỉ định |
| Granularity | Cả method | Chỉ vài dòng bạn chọn |
| Use when | Cả method cần bảo vệ | Chỉ một phần đụng shared state |

| Concept | Key Point |
|---------|-----------|
| **Intrinsic lock** | Mọi object đều có — `synchronized` dùng nó |
| **Reentrant** | Cùng thread vào lại được; hold count đếm bên trong |
| **Visibility** | Vào/ra monitor tự flush cache — khỏi cần `volatile` |
| **Fairness** | JVM không đảm bảo thứ tự — thread có thể bị starve |

### Limitations (lý do sinh ra ReentrantLock)

| Limitation | Nghĩa là |
|------------|----------|
| **No timeout** | Chặn vô thời hạn khi chờ khóa |
| **Not interruptible** | `Thread.interrupt()` không đánh thức thread đang chặn |
| **No try-lock** | Không thể "thử lấy, không được thì thôi" |
| **Single condition** | Chỉ một wait-set cho mỗi monitor |

> `synchronized` là lựa chọn **mặc định** cho mutual exclusion đơn giản. Chỉ chuyển sang `ReentrantLock` khi cần timeout, interruptibility, fairness, hoặc nhiều condition queue.
