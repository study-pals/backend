package com.studypals.domain.studyManage.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dto.*;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.service.DailyStudyInfoService;
import com.studypals.domain.studyManage.service.StudyTimeService;

/**
 * {@link StudyTimeFacade}에 대한 테스트코드
 *
 * @author jack8
 * @since 2025-04-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudyTimeFacade 유닛 테스트")
class StudyTimeFacadeTest {
    @Mock
    private StudyTimeService studyTimeService;

    @Mock
    private DailyStudyInfoService dailyStudyInfoService;

    @InjectMocks
    private StudyTimeFacade studyTimeFacade;

    @Test
    void getDailyStudyTimeByPeriod_success() {
        // given
        Long userId = 1L;
        PeriodDto period = new PeriodDto(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3));

        GetDailyStudyDto study1 = new GetDailyStudyDto(
                LocalDate.of(2025, 5, 1), List.of(new StudyList(StudyType.PERSONAL, 100L, null, 60L)));

        GetDailyStudyDto study2 = new GetDailyStudyDto(
                LocalDate.of(2025, 5, 2), List.of(new StudyList(StudyType.PERSONAL, 101L, null, 30L)));

        GetDailyStudyInfoDto info1 =
                new GetDailyStudyInfoDto(LocalDate.of(2025, 5, 1), LocalTime.of(8, 0), LocalTime.of(10, 0), "공부 열심히 함");

        GetDailyStudyInfoDto info2 =
                new GetDailyStudyInfoDto(LocalDate.of(2025, 5, 2), LocalTime.of(9, 0), LocalTime.of(11, 0), "적당히 함");

        List<GetDailyStudyDto> studyList = List.of(study1, study2);
        List<GetDailyStudyInfoDto> infoList = List.of(info1, info2);

        given(studyTimeService.getDailyStudyList(userId, period)).willReturn(studyList);
        given(dailyStudyInfoService.getDailyStudyInfoList(userId, period)).willReturn(infoList);

        // when
        List<GetDailyStudyRes> result = studyTimeFacade.getDailyStudyTimeByPeriod(userId, period);

        // then
        assertThat(result).hasSize(2);

        GetDailyStudyRes res1 = result.get(0);
        assertThat(res1.studiedDate()).isEqualTo(LocalDate.of(2025, 5, 1));
        assertThat(res1.studies()).hasSize(1);
        assertThat(res1.memo()).isEqualTo("공부 열심히 함");

        GetDailyStudyRes res2 = result.get(1);
        assertThat(res2.studiedDate()).isEqualTo(LocalDate.of(2025, 5, 2));
        assertThat(res2.studies()).hasSize(1);
        assertThat(res2.memo()).isEqualTo("적당히 함");
    }
}
