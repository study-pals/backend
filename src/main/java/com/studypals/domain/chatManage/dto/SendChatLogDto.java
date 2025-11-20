package com.studypals.domain.chatManage.dto;

/**
 * 채팅 내역 전송 요청 시 사용하는 dto 입니다.
 *
 * @author jack8
 * @since 2025-11-18
 */
public record SendChatLogDto(String sessionId, String roomId, String chatId) {}
