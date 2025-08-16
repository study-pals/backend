package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
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
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.worker.DailyInfoWriter;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.domain.studyManage.worker.StudySessionWorker;
import com.studypals.domain.studyManage.worker.StudyStatusWorker;
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
    private StudyStatus mockStudyStatus;

    @InjectMocks
    private StudySessionServiceImpl studySessionService;

    @Test
    void startStudy_success_firstCategory() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalTime time = LocalTime.of(10, 0);
        StudyType type = StudyType.PERSONAL;
        StartStudyReq req = new StartStudyReq(categoryId, null, time);

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .startTime(time)
                .categoryId(categoryId)
                .goal(3600L)
                .build();
        StartStudyRes expected = new StartStudyRes(true, time, 0L, categoryId, null, 3600L);

        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(studyStatusWorker.find(userId)).willReturn(Optional.empty());
        given(studyStatusWorker.startStatus(mockMember, req)).willReturn(status);
        given(mapper.toDto(status)).willReturn(expected);

        // when
        StartStudyRes result = studySessionService.startStudy(userId, req);

        // then
        assertThat(result).isEqualTo(expected);
        then(studyStatusWorker).should().saveStatus(status);
    }

    @Test
    void endStudy_success_sameDay() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.of(2025, 4, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(13, 0);
        Long duration = Duration.between(start, end).toSeconds();
        StudyType type = StudyType.PERSONAL;

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studyType(type)
                .typeId(1L)
                .startTime(start)
                .studying(true)
                .studyTime(300L)
                .build();
        StudyStatus updated = StudyStatus.builder()
                .id(userId)
                .studying(false)
                .studyTime(300L + duration)
                .build();

        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(timeUtils.getToday()).willReturn(today);
        given(studyStatusWorker.findAndDelete(userId)).willReturn(Optional.of(status));
        willDoNothing().given(studyStatusWorker).validStatus(status);
        given(studyStatusWorker.resetStatus(status, duration)).willReturn(updated);

        // when
        Long result = studySessionService.endStudy(userId, end);

        // then
        assertThat(result).isEqualTo(duration);
        then(studySessionWorker).should().upsert(mockMember, status, today, duration);
        then(studyStatusWorker).should().saveStatus(updated);
    }

    @Test
    void endStudy_success_acrossDays() {
        Long userId = 1L;
        LocalDate today = LocalDate.of(2025, 4, 15);
        LocalTime start = LocalTime.of(23, 0);
        LocalTime end = LocalTime.of(2, 0);

        long duration = Duration.between(start, LocalTime.MAX).toSeconds()
                + 1
                + Duration.between(LocalTime.MIN, end).toSeconds();

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studyType(StudyType.PERSONAL)
                .typeId(1L)
                .startTime(start)
                .studying(true)
                .studyTime(100L)
                .build();
        StudyStatus updated = StudyStatus.builder()
                .id(userId)
                .studying(false)
                .studyTime(100L + duration)
                .build();

        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(timeUtils.getToday()).willReturn(today);
        given(studyStatusWorker.findAndDelete(userId)).willReturn(Optional.of(status));
        willDoNothing().given(studyStatusWorker).validStatus(status);
        given(studyStatusWorker.resetStatus(status, duration)).willReturn(updated);

        Long result = studySessionService.endStudy(userId, end);

        assertThat(result).isEqualTo(duration);
        then(studySessionWorker).should().upsert(mockMember, status, today, duration);
        then(studyStatusWorker).should().saveStatus(updated);
    }
}
