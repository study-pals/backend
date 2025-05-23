package com.studypals.domain.studyManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.dto.GroupTypeDto;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;

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
        given(studyTimeRepository.findByMemberIdAndStudiedDate(userId, date)).willReturn(List.of(mockTime));

        // when
        List<StudyTime> result = studyTimeReader.getListByMemberAndDate(userId, date);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockTime);
    }

    @Test
    void findByMember_AndDate_success_nothingToReturn() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 1, 1);
        given(studyTimeRepository.findByMemberIdAndStudiedDate(userId, date)).willReturn(List.of());

        // when
        List<StudyTime> result = studyTimeReader.getListByMemberAndDate(userId, date);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getListByGroup_success() {
        // given
        GroupTypeDto groupTypeDto =
                new GroupTypeDto(new PeriodDto(LocalDate.now(), LocalDate.now()), StudyType.GROUP, Set.of(1L));
        StudyTime mockTime = mock(StudyTime.class);
        given(studyTimeRepository.findByStudyTypeBetween(
                        groupTypeDto.period().start(),
                        groupTypeDto.period().end(),
                        groupTypeDto.type().name(),
                        groupTypeDto.ids()))
                .willReturn(List.of(mockTime));

        // when
        List<StudyTime> result = studyTimeReader.getListByGroup(groupTypeDto);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockTime);
    }
}
