package com.studypals.integrationTest;

import static com.studypals.testModules.testUtils.JsonFieldResultMatcher.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.ResultActions;

import com.studypals.domain.chatManage.api.ChatRoomController;
import com.studypals.domain.chatManage.dto.*;
import com.studypals.domain.chatManage.entity.ChatMessage;
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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
                INSERT INTO chat_room_member(chat_room_id, member_id, role, last_read_message)
                VALUE(?, ?, ?, ?)
                """;

        jdbcTemplate.update(insertMember, chatRoomId, user1.getUserId(), "ADMIN", "11");
        jdbcTemplate.update(insertMember, chatRoomId, user2.getUserId(), "MANAGER", "14");
        jdbcTemplate.update(insertMember, chatRoomId, user3.getUserId(), "MEMBER", null);

        redisTemplate.opsForHash().put("lastRead:" + chatRoomId, user1.getUserId() + "", "15");
        redisTemplate.opsForHash().put("lastRead:" + chatRoomId, user2.getUserId() + "", "14");
        redisTemplate.opsForHash().put("lastRead:" + chatRoomId, user3.getUserId() + "", "15");

        List<MapRecord<String, String, String>> chats = new ArrayList<>();
        String streamKey = "chat:msg:room:" + chatRoomId;
        for (int i = 0; i < 15; i++) {
            Map<String, String> body = new HashMap<>(4);
            RecordId rid = RecordId.of(Long.parseLong((i + 1) + "", 16) + "-0");
            body.put(ChatMessage.FieldName.ID.getName(), rid.getValue());
            body.put(ChatMessage.FieldName.TYPE.getName(), "TEXT");
            body.put(ChatMessage.FieldName.SENDER.getName(), user1.getUserId().toString());
            body.put(ChatMessage.FieldName.MESSAGE.getName(), "message");
            chats.add(StreamRecords.<String, String, String>mapBacked(body)
                    .withStreamKey(streamKey)
                    .withId(rid));
        }
        for (var t : chats) {
            redisTemplate.opsForStream().add(t);
        }

        // cursor: Redis에 넣어준 것 기준
        List<ChatCursorRes> cursor = List.of(
                new ChatCursorRes(user1.getUserId(), "15"),
                new ChatCursorRes(user2.getUserId(), "14"),
                new ChatCursorRes(user3.getUserId(), "15"));

        // logs: id 15 ~ 1, 모두 TEXT / "message" / sender = user1
        List<LoggingMessage> logs = new ArrayList<>();
        for (int i = 15; i >= 1; i--) {
            logs.add(LoggingMessage.builder()
                    .id(String.valueOf(i))
                    .type(ChatType.TEXT)
                    .content("message")
                    .sender(user1.getUserId())
                    .build());
        }

        ChatRoomInfoRes responseData = ChatRoomInfoRes.builder()
                .roomId(chatRoomId)
                .name("chatRoom")
                .userInfos(List.of(
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(user1.getUserId())
                                .role(ChatRoomRole.ADMIN)
                                .imageUrl(null)
                                .build(),
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(user2.getUserId())
                                .role(ChatRoomRole.MANAGER)
                                .imageUrl(null)
                                .build(),
                        ChatRoomInfoRes.UserInfo.builder()
                                .userId(user3.getUserId())
                                .role(ChatRoomRole.MEMBER)
                                .imageUrl(null)
                                .build()))
                .cursor(cursor)
                .logs(logs)
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
