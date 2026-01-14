package com.studypals.domain.groupManage.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.studypals.domain.groupManage.entity.HashTag;
import com.studypals.testModules.testSupport.DataJpaSupport;

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
 * @since 2026-01-05
 */
class HashTagRepositoryTest extends DataJpaSupport {

    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    PlatformTransactionManager txManager;

    private HashTag insertHashTag(String tag) {
        return em.persist(HashTag.builder().tag(tag).build());
    }

    private TransactionTemplate tt;

    @BeforeEach
    void setUp() {
        tt = new TransactionTemplate(txManager);
    }

    @AfterEach
    void tearDown() {
        tt.execute(status -> {
            hashTagRepository.deleteAllInBatch();
            return null;
        });
    }

    @Test
    void search_success() {
        List<String> tags = List.of(
                "apple_pie",
                "apple_pan",
                "apple_jam",
                "apple_latte",
                "pan_apple_pan",
                "pineapple_pan",
                "appple_pan",
                "appleappleapple",
                "APPLE_BIG",
                "aple",
                "apple",
                "app_pie",
                "banana_pan");

        tags.forEach(this::insertHashTag);

        List<String> result = hashTagRepository.search("apple", Pageable.ofSize(20));

        assertThat(result).hasSize(9);
    }

    @Test
    void increaseUsedCountBulk_success() throws Exception {
        List<String> tags = List.of(
                "apple_pie",
                "apple_pan",
                "apple_jam",
                "apple_latte",
                "pan_apple_pan",
                "pineapple_pan",
                "appple_pan",
                "appleappleapple",
                "APPLE_BIG",
                "aple",
                "apple",
                "app_pie",
                "banana_pan");

        tags.forEach(
                tag -> hashTagRepository.saveAndFlush(HashTag.builder().tag(tag).build()));
        em.clear();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        int thread = 20;
        int callsPerThread = 50;
        int totalCall = thread * callsPerThread;

        ExecutorService pool = Executors.newFixedThreadPool(thread);
        CountDownLatch ready = new CountDownLatch(thread);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(thread);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < thread; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();

                    for (int j = 0; j < callsPerThread; j++) {
                        tt.execute(status -> {
                            hashTagRepository.increaseUsedCountBulk(tags);
                            return null;
                        });
                    }
                    return null;
                } finally {
                    done.countDown(); // 예외가 나도 done은 줄어야 함
                }
            }));
        }

        ready.await();
        start.countDown();
        done.await();

        // 스레드 내부 예외를 테스트 실패로 끌어올림
        for (Future<?> f : futures) {
            f.get();
        }

        pool.shutdown();

        List<HashTag> finded = hashTagRepository.findAllByTagIn(tags);

        assertThat(finded).hasSize(tags.size());
        assertThat(finded).allSatisfy(t -> assertThat(t.getUsedCount()).isEqualTo((long) totalCall + 1));
    }
}
