## ATOMICREFERENCE LÀ GÌ?

**`AtomicReference<V>`** là một `class` trong package `java.util.concurrent.atomic`, dùng để bọc một **object reference** và cho phép nhiều `thread` cùng đọc, ghi, cập nhật reference đó một cách **atomic** — nghĩa là không thể bị cắt ngang giữa chừng — mà **không cần `lock`**.

Nó giải quyết đúng một bài toán: *"nhiều thread cùng muốn thay thế một object dùng chung bằng object mới, làm sao để không ai ghi đè mất kết quả của ai"*.

> **Hiểu đơn giản:** `AtomicInteger` dành cho **số**, còn `AtomicReference` dành cho **object**.

---

## 🔑 Ví dụ đời thường — Tấm bảng tên người trực phòng máy

Ngoài cửa phòng máy có một **tấm bảng** ghi tên người đang trực. Quy định của trường:

- Ai muốn nhận ca trực phải **nhìn tên hiện tại trên bảng** trước.
- Khi xóa tên cũ để ghi tên mình, phải **kiểm tra lại**: nếu tên trên bảng vẫn đúng là tên lúc nãy mình nhìn thấy thì được ghi; nếu đã bị người khác đổi rồi thì **không được ghi đè** — phải nhìn lại từ đầu và thử lại.

Tình huống: bảng đang ghi **"An"**.

- **Bình** nhìn bảng → thấy "An" → định ghi "Bình".
- **Chi** cũng nhìn bảng → thấy "An" → định ghi "Chi".
- Chi nhanh tay hơn, ghi được "Chi" trước.
- Bình quay lại ghi: kiểm tra thấy bảng **không còn là "An"** nữa → ghi hỏng → Bình đọc lại bảng (giờ là "Chi") và thử lại.

Trong ví dụ này:

| Đời thường | Trong Java |
|---|---|
| Tấm bảng | `AtomicReference<String>` |
| Tên đang ghi trên bảng | Giá trị hiện tại — lấy bằng `get()` |
| "Nếu bảng vẫn là An thì ghi Bình" | `compareAndSet("An", "Bình")` |
| Ghi hỏng, phải đọc lại và thử lại | Vòng lặp retry của CAS |

Điểm cốt lõi: **không ai xếp hàng chờ ai cả**. Mọi người cứ thử ghi, ai thua thì làm lại. Đây gọi là cách tiếp cận **optimistic** (lạc quan), ngược với `lock` là **pessimistic** (bi quan — khóa cửa trước rồi mới làm).

---

## Vấn đề cần giải quyết

Giả sử cửa hàng lưu **tỷ giá hiện tại** trong một object, nhiều `thread` cùng cập nhật:

```java
// Object bất biến (immutable) — mỗi lần đổi giá là tạo object mới
record Rate(String code, int value) {}
```

> `record` cần Java 16 trở lên (xem bài [record](../keyword/record.md)). Với Java cũ hơn, thay bằng một `class` có đủ field `final`.

**Có vấn đề:**

```java
class RateHolder {
    private Rate rate = new Rate("USD", 25000);

    public void increase(int delta) {
        Rate current = rate;                                   // (1) đọc
        Rate next = new Rate(current.code(), current.value() + delta); // (2) tính
        rate = next;                                           // (3) ghi
    }

    public Rate get() { return rate; }
}

// 2 thread, mỗi thread gọi increase(10) đúng 1000 lần
// Mong đợi: 25000 + 20000 = 45000
// Thực tế: một số nhỏ hơn, khó đoán (vd 38720)
```

Các vấn đề:

- **Lost update:** giữa bước (1) và (3), `thread` khác có thể đã ghi giá trị mới. `Thread` sau ghi đè lên, **kết quả của người kia biến mất**. Đây chính là dạng `read-modify-write` trong bài [Race condition](../../error/Race%20condition.md).
- **Visibility:** biến `rate` không `volatile`, nên `thread` này sửa mà `thread` kia có thể **vẫn đọc được giá trị cũ** trong cache của CPU core khác.

Thêm `volatile` vào `rate` **không cứu được** — `volatile` chỉ sửa vấn đề visibility, còn chuỗi "đọc → tính → ghi" vẫn **không atomic**.

Dùng `synchronized` thì đúng, nhưng mọi `thread` phải **xếp hàng chờ nhau**:

```java
public synchronized void increase(int delta) { ... } // đúng, nhưng blocking
```

---

## Giải pháp — dùng AtomicReference

### Bước 1 — Bọc giá trị vào AtomicReference

```java
import java.util.concurrent.atomic.AtomicReference;

class RateHolder {
    private final AtomicReference<Rate> rate =
            new AtomicReference<>(new Rate("USD", 25000));

    public Rate get() {
        return rate.get();   // đọc luôn thấy giá trị mới nhất (như volatile read)
    }
}
```

