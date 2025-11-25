package com.studypals.domain.chatManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatMessageReactiveRepository;
import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.domain.chatManage.dao.ChatRoomRepository;
import com.studypals.domain.chatManage.dao.UserLastReadMessageRepository;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.domain.chatManage.entity.UserLastReadMessage;
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
    private final ChatMessageReactiveRepository chatMessageReactiveRepository;
    private final UserLastReadMessageRepository userLastReadMessageRepository;

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
    public List<ChatRoomMember> findChatRoomMembers(Member member) {
        return chatRoomMemberRepository.findAllByMemberId(member.getId());
    }

    /**
     * 캐시된 사용자의 메시지 커서 기록을 반환합니다. redis 에 저장된 데이터를 호출하며, 오래된 데이터는 없을 수 있습니다.
     * 따라서, 완전한 데이터를 위해서는 영속화된 데이터와의 비교가 필요합니다.
     * @param roomId 검색할 채팅방 아이디
     * @return 해당 채팅방에 대한 정보
     */
    public UserLastReadMessage getCachedCursor(String roomId) {
        return userLastReadMessageRepository
                .findById(roomId)
                .orElseThrow(() ->
                        new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND, "not cached room or not exist room"));
    }
}
