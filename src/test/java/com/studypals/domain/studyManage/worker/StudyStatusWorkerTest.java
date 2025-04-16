package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dao.StudyStatusRedisRepository;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

@ExtendWith(MockitoExtension.class)
class StudyStatusWorkerTest {

    @Mock
    private StudyStatusRedisRepository studyStatusRedisRepository;

    @InjectMocks
    private StudyStatusWorker studyStatusWorker;

    @Test
    void findStatus_success() {
        // given
        Long userId = 1L;
        StudyStatus status = StudyStatus.builder().id(userId).studyTime(120L).build();
        given(studyStatusRedisRepository.findById(userId)).willReturn(Optional.of(status));

        // when
        StudyStatus result = studyStatusWorker.findStatus(userId);

        // then
        assertThat(result).isEqualTo(status);
    }

    @Test
    void findStatus_success_notExist() {
        // given
        Long userId = 1L;
        given(studyStatusRedisRepository.findById(userId)).willReturn(Optional.empty());

        // when
        StudyStatus result = studyStatusWorker.findStatus(userId);

        // then
        assertThat(result).isNull();
    }

    @Test
    void firstStudyStatus_success() {
        // given
        Long userId = 1L;
        StartStudyReq req = new StartStudyReq(100L, null, LocalTime.of(9, 30));

        // when
        StudyStatus result = studyStatusWorker.firstStudyStatus(userId, req);

        // then
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getStartTime()).isEqualTo(req.startAt());
        assertThat(result.getCategoryId()).isEqualTo(req.categoryId());
        assertThat(result.getTemporaryName()).isEqualTo(req.temporaryName());
        assertThat(result.isStudying()).isTrue();
    }

    @Test
    void resetStudyStatus_success() {
        // given
        StudyStatus original = StudyStatus.builder()
                .id(1L)
                .studyTime(100L)
                .categoryId(10L)
                .temporaryName("test")
                .startTime(LocalTime.of(10, 0))
                .studying(true)
                .build();

        // when
        StudyStatus updated = studyStatusWorker.resetStudyStatus(original, 200L);

        // then
        assertThat(updated.isStudying()).isFalse();
        assertThat(updated.getStudyTime()).isEqualTo(300L);
        assertThat(updated.getCategoryId()).isNull();
        assertThat(updated.getTemporaryName()).isNull();
        assertThat(updated.getStartTime()).isNull();
    }

    @Test
    void restartStudyStatus_success() {
        // given
        StartStudyReq req = new StartStudyReq(null, "focus", LocalTime.of(9, 30));
        StudyStatus current =
                StudyStatus.builder().id(1L).studyTime(120L).studying(false).build();

        // when
        StudyStatus restarted = studyStatusWorker.restartStudyStatus(current, req);

        // then
        assertThat(restarted.isStudying()).isTrue();
        assertThat(restarted.getStartTime()).isEqualTo(req.startAt());
        assertThat(restarted.getCategoryId()).isNull();
        assertThat(restarted.getTemporaryName()).isEqualTo("focus");
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
