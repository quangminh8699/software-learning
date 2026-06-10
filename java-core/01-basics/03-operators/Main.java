/*
 * BÀI 03: OPERATORS — Toán tử số học, logic, bitwise, ternary
 * ================================================================
 * WHAT  : Các nhóm toán tử và cách chúng biên dịch thành lệnh bytecode.
 * WHY   : Toán tử bitwise/shift là công cụ tối ưu (cờ bit, hash, *2/2).
 *         Short-circuit (&&, ||) ảnh hưởng cả tính đúng lẫn hiệu năng.
 * HOW   : Toán tử số học map gần như 1-1 sang lệnh ALU (iadd, imul...).
 *         Bitwise thao tác trực tiếp trên bit của số bù 2.
 * WHEN  : Bitwise cho tập cờ/bitmask; ternary cho gán có điều kiện gọn.
 * WHICH : & vs && — & luôn tính cả hai vế; && short-circuit (dừng sớm).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: Số học & chia nguyên ===
        // ⚙️ Dưới nắp capo: 7/2 dùng lệnh `idiv` -> chia NGUYÊN, bỏ phần dư.
        System.out.println("7 / 2   = " + (7 / 2));    // 3 (chia nguyên)
        System.out.println("7 % 2   = " + (7 % 2));    // 1 (phần dư)
        System.out.println("7.0 / 2 = " + (7.0 / 2));  // 3.5 (có 1 vế double -> chia thực)

        // === Phần 2: Short-circuit (&&, ||) ===
        // ⚙️ && dừng ngay khi vế trái false -> vế phải KHÔNG chạy.
        //    Đây là cách tránh NPE: obj != null && obj.isReady()
        int[] data = null;
        boolean safe = (data != null) && (data.length > 0); // không NPE nhờ short-circuit
        System.out.println("safe (short-circuit) = " + safe);

        // === Phần 3: Bitwise & shift — thao tác trên bit ===
        // ⚙️ Dưới nắp capo: số nguyên lưu dạng bù 2; các phép này map sang
        //    iand/ior/ixor/ishl/ishr/iushr — cực nhanh ở mức CPU.
        int a = 0b1100; // 12
        int x = 0b1010; // 10
        System.out.println("12 & 10 = " + (a & x)); // 8  (AND bit)
        System.out.println("12 | 10 = " + (a | x)); // 14 (OR bit)
        System.out.println("12 ^ 10 = " + (a ^ x)); // 6  (XOR bit)
        System.out.println("12 << 1 = " + (a << 1));// 24 (dịch trái = *2)
        System.out.println("12 >> 1 = " + (a >> 1));// 6  (dịch phải = /2, giữ dấu)

        // >>> dịch phải KHÔNG dấu (điền 0 vào bit cao) — khác >> với số âm:
        System.out.println("-8 >> 1  = " + (-8 >> 1));   // -4 (giữ dấu)
        System.out.println("-8 >>> 1 = " + (-8 >>> 1));  // số dương rất lớn

        // === Phần 4: Bitmask — ứng dụng thực tế của bitwise ===
        // Gói nhiều cờ boolean vào một int thay vì nhiều biến.
        final int READ = 1, WRITE = 1 << 1, EXEC = 1 << 2; // 1, 2, 4
        int perms = READ | WRITE;                          // bật READ + WRITE
        boolean canWrite = (perms & WRITE) != 0;           // kiểm tra cờ
        System.out.println("Có quyền WRITE? " + canWrite);

        // === Phần 5: Ternary — biểu thức điều kiện ===
        // ⚙️ Là EXPRESSION (trả về giá trị), khác if (statement).
        int score = 75;
        String grade = (score >= 60) ? "Đạt" : "Trượt";
        System.out.println("Kết quả: " + grade);

        // === Phần 6: Tăng/giảm & thứ tự đánh giá ===
        int n = 5;
        System.out.println("n++ trả về " + (n++) + ", sau đó n = " + n); // post: dùng rồi tăng
        System.out.println("++n trả về " + (++n));                       // pre: tăng rồi dùng
    }
}
