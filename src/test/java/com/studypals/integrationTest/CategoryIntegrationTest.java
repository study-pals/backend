package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.studyManage.dto.CreateCategoryReq;
import com.studypals.domain.studyManage.dto.UpdateCategoryReq;
import com.studypals.domain.studyManage.entity.DateType;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.IntegrationSupport;

/**
 * {@link com.studypals.domain.studyManage.api.CategoryController CategoryController} 에 대한 통합 테스트입니다.
 *
 * @author jack8
 * @see IntegrationSupport
 * @since 2025-04-12
 */
@DisplayName("API TEST / 카테고리 통합 테스트")
public class CategoryIntegrationTest extends IntegrationSupport {

    @Test
    @DisplayName("POST /categories")
    void create_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        CreateCategoryReq req = new CreateCategoryReq(null, "알고리즘", DateType.DAILY, 1200L, "#112233", 7, "문제풀이");

        // when
        ResultActions result = mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/categories/\\d+")));
    }

    @Test
    @DisplayName("PUT /categories")
    void update_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        Long categoryId = createCategory(user.getUserId(), "이전 이름");

        UpdateCategoryReq req = new UpdateCategoryReq(categoryId, DateType.DAILY, "새 이름", 1200L, "#000000", 3, "설명 수정");

        // when
        ResultActions result = mockMvc.perform(put("/categories")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated()).andExpect(header().string("Location", "/categories/" + categoryId));
    }

    @Test
    @DisplayName("DELETE /categories/{categoryId}")
    void delete_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        Long categoryId = createCategory(user.getUserId(), "삭제할 카테고리");

        // when
        ResultActions result = mockMvc.perform(delete("/categories/{categoryId}", categoryId)
                .header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /categories")
    void read_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        createCategory(user.getUserId(), "카테고리1");
        createCategory(user.getUserId(), "카테고리2");

        // when
        ResultActions result =
                mockMvc.perform(get("/categories").header("Authorization", "Bearer " + user.getAccessToken()));

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
            INSERT INTO study_category (study_type, type_id, date_type, name, color, day_belong, description)
            VALUES ('PERSONAL', ?, 'DAILY', ?,'#000000', 127, '테스트 설명')
        """;
        jdbcTemplate.update(sql, userId, name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM study_category WHERE name = ? AND study_type = 'PERSONAL' AND type_id = ?",
                Long.class,
                name,
                userId);
    }
}
