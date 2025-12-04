package com.studypals.domain.chatManage.dto;

/**
 * 채팅 로그 요청 request dto 입니다.
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.api.ChatController ChatController
 * @since 2025-11-20
 */
public record SendChatLogReq(String room, String chat) {}
