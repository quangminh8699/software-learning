/*
 * BÀI 34: EXECUTOR FRAMEWORK — Thread pool, Callable/Future, tách "việc" khỏi "luồng"
 * ================================================================
 * WHAT  : ExecutorService quản lý một POOL thread tái dùng; nhận task (Runnable/
 *         Callable) qua hàng đợi; Future là "tay cầm" cho kết quả tương lai.
 * WHY   : Tạo thread thủ công rất đắt (~1MB stack + syscall). Pool tái dùng thread,
 *         giới hạn số luồng -> kiểm soát tài nguyên, throughput ổn định.
 * HOW   : submit(task) -> đưa vào BlockingQueue -> worker thread rảnh lấy ra chạy ->
 *         điền kết quả vào Future. future.get() CHẶN tới khi xong.
 * WHEN  : Hầu hết mọi nhu cầu chạy đồng thời trong production.
 * WHICH : fixed (số luồng cố định) / cached (co giãn) / virtual (Java 21, triệu task).
 */
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {

        // === Phần 1: Fixed thread pool + Callable/Future ===
        ExecutorService pool = Executors.newFixedThreadPool(3);

        // Callable: như Runnable nhưng TRẢ VỀ giá trị + ném checked exception.
        Callable<Integer> task = () -> {
            Thread.sleep(50);
            return 42;
        };
        Future<Integer> future = pool.submit(task);   // submit -> nhận Future ngay (không chặn)
        System.out.println("Đã submit, làm việc khác trong lúc chờ...");
        Integer result = future.get();                // get() CHẶN tới khi có kết quả
        System.out.println("Kết quả Future = " + result);

        // === Phần 2: invokeAll — chạy nhiều task, gom kết quả ===
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int n = i;
            tasks.add(() -> n * n);
        }
        List<Future<Integer>> futures = pool.invokeAll(tasks);   // chạy + chờ tất cả
        int total = 0;
        for (Future<Integer> f : futures) total += f.get();
        System.out.println("Tổng bình phương 1..5 = " + total);

        // === Phần 3: Thread được TÁI DÙNG — in tên để thấy pool xoay vòng ===
        for (int i = 0; i < 6; i++) {
            pool.submit(() -> System.out.println("  chạy trên " + Thread.currentThread().getName()));
        }
        Thread.sleep(100);

        // === Phần 4: Tắt pool ĐÚNG CÁCH (graceful shutdown) ===
        pool.shutdown();                                   // không nhận task mới, chờ task đang chạy
        if (!pool.awaitTermination(2, TimeUnit.SECONDS)) { // chờ tối đa 2s
            pool.shutdownNow();                            // cưỡng bức nếu quá hạn
        }
        System.out.println("Pool đã tắt: " + pool.isTerminated());

        // === Phần 5: Scheduled executor — chạy định kỳ/trễ ===
        ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> sf = sched.schedule(
                () -> System.out.println("Chạy sau 100ms (scheduled)"), 100, TimeUnit.MILLISECONDS);
        sf.get();
        sched.shutdown();

        // === Phần 6: Virtual-thread-per-task executor (Java 21) — triệu task rẻ ===
        try (ExecutorService vpool = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> f = vpool.submit(() -> "xong trên " + Thread.currentThread());
            System.out.println("Virtual executor: " + f.get());
        } // try-with-resources tự shutdown (ExecutorService là AutoCloseable từ Java 19+)
    }
}
