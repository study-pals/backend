package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dao.DailyStudyInfoRepository;
import com.studypals.domain.studyManage.dao.StudyStatusRedisRepository;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;
import com.studypals.global.utils.TimeUtils;

@ExtendWith(MockitoExtension.class)
class StudyStatusWorkerTest {

    @Mock
    private StudyStatusRedisRepository studyStatusRedisRepository;

    @Mock
    private MemberReader memberReader;

    @Mock
    private DailyStudyInfoRepository dailyStudyInfoRepository;

    @Mock
    private Member mockMember;

    @Mock
    private TimeUtils timeUtils;

    @InjectMocks
    private StudyStatusWorker studyStatusWorker;

    @Test
    void find_AndDelete_success() {
        // given
        Long userId = 1L;
        StudyStatus status = StudyStatus.builder().id(userId).studyTime(120L).build();
        given(studyStatusRedisRepository.findById(userId)).willReturn(Optional.of(status));

        // when
        Optional<StudyStatus> result = studyStatusWorker.findAndDelete(userId);

        // then
        assertThat(result).isPresent().contains(status);
    }

    @Test
    void find_AndDelete_success_notExist() {
        // given
        Long userId = 1L;
        given(studyStatusRedisRepository.findById(userId)).willReturn(Optional.empty());

        // when
        Optional<StudyStatus> result = studyStatusWorker.findAndDelete(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void startStatus_success_firstInDay() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalDate today = LocalDate.of(2025, 1, 1);
        LocalTime time = LocalTime.of(11, 0);
        StartStudyReq req = new StartStudyReq(categoryId, null, time);

        given(mockMember.getId()).willReturn(userId);
        given(timeUtils.getToday()).willReturn(today);
        given(dailyStudyInfoRepository.existsByMemberIdAndStudiedDate(userId, today))
                .willReturn(false);

        // when
        StudyStatus result = studyStatusWorker.startStatus(mockMember, req);

        // then
        then(dailyStudyInfoRepository).should().save(any());
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getStartTime()).isEqualTo(req.startTime());
        assertThat(result.getCategoryId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo(req.temporaryName());
        assertThat(result.isStudying()).isTrue();
    }

    @Test
    void validStatus_fail_whenNull() {
        // given
        StudyStatus status = null;

        // when & then
        assertThatThrownBy(() -> studyStatusWorker.validStatus(status))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);
    }

    @Test
    void validStatus_fail_whenInvalidStudyState() {
        // given
        StudyStatus invalid =
                StudyStatus.builder().id(1L).studying(false).studyTime(60L).build();

        // when & then
        assertThatThrownBy(() -> studyStatusWorker.validStatus(invalid))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);

        then(studyStatusRedisRepository).should().save(any(StudyStatus.class));
    }

    @Test
    void validStatus_fail_whenStartTimeIsNull() {
        // given
        StudyStatus invalid =
                StudyStatus.builder().id(1L).studying(true).startTime(null).build();

        // when & then
        assertThatThrownBy(() -> studyStatusWorker.validStatus(invalid))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);

        then(studyStatusRedisRepository).should().save(any(StudyStatus.class));
    }

    @Test
    void saveStatus_fail_exceptionThrown() {
        // given
        StudyStatus status = StudyStatus.builder().id(1L).build();
        willThrow(new RuntimeException("fail"))
                .given(studyStatusRedisRepository)
                .save(status);

        // when & then
        assertThatThrownBy(() -> studyStatusWorker.saveStatus(status))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);
    }
}
