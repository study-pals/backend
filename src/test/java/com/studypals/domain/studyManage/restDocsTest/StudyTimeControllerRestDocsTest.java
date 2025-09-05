package com.studypals.domain.studyManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.studyManage.api.StudyTimeController;
import com.studypals.domain.studyManage.dto.*;
import com.studypals.domain.studyManage.service.StudyTimeService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(StudyTimeController.class)
class StudyTimeControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private StudyTimeService studyTimeService;

    @Test
    @WithMockUser
    void getStudyTimeByDate_success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 4, 10);

        List<GetStudyDto> response = List.of(
                GetStudyDto.builder().categoryId(1L).time(1200L).build(),
                GetStudyDto.builder().categoryId(1L).time(1200L).build(),
                GetStudyDto.builder().name("temporary").time(1200L).build());

        Response<List<GetStudyDto>> expected =
                CommonResponse.success(ResponseCode.STUDY_TIME_PARTIAL, response, "data of date");

        given(studyTimeService.getStudyList(any(), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(
                get("/studies/stat").param("date", date.toString()).contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        queryParameters(
                                parameterWithName("date").description("조회할 날짜").attributes(constraints("YYYY-MM-DD"))),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data[].categoryId")
                                        .description("카테고리 아이디(영구)")
                                        .optional(),
                                fieldWithPath("data[].name")
                                        .description("카테고리 이름(임시)")
                                        .optional(),
                                fieldWithPath("data[].time").description("공부 시간 (초 단위)"))));
    }

    @Test
    @WithMockUser
    void studiesDateByPeriod_success() throws Exception {
        // given
        List<GetDailyStudyDto> expectedData = List.of(
                GetDailyStudyDto.builder()
                        .studiedDate(LocalDate.of(2024, 4, 1))
                        .studyTimeInfo(List.of(new StudyTimeInfo(1L, null, 60L), new StudyTimeInfo(2L, "some category", 30L)))
                        .build(),
                GetDailyStudyDto.builder()
                        .studiedDate(LocalDate.of(2024, 4, 3))
                        .studyTimeInfo(List.of(new StudyTimeInfo(1L, null, 60L), new StudyTimeInfo(2L, "some category", 30L)))
                        .build());

        Response<List<GetDailyStudyDto>> expectedResponse =
                CommonResponse.success(ResponseCode.STUDY_TIME_ALL, expectedData, "data of study time by period");

        given(studyTimeService.getDailyStudyList(any(), any())).willReturn(expectedData);

        // when
        ResultActions result = mockMvc.perform(get("/studies/stat")
                .param("start", "2024-04-01")
                .param("end", "2024-04-10")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expectedResponse))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        queryParameters(
                                parameterWithName("start").description("조회 시작일 (yyyy-MM-dd)"),
                                parameterWithName("end").description("조회 종료일 (yyyy-MM-dd)")),
                        responseFields(
                                fieldWithPath("code").description("S01-01 고정"),
                                fieldWithPath("status").description("응답 상태 (예: success 또는 fail)"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data[].studiedDate").description("공부한 날짜"),
                                // fieldWithPath("data[].startTime").description("해당 날짜 공부 시작 시간"),
                                // fieldWithPath("data[].endTime").description("해당 날짜 공부 종료 시간"),
                                // fieldWithPath("data[].description").description("간단한 메모"),
                                fieldWithPath("data[].studyList[].categoryId")
                                        .description("카테고리 아이디(영구)")
                                        .optional(),
                                fieldWithPath("data[].studyList[].name")
                                        .description("카테고리 이름 (임시)")
                                        .optional(),
                                fieldWithPath("data[].studyList[].time").description("해당 항목 공부 시간 (초 단위)"))));
    }
}
