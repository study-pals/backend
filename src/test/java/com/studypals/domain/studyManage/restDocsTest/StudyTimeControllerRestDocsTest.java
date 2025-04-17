package com.studypals.domain.studyManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
import com.studypals.domain.studyManage.dto.GetStudyRes;
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
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 4, 10);

        List<GetStudyRes> response = List.of(
                GetStudyRes.builder()
                        .categoryId(1L)
                        .name("자바")
                        .color("#FFCC00")
                        .description("자바 공부")
                        .time(120L)
                        .build(),
                GetStudyRes.builder()
                        .categoryId(null)
                        .name(null)
                        .color(null)
                        .description(null)
                        .temporaryName("백준 공부")
                        .time(500L)
                        .build(),
                GetStudyRes.builder().temporaryName("임시 카테고리").time(60L).build());

        Response<List<GetStudyRes>> expected =
                CommonResponse.success(ResponseCode.STUDY_TIME_PARTIAL, response, "data of date");

        given(studyTimeFacade.getStudyTimeByDate(any(), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/studies/{date}", date).contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(
                                parameterWithName("date").description("조회할 날짜").attributes(constraints("YYYY-MM-DD"))),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data[].categoryId")
                                        .description("카테고리 ID")
                                        .optional(),
                                fieldWithPath("data[].name")
                                        .description("카테고리 이름")
                                        .optional(),
                                fieldWithPath("data[].temporaryName")
                                        .description("임시 카테고리 이름")
                                        .optional(),
                                fieldWithPath("data[].color")
                                        .description("카테고리 색상")
                                        .optional(),
                                fieldWithPath("data[].description")
                                        .description("카테고리 설명")
                                        .optional(),
                                fieldWithPath("data[].time").description("공부 시간 (분 단위)"))));
    }
}
