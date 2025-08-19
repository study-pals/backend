package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;

/**
 * {@link StudySessionWorker} 에 대한 테스트
 *
 * @author jack8
 * @since 2025-04-19
 */
@ExtendWith(MockitoExtension.class)
class StudySessionWorkerTest {

    @Mock
    private StudyTimeRepository studyTimeRepository;

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @Mock
    private Member mockMember;

    @Mock
    private StudyStatus mockStatus;

    @Mock
    private StudyTime mockStudyTime;

    @Mock
    private StudyCategory mockStudyCategory;

    @InjectMocks
    private StudySessionWorker studySessionWorker;

    @Test
    void upsert_success_withCategory_studiedBefore() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalDate date = LocalDate.of(2025, 3, 1);
        Long time = 300L;
        given(mockStatus.getCategoryId()).willReturn(categoryId);
        given(mockStatus.getName()).willReturn(null);
        given(mockMember.getId()).willReturn(userId);

        given(studyTimeRepository.findByCategoryAndDate(userId, date, categoryId))
                .willReturn(Optional.of(mockStudyTime));

        // when
        StudyTime studyTime = studySessionWorker.upsert(mockMember, mockStatus, date, time);

        // then
        then(mockMember).should().addToken(time / 60);
        then(mockStudyTime).should().addTime(time);
        then(studyTimeRepository).should().save(mockStudyTime);
        assertThat(studyTime).isEqualTo(mockStudyTime);
    }

    @Test
    void upsert_success_withCategory_firstSaveStudyTime() {
        // given
        Long userId = 1L;
        Long categoryId = 2L;
        LocalDate date = LocalDate.of(2025, 3, 1);
        Long time = 300L;
        given(mockStatus.getCategoryId()).willReturn(categoryId);
        given(mockStatus.getName()).willReturn(null);
        given(mockMember.getId()).willReturn(userId);

        given(studyTimeRepository.findByCategoryAndDate(userId, date, categoryId))
                .willReturn(Optional.empty());
        given(studyCategoryRepository.findById(categoryId)).willReturn(Optional.of(mockStudyCategory));
        given(mockStatus.getGoal()).willReturn(6000L);

        // when
        StudyTime studyTime = studySessionWorker.upsert(mockMember, mockStatus, date, time);

        // then
        then(mockMember).should().addToken(time / 60);
        then(studyTimeRepository).should().save(any());
        assertThat(studyTime.getGoal()).isEqualTo(6000L);
        assertThat(studyTime.getStudyCategory()).isEqualTo(mockStudyCategory);
    }

    @Test
    void upsert_success_withoutCategory_studiedBefore() {
        // given
        Long userId = 1L;
        String name = "name";
        LocalDate date = LocalDate.of(2025, 3, 1);
        Long time = 300L;
        given(mockStatus.getCategoryId()).willReturn(null);
        given(mockStatus.getName()).willReturn(name);
        given(mockMember.getId()).willReturn(userId);

        given(studyTimeRepository.findByMemberIdAndStudiedDateAndName(userId, date, name))
                .willReturn(Optional.of(mockStudyTime));

        // when
        StudyTime studyTime = studySessionWorker.upsert(mockMember, mockStatus, date, time);

        // then
        then(mockMember).should().addToken(time / 60);
        then(mockStudyTime).should().addTime(time);
        then(studyTimeRepository).should().save(mockStudyTime);
        assertThat(studyTime).isEqualTo(mockStudyTime);
    }

    @Test
    void upsert_success_withoutCategory_firstSaveStudyTime() {
        // given
        Long userId = 1L;
        String name = "name";
        LocalDate date = LocalDate.of(2025, 3, 1);
        Long time = 300L;
        given(mockStatus.getCategoryId()).willReturn(null);
        given(mockStatus.getName()).willReturn(name);
        given(mockMember.getId()).willReturn(userId);

        given(studyTimeRepository.findByMemberIdAndStudiedDateAndName(userId, date, name))
                .willReturn(Optional.empty());
        given(mockStatus.getGoal()).willReturn(11000L);

        // when
        StudyTime studyTime = studySessionWorker.upsert(mockMember, mockStatus, date, time);

        // then
        then(mockMember).should().addToken(time / 60);
        then(studyTimeRepository).should().save(any());
        assertThat(studyTime.getName()).isEqualTo(name);
        assertThat(studyTime.getGoal()).isEqualTo(11000L);
    }
}
