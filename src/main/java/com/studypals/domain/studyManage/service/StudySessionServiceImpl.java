package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.worker.GroupStudyStatusWorker;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.worker.DailyInfoWriter;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudySessionWorker;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;
import com.studypals.global.utils.TimeUtils;

/**
 * {@link StudySessionService} 의 구현 클래스입니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code StudySessionService} 의 구현 클래스
 *
 * <p><b>빈 관리:</b><br>
 * Service
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
    private final StudyCategoryReader studyCategoryReader;
    private final GroupStudyStatusWorker groupStudyStatusWorker;
    private final DailyInfoWriter dailyInfoWriter;
    private final MemberReader memberReader;

    private static final int SECS_PER_DAY = 24 * 60 * 60;

    @Override
    @Transactional
    public StartStudyRes startStudy(Long userId, StartStudyReq dto) {

        Member member = memberReader.getRef(userId);
        Optional<StudyStatus> status = studyStatusWorker.find(userId);

        // 만약 이미 공부 중인 경우, 공부를 시작하는 대신 현재 공부 상태를 반환함
        if (status.isPresent() && status.get().isStudying()) return mapper.toDto(status.get());

        // 공부 중이 아닌 경우, 새로운 redis에 저장할 공부 상태 객체 생성
        StudyStatus startStatus = studyStatusWorker.startStatus(member, dto);

        if (dto.categoryId() != null) { // 만약 졍규 카테고리에 대한 공부라면
            StudyCategory category = studyCategoryReader.getById(dto.categoryId());
            startStatus = startStatus // 카테고리 정보 반영
                    .update()
                    .goal(category.getGoal())
                    .name(category.getName())
                    .build();
        }

        // 공부 상태 저장
        studyStatusWorker.saveStatus(startStatus);

        return mapper.toDto(startStatus);
    }

    @Override
    @Transactional
    public Long endStudy(Long userId, LocalTime endTime) {
        // 저장할 날짜 및 저장 중인 공부 상태 객체 불러오기
        LocalDate today = timeUtils.getToday();

        // 현재 공부 상태 불러오기
        StudyStatus status = studyStatusWorker
                .find(userId)
                .orElseThrow(() -> new StudyException(
                        StudyErrorCode.STUDY_TIME_END_FAIL, "[StudySessionServiceImpl#endStudy] unknown status"));

        // 공부 시작 시간, member reference, 공부 시간 변수 정의
        LocalTime start = status.getStartTime();
        Member member = memberReader.getRef(userId);
        long durationInSec = getTimeDuration(start, endTime);

        studyStatusWorker.validStatus(status); // 받아온 status 가 정상인지 확인

        // 만약 오전 6시(하루가 바뀌는 지점) 이전에 시작하여 이후에 끝나는 경우
        if (status.getStartTime().isBefore(LocalTime.of(6, 0)) && endTime.isAfter(LocalTime.of(6, 0))) {

            // 시작 ~ 오전 6시 까지를 전 날로 기록/ 이후는 6시를 시작 시각으로 지정
            LocalTime point = LocalTime.of(6, 0);
            Long durationInSecBeforeDay = getTimeDuration(start, point);
            studySessionWorker.upsert(member, status, today.minusDays(1L), durationInSecBeforeDay);
            dailyInfoWriter.updateEndtime(member, today.minusDays(1L), point);
            durationInSec = getTimeDuration(point, endTime);
        }

        // 시작~종료까지의 정보를 저장
        StudyTime studyTime = studySessionWorker.upsert(member, status, today, durationInSec);
        dailyInfoWriter.updateEndtime(member, today, endTime);

        // 커밋 이후 status 반영 및 초기화
        Long finalDurationInSec = durationInSec;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                groupStudyStatusWorker.updateStatusCache(studyTime, finalDurationInSec);
                studyStatusWorker.delete(userId);
            }
        });

        return durationInSec;
    }

    /**
     * 시작 시간과 종료 시간에 대해 second로 반환하는 메서드.
     * 00:00 을 기점으로 계산 로직이 달라진다.
     * @param start 공부 시작 시간
     * @param end 공부 종료 시간
     * @return 초 단위 공부 시간
     */
    private long getTimeDuration(LocalTime start, LocalTime end) {
        int s = start.toSecondOfDay();
        int e = end.toSecondOfDay();
        if (s == e) return 0L;
        return (e >= s) ? (e - s) : (SECS_PER_DAY - s) + e;
    }
}
