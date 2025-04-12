package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.IntegrationSupport;

/**
 * {@link com.studypals.domain.studyManage.api.CategoryController CategoryController} 에 대한 통합 테스트입니다.
 *
 * @author jack8
 * @see IntegrationSupport
 * @since 2025-04-12
 */
@ActiveProfiles("test")
@DisplayName("API TEST / 카테고리 통합 테스트")
public class CategoryIntegrationTest extends IntegrationSupport {
    @Test
    @DisplayName("POST /category")
    void create_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        CreateCategoryReq req = new CreateCategoryReq("알고리즘", "#112233", 7, "문제풀이");

        // when
        ResultActions result = mockMvc.perform(post("/category")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.STUDY_CATEGORY_ADD.getCode()))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("PUT /category")
    void update_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        Long categoryId = createCategory(user.getUserId(), "이전 이름");

        UpdateCategoryReq req = new UpdateCategoryReq(categoryId, "새 이름", "#000000", 3, "설명 수정");

        // when
        ResultActions result = mockMvc.perform(put("/category")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isOk()).andExpect(hasKey("code", ResponseCode.STUDY_CATEGORY_UPDATE.getCode()));
    }

    @Test
    @DisplayName("DELETE /category/{categoryId}")
    void delete_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        Long categoryId = createCategory(user.getUserId(), "삭제할 카테고리");

        // when
        ResultActions result = mockMvc.perform(delete("/category/{categoryId}", categoryId)
                .header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isOk()).andExpect(hasKey("code", ResponseCode.STUDY_CATEGORY_DELETE.getCode()));
    }

    @Test
    @DisplayName("DELETE /category/all")
    void deleteAll_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        createCategory(user.getUserId(), "1번");
        createCategory(user.getUserId(), "2번");

        // when
        ResultActions result =
                mockMvc.perform(delete("/category/all").header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isOk()).andExpect(hasKey("code", ResponseCode.STUDY_CATEGORY_DELETE.getCode()));
    }

    @Test
    @DisplayName("GET /category")
    void read_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        createCategory(user.getUserId(), "카테고리1");
        createCategory(user.getUserId(), "카테고리2");

        // when
        ResultActions result =
                mockMvc.perform(get("/category").header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.STUDY_CATEGORY_LIST.getCode()))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ==============================
    // 테스트용 유틸 메서드
    // ==============================
    private Long createCategory(Long userId, String name) {
        String sql =
                """
            INSERT INTO study_category (name, color, day_belong, description, member_id)
            VALUES (?, '#000000', 1, '테스트 설명', ?)
        """;
        jdbcTemplate.update(sql, name, userId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM study_category WHERE name = ? AND member_id = ?", Long.class, name, userId);
    }
}
