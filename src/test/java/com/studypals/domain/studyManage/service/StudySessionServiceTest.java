package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        given(mapper.toDto(status)).willReturn(expected);

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
        LocalTime time = LocalTime.of(10, 30);
        LocalDateTime dateTime = LocalDateTime.of(today, time);

        StartStudyReq req = new StartStudyReq(categoryId, null, time);
        StartStudyDto dto = new StartStudyDto(categoryId, null, dateTime);

        StartStudyRes expected = new StartStudyRes(true, dateTime, 0L, categoryId, null, 3600L);

        given(timeUtils.getToday(eq(time))).willReturn(today);
        given(mapper.toDto(eq(req), eq(dateTime))).willReturn(dto);
        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(studyStatusWorker.find(userId)).willReturn(Optional.of(mockStudyStatus));
        given(mockStudyStatus.isStudying()).willReturn(true);
        given(mapper.toDto(mockStudyStatus)).willReturn(expected);

        // when
        StartStudyRes res = studySessionService.startStudy(userId, req);

        // then
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void endStudy_success() {
        // given
        Long userId = 1L;

        LocalDateTime startDateTime = LocalDateTime.of(2025, 8, 20, 10, 30);
        LocalDateTime endDateTime = LocalDateTime.of(2025, 8, 20, 12, 30);

        long time = 7200L;

        given(timeUtils.getToday(eq(endDateTime.toLocalTime()))).willReturn(endDateTime.toLocalDate());
        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(studyStatusWorker.find(userId)).willReturn(Optional.of(mockStudyStatus));
        given(mockStudyStatus.getStartTime()).willReturn(startDateTime);

        // in #saveDailyInfo
        given(dailyInfoWriter.createIfNotExist(
                        mockMember, endDateTime.toLocalDate(), startDateTime.toLocalTime(), endDateTime.toLocalTime()))
                .willReturn(true);

        // when
        Long res = studySessionService.endStudy(userId, endDateTime.toLocalTime());

        // then
        assertThat(res).isEqualTo(time);
        ArgumentCaptor<Long> durationCaptor = ArgumentCaptor.forClass(Long.class);
        then(studySessionWorker)
                .should()
                .upsert(eq(mockMember), eq(mockStudyStatus), eq(endDateTime.toLocalDate()), durationCaptor.capture());
        assertThat(durationCaptor.getValue()).isEqualTo(time);
        then(dailyInfoWriter).should(never()).updateEndtime(any(), any(), any());
    }

    @Test
    void endStudy_success_between6AM() {
        // given
        Long userId = 1L;

        LocalDateTime startDateTime = LocalDateTime.of(2025, 8, 19, 3, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2025, 8, 20, 8, 0);

        long time = 3600 * 5;

        given(timeUtils.getToday(endDateTime.toLocalTime())).willReturn(endDateTime.toLocalDate());
        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(studyStatusWorker.find(userId)).willReturn(Optional.of(mockStudyStatus));
        given(mockStudyStatus.getStartTime()).willReturn(startDateTime);

        given(dailyInfoWriter.createIfNotExist(any(), any(), any(), any())).willReturn(true);

        // when
        Long res = studySessionService.endStudy(userId, endDateTime.toLocalTime());

        // then
        assertThat(res).isEqualTo(time);
        ArgumentCaptor<Long> beforeDateDurationCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> afterDateDurationCaptor = ArgumentCaptor.forClass(Long.class);

        then(studySessionWorker)
                .should()
                .upsert(
                        eq(mockMember),
                        eq(mockStudyStatus),
                        eq(startDateTime.toLocalDate()),
                        beforeDateDurationCaptor.capture());
        then(studySessionWorker)
                .should()
                .upsert(
                        eq(mockMember),
                        eq(mockStudyStatus),
                        eq(endDateTime.toLocalDate()),
                        afterDateDurationCaptor.capture());

        assertThat(beforeDateDurationCaptor.getValue()).isEqualTo(3600 * 3);
        assertThat(afterDateDurationCaptor.getValue()).isEqualTo(3600 * 2);
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
