package com.studypals.domain.studyManage.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.StartStudyDto;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
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
    private final DailyInfoWriter dailyInfoWriter;
    private final MemberReader memberReader;

    private static final int SECS_PER_DAY = 24 * 60 * 60;

    @Override
    @Transactional
    public StartStudyRes startStudy(Long userId, StartStudyReq req) {
        LocalDate today = timeUtils.getToday(req.startTime());
        LocalDateTime startDateTime = LocalDateTime.of(today, req.startTime());
        StartStudyDto dto = mapper.toDto(req, startDateTime);

        Member member = memberReader.getRef(userId);
        Optional<StudyStatus> status = studyStatusWorker.find(userId);

        // 만약 이미 공부 중인 경우, 공부를 시작하는 대신 현재 공부 상태를 반환함
        if (status.isPresent() && status.get().isStudying()) return mapper.toDto(status.get());

        // 공부 중이 아닌 경우, 새로운 redis에 저장할 공부 상태 객체 생성
        StudyStatus startStatus = studyStatusWorker.startStatus(member, dto);
        dailyInfoWriter.createIfNotExist(member, today, req.startTime());

        if (req.categoryId() != null) { // 만약 졍규 카테고리에 대한 공부라면
            StudyCategory category = studyCategoryReader.getById(req.categoryId());
            startStatus.setGoal(category.getGoal());
        }

        // 공부 상태 저장
        studyStatusWorker.saveStatus(startStatus);

        return mapper.toDto(startStatus);
    }

    @Override
    @Transactional
    public Long endStudy(Long userId, LocalTime endTime) {
        // 저장할 날짜 및 저장 중인 공부 상태 객체 불러오기
        Long totalTime = 0L;
        LocalDate today = timeUtils.getToday(endTime);
        Member member = memberReader.getRef(userId);

        // 현재 공부 상태 불러오기
        StudyStatus status = studyStatusWorker
                .find(userId)
                .orElseThrow(() -> new StudyException(
                        StudyErrorCode.STUDY_TIME_END_FAIL, "[StudySessionServiceImpl#endStudy] unknown status"));
        studyStatusWorker.validStatus(status);

        LocalDate startDate = status.getStartTime().toLocalDate();
        LocalTime startTime = status.getStartTime().toLocalTime();

        Map<LocalDate, TimeSaveInfo> timeSaveInfoMap = new HashMap<>();
        if (startDate.isEqual(today)) {

            long durationInSec = getTimeDuration(startTime, endTime);
            timeSaveInfoMap.put(today, new TimeSaveInfo(member, startTime, endTime));
            studySessionWorker.upsert(member, status, today, durationInSec);
            totalTime += durationInSec;

        } else if (startDate.plusDays(1).isEqual(today)) {

            LocalTime pointTime = LocalTime.of(6, 0);//to static var
            long day1DurationInSec = getTimeDuration(startTime, pointTime);
            long day2DurationInSec = getTimeDuration(pointTime, endTime);
            timeSaveInfoMap.put(startDate, new TimeSaveInfo(member, startTime, pointTime));
            timeSaveInfoMap.put(today, new TimeSaveInfo(member, pointTime, endTime));

            studySessionWorker.upsert(member, status, startDate, day1DurationInSec);
            studySessionWorker.upsert(member, status, today, day2DurationInSec);

            totalTime += day1DurationInSec + day2DurationInSec;
        } else {
            throw new StudyException(
                    StudyErrorCode.STUDY_TIME_END_FAIL,
                    "[StudySessionServiceImpl#endStudy] over 1 day pass is invalid");
        }
        saveDailyInfo(timeSaveInfoMap);

        // 커밋 이후 status 반영 및 초기화
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    studyStatusWorker.delete(userId);
                }
            });
        }

        return totalTime;
    }

    /**
     *  dailyStudyInfo 를 저장하기 위해 구성된 데이터 전송 목적의 record. saveDailyInfo 메서드의 매개변수
     *  구성 요소로 사용됩니다.
     * @param member 저장할 사용자
     * @param start 공부 시작 시간
     * @param end 공부 종료 시간
     */
    private record TimeSaveInfo(Member member, LocalTime start, LocalTime end) {}

    private void saveDailyInfo(Map<LocalDate, TimeSaveInfo> saveMap) {

        for (Map.Entry<LocalDate, TimeSaveInfo> entry : saveMap.entrySet()) {
            TimeSaveInfo info = entry.getValue();
            LocalDate date = entry.getKey();
            if (!dailyInfoWriter.createIfNotExist(info.member, date, info.start, info.end)) {
                dailyInfoWriter.updateEndtime(info.member, date, info.end);
            }
        }
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
