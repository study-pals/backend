package com.studypals.integrationTest;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.fixture.GroupFixture;
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
    @DisplayName("POST /groups")
    void createGroup_success() throws Exception {
        // given
        CreateUserVar user = createUser();
        CreateGroupReq req = GroupFixture.createGroupReq();

        // when
        ResultActions result = mockMvc.perform(post("/groups")
                .header("Authorization", "Bearer " + user.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // then
        result.andExpect(status().isCreated()).andExpect(header().string("Location", matchesPattern("/groups/\\d+")));
        ;
    }
}
