/*
 * BÀI 35: CONCURRENT UTILITIES — CompletableFuture, concurrent collections, virtual threads
 * ================================================================
 * WHAT  : Bộ công cụ cao cấp của java.util.concurrent: CompletableFuture (async
 *         compose), ConcurrentHashMap (map thread-safe), CountDownLatch, và
 *         virtual threads (Java 21).
 * WHY   : Viết luồng bất đồng bộ KHÔNG CHẶN, ghép pipeline; cấu trúc dữ liệu
 *         chia sẻ an toàn mà không khoá toàn cục; song song hoá I/O quy mô lớn.
 * HOW   : CompletableFuture chạy task trên executor, cho phép thenApply/thenCompose
 *         nối tiếp khi xong. ConcurrentHashMap khoá theo segment/bin -> ít tranh chấp.
 * WHEN  : Async pipeline (gọi nhiều service), cache chia sẻ, fan-out I/O.
 * WHICH : CompletableFuture (compose async) vs Future (chỉ get chặn).
 */
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class Main {
    public static void main(String[] args) throws Exception {

        // === Phần 1: CompletableFuture — async, KHÔNG chặn, ghép pipeline ===
        // ⚙️ supplyAsync chạy trên ForkJoinPool.commonPool; thenApply nối khi xong.
        CompletableFuture<String> cf = CompletableFuture
                .supplyAsync(() -> { sleep(50); return "dữ-liệu"; })   // bước 1 (async)
                .thenApply(String::toUpperCase)                         // bước 2 (biến đổi)
                .thenApply(s -> s + "-đã-xử-lý");                       // bước 3
        System.out.println("CompletableFuture: " + cf.get());

        // thenCompose: nối hai async (flatMap cho future) -> tránh Future<Future<>>.
        CompletableFuture<Integer> chained = CompletableFuture
                .supplyAsync(() -> 10)
                .thenCompose(x -> CompletableFuture.supplyAsync(() -> x * 2));
        System.out.println("thenCompose: " + chained.get());

        // thenCombine: gộp kết quả 2 future ĐỘC LẬP (chạy song song).
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> { sleep(40); return 3; });
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> { sleep(40); return 4; });
        System.out.println("thenCombine (song song): " + a.thenCombine(b, Integer::sum).get());

        // exceptionally: xử lý lỗi trong pipeline async.
        String safe = CompletableFuture
                .supplyAsync(() -> { throw new RuntimeException("hỏng"); })
                .exceptionally(ex -> "phục hồi: " + ex.getMessage())
                .thenApply(Object::toString).get();
        System.out.println("exceptionally: " + safe);

        // === Phần 2: ConcurrentHashMap — map thread-safe, ít tranh chấp ===
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<?>> fs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            fs.add(pool.submit(() -> {
                for (int k = 0; k < 10_000; k++)
                    map.merge("count", 1, Integer::sum);   // merge nguyên tử
            }));
        }
        for (Future<?> f : fs) f.get();
        System.out.println("ConcurrentHashMap count (mong đợi 40000): " + map.get("count"));

        // === Phần 3: LongAdder — counter ghi nhiều, đọc ít: nhanh hơn AtomicLong ===
        LongAdder adder = new LongAdder();
        runParallel(pool, () -> { for (int i = 0; i < 100_000; i++) adder.increment(); }, 4);
        System.out.println("LongAdder (4x100k): " + adder.sum());

        // === Phần 4: CountDownLatch — chờ N việc cùng hoàn thành ===
        int n = 3;
        CountDownLatch latch = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            final int id = i;
            pool.submit(() -> { sleep(30); System.out.println("  task " + id + " xong"); latch.countDown(); });
        }
        latch.await();                       // CHẶN tới khi đếm về 0
        System.out.println("Tất cả " + n + " task đã xong (latch)");
        pool.shutdown();

        // === Phần 5: Virtual threads (Java 21) — hàng vạn task I/O rẻ ===
        try (var vexec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < 10_000; i++) {     // 10k virtual thread -> bình thường
                final int id = i;
                results.add(vexec.submit(() -> { sleep(1); return id; }));
            }
            int sum = 0; for (Future<Integer> f : results) sum += f.get();
            System.out.println("10.000 virtual threads, tổng id = " + sum);
        }
    }

    static void runParallel(ExecutorService pool, Runnable r, int n) throws Exception {
        List<Future<?>> fs = new ArrayList<>();
        for (int i = 0; i < n; i++) fs.add(pool.submit(r));
        for (Future<?> f : fs) f.get();
    }
    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
