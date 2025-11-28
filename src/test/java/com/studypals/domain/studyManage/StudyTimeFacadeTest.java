package com.studypals.domain.studyManage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dto.*;
import com.studypals.domain.studyManage.facade.StudyTimeFacade;
import com.studypals.domain.studyManage.service.DailyStudyInfoService;
import com.studypals.domain.studyManage.service.StudyTimeService;

@ExtendWith(MockitoExtension.class)
class StudyTimeFacadeTest {

    @Mock
    private StudyTimeService studyTimeService;

    @Mock
    private DailyStudyInfoService dailyStudyInfoService;

    @InjectMocks
    private StudyTimeFacade studyTimeFacade;

    @Test
    void readAndConcatStudyData_success() {
        // given
        Long userId = 1L;
        LocalDate start = LocalDate.of(2025, 8, 1);
        LocalDate end = LocalDate.of(2025, 8, 4);
        LocalTime start1 = LocalTime.of(8, 30);
        LocalTime end1 = LocalTime.of(20, 30);
        LocalTime start2 = LocalTime.of(6, 0);
        LocalTime end2 = LocalTime.of(4, 0);
        PeriodDto periodDto = new PeriodDto(start, end);

        List<GetDailyStudyDto> studyData = List.of(
                GetDailyStudyDto.builder()
                        .studiedDate(start)
                        .studyTimeInfo(List.of(
                                new StudyTimeInfo(1L, null, 3600L),
                                new StudyTimeInfo(2L, null, 7200L),
                                new StudyTimeInfo(null, "study", 1800L)))
                        .build(),
                GetDailyStudyDto.builder()
                        .studiedDate(start.plusDays(2))
                        .studyTimeInfo(
                                List.of(new StudyTimeInfo(1L, null, 1800L), new StudyTimeInfo(null, "work", 1000L)))
                        .build(),
                GetDailyStudyDto.builder()
                        .studiedDate(end)
                        .studyTimeInfo(List.of(new StudyTimeInfo(4L, null, 1800L), new StudyTimeInfo(2L, null, 2000L)))
                        .build());

        List<GetDailyStudyInfoDto> dailyData = List.of(
                new GetDailyStudyInfoDto(start, start1, end1, null),
                new GetDailyStudyInfoDto(start.plusDays(2), start2, end2, "description"),
                new GetDailyStudyInfoDto(end, start1, end2, "des2"));

        List<GetDailyStudyRes> expected = List.of(
                GetDailyStudyRes.builder()
                        .studiedDate(start)
                        .startTime(start1)
                        .endTime(end1)
                        .studies(studyData.get(0).studyTimeInfo())
                        .description(null)
                        .build(),
                GetDailyStudyRes.builder()
                        .studiedDate(start.plusDays(2))
                        .startTime(start2)
                        .endTime(end2)
                        .studies(studyData.get(1).studyTimeInfo())
                        .description("description")
                        .build(),
                GetDailyStudyRes.builder()
                        .studiedDate(end)
                        .startTime(start1)
                        .endTime(end2)
                        .studies(studyData.get(2).studyTimeInfo())
                        .description("des2")
                        .build());
        given(studyTimeService.getDailyStudyList(userId, periodDto)).willReturn(studyData);
        given(dailyStudyInfoService.getDailyStudyInfoList(userId, periodDto)).willReturn(dailyData);

        // when
        List<GetDailyStudyRes> result = studyTimeFacade.readAndConcatStudyData(userId, periodDto);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
