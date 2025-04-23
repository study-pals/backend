package com.studypals.domain.chatManage.dto;

import lombok.Builder;

/**
 * 채팅을 message mapping으로 받을 때 payload에 바인딩되는 dto 입니다. 클라이언트로부터 도착하는 정보입니다.
 *
 * @author jack8
 * @since 2025-04-21
 */
@Builder
public record ChatDto(MessageType type, Long sender, String message) {
    public enum MessageType {
        ENTER,
        TALK,
        LEAVE
    }
}
