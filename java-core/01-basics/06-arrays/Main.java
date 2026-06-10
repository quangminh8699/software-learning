/*
 * BÀI 06: ARRAYS — Mảng 1D/2D, bố cục bộ nhớ, lớp tiện ích Arrays
 * ================================================================
 * WHAT  : Mảng là object kích thước CỐ ĐỊNH chứa các phần tử cùng kiểu, liên tục.
 * WHY   : Mảng là khối xây dựng của mọi Collection (ArrayList bọc một mảng).
 *         Hiểu bố cục giúp lý giải vì sao truy cập theo index là O(1).
 * HOW   : Mảng nằm trên HEAP, có header + trường length + vùng phần tử LIÊN TỤC.
 *         Truy cập arr[i] = địa chỉ_gốc + i * kích_thước_phần_tử (số học con trỏ).
 * WHEN  : Khi kích thước cố định & cần truy cập index nhanh, ít overhead.
 * WHICH : Mảng (cố định, nhanh) vs ArrayList (động, tiện) — đánh đổi.
 */
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: Khởi tạo & truy cập ===
        // ⚙️ Dưới nắp capo: `new int[5]` cấp 1 object trên heap, phần tử khởi tạo = 0.
        //    Mảng object (String[]) khởi tạo = null.
        int[] nums = new int[5];          // {0,0,0,0,0}
        int[] primes = {2, 3, 5, 7, 11};  // khởi tạo trực tiếp
        nums[0] = 100;
        System.out.println("primes[2] = " + primes[2] + " (truy cập O(1))");
        System.out.println("length    = " + primes.length + " (trường, không phải method)");

        // === Phần 2: Bounds-check — an toàn bộ nhớ của Java ===
        // ⚙️ Mỗi truy cập arr[i] được JVM kiểm tra 0 <= i < length.
        //    Vượt biên -> ArrayIndexOutOfBoundsException, KHÔNG ghi đè bộ nhớ lạ (khác C).
        try {
            int bad = primes[10];
            System.out.println(bad);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Bị chặn truy cập ngoài biên: " + e.getMessage());
        }

        // === Phần 3: Mảng 2D — mảng-của-mảng (jagged), KHÔNG liền khối ===
        // ⚙️ Java không có mảng 2D "phẳng" như C; int[][] là mảng các tham chiếu
        //    tới những mảng 1D riêng -> các hàng có thể nằm rải rác trên heap.
        int[][] grid = {
            {1, 2, 3},
            {4, 5},          // hàng có độ dài khác -> "jagged" hợp lệ
            {6, 7, 8, 9}
        };
        System.out.println("grid[2][3] = " + grid[2][3]);
        System.out.println("Số hàng = " + grid.length + ", hàng 1 có " + grid[1].length + " phần tử");

        // === Phần 4: Lớp tiện ích java.util.Arrays ===
        int[] data = {5, 2, 8, 1, 9, 3};
        Arrays.sort(data);                                  // sắp xếp tại chỗ (dual-pivot quicksort)
        System.out.println("Đã sort: " + Arrays.toString(data));
        int idx = Arrays.binarySearch(data, 8);             // chỉ đúng khi đã sort
        System.out.println("Tìm 8 -> index " + idx);

        int[] copy = Arrays.copyOf(data, 8);                // sao chép + nới dài (phần thêm = 0)
        System.out.println("copyOf(8): " + Arrays.toString(copy));

        // === Phần 5: So sánh mảng — KHÔNG dùng == ===
        // ⚙️ == so địa chỉ (hai object khác nhau -> false dù nội dung giống).
        int[] a1 = {1, 2, 3}, a2 = {1, 2, 3};
        System.out.println("a1 == a2          : " + (a1 == a2));            // false (khác object)
        System.out.println("Arrays.equals     : " + Arrays.equals(a1, a2)); // true  (so nội dung)

        // 🔍 Mảng là nền của ArrayList: khi đầy, ArrayList tạo mảng lớn hơn rồi copyOf.
    }
}
