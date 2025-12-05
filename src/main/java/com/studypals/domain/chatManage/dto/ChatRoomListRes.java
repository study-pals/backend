package com.studypals.domain.chatManage.dto;

import java.util.List;

/**
 * 채팅방 리스트를 반환하기 위해 사용되는 response dto 입니다.
 * @author jack8
 * @since 2025-12-04
 */
public record ChatRoomListRes(List<ChatRoomInfo> rooms) {
    public record ChatRoomInfo(
            String chatRoomId,
            String chatRoomName,
            String chatRoomUrl,
            Long joined,
            Long unread,
            String lastMessage,
            String messageId,
            Long sender) {}
}