> `final` ở đây là cố định **cái hộp**, còn **giá trị bên trong hộp** vẫn thay được. Xem thêm bài [final](../keyword/final.md).

### Bước 2 — Cập nhật bằng compareAndSet + vòng lặp retry

```java
public void increase(int delta) {
    Rate current;
    Rate next;
    do {
        current = rate.get();                                     // đọc giá trị hiện tại
        next = new Rate(current.code(), current.value() + delta); // tính giá trị mới
    } while (!rate.compareAndSet(current, next));                 // chỉ ghi nếu chưa ai đổi
}

// 2 thread × 1000 lần increase(10)
// Kết quả: luôn đúng 45000
```

Đọc `compareAndSet(current, next)` thành lời: *"nếu giá trị hiện tại **vẫn đúng là** `current` thì đổi thành `next` và trả về `true`; nếu đã có người đổi rồi thì không ghi gì cả và trả về `false`"*.

Trả về `false` nghĩa là có `thread` khác chen vào trước → vòng `do...while` **đọc lại và tính lại** → thử tiếp.

### Bước 3 — Viết gọn lại bằng updateAndGet

Từ Java 8, không cần tự viết vòng lặp nữa:

```java
public void increase(int delta) {
    rate.updateAndGet(r -> new Rate(r.code(), r.value() + delta));
}
```

`updateAndGet` đã bao gồm sẵn vòng lặp CAS bên trong và trả về giá trị **sau khi** cập nhật (bản `getAndUpdate` trả về giá trị **trước khi** cập nhật).

> ⚠️ Hàm truyền vào `updateAndGet` phải **không có side effect** (không in log, không ghi DB, không sửa biến ngoài). Khi CAS thất bại, hàm này sẽ **bị gọi lại nhiều lần**.

### Bước 4 — Thay nguyên cục giá trị bằng getAndSet

Khi không cần biết giá trị cũ là gì, chỉ muốn thay thế thẳng:

```java
Rate old = rate.getAndSet(new Rate("USD", 26000)); // đặt giá trị mới, trả về giá trị cũ
rate.set(new Rate("USD", 26000));                  // chỉ đặt, không quan tâm giá trị cũ
```

---

## ⚙️ Cách hoạt động bên dưới

### CAS — Compare-And-Swap

Bên trong `AtomicReference` chỉ có **một biến `volatile`** giữ reference, cộng với lệnh **CAS** của CPU.

CAS là **một lệnh phần cứng duy nhất** gộp cả ba việc "so sánh giá trị hiện tại — nếu khớp thì ghi giá trị mới — báo thành công hay thất bại". Vì là một lệnh nên **không thread nào chen vào giữa được**.

```
Thread A: get() = R1
Thread B: get() = R1
Thread B: CAS(R1 → R3)  ✅ thành công, giá trị giờ là R3
Thread A: CAS(R1 → R2)  ❌ thất bại vì giá trị hiện tại là R3, không phải R1
Thread A: get() = R3, tính lại, CAS(R3 → R4)  ✅ thành công
```

Không `thread` nào bị **block** (bị treo chờ). Thread thua chỉ đơn giản là **làm lại**.

### So sánh bằng `==`, không phải `equals()`

Đây là điểm rất dễ nhầm:

```java
AtomicReference<String> ref = new AtomicReference<>(new String("An"));

ref.compareAndSet(new String("An"), "Bình");  // ❌ false — khác object, dù nội dung giống
ref.compareAndSet(ref.get(), "Bình");         // ✅ true  — đúng cùng một object
```

`compareAndSet` so sánh **identity** (cùng một object trong bộ nhớ hay không), nên giá trị `expected` gần như luôn phải là thứ vừa lấy ra từ `get()`.

### Vấn đề ABA

CAS chỉ hỏi *"giá trị có còn giống lúc tôi đọc không"*, chứ không hỏi *"trong lúc tôi quay đi, nó có bị đổi rồi đổi lại không"*.

```
Giá trị ban đầu: A
Thread B: A → B → A   (đổi đi rồi đổi về đúng object A cũ)
Thread A: CAS(A → C)  ✅ vẫn thành công, dù thực tế mọi thứ đã thay đổi ở giữa
```

Với dữ liệu immutable thông thường, ABA thường **vô hại**. Nhưng khi tự cài đặt cấu trúc dữ liệu lock-free (stack, queue), nó có thể gây hỏng dữ liệu. Khi đó dùng `AtomicStampedReference` — gắn thêm một **con tem số phiên bản**:

