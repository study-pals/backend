package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyStatusRedisRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.dto.StartStudyDto;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;
import com.studypals.global.utils.TimeUtils;

/**
 * {@link StudyTimeService} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2025-04-14
 */
@ExtendWith(MockitoExtension.class)
class StudyTimeServiceTest {

    @Mock
    private StudyStatusRedisRepository studyStatusRepository;

    @Mock
    private StudyTimeRepository studyTimeRepository;

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @Mock
    private StudyTimeMapper mapper;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TimeUtils timeUtils;

    @Mock
    private StudyTime mockStudyTime;

    @Mock
    private StudyCategory mockStudyCategory;

    @Mock
    private Member mockMember;

    @InjectMocks
    private StudyTimeServiceImpl studyTimeService;

    @Test
    void startStudy_success_firstCategory() { // 해당 메서드의 테스트케이스는 mockStudyStatus 를 쓰지 않았다 / 직접적인 값 검증 필요
        // given
        Long userId = 0L;
        Long categoryId = 1L;
        LocalTime time = LocalTime.of(12, 30, 30);
        StartStudyReq dto = new StartStudyReq(categoryId, null, time);
        StartStudyDto ans = new StartStudyDto(true, time, 0L, categoryId, null);

        given(studyStatusRepository.findById(userId)).willReturn(Optional.empty());
        given(mapper.toDto(any())).willReturn(ans);

        // when
        StartStudyDto value = studyTimeService.startStudy(userId, dto);

        // then
        ArgumentCaptor<StudyStatus> captor = ArgumentCaptor.forClass(StudyStatus.class);
        then(studyStatusRepository).should().save(captor.capture());

        StudyStatus savedStatus = captor.getValue();

        assertThat(savedStatus.getId()).isEqualTo(userId);
        assertThat(savedStatus.isStudying()).isTrue();
        assertThat(savedStatus.getStartTime()).isEqualTo(time);
        assertThat(savedStatus.getCategoryId()).isEqualTo(categoryId);
        assertThat(savedStatus.getTemporaryName()).isNull();

        assertThat(value).isNotNull();
    }

    @Test
    void startStudy_success_firstTemporary() {
        // given
        Long userId = 0L;
        String name = "name";
        LocalTime time = LocalTime.of(12, 30, 30);
        StartStudyReq dto = new StartStudyReq(null, name, time);
        StartStudyDto ans = new StartStudyDto(true, time, 0L, null, name);

        given(studyStatusRepository.findById(userId)).willReturn(Optional.empty());
        given(mapper.toDto(any())).willReturn(ans);

        // when
        StartStudyDto value = studyTimeService.startStudy(userId, dto);

        // then
        ArgumentCaptor<StudyStatus> captor = ArgumentCaptor.forClass(StudyStatus.class);
        then(studyStatusRepository).should().save(captor.capture());

        StudyStatus savedStatus = captor.getValue();

        assertThat(savedStatus.getId()).isEqualTo(userId);
        assertThat(savedStatus.isStudying()).isTrue();
        assertThat(savedStatus.getStartTime()).isEqualTo(time);
        assertThat(savedStatus.getCategoryId()).isNull();
        assertThat(savedStatus.getTemporaryName()).isEqualTo(name);

        assertThat(value).isNotNull();
    }

    @Test
    void startStudy_success_exist() {
        // given
        Long userId = 0L;
        Long categoryId = 1L;
        LocalTime time = LocalTime.of(12, 30, 30);
        StartStudyReq dto = new StartStudyReq(categoryId, null, time);
        StudyStatus existStatus = new StudyStatus(userId, false, null, 3400L, null, null, 1L);
        StartStudyDto ans = new StartStudyDto(true, time, 3400L, categoryId, null);

        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(existStatus));
        given(mapper.toDto(any())).willReturn(ans);

        // when
        StartStudyDto value = studyTimeService.startStudy(userId, dto);

