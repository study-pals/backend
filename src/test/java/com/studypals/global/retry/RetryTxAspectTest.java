package com.studypals.global.retry;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.HashTagRepository;
import com.studypals.domain.groupManage.entity.HashTag;
import com.studypals.testModules.testSupport.TestEnvironment;

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
 * @since 2026-01-03
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RetryTxAspectTest extends TestEnvironment {

    @Autowired
    HashTagRepository hashTagRepository;
    @Autowired
    RaceConditionHashTagService raceConditionHashTagService;
Data

    @BeforeEach
    void setUp() {
        hashTagRepository.saveAndFlush(HashTag.builder()
                .tag("spring")
                .usedCount(1L)
                .build());
    }

    @TestConfiguration


    @TestConfiguration
    static class Config {
        @Bean
        RaceConditionHashTagService raceConditionHashTagService(HashTagRepository hashTagRepository) {
            return new RaceConditionHashTagService(hashTagRepository);
        }

    }


    @RequiredArgsConstructor
    public static class RaceConditionHashTagService {
        private final HashTagRepository hashTagRepository;
        private final AtomicInteger attempts = new AtomicInteger(0);

        public int getAttempts() {
            return attempts.get();
        }

        @RetryTx(
                maxAttempts = 2,
                backoffMs = 0,
                retryFor = {DataIntegrityViolationException.class}
        )
        public void create(String tag) {
            int attempt = attempts.incrementAndGet();

            if (attempt == 1) {
                // 1회차: insert + flush로 unique 위반 강제
                hashTagRepository.save(HashTag.builder()
                        .tag(tag)
                        .usedCount(1L)
                        .build());
                hashTagRepository.flush(); // 여기서 DataIntegrityViolationException
                return;
            }

            // 2회차: 복구 동작 (usedCount 증가)
            hashTagRepository.increaseUsedCount(tag);
        }

    }
}
