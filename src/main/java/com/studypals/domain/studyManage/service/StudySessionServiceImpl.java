package com.studypals.domain.studyManage.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.worker.StudySessionWorker;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
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

    @Override
    public StartStudyRes startStudy(Long userId, StartStudyReq dto) {

        // status 받아오기
        StudyStatus status = studyStatusWorker.findStatus(userId);

        // 오늘 처음 공부 시
        if (status == null) {
            status = studyStatusWorker.firstStudyStatus(userId, dto);

            studyStatusWorker.saveStatus(status);

        } else if (!status.isStudying()) {
            status = studyStatusWorker.restartStudyStatus(status, dto);

            studyStatusWorker.saveStatus(status);
        }

        return mapper.toDto(status);
    }

    @Override
    @Transactional
    public Long endStudy(Long userId, LocalTime endedAt) {
        LocalDate today = timeUtils.getToday();
        StudyStatus status = studyStatusWorker.findStatus(userId);

        studyStatusWorker.validStatus(status); // 받아온 status 가 정상인지 확인

        // 1) 실제 공부 시간(초)
        Long durationInSec = getTimeDuration(status.getStartTime(), endedAt);

        // 2) DB에 공부시간 upsert
        studySessionWorker.upsertStudyTime(userId, status, today, durationInSec);

        // 3) 레디스 상태 값 리셋
        status = studyStatusWorker.resetStudyStatus(status, durationInSec);
        studyStatusWorker.saveStatus(status);

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

        long time;

        if (end.isBefore(start)) { // startAt 이 00:00 전이고, endedAt 이 이후인 경우
            long startToMidnight = Duration.between(start, LocalTime.MIDNIGHT).toSeconds();
            long midnightToEnd = Duration.between(LocalTime.MIN, end).toSeconds();
            time = startToMidnight + midnightToEnd;
        } else { // 정상적인 경우
            time = Duration.between(start, end).toSeconds();
        }
        return time;
    }
}
