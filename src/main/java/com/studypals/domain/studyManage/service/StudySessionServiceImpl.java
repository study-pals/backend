package com.studypals.domain.studyManage.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.worker.DailyInfoWriter;
import com.studypals.domain.studyManage.worker.StudySessionWorker;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
import com.studypals.domain.studyManage.worker.strategy.StudyTimePersistenceStrategy;
import com.studypals.domain.studyManage.worker.strategy.StudyTimePersistenceStrategyFactory;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;
import com.studypals.global.utils.TimeUtils;

/**
 * {@link StudySessionService} 의 구현 클래스입니다.
 * <p>
 * 오버라이드된 메서드와, 해당 메서드에 사용되는 private 메서드가 선언되어 있습니다. 자세한 주석은
 * interface 에 존재합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code StudySessionService} 의 구현 클래스
 *
 *
 * <p><b>빈 관리:</b><br>
 * Service 빈
 * <pre>
 * private final StudyTimeMapper mapper;
 * private final TimeUtils timeUtils;
 *
 * private final StudySessionWorker studySessionWorker;
 * private final StudyStatusWorker studyStatusWorker;
 * </pre>>
 *
 *
 * @author jack8
 * @see StudySessionService
 * @since 2025-04-13
 */
@Service
@RequiredArgsConstructor
public class StudySessionServiceImpl implements StudySessionService {

    private final StudyTimeMapper mapper;
    private final TimeUtils timeUtils;

    private final StudySessionWorker studySessionWorker;
    private final StudyStatusWorker studyStatusWorker;
    private final DailyInfoWriter dailyInfoWriter;
    private final MemberReader memberReader;

    private final StudyTimePersistenceStrategyFactory studyTimeFactory;

    @Override
    @Transactional
    public StartStudyRes startStudy(Long userId, StartStudyReq dto) {

        Member member = memberReader.getRef(userId);
        Optional<StudyStatus> status = studyStatusWorker.find(userId);

        if (status.isPresent() && status.get().isStudying()) return mapper.toDto(status.get());

        StudyStatus startStatus = studyStatusWorker.startStatus(member, dto);

        StudyTimePersistenceStrategy strategy = studyTimeFactory.resolve(startStatus);
        try {
            Optional<? extends StudyCategory> category = strategy.getCategoryInfo(member, startStatus.getTypeId());

            if (category.isPresent()) {
                startStatus = startStatus
                        .update()
                        .goal(category.get().getGoal())
                        .name(category.get().getName())
                        .build();
            }
        } catch (IllegalArgumentException e) {
            throw new StudyException(StudyErrorCode.STUDY_TIME_START_FAIL, e.getMessage());
        }

        studyStatusWorker.saveStatus(startStatus);

        return mapper.toDto(startStatus);
    }

    @Override
    @Transactional
    public Long endStudy(Long userId, LocalTime endTime) {
        LocalDate today = timeUtils.getToday();
        StudyStatus status = studyStatusWorker
                .findAndDelete(userId)
                .orElseThrow(() -> new StudyException(StudyErrorCode.STUDY_TIME_END_FAIL));

        studyStatusWorker.validStatus(status); // 받아온 status 가 정상인지 확인

        // 1) 실제 공부 시간(초)
        Long durationInSec = getTimeDuration(status.getStartTime(), endTime);

        // 2) DB에 공부시간 및 종료 시간 upsert
        Member member = memberReader.getRef(userId);
        studySessionWorker.upsert(member, status, today, durationInSec);
        dailyInfoWriter.updateEndtime(member, today, endTime);

        return durationInSec;
    }

    /**
     * 시작 시간과 종료 시간에 대해 second로 반환하는 메서드.
     * 00:00 을 기점으로 계산 로직이 달라진다.
     * @param start 공부 시작 시간
     * @param end 공부 종료 시간
     * @return 초 단위 공부 시간
     */
    private Long getTimeDuration(LocalTime start, LocalTime end) {
        if (!start.isAfter(end)) {
            return Duration.between(start, end).toSeconds();
        }

        // startTime 이 00:00 전이고, endTime 이 이후인 경우
        long startToMidnight = Duration.between(start, LocalTime.MAX).toSeconds() + 1L;
        long midnightToEnd = Duration.between(LocalTime.MIN, end).toSeconds();
        return startToMidnight + midnightToEnd;
    }
}
