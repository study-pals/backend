package com.studypals.domain.chatManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
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
    private Member mockMember;

    @Mock
    private ChatRoom mockChatRoom;

    @Mock
    private ChatRoomMember mockCrm1;

    @Mock
    private ChatRoomMember mockCrm2;

    @InjectMocks
    private ChatRoomServiceImpl chatRoomService;

    @Test
    void getChatRoomInfo_success() {
        // given
        Long userId = 1L;
        String chatRoomId = "chat";
        given(chatRoomReader.getById(chatRoomId)).willReturn(mockChatRoom);
        given(chatRoomReader.findChatRoomMembersWithMember(mockChatRoom)).willReturn(List.of(mockCrm1, mockCrm2));

        given(mockCrm1.getMember()).willReturn(mockMember);
        given(mockCrm2.getMember()).willReturn(mockMember);
        given(mockMember.getId()).willReturn(userId);

        // when
        ChatRoomInfoRes result = chatRoomService.getChatRoomInfo(userId, chatRoomId);

        // then
        assertThat(result.id()).isEqualTo(chatRoomId);
        assertThat(result.userInfos()).hasSize(2);
    }
}
