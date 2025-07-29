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
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.studyManage.api.StudyTimeController;
import com.studypals.domain.studyManage.dto.GetDailyStudyRes;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.dto.StudyList;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.facade.StudyTimeFacade;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(StudyTimeController.class)
class StudyTimeControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private StudyTimeFacade studyTimeFacade;

    @Test
    @WithMockUser
    void getStudyTimeByDate_success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 4, 10);

        List<GetStudyRes> response = List.of(
                GetStudyRes.builder()
                        .studyType(StudyType.PERSONAL)
                        .typeId(1L)
                        .name("자바")
                        .color("#FFCC00")
                        .description("자바 공부")
                        .time(1200L)
                        .goal(120L)
                        .build(),
                GetStudyRes.builder()
                        .studyType(StudyType.TEMPORARY)
                        .color(null)
                        .description(null)
                        .name("백준 공부")
                        .time(5000L)
                        .build(),
                GetStudyRes.builder()
                        .studyType(StudyType.TEMPORARY)
                        .name("임시 카테고리")
                        .time(600L)
                        .build());

        Response<List<GetStudyRes>> expected =
                CommonResponse.success(ResponseCode.STUDY_TIME_PARTIAL, response, "data of date");

        given(studyTimeFacade.getStudyTimeByDate(any(), any())).willReturn(response);

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
                                fieldWithPath("data[].studyType").description("타입 종류"),
                                fieldWithPath("data[].typeId")
                                        .description("해당 타입의 id")
                                        .optional(),
                                fieldWithPath("data[].name")
                                        .description("카테고리 이름")
                                        .optional(),
                                fieldWithPath("data[].name")
                                        .description("카테고리 이름 / 혹은 TEMPORARY 시 임시 카테고리 이름")
                                        .optional(),
                                fieldWithPath("data[].color")
                                        .description("카테고리 색상")
                                        .optional(),
                                fieldWithPath("data[].description")
                                        .description("카테고리 설명")
                                        .optional(),
                                fieldWithPath("data[].time").description("공부 시간 (초 단위)"),
                                fieldWithPath("data[].goal")
                                        .type(JsonFieldType.NUMBER)
                                        .description("공부 목표 (분 단위) / 임시 토픽 -> null")
                                        .optional())));
    }

    @Test
    @WithMockUser
    void studiesDateByPeriod_success() throws Exception {
        // given
        List<GetDailyStudyRes> expectedData = List.of(
                GetDailyStudyRes.builder()
                        .studiedDate(LocalDate.of(2024, 4, 1))
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(11, 0))
                        .memo("집중 잘 됨")
                        .studies(List.of(
                                new StudyList(StudyType.TEMPORARY, null, "알고리즘", 60L),
                                new StudyList(StudyType.PERSONAL, 2L, null, 30L)))
                        .build(),
                GetDailyStudyRes.builder()
                        .studiedDate(LocalDate.of(2024, 4, 3))
                        .startTime(LocalTime.of(11, 0))
                        .endTime(LocalTime.of(13, 0))
                        .memo("집중 잘 됨")
                        .studies(List.of(
                                new StudyList(StudyType.PERSONAL, 1L, null, 60L),
                                new StudyList(StudyType.PERSONAL, 2L, null, 30L),
                                new StudyList(StudyType.TEMPORARY, null, "토익", 30L)))
                        .build());

        Response<List<GetDailyStudyRes>> expectedResponse =
                CommonResponse.success(ResponseCode.STUDY_TIME_ALL, expectedData, "data of study time by period");

        given(studyTimeFacade.getDailyStudyTimeByPeriod(any(), any())).willReturn(expectedData);

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
                                fieldWithPath("data[].startTime").description("해당 날짜 공부 시작 시간"),
                                fieldWithPath("data[].endTime").description("해당 날짜 공부 종료 시간"),
                                fieldWithPath("data[].memo").description("간단한 메모"),
                                fieldWithPath("data[].studies[].studyType").description("타입 종류"),
                                fieldWithPath("data[].studies[].typeId")
                                        .description("해당 타입의 ID (없으면 null)")
                                        .optional(),
                                fieldWithPath("data[].studies[].name")
                                        .description("카테고리 이름 (임시/영구)")
                                        .optional(),
                                fieldWithPath("data[].studies[].time").description("해당 항목 공부 시간 (초 단위)"))));
    }
}
