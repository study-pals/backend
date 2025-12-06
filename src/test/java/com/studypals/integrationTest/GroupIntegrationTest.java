package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;
import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.global.responses.ResponseCode;

/**
 * {@link com.studypals.domain.groupManage.api.GroupController} 에 대한 통합 테스트 {@link
 * AbstractGroupIntegrationTest} 를 사용하였다.
 *
 * @author s0o0bn
 * @see com.studypals.domain.groupManage.api.GroupController
 * @see AbstractGroupIntegrationTest
 * @since 2025-04-12
 */
@ActiveProfiles("test")
@DisplayName("API TEST / 그룹 관리 통합 테스트")
public class GroupIntegrationTest extends AbstractGroupIntegrationTest {
    @Autowired
    private GroupEntryCodeRedisRepository entryCodeRedisRepository;

    @Test
    @DisplayName("GET /groups/tags")
    void getGroupTags_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        createGroupTag("tag1");
        createGroupTag("tag2");

        // when
        ResultActions result =
                mockMvc.perform(get("/groups/tags").header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.GROUP_TAG_LIST.getCode()))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("POST /groups")
    void createGroup_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        createGroupTag("group tag");
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false, "image.example.com");

        // when
        ResultActions result = mockMvc.perform(post("/groups")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated()).andExpect(header().string("Location", matchesPattern("/groups/\\d+")));
        ;
    }

    private void createGroupTag(String name) {
        String sql = """
            INSERT INTO group_tag (name)
            VALUES (?)
        """;
        jdbcTemplate.update(sql, name);
    }
}
