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
 * <br>package name   : com.studypals.domain.groupManage.worker
 * <br>file name      : GroupStudyStatusWorker
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

// todo : delta buffer implement?
@Worker
@RequiredArgsConstructor
public class GroupStudyStatusWorker {

    private final GroupStudyStatusRepository studyStatusRepository;
    private final TimeUtils timeUtils;
    private final StudyTimeRepository studyTimeRepository;
    private final StudyCategoryRepository studyCategoryRepository;

    private static final Long UPDATE_COUNT_MAX = 30L;
    private final GroupStudyStatusRepository groupStudyStatusRepository;

    public void overwriteData(Long groupId, LocalDate today) {
        String token = studyStatusRepository.tryLock(groupId, Duration.ofSeconds(15));
        if (token == null) return;

        try {
            List<Long> categoryIds = studyCategoryRepository.findByStudyTypeAndTypeId(StudyType.GROUP, groupId).stream()
                    .map(StudyCategory::getId)
                    .toList();

            List<StudyTime> studyTimes = studyTimeRepository.findByCategoryAndDate(today, categoryIds);

            Map<Long, Long> timeStat = new HashMap<>();
            for (StudyTime time : studyTimes) {
                Long categoryId = time.getStudyCategory().getId();
                long studying = time.getTime();
                long goal = time.getGoal() == null ? Long.MAX_VALUE : time.getGoal();
                long inc = Math.min(studying, goal);
                timeStat.merge(categoryId, inc, Long::sum);
            }

            groupStudyStatusRepository.save(GroupStudyStatus.builder()
                    .id(groupId)
                    .studyStatus(timeStat)
                    .date(today)
                    .updateCnt(0L)
                    .build());
        } finally {
            studyStatusRepository.unlock(groupId, token);
        }
    }

    public void updateStatusCache(StudyTime studyTime, Long deltaSecond) {
        StudyCategory c = studyTime.getStudyCategory();
        if (c == null || c.getStudyType() != StudyType.GROUP) return;

        Long groupId = c.getTypeId();
        LocalDate today = timeUtils.getToday();

        Optional<GroupStudyStatus> curOpt = studyStatusRepository.findById(groupId);

        if (curOpt.isPresent()
                && today.equals(curOpt.get().getDate())
                && (curOpt.get().getUpdateCnt() == null || curOpt.get().getUpdateCnt() <= UPDATE_COUNT_MAX)) {

            studyStatusRepository.initOrIncrField(groupId, c.getId(), deltaSecond);
            return;
        }

        overwriteData(groupId, today);
    }

    private GroupStudyStatus create(Long groupId, LocalDate date) {
        return GroupStudyStatus.builder().id(groupId).date(date).updateCnt(1L).build();
    }
}
