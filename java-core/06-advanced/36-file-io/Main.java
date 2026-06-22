/*
 * BÀI 36: FILE I/O — Byte stream vs Char stream, buffering, vì sao buffer quan trọng
 * ================================================================
 * WHAT  : I/O cổ điển (java.io): InputStream/OutputStream (byte), Reader/Writer (char),
 *         và lớp buffer bọc ngoài để giảm số lần gọi hệ thống (syscall).
 * WHY   : Mỗi lần đọc/ghi 1 byte tới đĩa = 1 syscall (rất đắt). Buffer gom thành
 *         khối lớn -> ít syscall -> nhanh gấp nhiều lần. Char stream xử lý encoding.
 * HOW   : Stream là chuỗi byte/char tuần tự. Buffer giữ mảng đệm trong RAM, chỉ
 *         chạm đĩa khi đầy/flush. try-with-resources đảm bảo đóng (flush) đúng lúc.
 * WHEN  : Đọc/ghi file, network, console. Buffer LUÔN nên dùng cho I/O lượng lớn.
 * WHICH : byte stream (nhị phân: ảnh, file) vs char stream (văn bản, có encoding).
 */
import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) throws IOException {

        Path tmp = Files.createTempFile("javacore", ".txt");
        System.out.println("File tạm: " + tmp);

        // === Phần 1: Ghi văn bản có BUFFER (char stream) ===
        // ⚙️ BufferedWriter gom ký tự vào mảng đệm; chỉ ghi xuống đĩa khi đầy/flush/close.
        try (BufferedWriter bw = Files.newBufferedWriter(tmp)) {
            for (int i = 1; i <= 5; i++) {
                bw.write("Dòng " + i);
                bw.newLine();               // ký tự xuống dòng theo nền tảng
            }
        } // close() -> flush đệm + đóng tài nguyên (giải phóng file handle)

        // === Phần 2: Đọc văn bản có BUFFER ===
        System.out.println("--- Đọc từng dòng ---");
        try (BufferedReader br = Files.newBufferedReader(tmp)) {
            String line;
            while ((line = br.readLine()) != null) {   // readLine: 1 dòng/lần, đã buffer
                System.out.println("  " + line);
            }
        }

        // === Phần 3: Tiện ích Files đọc/ghi gọn (Java 11+) ===
        Files.writeString(tmp, "Xin chào\nFile I/O\n");
        String all = Files.readString(tmp);
        System.out.print("readString:\n" + all);
        System.out.println("readAllLines: " + Files.readAllLines(tmp));

        // === Phần 4: Byte stream — dữ liệu nhị phân ===
        Path bin = Files.createTempFile("javacore", ".bin");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(bin))) {
            for (int b = 0; b < 256; b++) os.write(b);   // ghi 256 byte 0..255
        }
        try (InputStream is = new BufferedInputStream(Files.newInputStream(bin))) {
            int first = is.read();                       // đọc 1 byte (0..255), -1 = hết
            System.out.println("Byte đầu = " + first + ", tổng byte = " + Files.size(bin));
        }

        // === Phần 5: Vì sao buffer? Minh hoạ khái niệm (không buffer = nhiều syscall) ===
        System.out.println("⚙️ Buffer gom dữ liệu -> ít syscall -> nhanh hơn nhiều lần.");

        // Dọn dẹp
        Files.deleteIfExists(tmp);
        Files.deleteIfExists(bin);
        System.out.println("Đã xoá file tạm.");
    }
}
