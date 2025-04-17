package com.studypals.domain.studyManage.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.service.StudyCategoryService;
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
    private StudyCategoryService studyCategoryService;

    @InjectMocks
    private StudyTimeFacade studyTimeFacade;

    private final Long userId = 1L;
    private final LocalDate date = LocalDate.of(2025, 4, 16);

    @Test
    void getStudyTimeByDate_success_withCategoryAndTemp() {
        // given
        List<GetStudyDto> studyList = List.of(new GetStudyDto(100L, null, 60L), new GetStudyDto(null, "임시공부", 45L));

        List<GetCategoryRes> categoryList = List.of(GetCategoryRes.builder()
                .categoryId(100L)
                .name("수학")
                .color("#123456")
                .dayBelong(12)
                .description("수학 공부")
                .build());

        given(studyTimeService.getStudyList(userId, date)).willReturn(studyList);
        given(studyCategoryService.getUserCategoryByDate(userId, date)).willReturn(categoryList);

        // when
        List<GetStudyRes> result = studyTimeFacade.getStudyTimeByDate(userId, date);

        // then
        assertThat(result).hasSize(2);

        GetStudyRes categoryStudy =
                result.stream().filter(r -> r.categoryId() != null).findFirst().orElseThrow();
        assertThat(categoryStudy.time()).isEqualTo(60L);
        assertThat(categoryStudy.name()).isEqualTo("수학");

        GetStudyRes tempStudy = result.stream()
                .filter(r -> r.temporaryName() != null)
                .findFirst()
                .orElseThrow();
        assertThat(tempStudy.temporaryName()).isEqualTo("임시공부");
        assertThat(tempStudy.time()).isEqualTo(45L);
    }

    @Test
    void getStudyTimeByDate_success_onlyCategory() {
        // given
        List<GetStudyDto> studyList = List.of(new GetStudyDto(100L, null, 120L));

        List<GetCategoryRes> categoryList = List.of(GetCategoryRes.builder()
                .categoryId(100L)
                .name("영어")
                .color("#000000")
                .dayBelong(10)
                .description("영어 공부")
                .build());

        given(studyTimeService.getStudyList(userId, date)).willReturn(studyList);
        given(studyCategoryService.getUserCategoryByDate(userId, date)).willReturn(categoryList);

        // when
        List<GetStudyRes> result = studyTimeFacade.getStudyTimeByDate(userId, date);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).categoryId()).isEqualTo(100L);
        assertThat(result.get(0).temporaryName()).isNull();
        assertThat(result.get(0).time()).isEqualTo(120L);
    }

    @Test
    void getStudyTimeByDate_success_onlyTemp() {
        // given
        List<GetStudyDto> studyList = List.of(new GetStudyDto(null, "기타", 90L));

        List<GetCategoryRes> categoryList = List.of();

        given(studyTimeService.getStudyList(userId, date)).willReturn(studyList);
        given(studyCategoryService.getUserCategoryByDate(userId, date)).willReturn(categoryList);

        // when
        List<GetStudyRes> result = studyTimeFacade.getStudyTimeByDate(userId, date);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).temporaryName()).isEqualTo("기타");
        assertThat(result.get(0).time()).isEqualTo(90L);
    }

    @Test
    void getStudyTimeByDate_success_emptyAll() {
        // given
        given(studyTimeService.getStudyList(userId, date)).willReturn(List.of());
        given(studyCategoryService.getUserCategoryByDate(userId, date)).willReturn(List.of());

        // when
        List<GetStudyRes> result = studyTimeFacade.getStudyTimeByDate(userId, date);

        // then
        assertThat(result).isEmpty();
    }
}
