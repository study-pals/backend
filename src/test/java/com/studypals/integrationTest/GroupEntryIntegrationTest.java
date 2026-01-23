package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Objects;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.studypals.domain.groupManage.dao.groupEntryRepository.GroupEntryCodeRedisRepository;
import com.studypals.domain.groupManage.dto.GroupEntryReq;
import com.studypals.domain.groupManage.entity.GroupEntryCode;
import com.studypals.global.responses.ResponseCode;

/**
 * {@link com.studypals.domain.groupManage.api.GroupEntryController} 에 대한 통합 테스트 {@link
 * AbstractGroupIntegrationTest} 를 사용하였다.
 *
 * @author s0o0bn
 * @see com.studypals.domain.groupManage.api.GroupEntryController
 * @see AbstractGroupIntegrationTest
 * @since 2025-04-25
 */
@ActiveProfiles("test")
@DisplayName("API TEST / 그룹 가입 관리 통합 테스트")
@Disabled // 추후 전반적인 리펙토링 예정
public class GroupEntryIntegrationTest extends AbstractGroupIntegrationTest {
    @Autowired
    private GroupEntryCodeRedisRepository entryCodeRedisRepository;

    @Test
    @DisplayName("POST /groups/:id/entry-code")
    void generateEntryCode_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        CreateGroupVar group = createGroup(user.getUserId(), "group", "tag");

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/groups/" + group.groupId() + "/entry-code")
                .header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/groups/\\d+/entry-code/[A-Z0-9]+")))
                .andExpect(hasKey("code", ResponseCode.GROUP_ENTRY_CODE.getCode()))
                .andExpect(jsonPath("$.data.code").isString());
    }

    @Test
    @DisplayName("GET /groups/summary")
    void getGroupSummary_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        CreateGroupVar group = createGroup(user.getUserId(), "group", "tag");
        GroupEntryCode entryCode =
                GroupEntryCode.builder().code("A1B2C3").groupId(group.groupId()).build();

        entryCodeRedisRepository.save(entryCode);
        entryCodeRedisRepository.saveIdx(entryCode);

        // when
        ResultActions result = mockMvc.perform(get("/groups/summary")
                .param("entryCode", entryCode.getCode())
                .header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey("code", ResponseCode.GROUP_SUMMARY.getCode()))
                .andExpect(jsonPath("$.data.id").value(group.groupId()))
                .andExpect(jsonPath("$.data.name").value(group.name()))
                .andExpect(jsonPath("$.data.profiles.length()").value(1));
    }

    @Test
    @DisplayName("POST /groups/join")
    void joinGroup_success() throws Exception {
        // given
        CreateUserVar initUser = createUser();
        CreateGroupVar group = createGroup(initUser.getUserId(), "group", "tag", false);
        CreateUserVar user = createUser("member_username", "member");
        GroupEntryCode groupEntryCode =
                GroupEntryCode.builder().code("A1B2C3").groupId(group.groupId()).build();
        GroupEntryReq req = new GroupEntryReq(group.groupId(), groupEntryCode.getCode());

        entryCodeRedisRepository.save(groupEntryCode);
        entryCodeRedisRepository.saveIdx(groupEntryCode);

        // when
        ResultActions result = mockMvc.perform(post("/groups/join")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/groups/\\d+/members/\\d+")));
    }

    @Test
    @DisplayName("POST /groups/entry-requests")
    void requestParticipant_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        CreateGroupVar group = createGroup(user.getUserId(), "group", "tag");
        GroupEntryCode groupEntryCode =
                GroupEntryCode.builder().code("A1B2C3").groupId(group.groupId()).build();
        GroupEntryReq req = new GroupEntryReq(group.groupId(), groupEntryCode.getCode());

        entryCodeRedisRepository.save(groupEntryCode);
        entryCodeRedisRepository.saveIdx(groupEntryCode);

        // when
        ResultActions result = mockMvc.perform(post("/groups/entry-requests")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/groups/\\d+/requests/\\d+")));
    }

    @Test
    @DisplayName("GET /groups/:groupId/entry-requests")
    void getEntryRequests_success() throws Exception {
        // given
        CreateUserVar leader = createUser("leader", "leader");
        CreateUserVar member1 = createUser("member1", "member1");
        CreateUserVar member2 = createUser("member2", "member2");
        CreateGroupVar group = createGroup(leader.getUserId(), "group", "tag");
        long request1 = createRequest(member1.getUserId(), group.groupId());
        long request2 = createRequest(member2.getUserId(), group.groupId());

        // when
        ResultActions result = mockMvc.perform(get("/groups/{groupId}/entry-requests", group.groupId())
                .header("Authorization", "Bearer " + leader.getAccessToken()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].requestId").value(request1))
                .andExpect(jsonPath("$.data.content[0].member.id").value(member1.getUserId()))
                .andExpect(jsonPath("$.data.content[1].requestId").value(request2))
                .andExpect(jsonPath("$.data.content[1].member.id").value(member2.getUserId()))
                .andExpect(jsonPath("$.data.next").value(request2))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("POST /groups/entry-requests/:requestId/accept")
    void approveEntryRequest_success() throws Exception {
        // given
        CreateUserVar user = createUser("leader", "leader");
        CreateUserVar member = createUser("member", "member");
        CreateGroupVar group = createGroup(user.getUserId(), "group", "tag");
        long requestId = createRequest(member.getUserId(), group.groupId());

        // when
        ResultActions result =
                mockMvc.perform(MockMvcRequestBuilders.post("/groups/entry-requests/{requestId}/accept", requestId)
                        .header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/groups/\\d+/members/\\d+")));
    }

    @Test
    @DisplayName("DELETE /groups/entry-requests/:requestId")
    void refuseEntryRequest_success() throws Exception {
        // given
        CreateUserVar user = createUser("leader", "leader");
        CreateUserVar member = createUser("member", "member");
        CreateGroupVar group = createGroup(user.getUserId(), "group", "tag");
        long requestId = createRequest(member.getUserId(), group.groupId());

        // when
        ResultActions result =
                mockMvc.perform(MockMvcRequestBuilders.delete("/groups/entry-requests/{requestId}", requestId)
                        .header("Authorization", "Bearer " + user.getAccessToken()));

        // then
        result.andExpect(status().isNoContent());
    }

    private long createRequest(Long userId, Long groupId) {
        String sql =
                """
                INSERT INTO group_entry_request(member_id, group_id, created_at)
                VALUES(?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, userId);
                    ps.setLong(2, groupId);
                    ps.setString(3, LocalDate.now().toString());
                    return ps;
                },
                keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }
}
