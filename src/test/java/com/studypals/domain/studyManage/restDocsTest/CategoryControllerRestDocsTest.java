package com.studypals.domain.studyManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.studyManage.api.CategoryController;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.service.StudyCategoryService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(CategoryController.class)
class CategoryControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private StudyCategoryService studyCategoryService;

    @Test
    @WithMockUser
    void create_success() throws Exception {
        // given
        CreateCategoryReq req = new CreateCategoryReq("알고리즘", 1200L, "#FF5733", 10, "매일 10문제");

        given(studyCategoryService.createCategory(any(), any())).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/categories/1"))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("name").description("카테고리 이름").attributes(constraints("not blank")),
                                fieldWithPath("goal").description("목표 시간"),
                                fieldWithPath("color")
                                        .description("카테고리 색상 HEX")
                                        .attributes(constraints("not blank")),
                                fieldWithPath("dayBelong").description("요일 소속값").attributes(constraints("not blank")),
                                fieldWithPath("description").description("설명").attributes(constraints("optional"))),
                        responseHeaders(headerWithName("Location").description("추가된 카테고리 id"))));
    }

    @Test
    @WithMockUser
    void update_success() throws Exception {
        // given
        UpdateCategoryReq req = new UpdateCategoryReq(1L, "DB", "#000000", 20, "설명");

        given(studyCategoryService.updateCategory(any(), any())).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(put("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/categories/1"))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("categoryId")
                                        .description("카테고리 ID")
                                        .attributes(constraints("not blank")),
                                fieldWithPath("name").description("카테고리 이름").attributes(constraints("not blank")),
                                fieldWithPath("color")
                                        .description("카테고리 색상 HEX")
                                        .attributes(constraints("not blank")),
                                fieldWithPath("dayBelong").description("요일 소속값").attributes(constraints("not blank")),
                                fieldWithPath("description").description("설명").attributes(constraints("optional"))),
                        responseHeaders(headerWithName("Location").description("변경된 cateogory id"))));
    }

    @Test
    @WithMockUser
    void delete_success() throws Exception {
        // given
        Long categoryId = 1L;

        willDoNothing().given(studyCategoryService).deleteCategory(any(), eq(categoryId));

        // when
        ResultActions result = mockMvc.perform(delete("/categories/{categoryId}", categoryId));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("categoryId")
                                .description("삭제 카테고리 id")
                                .attributes(constraints("not null")))));
    }

    @Test
    @WithMockUser
    void deleteAll_success() throws Exception {
        // given
        willDoNothing().given(studyCategoryService).initCategory(any());

        // when
        ResultActions result = mockMvc.perform(delete("/categories/all"));

        // then
        result.andExpect(status().isNoContent()).andDo(restDocs.document(httpRequest(), httpResponse()));
    }

    @Test
    @WithMockUser
    void read_success() throws Exception {
        // given
        List<GetCategoryRes> list = List.of(
                new GetCategoryRes(StudyType.PERSONAL, 1L, "백준", 1200L, "#FFAA00", 12, "Spring 공부"),
                new GetCategoryRes(StudyType.PERSONAL, 2L, "알고리즘", 1200L, "#00CCFF", 14, "문제풀이"));
        Response<List<GetCategoryRes>> expected = CommonResponse.success(ResponseCode.STUDY_CATEGORY_LIST, list);

        given(studyCategoryService.getUserCategory(any())).willReturn(list);

        // when
        ResultActions result = mockMvc.perform(get("/categories"));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("data[].studyType").description("카테고리 테이블 정보(타입)"),
                                fieldWithPath("data[].typeId").description("연관 테이블 타입 ID"),
                                fieldWithPath("data[].name").description("카테고리 이름"),
                                fieldWithPath("data[].goal").description("카테고리 목표 시간"),
                                fieldWithPath("data[].color").description("색상 코드"),
                                fieldWithPath("data[].dayBelong").description("요일 소속값"),
                                fieldWithPath("data[].description").description("설명"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }
}