```java
import java.util.concurrent.atomic.AtomicStampedReference;

AtomicStampedReference<Rate> ref =
        new AtomicStampedReference<>(new Rate("USD", 25000), 0); // version = 0

int[] stampHolder = new int[1];
Rate current = ref.get(stampHolder);          // lấy cả giá trị lẫn version
int stamp = stampHolder[0];

ref.compareAndSet(current, next, stamp, stamp + 1); // phải khớp CẢ giá trị lẫn version
```

### Chỉ reference là atomic, ruột object thì không

```java
class MutableRate { int value; }   // object có thể sửa được

AtomicReference<MutableRate> ref = new AtomicReference<>(new MutableRate());
ref.get().value++;                 // ❌ race condition — CAS không bảo vệ gì ở đây cả
```

`AtomicReference` chỉ đảm bảo **việc thay cái reference** là atomic. Nếu nhiều `thread` cùng sửa ruột của object được trỏ tới thì vẫn race như thường.

> 📌 Quy tắc bắt buộc: giá trị bỏ vào `AtomicReference` nên là **immutable** (ví dụ `record`, hoặc class có field `final`). Muốn đổi thì **tạo object mới** rồi CAS vào, không sửa tại chỗ.

---

## ⚠️ Hiểu nhầm thường gặp — "AtomicReference là một hàng chờ"

Rất nhiều người mới hiểu `AtomicReference` thành: *"nó bắt các thread lần lượt vào sửa giá trị, A xong thì tới B"*. **Đó là mô tả của `lock`, không phải của `AtomicReference`.**

Với `synchronized`, thread thua thực sự **bị block** — hệ điều hành cho nó ngủ, nó không chạy được gì cho tới khi được đánh thức:

```text
synchronized (xếp hàng)
  A vào, khóa cửa, làm việc
  B tới → cửa khóa → B NGỦ ngoài cửa, không chạy gì cả
  A xong, mở khóa → hệ điều hành đánh thức B → B vào làm
```

Với `AtomicReference`, **không ai ngủ cả**. Cả hai cùng chạy, chỉ khi ghi mới có người thắng người thua, và người thua **tính lại** chứ không chờ:

```text
AtomicReference (không xếp hàng)
  A đọc 25000 → tính ra 25010
  B đọc 25000 → tính ra 25010      ← A và B chạy SONG SONG
  B ghi: "nếu còn 25000 thì thành 25010"  ✅ giá trị = 25010
  A ghi: "nếu còn 25000 thì thành 25010"  ❌ hỏng, hiện tại đã là 25010
                                          👉 kết quả A vừa tính bị VỨT
  A đọc lại 25010 → tính ra 25020 → ghi   ✅ giá trị cuối = 25020
```

Kết quả cuối là **25020** — đủ cả hai lần cộng. Giá trị A tính ra lần đầu bị bỏ, nhưng **việc A cần làm thì không mất** — nó được làm lại trên dữ liệu mới nhất.

Ba điểm cần phân biệt cho rõ:

| Câu hỏi | `synchronized` | `AtomicReference` |
|---|---|---|
| Thread thua có bị treo không? | **Có** — ngủ chờ đánh thức | **Không** — vẫn chạy, chỉ tính lại |
| Có đảm bảo thứ tự trước sau không? | Có xếp hàng (fair lock thì đúng FIFO) | Không — ai ghi được trước thì thắng |
| Cái phải trả giá là gì? | Thời gian **chờ** | Công sức **tính lại** (tốn CPU) |

> 📌 Chỗ duy nhất "một người một lúc" là **bản thân lệnh CAS** trên ô nhớ đó — phần cứng đảm bảo hai lệnh CAS không chồng lên nhau. Nhưng đó là chuyện ở mức ô nhớ, tính bằng nano giây, **không phải thread xếp hàng**. Còn thao tác đọc `get()` thì không tranh chấp gì cả: bao nhiêu thread đọc cùng lúc cũng được.

> **Nhớ cách này:** `lock` = *xếp hàng trước khi làm*. `AtomicReference` = *cứ làm, ai bị chen thì làm lại*.

---

## Tại sao tốt hơn lock?

| | `synchronized` / `ReentrantLock` | `AtomicReference` |
|---|---|---|
| Cách tiếp cận | Pessimistic — khóa trước rồi mới làm | Optimistic — cứ làm, hỏng thì thử lại |
| Thread thua cuộc | Bị **block**, phải chờ được đánh thức | Không bị block, chỉ **lặp lại** |
| Nguy cơ deadlock | Có, khi giữ nhiều khóa — xem [Deadlock](../../error/Deadlock.md) | Không, vì không giữ khóa nào |
| Chi phí khi tranh chấp **thấp** | Cao hơn (phải vào/ra monitor) | Rất rẻ |
| Chi phí khi tranh chấp **cao** | Ổn định hơn | Tốn CPU vì retry liên tục |
| Bảo vệ được | Nhiều biến, nhiều dòng lệnh | **Đúng một** reference |

