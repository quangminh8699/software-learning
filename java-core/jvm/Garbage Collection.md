## WHAT IS GARBAGE COLLECTION (GC)?

**Garbage Collection** là cơ chế **tự động thu hồi bộ nhớ** của JVM: nó tìm những object **không còn ai dùng tới nữa** trên **heap** và giải phóng chúng, trả chỗ trống lại cho chương trình cấp phát object mới.

Trong C/C++ bạn phải tự `malloc`/`free` (tự xin, tự trả). Java thì khác: bạn chỉ `new` (xin), còn **việc trả bộ nhớ là của GC lo** — bạn không bao giờ gọi `free()`. Đây là một trong những lý do Java "an toàn bộ nhớ": không lo *dangling pointer*, ít lo *memory leak thủ công*.

```
Không có GC (C/C++):   new/malloc  →  ...dùng...  →  BẠN tự free()   ← quên = leak, free 2 lần = crash
Có GC (Java):          new         →  ...dùng...  →  GC tự dọn        ← bạn không phải làm gì
```

> ⚠️ **HIỂU NHẦM THƯỜNG GẶP:** "Có GC nên Java không bao giờ memory leak." **SAI.** GC chỉ dọn object **không còn ai tham chiếu tới**. Nếu bạn *vô tình còn giữ tham chiếu* (ví dụ nhét mãi vào một `static List` mà không xóa) → object đó vẫn "sống" trong mắt GC → **vẫn leak**. Xem phần "GC leak vẫn xảy ra" bên dưới.

---

## 🗑️ VÍ DỤ ĐỜI THƯỜNG — Nhân viên dọn bàn quán cà phê

Tưởng tượng heap là **cái quán cà phê**, mỗi object là một **cái bàn có người ngồi**.

- Khách vào → chiếm 1 bàn (`new` một object).
- Khách đứng dậy đi về → **cái bàn đó không ai ngồi nữa** (object mất hết tham chiếu).
- **Nhân viên dọn bàn** (GC) đi vòng quanh, thấy bàn nào **không còn ai ngồi** → dọn sạch, trả bàn trống cho khách mới.

Vài ý quan trọng từ ẩn dụ này:

- Nhân viên **không dọn bàn đang có người** — object còn được tham chiếu thì GC không đụng tới.
- Làm sao biết bàn "có người"? Nhân viên nhìn từ **cửa ra vào** (GC Roots) lần theo xem bàn nào còn nối tới. Bàn nào **không lần tới được từ cửa** = bỏ hoang = dọn.
- Lúc dọn dẹp lớn, đôi khi **quán phải tạm dừng phục vụ** (Stop-The-World) để nhân viên dọn cho gọn — đây chính là thứ làm ứng dụng "khựng" một nhịp.

👉 GC = **anh nhân viên dọn bàn tự động**. Bạn cứ ngồi (viết logic), việc dọn để anh ấy lo.

---

## ❓ WHY — Tại sao cần GC? Đánh đổi gì?

**Tại sao Java chọn GC thay vì để lập trình viên tự quản lý?**

| Vấn đề khi tự quản lý bộ nhớ (C/C++) | GC giải quyết thế nào |
|---|---|
| Quên `free` → **memory leak** | Tự dọn khi object hết tham chiếu |
| `free` 2 lần → **double free / crash** | Không có `free` thủ công |
| Dùng con trỏ đã `free` → **dangling pointer** | Object còn được tham chiếu thì không bị dọn |
| Quản lý ownership phức tạp | Lập trình viên không cần nghĩ tới |

**Nhưng GC không miễn phí — cái giá phải trả:**

- **Overhead CPU:** GC cũng là code chạy, tốn CPU để quét và dọn.
- **Stop-The-World (STW):** nhiều pha GC phải **tạm dừng toàn bộ application thread** để dọn an toàn → gây *latency spike*. Đây là kẻ thù số 1 của app cần độ trễ thấp (trading, game, real-time).
- **Khó đoán thời điểm:** bạn không kiểm soát *khi nào* GC chạy. `System.gc()` chỉ là **gợi ý**, JVM có quyền phớt lờ.

> 💡 Toàn bộ lịch sử phát triển các loại GC (Serial → G1 → ZGC) thực chất là **cuộc chiến giảm thời gian Stop-The-World** trong khi vẫn giữ throughput tốt.

---

