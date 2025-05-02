package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;
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
        GroupEntryCode entryCode = new GroupEntryCode("entry-code", group.groupId());

        entryCodeRedisRepository.save(entryCode);

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
    @WithMockUser
    void joinGroup_success() throws Exception {
        // given
        CreateGroupVar group = createGroup(createUser().getUserId(), "group", "tag", false);
        CreateUserVar user = createUser("member_username", "member");
        GroupEntryCode groupEntryCode = new GroupEntryCode("1A2B3C", group.groupId());
        GroupEntryReq req = new GroupEntryReq(group.groupId(), groupEntryCode.getCode());

        entryCodeRedisRepository.save(groupEntryCode);

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
    @WithMockUser
    void requestParticipant_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        CreateGroupVar group = createGroup(user.getUserId(), "group", "tag");
        GroupEntryCode groupEntryCode = new GroupEntryCode("1A2B3C", group.groupId());
        GroupEntryReq req = new GroupEntryReq(group.groupId(), groupEntryCode.getCode());

        entryCodeRedisRepository.save(groupEntryCode);

        // when
        ResultActions result = mockMvc.perform(post("/groups/request-entry")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/groups/\\d+/requests/\\d+")));
    }
}
