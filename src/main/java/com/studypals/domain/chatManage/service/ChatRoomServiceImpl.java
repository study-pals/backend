package com.studypals.domain.chatManage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;

/**
 * <br>package name   : com.studypals.domain.chatManage.service
 * <br>file name      : ChatRoomServiceImpl
 * <br>date           : 5/10/25
 * <pre>
 * <span style="color: white;">[description]</span>
 *
 * </pre>
 * <pre>
 * <span style="color: white;">usage:</span>
 * {@code
 *
 * } </pre>
 */
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomReader chatRoomReader;

    @Override
    public ChatRoomInfoRes getChatRoomInfo(Long userId, String chatRoomId) {
        ChatRoom chatRoom = chatRoomReader.getById(chatRoomId);
        List<ChatRoomMember> members = chatRoomReader.findChatRoomMembersWithMember(chatRoom);

        boolean exist = members.stream().anyMatch(m -> m.getMember().getId().equals(userId));

        if (!exist) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_PERMISSION_DENIED, "[ChatRoomService#getChatRoomInfo] not included user");
        }

        return ChatRoomInfoRes.builder()
                .id(chatRoomId)
                .name(chatRoom.getName())
                .userInfos(members.stream().map(this::toDto).toList())
                .build();
    }

    private ChatRoomInfoRes.UserInfo toDto(ChatRoomMember entity) {
        return ChatRoomInfoRes.UserInfo.builder()
                .userId(entity.getMember().getId())
                .role(entity.getRole())
                .imageUrl(entity.getMember().getImageUrl())
                .build();
    }
}
