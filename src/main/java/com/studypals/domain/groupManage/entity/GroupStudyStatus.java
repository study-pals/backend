package com.studypals.domain.groupManage.entity;

import java.time.LocalDate;
import java.util.Map;

import lombok.*;

import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

/**
 * 현재 그룹의 공부 목표에 대한 캐싱을 지원하는 redis hash entity 입니다. 각 그룹 별 카테고리 당 누적 공부 시간을 캐싱하여 저장합니다.
 * 분산 환경 등에서의 race condition 을 방지하기 위해 데이터 갱신 - updateCnt 증가 는 원자적으로 실행되어야 합니다.
 * 락을 위해 lock prefix 를 설정하였습니다.
 * <p>
 * {@link com.studypals.domain.groupManage.dao.GroupStudyStatusRepository GroupStudyStatusRepostory} 에 의해 dao 메서드가
 * 정의됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code GroupStudyStatus.builder().build();}  <br>
 *
 * @author jack8
 * @since 2025-08-14
 */
@RedisHashEntity(value = "groupStudyStatus", lock = "lock")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GroupStudyStatus {

    /**
     * group 의 PK 와 대응됩니다. 해당 엔티티는 group id 를 key로 가집니다.
     */
    @RedisId
    private Long id;

    /**
     * 해당 엔티티가 몇번 갱신 되었는지를 표시합니다. 해당 숫자가 임계값을 넘기면, 영속화된 데이터와 동기화를 진행합니다.
     * <p> 캐시 안에서의 필드 값은 {@code f:updateCnt} 입니다.
     */
    @Builder.Default
    private Long updateCnt = 0L;

    /**
     * 해당 캐시가 유효한 날짜, 혹은 적용 가능한 날짜에 대한 정보를 표시합니다. 오늘과 현재 날짜가 일치하지 않으면 캐시가 초기화됩니다.
     * <p> 캐시 안에서의 필드 값은 {@code f:date} 입니다. (YYYY-mm-dd) 형식으로 저장됩니다.
     */
    private LocalDate date;

    /**
     * 공부 진행도를 저장하는 Map 자료구조입니다. 값을 집어넣을 때가 아닌, 값을 읽을 때 사용하는 것을 권장합니다.
     * 카테고리 아이디 - 누적 공부 시간에 대한 매핑으로 이루어져 있습니다.
     */
    @RedisHashMapField
    private Map<Long, Long> studyStatus;
}
