package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.studypals.domain.groupManage.worker.GroupStudyStatusWorker;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
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
    private StudyCategory mockStudyCategory;

    @Mock
    private StudyStatus mockStudyStatus;

    @Mock
    private StudyTime mockStudyTime;

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
        given(studyCategoryReader.getById(categoryId)).willReturn(mockStudyCategory);
        given(mockStudyCategory.getGoal()).willReturn(3600L);
        given(mockStudyCategory.getName()).willReturn(null);
        given(mapper.toDto(any(StudyStatus.class))).willReturn(expected);

        // when
        StartStudyRes result = studySessionService.startStudy(userId, req);

        // then
        assertThat(result).isEqualTo(expected);
        then(studyStatusWorker).should().saveStatus(any());
    }

    @Test
    void endStudy_success_acrossDays() {
        TransactionSynchronizationManager.initSynchronization();
        Long userId = 1L;
        LocalDate today = LocalDate.of(2025, 4, 15);
        LocalTime start = LocalTime.of(23, 0);
        LocalTime end = LocalTime.of(2, 0);

        long duration = 3 * 60 * 60;

        given(studyStatusWorker.find(userId)).willReturn(Optional.of(mockStudyStatus));
        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(mockStudyStatus.getStartTime()).willReturn(start);
        given(timeUtils.getToday()).willReturn(today);
        given(studySessionWorker.upsert(mockMember, mockStudyStatus, today, duration))
                .willReturn(mockStudyTime);

        Long result = studySessionService.endStudy(userId, end);

        assertThat(result).isEqualTo(duration);

        TransactionSynchronizationManager.clearSynchronization();
    }
}
