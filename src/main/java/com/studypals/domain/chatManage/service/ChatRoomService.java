package com.studypals.domain.chatManage.service;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;

/**
 * 채팅방에 대한 기본적인 조작 로직을 정의한 인터페이스입니다. 다만, 대부분의 로직은 Group 에서 수행됩니다.(생성,삭제, 유저 참여 등)
 * <p>
 * 그룹과 별개의 로직(직접적인 chat room 엔드포인트에서 사용되는 메서드 등)에 대한 정의만 포함됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * ChatRoomServiceImpl 의 부모 인터페이스입니다.
 *
 * @author jack8
 * @see ChatRoomServiceImpl
 * @since 2025-05-10
 */
public interface ChatRoomService {

    /**
     * 채팅방의 정보를 사용자와 채팅방 id를 통해 호출합니다. 사용자의 id는 단순히 해당 채팅방에
     * 해당 사용자가 접근할 권한이 존재하는지를 검증할 때 사용됩니다.
     * <p>
     * 해당 채팅방의 정보와, 채팅방에 속한 유저의 리스트 또한 포함됩니다.
     * @param userId 검증을 위한 요청자의 userId
     * @param chatRoomId 검색하고자 할 채팅방 아이디
     * @param chatId 채팅 내역 불러오기 시 기준이 되는 아이디
     * @return 채팅방 정보 및 해당 채팅방에 소속된 유저의 정보 리스트
     */
    ChatRoomInfoRes getChatRoomInfo(Long userId, String chatRoomId, String chatId);
}
