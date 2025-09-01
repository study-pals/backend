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
    public StudyTime upsert(Member member, StudyStatus status, LocalDate studiedDate, Long time) {

        // 1. 매개변수 검증
        if (member == null || studiedDate == null || time == null || time <= 0) {
            throw new StudyException(
                    StudyErrorCode.STUDY_TIME_END_FAIL, "[StudySessionWorker#upsert] invalid arguments");
        }

        // 2. 해당 멤버가 보유할 토큰 정보 갱신
        member.addToken(calculateToken(time));

        // 3. 영속화 진행
        Long categoryId = status.getCategoryId();
        String name = status.getName();

        // 3-1 : 영속화된 카테고리에 대한 공부 기록 갱신의 경우
        if (categoryId != null) {
            // 기존에 동일한 카테고리로 공부한 적 있는지 검색
            StudyTime studyTime = studyTimeRepository
                    .findByCategoryAndDate(member.getId(), studiedDate, categoryId)
                    .orElseGet(
                            () -> { // 없으면 새로운 studyTime 을 생성 - category 검색 후 연관관계 설정
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

            // 공부한 시간을 더해주고 저장(이미 존재하는 경우 / 더해줌 || 새롭게 생성된 경우 / 0으로 초기화 되어 있어 정상적으로 들어감)
            studyTime.addTime(time);
            saveTime(studyTime);

            return studyTime;
        }

        // 3-2 : 이름이 null 이거나 빈 칸인 경우 - 예외(올바르지 않은 값)
        if (name == null || name.isBlank()) {
            throw new StudyException(
                    StudyErrorCode.STUDY_TIME_END_FAIL,
                    "[StudySessionWorker#upsert] both categoryId and name cannot be null/blank");
        }

        // 3-3 : 영속화된 카테고리에 대한 갱신이 아닌 임시 카테고리에 대한 갱신 요청인 경우 -
        // 동일한 임시 카테고리를 해당 날짜에 공부한 기록이 있는지 검색 - 존재하면 여기에 시간 추가
        StudyTime studyTime = studyTimeRepository
                .findByMemberIdAndStudiedDateAndName(member.getId(), studiedDate, name)
                .orElseGet(() -> StudyTime.builder() // 존재하지 않으면 새로운 StudyTime 생성
                        .member(member)
                        .studiedDate(studiedDate)
                        .goal(status.getGoal())
                        .time(0L)
                        .name(name)
                        .build());

        // 공부 시간 추가 및 저장
        studyTime.addTime(time);
        saveTime(studyTime);

        return studyTime;
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
