# Bài 37: NIO — Path/Files, Buffer/Channel & memory-mapped files

NIO không chỉ là "java.io mới" — nó đổi **mô hình**: từ stream (từng byte, blocking) sang **buffer + channel** (khối, đổi chiều, mmap, non-blocking).

## 📖 Mô tả
- **`Path`/`Files`** (NIO.2): API thao tác file/thư mục hiện đại, an toàn, nhiều tiện ích.
- **Buffer + Channel**: mô hình hướng khối; `Channel` chuyển dữ liệu vào/ra `ByteBuffer`.
- **mmap**: ánh xạ file vào không gian địa chỉ → truy cập như mảng RAM.

## 🔧 Kỹ thuật
| | java.io (cũ) | NIO |
|--|--------------|-----|
| Mô hình | stream (1 chiều) | buffer + channel (2 chiều) |
| Blocking | luôn | có thể non-blocking (Selector) |
| Đơn vị | byte/char tuần tự | khối trong buffer |
| File API | `File` | `Path`/`Files` (giàu hơn) |

`ByteBuffer`: `capacity` (cố định), `position` (con trỏ), `limit` (giới hạn), `flip()`/`clear()`/`rewind()`.

## ⚙️ Dưới nắp capo (Under the hood)
- **ByteBuffer state machine** (nguồn nhầm lẫn kinh điển):
```
   Sau allocate(16):   pos=0    limit=16  (chế độ GHI)
   ghi 6 byte:         pos=6    limit=16
   flip():             pos=0    limit=6   (chế độ ĐỌC: đọc đúng 6 byte đã ghi)
   clear():            pos=0    limit=16  (sẵn sàng ghi lại; dữ liệu cũ chưa xoá)
```
  → quên `flip()` trước khi đọc = đọc rác/0 byte.
- **Channel** là cầu hai chiều giữa nguồn (file/socket) và buffer; hỗ trợ **non-blocking** + `Selector` (1 thread quản nhiều kết nối) → nền của server hiệu năng cao (Netty, NIO của JDK).
- **Direct buffer** (`allocateDirect`): cấp ngoài heap (off-heap) → kernel đọc/ghi thẳng, tránh sao chép qua heap; nhưng cấp phát đắt → dùng cho buffer sống lâu.
- **Memory-mapped file (`map`)**: yêu cầu kernel ánh xạ các **trang** của file vào bộ nhớ ảo tiến trình. Truy cập = page fault nạp trang theo nhu cầu; không có read/write syscall mỗi lần → cực nhanh cho file **lớn**, truy cập **ngẫu nhiên**, hoặc chia sẻ giữa tiến trình. Là cơ chế đằng sau nhiều DB/log (Kafka).
- `Files.lines`/`Files.walk` trả **Stream lazy** + cần `try-with-resources` để đóng handle.

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
Path: /var/folders/.../nio-demo.../data.txt
fileName=data.txt, parent=/var/folders/.../nio-demo...
exists=true, size=16
Số dòng dài 5 ký tự: 2
Đọc 16 byte qua Channel: "alpha\nbeta\ngamm"
Buffer state: pos=16 limit=16 cap=16
mmap: ký tự đầu = 'a'
Cây thư mục:
  
  b.txt
  data.txt
Đã dọn thư mục tạm.
```

## 🎨 Bản vẽ — mmap
```
   Không gian địa chỉ ảo tiến trình
   ┌───────────────┬───────────────┐
   │  heap/stack   │ [trang file]  │◄── ánh xạ ──┐
   └───────────────┴───────────────┘             │
                    truy cập như RAM      ┌───────┴────┐
                    page fault ─────────▶ │  File trên │
                    nạp trang theo nhu cầu│   đĩa      │
                                          └────────────┘
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Quên `flip()`** giữa ghi và đọc buffer → bug số 1 của người mới dùng NIO.
- `Files.lines`/`walk` trả Stream giữ file handle mở → **phải** đóng (try-with-resources).
- **mmap không tự giải phóng** ngay khi GC buffer (phụ thuộc cleaner) → file có thể bị "khoá" trên Windows; cẩn thận với file tạm/đổi tên.
- Direct buffer tốn off-heap không nằm trong `-Xmx` → có thể OOM "ẩn" (`-XX:MaxDirectMemorySize`).
- Non-blocking NIO + Selector phức tạp → thường dùng framework (Netty) thay vì viết tay; hoặc nay dùng **virtual threads** (bài 35) để viết blocking mà vẫn scale.

## 🔗 Bài tiếp theo
👉 [38 — Annotations](../38-annotations)
