package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.chatManage.api.ChatRoomController;
import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.entity.ChatRoomRole;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.testModules.testSupport.IntegrationSupport;

/**
 * {@link ChatRoomController} 에 대한 통합 테스트
 *
 * @author jack8
 * @since 2025-05-19
 */
@DisplayName("API TEST / 채팅방 정보 통합 테스트")
public class ChatRoomIntegrationTest extends IntegrationSupport {

    @Test
    @DisplayName("GET /chat/room/{chatRoomId}")
    void getRoomInfo_success() throws Exception {
        // given
        String chatRoomId = "chatRoomId";
        CreateUserVar user1 = createUser("user1", "user1");
        CreateUserVar user2 = createUser("user2", "user2");
        CreateUserVar user3 = createUser("user3", "user3");

        String insertChatRoom =
                """
                INSERT INTO chat_room(id, name)
                VALUE(?, ?)
                """;
        jdbcTemplate.update(insertChatRoom, chatRoomId, "chatRoom");
        String insertMember =
                """
                INSERT INTO chat_room_member(chat_room_id, member_id, role)
                VALUE(?, ?, ?)
                """;

        jdbcTemplate.update(insertMember, chatRoomId, user1.getUserId(), "ADMIN");
        jdbcTemplate.update(insertMember, chatRoomId, user2.getUserId(), "MANAGER");
        jdbcTemplate.update(insertMember, chatRoomId, user3.getUserId(), "MEMBER");

        ChatRoomInfoRes responseData = ChatRoomInfoRes.builder()
                .id(chatRoomId)
                .name("chatRoom")
                .userInfos(List.of(
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(user1.getUserId())
                                .role(ChatRoomRole.ADMIN)
                                .build(),
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(user2.getUserId())
                                .role(ChatRoomRole.MANAGER)
                                .build(),
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(user3.getUserId())
                                .role(ChatRoomRole.MEMBER)
                                .build()))
                .build();
        Response<ChatRoomInfoRes> expected =
                CommonResponse.success(ResponseCode.CHAT_ROOM_SEARCH, responseData, chatRoomId);

        // when
        ResultActions result = mockMvc.perform(
                get("/chat/room/{chatRoomId}", chatRoomId).header("Authorization", "Bearer " + user1.getAccessToken()));

        // then
        result.andExpect(status().isOk()).andExpect(hasKey(expected));
    }
}
