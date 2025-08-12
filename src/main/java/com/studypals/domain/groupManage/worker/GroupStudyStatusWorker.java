package com.studypals.domain.groupManage.worker;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupStudyStatusRepository;
import com.studypals.domain.groupManage.entity.GroupStudyStatus;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.DateType;
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
@Worker
@RequiredArgsConstructor
public class GroupStudyStatusWorker {

    private final GroupStudyStatusRepository studyStatusRepository;
    private final TimeUtils timeUtils;
    private final StudyTimeRepository studyTimeRepository;

    private static final Long UPDATE_COUNT_MAX = 30L;

    public void overwriteData(Long groupId) {
        String token = studyStatusRepository.tryLock()
    }

    public void updateStatusCache(StudyTime studyTime, Long deltaSecond) {
        //카테고리 추출
        StudyCategory c = studyTime.getStudyCategory();

        //GROUP 타입 여부 검사
        if(c == null) return;
        if(c.getStudyType() != StudyType.GROUP) return;

        //group 및 주차 추출
        Long groupId = c.getTypeId();
        LocalDate date = timeUtils.getToday();


        //
        Optional<GroupStudyStatus> curStatus = studyStatusRepository.findById(groupId);

        if(curStatus.isPresent() && curStatus.get().getDate() == date) {
            if(curStatus.get().getUpdateCnt() <= UPDATE_COUNT_MAX) {
                studyStatusRepository.hIncrField(groupId, c.getId(), deltaSecond);

            } else {
                overwriteData(groupId);
            }
            return;
        } else {
            GroupStudyStatus groupStudyStatus = create(groupId, date);
            groupStudyStatus.getStudyStatus().put(c.getId(), deltaSecond);
            studyStatusRepository.save(groupStudyStatus);
        }

    }



    private GroupStudyStatus create(Long groupId, LocalDate date) {
        return GroupStudyStatus.builder()
                .id(groupId)
                .date(date)
                .updateCnt(1L)
                .build();
    }
}
