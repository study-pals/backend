package com.studypals.domain.chatManage.dto;

/**
 * session 정보를 검증하기 위한 dto 입니다. 세션 id, roomid, userid
 * 정보가 포함되어 있습니다.
 *
 * @author jack8
 * @since 2025-04-22
 */
public record ChatSessionDto(String sessionId, Long userId, String roomId) {}
