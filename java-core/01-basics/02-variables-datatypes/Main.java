/*
 * BÀI 02: VARIABLES & DATA TYPES — Kiểu dữ liệu & nơi biến sống
 * ================================================================
 * WHAT  : 8 kiểu nguyên thuỷ (primitive) vs kiểu tham chiếu (reference);
 *         biến local nằm ở đâu trong bộ nhớ; ép kiểu (casting).
 * WHY   : Chọn đúng kiểu = đúng phạm vi giá trị + đúng chi phí bộ nhớ.
 *         Nhầm primitive/reference dẫn tới bug NPE, autoboxing tốn kém.
 * HOW   : primitive lưu TRỰC TIẾP giá trị; reference lưu một "con trỏ"
 *         tới object trên heap. Biến local sống trên STACK frame.
 * WHEN  : Dùng primitive cho dữ liệu số/logic thuần; reference cho object.
 * WHICH : int vs long vs double — chọn theo phạm vi & độ chính xác cần.
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: 8 kiểu nguyên thuỷ (primitive) ===
        // ⚙️ Dưới nắp capo: mỗi biến này nằm TRỰC TIẾP trên stack frame của main,
        //    KHÔNG có object header, không qua heap -> rất rẻ.
        byte    b   = 127;                 // 1 byte,  -128..127
        short   s   = 32_000;              // 2 byte
        int     i   = 2_147_483_647;       // 4 byte, kiểu số nguyên mặc định
        long    lng = 9_000_000_000L;      // 8 byte, hậu tố L bắt buộc
        float   f   = 3.14f;               // 4 byte, hậu tố f; độ chính xác ~7 chữ số
        double  d   = 3.141592653589793;   // 8 byte, kiểu thực mặc định
        char    c   = 'A';                 // 2 byte, là số UTF-16 (65)
        boolean bool= true;                // JVM không định nghĩa kích thước cố định

        System.out.println("int max   = " + i);
        System.out.println("char 'A'  = " + (int) c + " (mã UTF-16)");
        System.out.println("long      = " + lng);

        // === Phần 2: Tràn số (overflow) — hệ quả của kích thước cố định ===
        // ⚙️ int là 32-bit bù 2; vượt max sẽ "quay vòng" về min, KHÔNG báo lỗi.
        int overflow = Integer.MAX_VALUE + 1;
        System.out.println("MAX_VALUE + 1 = " + overflow + " (overflow quay vòng!)");

        // === Phần 3: Kiểu tham chiếu (reference) ===
        // ⚙️ Dưới nắp capo: biến `name` nằm trên STACK nhưng chỉ chứa ĐỊA CHỈ;
        //    đối tượng String thật nằm trên HEAP. Đây là khác biệt cốt lõi.
        String name = "Java";          // String là object, không phải primitive
        int[]  arr  = {1, 2, 3};       // mảng cũng là object trên heap
        System.out.println("name -> heap object, độ dài = " + name.length());
        System.out.println("arr  -> heap object, phần tử [0] = " + arr[0]);

        // === Phần 4: Casting (ép kiểu) ===
        // Widening (nới rộng) — tự động, không mất dữ liệu:
        double fromInt = i;            // int -> double ngầm định
        // Narrowing (thu hẹp) — phải ép tay, CÓ THỂ mất dữ liệu:
        int    truncated = (int) 3.99; // -> 3 (cắt phần thập phân, không làm tròn)
        System.out.println("int->double: " + fromInt);
        System.out.println("(int)3.99 = " + truncated + " (cắt cụt, không làm tròn)");

        // === Phần 5: `var` (Java 10+) — suy luận kiểu lúc COMPILE ===
        // ⚙️ var KHÔNG phải kiểu động: trình biên dịch chốt kiểu tại chỗ.
        //    `message` vẫn là String 100%, chỉ là gõ ngắn hơn.
        var message = "var được suy luận thành String";
        System.out.println(message + " | kiểu thực: " + message.getClass().getSimpleName());

        // 🔍 Thử: javap -c -p Main  -> thấy biến local được đánh chỉ số slot trên stack frame.
    }
}
