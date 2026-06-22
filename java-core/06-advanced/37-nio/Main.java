/*
 * BÀI 37: NIO — Path/Files, Buffer & Channel, mmap, mô hình hướng buffer
 * ================================================================
 * WHAT  : NIO (New I/O): API hiện đại với Path/Files (thao tác file/thư mục),
 *         và mô hình Buffer + Channel (hướng khối, có thể non-blocking, mmap).
 * WHY   : java.io hướng STREAM (1 chiều, blocking, từng byte). NIO hướng BUFFER
 *         (đọc/ghi khối, đổi chiều, channel hai chiều) + mmap (ánh xạ file vào RAM).
 * HOW   : Channel chuyển dữ liệu giữa file/socket và ByteBuffer. Buffer có
 *         position/limit/capacity; flip() chuyển từ chế độ ghi sang đọc.
 * WHEN  : File lớn, truy cập ngẫu nhiên, mmap; nền của framework non-blocking (Netty).
 * WHICH : Files (tiện, thao tác cao cấp) vs Channel/Buffer (kiểm soát thấp, hiệu năng).
 */
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {

        // === Phần 1: Path & Files — API thao tác file hiện đại ===
        Path dir = Files.createTempDirectory("nio-demo");
        Path file = dir.resolve("data.txt");          // ghép đường dẫn an toàn (không nối chuỗi)
        System.out.println("Path: " + file);
        System.out.println("fileName=" + file.getFileName() + ", parent=" + file.getParent());

        Files.writeString(file, "alpha\nbeta\ngamma\n");
        System.out.println("exists=" + Files.exists(file) + ", size=" + Files.size(file));

        // === Phần 2: Files.lines — đọc file như STREAM (lazy, không tải hết RAM) ===
        try (Stream<String> lines = Files.lines(file)) {
            long count = lines.filter(l -> l.length() == 5).count();
            System.out.println("Số dòng dài 5 ký tự: " + count);
        }

        // === Phần 3: Buffer + Channel — mô hình hướng khối ===
        // ⚙️ ByteBuffer có position/limit/capacity; ghi xong gọi flip() để đọc lại.
        try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
            ByteBuffer buf = ByteBuffer.allocate(16);   // đệm 16 byte
            int read = ch.read(buf);                    // channel -> buffer (đang ở chế độ GHI)
            buf.flip();                                  // chuyển sang chế độ ĐỌC: limit=position, position=0
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            System.out.println("Đọc " + read + " byte qua Channel: \""
                    + new String(bytes, StandardCharsets.UTF_8).replace("\n", "\\n") + "\"");
            System.out.printf("Buffer state: pos=%d limit=%d cap=%d%n",
                    buf.position(), buf.limit(), buf.capacity());
        }

        // === Phần 4: Memory-mapped file (mmap) — ánh xạ file vào không gian địa chỉ ===
        // ⚙️ Dưới nắp capo: mmap để KERNEL ánh xạ trang file vào RAM ảo; truy cập file
        //    như truy cập mảng bộ nhớ, không qua read/write syscall mỗi lần -> rất nhanh cho file lớn.
        try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
            MappedByteBuffer mbb = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
            char firstChar = (char) mbb.get(0);          // đọc trực tiếp như bộ nhớ
            System.out.println("mmap: ký tự đầu = '" + firstChar + "'");
        }

        // === Phần 5: Duyệt cây thư mục (walk) ===
        Files.writeString(dir.resolve("b.txt"), "x");
        try (Stream<Path> walk = Files.walk(dir)) {
            System.out.println("Cây thư mục:");
            walk.forEach(p -> System.out.println("  " + dir.relativize(p)));
        }

        // Dọn dẹp đệ quy.
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount())  // con trước, thư mục sau
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        }
        System.out.println("Đã dọn thư mục tạm.");
    }
}
