package com.studypals.domain.chatManage.dto;

import java.util.List;

/**
 * 채팅 로그를 websocket 으로 보낼 때 사용하는 응답 객체입니다.
 *
 * @author jack8
 * @since 2025-11-20
 */
public record ChatLogRes(String roomId, List<OutgoingMessage> messages) {}
