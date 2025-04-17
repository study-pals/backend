package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyTime;

@ExtendWith(MockitoExtension.class)
class StudyTimeReaderTest {

    @Mock
    private StudyTimeRepository studyTimeRepository;

    @InjectMocks
    private StudyTimeReader studyTimeReader;

    @Test
    void findByMember_AndDate_success() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.now();
        StudyTime mockTime = mock(StudyTime.class);
        given(studyTimeRepository.findByMemberIdAndStudiedAt(userId, date)).willReturn(List.of(mockTime));

        // when
        List<StudyTime> result = studyTimeReader.findByMemberAndDate(userId, date);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockTime);
    }

    @Test
    void findByMember_AndDate_success_nothingToReturn() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 1, 1);
        given(studyTimeRepository.findByMemberIdAndStudiedAt(userId, date)).willReturn(List.of());

        // when
        List<StudyTime> result = studyTimeReader.findByMemberAndDate(userId, date);

        // then
        assertThat(result).isEmpty();
    }
}
