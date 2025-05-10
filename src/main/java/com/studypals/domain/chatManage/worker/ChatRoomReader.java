package com.studypals.domain.chatManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.domain.chatManage.dao.ChatRoomRepository;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;

/**
 * <br>package name   : com.studypals.domain.chatManage.worker
 * <br>file name      : ChatRoomReader
 * <br>date           : 5/9/25
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
@Worker
@RequiredArgsConstructor
public class ChatRoomReader {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public ChatRoom getById(String chatRoomId) {
        return chatRoomRepository
                .findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND, "in ChatRoomReader#findById"));
    }

    public List<ChatRoomMember> findChatRoomMembers(ChatRoom chatRoom) {
        return chatRoomMemberRepository.findAllByChatRoomId(chatRoom.getId());
    }

    public List<ChatRoomMember> findChatRoomMembersWithMember(ChatRoom chatRoom) {
        return chatRoomMemberRepository.findAllByChatRoomIdWithMember(chatRoom.getId());
    }

    public List<ChatRoomMember> findChatRooms(Member member) {
        return chatRoomMemberRepository.findAllByMemberId(member.getId());
    }

    public Boolean isExistName(String name) {
        return chatRoomRepository.existsByName(name);
    }
}