## 🔍 HOW — GC hoạt động thế nào bên dưới?

### 1. Xác định "rác": GC Roots & Reachability

GC **không đếm** xem object được dùng bao nhiêu lần. Cách chính xác mà JVM dùng là **Reachability Analysis** (phân tích khả năng truy cập):

```
Từ tập GC Roots, đi lần theo mọi tham chiếu:
  - Object nào ĐI TỚI ĐƯỢC  → còn sống (reachable) → GIỮ
  - Object nào KHÔNG tới được → rác (unreachable)   → DỌN
```

**GC Roots** là các điểm "gốc" chắc chắn đang sống, gồm:
- Biến local trên **stack** của các thread đang chạy.
- **Static field** của các class đã load.
- **Active thread** object.
- JNI references (native).

```java
void demo() {
    Object a = new Object();   // (1) a là GC Root (biến local) → object sống
    a = null;                  // (2) cắt tham chiếu → object (1) thành rác → sẽ bị dọn
    Object b = new Object();   // b sống chừng nào method chưa return
}                              // (3) method return → b hết là root → b thành rác
```

> Vì sao không dùng **reference counting** (đếm số tham chiếu)? Vì nó **không xử lý được vòng lặp**: A trỏ B, B trỏ A, nhưng cả hai đều bị bỏ hoang → count của cả hai vẫn = 1 → không bao giờ dọn được. Reachability từ GC Roots né được bẫy này.

### 2. Ba thao tác nền tảng: Mark → Sweep → Compact

Gần như mọi thuật toán GC đều xây trên 3 bước này:

```
1. MARK    — Đi từ GC Roots, đánh dấu mọi object còn sống.
2. SWEEP   — Quét heap, thu hồi (giải phóng) mọi object KHÔNG được đánh dấu.
3. COMPACT — Dồn các object còn sống về sát nhau, xóa "lỗ hổng" (chống phân mảnh).
```

**Vì sao cần Compact?** Sau khi sweep, bộ nhớ trống bị **phân mảnh** (chỗ trống rải rác, xen kẽ object sống). Khi cần cấp phát một object lớn liên tục → không tìm ra khối trống đủ to dù tổng bộ nhớ trống vẫn nhiều. Compact dồn lại để có khối trống liền mạch.

### 3. Generational Hypothesis — Chia heap theo "tuổi"

Quan sát thực nghiệm cốt lõi (**Weak Generational Hypothesis**):

> **"Hầu hết object chết trẻ."** — Đa số object (biến tạm, object trong vòng lặp...) chỉ sống rất ngắn rồi thành rác ngay.

Từ đó JVM chia heap thành các **thế hệ (generation)** để dọn thông minh hơn:

```
HEAP
├── Young Generation  (object mới sinh — dọn thường xuyên, nhanh)
│   ├── Eden        ← object mới new vào đây
│   ├── Survivor S0  ┐ object sống sót qua GC được chuyển qua lại
│   └── Survivor S1  ┘ giữa S0/S1, mỗi lần sống sót "già" thêm 1 tuổi
│
└── Old / Tenured Generation  (object sống lâu — dọn ít, chậm hơn)
    └── object đủ "già" (qua nhiều lần GC) được thăng cấp (promote) lên đây
```

- **Minor GC:** dọn Young Gen. Xảy ra **thường xuyên** nhưng **nhanh** (vì đa số object ở đây chết trẻ, ít cái sống sót).
- **Major GC / Full GC:** dọn Old Gen (thường kèm cả Young). **Hiếm hơn** nhưng **chậm và STW lâu** hơn nhiều → thứ ta muốn tránh.

```
Vòng đời một object điển hình:
new → Eden → (sống sót Minor GC) → Survivor → ...qua vài lần GC... → promote → Old Gen → (Full GC dọn nếu chết)
```

> 📌 **Metaspace** (Java 8+, thay cho PermGen cũ) nằm **ngoài heap**, chứa class metadata. Nó không được các GC young/old dọn theo cùng cơ chế — nhớ rằng "GC dọn heap", còn class metadata là chuyện riêng.

---

## 🧰 CÁC LOẠI GC TRONG JAVA

JVM (HotSpot) cho chọn thuật toán GC bằng flag khi chạy. Mỗi loại là một **đánh đổi khác nhau giữa Throughput và Latency (thời gian STW)**.

