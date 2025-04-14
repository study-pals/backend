package com.studypals.domain.studyManage.entity;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.Id;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 현재 유저의 공부 상태 및 하루 간 공부 총합을 저장합니다. redis에 의해 관리됩니다.
 * <p>
 * 다음과 같은 필드를 가지고 있습니다.
 * <pre>
 *     {@code
 * Long id;
 * boolean studying;
 * LocalTime startTime;
 * Long studyTime;
 * Long expiration;
 *     }
 * </pre>
 *
 * @author jack8
 * @since 2025-04-10
 */
@RedisHash("studyStatus")
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
public class StudyStatus {
    @Id
    private Long id;

    @Builder.Default
    private boolean studying = true;

    private LocalTime startTime;

    @Builder.Default
    private Long studyTime = 0L;

    private Long categoryId;

    private String temporaryName;

    @TimeToLive(unit = TimeUnit.DAYS)
    @Builder.Default
    private Long expiration = 1L;

    public StudyStatusBuilder update() {
        return this.toBuilder();
    }
}
