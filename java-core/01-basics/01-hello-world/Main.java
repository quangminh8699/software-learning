/*
 * BÀI 01: HELLO WORLD — Cấu trúc chương trình & vòng đời thực thi
 * ================================================================
 * WHAT  : Chương trình Java nhỏ nhất chạy được + giải phẫu vòng đời
 *         từ mã nguồn .java -> bytecode .class -> JVM thực thi.
 * WHY   : Hiểu "điều gì xảy ra khi gõ java Main" là nền tảng để sau này
 *         debug class loading, classpath, và hiệu năng khởi động.
 * HOW   : javac biên dịch .java -> .class (bytecode trung lập nền tảng).
 *         JVM nạp class qua ClassLoader, xác minh bytecode (verifier),
 *         rồi Execution Engine (interpreter + JIT) thực thi từ main().
 * WHEN  : Mọi chương trình Java đều bắt đầu từ một method main() như dưới.
 * WHICH : Java 21 cho phép "instance main" rút gọn (preview), nhưng bản
 *         chuẩn dùng `public static void main(String[])` — ta dùng bản này.
 */
public class Main {

    // ⚙️ DƯỚI NẮP CAPO — vì sao chữ ký main PHẢI là dạng này:
    //  - public : JVM (ở ngoài package) phải gọi được -> phải public.
    //  - static : JVM gọi main TRƯỚC khi có bất kỳ object nào của Main
    //             => không cần `new Main()`, gọi thẳng trên class.
    //  - void   : JVM không nhận giá trị trả về; muốn báo lỗi -> System.exit(code).
    //  - String[] args : tham số dòng lệnh, JVM cấp phát mảng này trên HEAP.
    public static void main(String[] args) {

        // === Phần 1: Câu lệnh in đầu tiên ===
        // System  : một class trong java.lang (tự động import).
        // System.out : một object PrintStream tĩnh (đã mở sẵn stdout).
        // println : method ghi chuỗi + xuống dòng.
        // ⚙️ Dưới nắp capo: lời gọi này biên dịch thành bytecode `invokevirtual`
        //    -> gọi động qua bảng method của PrintStream.
        System.out.println("Hello, World!");

        // === Phần 2: Đọc tham số dòng lệnh ===
        // Chạy: java Main Alice  -> args[0] = "Alice"
        if (args.length > 0) {
            System.out.println("Xin chào, " + args[0] + "!");
        } else {
            System.out.println("(Mẹo: chạy `java Main TênBạn` để truyền tham số)");
        }

        // === Phần 3: Thông tin runtime — minh hoạ JVM đang chạy ta ===
        // ⚙️ Các giá trị này do JVM cung cấp lúc runtime, không nằm cứng trong code.
        System.out.println("Java version : " + System.getProperty("java.version"));
        System.out.println("JVM name     : " + System.getProperty("java.vm.name"));
        System.out.println("Số CPU core  : " + Runtime.getRuntime().availableProcessors());

        // 🔍 Thử nghiệm cho người học:
        //   javac Main.java        -> tạo Main.class
        //   javap -c -p Main       -> xem bytecode của main()
        //   java -Xint Main        -> chạy thuần interpreter (tắt JIT)
    }
}
