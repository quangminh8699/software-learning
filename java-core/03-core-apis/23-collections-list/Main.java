/*
 * BÀI 23: COLLECTIONS — List: ArrayList vs LinkedList, độ phức tạp, mở rộng
 * ================================================================
 * WHAT  : List = chuỗi phần tử có thứ tự, cho phép trùng. Hai cài đặt chính:
 *         ArrayList (mảng động) và LinkedList (danh sách liên kết đôi).
 * WHY   : Chọn sai cài đặt -> hiệu năng tệ. Hiểu cấu trúc bên trong = chọn đúng.
 * HOW   : ArrayList bọc một MẢNG -> truy cập index O(1), chèn giữa O(n) + resize.
 *         LinkedList là các Node nối nhau -> chèn/xoá đầu-cuối O(1), truy cập O(n).
 * WHEN  : ArrayList ~99% trường hợp; LinkedList chỉ khi chèn/xoá ĐẦU nhiều.
 * WHICH : ArrayList (cache-friendly, mặc định) vs LinkedList (deque, ít dùng).
 */
import java.util.*;

public class Main {
    public static void main(String[] args) {

        // === Phần 1: ArrayList — mảng động ===
        List<String> fruits = new ArrayList<>();
        fruits.add("táo"); fruits.add("chuối"); fruits.add("cam");
        // ⚙️ get(i) = arr[i] -> O(1). add cuối = amortized O(1) (thỉnh thoảng resize).
        System.out.println("get(1) O(1): " + fruits.get(1));
        fruits.add(1, "xoài");        // chèn GIỮA -> dịch phần tử -> O(n)
        System.out.println("Sau chèn giữa: " + fruits);

        // === Phần 2: Resize của ArrayList — vì sao add đôi khi đắt ===
        // ⚙️ Khi đầy, ArrayList tạo mảng mới ~1.5x rồi Arrays.copyOf -> O(n) lần đó,
        //    nhưng phân bổ đều ra nhiều lần add -> AMORTIZED O(1).
        List<Integer> nums = new ArrayList<>(4);     // gợi ý dung lượng ban đầu -> giảm resize
        for (int i = 0; i < 10; i++) nums.add(i);
        System.out.println("nums = " + nums);

        // === Phần 3: LinkedList — danh sách liên kết đôi, mạnh ở 2 đầu ===
        LinkedList<Integer> dq = new LinkedList<>();
        dq.addFirst(1); dq.addLast(2); dq.addFirst(0);   // O(1) ở hai đầu
        System.out.println("LinkedList như deque: " + dq);
        System.out.println("peekFirst=" + dq.peekFirst() + ", peekLast=" + dq.peekLast());

        // === Phần 4: Duyệt & sửa an toàn — tránh ConcurrentModificationException ===
        List<Integer> data = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        // ❌ for-each + remove -> ConcurrentModificationException.
        // ✅ removeIf (Java 8) hoặc Iterator.remove():
        data.removeIf(x -> x % 2 == 0);     // xoá số chẵn an toàn
        System.out.println("Sau removeIf chẵn: " + data);

        // === Phần 5: Tiện ích Collections ===
        List<Integer> list = new ArrayList<>(List.of(5, 3, 9, 1));
        Collections.sort(list);                       // sort tăng dần
        System.out.println("sort: " + list);
        System.out.println("binarySearch(9): " + Collections.binarySearch(list, 9));
        System.out.println("max: " + Collections.max(list));

        // List bất biến (Java 9+): List.of -> không add/remove được.
        List<String> immutable = List.of("a", "b");
        try { immutable.add("c"); } catch (UnsupportedOperationException e) {
            System.out.println("List.of là bất biến -> không add được");
        }
    }
}
