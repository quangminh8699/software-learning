/*
 * BÀI 09: CLASSES & OBJECTS — Khuôn mẫu vs thực thể, `this`, vòng đời object
 * ================================================================
 * WHAT  : Class là KHUÔN (template); object là THỰC THỂ tạo từ khuôn, sống trên heap.
 * WHY   : OOP gói dữ liệu + hành vi vào một đơn vị -> mô hình hoá miền nghiệp vụ.
 * HOW   : `new Foo()` cấp bộ nhớ trên HEAP, khởi tạo field mặc định, chạy constructor,
 *         trả về THAM CHIẾU. Biến object trên stack chỉ giữ địa chỉ đó.
 * WHEN  : Khi cần nhiều thực thể có cùng cấu trúc nhưng trạng thái riêng.
 * WHICH : instance field (mỗi object 1 bản) vs static field (chia sẻ toàn class).
 */
public class Main {
    public static void main(String[] args) {

        // === Phần 1: Tạo object bằng `new` ===
        // ⚙️ Dưới nắp capo: `new` (1) cấp vùng nhớ trên heap cho các field,
        //    (2) gán giá trị mặc định (0/null/false), (3) chạy constructor, (4) trả tham chiếu.
        BankAccount a1 = new BankAccount("Alice", 100);
        BankAccount a2 = new BankAccount("Bob", 50);

        // a1 và a2 là HAI object riêng -> trạng thái độc lập.
        a1.deposit(40);
        a2.withdraw(20);
        a1.printStatus();
        a2.printStatus();

        // === Phần 2: Tham chiếu — hai biến cùng trỏ một object ===
        // ⚙️ alias = a1 KHÔNG sao chép object, chỉ sao chép ĐỊA CHỈ -> chung 1 object.
        BankAccount alias = a1;
        alias.deposit(1000);              // sửa qua alias...
        System.out.println("Qua a1 thấy số dư: " + a1.getBalance() + " (chung object với alias)");
        System.out.println("a1 == alias ? " + (a1 == alias)); // true (cùng địa chỉ)

        // === Phần 3: static field — chia sẻ toàn class ===
        // ⚙️ instanceCount nằm trong Method Area/Metaspace, KHÔNG thuộc object nào.
        System.out.println("Tổng số account đã tạo: " + BankAccount.getInstanceCount());

        // === Phần 4: Object "mất tham chiếu" -> ứng viên cho GC ===
        BankAccount temp = new BankAccount("Temp", 0);
        temp = null; // object "Temp" không còn ai trỏ tới -> GC có thể thu hồi
        System.out.println("temp = null -> object Temp đủ điều kiện bị GC dọn");
    }
}

// Class = khuôn mẫu: định nghĩa FIELD (trạng thái) + METHOD (hành vi).
class BankAccount {
    // instance field: MỖI object có bản riêng.
    private String owner;
    private double balance;

    // static field: MỘT bản dùng chung cho mọi object của class.
    private static int instanceCount = 0;

    // Constructor: khởi tạo trạng thái ban đầu của object.
    BankAccount(String owner, double balance) {
        this.owner = owner;          // `this` phân biệt field với tham số trùng tên
        this.balance = balance;
        instanceCount++;             // tăng bộ đếm dùng chung
    }

    void deposit(double amount) { this.balance += amount; }

    void withdraw(double amount) {
        if (amount > balance) { System.out.println(owner + ": không đủ số dư"); return; }
        this.balance -= amount;
    }

    double getBalance() { return balance; }
    void printStatus() { System.out.println(owner + " có số dư: " + balance); }
    static int getInstanceCount() { return instanceCount; }
}
