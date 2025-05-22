package com.studypals.domain.chatManage.dto.chatDto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 채팅을 message mapping으로 받을 때 payload에 바인딩되는 dto 입니다. 클라이언트로부터 도착하는 정보입니다.
 *
 * @author jack8
 * @since 2025-05-21
 */
@Builder
@Getter
@Setter
public class SendChatDto {
    private String id;
    private String message;

    private MessageType type;
    private Long sender;
    private LocalDateTime timestamp;
}