### 1. Serial GC  `-XX:+UseSerialGC`

- **Một thread duy nhất** làm toàn bộ việc GC. Khi GC chạy → **STW toàn bộ**.
- Đơn giản, ít overhead, **không tốn tài nguyên phối hợp đa luồng**.
- **Phù hợp khi:** app nhỏ, heap nhỏ (vài trăm MB), môi trường 1 CPU, hoặc client-side/CLI ngắn hạn.

### 2. Parallel GC (Throughput Collector)  `-XX:+UseParallelGC`

- Dùng **nhiều thread** để dọn (song song) → nhanh hơn Serial. Vẫn **STW** khi dọn, nhưng dừng ngắn hơn nhờ chia việc.
- Tối ưu cho **throughput** (tổng lượng việc làm được), **không** tối ưu độ trễ từng lần dừng.
- **Phù hợp khi:** batch job, xử lý dữ liệu lớn, app chạy nền — nơi **tổng thông lượng quan trọng hơn** độ giật từng nhịp. (Là GC mặc định của Java 8.)

### 3. CMS — Concurrent Mark Sweep  `-XX:+UseConcMarkSweepGC`  *(đã DEPRECATED, gỡ từ Java 14)*

- Chạy **đồng thời (concurrent)** phần lớn công việc mark/sweep **song song với application thread** → giảm mạnh STW.
- Nhược điểm: **không compact** → gây phân mảnh; tốn CPU; có thể "Concurrent Mode Failure" phải fallback Full GC.
- Đã bị **thay thế bởi G1**. Ghi nhớ cho ngữ cảnh lịch sử / hệ thống cũ.

### 4. G1 GC — Garbage First  `-XX:+UseG1GC`  *(mặc định từ Java 9+)*

- Chia heap thành **nhiều region nhỏ bằng nhau** (không còn Young/Old cố định liền khối — mỗi region đóng vai trò linh hoạt).
- **"Garbage First":** ưu tiên dọn các region **nhiều rác nhất** trước → thu hồi được nhiều nhất với ít công nhất.
- Cho phép đặt **mục tiêu thời gian dừng**: `-XX:MaxGCPauseMillis=200` → G1 cố giữ mỗi lần STW dưới 200ms.
- **Có compact** (dồn khi copy region) → tránh phân mảnh.
- **Phù hợp khi:** app server phổ thông, heap vừa–lớn (vài GB), cần cân bằng throughput & latency. **Lựa chọn mặc định hợp lý cho hầu hết app.**

### 5. ZGC  `-XX:+UseZGC`  *(production từ Java 15)*

- **Low-latency cực đại:** thời gian STW **< 1ms**, gần như không đổi **dù heap to cỡ nào** (hàng chục GB → TB).
- Làm gần như mọi việc **concurrent** (kể cả relocate object) nhờ **colored pointers** + **load barriers**.
- Đánh đổi: tốn thêm CPU/bộ nhớ, throughput có thể thấp hơn G1 một chút.
- **Phù hợp khi:** app cần **độ trễ thấp và ổn định**, heap rất lớn — trading, real-time, service latency-sensitive.

### 6. Shenandoah GC  `-XX:+UseShenandoahGC`

- Cũng là **low-pause concurrent collector** (giống mục tiêu ZGC), do Red Hat phát triển.
- Compact/relocate **concurrent** với app → pause gần như không phụ thuộc kích thước heap.
- **Phù hợp khi:** cần low-latency, trên các bản OpenJDK có hỗ trợ Shenandoah.

### 7. Epsilon GC  `-XX:+UseEpsilonGC`  *(No-Op)*

- **GC "không làm gì cả":** chỉ cấp phát, **không bao giờ thu hồi**. Khi hết heap → app crash (OOM).
- **Phù hợp khi:** benchmark hiệu năng, test độ chịu tải, hoặc app cực ngắn hạn biết chắc không đầy heap. **Không dùng cho production thường.**

---

## 📊 BẢNG SO SÁNH CÁC LOẠI GC

