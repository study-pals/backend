package com.studypals.domain.chatManage.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.ChatCursorRes;
import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.dto.mapper.ChatRoomMapper;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;

/**
 *
 * <p><b>상속 정보:</b><br>
 * {@link ChatRoomService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author jack8
 * @see ChatRoomService
 * @since 2025-05-10
 */
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomReader chatRoomReader;
    private final ChatRoomMapper chatRoomMapper;

    @Override
    @Transactional(readOnly = true)
    public ChatRoomInfoRes getChatRoomInfo(Long userId, String chatRoomId, String chatId) {
        ChatRoom chatRoom = chatRoomReader.getById(chatRoomId);

        // Fetch Join 을 통한 1 + N 문제 방지
        List<ChatRoomMember> members = chatRoomReader.findChatRoomMembersWithMember(chatRoom);

        // 유저 권한 검증
        boolean exist = members.stream().anyMatch(m -> m.getMember().getId().equals(userId));
        if (!exist) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_PERMISSION_DENIED, "[ChatRoomService#getChatRoomInfo] not included user");
        }

        Map<Long, String> cursorData = new HashMap<>();
        for (ChatRoomMember chatRoomMember : members) {
            cursorData.put(chatRoomMember.getId(), chatRoomMember.getLastReadMessage());
        }
        cursorData.putAll(chatRoomReader.getCachedCursor(chatRoomId).getLastMessage());

        List<ChatCursorRes> chatCursorRes = cursorData.entrySet().stream()
                .map(t -> new ChatCursorRes(t.getKey(), t.getValue()))
                .toList();

        return ChatRoomInfoRes.builder()
                .id(chatRoomId)
                .name(chatRoom.getName())
                .userInfos(members.stream().map(chatRoomMapper::toDto).toList())
                .cursor(chatCursorRes)
                .build();
    }
}