---

## Phân biệt với các công cụ gần giống

| Công cụ | Bảo vệ được gì | Ghi chú |
|---|---|---|
| `volatile` | Chỉ **visibility** | Không làm "đọc → sửa → ghi" thành atomic |
| `AtomicInteger` | Một biến số `int` | Có sẵn `incrementAndGet()` cho biến đếm |
| `AtomicReference<V>` | Một **object reference** | Giá trị nên immutable |
| `AtomicStampedReference<V>` | Reference + version | Dùng khi cần chống **ABA** |
| `synchronized` | Nhiều biến, cả khối lệnh | Xem bài [Synchronized Lock](../lock/Synchronized%20Lock.md) |

> ⚠️ **Hiểu nhầm thường gặp:** "Dùng `AtomicReference` thì code tự động thread-safe". Sai. Nó chỉ làm **một thao tác thay reference** trở nên atomic. Nếu logic của bạn cần cập nhật **hai biến liên quan nhau** thì phải gom hai biến đó vào **một object immutable** rồi CAS, hoặc dùng `lock`.

---

## Khi nào nên dùng?

| Dùng khi | Ví dụ thực tế |
|---|---|
| Cần thay thế nguyên một object cấu hình dùng chung | Snapshot config, bảng tỷ giá, feature flag |
| Trạng thái chuyển theo kiểu state machine | `NEW → RUNNING → DONE` bằng `compareAndSet` |
| Khởi tạo lazy chỉ đúng một lần | `ref.compareAndSet(null, heavyObject())` |
| Tranh chấp giữa các thread **ít xảy ra** | Nhiều `thread` đọc, thỉnh thoảng mới ghi |
| Cần non-blocking, không được để thread bị treo | Code trong thread pool nhỏ, code nhạy độ trễ |

Không nên dùng khi:

- Object bên trong **mutable** và nhiều `thread` cùng sửa ruột nó.
- Cần cập nhật **nhiều biến cùng lúc** mà không gom được vào một object.
- Tranh chấp **rất cao** — retry liên tục có thể tốn CPU hơn là xếp hàng với `lock`.
- Chỉ cần **đếm số** — dùng `AtomicInteger` / `LongAdder` gọn hơn.

> ⚠️ **Đừng lạm dụng:** `AtomicReference` là công cụ cho *một* ô dữ liệu. Code có 5 biến chung mà nhét thành 5 `AtomicReference` thì mỗi biến atomic riêng lẻ, nhưng **tổng thể vẫn không nhất quán**.

---

## API thường dùng

| Method | Ý nghĩa |
|---|---|
| `get()` | Đọc giá trị hiện tại |
| `set(V)` | Ghi đè, không quan tâm giá trị cũ |
| `getAndSet(V)` | Ghi giá trị mới, trả về giá trị cũ |
| `compareAndSet(expect, update)` | Chỉ ghi nếu giá trị hiện tại `==` `expect`; trả về `true`/`false` |
| `updateAndGet(f)` | CAS lặp sẵn, trả về giá trị **sau** khi đổi (Java 8+) |
| `getAndUpdate(f)` | CAS lặp sẵn, trả về giá trị **trước** khi đổi (Java 8+) |
| `accumulateAndGet(x, f)` | Gộp giá trị hiện tại với `x` theo hàm `f` (Java 8+) |

---

## Tóm tắt

```
AtomicReference<V> = một ô nhớ giữ object reference, cập nhật atomic bằng CAS

Cơ chế:
  get()                     → đọc giá trị mới nhất (volatile read)
  compareAndSet(old, new)   → "nếu vẫn là old thì đổi thành new"
        thành công → xong
        thất bại   → đọc lại, tính lại, thử lại (retry)

Bắt buộc nhớ:
  1. So sánh bằng ==, KHÔNG phải equals()
  2. Chỉ reference atomic — ruột object KHÔNG được bảo vệ → dùng immutable
  3. Hàm trong updateAndGet phải không có side effect (bị gọi lại khi retry)
  4. Đổi đi rồi đổi về (ABA) → AtomicStampedReference
  5. KHÔNG phải hàng chờ — thread thua không bị block, chỉ tính lại

Chọn công cụ:
  đếm số              → AtomicInteger / LongAdder
  thay 1 object       → AtomicReference          (optimistic, không block)
  nhiều biến / nhiều dòng lệnh → synchronized / ReentrantLock (pessimistic)
```

> **Một câu tóm gọn:** `AtomicReference` cho phép nhiều `thread` **thi nhau thay một object dùng chung** mà không cần khóa — ai bị chen ngang thì đọc lại và làm lại, nên không bao giờ mất cập nhật và cũng không bao giờ deadlock.
