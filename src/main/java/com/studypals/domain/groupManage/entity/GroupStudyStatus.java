package com.studypals.domain.groupManage.entity;

import java.time.LocalDate;
import java.util.Map;

import lombok.*;

import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

/**
 * <br>package name   : com.studypals.domain.groupManage.entity
 * <br>file name      : GroupStudyStatus
 * <br>date           : 8/12/25
 * <pre>
 * <span style="color: white;">[description]</span>
 *
 * </pre>
 * <pre>
 * <span style="color: white;">usage:</span>
 * {@code
 *
 * } </pre>
 */
@RedisHashEntity(value = "groupStudyStatus", lock = "lock")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GroupStudyStatus {

    @RedisId
    private Long id;

    @Builder.Default
    private Long updateCnt = 0L;

    private LocalDate date;

    @RedisHashMapField
    private Map<Long, Long> studyStatus;

    public Long setUpdateCntZero() {
        this.updateCnt = 0L;
        return 0L;
    }

    public Long addUpdateCnt() {
        this.updateCnt++;
        return this.updateCnt;
    }
}
