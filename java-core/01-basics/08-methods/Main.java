/*
 * BÀI 08: METHODS — Stack frame, overloading, varargs, đệ quy
 * ================================================================
 * WHAT  : Method = đơn vị code tái dùng; mỗi lời gọi tạo 1 STACK FRAME.
 * WHY   : Hiểu stack frame giải thích StackOverflowError, truyền tham số
 *         (pass-by-value), và vì sao overloading được quyết lúc compile.
 * HOW   : Gọi method -> JVM đẩy 1 frame mới (biến local + toán hạng + return addr)
 *         lên JVM Stack của thread; return -> pop frame đó ra.
 * WHEN  : Tách logic lặp lại / đặt tên cho một ý niệm.
 * WHICH : Overloading (cùng tên, khác tham số) -> chọn lúc COMPILE (static binding).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: Truyền tham số — Java LUÔN pass-by-value ===
        // ⚙️ Dưới nắp capo: với primitive, "value" là chính giá trị (bản sao).
        //    Với object, "value" là BẢN SAO của THAM CHIẾU (địa chỉ) -> không phải bản sao object.
        int x = 10;
        modifyPrimitive(x);
        System.out.println("primitive sau gọi: " + x + " (KHÔNG đổi - sao chép giá trị)");

        int[] arr = {1, 2, 3};
        modifyArray(arr);
        System.out.println("array sau gọi: " + arr[0] + " (ĐỔI - chung object qua tham chiếu sao chép)");

        // === Phần 2: Overloading — cùng tên, khác danh sách tham số ===
        // ⚙️ Compiler chọn overload theo KIỂU tham số lúc biên dịch (static binding).
        System.out.println(describe(42));        // -> bản int
        System.out.println(describe(3.14));      // -> bản double
        System.out.println(describe("xin chào")); // -> bản String

        // === Phần 3: Varargs — số tham số tuỳ ý ===
        // ⚙️ Dưới nắp capo: varargs là "đường cú pháp"; JVM gói các đối số thành 1 MẢNG.
        System.out.println("sum() = " + sum());
        System.out.println("sum(1,2,3) = " + sum(1, 2, 3));
        System.out.println("sum(10..50) = " + sum(10, 20, 30, 40, 50));

        // === Phần 4: Đệ quy & StackOverflow ===
        System.out.println("factorial(5) = " + factorial(5));
        // ⚙️ Mỗi lời gọi đệ quy đẩy 1 frame; đệ quy quá sâu -> tràn JVM Stack.
        try {
            deepRecursion(0);
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError: stack đã đầy frame (đệ quy không đáy)");
        }
    }

    // Pass-by-value: nhận BẢN SAO của 10 -> sửa không ảnh hưởng bên ngoài.
    static void modifyPrimitive(int n) { n = 999; }

    // Nhận BẢN SAO của THAM CHIẾU -> vẫn trỏ cùng mảng -> sửa phần tử thì bên ngoài thấy.
    static void modifyArray(int[] a) { a[0] = 999; }

    // 3 overload "describe" — phân biệt bởi kiểu tham số:
    static String describe(int v)    { return "int: " + v; }
    static String describe(double v) { return "double: " + v; }
    static String describe(String v) { return "String: \"" + v + "\""; }

    // Varargs: int... gói thành int[] khi gọi.
    static int sum(int... nums) {
        int total = 0;
        for (int n : nums) total += n;   // nums là một mảng thật sự
        return total;
    }

    // Đệ quy có đáy (base case n<=1) -> dừng đúng.
    static long factorial(int n) {
        if (n <= 1) return 1;            // base case
        return n * factorial(n - 1);     // mỗi lời gọi 1 frame mới
    }

    // Đệ quy KHÔNG đáy -> đẩy frame vô hạn -> StackOverflowError.
    static void deepRecursion(int depth) { deepRecursion(depth + 1); }
}
