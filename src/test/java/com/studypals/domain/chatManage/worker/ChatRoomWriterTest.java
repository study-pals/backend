package com.studypals.domain.chatManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.domain.chatManage.dao.ChatRoomRepository;
import com.studypals.domain.chatManage.dto.CreateChatRoomDto;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;

/**
 * {@link ChatRoomWriter} 에 대한 unit test 입니다.
 *
 * @author jack8
 * @since 2025-05-16
 */
@ExtendWith(MockitoExtension.class)
class ChatRoomWriterTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatRoomMember mockCrm;

    @Mock
    private Member mockMember;

    @Mock
    private ChatRoom mockChatRoom;

    @InjectMocks
    private ChatRoomWriter chatRoomWriter;

    @Test
    void create_success() {
        // given
        CreateChatRoomDto dto = new CreateChatRoomDto("chatRoom", "example.com");
        given(chatRoomRepository.save(any())).willReturn(mockChatRoom);

        // when
        ChatRoom result = chatRoomWriter.create(dto);

        // then
        assertThat(result.getName()).isEqualTo("chatRoom");
        assertThatCode(() -> UUID.fromString(result.getId())).doesNotThrowAnyException();
    }

    @Test
    void leave_fail_adminTryLeave() {
        // given
        Long userId = 1L;
        String chatRoomId = "chatroom";
        given(mockMember.getId()).willReturn(userId);
        given(mockChatRoom.getId()).willReturn(chatRoomId);
        given(chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoomId, userId))
                .willReturn(Optional.of(mockCrm));
        given(mockCrm.isAdmin()).willReturn(true);
        given(chatRoomRepository.decreaseChatMember(chatRoomId)).willReturn(1);

        // when & then
        assertThatThrownBy(() -> chatRoomWriter.leave(mockChatRoom, mockMember))
                .isInstanceOf(ChatException.class)
                .extracting("errorCode")
                .isEqualTo(ChatErrorCode.CHAT_ROOM_ADMIN_LEAVE);
    }
}
