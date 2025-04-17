package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;

/**
 * {@link StudyCategoryReader} 에 대한 테스트
 *
 * @author jack8
 * @since 2025-04-17
 */
@ExtendWith(MockitoExtension.class)
class StudyCategoryReaderTest {

    @Mock
    private StudyCategoryRepository studyCategoryRepository;

    @Mock
    private StudyCategory mockCategory1;

    @Mock
    private StudyCategory mockCategory2;

    @InjectMocks
    private StudyCategoryReader studyCategoryReader;

    @Test
    void findByMemberAndDay_success() {
        // given
        Long userId = 1L;
        int dayBit = 0b0100; // 수요일만 선택된 비트
        given(mockCategory1.getDayBelong()).willReturn(0b0110); // 화, 수 포함
        given(mockCategory2.getDayBelong()).willReturn(0b0001); // 월

        given(studyCategoryRepository.findByMemberId(userId)).willReturn(List.of(mockCategory1, mockCategory2));

        // when
        List<StudyCategory> result = studyCategoryReader.findByMemberAndDay(userId, dayBit);

        // then
        assertThat(result).containsExactly(mockCategory1);
    }
}
