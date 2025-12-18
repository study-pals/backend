package com.studypals.domain.studyManage.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
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
import com.studypals.domain.studyManage.dto.StudyStatusRes;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.worker.DailyInfoWriter;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudySessionWorker;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
import com.studypals.domain.studyManage.worker.StudyTimeReader;
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
@Slf4j
public class StudySessionServiceImpl implements StudySessionService {

    private final StudyTimeMapper mapper;
    private final TimeUtils timeUtils;

    private final StudySessionWorker studySessionWorker;
    private final StudyStatusWorker studyStatusWorker;
    private final StudyCategoryReader studyCategoryReader;
    private final DailyInfoWriter dailyInfoWriter;
    private final MemberReader memberReader;

    private static final int SECS_PER_DAY = 24 * 60 * 60;
    private final StudyTimeReader studyTimeReader;

    @Override
    @Transactional
    public StartStudyRes startStudy(Long userId, StartStudyReq req) {
        LocalDate today = timeUtils.getToday(req.startTime());
        LocalDateTime startDateTime = LocalDateTime.of(today, req.startTime());
        log.info("today : {}", today);
        log.info("startDateTime : {}", startDateTime);

        Optional<StudyStatus> status = studyStatusWorker.find(userId);

        // 만약 이미 공부 중인 경우, 공부를 시작하는 대신 현재 공부 상태를 반환함
        if (status.isPresent() && status.get().isStudying()) {
            // 사용자가 이미 공부하고 있으므로, (현재 서버 시간 - 최초 시작 시간) 은 현재 유저의 공부 시간입니다.
            // 이때 현재 서버 시간의 경우 비즈니스 날짜 (새벽 6시 기준 날짜)가 아닌 실제 날짜/시간을 사용해야 합니다.
            long diff = getTimeDuration(status.get().getStartTime().toLocalTime(),
                    timeUtils.getTime());
            log.info("diff : {}", diff);
            return mapper.toDto(status.get(), diff);
        }

        StartStudyDto dto = mapper.toDto(req, startDateTime);
        Member member = memberReader.getRef(userId);

        // 공부 중이 아닌 경우, 새로운 redis에 저장할 공부 상태 객체 생성
        StudyStatus startStatus = studyStatusWorker.startStatus(member, dto);
        dailyInfoWriter.createIfNotExist(member, today, req.startTime());

        if (req.categoryId() != null) { // 만약 졍규 카테고리에 대한 공부라면
            StudyCategory category = studyCategoryReader.getById(req.categoryId());
            startStatus.setGoal(category.getGoal());
        }

        // 공부 상태 저장
        studyStatusWorker.saveStatus(startStatus);

        return mapper.toDto(startStatus, 0L);
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
            studySessionWorker.upsert(member, status, today, durationInSec); // studyTime update
//            studyStatusWorker.addStudyTimeAndSave(status, durationInSec); // studyStatus update
            totalTime += durationInSec;

        } else if (startDate.plusDays(1).isEqual(today)) {

            LocalTime pointTime = LocalTime.of(6, 0); // to static var
            long day1DurationInSec = getTimeDuration(startTime, pointTime);
            long day2DurationInSec = getTimeDuration(pointTime, endTime);
            timeSaveInfoMap.put(startDate, new TimeSaveInfo(member, startTime, pointTime));
            timeSaveInfoMap.put(today, new TimeSaveInfo(member, pointTime, endTime));

            // 오전 6시 전에는 studyStatus가 존재한다. 기존의 것을 그대로 이용한다.
            // 어제자 dailyInfo는 밑에 saveDailyInfo에서 처리한다.
            studySessionWorker.upsert(member, status, startDate, day1DurationInSec);
//            studyStatusWorker.addStudyTimeAndSave(status, day1DurationInSec);

            // 오전 6시 이후에는 studyStatus가 존재하지 않는다. 새로 만들어야 한다.
//            LocalDateTime tomorrow = LocalDateTime.of(today, pointTime);
            studySessionWorker.upsert(member, status, today, day2DurationInSec);
//            StudyStatus tomorrowStudyStatus = studyStatusWorker.startStatus(
//                    member, new StartStudyDto(status.getCategoryId(), status.getName(), tomorrow));
//            studyStatusWorker.addStudyTimeAndSave(tomorrowStudyStatus, day2DurationInSec);

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
     * 사용자의 공부 상태를 반환합니다.
     * 공부하고 있다면 해당 공부에 대한 정보를 반환하고, 아니면 단순 false 값만 담아 반환합니다.
     * @param userId
     * @return StudyStatusRes
     */
    @Override
    public StudyStatusRes checkStudyStatus(Long userId) {
        Optional<StudyStatus> optionalStatus = studyStatusWorker.find(userId);

        // redis에 공부 정보가 없다면 false 반환
        if (optionalStatus.isEmpty()) {
            return mapper.toStudyStatusDto(false);
        }

        StudyStatus studyStatus = optionalStatus.get();

        // 공부 정보가 있는데, 공부 중은 아니라면 false 반환
        if (!studyStatus.isStudying()) {
            return mapper.toStudyStatusDto(false);
        }

        // StudyStatus 엔티티의 studyTime 값은 사용하지 않는 값으로 무조건 0이다. 따라서 StudyTime에서 따로 가져와야 한다.
        // 이거는 해당 카테고리를 오늘 공부한 총 시간을 가져오는 쿼리이다.
        // TODO: mysql의 StudyTime에서 가져오고 있지만 추후에는 StudyStatus에 유의미한 time 값을 사용해 반환해야 한다.
        Long studyTime = studyTimeReader
                .findByCategoryId(userId, LocalDate.from(studyStatus.getStartTime()), studyStatus.getCategoryId())
                .orElse(0L);

        return mapper.toStudyStatusDto(studyStatus, studyTime);
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
