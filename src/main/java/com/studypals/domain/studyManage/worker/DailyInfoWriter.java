package com.studypals.domain.studyManage.worker;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.DailyStudyInfoRepository;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * dailyInfo 엔티티를 삭제/갱신/추가 등을 할 때 사용합니다.
 * @author jack8
 * @see DailyStudyInfo
 * @since 2025-04-19
 */
@Worker
@RequiredArgsConstructor
public class DailyInfoWriter {

    private final DailyStudyInfoRepository dailyStudyInfoRepository;

    /**
     * daily info 엔티티에서 종료 시간을 최신화 합니다. 종료 시간은 가장 마지막에 종료되는 시각이므로, 최신화가 되어야 합니다.
     * 종료 시간이 들어가지 않아있는 경우, 아침 6시로 간주하여야 합니다.
     * <p> NOT TESTED / simple logic </p>
     * @param userId 갱신할 user 의 id
     * @param studiedAt 공부 날짜(검색을 위한)
     * @param endedAt 종료 시간(갱신을 위한)
     */
    public void updateEndtime(Long userId, LocalDate studiedAt, LocalTime endedAt) {
        DailyStudyInfo summary = dailyStudyInfoRepository
                .findByMemberIdAndStudiedAt(userId, studiedAt)
                .orElseThrow(() -> new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL));

        summary.setEndAt(endedAt);
    }
}