        // then
        ArgumentCaptor<StudyStatus> captor = ArgumentCaptor.forClass(StudyStatus.class);
        then(studyStatusRepository).should().save(captor.capture());

        StudyStatus savedStatus = captor.getValue();

        assertThat(savedStatus.getId()).isEqualTo(userId);
        assertThat(savedStatus.isStudying()).isTrue();
        assertThat(savedStatus.getStartTime()).isEqualTo(time);
        assertThat(savedStatus.getStudyTime()).isEqualTo(3400L);
        assertThat(savedStatus.getCategoryId()).isEqualTo(categoryId);
        assertThat(savedStatus.getTemporaryName()).isNull();

        assertThat(value).isNotNull();
    }

    @Test
    void startStudy_success_alreadyStart() {
        // given
        Long userId = 0L;
        Long categoryId = 1L;
        LocalTime afterTime = LocalTime.of(13, 10, 10);
        LocalTime time = LocalTime.of(12, 30, 30);

        StartStudyReq dto = new StartStudyReq(categoryId, null, afterTime);
        StudyStatus existStatus = new StudyStatus(userId, true, time, 3400L, categoryId, null, 1L);
        StartStudyDto ans = new StartStudyDto(true, time, 3400L, categoryId, null);

        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(existStatus));
        given(mapper.toDto(any())).willReturn(ans);

        // when
        StartStudyDto value = studyTimeService.startStudy(userId, dto);

        // then
        ArgumentCaptor<StudyStatus> captor = ArgumentCaptor.forClass(StudyStatus.class);
        then(mapper).should().toDto(captor.capture());

        StudyStatus savedStatus = captor.getValue();

        assertThat(savedStatus.getId()).isEqualTo(userId);
        assertThat(savedStatus.isStudying()).isTrue();
        assertThat(savedStatus.getStartTime()).isEqualTo(time);
        assertThat(savedStatus.getStudyTime()).isEqualTo(3400L);
        assertThat(savedStatus.getCategoryId()).isEqualTo(categoryId);
        assertThat(savedStatus.getTemporaryName()).isNull();

        assertThat(value).isNotNull();
    }

    // 종료 성공 - 시작과 종료가 같은 날에 존재 - 이미 studyTime 안에 해당 데이터가 존재할 때 - 카테고리에 대한 검색
    @Test
    void endStudy_success_inOneDay_alreadyExistStudytime_byCategory() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalTime startAt = LocalTime.of(16, 5, 20);
        LocalTime endedAt = LocalTime.of(20, 30, 0);
        LocalDate today = LocalDate.of(2025, 4, 14);

        Long time = Duration.between(startAt, endedAt).toSeconds();

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .studyTime(3400L)
                .categoryId(categoryId)
                .temporaryName(null)
                .startTime(startAt)
                .build();

        given(timeUtils.getToday()).willReturn(today);
        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(status));
        given(studyTimeRepository.findByMemberIdAndStudiedAtAndCategoryId(userId, today, categoryId))
                .willReturn(Optional.of(mockStudyTime));
        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));

        // when
        Long value = studyTimeService.endStudy(userId, endedAt);

        // then
        ArgumentCaptor<StudyStatus> captor = ArgumentCaptor.forClass(StudyStatus.class);
        then(mockStudyTime).should().addTime(time);
        then(studyStatusRepository).should().save(captor.capture());

        StudyStatus savedStatus = captor.getValue();

        assertThat(value).isEqualTo(time);
        assertThat(savedStatus.isStudying()).isFalse();
        assertThat(savedStatus.getStudyTime()).isEqualTo(3400L + time);
        assertThat(savedStatus.getCategoryId()).isNull();
        assertThat(savedStatus.getTemporaryName()).isNull();
    }

    // 종료 성공 - 시작과 종료가 같은 날에 존재 - 이미 studyTime 안에 해당 데이터가 존재할 때 - 이름에 대한
    @Test
    void endStudy_success_inOneDay_alreadyExistStudytime_byTemporaryName() {
        // given
        Long userId = 1L;
        String name = "algorithm";
        LocalTime startAt = LocalTime.of(16, 5, 20);
        LocalTime endedAt = LocalTime.of(20, 30, 0);
        LocalDate today = LocalDate.of(2025, 4, 14);

        Long time = Duration.between(startAt, endedAt).toSeconds();

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .studyTime(3400L)
                .categoryId(null)
                .temporaryName(name)
                .startTime(startAt)
                .build();

        given(timeUtils.getToday()).willReturn(today);
        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(status));
        given(studyTimeRepository.findByMemberIdAndStudiedAtAndTemporaryName(userId, today, name))
                .willReturn(Optional.of(mockStudyTime));
        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));

        // when
        Long value = studyTimeService.endStudy(userId, endedAt);

        // then
        ArgumentCaptor<StudyStatus> captor = ArgumentCaptor.forClass(StudyStatus.class);
        then(mockStudyTime).should().addTime(time);
        then(studyStatusRepository).should().save(captor.capture());

        StudyStatus savedStatus = captor.getValue();

        assertThat(value).isEqualTo(time);
        assertThat(savedStatus.isStudying()).isFalse();
        assertThat(savedStatus.getStudyTime()).isEqualTo(3400L + time);
        assertThat(savedStatus.getCategoryId()).isNull();
        assertThat(savedStatus.getTemporaryName()).isNull();
    }

    @Test
    void endStudy_success_inOneDay_notExistStudyTime_byCategory() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalTime startAt = LocalTime.of(16, 5, 20);
        LocalTime endedAt = LocalTime.of(20, 30, 0);
        LocalDate today = LocalDate.of(2025, 4, 14);

        Long time = Duration.between(startAt, endedAt).toSeconds();

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .studyTime(3400L)
                .categoryId(categoryId)
                .temporaryName(null)
                .startTime(startAt)
                .build();

        given(timeUtils.getToday()).willReturn(today);
        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(status));
        given(studyTimeRepository.findByMemberIdAndStudiedAtAndCategoryId(userId, today, categoryId))
                .willReturn(Optional.empty());
        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));
        given(studyCategoryRepository.getReferenceById(categoryId)).willReturn(mockStudyCategory);

        // when
        Long value = studyTimeService.endStudy(userId, endedAt);

        // then
        ArgumentCaptor<StudyStatus> statusCaptor = ArgumentCaptor.forClass(StudyStatus.class);
        ArgumentCaptor<StudyTime> timeCaptor = ArgumentCaptor.forClass(StudyTime.class);

        then(studyStatusRepository).should().save(statusCaptor.capture());

        then(studyTimeRepository).should().save(timeCaptor.capture());

        StudyStatus savedStatus = statusCaptor.getValue();
        StudyTime savedTime = timeCaptor.getValue();

        assertThat(value).isEqualTo(time);
        assertThat(savedStatus.isStudying()).isFalse();
        assertThat(savedStatus.getStudyTime()).isEqualTo(3400L + time);
        assertThat(savedStatus.getCategoryId()).isNull();
        assertThat(savedStatus.getTemporaryName()).isNull();

        assertThat(savedTime.getMember()).isEqualTo(mockMember);
        assertThat(savedTime.getCategory()).isEqualTo(mockStudyCategory);
        assertThat(savedTime.getStudiedAt()).isEqualTo(today);
        assertThat(savedTime.getTime()).isEqualTo(time);
    }

    @Test
    void endStudy_success_acrossDays_alreadyExistStudytime_byCategory() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalTime startAt = LocalTime.of(20, 0); // 오후 8시
        LocalTime endedAt = LocalTime.of(2, 0); // 다음날 새벽 2시
        LocalDate today = LocalDate.of(2025, 4, 14);
        Long time = Duration.between(startAt, LocalTime.MIDNIGHT).toSeconds()
                + Duration.between(LocalTime.MIN, endedAt).toSeconds();

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .studyTime(100L)
                .categoryId(categoryId)
                .temporaryName(null)
                .startTime(startAt)
                .build();

        given(timeUtils.getToday()).willReturn(today);
        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(status));
        given(studyTimeRepository.findByMemberIdAndStudiedAtAndCategoryId(userId, today, categoryId))
                .willReturn(Optional.of(mockStudyTime));
        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));

        // when
        Long result = studyTimeService.endStudy(userId, endedAt);

        // then
        then(mockStudyTime).should().addTime(time);
        then(studyStatusRepository).should().save(any());
        assertThat(result).isEqualTo(time);
    }

    @Test
    void endStudy_success_acrossDays_notExistStudytime_byCategory() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalTime startAt = LocalTime.of(21, 30); // 오후 9시 30분
        LocalTime endedAt = LocalTime.of(3, 0); // 다음날 새벽 3시
        LocalDate today = LocalDate.of(2025, 4, 14);
        Long time = Duration.between(startAt, LocalTime.MIDNIGHT).toSeconds()
                + Duration.between(LocalTime.MIN, endedAt).toSeconds();

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .studyTime(500L)
                .categoryId(categoryId)
                .temporaryName(null)
                .startTime(startAt)
                .build();

        given(timeUtils.getToday()).willReturn(today);
        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(status));
        given(studyTimeRepository.findByMemberIdAndStudiedAtAndCategoryId(userId, today, categoryId))
                .willReturn(Optional.empty());
        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));
        given(studyCategoryRepository.getReferenceById(categoryId)).willReturn(mockStudyCategory);

        // when
        Long result = studyTimeService.endStudy(userId, endedAt);

        // then
        then(studyTimeRepository).should().save(any());
        then(studyStatusRepository).should().save(any());
        assertThat(result).isEqualTo(time);
    }

    @Test
    void startStudy_fail_bothCategoryAndNameExist() {
        // given
        Long userId = 1L;
        StartStudyReq dto = new StartStudyReq(1L, "temp", LocalTime.of(10, 0));

        // when & then
        assertThatThrownBy(() -> studyTimeService.startStudy(userId, dto))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_START_FAIL);
    }

    @Test
    void endStudy_fail_statusNotExistInRedis() {
        // given
        Long userId = 1L;
        LocalTime endAt = LocalTime.of(12, 0);

        given(studyStatusRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyTimeService.endStudy(userId, endAt))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);
    }

    @Test
    void endStudy_fail_notStudying() {
        // given
        Long userId = 1L;
        LocalTime endAt = LocalTime.of(12, 0);
        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(false)
                .startTime(LocalTime.of(10, 0))
                .categoryId(1L)
                .build();

        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(status));

        // when & then
        assertThatThrownBy(() -> studyTimeService.endStudy(userId, endAt))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);
    }

    @Test
    void endStudy_fail_startTimeNull() {
        // given
        Long userId = 1L;
        LocalTime endAt = LocalTime.of(12, 0);
        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .startTime(null) // 시작 시간 없음
                .categoryId(1L)
                .build();

        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(status));

        // when & then
        assertThatThrownBy(() -> studyTimeService.endStudy(userId, endAt))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);
    }

    @Test
    void endStudy_fail_bothCategoryAndNameNullInStatus() {
        // given
        Long userId = 1L;
        LocalTime endAt = LocalTime.of(12, 0);
        LocalTime startAt = LocalTime.of(10, 0);
        LocalDate today = LocalDate.of(2025, 4, 14);

        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .studying(true)
                .startTime(startAt)
                .categoryId(null)
                .temporaryName(null)
                .build();

        given(timeUtils.getToday()).willReturn(today);
        given(studyStatusRepository.findById(userId)).willReturn(Optional.of(status));
        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));

        // when & then
        assertThatThrownBy(() -> studyTimeService.endStudy(userId, endAt))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);
    }
}
