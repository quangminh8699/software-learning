/*
 * BÀI 04: CONTROL FLOW — if/else & switch (statement vs expression)
 * ================================================================
 * WHAT  : Rẽ nhánh điều kiện: if/else, switch cổ điển, switch expression (Java 14+).
 * WHY   : switch trên nhiều nhánh được JVM tối ưu bằng bảng nhảy -> nhanh hơn
 *         chuỗi if-else dài. switch expression loại bỏ bug "quên break".
 * HOW   : switch trên int/enum biên dịch thành `tableswitch` (nhảy O(1) theo
 *         chỉ số) hoặc `lookupswitch` (tìm nhị phân) tuỳ độ thưa của nhãn.
 * WHEN  : Nhiều nhánh rời rạc theo một giá trị -> switch; điều kiện phức hợp -> if.
 * WHICH : switch expression (->) thay cho switch statement khi cần TRẢ VỀ giá trị.
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: if / else if / else ===
        int hour = 14;
        if (hour < 12) {
            System.out.println("Buổi sáng");
        } else if (hour < 18) {
            System.out.println("Buổi chiều");
        } else {
            System.out.println("Buổi tối");
        }

        // === Phần 2: switch cổ điển — CHÚ Ý "fall-through" ===
        // ⚙️ Dưới nắp capo: thiếu `break`, luồng "rơi" xuống nhánh kế.
        //    Đây là nguồn bug kinh điển -> switch expression ra đời để diệt nó.
        int day = 3;
        String kind;
        switch (day) {
            case 1: case 2: case 3: case 4: case 5:
                kind = "Ngày làm việc";
                break;                       // thiếu dòng này -> rơi xuống case 6
            case 6: case 7:
                kind = "Cuối tuần";
                break;
            default:
                kind = "Không hợp lệ";
        }
        System.out.println("day=" + day + " -> " + kind);

        // === Phần 3: switch EXPRESSION (Java 14+) — trả về giá trị, không fall-through ===
        // ⚙️ Dạng `->` không rơi nhánh; compiler bắt buộc bao phủ hết -> an toàn hơn.
        String season = switch (4) {
            case 12, 1, 2 -> "Đông";
            case 3, 4, 5  -> "Xuân";
            case 6, 7, 8  -> "Hạ";
            default       -> "Thu";
        };
        System.out.println("Tháng 4 -> mùa " + season);

        // === Phần 4: switch trả về qua `yield` (khối nhiều câu lệnh) ===
        int code = 2;
        String msg = switch (code) {
            case 1 -> "OK";
            case 2 -> {
                String detail = "tải đang cao";   // cần xử lý nhiều dòng
                yield "BUSY: " + detail;           // yield = "giá trị trả về" của khối
            }
            default -> "UNKNOWN";
        };
        System.out.println("code=" + code + " -> " + msg);

        // 🔍 javap -c Main  -> tìm `tableswitch` (nhãn liền) hoặc `lookupswitch` (nhãn thưa).
        //    switch(1..4) liền nhau -> JVM thường dùng tableswitch: nhảy O(1) theo offset.
    }
}
