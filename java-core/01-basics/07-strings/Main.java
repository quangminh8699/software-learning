/*
 * BÀI 07: STRINGS — Tính bất biến, String pool, StringBuilder
 * ================================================================
 * WHAT  : String là object BẤT BIẾN (immutable); cách JVM tái dùng chuỗi
 *         literal qua String Pool; vì sao nối chuỗi trong vòng lặp lại đắt.
 * WHY   : Immutability cho an toàn thread + cache hashCode + chia sẻ an toàn.
 *         Nhưng nối chuỗi sai cách tạo rác O(n^2) -> phải biết StringBuilder.
 * HOW   : Literal "abc" được intern vào String Pool (trong heap). Mỗi phép
 *         "a" + "b" lúc runtime tạo String MỚI; StringBuilder sửa tại chỗ mảng.
 * WHEN  : Nối nhiều mảnh động -> StringBuilder; literal cố định -> String thường.
 * WHICH : String (bất biến, an toàn) vs StringBuilder (đột biến, nhanh khi nối).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: String Pool & so sánh ==  vs equals ===
        // ⚙️ Dưới nắp capo: literal "Java" được INTERN -> mọi literal "Java"
        //    trỏ về CÙNG một object trong String Pool.
        String a = "Java";
        String b = "Java";              // cùng object pool -> a == b là true
        String c = new String("Java");  // `new` ÉP tạo object MỚI trên heap -> khác địa chỉ

        System.out.println("a == b (pool)      : " + (a == b));          // true
        System.out.println("a == c (new)       : " + (a == c));          // false
        System.out.println("a.equals(c) (nội dung): " + a.equals(c));    // true
        System.out.println("a == c.intern()    : " + (a == c.intern())); // true (kéo về pool)

        // === Phần 2: Immutability — "sửa" chuỗi luôn tạo object mới ===
        // ⚙️ String không có method nào sửa chính nó; toUpperCase tạo String MỚI.
        String s = "hello";
        String upper = s.toUpperCase();
        System.out.println("s gốc giữ nguyên   : " + s);     // hello (không đổi)
        System.out.println("kết quả mới        : " + upper); // HELLO

        // === Phần 3: Cạm bẫy nối chuỗi trong vòng lặp ===
        // ❌ CÁCH SAI: mỗi vòng tạo 1 String mới -> rác + sao chép -> O(n^2)
        String bad = "";
        for (int i = 0; i < 5; i++) bad += i;     // mỗi += tạo StringBuilder ẩn + String mới
        System.out.println("Nối kiểu += : " + bad);

        // ✅ CÁCH ĐÚNG: StringBuilder giữ MỘT mảng char, mở rộng khi cần -> O(n)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i); // sửa tại chỗ, không tạo String trung gian
        String good = sb.toString();
        System.out.println("StringBuilder: " + good);

        // === Phần 4: Các method hay dùng ===
        String csv = "an,binh,cuong";
        System.out.println("split    : " + java.util.Arrays.toString(csv.split(",")));
        System.out.println("substring: " + csv.substring(3, 7));
        System.out.println("indexOf  : " + csv.indexOf("binh"));
        System.out.println("replace  : " + csv.replace(",", " | "));

        // === Phần 5: Text block (Java 15+) — chuỗi nhiều dòng ===
        String json = """
                {
                  "name": "Java",
                  "version": 21
                }""";
        System.out.println("Text block:\n" + json);

        // 🔍 So sánh hiệu năng: nối 100k lần bằng += vs StringBuilder -> chênh lệch khổng lồ.
    }
}
