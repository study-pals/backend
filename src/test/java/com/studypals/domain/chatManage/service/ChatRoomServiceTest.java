package com.studypals.domain.chatManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.dto.ChatRoomListRes;
import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.domain.chatManage.dto.mapper.ChatRoomMapper;
import com.studypals.domain.chatManage.entity.*;
import com.studypals.domain.chatManage.worker.ChatMessageReader;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;

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
    private Member mockMember1;

    @Mock
    private Member mockMember2;

    @Mock
    private ChatRoom mockChatRoom;

    @Mock
    private ChatRoomMember mockCrm1;

    @Mock
    private ChatRoomMember mockCrm2;

    @Mock
    private ChatMessageReader chatMessageReader;

    @Mock
    private MemberReader memberReader;

    private ChatRoomServiceImpl chatRoomService;

    private final ChatMessageMapper chatMessageMapper = Mappers.getMapper(ChatMessageMapper.class);

    @BeforeEach
    void setup() {
        chatRoomService = new ChatRoomServiceImpl(
                chatRoomReader, chatRoomMapper, chatMessageMapper, chatMessageReader, memberReader);
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
        given(mockCrm1.getMember()).willReturn(mockMember1);
        given(mockCrm2.getMember()).willReturn(mockMember2);
        given(mockMember1.getId()).willReturn(userId);
        given(mockMember1.getId()).willReturn(2L);

        given(chatRoomMapper.toDto(any()))
                .willReturn(new ChatRoomInfoRes.UserInfo(userId, "nickname", ChatRoomRole.MEMBER, "image"));

        given(mockMember1.getId()).willReturn(userId);
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
        assertThat(result.roomId()).isEqualTo(chatRoomId);
        assertThat(result.userInfos()).hasSize(2);
        assertThat(result.cursor()).hasSize(2);
        assertThat(result.cursor().get(0).chatId()).isEqualTo("2");
        assertThat(result.cursor().get(1).chatId()).isEqualTo("3");
        assertThat(result.logs()).hasSize(4);
    }

    ChatMessage createChat(String id, String roomId) {
        return ChatMessage.builder()
                .id(id)
                .roomId(roomId)
                .type(ChatType.TEXT)
                .sender(1L)
                .content("message")
                .build();
    }

    @Test
    void getChatRoomList_success() {
        // given
        Long userId = 1L;

        ChatRoomListRes.ChatRoomInfo mappedInfo = new ChatRoomListRes.ChatRoomInfo(
                "chat-room",
                "테스트 채팅방",
                "https://example.com/chat-room",
                5,
                10L,
                "message",
                "last_read_message_recent",
                2L);

        given(memberReader.getRef(userId)).willReturn(mockMember1);
        given(chatRoomReader.findChatRoomMembers(mockMember1)).willReturn(List.of(mockCrm1));

        given(mockCrm1.getChatRoom()).willReturn(mockChatRoom);
        given(mockChatRoom.getId()).willReturn("chat-room");
        // 필요하면 이름, URL, 인원 수도 스텁

        // DB 기준 마지막 읽은 메시지
        given(mockCrm1.getLastReadMessage()).willReturn("last_read_message");

        // Redis 에 저장된 더 최신 커서 (키는 chatRoomId 와 같게 맞춘다)
        given(chatRoomReader.getEachUserCursor(eq(userId), eq(List.of("chat-room"))))
                .willReturn(Map.of("chat-room", "last_read_message_recent"));

        // latest info 응답
        given(chatMessageReader.getLatestInfo(any()))
                .willReturn(Map.of(
                        "chat-room",
                        new ChatroomLatestInfo(10L, "last_read_message_recent", ChatType.TEXT, "message", 2L)));

        given(chatRoomMapper.toChatRoomInfo(any(ChatRoomMember.class), any())).willReturn(mappedInfo);

        // when
        ChatRoomListRes result = chatRoomService.getChatRoomList(userId);

        // then 1) chatMessageReader.getLatestInfo 에 넘어간 cursor 검증
        ArgumentCaptor<Map<String, String>> cursorCaptor = ArgumentCaptor.forClass(Map.class);

        then(chatMessageReader).should().getLatestInfo(cursorCaptor.capture());

        Map<String, String> mergedCursor = cursorCaptor.getValue();
        assertThat(mergedCursor).hasSize(1).containsEntry("chat-room", "last_read_message_recent");

        // then 2) 반환 DTO 검증
        assertThat(result.rooms()).hasSize(1);
        ChatRoomListRes.ChatRoomInfo info = result.rooms().get(0);

        assertThat(info.roomId()).isEqualTo("chat-room");
        assertThat(info.name()).isEqualTo("테스트 채팅방");
        assertThat(info.url()).isEqualTo("https://example.com/chat-room");
        assertThat(info.totalMember()).isEqualTo(5);
        assertThat(info.unread()).isEqualTo(10L);
        assertThat(info.content()).isEqualTo("message");
        assertThat(info.chatId()).isEqualTo("last_read_message_recent");
        assertThat(info.sender()).isEqualTo(2L);

        // then 3) 기본 호출 관계 검증 (선택)
        then(memberReader).should().getRef(userId);
        then(chatRoomReader).should().findChatRoomMembers(mockMember1);
        then(chatRoomReader).should().getEachUserCursor(userId, List.of("chat-room"));
    }
}
