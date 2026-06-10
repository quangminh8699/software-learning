# ☕ Java Core — Từ cơ bản đến nâng cao (cấp độ Senior/Techlead)

Bộ giáo trình Java Core đào sâu **không chỉ cú pháp** mà cả **cách JVM & máy thực thi**:
bố cục bộ nhớ, bytecode, class loading, Garbage Collection, JIT, Java Memory Model,
cache CPU và chi phí hiệu năng.

> 🎯 **Đối tượng:** lập trình viên middle → senior → techlead.
> Mỗi bài trả lời được câu hỏi: *"JVM/máy thực sự làm gì khi đoạn code này chạy?"*

---

## 📐 Cấu trúc mỗi bài học

Mỗi folder bài học gồm 2 file:

| File | Nội dung |
|------|----------|
| `Main.java` | Code chạy được, comment **4W** (What/Why/How/When-Which) + chú thích **"dưới nắp capo"** |
| `README.md` | Mô tả · Kỹ thuật · ⚙️ Dưới nắp capo · Cách dùng · 🎨 Bản vẽ ASCII · ⚠️ Cạm bẫy senior · 🔗 Bài tiếp |

---

## 🗺️ Lộ trình học (8 module · 48 bài)

### 📦 01 — Basics (Nền tảng)
| # | Bài | Điểm nhấn dưới nắp capo |
|---|-----|------------------------|
| 01 | [hello-world](01-basics/01-hello-world) | JDK/JRE/JVM, vòng đời compile → bytecode → execute |
| 02 | [variables-datatypes](01-basics/02-variables-datatypes) | primitive trên stack vs object trên heap, kích thước byte |
| 03 | [operators](01-basics/03-operators) | bitwise, overflow số nguyên, toán tử trên bytecode |
| 04 | [control-flow](01-basics/04-control-flow) | `switch` expression, `tableswitch`/`lookupswitch` |
| 05 | [loops](01-basics/05-loops) | for-each = iterator, loop unrolling của JIT |
| 06 | [arrays](01-basics/06-arrays) | bố cục mảng trên heap, bounds-check |
| 07 | [strings](01-basics/07-strings) | String pool, immutability, `StringBuilder` |
| 08 | [methods](01-basics/08-methods) | stack frame, overloading (static binding), đệ quy & StackOverflow |

### 🧱 02 — OOP
| # | Bài |
|---|-----|
| 09 | [classes-objects](02-oop/09-classes-objects) · 10 [constructors](02-oop/10-constructors) · 11 [encapsulation](02-oop/11-encapsulation) |
| 12 | [inheritance](02-oop/12-inheritance) · 13 [polymorphism](02-oop/13-polymorphism) · 13b [overloading-overriding](02-oop/13b-overloading-overriding) |
| 14 | [abstraction](02-oop/14-abstraction) · 15 [interfaces](02-oop/15-interfaces) · 16 [static-final](02-oop/16-static-final) |
| 17 | [nested-inner-classes](02-oop/17-nested-inner-classes) · 18 [enums](02-oop/18-enums) · 19 [records-sealed](02-oop/19-records-sealed) |

### 🔌 03 — Core APIs & Generics
20 [exception-handling](03-core-apis/20-exception-handling) · 21 [wrapper-autoboxing](03-core-apis/21-wrapper-autoboxing) ·
22 [generics](03-core-apis/22-generics) · 23 [collections-list](03-core-apis/23-collections-list) ·
24 [collections-set-map](03-core-apis/24-collections-set-map) · 25 [iterator-comparator](03-core-apis/25-iterator-comparator) ·
26 [optional](03-core-apis/26-optional)

### λ 04 — Functional & Streams
27 [lambda-expressions](04-functional-streams/27-lambda-expressions) · 28 [functional-interfaces](04-functional-streams/28-functional-interfaces) ·
29 [method-references](04-functional-streams/29-method-references) · 30 [stream-api](04-functional-streams/30-stream-api) ·
31 [pattern-matching](04-functional-streams/31-pattern-matching)

### 🧵 05 — Concurrency
32 [threads-basics](05-concurrency/32-threads-basics) · 33 [synchronization](05-concurrency/33-synchronization) ·
34 [executor-framework](05-concurrency/34-executor-framework) · 35 [concurrent-utilities](05-concurrency/35-concurrent-utilities)

### 🛠️ 06 — Advanced
36 [file-io](06-advanced/36-file-io) · 37 [nio](06-advanced/37-nio) ·
38 [annotations](06-advanced/38-annotations) · 39 [reflection](06-advanced/39-reflection)

### ⚙️ 07 — JVM Internals & Performance
40 [jvm-architecture](07-jvm-internals/40-jvm-architecture) · 41 [bytecode](07-jvm-internals/41-bytecode) ·
42 [memory-layout](07-jvm-internals/42-memory-layout) · 43 [garbage-collection](07-jvm-internals/43-garbage-collection) ·
44 [jit-performance](07-jvm-internals/44-jit-performance)

### 🎭 08 — Design Patterns (GoF)
45 [creational](08-design-patterns/45-creational) · 46 [structural](08-design-patterns/46-structural) ·
47 [behavioral](08-design-patterns/47-behavioral)

---

## ▶️ Yêu cầu & cách chạy

```bash
# Yêu cầu: JDK 21 trở lên
java -version     # phải >= 21

# Chạy một bài bất kỳ:
cd java-core/01-basics/01-hello-world
javac Main.java        # biên dịch -> Main.class (bytecode)
java Main              # JVM nạp & thực thi

# Quan sát "dưới nắp capo" (tuỳ bài):
javap -c -p Main                  # xem bytecode
java -Xint Main                   # ép chạy interpreter (tắt JIT)
java -XX:+PrintGCDetails Main     # log GC
```

> 💡 Mỗi `Main.java` độc lập (default package) — biên dịch & chạy riêng từng folder,
> không cần build tool, không xung đột tên class.

---

## 🧭 Bản đồ kiến trúc JVM (tham chiếu xuyên suốt)

```
        File .java ──javac──▶ File .class (bytecode)
                                   │
                                   ▼
   ┌───────────────────────── JVM ─────────────────────────┐
   │  ClassLoader ─▶  Runtime Data Areas                    │
   │                   ├── Method Area / Metaspace (class)  │
   │                   ├── Heap (object, mảng) ── GC quét   │
   │                   ├── JVM Stack (frame / biến local)   │
   │                   ├── PC Register (mỗi thread)         │
   │                   └── Native Method Stack              │
   │  Execution Engine: Interpreter + JIT (C1/C2) + GC      │
   └────────────────────────────────────────────────────────┘
```

Bắt đầu tại 👉 [01-basics/01-hello-world](01-basics/01-hello-world)
