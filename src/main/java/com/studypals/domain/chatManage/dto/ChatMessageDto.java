package com.studypals.domain.chatManage.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 채팅 메시지 저장을 위한 dto 입니다.
 * room, user, message 정보가 포함되어 있는 record 클래스입니다.
 *
 * @author jack8
 * @since 2025-04-22
 */
@Builder
public record ChatMessageDto(Long sender, String message, LocalDateTime sendedAt) {}
