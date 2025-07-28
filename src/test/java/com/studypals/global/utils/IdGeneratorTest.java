package com.studypals.global.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IdGeneratorTest {

    /* ────────────────────────────────── */
    /*   실행 스위치                        */
    /*   true  → 모든 성능 테스트 수행       */
    /*   false → generate_success 만 수행   */
    /* ────────────────────────────────── */
    private static final boolean RUN_PERFORMANCE_TESTS = false;

    @Autowired
    private IdGenerator idGenerator;

    /* ────────────────── 기능 검증 (항상) ────────────────── */
    @Test
    void generate_success() throws Exception {
        long id = idGenerator.requestId(1, 2).get(100, TimeUnit.MILLISECONDS);
        assertThat(id).isPositive();
        printReport("generate_success", 1, Duration.ZERO, 1);
    }

    /* ──────────────── 성능 테스트 (수동) ──────────────── */

    @Test
    void testMaxPerMillisLimit() throws Exception {
        if (!RUN_PERFORMANCE_TESTS) return;
        int total = 300;
        Set<Long> ids = new HashSet<>();

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            ids.add(idGenerator.requestId(1, 2).get(100, TimeUnit.MILLISECONDS));
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        printReport("MaxPerMillis(300)", total, elapsed, ids.size());
    }

    @Test
    void testConcurrentGeneration_noDuplicates() throws Exception {
        if (!RUN_PERFORMANCE_TESTS) return;

        final int threads = 5, perThread = 200_000;
        final int total = threads * perThread;

        Set<Long> set = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        set.add(idGenerator.requestId(1, 2).get(100, TimeUnit.MILLISECONDS));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        pool.shutdown();
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        printReport("Concurrent(5×200000)", total, elapsed, set.size());
        assertThat(set).hasSize(total);
    }

    @Test
    void testSingleThreadPerformance() throws Exception {
        if (!RUN_PERFORMANCE_TESTS) return;

        final int total = 1_000_000;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            idGenerator.requestId(7, 2).get(100, TimeUnit.MILLISECONDS);
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        printReport("SingleThread(1m)", total, elapsed, total);
    }

    @Test
    void testOneSecondThroughput() throws Exception {
        if (!RUN_PERFORMANCE_TESTS) return;

        final int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        AtomicInteger counter = new AtomicInteger();
        Set<Long> ids = ConcurrentHashMap.newKeySet();

        long endAt = System.currentTimeMillis() + 1_000;
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    while (System.currentTimeMillis() < endAt) {
                        try {
                            long id = idGenerator.requestId(1, 2).get(100, TimeUnit.MILLISECONDS);
                            ids.add(id);
                            counter.incrementAndGet();
                        } catch (Exception ignored) {
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        pool.shutdown();

        printReport("1-Second-Burst", counter.get(), Duration.ofSeconds(1), ids.size());
    }

    /* ────────────── 출력 유틸 ────────────── */
    private static void printReport(String title, int requested, Duration elapsed, int unique) {
        System.out.println("\n─── [" + title + "] ────────────────────────────────────");
        System.out.printf("Requested        : %,d%n", requested);
        if (!elapsed.isZero()) {
            long ms = elapsed.toMillis();
            System.out.printf("Elapsed (ms)     : %,d%n", ms);
            System.out.printf("Throughput (TPS) : %,d%n", requested * 1000L / ms);
        }
        System.out.printf("Unique IDs       : %,d%n", unique);
        System.out.println("──────────────────────────────────────────────────────");
    }
}
