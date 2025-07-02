package com.studypals.domain.chatManage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * server -> client 로의 메시지 형식을 정의합니다.
 * incomingMessage 에 대해, 일부 정보를 서버에서 추가하여 반환합니다.
 *
 * @author jack8
 * @see IncomingMessage
 * @since 2025-06-19
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OutgoingMessage {
    private ChatType type;
    private String message;
    private Long senderId;
    private String time;
}
