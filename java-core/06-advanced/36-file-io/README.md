# Bài 36: File I/O — Stream, buffering & vì sao syscall đắt

I/O chậm không phải vì đĩa — mà vì **mỗi lần chạm đĩa là một syscall**. Buffer là chìa khoá.

## 📖 Mô tả
`java.io` mô hình hoá I/O thành **stream** (chuỗi tuần tự): **byte stream** (`InputStream`/`OutputStream`) cho dữ liệu nhị phân, **char stream** (`Reader`/`Writer`) cho văn bản (có encoding). Lớp **Buffered\*** bọc ngoài để giảm syscall.

## 🔧 Kỹ thuật
| | Byte | Char |
|--|------|------|
| Đọc | `InputStream` | `Reader` |
| Ghi | `OutputStream` | `Writer` |
| Buffer | `BufferedInput/OutputStream` | `BufferedReader/Writer` |
| Dùng cho | ảnh, file nhị phân | text, encoding |

Tiện ích cấp cao: `Files.readString`, `Files.writeString`, `Files.readAllLines`, `Files.newBufferedReader`.

## ⚙️ Dưới nắp capo (Under the hood)
```
   KHÔNG buffer: write 1 byte → syscall → kernel → đĩa   (lặp N lần = N syscall)
   CÓ buffer:    write → [mảng đệm RAM 8KB] ─đầy/flush─▶ 1 syscall cho cả khối

   Ứng dụng ── JVM ── (syscall) ── Kernel ── Page cache ── Đĩa
                        ▲ đắt: chuyển user→kernel mode, ngắt
```
- **Mỗi syscall** (read/write) phải chuyển từ user-mode sang kernel-mode → tốn. Ghi từng byte = hàng triệu syscall → cực chậm. **Buffer** gom vào mảng (mặc định ~8KB) → chỉ syscall khi đầy/flush → giảm số syscall hàng nghìn lần.
- **`flush()`**: đẩy đệm xuống tầng dưới. `close()` tự flush. Quên flush/close → dữ liệu kẹt trong đệm, **mất** khi chương trình thoát.
- **Char stream** cần **charset** (UTF-8...) để chuyển byte↔ký tự. Đọc byte như text với charset sai → ký tự hỏng (mojibake). `Files.newBufferedReader` mặc định UTF-8.
- **try-with-resources** (bài 20): đảm bảo `close()` (→ flush) chạy kể cả khi có exception → không rò file handle (OS giới hạn số handle mở).
- Tầng kernel còn có **page cache** → ghi "thành công" chưa chắc đã xuống đĩa vật lý (cần `fsync`/`force` để bền thật).

## ▶️ Cách dùng
```bash
javac Main.java && java Main
```
Output mẫu:
```
File tạm: /var/folders/.../javacore....txt
--- Đọc từng dòng ---
  Dòng 1
  Dòng 2
  ...
readString:
Xin chào
File I/O
readAllLines: [Xin chào, File I/O]
Byte đầu = 0, tổng byte = 256
⚙️ Buffer gom dữ liệu -> ít syscall -> nhanh hơn nhiều lần.
Đã xoá file tạm.
```

## 🎨 Bản vẽ — decorator của I/O streams
```
   Files.newOutputStream(path)              ← nguồn thô (chạm đĩa)
        └─ BufferedOutputStream(...)         ← thêm đệm (giảm syscall)
             └─ DataOutputStream(...)        ← thêm khả năng ghi kiểu (int, double)
   (Đây chính là Decorator pattern — xem bài 46)
```

## ⚠️ Cạm bẫy & lưu ý senior
- **Luôn buffer** cho I/O lượng lớn; đọc/ghi từng byte không buffer = thảm hoạ hiệu năng.
- **Luôn try-with-resources** → tránh rò file handle (lỗi "Too many open files").
- **Charset tường minh** (UTF-8) → tránh phụ thuộc default OS, lỗi mojibake giữa môi trường.
- Đọc file lớn bằng `Files.readString`/`readAllLines` → tải hết vào RAM → OOM. Dùng `Files.lines` (stream) hoặc đọc theo dòng.
- "Ghi xong" ≠ "đã bền trên đĩa" → cần `force`/`fsync` cho dữ liệu quan trọng (DB, log).

## 🔗 Bài tiếp theo
👉 [37 — NIO](../37-nio)