| GC | Threads / Mô hình | STW | Tối ưu cho | Heap phù hợp | Ghi chú |
|---|---|---|---|---|---|
| **Serial** | 1 thread, STW | Cao | Đơn giản, ít overhead | Nhỏ (<100–500MB) | App nhỏ, 1 CPU |
| **Parallel** | Nhiều thread, STW | Trung bình | **Throughput** | Vừa | Mặc định Java 8, batch |
| **CMS** | Concurrent mark-sweep | Thấp | Latency | Vừa–lớn | ❌ Deprecated (gỡ Java 14) |
| **G1** | Region-based, phần lớn concurrent | Thấp (có mục tiêu) | Cân bằng | Vừa–lớn (nhiều GB) | ✅ Mặc định Java 9+ |
| **ZGC** | Concurrent, colored pointers | **< 1ms** | **Latency cực thấp** | Lớn → rất lớn (TB) | Production Java 15+ |
| **Shenandoah** | Concurrent compact | **< 1ms** | Latency cực thấp | Lớn | OpenJDK (Red Hat) |
| **Epsilon** | Không thu hồi | Không GC | Benchmark/test | — | No-op, OOM khi đầy |

---

## ❌ GC LEAK VẪN XẢY RA — Đừng chủ quan

GC chỉ dọn object **unreachable**. Nếu code **vô tình giữ tham chiếu**, object sống mãi → **memory leak** dù có GC:

```java
class Cache {
    // static → là GC Root, sống suốt đời app
    private static final List<byte[]> CACHE = new ArrayList<>();

    void handle(byte[] data) {
        CACHE.add(data);   // ❌ thêm mãi, KHÔNG BAO GIỜ remove
    }                      // → mọi data vẫn reachable từ static → GC không dọn → leak dần tới OOM
}
```

**Các nguồn leak kinh điển dù có GC:**
- `static` collection cứ thêm mà không xóa (như trên).
- Listener/callback đăng ký mà **quên hủy đăng ký**.
- `ThreadLocal` không `remove()` trong thread pool.
- Key trong `HashMap` không override `equals`/`hashCode` đúng → không xóa được.

> **Câu tự vấn:** "Object này còn *cần* nữa không, và có còn *đường nào từ GC Root* lần tới nó không?" Nếu không cần nhưng vẫn còn đường tới → chủ động cắt tham chiếu (`= null`, `remove()`, `unregister()`).

---

## 🔗 Reference Types — Điều khiển "độ bám" của GC

Java cho bạn tinh chỉnh mức độ GC "nể" một tham chiếu:

| Loại | Class | GC dọn khi nào | Dùng cho |
|---|---|---|---|
| **Strong** | `Object o = new...` | Chỉ khi **unreachable** (mặc định) | Bình thường |
| **Soft** | `SoftReference` | Khi **sắp hết bộ nhớ** | Cache "giữ được thì giữ" |
| **Weak** | `WeakReference` | **Ngay lần GC kế tiếp** nếu không còn strong ref | `WeakHashMap`, metadata |
| **Phantom** | `PhantomReference` | Sau khi finalize, để **dọn dẹp tài nguyên** | Thay `finalize()` |

---

## Summary

```
Garbage Collection = JVM tự động thu hồi object KHÔNG CÒN reachable trên heap

WHAT  → Cơ chế tự dọn bộ nhớ, thay cho free() thủ công của C/C++
WHY   → An toàn bộ nhớ (hết leak/dangling thủ công), ĐỔI LẠI: overhead CPU + Stop-The-World
HOW   → GC Roots → Reachability → Mark/Sweep/Compact → chia Generation (Young/Old)

Các loại GC (đánh đổi Throughput ⟷ Latency):
├── Serial      → 1 thread, app nhỏ
├── Parallel    → đa thread, tối ưu throughput (mặc định Java 8)
├── CMS         → concurrent, đã deprecated
├── G1          → region-based, cân bằng (mặc định Java 9+)   ← chọn mặc định tốt
├── ZGC         → STW < 1ms, heap khổng lồ, latency cực thấp
├── Shenandoah  → low-pause concurrent (Red Hat)
└── Epsilon     → no-op, chỉ để benchmark

⚠️ GC KHÔNG cứu được leak do CÒN tham chiếu (static list, listener, ThreadLocal...)
```

> Chọn GC theo nhu cầu, đừng chọn theo "cái nào mới nhất": cần **throughput** (batch) → Parallel; cần **cân bằng** cho app server thường → **G1** (mặc định, hợp lý nhất cho đa số); cần **latency thấp + heap lớn** → ZGC/Shenandoah. Với hầu hết ứng dụng, **để mặc định G1 và đo trước khi tune** là lời khuyên đúng nhất.
