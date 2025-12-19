package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.worker.GroupStudyStatusWorker;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.StartStudyDto;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.StudyStatusRes;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.worker.DailyInfoWriter;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudySessionWorker;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
import com.studypals.domain.studyManage.worker.StudyTimeReader;
import com.studypals.global.utils.TimeUtils;

/**
 * {@link StudySessionService} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2025-04-14
 */
@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudyTimeMapper mapper;

    @Mock
    private TimeUtils timeUtils;

    @Mock
    private StudySessionWorker studySessionWorker;

    @Mock
    private StudyStatusWorker studyStatusWorker;

    @Mock
    private StudyCategoryReader studyCategoryReader;

    @Mock
    private GroupStudyStatusWorker groupStudyStatusWorker;

    @Mock
    private DailyInfoWriter dailyInfoWriter;

    @Mock
    private MemberReader memberReader;

    @Mock
    private Member mockMember;

    @Mock
    private StudyCategory mockStudyCategory;

    @Mock
    private StudyStatus mockStudyStatus;

    @Mock
    private StudyTime mockStudyTime;

    @Mock
    private StudyTimeReader mockStudyTimeReader;

    @InjectMocks
    private StudySessionServiceImpl studySessionService;

    @Test
    void startStudy_success_first() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalDate today = LocalDate.of(2025, 8, 20);
        LocalTime time = LocalTime.of(10, 0);
        LocalDateTime dateTime = LocalDateTime.of(today, time);
        StartStudyReq req = new StartStudyReq(categoryId, null, time);
        StartStudyDto dto = new StartStudyDto(categoryId, null, dateTime);

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .startTime(dateTime)
                .categoryId(categoryId)
                .build();
        StartStudyRes expected = new StartStudyRes(true, dateTime, 0L, categoryId, null, 3600L);

        given(timeUtils.getToday(any())).willReturn(today);
        given(mapper.toDto(eq(req), eq(dateTime))).willReturn(dto);
        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(studyStatusWorker.find(userId)).willReturn(Optional.empty());
        given(studyStatusWorker.startStatus(mockMember, dto)).willReturn(status);
        given(studyCategoryReader.getById(categoryId)).willReturn(mockStudyCategory);
        given(mapper.toDto(status, 0L)).willReturn(expected);

        given(mockStudyCategory.getGoal()).willReturn(3600L);

        // when
        StartStudyRes result = studySessionService.startStudy(userId, req);

        // then
        assertThat(result).isEqualTo(expected);
        then(studyStatusWorker).should().saveStatus(any());
    }

    @Test
    void startStudy_success_alreadyStartBefore() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalDate today = LocalDate.of(2025, 8, 20);
        LocalTime reqTime = LocalTime.of(10, 30);
        LocalDateTime startDateTime = LocalDateTime.of(today, reqTime);

        // 가상의 기존 공부 시작 시간 (1시간 전부터 공부 중이었다고 가정)
        LocalDateTime existingStartTime = startDateTime.minusHours(1);
        long expectedDiff = 3600L; // 1시간 차이

        StartStudyReq req = new StartStudyReq(categoryId, null, reqTime);
        StartStudyRes expected = new StartStudyRes(true, existingStartTime, expectedDiff, categoryId, null, 3600L);

        // 기존에 공부 중이던 상태 객체 설정
        given(mockStudyStatus.isStudying()).willReturn(true);
        //        given(timeUtils.getToday(eq(reqTime))).willReturn(today);
        given(mockStudyStatus.getStartTime()).willReturn(startDateTime);
        //        given(timeUtils.exceeds24Hours(expectedDiff)).willReturn(false);
        given(studyStatusWorker.find(userId)).willReturn(Optional.of(mockStudyStatus));
        given(mapper.toDto(eq(mockStudyStatus), anyLong())).willReturn(expected);

        // when
        StartStudyRes res = studySessionService.startStudy(userId, req);

        // then
        assertThat(res).isEqualTo(expected);
        assertThat(res.studyTime()).isEqualTo(expectedDiff);
    }

    @Test
    void endStudy_success() {
        // given
        Long userId = 1L;
        LocalDate testDate = LocalDate.of(2025, 8, 20);
        LocalTime startTime = LocalTime.of(10, 30);
        LocalTime endTime = LocalTime.of(12, 30);
        LocalDateTime startDateTime = LocalDateTime.of(testDate, startTime);
        long expectedTime = 7200L; // 2시간

        // 1. 기본 Mock 설정
        given(timeUtils.getToday(eq(endTime))).willReturn(testDate);
        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(studyStatusWorker.find(userId)).willReturn(Optional.of(mockStudyStatus));
        given(mockStudyStatus.getStartTime()).willReturn(startDateTime);

        // 2. [필수] 시간 차이 계산 로직 Stubbing
        given(timeUtils.getTimeDuration(eq(startTime), eq(endTime))).willReturn(expectedTime);

        // 3. saveDailyInfo 내부의 호출 검증을 위한 설정 (메서드 파라미터가 정확히 일치해야 함)
        // 인자 매칭이 까다롭다면 any()를 사용해 우선 동작 확인 후 정교화
        given(dailyInfoWriter.createIfNotExist(any(), any(), any(), any())).willReturn(true);

        // when
        Long res = studySessionService.endStudy(userId, endTime);

        // then
        assertThat(res).isEqualTo(expectedTime);

        // 4. 동작 검증
        then(studySessionWorker).should().upsert(eq(mockMember), eq(mockStudyStatus), eq(testDate), eq(expectedTime));
    }

    @Test
    void endStudy_success_between6AM() {
        // given
        Long userId = 1L;

        // [중요] 서비스 로직의 else if (startDate.plusDays(1).isEqual(today)) 조건을 만족해야 함
        // startDate: 2025-08-19, today: 2025-08-20
        LocalDateTime startDateTime = LocalDateTime.of(2025, 8, 19, 3, 0); // 시작: 19일 새벽 3시
        LocalTime endTime = LocalTime.of(8, 0); // 종료: 20일 오전 8시
        LocalDate today = LocalDate.of(2025, 8, 20);
        LocalTime cutOff = LocalTime.of(6, 0);
        // 기대하는 시간 계산 (새벽 6시 CUTOFF 기준)
        // Day 1 (19일): 03:00 ~ 06:00 = 3시간 (10800초)
        // Day 2 (20일): 06:00 ~ 08:00 = 2시간 (7200초)
        long day1Duration = 3600 * 3;
        long day2Duration = 3600 * 2;
        long totalExpectedTime = day1Duration + day2Duration;

        // Mock 설정
        given(timeUtils.getToday(eq(endTime))).willReturn(today);
        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(studyStatusWorker.find(userId)).willReturn(Optional.of(mockStudyStatus));
        given(mockStudyStatus.getStartTime()).willReturn(startDateTime);

        // [필수] timeUtils.getTimeDuration Stubbing (두 번 호출됨)
        // 1. (03:00, 06:00) -> Day 1
        given(timeUtils.getTimeDuration(eq(startDateTime.toLocalTime()), eq(cutOff)))
                .willReturn(day1Duration);
        // 2. (06:00, 08:00) -> Day 2
        given(timeUtils.getTimeDuration(eq(cutOff), eq(endTime))).willReturn(day2Duration);

        given(dailyInfoWriter.createIfNotExist(any(), any(), any(), any())).willReturn(true);

        // when
        Long res = studySessionService.endStudy(userId, endTime);

        // then
        assertThat(res).isEqualTo(totalExpectedTime);

        // 호출 검증 및 값 확인
        then(studySessionWorker)
                .should()
                .upsert(eq(mockMember), eq(mockStudyStatus), eq(startDateTime.toLocalDate()), eq(day1Duration));
        then(studySessionWorker).should().upsert(eq(mockMember), eq(mockStudyStatus), eq(today), eq(day2Duration));
    }

    @Test
    void checkStudyStatus_success() {
        Long userId = 1L;
        Long studyTime = 10L;
        Optional<StudyStatus> optionalMockStatus = Optional.of(mockStudyStatus);
        StudyStatusRes expected = new StudyStatusRes(true, LocalDateTime.now(), studyTime, 1L, "target", 20L);

        given(studyStatusWorker.find(userId)).willReturn(optionalMockStatus);
        given(mockStudyStatus.isStudying()).willReturn(true);
        given(mockStudyTimeReader.findByCategoryId(any(), any(), any())).willReturn(Optional.of(studyTime));
        given(mockStudyStatus.getStartTime()).willReturn(LocalDateTime.now());
        given(mapper.toStudyStatusDto(mockStudyStatus, studyTime)).willReturn(expected);

        StudyStatusRes result = studySessionService.checkStudyStatus(userId);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void checkStudyStatus_success_not_in_redis() {
        Long userId = -1L;
        StudyStatusRes expected = new StudyStatusRes(false, null, null, null, null, null);

        given(studyStatusWorker.find(userId)).willReturn(Optional.empty());
        given(mapper.toStudyStatusDto(false)).willReturn(expected);

        StudyStatusRes result = studySessionService.checkStudyStatus(userId);

        assertThat(result.studying()).isFalse();
    }

    @Test
    void checkStudyStatus_success_not_studying() {
        Long userId = -1L;
        Optional<StudyStatus> optionalMockStatus = Optional.of(mockStudyStatus);
        StudyStatusRes expected = new StudyStatusRes(false, null, null, null, null, null);

        given(studyStatusWorker.find(userId)).willReturn(optionalMockStatus);
        given(mockStudyStatus.isStudying()).willReturn(false);
        given(mapper.toStudyStatusDto(false)).willReturn(expected);

        StudyStatusRes result = studySessionService.checkStudyStatus(userId);

        assertThat(result.studying()).isFalse();
    }
}
