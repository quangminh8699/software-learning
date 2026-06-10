/*
 * BÀI 05: LOOPS — for, while, do-while, for-each, break/continue
 * ================================================================
 * WHAT  : Các dạng vòng lặp và cách JVM/JIT xử lý chúng.
 * WHY   : Vòng lặp là "điểm nóng" (hot loop) — nơi JIT tối ưu mạnh nhất
 *         (loop unrolling, bỏ bounds-check). Chọn đúng dạng = code rõ + nhanh.
 * HOW   : for-each trên Iterable biên dịch thành gọi iterator().hasNext()/next();
 *         for-each trên mảng biên dịch thành vòng for theo chỉ số (không tạo iterator).
 * WHEN  : Biết số lần -> for; theo điều kiện -> while; chạy ít nhất 1 lần -> do-while.
 * WHICH : for-each cho duyệt đọc; for chỉ số khi cần index hoặc sửa phần tử.
 */
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: for cổ điển (đếm) ===
        // ⚙️ Dưới nắp capo: JIT có thể "unroll" vòng đếm ngắn và loại bỏ
        //    kiểm tra biên (bounds-check elimination) khi chứng minh được an toàn.
        int sum = 0;
        for (int i = 1; i <= 5; i++) sum += i;
        System.out.println("Tổng 1..5 = " + sum);

        // === Phần 2: while & do-while ===
        int n = 8, steps = 0;
        while (n > 1) { n = (n % 2 == 0) ? n / 2 : 3 * n + 1; steps++; } // Collatz
        System.out.println("Collatz từ 8 -> 1 mất " + steps + " bước");

        int attempt = 0;
        do { attempt++; } while (attempt < 1);  // thân chạy ÍT NHẤT 1 lần
        System.out.println("do-while chạy ít nhất 1 lần, attempt = " + attempt);

        // === Phần 3: for-each trên MẢNG vs trên COLLECTION ===
        int[] arr = {10, 20, 30};
        // ⚙️ for-each trên MẢNG -> biên dịch thành for(int idx...) arr[idx] (KHÔNG tạo iterator)
        int total = 0;
        for (int v : arr) total += v;
        System.out.println("Tổng mảng = " + total);

        // ⚙️ for-each trên LIST -> biên dịch thành it.hasNext()/it.next() (CÓ tạo Iterator object)
        List<String> names = List.of("An", "Bình", "Cường");
        for (String name : names) System.out.println("Tên: " + name);

        // === Phần 4: break & continue, có nhãn (labeled) ===
        // ⚙️ break/continue là lệnh nhảy (goto) trong bytecode tới nhãn vòng lặp.
        outer:
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                if (i * j > 4) {
                    System.out.println("break outer tại i=" + i + ", j=" + j);
                    break outer;             // thoát CẢ hai vòng cùng lúc
                }
            }
        }

        // continue: bỏ qua phần còn lại của LẦN lặp hiện tại
        StringBuilder evens = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            if (i % 2 != 0) continue;        // bỏ số lẻ
            evens.append(i).append(' ');
        }
        System.out.println("Số chẵn 1..10: " + evens.toString().trim());

        // 🔍 java -Xint Main (tắt JIT) vs java Main trên vòng lặp lớn -> thấy chênh lệch tốc độ JIT.
    }
}
