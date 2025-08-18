package com.studypals.domain.studyManage.restDocsTest;

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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.studyManage.api.CategoryController;
import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.domain.studyManage.service.StudyCategoryService;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
import com.studypals.testModules.testSupport.RestDocsSupport;

@WebMvcTest(CategoryController.class)
class CategoryControllerRestDocsTest extends RestDocsSupport {

    @MockitoBean
    private StudyCategoryService studyCategoryService;

    @Autowired
    private StudyCategoryReader studyCategoryReader;

    @Test
    @WithMockUser
    void create_success() throws Exception {
        // given
        CreateCategoryReq req = new CreateCategoryReq(null, "알고리즘", DateType.DAILY, 1200L, "#FF5733", 10, "매일 10문제");

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
        UpdateCategoryReq req = new UpdateCategoryReq(1L, DateType.DAILY, "name", 1200L, "#000000", 20, "설명");

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
}
