/*
 * BÀI 21: WRAPPER & AUTOBOXING — Integer vs int, cache, cạm bẫy hiệu năng
 * ================================================================
 * WHAT  : Wrapper class (Integer, Long...) bọc primitive thành OBJECT; autoboxing/
 *         unboxing là chuyển đổi ngầm giữa primitive và wrapper.
 * WHY   : Collections/Generics chỉ làm việc với OBJECT, không với primitive ->
 *         cần wrapper. Nhưng autobox ngầm tạo object trên heap -> bẫy hiệu năng + NPE.
 * HOW   : autobox -> Integer.valueOf(i); unbox -> i.intValue(). valueOf CACHE
 *         các giá trị nhỏ (-128..127) -> == đôi khi true, đôi khi false (bẫy!).
 * WHEN  : Wrapper khi cần object (Generics, null-able); primitive cho tính toán nóng.
 * WHICH : int (rẻ, không null) vs Integer (object, null-able, vào được Collection).
 */
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: Autoboxing / Unboxing ngầm ===
        // ⚙️ Dưới nắp capo: dòng dưới biên dịch thành Integer.valueOf(10).
        Integer boxed = 10;          // autobox: int -> Integer (tạo/đẩy về cache object)
        int unboxed = boxed;         // unbox: Integer -> int (boxed.intValue())
        System.out.println("boxed=" + boxed + ", unboxed=" + unboxed);

        // === Phần 2: BẪY Integer cache (-128..127) với == ===
        // ⚙️ valueOf CACHE [-128,127] -> trả CÙNG object -> == true.
        //    Ngoài khoảng đó -> object MỚI -> == false. Đây là bug ẩn kinh điển.
        Integer a1 = 127, a2 = 127;
        Integer b1 = 128, b2 = 128;
        System.out.println("127 == 127 ? " + (a1 == a2));   // true  (trong cache)
        System.out.println("128 == 128 ? " + (b1 == b2));   // false (ngoài cache!)
        System.out.println("equals luôn đúng: " + b1.equals(b2)); // true (so giá trị)

        // === Phần 3: BẪY NullPointerException khi unbox null ===
        Integer maybeNull = null;
        try {
            int x = maybeNull;        // unbox null -> null.intValue() -> NPE
            System.out.println(x);
        } catch (NullPointerException e) {
            System.out.println("Unbox null -> NullPointerException (bẫy ẩn!)");
        }

        // === Phần 4: BẪY hiệu năng — autobox trong vòng lặp ===
        // ❌ Dùng Long (wrapper) làm accumulator -> mỗi += tạo object mới trên heap.
        Long sumBoxed = 0L;
        for (long i = 0; i < 1_000_000; i++) sumBoxed += i;  // ~1 triệu Long object rác!
        System.out.println("sumBoxed = " + sumBoxed + "  (đã tạo cả triệu object thừa)");

        // ✅ Dùng primitive long -> 0 object, nhanh hơn nhiều.
        long sumPrim = 0L;
        for (long i = 0; i < 1_000_000; i++) sumPrim += i;
        System.out.println("sumPrim  = " + sumPrim + "  (không tạo object)");

        // === Phần 5: Wrapper là cầu nối vào Collections/Generics ===
        List<Integer> list = new ArrayList<>();   // không thể List<int>
        list.add(5);                              // autobox 5 -> Integer
        int first = list.get(0);                  // unbox
        System.out.println("List<Integer> phần tử đầu = " + first);

        // Tiện ích parse/box:
        System.out.println("parseInt: " + Integer.parseInt("42"));
        System.out.println("toBinaryString(10): " + Integer.toBinaryString(10));
        System.out.println("Integer.MAX_VALUE: " + Integer.MAX_VALUE);
    }
}
