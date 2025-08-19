package com.studypals.domain.groupManage.worker;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupStudyStatusRepository;
import com.studypals.domain.groupManage.entity.GroupStudyStatus;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.annotations.Worker;
import com.studypals.global.utils.TimeUtils;

/**
 * {@link GroupStudyStatus} 에 대한 워커 클래스입니다. 값을 갱신하거나, 동기화 과정을 포함하고 있습니다.
 * <p>
 * 캐시에 대한 갱신 요청의 경우, 사용자가 공부를 종료하고, 해당 데이터가 영속화된 시점 이후에 발생합니다. 추가된 시간에 대해,
 * 만약 그룹에서 정의한 카테고리인 경우 캐시에 값을 더해줍니다. 이때, updateCnt 가 입계값을 넘기면, 영속화된 데이터를 꺼내와
 * 동기화 하는 과정을 수행합니다.
 * <br>
 * 동기화는 사용자가 임의로 동작시킬 수 있습니다.
 *
 * <p><b>빈 관리:</b><br>
 * Woker
 *
 * @author jack8
 * @see GroupStudyStatus
 * @since 2025-07-24
 */

// todo : delta buffer implement?
@Worker
@RequiredArgsConstructor
public class GroupStudyStatusWorker {

    private final GroupStudyStatusRepository studyStatusRepository;
    private final TimeUtils timeUtils;
    private final StudyTimeRepository studyTimeRepository;
    private final StudyCategoryRepository studyCategoryRepository;
    private final GroupStudyStatusRepository groupStudyStatusRepository;

    /**
     * updateCnt 의 임계값입니다. 해당 값보다 큰 경우, 동기화를 실시합니다. 동기화는 {@code overwriteData} 메서드에 의해
     * 진행됩니다.
     */
    private static final Long UPDATE_COUNT_MAX = 30L;

    /**
     * 캐시의 데이터를 영속화된 데이터에 대해 동기화합니다. {@link StudyTime} 에 대한 리스트를 가져와, 해당 데이터를 계산하여 당일 날의
     * 올바른 캐시 값을 계산하고 덮어 씌웁니다. 중복된 동기화를 방지하기 위해, 동기화 과정에서 락을 취득할 것을 강제합니다.
     * @param groupId 동기화하고자 할 그룹의 id
     * @param today 동기화 하고자 할 날짜
     */
    public void overwriteData(Long groupId, LocalDate today) {
        // 락 취득 시도 - UUID.String 반환 / 해당 값은 redis에 저장
        String token = studyStatusRepository.tryLock(groupId, Duration.ofSeconds(15));
        // 락 취득 실패 시(redis에 락 정보 존재) null 반환 / 다른 서버 인스턴스에서 동기화 진행 중
        if (token == null) return;

        try { // 도중에 실패하더라도 락 반환 필수 - finally 를 써야 함

            // 1. 해당 그룹이 정의한 studyCategory id  list 를 가져옴.
            List<Long> categoryIds = studyCategoryRepository.findByStudyTypeAndTypeId(StudyType.GROUP, groupId).stream()
                    .map(StudyCategory::getId)
                    .toList();

            // 2. 카테고리 아이디에 대해 오늘 날짜의 study Time 리스트를 불러옴.
            List<StudyTime> studyTimes = studyTimeRepository.findByCategoryAndDate(today, categoryIds);

            // 3. List<StudyTime> 에 대하여 카테고리 아이디 별 누적 시간 저장. 단, Math.min(공부 시간, 목표) 를 통해 목표 시간을 넘기는 경우 무시
            Map<Long, Long> timeStat = new HashMap<>();
            for (StudyTime time : studyTimes) {
                Long categoryId = time.getStudyCategory().getId();
                long studying = time.getTime();
                long goal = time.getGoal() == null ? Long.MAX_VALUE : time.getGoal();
                long inc = Math.min(studying, goal);
                timeStat.merge(categoryId, inc, Long::sum); // 기존의 map 에 해당 key 값이 있으면 더하기, 없으면 추가
            }

            // 4. 캐시에 저장, updatecnt 초기화
            groupStudyStatusRepository.save(GroupStudyStatus.builder()
                    .id(groupId)
                    .studyStatus(timeStat)
                    .date(today)
                    .updateCnt(0L)
                    .build());
        } finally { // 락 풀기
            studyStatusRepository.unlock(groupId, token);
        }
    }

    /**
     * group 에서 정의한 카테고리에 대한 캐시 갱신 요청을 처리하는 메서드. 만약 GROUP 관련된 요청이 아닌 경우 아무 행동을 하지 않는다.
     * @param studyTime 갱신하고자 할 공부 시간 정보
     * @param deltaSecond 해당 세션동안 공부한 시간(초단위)
     */
    public void updateStatusCache(StudyTime studyTime, Long deltaSecond) {

        // 1. Group 카테고리 인지 검증
        StudyCategory c = studyTime.getStudyCategory();
        if (c == null || c.getStudyType() != StudyType.GROUP) return;

        // 2. groupId 및 오늘 날짜 가져오기
        Long groupId = c.getTypeId();
        LocalDate today = timeUtils.getToday();

        // 3. 현재 저장된 GroupStudyStatus 가져오기
        Optional<GroupStudyStatus> curOpt = studyStatusRepository.findById(groupId);

        // 4. 만약 status 가 존재하고, 오늘에 대한 기록이고, updateCnt 가 임계값 이하일 때,
        if (curOpt.isPresent()
                && today.equals(curOpt.get().getDate())
                && (curOpt.get().getUpdateCnt() == null || curOpt.get().getUpdateCnt() <= UPDATE_COUNT_MAX)) {

            // updateCnt 증가 및 시간 반영
            studyStatusRepository.incrField(groupId, c.getId(), deltaSecond);
            return;
        }

        // 5. 만약 위 조건을 만족하지 못하는 경우, 데이터를 새로 씀(동기화)
        overwriteData(groupId, today);
    }
}
