package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

@ExtendWith(MockitoExtension.class)
class StudyCategoryWorkerTest {

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @InjectMocks
    private StudyCategoryWorker studyCategoryWorker;

    @Mock
    private StudyCategory mockCategory1;

    @Mock
    private StudyCategory mockCategory2;

    @Test
    void findCategoryByMemberAndDay_success() {
        // given
        Long userId = 1L;
        int dayBit = 0b0100; // 수요일만 선택된 비트
        given(mockCategory1.getDayBelong()).willReturn(0b0110); // 화, 수 포함
        given(mockCategory2.getDayBelong()).willReturn(0b0001); // 월

        given(studyCategoryRepository.findByMemberId(userId)).willReturn(List.of(mockCategory1, mockCategory2));

        // when
        List<StudyCategory> result = studyCategoryWorker.findCategoryByMemberAndDay(userId, dayBit);

        // then
        assertThat(result).containsExactly(mockCategory1);
    }

    @Test
    void findCategoryAndValidate_fail_notOwner() {
        // given
        Long userId = 1L;
        Long categoryId = 10L;
        given(studyCategoryRepository.findById(categoryId)).willReturn(Optional.of(mockCategory1));
        given(mockCategory1.isOwner(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> studyCategoryWorker.findCategoryAndValidate(userId, categoryId))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_CATEGORY_DELETE_FAIL);
    }
}
