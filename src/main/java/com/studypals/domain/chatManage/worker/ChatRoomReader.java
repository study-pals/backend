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
 * 채팅방 정보를 읽어오는 역할을 수행하는 worker 클래스
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author jack8
 * @since 2025-05-10
 */
@Worker
@RequiredArgsConstructor
public class ChatRoomReader {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 채팅방 엔티티를 채팅방 id를 통해 가져옵니다. 예외가 정의되어 있습니다.
     * @param chatRoomId 검색하고자 하는 채팅방 id
     * @return 채팅방 엔티티
     * @throws ChatException CHAT_ROOM_NOT_FOUND / 채팅방을 찾을 수 없는 경우
     */
    public ChatRoom getById(String chatRoomId) {
        return chatRoomRepository
                .findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND, "[ChatRoomReader#findById]"));
    }

    /**
     * 채팅방에 속한 유저에 대한 엔티티인 chatRoomMember 엔티티의 리스트를 반환합니다. fetch join이 아니기에 member 등에 대해 1+N 문제를
     * 일으킬 수 있습니다. 만약 리스트에 포함된 member 의 칼럼에 접근해야 한다면 {@code findChatRoomMembersWithMember} 메서드를 사용해야
     * 합니다.
     * @param chatRoom 유저 정보를 가져올 채팅방 엔티티
     * @return ChatRoomMember 의 리스트
     */
    public List<ChatRoomMember> findChatRoomMembers(ChatRoom chatRoom) {
        return chatRoomMemberRepository.findAllByChatRoomId(chatRoom.getId());
    }

    /**
     * 채팅방에 속한 유저에 대한 엔티티인 chatRoomMember 엔티티의 리스트를 반환합니다. fetch join 메서드를 사용하여 내부 member 를 미리
     * 로딩하였기 때문에, member 엔티티 칼럼에 접근할 필요가 없다면 {@code findChatRoomMembers} 메서드를 사용해야 합니다.
     * @param chatRoom 유저 정보를 가져올 채팅방 엔티티
     * @return ChatRoomMember 의 리스트
     */
    public List<ChatRoomMember> findChatRoomMembersWithMember(ChatRoom chatRoom) {
        return chatRoomMemberRepository.findAllByChatRoomIdWithMember(chatRoom.getId());
    }

    /**
     * 해당 유저가 소속한 chatRoomMember 엔티티 리스트를 반환합니다. 어쩌면 fetch join 타입으로 가져와야 할 수 도 있습니다.
     * @param member 검색할 멤버 엔티티
     * @return chatRoomMember 엔티티 리스트
     */
    public List<ChatRoomMember> findChatRooms(Member member) {
        return chatRoomMemberRepository.findAllByMemberId(member.getId());
    }
}
