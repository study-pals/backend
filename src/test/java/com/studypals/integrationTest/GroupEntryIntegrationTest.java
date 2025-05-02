package com.studypals.integrationTest;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;
import com.studypals.domain.groupManage.dto.GroupEntryReq;
import com.studypals.domain.groupManage.entity.GroupEntryCode;

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
