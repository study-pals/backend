package com.studypals.domain.studyManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.studyManage.api.StudySessionController;
import com.studypals.domain.studyManage.dto.EndStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.service.StudySessionService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

/**
 * {@link StudySessionController} 에 대한 rest docs 테스트입니다.
 *
 * @author jack8
 * @since 2025-04-14
 */
@WebMvcTest(StudySessionController.class)
class StudySessionControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private StudySessionService studySessionService;

    @Test
    @WithMockUser
    void start_success_withCategoryId() throws Exception {
        // given
        StartStudyReq req = new StartStudyReq(1L, null, LocalTime.of(10, 0));

        StartStudyRes res = new StartStudyRes(true, LocalTime.of(10, 0, 0), 0L, null, "some name", 1200L);
        Response<StartStudyRes> expected = CommonResponse.success(ResponseCode.STUDY_START, res, "success start");

        given(studySessionService.startStudy(any(), any())).willReturn(res);

        // when
        ResultActions result = mockMvc.perform(post("/studies/sessions/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("categoryId")
                                        .description("공부를 시작할 카테고리의 아이디")
                                        .attributes(constraints("temporaryName과 상호 베타적")),
                                fieldWithPath("temporaryName")
                                        .description("임시 카테고리 이름")
                                        .attributes(constraints("categoryId 와 상호 베타적")),
                                fieldWithPath("startTime").description("공부 시작 시간 - HH:mm 형식")),
                        responseFields(
                                fieldWithPath("code").description("U03-03 고정"),
                                fieldWithPath("status").description("응답 상태 (예: success 또는 fail)"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data.studying").description("공부 중 여부"),
                                fieldWithPath("data.startTime").description("공부 시작 시간"),
                                fieldWithPath("data.studyTime").description("현재까지 누적 공부 시간"),
                                fieldWithPath("data.categoryId").description("공부 중인 카테고리 아이디"),
                                fieldWithPath("data.name").description("공부 중인 임시 카테고리 이름"),
                                fieldWithPath("data.goal").description("공부 중인 카테고리의 목표 시간"))));
    }

    @Test
    @WithMockUser
    void end_success() throws Exception {
        // given
        EndStudyReq req = new EndStudyReq(LocalTime.of(12, 0));
        Response<Long> expected = CommonResponse.success(ResponseCode.STUDY_START, 300L, "success end");

        given(studySessionService.endStudy(any(), any())).willReturn(300L);

        // when
        ResultActions result = mockMvc.perform(post("/studies/sessions/end")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(fieldWithPath("endTime").description("공부 종료 시간 - HH:mm 형식")),
                        responseFields(
                                fieldWithPath("code").description("U03-03 고정"),
                                fieldWithPath("status").description("응답 상태 (예: success 또는 fail)"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data").description("총 공부 시간(분)"))));
    }
}
