/*
 * BÀI 32: THREADS BASICS — Thread, Runnable, vòng đời, ánh xạ OS thread
 * ================================================================
 * WHAT  : Thread = luồng thực thi độc lập trong cùng tiến trình, chia sẻ heap.
 * WHY   : Tận dụng nhiều core; làm việc song song/không chặn (I/O, tính toán).
 * HOW   : Mỗi Thread (platform) ánh xạ 1-1 tới một OS thread; có STACK riêng
 *         nhưng CHUNG heap -> chia sẻ dữ liệu (và rủi ro race condition).
 * WHEN  : Khi cần làm nhiều việc đồng thời; nhưng nay ưu tiên ExecutorService (34).
 * WHICH : extends Thread (chiếm suất kế thừa) vs implements Runnable (ưu tiên).
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {

        // === Phần 1: Tạo thread bằng Runnable (ưu tiên hơn extends Thread) ===
        Runnable task = () -> {
            // ⚙️ Thread.currentThread() cho biết ta đang ở luồng nào.
            System.out.println("Chạy trong: " + Thread.currentThread().getName());
        };
        Thread t1 = new Thread(task, "worker-1");
        t1.start();          // start() -> tạo OS thread mới rồi gọi run(). KHÔNG gọi run() trực tiếp!
        t1.join();           // main CHỜ t1 xong (đồng bộ hoá kết thúc)

        // ⚠️ Bẫy: gọi run() trực tiếp -> chạy NGAY trên thread main, KHÔNG tạo thread mới.
        new Thread(task, "không-dùng").run();   // in ra "main" -> minh hoạ bẫy

        // === Phần 2: Nhiều thread chạy đồng thời, xen kẽ không xác định ===
        Thread[] threads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                for (int k = 0; k < 2; k++)
                    System.out.println("Thread-" + id + " bước " + k);
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();   // chờ tất cả xong

        // === Phần 3: RACE CONDITION — vì sao cần đồng bộ (xem bài 33) ===
        // ⚙️ counter++ KHÔNG nguyên tử (read-modify-write) -> nhiều thread đè nhau -> mất cập nhật.
        Counter unsafe = new Counter();
        Thread a = new Thread(() -> { for (int i = 0; i < 100_000; i++) unsafe.inc(); });
        Thread b = new Thread(() -> { for (int i = 0; i < 100_000; i++) unsafe.inc(); });
        a.start(); b.start(); a.join(); b.join();
        System.out.println("Counter KHÔNG đồng bộ (mong đợi 200000): " + unsafe.value
                + "  <- thường NHỎ HƠN do race condition");

        // === Phần 4: daemon thread & trạng thái ===
        Thread daemon = new Thread(() -> { while (true) {} });
        daemon.setDaemon(true);     // daemon: JVM thoát không cần chờ nó
        System.out.println("Thread state trước start: " + daemon.getState()); // NEW
        // (không start để khỏi treo) — chỉ minh hoạ trạng thái

        // === Phần 5: virtual thread (Java 21) — nhẹ, không map 1-1 OS thread ===
        Thread vt = Thread.ofVirtual().name("vthread").start(() ->
                System.out.println("Virtual thread: " + Thread.currentThread()));
        vt.join();
    }
}

class Counter {
    int value = 0;                 // KHÔNG volatile, KHÔNG khoá -> không an toàn
    void inc() { value++; }        // value++ = đọc + cộng + ghi (3 bước, không nguyên tử)
}
