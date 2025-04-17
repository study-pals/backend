package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.worker.StudyTimeReader;
import com.studypals.global.utils.TimeUtils;

/**
 * {@link StudyTimeService} 에 대한 unit test 입니다.
 *
 * @author jack8
 * @since 2025-04-14
 */
@ExtendWith(MockitoExtension.class)
class StudyTimeServiceTest {

    @Mock
    private TimeUtils timeUtils;

    @Mock
    private StudyTimeReader studyTimeReader;

    @Mock
    private StudyTimeMapper mapper;

    @InjectMocks
    private StudyTimeServiceImpl studyTimeService;

    @Mock
    private StudyTime mockStudyTime;

    @Test
    void getStudyList_success_futureDate() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.of(2025, 4, 14);
        LocalDate future = today.plusDays(1);

        given(timeUtils.getToday()).willReturn(today);

        // when
        List<GetStudyDto> result = studyTimeService.getStudyList(userId, future);

        // then
        assertThat(result).isEmpty();
        then(studyTimeReader).shouldHaveNoInteractions();
        then(mapper).shouldHaveNoInteractions();
    }

    @Test
    void getStudyList_success_withData() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.of(2025, 4, 14);
        GetStudyDto dto = new GetStudyDto(1L, null, 3600L);

        given(timeUtils.getToday()).willReturn(today);
        given(studyTimeReader.findByMemberAndDate(userId, today)).willReturn(List.of(mockStudyTime));
        given(mapper.toDto(mockStudyTime)).willReturn(dto);

        // when
        List<GetStudyDto> result = studyTimeService.getStudyList(userId, today);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    void getStudyList_success_emptyData() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.of(2025, 4, 14);

        given(timeUtils.getToday()).willReturn(today);
        given(studyTimeReader.findByMemberAndDate(userId, today)).willReturn(List.of());

        // when
        List<GetStudyDto> result = studyTimeService.getStudyList(userId, today);

        // then
        assertThat(result).isEmpty();
    }
}
