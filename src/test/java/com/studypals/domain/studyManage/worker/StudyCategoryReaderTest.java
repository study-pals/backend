package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;

/**
 * {@link StudyCategoryReader} 에 대한 단위 테스트입니다.
 *
 * @author jack8
 * @see StudyCategoryReader
 * @since 2025-08-15
 */
@ExtendWith(MockitoExtension.class)
class StudyCategoryReaderTest {

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @Mock
    private StudyCategory mockStudyCategory;

    @InjectMocks
    private StudyCategoryReader studyCategoryReader;

    @Test
    void getById_success() {
        // given
        Long id = 1L;
        given(studyCategoryRepository.findById(id)).willReturn(Optional.of(mockStudyCategory));

        // when
        StudyCategory category = studyCategoryReader.getById(id);

        // then
        assertThat(category).isEqualTo(mockStudyCategory);
    }
}
