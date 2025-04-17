package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
class StudyCategoryWriterTest {

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @InjectMocks
    private StudyCategoryWriter studyCategoryWriter;

    @Mock
    private StudyCategory mockCategory;

    @Test
    void findAndValidate_fail_notOwner() {
        // given
        Long userId = 1L;
        Long categoryId = 10L;
        given(studyCategoryRepository.findById(categoryId)).willReturn(Optional.of(mockCategory));
        given(mockCategory.isOwner(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> studyCategoryWriter.findAndValidate(userId, categoryId))
                .isInstanceOf(StudyException.class)
                .extracting("errorCode")
                .isEqualTo(StudyErrorCode.STUDY_CATEGORY_DELETE_FAIL);
    }
}
