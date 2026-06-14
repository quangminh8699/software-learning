/*
 * BÀI 33: SYNCHRONIZATION & JAVA MEMORY MODEL — synchronized, volatile, Lock, happens-before
 * ================================================================
 * WHAT  : Cơ chế đồng bộ để chống race condition + đảm bảo VISIBILITY giữa thread:
 *         synchronized (mutual exclusion + memory barrier), volatile (visibility),
 *         Atomic (CAS), Lock (linh hoạt). Nền tảng: Java Memory Model (happens-before).
 * WHY   : Không đồng bộ -> không chỉ sai do xen kẽ (atomicity) mà còn do thread KHÔNG
 *         THẤY thay đổi của nhau (cache CPU / reorder). JMM định nghĩa khi nào thấy.
 * HOW   : synchronized lấy monitor -> flush/invalidate cache (memory barrier) ->
 *         thiết lập quan hệ happens-before. volatile đảm bảo đọc/ghi thấy ngay + cấm reorder.
 * WHEN  : synchronized cho khối phức hợp; volatile cho cờ/visibility; Atomic cho counter.
 * WHICH : volatile (visibility, KHÔNG atomicity ghép) vs synchronized/Atomic (atomicity).
 */
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        // === Phần 1: synchronized -> đếm ĐÚNG (atomicity + visibility) ===
        SyncCounter sc = new SyncCounter();
        runTwoThreads(() -> { for (int i = 0; i < 100_000; i++) sc.inc(); });
        System.out.println("synchronized counter (mong đợi 200000): " + sc.get());

        // === Phần 2: AtomicInteger -> CAS lock-free, nhanh cho counter ===
        AtomicInteger atomic = new AtomicInteger();
        runTwoThreads(() -> { for (int i = 0; i < 100_000; i++) atomic.incrementAndGet(); });
        System.out.println("AtomicInteger (CAS) counter: " + atomic.get());

        // === Phần 3: ReentrantLock -> đồng bộ linh hoạt (tryLock, fair, nhiều condition) ===
        LockCounter lc = new LockCounter();
        runTwoThreads(() -> { for (int i = 0; i < 100_000; i++) lc.inc(); });
        System.out.println("ReentrantLock counter: " + lc.get());

        // === Phần 4: volatile -> VISIBILITY của cờ dừng (không đảm bảo atomicity ghép) ===
        // ⚙️ Không volatile, thread con có thể đọc cờ từ CACHE cũ -> chạy MÃI.
        Flag flag = new Flag();
        Thread worker = new Thread(() -> {
            long c = 0;
            while (flag.running) c++;     // running là volatile -> thấy thay đổi ngay
            System.out.println("Worker dừng sau khi thấy cờ, đếm = " + c);
        });
        worker.start();
        Thread.sleep(50);
        flag.running = false;             // ghi volatile -> worker thấy & thoát
        worker.join();

        // === Phần 5: happens-before demo (qua synchronized) ===
        System.out.println("Tất cả counter ĐÚNG vì synchronized/Atomic/Lock thiết lập happens-before.");
    }

    static void runTwoThreads(Runnable r) throws InterruptedException {
        Thread a = new Thread(r), b = new Thread(r);
        a.start(); b.start(); a.join(); b.join();
    }
}

class SyncCounter {
    private int value = 0;
    // synchronized: chỉ 1 thread giữ monitor của `this` tại 1 thời điểm.
    // ⚙️ Vào synchronized = acquire (invalidate cache, đọc mới); ra = release (flush ra RAM).
    synchronized void inc() { value++; }
    synchronized int get() { return value; }
}

class LockCounter {
    private int value = 0;
    private final ReentrantLock lock = new ReentrantLock();
    void inc() {
        lock.lock();                      // tường minh hơn synchronized
        try { value++; } finally { lock.unlock(); }   // PHẢI unlock trong finally
    }
    int get() { lock.lock(); try { return value; } finally { lock.unlock(); } }
}

class Flag {
    // volatile: mọi đọc/ghi đi thẳng bộ nhớ chính + cấm reorder -> thread khác thấy NGAY.
    volatile boolean running = true;
}
