package com.studypals.domain.studyManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dto.*;
import com.studypals.domain.studyManage.dto.mappers.StudyTimeMapper;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;
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
    private StudyTimeMapper studyTimeMapper;

    @InjectMocks
    private StudyTimeServiceImpl studyTimeService;

    @Mock
    private StudyTime mockStudyTime;

    private StudyTime make(String name, LocalDate studiedDate, Long time) {
        return StudyTime.builder()
                .temporaryName(name)
                .studiedDate(studiedDate)
                .time(time)
                .build();
    }

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
        then(studyTimeMapper).shouldHaveNoInteractions();
    }

    @Test
    void getStudyList_success_withData() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.of(2025, 4, 14);
        GetStudyDto dto = new GetStudyDto(StudyType.PERSONAL, 1L, null, 3600L);

        given(timeUtils.getToday()).willReturn(today);
        given(studyTimeReader.getListByMemberAndDate(userId, today)).willReturn(List.of(mockStudyTime));
        given(studyTimeMapper.toDto(mockStudyTime)).willReturn(dto);

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
        given(studyTimeReader.getListByMemberAndDate(userId, today)).willReturn(List.of());

        // when
        List<GetStudyDto> result = studyTimeService.getStudyList(userId, today);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getDailyStudyList_success() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 4, 14);
        PeriodDto period = new PeriodDto(date, date.plusDays(3));

        List<StudyTime> timeList = List.of(
                make("time1", date, 100L),
                make("time2", date, 200L),
                make("time3", date, 300L),
                make("time4", date.plusDays(1), 100L),
                make("time5", date.plusDays(3), 100L),
                make("time6", date.plusDays(3), 100L));

        given(studyTimeReader.getListByMemberAndDateByPeriod(userId, period)).willReturn(timeList);

        // when
        List<GetDailyStudyDto> results = studyTimeService.getDailyStudyList(userId, period);

        // then
        assertThat(results).hasSize(3);
        assertThat(results)
                .filteredOn(r -> r.studiedDate().equals(date))
                .singleElement()
                .extracting(GetDailyStudyDto::studyList)
                .asInstanceOf(LIST)
                .hasSize(3);

        assertThat(results)
                .filteredOn(r -> r.studiedDate().equals(date.plusDays(1)))
                .singleElement()
                .extracting(GetDailyStudyDto::studyList)
                .asInstanceOf(LIST)
                .hasSize(1);

        assertThat(results)
                .filteredOn(r -> r.studiedDate().equals(date.plusDays(3)))
                .singleElement()
                .extracting(GetDailyStudyDto::studyList)
                .asInstanceOf(LIST)
                .hasSize(2);
    }

    @Test
    void getStudyListOfGroup_success() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 21);
        GroupTypeDto groupTypeDto = new GroupTypeDto(new PeriodDto(date, date), StudyType.GROUP, Set.of(1L));

        List<StudyTime> studyTimes =
                List.of(make("time1", date, 100L), make("time2", date, 200L), make("time3", date, 300L));

        given(studyTimeReader.getListByGroup(groupTypeDto)).willReturn(studyTimes);

        // when
        List<GetStudyOfMemberDto> actual = studyTimeService.getStudyListOfGroup(groupTypeDto);

        // then
        assertThat(actual).hasSize(3);
    }
}
