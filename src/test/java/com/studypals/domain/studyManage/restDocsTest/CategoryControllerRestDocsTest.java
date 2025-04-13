package com.studypals.domain.studyManage.restDocsTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
        CreateCategoryReq req = new CreateCategoryReq("알고리즘", "#FF5733", 10, "매일 10문제");
        Response<Long> expected = CommonResponse.success(ResponseCode.STUDY_CATEGORY_ADD, 1L);

        given(studyCategoryService.createCategory(any(), any())).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(post("/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        requestFields(
                                fieldWithPath("name").description("카테고리 이름").attributes(constraints("not blank")),
                                fieldWithPath("color")
                                        .description("카테고리 색상 HEX")
                                        .attributes(constraints("not blank")),
                                fieldWithPath("dayBelong").description("요일 소속값").attributes(constraints("not blank")),
                                fieldWithPath("description").description("설명").attributes(constraints("optional"))),
                        responseFields(
                                fieldWithPath("data").description("생성된 카테고리 ID"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    @WithMockUser
    void update_success() throws Exception {
        // given
        UpdateCategoryReq req = new UpdateCategoryReq(1L, "DB", "#000000", 20, "설명");
        Response<Void> expected = CommonResponse.success(ResponseCode.STUDY_CATEGORY_UPDATE);

        willDoNothing().given(studyCategoryService).updateCategory(any(), any());

        // when
        ResultActions result = mockMvc.perform(
                put("/category").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
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
                        responseFields(
                                fieldWithPath("data")
                                        .description("응답 데이터 (null)")
                                        .optional(),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    @WithMockUser
    void delete_success() throws Exception {
        // given
        Long categoryId = 1L;
        Response<Void> expected = CommonResponse.success(ResponseCode.STUDY_CATEGORY_DELETE);

        willDoNothing().given(studyCategoryService).deleteCategory(any(), eq(categoryId));

        // when
        ResultActions result = mockMvc.perform(delete("/category/{categoryId}", categoryId));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        pathParameters(parameterWithName("categoryId")
                                .description("삭제 카테고리 id")
                                .attributes(constraints("not null"))),
                        responseFields(
                                fieldWithPath("data").description("응답 데이터 (null)"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    @WithMockUser
    void deleteAll_success() throws Exception {
        // given
        Response<Void> expected = CommonResponse.success(ResponseCode.STUDY_CATEGORY_DELETE);

        willDoNothing().given(studyCategoryService).initCategory(any());

        // when
        ResultActions result = mockMvc.perform(delete("/category/all"));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("data").description("응답 데이터 (null)"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }

    @Test
    @WithMockUser
    void read_success() throws Exception {
        // given
        List<GetCategoryRes> list = List.of(
                new GetCategoryRes(1L, "백엔드", "#FFAA00", 12, "Spring 공부"),
                new GetCategoryRes(2L, "알고리즘", "#00CCFF", 14, "문제풀이"));
        Response<List<GetCategoryRes>> expected = CommonResponse.success(ResponseCode.STUDY_CATEGORY_LIST, list);

        given(studyCategoryService.getUserCategory(any())).willReturn(list);

        // when
        ResultActions result = mockMvc.perform(get("/category"));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected))
                .andDo(restDocs.document(
                        httpRequest(),
                        httpResponse(),
                        responseFields(
                                fieldWithPath("data[].categoryId").description("카테고리 ID"),
                                fieldWithPath("data[].name").description("카테고리 이름"),
                                fieldWithPath("data[].color").description("색상 코드"),
                                fieldWithPath("data[].dayBelong").description("요일 소속값"),
                                fieldWithPath("data[].description").description("설명"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("status").description("응답 상태"),
                                fieldWithPath("message").description("응답 메시지"))));
    }
}
