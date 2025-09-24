package com.studypals.domain.studyManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.studypals.domain.studyManage.entity.StudyStatus;

/**
 * {@link StudyStatusRedisRepository} 에 대한 redis 테스트
 *
 * @author jack8
 * @since 2025-04-10
 */
@DataRedisTest
class StudyStatusRedisRepositoryTest {
    @Autowired
    private StudyStatusRedisRepository studyStatusRedisRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    void save_success() {
        // given
        StudyStatus status = StudyStatus.builder()
                .id(1L)
                .startTime(LocalDateTime.of(2025, 8, 20, 10, 0))
                .studyTime(120L)
                .expiration(1L)
                .build();

        // when
        studyStatusRedisRepository.save(status);
        Optional<StudyStatus> result = studyStatusRedisRepository.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getStudyTime()).isEqualTo(120L);
    }

    @Test
    void delete_success() {
        // given
        StudyStatus status = StudyStatus.builder()
                .id(2L)
                .startTime(LocalDateTime.of(2025, 8, 20, 12, 0))
                .studyTime(90L)
                .expiration(1L)
                .build();
        studyStatusRedisRepository.save(status);

        // when
        studyStatusRedisRepository.deleteById(2L);
        Optional<StudyStatus> result = studyStatusRedisRepository.findById(2L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_fail_noExistData() {
        // when
        StudyStatus status = studyStatusRedisRepository.findById(0L).orElse(null);

        // then
        assertThat(status).isNull();
    }
}
