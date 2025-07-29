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
import com.studypals.domain.studyManage.entity.StudyType;
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
    void find_success() {
        // given
        Long userId = 1L;
        StudyStatus status = StudyStatus.builder().id(userId).studyTime(120L).build();
        given(studyStatusRedisRepository.findById(userId)).willReturn(Optional.of(status));

        // when
        Optional<StudyStatus> result = studyStatusWorker.find(userId);

        // then
        assertThat(result).isPresent().contains(status);
    }

    @Test
    void find_success_notExist() {
        // given
        Long userId = 1L;
        given(studyStatusRedisRepository.findById(userId)).willReturn(Optional.empty());

        // when
        Optional<StudyStatus> result = studyStatusWorker.find(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void firstStatus_success() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.of(2025, 1, 1);
        StartStudyReq req = new StartStudyReq(StudyType.PERSONAL, 1L, null, LocalTime.of(9, 30));
        given(timeUtils.getToday()).willReturn(today);
        given(mockMember.getId()).willReturn(userId);

        // when
        StudyStatus result = studyStatusWorker.firstStatus(mockMember, req);

        // then
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getStartTime()).isEqualTo(req.startTime());
        assertThat(result.getTypeId()).isEqualTo(req.typeId());
        assertThat(result.getName()).isEqualTo(req.temporaryName());
        assertThat(result.isStudying()).isTrue();
    }

    @Test
    void resetStatus_success() {
        // given
        StudyStatus original = StudyStatus.builder()
                .id(1L)
                .studyTime(100L)
                .studyType(StudyType.TEMPORARY)
                .name("test")
                .startTime(LocalTime.of(10, 0))
                .studying(true)
                .build();

        // when
        StudyStatus updated = studyStatusWorker.resetStatus(original, 200L);

        // then
        assertThat(updated.isStudying()).isFalse();
        assertThat(updated.getStudyTime()).isEqualTo(300L);
        assertThat(updated.getName()).isNull();
        assertThat(updated.getStartTime()).isNull();
    }

    @Test
    void restartStatus_success() {
        // given
        StartStudyReq req = new StartStudyReq(StudyType.TEMPORARY, null, "focus", LocalTime.of(9, 30));
        StudyStatus current =
                StudyStatus.builder().id(1L).studyTime(120L).studying(false).build();

        // when
        StudyStatus restarted = studyStatusWorker.restartStatus(current, req);

        // then
        assertThat(restarted.isStudying()).isTrue();
        assertThat(restarted.getStartTime()).isEqualTo(req.startTime());
        assertThat(restarted.getTypeId()).isNull();
        assertThat(restarted.getName()).isEqualTo("focus");
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
