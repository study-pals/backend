package com.studypals.domain.chatManage.dto;

import lombok.*;

/**
 * server -> client 로의 메시지 형식을 정의합니다.
 * OutgoingMessage 에서, roomId 를 제거하여, 그룹화된 채팅 로그 반환 시 필요없는 정보를 제외했습니다.
 *
 * @author jack8
 * @see OutgoingMessage
 * @since 2025-12-10
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoggingMessage {
    private String id;

    private ChatType type;
    private String content;
    private Long sender;
}
