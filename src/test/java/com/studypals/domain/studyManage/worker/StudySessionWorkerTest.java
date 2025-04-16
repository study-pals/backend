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

import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

@ExtendWith(MockitoExtension.class)
class StudySessionWorkerTest {

    @Mock
    private StudyTimeRepository studyTimeRepository;

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Member mockMember;

    @Mock
    private StudyTime mockStudyTime;

    @Mock
    private StudyCategory mockCategory;

    @InjectMocks
    private StudySessionWorker studySessionWorker;

    @Test
    void upsertStudyTime_success_withCategory() {
        // given
        Long userId = 1L;
        Long categoryId = 100L;
        LocalDate today = LocalDate.now();
        Long time = 300L;
        StudyStatus status = StudyStatus.builder()
                .id(userId)
                .categoryId(categoryId)
                .startTime(LocalTime.of(10, 0))
                .build();

        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));
        given(studyTimeRepository.findByMemberIdAndStudiedAtAndCategoryId(userId, today, categoryId))
                .willReturn(Optional.of(mockStudyTime));

        // when
        studySessionWorker.upsertStudyTime(userId, status, today, time);

        // then
        then(mockStudyTime).should().addTime(time);
        then(mockMember).should().addToken(time / 60);
    }

    @Test
    void upsertStudyTime_success_withTemporaryName() {
        // given
        Long userId = 1L;
        String tempName = "temp-study";
        LocalDate today = LocalDate.now();
        Long time = 150L;
        StudyStatus status =
                StudyStatus.builder().id(userId).temporaryName(tempName).build();

        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));
        given(studyTimeRepository.findByMemberIdAndStudiedAtAndTemporaryName(userId, today, tempName))
                .willReturn(Optional.of(mockStudyTime));

        // when
        studySessionWorker.upsertStudyTime(userId, status, today, time);

        // then
        then(mockStudyTime).should().addTime(time);
        then(mockMember).should().addToken(time / 60);
    }

    @Test
    void upsertStudyTime_fail_bothCategoryAndTempNameNull() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        Long time = 180L;
        StudyStatus status = StudyStatus.builder().id(userId).build(); // both null

        given(memberRepository.findById(userId)).willReturn(Optional.of(mockMember));

        // when & then
        assertThatThrownBy(() -> studySessionWorker.upsertStudyTime(userId, status, today, time))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_TIME_END_FAIL);
    }

    @Test
    void createNewStudyTimeWithCategory_success() {
        // given
        Long categoryId = 10L;
        LocalDate today = LocalDate.now();
        Long time = 240L;

        given(studyCategoryRepository.getReferenceById(categoryId)).willReturn(mockCategory);

        // when
        studySessionWorker.createNewStudyTimeWithCategory(mockMember, categoryId, today, time);

        // then
        then(studyTimeRepository)
                .should()
                .save(argThat(timeEntity -> timeEntity.getCategory() == mockCategory
                        && timeEntity.getMember() == mockMember
                        && timeEntity.getStudiedAt().equals(today)
                        && timeEntity.getTime().equals(time)));
    }

    @Test
    void createNewStudyTimeWithTemporaryName_success() {
        // given
        String tempName = "free study";
        LocalDate today = LocalDate.now();
        Long time = 600L;

        // when
        studySessionWorker.createNewStudyTimeWithTemporaryName(mockMember, tempName, today, time);

        // then
        then(studyTimeRepository)
                .should()
                .save(argThat(timeEntity -> timeEntity.getTemporaryName().equals(tempName)
                        && timeEntity.getMember() == mockMember
                        && timeEntity.getStudiedAt().equals(today)
                        && timeEntity.getTime().equals(time)));
    }
}
