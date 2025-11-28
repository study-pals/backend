package com.studypals.domain.chatManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.domain.chatManage.dto.mapper.ChatRoomMapper;
import com.studypals.domain.chatManage.entity.*;
import com.studypals.domain.chatManage.worker.ChatMessageReader;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.memberManage.entity.Member;

/**
 * {@link ChatRoomService} 에 대한 테스트코드
 *
 * @author jack8
 * @since 2025-05-16
 */
@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomReader chatRoomReader;

    @Mock
    private ChatRoomMapper chatRoomMapper;

    @Mock
    private Member mockMember;

    @Mock
    private ChatRoom mockChatRoom;

    @Mock
    private ChatRoomMember mockCrm1;

    @Mock
    private ChatRoomMember mockCrm2;

    @Mock
    private ChatMessageReader chatMessageReader;

    private ChatRoomServiceImpl chatRoomService;

    private final ChatMessageMapper chatMessageMapper = Mappers.getMapper(ChatMessageMapper.class);

    @BeforeEach
    void setup() {
        chatRoomService = new ChatRoomServiceImpl(chatRoomReader, chatRoomMapper, chatMessageMapper, chatMessageReader);
    }

    @Test
    void getChatRoomInfo_success() {
        // given
        Long userId = 1L;
        String chatRoomId = "chat";
        UserLastReadMessage u = mock(UserLastReadMessage.class);
        given(chatRoomReader.getById(chatRoomId)).willReturn(mockChatRoom);
        given(chatRoomReader.findChatRoomMembersWithMember(mockChatRoom)).willReturn(List.of(mockCrm1, mockCrm2));
        given(mockCrm1.getLastReadMessage()).willReturn("1");
        given(mockCrm2.getLastReadMessage()).willReturn("2");
        given(mockCrm1.getMember()).willReturn(mockMember);

        given(chatRoomMapper.toDto(any()))
                .willReturn(new ChatRoomInfoRes.UserInfo(userId, ChatRoomRole.MEMBER, "image"));

        given(mockMember.getId()).willReturn(userId);
        given(chatRoomReader.getCachedCursor(chatRoomId)).willReturn(u);
        given(u.getLastMessage()).willReturn(Map.of(1L, "3"));

        given(chatMessageReader.getChatLog(chatRoomId, "0"))
                .willReturn(List.of(
                        createChat("0", chatRoomId),
                        createChat("1", chatRoomId),
                        createChat("2", chatRoomId),
                        createChat("3", chatRoomId)));

        // when
        ChatRoomInfoRes result = chatRoomService.getChatRoomInfo(userId, chatRoomId, "0");

        // then
        assertThat(result.id()).isEqualTo(chatRoomId);
        assertThat(result.userInfos()).hasSize(2);
        assertThat(result.cursor()).hasSize(2);
        assertThat(result.cursor().get(0).chatId()).isEqualTo("2");
        assertThat(result.cursor().get(1).chatId()).isEqualTo("3");
        assertThat(result.logs()).hasSize(4);
    }

    ChatMessage createChat(String id, String roomId) {
        return ChatMessage.builder()
                .id(id)
                .room(roomId)
                .type(ChatType.TEXT)
                .sender(1L)
                .message("message")
                .build();
    }
}
