package com.studypals.domain.studyManage.worker;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * service - repository 사이의 worker 계층입니다. 공부가 시작-종료 됨에 따라
 * 발생하는 로직들을 담당합니다.
 *
 * @author jack8
 * @since 2025-04-15
 */
@Worker
@RequiredArgsConstructor
public class StudySessionWorker {
    private static final Long TOKEN_CALCULATE_VALUE = 60L;

    private final StudyTimeRepository studyTimeRepository;
    private final StudyCategoryRepository studyCategoryRepository;

    /**
     * studyTime 을 최신화합니다. 전략 패턴에 따라 공부 타입을 분류합니다.
     * 갈린다.
     * @param status redis 에 저장된 사용자 상태
     * @param studiedDate 언제 공부했는지에 대한 날짜(today)
     * @param time 초 단위 공부 시간
     */
    public void upsert(Member member, StudyStatus status, LocalDate studiedDate, Long time) {

        if (member == null || studiedDate == null || time == null || time <= 0) {
            throw new StudyException(
                    StudyErrorCode.STUDY_TIME_END_FAIL, "[StudySessionWorker#upsert] invalid arguments");
        }

        member.addToken(calculateToken(time));

        Long categoryId = status.getCategoryId();
        String name = status.getName();

        if (categoryId != null) {
            StudyTime studyTime = studyTimeRepository
                    .findByCategoryAndDate(member.getId(), studiedDate, categoryId)
                    .orElseGet(() -> {
                        StudyCategory category = studyCategoryRepository
                                .findById(categoryId)
                                .orElseThrow(() -> new StudyException(
                                        StudyErrorCode.STUDY_CATEGORY_NOT_FOUND,
                                        "[StudySessionWorker#upsert] unknown category id from status"));

                        return StudyTime.builder()
                                .member(member)
                                .studiedDate(studiedDate)
                                .studyCategory(category)
                                .goal(status.getGoal())
                                .time(0L)
                                .build();
                    });

            studyTime.addTime(time);
            saveTime(studyTime);

            return;
        }

        if (name == null || name.isBlank()) {
            throw new StudyException(
                    StudyErrorCode.STUDY_TIME_END_FAIL,
                    "[StudySessionWorker#upsert] both categoryId and name cannot be null/blank");
        }

        StudyTime studyTime = studyTimeRepository
                .findByMemberIdAndStudiedDateAndName(member.getId(), studiedDate, name)
                .orElseGet(() -> StudyTime.builder()
                        .member(member)
                        .studiedDate(studiedDate)
                        .goal(status.getGoal())
                        .time(0L)
                        .name(name)
                        .build());

        studyTime.addTime(time);
        saveTime(studyTime);
    }

    private void saveTime(StudyTime time) {
        try {
            studyTimeRepository.save(time);
        } catch (Exception e) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL, "save fail");
        }
    }

    private Long calculateToken(Long time) {
        return time / TOKEN_CALCULATE_VALUE;
    }
}
