# Bài 01: Hello World — Cấu trúc chương trình & vòng đời thực thi

Chương trình Java nhỏ nhất, nhưng nhìn xuyên qua nó để hiểu **toàn bộ vòng đời** từ mã nguồn tới lúc CPU chạy.

## 📖 Mô tả
Mọi ứng dụng Java — từ CLI tới Spring Boot — đều khởi động từ một method `main`. Bài này mổ xẻ:
- Vì sao chữ ký `public static void main(String[] args)` phải đúng từng từ.
- Hành trình `.java → .class → JVM → CPU`.
- Phân biệt **JDK / JRE / JVM**.

## 🔧 Kỹ thuật
- **Class & method**: một file `.java` chứa class `public` cùng tên file.
- **Entry point**: JVM tìm `main` với chữ ký chuẩn để bắt đầu.
- **Bytecode**: `.class` chứa lệnh máy ảo trung lập nền tảng — "viết một lần, chạy mọi nơi".

| Khái niệm | Là gì | Chứa gì |
|-----------|-------|---------|
| **JDK** | Bộ công cụ phát triển | `javac`, `javap`, `jar`, ... + JRE |
| **JRE** | Môi trường chạy | thư viện chuẩn + JVM |
| **JVM** | Máy ảo thực thi bytecode | ClassLoader, Runtime Data Areas, Execution Engine |

## ⚙️ Dưới nắp capo (Under the hood)
```
   Main.java
      │  javac (biên dịch tĩnh, kiểm tra kiểu)
      ▼
   Main.class  (bytecode + constant pool)
      │  java Main
      ▼
 ┌──────────────── JVM ────────────────┐
 │ 1) ClassLoader: nạp Main.class       │  Bootstrap → Platform → App loader
 │ 2) Linking: verify → prepare → resolve│  verifier chặn bytecode độc/hỏng
 │ 3) Initialize: chạy static init       │
 │ 4) Execution Engine:                  │
 │      Interpreter (chạy ngay)          │
 │      + JIT C1/C2 (biên dịch chỗ nóng) │ → mã máy native
 └───────────────────────────────────────┘
```
- `javac` **không** sinh mã máy của CPU — chỉ sinh **bytecode**. Việc dịch sang mã máy
  do **JIT** làm lúc runtime, dựa trên hồ sơ thực thi (profile-guided).
- `main` là `static` vì JVM cần một điểm vào **không phụ thuộc object** — lúc đó chưa có instance nào.

## ▶️ Cách dùng
```bash
javac Main.java        # -> Main.class
java Main              # chạy
java Main Alice        # truyền tham số dòng lệnh

# Quan sát internals:
javap -c -p Main       # in bytecode method main()
java -Xint Main        # ép interpreter (tắt JIT) — thấy khởi động "lạnh"
```
Output mẫu:
```
Hello, World!
(Mẹo: chạy `java Main TênBạn` để truyền tham số)
Java version : 21.0.10
JVM name     : OpenJDK 64-Bit Server VM
Số CPU core  : 10
```

## 🎨 Bản vẽ
```
  Lập trình viên           Build-time            Run-time
  ┌──────────┐   javac    ┌───────────┐  java   ┌──────────┐
  │ Main.java│ ─────────▶ │ Main.class│ ──────▶ │   JVM    │ ─▶ CPU
  └──────────┘  (tĩnh)    └───────────┘ (động)  └──────────┘
   con người      kiểm tra kiểu       nạp+verify+JIT
```

## ⚠️ Cạm bẫy & lưu ý senior
- Sai một chữ trong chữ ký `main` (vd `String args` thay vì `String[] args`) → biên dịch OK
  nhưng runtime báo `NoSuchMethodError: main`.
- Tên class `public` **phải** trùng tên file, phân biệt hoa thường.
- Thời gian khởi động JVM (cold start) là chi phí thật trong serverless/CLI — đây là lý do
  có **CDS/AppCDS** và **GraalVM native image**. Hiểu vòng đời ở trên giúp bạn biết tối ưu chỗ nào.

## 🔗 Bài tiếp theo
👉 [02 — Variables & Data Types](../02-variables-datatypes)
