package com.studypals.domain.chatManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.domain.chatManage.dao.ChatRoomRepository;
import com.studypals.domain.chatManage.dto.CreateChatRoomDto;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.domain.chatManage.entity.ChatRoomRole;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.utils.RandomUtils;

/**
 * <br>package name   : com.studypals.domain.chatManage.worker
 * <br>file name      : ChatRoomWriter
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
@RequiredArgsConstructor
@Worker
public class ChatRoomWriter {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public String create(CreateChatRoomDto dto) {
        String chatRoomId = RandomUtils.createUUID();

        ChatRoom chatRoom = ChatRoom.builder().id(chatRoomId).name(dto.name()).build();

        try {
            chatRoomRepository.save(chatRoom);
        } catch (Exception e) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_SAVE_FAIL, "[ChatRoomWriter#createChatRoom]" + e.getMessage());
        }

        return chatRoomId;
    }

    public void joinAsAdmin(ChatRoom chatRoom, Member member) {
        internalJoin(chatRoom, member, ChatRoomRole.ADMIN);
    }

    public void join(ChatRoom chatRoom, Member member) {
        internalJoin(chatRoom, member, ChatRoomRole.MEMBER);
    }

    public void leave(ChatRoom chatRoom, Member member) {
        ChatRoomMember chatRoomMember = chatRoomMemberRepository
                .findByChatRoomIdAndMemberId(chatRoom.getId(), member.getId())
                .orElseThrow(() -> new ChatException(
                        ChatErrorCode.CHAT_ROOM_NOT_FOUND, "[ChatRoomWriter#leave] chatroom-member not found"));

        // This logic (which check if proposer is admin) should place in other worker layer(for example,
        // ChatRoomValidator)
        // but I think we must check this twice.
        if (chatRoomMember.isAdmin()) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_ADMIN_LEAVE, "[ChatRoomWriter#leave] admin try to leave chatRoom");
        }
        chatRoomMemberRepository.delete(chatRoomMember);
    }

    private void internalJoin(ChatRoom chatRoom, Member member, ChatRoomRole roomRole) {
        ChatRoomMember chatRoomMember = ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .member(member)
                .role(roomRole)
                .build();

        try {
            chatRoomMemberRepository.save(chatRoomMember);
        } catch (Exception e) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_JOIN_FAIL, "[ChatRoomWriter#joinAsAdmin]" + e.getMessage());
        }
    }
}
