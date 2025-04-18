package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import lombok.Builder;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.IntegrationSupport;

/**
 * {@link com.studypals.domain.groupManage.api.GroupController} 에 대한 통합 테스트 {@link
 * IntegrationSupport} 를 사용하였다.
 *
 * @author s0o0bn
 * @see com.studypals.domain.groupManage.api.GroupController
 * @see IntegrationSupport
 * @since 2025-04-12
 */
@ActiveProfiles("test")
@DisplayName("API TEST / 그룹 관리 통합 테스트")
public class GroupIntegrationTest extends IntegrationSupport {

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
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);

        // when
        ResultActions result = mockMvc.perform(post("/groups")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated()).andExpect(header().string("Location", matchesPattern("/groups/\\d+")));
        ;
    }

    @Test
    @DisplayName("POST /groups/:id/entry-code")
    void generateEntryCode_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        CreateGroupVar group = createGroup(user.getUserId(), "group", "tag");

        // when
        ResultActions result = mockMvc.perform(post("/groups/" + group.groupId + "/entry-code")
                .header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/groups/\\d+/entry-code/[A-Z0-9]+")))
                .andExpect(hasKey("code", ResponseCode.GROUP_ENTRY_CODE.getCode()))
                .andExpect(jsonPath("$.data.code").isString());
    }

    private void createGroupTag(String name) {
        String sql = """
            INSERT INTO group_tag (name)
            VALUES (?)
        """;
        jdbcTemplate.update(sql, name);
    }

    private CreateGroupVar createGroup(Long userId, String name, String tag) {
        String insertQuery =
                """
                INSERT INTO `group` (name, tag)
                VALUE(?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, name);
                    ps.setString(2, tag);
                    return ps;
                },
                keyHolder);

        Long groupId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        String memberInsertQuery =
                """
                INSERT INTO group_member (member_id, group_id, role, joined_at)
                VALUE(?, ?, ?, ?)
                """;
        jdbcTemplate.update(memberInsertQuery, userId, groupId, "LEADER", LocalDate.now());

        return CreateGroupVar.builder().groupId(groupId).name(name).tag(tag).build();
    }

    @Builder
    private record CreateGroupVar(Long groupId, String name, String tag) {}
}
