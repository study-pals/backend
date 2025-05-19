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
 * 채팅방에 대한 쓰기 작업을 정의해 놓은 worker 클래스
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author jack8
 * @since 2025-05-10
 */
@RequiredArgsConstructor
@Worker
public class ChatRoomWriter {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 채팅방을 생성합니다. 예외를 정의해 두었습니다.
     * @param dto 생성할 채팅방에서 필요한 정보
     * @return 영속화된 채팅방 엔티티
     * @throws ChatException CHAT_ROOM_SAVE_FAIL / 채팅방 저장 실패
     */
    public ChatRoom create(CreateChatRoomDto dto) {
        String chatRoomId = RandomUtils.createUUID();

        ChatRoom chatRoom = ChatRoom.builder().id(chatRoomId).name(dto.name()).build();

        try {
            chatRoomRepository.save(chatRoom);
            return chatRoom;
        } catch (Exception e) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_SAVE_FAIL, "[ChatRoomWriter#createChatRoom]" + e.getMessage());
        }
    }

    /**
     * 채팅방을 생성 시, 생성자(유저)를 해당 채팅방의 admin으로써 등록합니다.
     * @param chatRoom 참가할 채팅방
     * @param member 참가할 멤버
     * @throws ChatException CHAT_ROOM_JOIN_FAIL / 채팅방 참여가 실패
     */
    public void joinAsAdmin(ChatRoom chatRoom, Member member) {
        internalJoin(chatRoom, member, ChatRoomRole.ADMIN);
    }

    /**
     * 일반 유저가 채팅방에 MEMBER 로서 참여합니다.
     * @param chatRoom 참가할 채팅방
     * @param member 참가할 멤버
     * @throws ChatException CHAT_ROOM_JOIN_FAIL / 채팅방 참여가 실패
     */
    public void join(ChatRoom chatRoom, Member member) {
        internalJoin(chatRoom, member, ChatRoomRole.MEMBER);
    }

    /**
     * 채팅방에서 떠납니다. 단, 해당 유저가 ADMIN 인 경우 해당 행위가 불가능하기에 예외를 던집니다.
     * @param chatRoom 떠날 채팅방
     * @param member 떠날 멤버
     * @throws ChatException CHAT_ROOM_NOT_FOUND / 해당 member 가 속한 chatroom 을 찾을 수 없음(속해있지 않거나,id가 잘못됨)
     * @throws ChatException CHAT_ROOM_ADMIN_LEAVE / admin 이 채팅방 탈퇴를 시도하는 경우
     */
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

    /**
     * 채팅방 참여 시의 공통된 로직을 정의합니다.
     * @param chatRoom 채팅방
     * @param member 멤버
     * @param roomRole 어떤 역할로 참가할 것인지
     * @throws ChatException CHAT_ROOM_JOIN_FAIL / 채팅방 참여가 실패
     */
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
