package com.studypals.domain.studyManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.service.StudyCategoryService;
import com.studypals.domain.studyManage.service.StudyTimeService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(StudyTimeController.class)
class StudyTimeControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private StudyTimeService studyTimeService;

    @MockitoBean
    private StudyCategoryService studyCategoryService;

    @Test
    @WithMockUser
    void getStudyTimeByDate_success() throws Exception {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 4, 10);

        List<GetStudyDto> studyList = List.of(new GetStudyDto(1L, null, 120L), new GetStudyDto(null, "임시 카테고리", 60L));

        List<GetCategoryRes> categoryList = List.of(
                GetCategoryRes.builder()
                        .categoryId(1L)
                        .name("자바")
                        .color("#FFCC00")
                        .dayBelong(12)
                        .description("자바 공부")
                        .build(),
                GetCategoryRes.builder()
                        .categoryId(2L)
                        .name("백준")
                        .color("#FFCC00")
                        .dayBelong(12)
                        .description("백준 공부")
                        .build());

        List<GetStudyRes> response = List.of(
                GetStudyRes.builder()
                        .categoryId(1L)
                        .name("자바")
                        .color("#FFCC00")
                        .description("자바 공부")
                        .time(120L)
                        .build(),
                GetStudyRes.builder()
                        .categoryId(2L)
                        .name("백준")
                        .color("#FFCC00")
                        .description("백준 공부")
                        .build(),
                GetStudyRes.builder().temporaryName("임시 카테고리").time(60L).build());

        Response<List<GetStudyRes>> expected =
                CommonResponse.success(ResponseCode.STUDY_TIME_PARTIAL, response, "data of date");

        given(studyTimeService.getStudyList(any(), any())).willReturn(studyList);
        given(studyCategoryService.getUserCategoryByDate(any(), any())).willReturn(categoryList);

        // when
        ResultActions result = mockMvc.perform(get("/studies/{date}", date).contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("code").description("U03-04 고정"),
                                fieldWithPath("status").description("응답 상태 (예: success 또는 fail)"),
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
