package com.studypals.global.redis.redisHashRepository;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypals.global.redis.redisHashRepository.annotations.EnableRedisHashRepositories;
import com.studypals.testModules.testComponent.TestRedisHashEntity;
import com.studypals.testModules.testComponent.TestRedisHashRepository;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-07-19
 */
@SpringBootTest
@ActiveProfiles("test")
@EnableRedisHashRepositories(basePackageClasses = TestRedisHashRepository.class)
public class RedisHashPerformanceTest {

    private static final boolean RUN_PERFORMANCE_TESTS = true;

    @Autowired
    private TestRedisHashRepository testRedisHashRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void redisHash_CRD_performanceTest() throws Exception {
        if (!RUN_PERFORMANCE_TESTS) return;

        int total = 50_000;

        long start = System.nanoTime();
        int batchSize = 512;
        List<TestRedisHashEntity> buffer = new ArrayList<>(batchSize);

        for (int i = 1; i <= total; i++) {
            TestRedisHashEntity entity = new TestRedisHashEntity(String.valueOf(i), i + " name", i + 1, null);
            buffer.add(entity);

            if (buffer.size() == batchSize) {
                testRedisHashRepository.saveAll(buffer);
                buffer.clear(); // 버퍼 비우기
            }
        }
        if (!buffer.isEmpty()) {
            testRedisHashRepository.saveAll(buffer);
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        start = System.nanoTime();

        List<TestRedisHashEntity> answerList = (List<TestRedisHashEntity>) testRedisHashRepository.findAllById(
                IntStream.rangeClosed(1, total).mapToObj(Integer::toString).collect(Collectors.toList()));
        Duration callElapsed = Duration.ofNanos(System.nanoTime() - start);

        start = System.nanoTime();
        testRedisHashRepository.deleteAll(
                IntStream.range(1, total).mapToObj(Integer::toString).collect(Collectors.toSet()));
        Duration delElapsed = Duration.ofNanos(System.nanoTime() - start);

        printReport("Redis Hash performance test by 50_000 time save", total, elapsed, answerList.size());

        printReport("Redis Hash performance test by 50_000 time call", total, callElapsed, answerList.size());

        printReport("Redis Hash performance test by 50_000 time delete", total, delElapsed, -1);

        start = System.nanoTime();
        for (int i = 1; i <= total; i++) {
            redisTemplate.opsForValue().set("raw:string:" + i, Integer.toString(i));
        }
        Duration rawSaveElapsed = Duration.ofNanos(System.nanoTime() - start);

        start = System.nanoTime();
        Set<String> resultSet = new HashSet<>();
        for (int i = 1; i <= total; i++) {
            String val = (String) redisTemplate.opsForValue().get("raw:string:" + i);
            resultSet.add(val);
        }
        Duration rawCallElapsed = Duration.ofNanos(System.nanoTime() - start);

        start = System.nanoTime();
        for (int i = 1; i <= total; i++) {
            redisTemplate.delete("raw:string:" + i);
        }
        Duration rawDelElapsed = Duration.ofNanos(System.nanoTime() - start);

        printReport("Redis raw key-value save (opsForValue)", total, rawSaveElapsed, resultSet.size());
        printReport("Redis raw key-value call (opsForValue)", total, rawCallElapsed, resultSet.size());
        printReport("Redis raw key-value delete (opsForValue)", total, rawDelElapsed, -1);
    }

    @Test
    void redisHashMap_CRD_performanceTest() {
        if (!RUN_PERFORMANCE_TESTS) return;

        int total = 50_000;
    }

    private static void printReport(String title, int request, Duration elapsed, int success) {
        long ms = elapsed.toMillis();
        double seconds = elapsed.toNanos() / 1_000_000_000.0;
        double tps = seconds > 0 ? request / seconds : 0;

        int titleCnt = title.length() + 28;
        String line = "━".repeat(titleCnt);

        System.out.println("\n━━━━━━━━━━━ [ " + title + " ] ━━━━━━━━━━━");
        System.out.printf("요청 개수         : %,d건%n", request);
        System.out.printf("걸린 시간         : %,d ms (%.3f 초)%n", ms, seconds);
        System.out.printf("처리 속도 (TPS)   : %,.2f 건/초%n", tps);
        System.out.printf("성공 개수         : %,d건%n", success);
        System.out.println(line + "\n");
    }
}
